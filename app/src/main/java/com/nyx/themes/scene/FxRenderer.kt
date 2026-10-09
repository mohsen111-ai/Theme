package com.nyx.themes.scene

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/*
 * Kotlin port of the declarative effects in tools/engine.js (FX). Keep the maths identical.
 * Coordinates are logical (360 x 780); the caller scales the canvas.
 */
const val LW = 360f
const val LH = 780f
private const val TAU = (2 * PI).toFloat()
private const val DEG = (180 / PI).toFloat()

internal fun fmod(a: Float, b: Float): Float {
    val r = a % b
    return if (r < 0) r + b else r
}

internal fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t
internal fun smooth(t: Float) = t * t * (3 - 2 * t)
internal fun clamp01(x: Float) = x.coerceIn(0f, 1f)

private fun JSONArray.floats(): FloatArray = FloatArray(length()) { getDouble(it).toFloat() }
private fun JSONArray.floatRows(): List<FloatArray> = List(length()) { getJSONArray(it).floats() }
private fun JSONObject.f(key: String, def: Float) = if (has(key) && !isNull(key)) getDouble(key).toFloat() else def
private fun JSONObject.col(key: String, def: Int = Color.WHITE) = if (has(key) && !isNull(key)) Css.parse(getString(key)) else def

internal fun visAt(vis: List<FloatArray>?, p: Float): Float {
    if (vis == null || vis.isEmpty()) return 1f
    if (p <= vis[0][0]) return vis[0][1]
    for (i in 0 until vis.size - 1) {
        val a = vis[i]
        val b = vis[i + 1]
        if (p <= b[0]) return a[1] + (b[1] - a[1]) * ((p - a[0]) / (b[0] - a[0]))
    }
    return vis.last()[1]
}

/** Shared paints and sprite caches. One per renderer; not thread safe (renderers are single-threaded). */
internal class Gfx {
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val screen = Paint(Paint.FILTER_BITMAP_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN) }
    private val sprites = HashMap<Int, Bitmap>()
    private val tmp = RectF()
    val path = Path()

    /** radial glow sprite; falloff matches the canvas gradient colour -> transparent black used by the JS preview */
    private fun sprite(color: Int): Bitmap = sprites.getOrPut(color) {
        val n = 96
        val bmp = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        val ca = Color.alpha(color) / 255f
        val px = IntArray(n * n)
        val rr = n / 2f
        for (y in 0 until n) for (x in 0 until n) {
            val u = (hypot(x + .5f - rr, y + .5f - rr) / rr).coerceIn(0f, 1f)
            val k = 1f - u
            px[y * n + x] = Color.argb((ca * k * 255f).toInt(), (Color.red(color) * k).toInt(), (Color.green(color) * k).toInt(), (Color.blue(color) * k).toInt())
        }
        bmp.setPixels(px, 0, n, 0, 0, n, n)
        bmp
    }

    fun glow(c: Canvas, x: Float, y: Float, rad: Float, color: Int, alpha: Float) {
        if (alpha <= 0.003f || rad <= 0f) return
        screen.alpha = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        tmp.set(x - rad, y - rad, x + rad, y + rad)
        c.drawBitmap(sprite(color), null, tmp, screen)
    }

    /** vertical strip sprite for aurora bands: alpha 0 -> 1 at 25% -> 0 */
    private val strips = HashMap<Int, Bitmap>()
    fun strip(color: Int): Bitmap = strips.getOrPut(color) {
        val h = 64
        val bmp = Bitmap.createBitmap(4, h, Bitmap.Config.ARGB_8888)
        for (y in 0 until h) {
            val u = y / (h - 1f)
            val a = if (u < .25f) u / .25f else 1f - (u - .25f) / .75f
            val px = Css.withAlpha(color or (0xFF shl 24), a)
            for (x in 0 until 4) bmp.setPixel(x, y, px)
        }
        bmp
    }

    fun drawScreen(c: Canvas, bmp: Bitmap, dst: RectF, alpha: Float) {
        screen.alpha = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        c.drawBitmap(bmp, null, dst, screen)
    }

    val screenFill = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN) }
}

