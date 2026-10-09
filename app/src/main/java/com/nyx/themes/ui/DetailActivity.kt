package com.nyx.themes.ui

import androidx.appcompat.app.AlertDialog
import android.app.WallpaperManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nyx.themes.data.Prefs
import com.nyx.themes.scene.Mode
import com.nyx.themes.scene.SceneRepo

/** Full-screen preview of one wallpaper with the actions to apply it. */
class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = SceneRepo.get(this)
        val scene = repo.get(intent.getStringExtra(EXTRA_ID))
        if (scene == null) { finish(); return }
        val prefs = Prefs(this)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val preview = ScenePreviewView(this).also { it.scene = scene }
        root.addView(preview, Ui.fl(Ui.MATCH, Ui.MATCH))

        val back = Ui.text(this, "‹ Back", 16f, Ui.INK, bold = true).apply {
            setPadding(Ui.dp(this@DetailActivity, 14), Ui.dp(this@DetailActivity, 8), Ui.dp(this@DetailActivity, 16), Ui.dp(this@DetailActivity, 8))
            background = Ui.round(this@DetailActivity, 0x99090B14.toInt(), 20f)
            setOnClickListener { finish() }
            isClickable = true
        }
        root.addView(back, Ui.fl(Ui.WRAP, Ui.WRAP).apply { gravity = Gravity.TOP or Gravity.START; setMargins(Ui.dp(this@DetailActivity, 12), Ui.dp(this@DetailActivity, 12), 0, 0) })

        val panel = Ui.vbox(this).apply {
            setPadding(Ui.dp(this@DetailActivity, 18), Ui.dp(this@DetailActivity, 40), Ui.dp(this@DetailActivity, 18), Ui.dp(this@DetailActivity, 18))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x00000000, 0xE6070814.toInt()))
        }
        panel.addView(Ui.text(this, scene.name, 28f, Ui.INK, display = true))
        val kind = when (scene.mode) { Mode.STILL -> "Still"; Mode.LIVE -> "Live"; Mode.LONG -> "Long animation" }
        panel.addView(Ui.text(this, "$kind · ${if (scene.isDark) "Dark" else "Light"} · ${if (scene.style == "min") "Minimal" else "Ink sketch"}", 13f, Ui.MUTE).apply { setPadding(0, 0, 0, Ui.dp(this@DetailActivity, 12)) })
        val row = Ui.hbox(this)
        val fav = Ui.button(this, if (scene.id in prefs.favorites) "♥" else "♡") { }
        fav.setOnClickListener { fav.text = if (prefs.toggleFavorite(scene.id)) "♥" else "♡" }
        row.addView(Ui.button(this, "Set wallpaper", true) { choose(scene) }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(this@DetailActivity, 10) })
        row.addView(fav, Ui.lp(Ui.WRAP, Ui.WRAP))
        panel.addView(row)
        root.addView(panel, Ui.fl(Ui.MATCH, Ui.WRAP).apply { gravity = Gravity.BOTTOM })
        setContentView(root)
    }

    private fun choose(scene: com.nyx.themes.scene.SceneMeta) {
        val live = if (scene.isAnimated) "Nyx live wallpaper (moves)" else "Nyx live wallpaper (can change automatically)"
        val items = arrayOf(live, "Image on home screen", "Image on lock screen", "Image on both")
        AlertDialog.Builder(this).setTitle(scene.name).setItems(items) { _, which ->
            when (which) {
                0 -> WallpaperActions.applyLive(this, scene)
                else -> {
                    val flags = when (which) { 1 -> WallpaperManager.FLAG_SYSTEM; 2 -> WallpaperManager.FLAG_LOCK; else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK }
                    Toast.makeText(this, "Setting wallpaper…", Toast.LENGTH_SHORT).show()
                    WallpaperActions.applyImage(this, scene, flags) { ok ->
                        Toast.makeText(this, if (ok) "Wallpaper set" else "Could not set the wallpaper", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.show()
    }

    companion object { const val EXTRA_ID = "scene_id" }
}
