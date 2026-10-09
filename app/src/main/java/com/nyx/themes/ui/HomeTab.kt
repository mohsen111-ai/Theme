package com.nyx.themes.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import com.nyx.themes.data.IconPackId
import com.nyx.themes.data.Prefs
import com.nyx.themes.home.HomeMode
import com.nyx.themes.home.IconMap

/** Icon packs and the optional Nyx Home launcher. */
class HomeTab(ctx: Context) : ScrollView(ctx) {
    private val prefs = Prefs(ctx)
    private val body = Ui.vbox(ctx)

    init {
        isFillViewport = true
        body.setPadding(Ui.dp(ctx, 18), Ui.dp(ctx, 4), Ui.dp(ctx, 18), Ui.dp(ctx, 28))
        addView(body, Ui.lp(Ui.MATCH, Ui.WRAP))
        build()
    }

    fun refresh() = build()

    private fun build() {
        val c = context
        body.removeAllViews()
        body.addView(Ui.text(c, "Painted icons and home screen", 22f, Ui.INK, display = true).apply { setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 6)) })
        body.addView(Ui.text(c, "Nyx Home is a simple home screen that shows every app with a painted icon. It is optional. The wallpapers work without it.", 14f, Ui.MUTE))
        body.addView(Controls.wallpapersOnly(c) { build() })

        if (!prefs.wallpapersOnly) {
            val isDefault = HomeMode.isDefaultHome(c)
            val card = Ui.vbox(c).apply { setPadding(Ui.dp(c, 16), Ui.dp(c, 14), Ui.dp(c, 16), Ui.dp(c, 16)); background = Ui.round(c, Ui.PANEL, 18f, Ui.LINE) }
            card.addView(Ui.text(c, if (isDefault) "Nyx Home is your home app" else "Nyx Home is ready", 17f, Ui.INK, bold = true))
            card.addView(Ui.text(c, if (isDefault) "To go back to your old launcher, open the phone's home app settings and pick it." else "Make it your home app to see the painted icons.", 13f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 4), 0, Ui.dp(c, 12)) })
            card.addView(Ui.button(c, if (isDefault) "Open home settings" else "Make Nyx Home my home app", !isDefault) {
                try { c.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                catch (e: Exception) { Toast.makeText(c, "Open Settings > Apps > Default apps > Home app", Toast.LENGTH_LONG).show() }
            })
            body.addView(card, Ui.lp(Ui.MATCH, Ui.WRAP).apply { topMargin = Ui.dp(c, 10) })
        }

        body.addView(Ui.text(c, "Icon style", 20f, Ui.INK, display = true).apply { setPadding(0, Ui.dp(c, 24), 0, Ui.dp(c, 8)) })
        IconPackId.values().forEach { pack -> body.addView(packCard(c, pack), Ui.lp(Ui.MATCH, Ui.WRAP).apply { bottomMargin = Ui.dp(c, 12) }) }
        if (prefs.wallpapersOnly) body.addView(Ui.text(c, "Turn off Wallpapers only to use these icons.", 13f, Ui.MUTE))
    }

    private fun packCard(c: Context, pack: IconPackId): android.view.View {
        val sel = prefs.iconPack == pack
        val card = Ui.vbox(c).apply {
            setPadding(Ui.dp(c, 14), Ui.dp(c, 12), Ui.dp(c, 14), Ui.dp(c, 14))
            background = Ui.round(c, if (pack == IconPackId.PAPER) 0xFFE6DCC0.toInt() else Ui.PANEL, 18f, if (sel) Ui.AMBER else Ui.LINE)
            isClickable = true; isFocusable = true
            setOnClickListener { prefs.iconPack = pack; build() }
        }
        val titleColor = if (pack == IconPackId.PAPER) 0xFF3A2F20.toInt() else Ui.INK
        val head = Ui.hbox(c)
        head.addView(Ui.text(c, pack.label, 17f, titleColor, display = true), Ui.lp(0, Ui.WRAP, 1f))
        if (sel) head.addView(Ui.text(c, "Selected", 12f, if (pack == IconPackId.PAPER) 0xFF8A5A10.toInt() else Ui.AMBER, bold = true))
        card.addView(head)
        val row = Ui.hbox(c).apply { setPadding(0, Ui.dp(c, 10), 0, 0) }
        IconMap.GLYPHS.take(7).forEach { g ->
            val iv = ImageView(c).apply { scaleType = ImageView.ScaleType.FIT_CENTER; contentDescription = g }
            ThumbLoader.load(c, "icons/${pack.dir}/$g.png", iv)
            row.addView(iv, Ui.lp(0, Ui.dp(c, 44), 1f).apply { rightMargin = Ui.dp(c, 4) })
        }
        card.addView(row)
        return card
    }
}