internal sealed class Shape {
    abstract fun draw(c: Canvas, g: Gfx)
}

internal class RectShape(val x: Float, val y: Float, val w: Float, val h: Float, val col: Int) : Shape() {
    override fun draw(c: Canvas, g: Gfx) { g.fill.color = col; c.drawRect(x, y, x + w, y + h, g.fill) }
}

internal class CircShape(val x: Float, val y: Float, val r: Float, val col: Int) : Shape() {
    override fun draw(c: Canvas, g: Gfx) { g.fill.color = col; c.drawCircle(x, y, r, g.fill) }
}

internal class PolyShape(val pts: FloatArray, val col: Int) : Shape() {
    private val path = Path().also { p ->
        p.moveTo(pts[0], pts[1])
        var i = 2
        while (i < pts.size) { p.lineTo(pts[i], pts[i + 1]); i += 2 }
        p.close()
    }
    override fun draw(c: Canvas, g: Gfx) { g.fill.color = col; c.drawPath(path, g.fill) }
}

internal class LineShape(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val w: Float, val col: Int) : Shape() {
    override fun draw(c: Canvas, g: Gfx) { g.stroke.color = col; g.stroke.strokeWidth = w; c.drawLine(x1, y1, x2, y2, g.stroke) }
}

internal class GlowShape(val x: Float, val y: Float, val r: Float, val col: Int, val a: Float) : Shape() {
    override fun draw(c: Canvas, g: Gfx) = g.glow(c, x, y, r, col, a)
}

internal class EllipseShape(val x: Float, val y: Float, val rx: Float, val ry: Float, val rot: Float, val col: Int) : Shape() {
    private val rect = RectF(-rx, -ry, rx, ry)
    override fun draw(c: Canvas, g: Gfx) {
        g.fill.color = col
        c.save(); c.translate(x, y); c.rotate(rot * DEG); c.drawOval(rect, g.fill); c.restore()
    }
}

internal fun parseShapes(arr: JSONArray): List<Shape> = List(arr.length()) { i ->
    val s = arr.getJSONArray(i)
    when (val kind = s.getString(0)) {
        "r" -> RectShape(s.getDouble(1).toFloat(), s.getDouble(2).toFloat(), s.getDouble(3).toFloat(), s.getDouble(4).toFloat(), Css.parse(s.getString(5)))
        "c" -> CircShape(s.getDouble(1).toFloat(), s.getDouble(2).toFloat(), s.getDouble(3).toFloat(), Css.parse(s.getString(4)))
        "p" -> PolyShape(s.getJSONArray(1).floats(), Css.parse(s.getString(2)))
        "l" -> LineShape(s.getDouble(1).toFloat(), s.getDouble(2).toFloat(), s.getDouble(3).toFloat(), s.getDouble(4).toFloat(), s.getDouble(5).toFloat(), Css.parse(s.getString(6)))
        "g" -> GlowShape(s.getDouble(1).toFloat(), s.getDouble(2).toFloat(), s.getDouble(3).toFloat(), Css.parse(s.getString(4)), if (s.length() > 5 && !s.isNull(5)) s.getDouble(5).toFloat() else 1f)
        "e" -> EllipseShape(s.getDouble(1).toFloat(), s.getDouble(2).toFloat(), s.getDouble(3).toFloat(), s.getDouble(4).toFloat(), s.getDouble(5).toFloat(), Css.parse(s.getString(6)))
        else -> throw IllegalArgumentException("unknown shape $kind")
    }
}

internal abstract class Fx(val z: Int, val vis: List<FloatArray>?) {
    abstract fun draw(c: Canvas, t: Float, p: Float, g: Gfx)
}

private fun baseVis(o: JSONObject): List<FloatArray>? = if (o.has("vis") && !o.isNull("vis")) o.getJSONArray("vis").floatRows() else null

