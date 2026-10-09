package com.nyx.themes.ui

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.nyx.themes.scene.SceneMeta
import com.nyx.themes.scene.SceneRenderer

/** Plays a scene full-bleed inside the app (software layer: the effects use blend modes). */
class ScenePreviewView(context: Context) : View(context) {
    private var renderer: SceneRenderer? = null
    private var start = SystemClock.uptimeMillis()
    private var attached = false
    var scene: SceneMeta? = null
        set(value) { field = value; rebuild() }
    var fixedTime: Float? = null

    private fun rebuild() {
        renderer?.release(); renderer = null
        val s = scene ?: return
        if (width <= 0 || height <= 0) return
        renderer = try { SceneRenderer(context, s, width, height) } catch (e: Throwable) { Log.e("NyxPreview", "build failed", e); null }
        start = SystemClock.uptimeMillis()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { super.onSizeChanged(w, h, oldw, oldh); rebuild() }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); attached = true; invalidate() }
    override fun onDetachedFromWindow() { attached = false; renderer?.release(); renderer = null; super.onDetachedFromWindow() }

    override fun onDraw(canvas: Canvas) {
        val r = renderer ?: run { canvas.drawColor(Ui.BG); return }
        val t = fixedTime ?: ((SystemClock.uptimeMillis() - start) / 1000f)
        r.draw(canvas, t)
        if (attached && fixedTime == null && r.scene.isAnimated) postInvalidateDelayed(33)
    }
}
