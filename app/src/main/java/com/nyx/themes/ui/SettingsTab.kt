package com.nyx.themes.ui

import android.content.Context
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.nyx.themes.data.AutoMode
import com.nyx.themes.data.Prefs
import com.nyx.themes.data.Source
import com.nyx.themes.service.SceneController

/** Auto-change, playlist and power settings. Every control writes to [Prefs] immediately. */
class SettingsTab(ctx: Context) : ScrollView(ctx) {
    private val prefs = Prefs(ctx)
    private val controller = SceneController.get(ctx)
    private val body = Ui.vbox(ctx)

    init {
        isFillViewport = true
        body.setPadding(Ui.dp(ctx, 18), Ui.dp(ctx, 4), Ui.dp(ctx, 18), Ui.dp(ctx, 28))
        addView(body, Ui.lp(Ui.MATCH, Ui.WRAP))
        build()
    }

    private fun heading(s: String) = Ui.text(context, s, 20f, Ui.INK, display = true).apply { setPadding(0, Ui.dp(context, 22), 0, Ui.dp(context, 4)) }
    private fun note(s: String) = Ui.text(context, s, 13f, Ui.MUTE).apply { setPadding(0, 0, 0, Ui.dp(context, 8)) }

    private fun <T> chips(items: List<Pair<String, T>>, current: () -> T, onPick: (T) -> Unit): HorizontalScrollView {
        val row = Ui.hbox(context)
        val sv = HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false; addView(row) }
        fun draw() {
            row.removeAllViews()
            items.forEach { (label, v) ->
                row.addView(Ui.chip(context, label, v == current()) { onPick(v); draw() }, Ui.lp(Ui.WRAP, Ui.WRAP).apply { rightMargin = Ui.dp(context, 8) })
            }
        }
        draw()
        return sv
    }

    private fun build() {
        val c = context
        body.removeAllViews()

        body.addView(heading("Wallpapers only"))
        body.addView(Controls.wallpapersOnly(c))

        body.addView(heading("Change wallpaper automatically"))
        body.addView(note("Needs Nyx to be your live wallpaper. A new one is picked from the list below."))
        val timerRow = chips(listOf("5 min" to 5, "15 min" to 15, "30 min" to 30, "1 hour" to 60, "3 hours" to 180, "6 hours" to 360), { prefs.intervalMinutes }, { prefs.intervalMinutes = it })
        body.addView(chips(listOf("Off" to AutoMode.OFF, "Each time I unlock" to AutoMode.UNLOCK, "On a timer" to AutoMode.INTERVAL), { prefs.autoMode }) {
            prefs.autoMode = it; timerRow.visibility = if (it == AutoMode.INTERVAL) View.VISIBLE else View.GONE
        })
        timerRow.visibility = if (prefs.autoMode == AutoMode.INTERVAL) View.VISIBLE else View.GONE
        body.addView(timerRow, Ui.lp(Ui.MATCH, Ui.WRAP).apply { topMargin = Ui.dp(c, 10) })

        body.addView(heading("Pick from"))
        body.addView(chips(Source.values().map { it.label to it }, { prefs.source }, { prefs.source = it }))
        body.addView(Controls.switchRow(c, "Shuffle", "Random order. Off plays the list in order.", prefs.shuffle) { prefs.shuffle = it; true })
        body.addView(Controls.switchRow(c, "Match light and dark mode", "Use light wallpapers when the phone is in light mode.", prefs.followDarkMode) { prefs.followDarkMode = it; true })

        body.addView(heading("Battery"))
        body.addView(Controls.switchRow(c, "Pause animation in Battery Saver", "Shows a still frame instead of moving.", prefs.pauseOnBatterySaver) { prefs.pauseOnBatterySaver = it; true })
        body.addView(note("Animation smoothness"))
        body.addView(chips(listOf("24 fps" to 24, "30 fps" to 30, "45 fps" to 45, "60 fps" to 60), { prefs.fps }, { prefs.fps = it }))

        body.addView(heading("Diagnostics"))
        val diag = Ui.text(c, "", 13f, Ui.MUTE)
        fun refreshDiag() {
            val d = com.nyx.themes.service.Diag
            diag.text = "Scene: ${d.scene}\nCanvas: ${d.mode}\nSurface: ${d.surface}\nFrame rate: ${"%.0f".format(d.fps)} fps\nBattery Saver: ${if (d.saver) "ON" else "off"}" + (if (d.error.isNotEmpty()) "\nLast error: ${d.error}" else "")
        }
        refreshDiag()
        body.addView(diag)
        body.addView(Controls.switchRow(c, "Compatibility mode", "Turn on if the live wallpaper looks wrong or stutters.", prefs.renderMode == 1) { prefs.renderMode = if (it) 1 else 0; true })
        body.addView(Ui.button(c, "Refresh") { refreshDiag() }, Ui.lp(Ui.WRAP, Ui.WRAP).apply { topMargin = Ui.dp(c, 8) })
        if ((c.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager).isPowerSaveMode)
            body.addView(note("Battery Saver is on. Android slows background drawing then, so turn it off if the wallpaper looks choppy."))

        body.addView(heading("Now"))
        val row = Ui.hbox(c)
        row.addView(Ui.button(c, "Use Nyx as live wallpaper", true) { WallpaperActions.openLivePicker(c) }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        row.addView(Ui.button(c, "Next wallpaper") {
            val s = controller.next()
            Toast.makeText(c, s.name, Toast.LENGTH_SHORT).show()
        }, Ui.lp(0, Ui.WRAP, 1f))
        body.addView(row)
        body.addView(note("\nBattery tip: if wallpapers stop changing in the background, set Nyx to \"Unrestricted\" battery use in the phone's app settings.").apply { setPadding(0, Ui.dp(c, 14), 0, 0) })
    }
}