internal class StarsFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c")
    private val a = o.f("a", 1f)
    private val pts = o.getJSONArray("pts").floatRows()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p) * a
        if (va <= .003f) return
        for (s in pts) {
            val x = s[0]; val y = s[1]; val sz = s[2]; val ph = s[3]; val sp = s[4]; val big = s[5] != 0f
            val alpha = (.3f + .6f * (.5f + .5f * sin(t * sp + ph))) * va
            val col = Css.withAlpha(color, alpha)
            if (big) {
                g.stroke.color = col; g.stroke.strokeWidth = .7f
                c.drawLine(x - sz * 3.2f, y, x + sz * 3.2f, y, g.stroke)
                c.drawLine(x, y - sz * 3.2f, x, y + sz * 3.2f, g.stroke)
            }
            g.fill.color = col
            c.drawCircle(x, y, sz * .8f, g.fill)
        }
    }
}

internal class GlowsFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c", Color.WHITE)
    private val cols = if (o.has("cols")) o.getJSONArray("cols").let { a -> IntArray(a.length()) { Css.parse(a.getString(it)) } } else null
    private val a = o.f("a", 1f)
    private val pts = o.getJSONArray("pts").floatRows()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p) * a
        if (va <= .003f) return
        for (q in pts) {
            val amp = q[4]
            val fl = .5f + .5f * sin(t * 7 + q[3]) * sin(t * 3.1f + q[3])
            val col = if (cols != null && q.size > 5) cols[q[5].toInt()] else color
            g.glow(c, q[0], q[1], q[2], col, va * (1 - amp + amp * fl))
        }
    }
}

internal class CloudsFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c")
    private val items = o.getJSONArray("items").floatRows()
    private val rect = RectF()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        g.fill.color = Css.withAlpha(color, va)
        for (it in items) {
            val w = it[2]
            val x = fmod(it[0] + t * it[3] + w, LW + 2 * w) - w
            val y = it[1]
            val h = w * .22f
            for ((a, b, k) in CLOUD_BUMPS) c.drawCircle(x + w * a, y + h * b, h * k, g.fill)
            rect.set(x, y + h * .1f - h * .42f, x + w, y + h * .1f + h * .42f)
            c.drawOval(rect, g.fill)
        }
    }
    private companion object {
        val CLOUD_BUMPS = listOf(Triple(.28f, -.1f, .62f), Triple(.52f, -.5f, .9f), Triple(.76f, -.12f, .66f))
    }
}

internal class BirdsFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c")
    private val items = o.getJSONArray("items").floatRows()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        g.stroke.color = Css.withAlpha(color, va)
        g.stroke.strokeWidth = 1.4f
        for (b in items) {
            val x = fmod(b[0] + t * b[2], LW + 80f) - 40f
            val s = b[4]
            val f = sin(t * 7 + b[3]) * 7
            val path = g.path
            path.reset()
            path.moveTo(-10f * s, f * -.4f * s)
            path.quadTo(-5f * s, (-5f - f) * s, 0f, 0f)
            path.quadTo(5f * s, (-5f - f) * s, 10f * s, f * -.4f * s)
            c.save(); c.translate(x, b[1]); c.drawPath(path, g.stroke); c.restore()
        }
    }
}

internal class FallFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val mode = o.getString("m")
    private val color = o.col("c")
    private val glowColor = if (o.has("g")) Css.parse(o.getString("g")) else color
    private val k = o.f("k", .18f)
    private val top = o.f("top", -20f)
    private val span = o.f("span", 820f)
    private val pts = o.getJSONArray("pts").floatRows()
    private val rect = RectF()
    private var lines = FloatArray(pts.size * 4)
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        when (mode) {
            "snow" -> {
                g.fill.color = Css.withAlpha(color, va)
                for (q in pts) {
                    val y = fmod(q[1] + t * q[3], 790f) - 5f
                    val x = fmod(q[0] + sin(t * .8f + q[4] + y * .01f) * 14f, LW)
                    c.drawCircle(x, y, q[2], g.fill)
                }
            }
            "rain" -> {
                g.stroke.color = Css.withAlpha(color, va); g.stroke.strokeWidth = 1f
                var n = 0
                for (q in pts) {
                    val y = top + fmod(q[1] + t * q[3], span)
                    val x = q[0] - y * k
                    lines[n++] = x; lines[n++] = y; lines[n++] = x - q[2] * k; lines[n++] = y + q[2]
                }
                c.drawLines(lines, 0, n, g.stroke)
            }
            "rise" -> for (q in pts) {
                val y = fmod(q[1] - t * q[3] + 30f, 840f) - 30f
                val x = q[0] + sin(t * .6f + q[5]) * 10f
                g.glow(c, x, y, q[2] * 4f, glowColor, va)
                g.fill.color = Css.withAlpha(color, va)
                c.drawCircle(x, y, q[2], g.fill)
            }
            "petal" -> {
                g.fill.color = Css.withAlpha(color, va)
                for (q in pts) {
                    val y = fmod(q[1] + t * q[3], 800f) - 10f
                    val x = fmod(q[0] + t * 8f + sin(t * .7f + q[5]) * 30f, LW + 20f) - 10f
                    rect.set(-q[2] * 1.2f, -q[2] * .7f, q[2] * 1.2f, q[2] * .7f)
                    c.save(); c.translate(x, y); c.rotate((t + q[5]) * DEG); c.drawOval(rect, g.fill); c.restore()
                }
            }
        }
    }
}

