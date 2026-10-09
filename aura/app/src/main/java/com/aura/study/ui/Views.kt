package com.aura.study.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.view.View
import kotlin.math.max

/** Circular countdown ring. */
class RingView(c: Context) : View(c) {
    var progress = 0f; set(v) { field = v.coerceIn(0f, 1f); invalidate() }
    var label = "25:00"; set(v) { field = v; invalidate() }
    var sub = "Focus"; set(v) { field = v; invalidate() }
    var accent = Ui.MINT; set(v) { field = v; invalidate() }
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Ui.PANEL_HI; strokeCap = Paint.Cap.ROUND }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val big = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Ui.INK; textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL) }
    private val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Ui.MUTE; textAlign = Paint.Align.CENTER }
    private val rect = RectF()
    override fun onDraw(canvas: Canvas) {
        val s = minOf(width, height).toFloat(); val w = s * .055f; track.strokeWidth = w; arc.strokeWidth = w
        rect.set((width - s) / 2 + w, (height - s) / 2 + w, (width + s) / 2 - w, (height + s) / 2 - w)
        canvas.drawOval(rect, track)
        arc.shader = SweepGradient(width / 2f, height / 2f, intArrayOf(accent, Ui.VIOLET, accent), floatArrayOf(0f, .6f, 1f)).also { val m = android.graphics.Matrix(); m.postRotate(-90f, width / 2f, height / 2f); it.setLocalMatrix(m) }
        if (progress > 0f) canvas.drawArc(rect, -90f, 360f * progress, false, arc)
        big.textSize = s * .22f; small.textSize = s * .065f
        canvas.drawText(label, width / 2f, height / 2f + s * .06f, big); canvas.drawText(sub, width / 2f, height / 2f + s * .17f, small)
    }
    override fun onMeasure(w: Int, h: Int) { val s = minOf(MeasureSpec.getSize(w), Ui.dp(context, 280)); setMeasuredDimension(s, s) }
}

/** GitHub style grid of the last 12 weeks. levels: epoch day -> 0..4 */
class HeatmapView(c: Context) : View(c) {
    var today = 0L; var levels: Map<Long, Int> = emptyMap(); set(v) { field = v; invalidate() }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cols = intArrayOf(0xFF1B2540.toInt(), 0xFF1F5E55.toInt(), 0xFF2E9C86.toInt(), 0xFF4FCDAE.toInt(), 0xFF8FF5D8.toInt())
    private val weeks = 12
    override fun onDraw(canvas: Canvas) {
        val gap = Ui.dp(context, 3).toFloat(); val cell = cellSize()
        val dow = java.time.LocalDate.ofEpochDay(today).dayOfWeek.value - 1
        val start = today - dow - (weeks - 1) * 7
        for (wk in 0 until weeks) for (d in 0 until 7) {
            val day = start + wk * 7 + d; if (day > today) continue
            p.color = cols[levels[day] ?: 0]
            val x = wk * (cell + gap); val y = d * (cell + gap)
            canvas.drawRoundRect(x, y, x + cell, y + cell, cell * .25f, cell * .25f, p)
        }
    }
    private fun cellSize(): Float = minOf((width - Ui.dp(context, 3) * (weeks - 1)) / weeks.toFloat(), Ui.dp(context, 22).toFloat())
    override fun onMeasure(w: Int, h: Int) { val ww = MeasureSpec.getSize(w); val gap = Ui.dp(context, 3); val cell = minOf((ww - gap * (weeks - 1)) / weeks, Ui.dp(context, 22)); setMeasuredDimension(ww, cell * 7 + gap * 6) }
}

/** Simple bar chart for minutes per day. */
class BarsView(c: Context) : View(c) {
    var values: List<Pair<String, Int>> = emptyList(); set(v) { field = v; invalidate() }
    private val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Ui.MINT }
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Ui.MUTE; textAlign = Paint.Align.CENTER; textSize = Ui.dp(c, 11).toFloat() }
    private val val_ = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Ui.INK; textAlign = Paint.Align.CENTER; textSize = Ui.dp(c, 11).toFloat() }
    override fun onDraw(canvas: Canvas) {
        if (values.isEmpty()) return
        val top = Ui.dp(context, 18).toFloat(); val bottom = height - Ui.dp(context, 20).toFloat(); val mx = max(1, values.maxOf { it.second })
        val slot = width / values.size.toFloat(); val bw = slot * .5f
        values.forEachIndexed { i, (label, v) ->
            val h = (bottom - top) * v / mx; val cx = slot * i + slot / 2
            canvas.drawRoundRect(RectF(cx - bw / 2, bottom - h, cx + bw / 2, bottom), bw * .25f, bw * .25f, bar.apply { alpha = if (v == 0) 60 else 255 })
            if (v > 0) canvas.drawText("$v", cx, bottom - h - Ui.dp(context, 4), val_)
            canvas.drawText(label, cx, height - Ui.dp(context, 4).toFloat(), txt)
        }
    }
}
