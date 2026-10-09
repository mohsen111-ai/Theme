package com.aura.study.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Code-built UI helpers: one dark "aurora" look for every screen. */
object Ui {
    const val BG = 0xFF0B0F1A.toInt()
    const val PANEL = 0xFF131A2B.toInt()
    const val PANEL_HI = 0xFF1B2540.toInt()
    const val INK = 0xFFE8F0F7.toInt()
    const val MUTE = 0xFF8FA0B8.toInt()
    const val MINT = 0xFF5EE0B8.toInt()
    const val VIOLET = 0xFF8E7CFF.toInt()
    const val WARM = 0xFFFFB86B.toInt()
    const val RED = 0xFFFF7A8A.toInt()
    const val LINE = 0x338FA0B8
    const val ON_ACCENT = 0xFF06201A.toInt()
    val MATCH = LinearLayout.LayoutParams.MATCH_PARENT
    val WRAP = LinearLayout.LayoutParams.WRAP_CONTENT

    fun dp(c: Context, v: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), c.resources.displayMetrics).toInt()
    fun round(c: Context, color: Int, r: Int, stroke: Int = 0) = GradientDrawable().apply { setColor(color); cornerRadius = dp(c, r).toFloat(); if (stroke != 0) setStroke(dp(c, 1), stroke) }
    fun lp(w: Int, h: Int, weight: Float = 0f) = LinearLayout.LayoutParams(w, h, weight)
    fun lpm(w: Int, h: Int, l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0, ctx: Context) = LinearLayout.LayoutParams(w, h).apply { setMargins(dp(ctx, l), dp(ctx, t), dp(ctx, r), dp(ctx, b)) }
    fun vbox(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL }
    fun hbox(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }

    fun text(c: Context, s: CharSequence, sp: Float = 15f, color: Int = INK, bold: Boolean = false) = TextView(c).apply {
        text = s; setTextSize(TypedValue.COMPLEX_UNIT_SP, sp); setTextColor(color); if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    fun title(c: Context, s: String) = text(c, s, 20f, INK, true).apply { setPadding(0, dp(c, 18), 0, dp(c, 6)) }
    fun note(c: Context, s: String) = text(c, s, 13f, MUTE).apply { setPadding(0, 0, 0, dp(c, 8)) }

    fun chip(c: Context, label: String, selected: Boolean, onClick: () -> Unit) = text(c, label, 13f, if (selected) ON_ACCENT else INK, true).apply {
        gravity = Gravity.CENTER; setPadding(dp(c, 14), dp(c, 8), dp(c, 14), dp(c, 8)); background = round(c, if (selected) MINT else 0, 20, if (selected) MINT else LINE)
        isClickable = true; isFocusable = true; setOnClickListener { onClick() }
    }
    fun button(c: Context, label: String, primary: Boolean = false, onClick: () -> Unit) = text(c, label, 15f, if (primary) ON_ACCENT else INK, true).apply {
        gravity = Gravity.CENTER; setPadding(dp(c, 18), dp(c, 13), dp(c, 18), dp(c, 13)); background = round(c, if (primary) MINT else PANEL_HI, 26, if (primary) MINT else LINE)
        isClickable = true; isFocusable = true; setOnClickListener { onClick() }
    }
    fun card(c: Context) = vbox(c).apply { setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14)); background = round(c, PANEL, 18, LINE) }
    fun input(c: Context, hint: String, lines: Int = 1, password: Boolean = false) = EditText(c).apply {
        this.hint = hint; setHintTextColor(MUTE); setTextColor(INK); textSize = 15f; background = round(c, PANEL_HI, 14, LINE)
        setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12)); gravity = if (lines > 1) Gravity.TOP else Gravity.CENTER_VERTICAL
        if (lines > 1) { minLines = lines; maxLines = 12; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
        else { isSingleLine = true; inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
    }
    fun chipRow(c: Context): Pair<HorizontalScrollView, LinearLayout> { val row = hbox(c); val sv = HorizontalScrollView(c).apply { isHorizontalScrollBarEnabled = false; addView(row) }; return sv to row }
    fun gap(c: Context, h: Int) = View(c).apply { layoutParams = lp(1, dp(c, h)) }
    fun scroll(c: Context, body: View) = ScrollView(c).apply { isFillViewport = true; addView(body, FrameLayout.LayoutParams(MATCH, WRAP)) }
    fun transparent() = Color.TRANSPARENT

    /** a row of single-choice chips that redraws itself */
    fun <T> choice(c: Context, items: List<Pair<String, T>>, current: () -> T, onPick: (T) -> Unit): HorizontalScrollView {
        val (sv, row) = chipRow(c)
        fun draw() { row.removeAllViews(); items.forEach { (label, v) -> row.addView(chip(c, label, v == current()) { onPick(v); draw() }, lp(WRAP, WRAP).apply { rightMargin = dp(c, 8) }) } }
        draw(); return sv
    }
}