internal class FliesFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c")
    private val core = if (o.has("c2")) Css.parse(o.getString("c2")) else Color.rgb(255, 255, 200)
    private val pts = o.getJSONArray("pts").floatRows()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        for (q in pts) {
            val x = q[0] + sin(t * q[4] + q[5]) * q[2]
            val y = q[1] + cos(t * q[4] * 1.3f + q[5]) * q[3]
            val b = max(0f, sin(t * q[6] + q[5])) * va
            g.glow(c, x, y, 16f, color, b)
            if (b > .003f) { g.fill.color = Css.withAlpha(core, b); c.drawCircle(x, y, 1.3f, g.fill) }
        }
    }
}

internal class SmokeFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c")
    private val a0 = o.f("a", .22f)
    private val x = o.f("x", 0f); private val y = o.f("y", 0f)
    private val n = o.getInt("n"); private val drift = o.f("drift", 0f); private val rise = o.f("rise", 0f)
    private val r0 = o.f("r0", 3f); private val grow = o.f("grow", 10f); private val speed = o.f("speed", .1f)
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        for (i in 0 until n) {
            val q = fmod(t * speed + i.toFloat() / n, 1f)
            g.fill.color = Css.withAlpha(color, a0 * (1 - q) * va)
            c.drawCircle(x + sin(q * 6 + i) * 8 + q * drift, y - q * rise, r0 + q * grow, g.fill)
        }
    }
}

internal class ShootFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val items = o.getJSONArray("items").floatRows()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        for (s in items) {
            val period = s[0]; val off = s[1]; val dur = s[2]
            val q = fmod(t + off, period) / period
            if (q >= dur) continue
            val u = q / dur
            val dx = s[5]; val dy = s[6]; val len = s[7]
            val px = s[3] + dx * u; val py = s[4] + dy * u
            val a = sin(u * PI.toFloat())
            val d = hypot(dx, dy)
            val tx = px - dx / d * len; val ty = py - dy / d * len
            g.stroke.shader = LinearGradient(px, py, tx, ty, Color.argb((a * 255).toInt(), 255, 255, 255), Color.argb(0, 255, 255, 255), Shader.TileMode.CLAMP)
            g.stroke.strokeWidth = 1.8f
            c.drawLine(px, py, tx, ty, g.stroke)
            g.stroke.shader = null
        }
    }
}

internal class ShimmerFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val color = o.col("c")
    private val x = o.f("x", 0f); private val y = o.f("y", 0f); private val n = o.getInt("n"); private val step = o.f("step", 10f)
    private val w0 = o.f("w0", 60f); private val w1 = o.f("w1", 20f); private val a0 = o.f("a0", .5f); private val dec = o.f("dec", .014f)
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        for (i in 0 until n) {
            val w = lerpF(w0, w1, i.toFloat() / n) * (.85f + .15f * sin(t * 1.6f + i * .9f))
            val px = x + sin(t * .9f + i * .7f) * (3 + i * .25f)
            val a = max(0f, a0 - i * dec)
            g.fill.color = Css.withAlpha(color, a)
            c.drawRect(px - w / 2, y + i * step, px + w / 2, y + i * step + 2.2f, g.fill)
        }
    }
}

