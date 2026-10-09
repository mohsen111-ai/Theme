package com.nyx.themes.ui

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.provider.Settings
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.nyx.themes.data.Prefs
import com.nyx.themes.home.HomeMode

/** Reusable rows shared by several screens. */
object Controls {
    /** title + subtitle + switch. [onChange] returns false to refuse the change (the switch snaps back). */
    fun switchRow(c: Context, title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Boolean): View {
        val row = Ui.hbox(c).apply { setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 8)) }
        val col = Ui.vbox(c)
        col.addView(Ui.text(c, title, 16f, Ui.INK, bold = true)); col.addView(Ui.text(c, sub, 13f, Ui.MUTE))
        row.addView(col, Ui.lp(0, Ui.WRAP, 1f))
        val sw = SwitchCompat(c).apply {
            isChecked = checked
            thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(Ui.AMBER, Ui.MUTE))
            trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(0x66F0A54A, 0x33FFFFFF))
        }
        var internal = false
        sw.setOnCheckedChangeListener { _, on ->
            if (internal) return@setOnCheckedChangeListener
            if (!onChange(on)) { internal = true; sw.isChecked = !on; internal = false }
        }
        row.addView(sw, Ui.lp(Ui.WRAP, Ui.WRAP))
        return row
    }

    /** The "Wallpapers only" switch (settings and Home tab). */
    fun wallpapersOnly(c: Context, onApplied: () -> Unit = {}): View = switchRow(
        c, "Wallpapers only (no icons, no Nyx Home)",
        "Keep your own launcher and icons. Only the wallpapers are used.", Prefs(c).wallpapersOnly,
    ) { on ->
        when (HomeMode.setWallpapersOnly(c, on)) {
            HomeMode.Result.OK -> { onApplied(); true }
            HomeMode.Result.NYX_IS_DEFAULT_HOME -> {
                AlertDialog.Builder(c).setTitle("Pick another home app first")
                    .setMessage("Nyx Home is your home app right now. Choose your usual launcher in the phone's home app settings, then switch this on.")
                    .setPositiveButton("Open home settings") { _, _ -> runCatching { c.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
                    .setNegativeButton("Cancel", null).show()
                false
            }
        }
    }
}
