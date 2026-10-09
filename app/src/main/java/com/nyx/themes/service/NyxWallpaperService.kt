package com.nyx.themes.service

import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.nyx.themes.scene.SceneMeta
import com.nyx.themes.scene.SceneRenderer

/**
 * The Nyx live wallpaper. Plays still, live and long scenes, and swaps scenes on unlock / on a timer (see [SceneController]).
 * All drawing happens on a private render thread.
 */
class NyxWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = NyxEngine()

    private inner class NyxEngine : Engine(), SceneController.Listener {
        private val controller = SceneController.get(this@NyxWallpaperService)
        private val thread = HandlerThread("nyx-render").apply { start() }
        private val handler = Handler(thread.looper)
        private val lock = Any()
        private val power = getSystemService(POWER_SERVICE) as PowerManager

        // surface state: guarded by [lock]
        private var surface: SurfaceHolder? = null

        // render-thread state
        private var width = 0
        private var height = 0
        private var renderer: SceneRenderer? = null
        private var previous: SceneRenderer? = null
        private var previousTime = 0f
        private var fadeStart = 0L
        private var sceneStart = SystemClock.uptimeMillis()
        @Volatile private var visible = false
        @Volatile private var destroyed = false
        private var lastTick = 0L
        private var failures = 0
        private var level = 0
        private var slowFrames = 0
        private var requested = 0
        private var frameMs = 0L
        private val full: Pair<Int, Int> = resources.displayMetrics.let { minOf(it.widthPixels, it.heightPixels) to maxOf(it.widthPixels, it.heightPixels) }

        private fun targetW(): Int = minOf(full.first, WIDTHS[maxOf(controller.prefs.quality, level).coerceIn(0, WIDTHS.size - 1)])

        /** keeps the drawing surface at the quality's size (the compositor scales it up). true = a resize was requested */
        private fun applySize(holder: SurfaceHolder, w: Int, h: Int): Boolean {
            val tw = targetW()
            val th = (full.second.toLong() * tw / full.first).toInt()
            if ((w == tw && h == th) || requested == tw) return false
            requested = tw
            return try { holder.setFixedSize(tw, th); true } catch (e: Exception) { Log.w(TAG, "setFixedSize failed", e); false }
        }

        /** measured frame cost too high for a smooth wallpaper: step the resolution down */
        private fun noteFrame(ms: Long) {
            frameMs = (frameMs * 7 + ms) / 8
            if (frameMs > SLOW_MS) slowFrames++ else slowFrames = 0
            Diag.frameMs = frameMs
            val eff = maxOf(controller.prefs.quality, level)
            if (slowFrames > 45 && eff < WIDTHS.size - 1) {
                slowFrames = 0; level = eff + 1; frameMs = 0
                Diag.error = "slow frames: lowered quality to ${WIDTHS[level]}px"
                val h = synchronized(lock) { surface }
                if (h != null) applySize(h, width, height)
            }
        }
        private var hwFailures = 0
        private var frames = 0
        private var fpsStart = SystemClock.uptimeMillis()

        private val frame = object : Runnable {
            override fun run() {
                if (destroyed) return
                val t0 = SystemClock.uptimeMillis()
                try {
                    draw()
                    failures = 0
                    noteFrame(SystemClock.uptimeMillis() - t0)
                } catch (e: Throwable) {
                    // never let a drawing error kill the render thread; give up on this scene after a few tries
                    Log.e(TAG, "draw failed", e)
                    if (++failures >= 5) { Log.e(TAG, "too many draw failures, falling back to the first scene"); failures = 0; recover(); return }
                }
                if (wantsLoop()) {
                    val interval = 1000L / controller.prefs.fps
                    handler.postDelayed(this, (interval - (SystemClock.uptimeMillis() - t0)).coerceAtLeast(1L))
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            controller.attach(this)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            synchronized(lock) { surface = holder }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            synchronized(lock) { surface = holder }
            applySize(holder, w, h)
            handler.post {
                Diag.surface = "${w}x$h"
                if (w != width || h != height) {
                    width = w; height = h
                    releaseRenderers()
                }
                ensureRenderer()
                restartLoop()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            synchronized(lock) { surface = null }
            handler.removeCallbacks(frame)
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                controller.onVisible()
                synchronized(lock) { surface }?.let { if (width > 0) applySize(it, width, height) }
                handler.post { ensureRenderer(); restartLoop() }
            } else {
                handler.removeCallbacks(frame)
            }
        }

        override fun onSceneChanged(scene: SceneMeta, animate: Boolean) {
            handler.post { swapScene(scene, animate); restartLoop() }
        }

        override fun onDestroy() {
            destroyed = true
            controller.detach(this)
            handler.removeCallbacksAndMessages(null)
            synchronized(lock) { surface = null }
            handler.post { releaseRenderers(); thread.quitSafely() }
            super.onDestroy()
        }

        // ---- render thread ----
        private fun ensureRenderer() {
            if (renderer != null || width <= 0 || height <= 0) return
            renderer = build(controller.current())
            sceneStart = SystemClock.uptimeMillis()
        }

        private fun build(scene: SceneMeta): SceneRenderer? = try {
            SceneRenderer(applicationContext, scene, width, height)
        } catch (e: Throwable) {
            Diag.error = "build ${scene.id}: $e"
            Log.e(TAG, "cannot build ${scene.id}", e)
            null
        }

        private fun swapScene(scene: SceneMeta, animate: Boolean) {
            if (width <= 0 || height <= 0) return
            if (renderer?.scene?.id == scene.id) return
            val fresh = build(scene) ?: return
            previous?.release()
            previous = if (animate) renderer else { renderer?.release(); null }
            previousTime = timeSec()
            renderer = fresh
            sceneStart = SystemClock.uptimeMillis()
            fadeStart = SystemClock.uptimeMillis()
        }

        /** last resort after repeated draw errors: drop renderers and rebuild from the first scene */
        private fun recover() {
            releaseRenderers()
            val first = controller.repo.first()
            renderer = build(first)
            sceneStart = SystemClock.uptimeMillis()
            if (visible) handler.postDelayed(frame, 200)
        }

        private fun releaseRenderers() {
            renderer?.release(); renderer = null
            previous?.release(); previous = null
        }

        private fun animationsOn(): Boolean { val on = !(controller.prefs.pauseOnBatterySaver && power.isPowerSaveMode); Diag.saver = power.isPowerSaveMode; return on }

        private fun fading() = previous != null && SystemClock.uptimeMillis() - fadeStart < FADE_MS

        private fun wantsLoop(): Boolean {
            if (!visible || destroyed) return false
            val r = renderer ?: return false
            return fading() || (r.scene.isAnimated && animationsOn())
        }

        private fun timeSec(): Float = (SystemClock.uptimeMillis() - sceneStart) / 1000f

        private fun restartLoop() {
            handler.removeCallbacks(frame)
            if (visible) handler.post(frame)
        }

        private fun draw() {
            val now = SystemClock.uptimeMillis()
            if (now - lastTick > 1000) { lastTick = now; controller.tick() }
            if (previous != null && now - fadeStart >= FADE_MS) { previous?.release(); previous = null }
            synchronized(lock) {
                val holder = surface ?: return
                val useHw = Build.VERSION.SDK_INT >= 26 && hwFailures < 3 && controller.prefs.renderMode == 0
                val canvas = try {
                    if (useHw) holder.lockHardwareCanvas() else holder.lockCanvas()
                } catch (e: Throwable) {
                    if (useHw) { hwFailures++; Diag.error = "hw canvas: ${e.message}" }
                    try { holder.lockCanvas() } catch (e2: Throwable) { null }
                } ?: return
                Diag.mode = if (canvas.isHardwareAccelerated) "hardware" else "software"
                try {
                    val r = renderer
                    if (r == null) { canvas.drawColor(Color.rgb(8, 11, 30)); ensureRenderer(); return }
                    val t = if (r.scene.isAnimated && !animationsOn()) r.scene.poster else timeSec()
                    paint(canvas, r, previous, t, now)
                    debugLastSceneId = r.scene.id
                    debugFrames++
                    if (++frames >= 30) { val n = SystemClock.uptimeMillis(); Diag.fps = frames * 1000f / (n - fpsStart); frames = 0; fpsStart = n }
                    Diag.scene = r.scene.id
                } finally {
                    try { holder.unlockCanvasAndPost(canvas) } catch (e: Exception) { Log.w(TAG, "unlock failed", e) }
                }
            }
        }

        private fun paint(canvas: Canvas, r: SceneRenderer, prev: SceneRenderer?, t: Float, now: Long) {
            if (prev != null) {
                prev.draw(canvas, previousTime)
                val a = ((now - fadeStart).toFloat() / FADE_MS).coerceIn(0f, 1f)
                val save = canvas.saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(), (a * 255).toInt())
                r.draw(canvas, t)
                canvas.restoreToCount(save)
            } else {
                r.draw(canvas, t)
            }
        }
    }

    companion object {
        private const val TAG = "NyxWallpaper"
        private const val FADE_MS = 700L
        private val WIDTHS = intArrayOf(810, 630, 480, 360)
        private const val SLOW_MS = 40L
        /** last scene an engine drew and how many frames were drawn (read by tests and handy for debugging) */
        @Volatile var debugLastSceneId: String? = null
        @Volatile var debugFrames: Int = 0
    }
}

/** tiny status board shown in Settings > Diagnostics */
object Diag {
    @Volatile var mode = "-"
    @Volatile var surface = "-"
    @Volatile var fps = 0f
    @Volatile var scene = "-"
    @Volatile var error = ""
    @Volatile var saver = false
    @Volatile var frameMs = 0L
}