internal class FlameFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val x = o.f("x", 0f); private val y = o.f("y", 0f); private val s = o.f("s", 1f)
    private val outer = Css.parse("#e8531c"); private val mid = Css.parse("#f08a2a"); private val inner = Css.parse("#ffd870")
    private fun flame(c: Canvas, g: Gfx, fx: Float, fy: Float, w: Float, h: Float, col: Int, sw: Float) {
        val p = g.path
        p.reset()
        p.moveTo(fx - w, fy)
        p.quadTo(fx - w * 1.1f, fy - h * .55f, fx + sw, fy - h)
        p.quadTo(fx + w * 1.1f, fy - h * .5f, fx + w, fy)
        p.close()
        g.fill.color = col
        c.drawPath(p, g.fill)
    }
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val defs = listOf(floatArrayOf(0f, 16f, 54f, 0f), floatArrayOf(-9f, 11f, 38f, 1.6f), floatArrayOf(9f, 11f, 36f, -1.4f))
        val cols = intArrayOf(outer, mid, mid)
        defs.forEachIndexed { i, d ->
            flame(c, g, x + d[0] * s, y, d[1] * .7f * s, d[2] * s * (.85f + .15f * sin(t * 10 + i * 2)), cols[i], (sin(t * 6 + i) * 3 + d[3]) * s)
        }
        flame(c, g, x, y, 6 * s, 26 * s * (.85f + .15f * sin(t * 12)), inner, sin(t * 7) * 2 * s)
    }
}

internal class AuroraFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private class Band(val k: Float, val by: Float, val color: Int, val hh: Float)
    private val bands = o.getJSONArray("bands").let { a ->
        List(a.length()) { i ->
            val b = a.getJSONArray(i)
            val rgb = b.getString(2).split(',').map { it.trim().toInt() }
            Band(b.getDouble(0).toFloat(), b.getDouble(1).toFloat(), Color.rgb(rgb[0], rgb[1], rgb[2]), b.getDouble(3).toFloat())
        }
    }
    private val dst = RectF()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        for (b in bands) {
            val sprite = g.strip(b.color)
            var x = 0f
            while (x <= LW) {
                val y = b.by + sin(x * .016f + t * .4f + b.k * 1.9f) * 34f + sin(x * .05f - t * .6f + b.k) * 10f
                val a = (.26f + .16f * sin(x * .03f + t * .5f + b.k)) * va
                dst.set(x, y - b.hh * .2f, x + 6.5f, y + b.hh)
                g.drawScreen(c, sprite, dst, a)
                x += 6f
            }
        }
    }
}

internal class MoverFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val path = o.getJSONArray("path").floats()
    private val dur = o.f("dur", 60f); private val off = o.f("off", 0f)
    private val bob = if (o.has("bob")) o.getJSONArray("bob").floats() else null
    private val tilt = o.f("tilt", 0f)
    private val shapes = parseShapes(o.getJSONArray("shapes"))
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        val va = visAt(vis, p)
        if (va <= .003f) return
        val u = fmod(t + off, dur) / dur
        val px = lerpF(path[0], path[2], u); val py = lerpF(path[1], path[3], u)
        val b = if (bob != null) sin(t * bob[1] + (if (bob.size > 2) bob[2] else 0f)) * bob[0] else 0f
        val tl = if (tilt != 0f) sin(t * (bob?.get(1) ?: 1f) * .8f) * tilt else 0f
        c.save()
        c.translate(px, py + b)
        if (tl != 0f) c.rotate(tl * DEG)
        for (s in shapes) s.draw(c, g)
        c.restore()
    }
}

internal class SpinFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val x = o.f("x", 0f); private val y = o.f("y", 0f); private val speed = o.f("speed", 1f); private val phase = o.f("phase", 0f)
    private val shapes = parseShapes(o.getJSONArray("shapes"))
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        c.save(); c.translate(x, y); c.rotate((t * speed + phase) * DEG)
        for (s in shapes) s.draw(c, g)
        c.restore()
    }
}

