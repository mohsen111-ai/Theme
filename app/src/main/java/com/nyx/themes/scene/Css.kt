package com.nyx.themes.scene

import android.graphics.Color
import kotlin.math.roundToInt

/** Minimal CSS colour parser for the strings the scene pipeline emits (#rgb, #rrggbb, rgb(), rgba()). */
object Css {
    fun parse(input: String): Int {
        val s = input.trim()
        if (s.startsWith("#")) {
            var h = s.substring(1)
            if (h.length == 3) h = h.map { "$it$it" }.joinToString("")
            require(h.length == 6) { "bad colour $input" }
            return Color.rgb(h.substring(0, 2).toInt(16), h.substring(2, 4).toInt(16), h.substring(4, 6).toInt(16))
        }
        val open = s.indexOf('(')
        val close = s.lastIndexOf(')')
        require(open > 0 && close > open) { "bad colour $input" }
        val p = s.substring(open + 1, close).split(',').map { it.trim().toFloat() }
        val a = if (p.size > 3) p[3] else 1f
        return Color.argb((a.coerceIn(0f, 1f) * 255f).roundToInt(), p[0].roundToInt(), p[1].roundToInt(), p[2].roundToInt())
    }

    /** colour with its alpha multiplied by [a] (0..1) */
    fun withAlpha(color: Int, a: Float): Int {
        val na = ((color ushr 24) * a.coerceIn(0f, 1f)).roundToInt()
        return (na shl 24) or (color and 0x00FFFFFF)
    }

    fun mix(a: Int, b: Int, t: Float): Int {
        fun ch(x: Int, y: Int) = (x + (y - x) * t).roundToInt().coerceIn(0, 255)
        return Color.argb(
            ch(Color.alpha(a), Color.alpha(b)), ch(Color.red(a), Color.red(b)),
            ch(Color.green(a), Color.green(b)), ch(Color.blue(a), Color.blue(b)),
        )
    }
}
