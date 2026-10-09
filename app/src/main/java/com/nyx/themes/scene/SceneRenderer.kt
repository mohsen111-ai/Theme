package com.nyx.themes.scene

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Draws one scene at a given target size: sky frames (long scenes) -> back effects -> background image -> front effects.
 * The background is pre-scaled once to the "cover" size so each frame is a 1:1 blit. Not thread safe.
 */
class SceneRenderer(context: Context, val scene: SceneMeta, val width: Int, val height: Int) {
    private val scale = max(width / LW, height / LH)
    private val dw = (LW * scale).roundToInt()
    private val dh = (LH * scale).roundToInt()
    private val ox = (width - dw) / 2f
    private val oy = (height - dh) / 2f
    private val gfx = Gfx()
    private val fx: List<Fx> = List(scene.fx.length()) { FxFactory.create(scene.fx.getJSONObject(it)) }
    private val back = fx.filter { it.z == 0 }
    private val front = fx.filter { it.z != 0 }
    private val bg: Bitmap
    private val sky: List<Pair<Float, Bitmap>>
    private val bmpPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val alphaPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val dst = RectF(0f, 0f, dw.toFloat(), dh.toFloat())

    init {
        // decode close to the target size, then scale once so per-frame drawing is 1:1
        bg = fit(SceneRepo.decodeAsset(context, scene.bg, dw))
        // sky frames are smooth: keep them at half resolution (RGB_565) and let drawBitmap scale them up
        sky = scene.sky.map { (p, img) -> p to SceneRepo.decodeAsset(context, img, dw / 2, Bitmap.Config.RGB_565) }
    }

    private fun fit(src: Bitmap): Bitmap {
        if (src.width == dw && src.height == dh) return src
        val out = Bitmap.createScaledBitmap(src, dw, dh, true)
        if (out !== src) src.recycle()
        return out
    }

    /** t = seconds since the scene started. */
    fun draw(canvas: Canvas, t: Float) {
        val loop = scene.loop
        val p = if (loop > 0f) fmod(t, loop) / loop else 0f
        canvas.save()
        canvas.translate(ox, oy)
        if (sky.isNotEmpty()) drawSky(canvas, p)
        canvas.save(); canvas.scale(scale, scale)
        for (e in back) e.draw(canvas, t, p, gfx)
        canvas.restore()
        canvas.drawBitmap(bg, 0f, 0f, null)
        canvas.save(); canvas.scale(scale, scale)
        for (e in front) e.draw(canvas, t, p, gfx)
        canvas.restore()
        canvas.restore()
    }

    private fun drawSky(canvas: Canvas, p: Float) {
        val n = sky.size
        var i = 0
        for (k in 0 until n) if (p >= sky[k].first) i = k
        val j = (i + 1) % n
        val a = sky[i].first
        val b = if (j == 0) sky[j].first + 1 else sky[j].first
        val pp = if (j == 0 && p < a) p + 1 else p
        val u = smooth(clamp01((pp - a) / (b - a)))
        canvas.drawBitmap(sky[i].second, null, dst, bmpPaint)
        if (u > .003f && n > 1) {
            alphaPaint.alpha = (u * 255).toInt()
            canvas.drawBitmap(sky[j].second, null, dst, alphaPaint)
        }
    }

    fun release() {
        bg.recycle()
        sky.forEach { it.second.recycle() }
    }
}