internal class OrbitFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val x = o.f("x", 0f); private val y = o.f("y", 0f); private val r = o.f("r", 10f); private val n = o.getInt("n")
    private val speed = o.f("speed", 1f); private val phase = o.f("phase", 0f)
    private val shapes = parseShapes(o.getJSONArray("shapes"))
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        for (i in 0 until n) {
            val a = t * speed + phase + TAU * i / n
            c.save(); c.translate(x + cos(a) * r, y + sin(a) * r)
            for (s in shapes) s.draw(c, g)
            c.restore()
        }
    }
}

internal class OrbFx(o: JSONObject) : Fx(o.optInt("z", 1), baseVis(o)) {
    private val a = o.f("a", 0f); private val b = o.f("b", 1f)
    private val q0 = o.f("q0", 0f); private val q1 = o.f("q1", 1f)
    private val x0 = o.f("x0", 0f); private val x1 = o.f("x1", 0f); private val y0 = o.f("y0", 0f); private val yAmp = o.f("yAmp", 0f)
    private val r = o.f("r", 30f)
    private val color = o.col("c"); private val warm = if (o.has("warm")) Css.parse(o.getString("warm")) else null
    private val glowColor = o.col("glow"); private val hi = if (o.has("hi")) Css.parse(o.getString("hi")) else Css.parse("#f4f7fd")
    private val craters = o.optInt("craters", 0) != 0
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        if (p < a || p > b) return
        val q = lerpF(q0, q1, (p - a) / (b - a))
        val k = sin(q * PI.toFloat())
        val x = lerpF(x0, x1, q); val y = y0 - k * yAmp
        val col = if (warm != null) Css.mix(color, warm, (1 - k).pow(3)) else color
        g.glow(c, x, y, r * 3.6f, glowColor, 1f)
        g.fill.shader = RadialGradient(x - r * .15f, y - r * .15f, r * 1.1f, intArrayOf(hi, col), floatArrayOf(.0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(x, y, r, g.fill)
        g.fill.shader = null
        if (craters) {
            g.fill.color = Color.argb(18, 120, 135, 180)
            for ((dx, dy, kk) in CRATERS) c.drawCircle(x + dx * r, y + dy * r, kk * r, g.fill)
        }
    }
    private companion object {
        val CRATERS = listOf(Triple(.3f, -.2f, .22f), Triple(-.25f, .25f, .3f), Triple(.1f, .45f, .14f), Triple(-.4f, -.3f, .12f))
    }
}

internal class TintFx(o: JSONObject) : Fx(o.optInt("z", 1), null) {
    private val keys = o.getJSONArray("keys").let { a -> List(a.length()) { i -> a.getJSONArray(i).let { k -> k.getDouble(0).toFloat() to Css.parse(k.getString(1)) } } }
    private val screen = o.optString("m") == "screen"
    private val paint = Paint()
    override fun draw(c: Canvas, t: Float, p: Float, g: Gfx) {
        var i = 0
        while (i < keys.size - 2 && p > keys[i + 1].first) i++
        val (a, ca) = keys[i]; val (b, cb) = keys[i + 1]
        val u = clamp01((p - a) / (b - a))
        val col = Css.mix(ca, cb, u)
        if (Color.alpha(col) <= 1) return
        paint.color = col
        paint.xfermode = if (screen) PorterDuffXfermode(PorterDuff.Mode.SCREEN) else null
        c.drawRect(0f, 0f, LW, LH, paint)
    }
}

object FxFactory {
    internal fun create(o: JSONObject): Fx = when (val t = o.getString("t")) {
        "stars" -> StarsFx(o); "glows" -> GlowsFx(o); "clouds" -> CloudsFx(o); "birds" -> BirdsFx(o); "fall" -> FallFx(o)
        "flies" -> FliesFx(o); "smoke" -> SmokeFx(o); "shoot" -> ShootFx(o); "shimmer" -> ShimmerFx(o); "flame" -> FlameFx(o)
        "aurora" -> AuroraFx(o); "mover" -> MoverFx(o); "spin" -> SpinFx(o); "orbit" -> OrbitFx(o); "orb" -> OrbFx(o); "tint" -> TintFx(o)
        else -> throw IllegalArgumentException("unknown effect $t")
    }
}
