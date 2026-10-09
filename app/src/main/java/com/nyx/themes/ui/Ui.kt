package com.nyx.themes.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/** Small helpers so screens can be built in code with one consistent look. */
object Ui {
    const val BG = 0xFF09091A.toInt()
    const val PANEL = 0xFF12122B.toInt()
    const val PANEL_HI = 0xFF1C1C40.toInt()
    const val INK = 0xFFEBE6F5.toInt()
    const val MUTE = 0xFF9D98BD.toInt()
    const val AMBER = 0xFFF0A54A.toInt()
    const val VIOLET = 0xFF9D90FF.toInt()
    const val LINE = 0x29BEB9F0

    fun dp(ctx: Context, v: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.resources.displayMetrics).toInt()
    fun dp(ctx: Context, v: Int): Int = dp(ctx, v.toFloat())

    private var display: Typeface? = null
    fun displayFace(ctx: Context): Typeface = display ?: runCatching { Typeface.createFromAsset(ctx.applicationContext.assets, "fonts/display_italic.ttf") }
        .getOrDefault(Typeface.create(Typeface.SERIF, Typeface.ITALIC)).also { display = it }

    fun round(ctx: Context, color: Int, radiusDp: Float, stroke: Int = 0): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(ctx, radiusDp).toFloat()
        if (stroke != 0) setStroke(dp(ctx, 1), stroke)
    }

    fun text(ctx: Context, s: CharSequence, sp: Float = 15f, color: Int = INK, display: Boolean = false, bold: Boolean = false): TextView = TextView(ctx).apply {
        text = s
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        setTextColor(color)
        if (display) typeface = displayFace(ctx) else if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun chip(ctx: Context, label: String, selected: Boolean, onClick: () -> Unit): TextView = text(ctx, label, 13f, if (selected) 0xFF1A1020.toInt() else INK, bold = true).apply {
        gravity = Gravity.CENTER
        setPadding(dp(ctx, 14), dp(ctx, 8), dp(ctx, 14), dp(ctx, 8))
        background = round(ctx, if (selected) AMBER else 0, 20f, if (selected) AMBER else LINE)
        setOnClickListener { onClick() }
        isClickable = true
        isFocusable = true
    }

    fun button(ctx: Context, label: String, primary: Boolean = false, onClick: () -> Unit): TextView = text(ctx, label, 15f, if (primary) 0xFF1A1020.toInt() else INK, bold = true).apply {
        gravity = Gravity.CENTER
        setPadding(dp(ctx, 18), dp(ctx, 13), dp(ctx, 18), dp(ctx, 13))
        background = round(ctx, if (primary) AMBER else PANEL_HI, 26f, if (primary) AMBER else LINE)
        setOnClickListener { onClick() }
        isClickable = true
        isFocusable = true
    }

    fun clipRounded(v: View, radiusDp: Float) {
        val r = dp(v.context, radiusDp).toFloat()
        v.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) { outline.setRoundRect(0, 0, view.width, view.height, r) }
        }
        v.clipToOutline = true
    }

    fun vbox(ctx: Context) = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
    fun hbox(ctx: Context) = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun lp(w: Int, h: Int, weight: Float = 0f) = LinearLayout.LayoutParams(w, h, weight)
    val MATCH = LinearLayout.LayoutParams.MATCH_PARENT
    val WRAP = LinearLayout.LayoutParams.WRAP_CONTENT
    fun fl(w: Int, h: Int) = FrameLayout.LayoutParams(w, h)
    fun transparent() = Color.TRANSPARENT
}

/** frame that keeps a fixed width:height ratio (phone wallpaper cards) */
class AspectFrame(ctx: Context, private val ratioHOverW: Float) : FrameLayout(ctx) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((w * ratioHOverW).toInt(), MeasureSpec.EXACTLY))
    }
}
