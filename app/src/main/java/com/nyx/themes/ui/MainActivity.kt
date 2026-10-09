package com.nyx.themes.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nyx.themes.home.HomeMode
import com.nyx.themes.scene.SceneMeta

class MainActivity : AppCompatActivity() {
    private lateinit var content: FrameLayout
    private lateinit var tabBar: LinearLayout
    private val tabs = mutableListOf<View>()
    private var current = 0
    private var wallpapers: WallpapersTab? = null
    private var home: HomeTab? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.BG
        window.navigationBarColor = Ui.BG
        val root = Ui.vbox(this).apply { setBackgroundColor(Ui.BG) }
        val header = Ui.hbox(this).apply { setPadding(Ui.dp(this@MainActivity, 18), Ui.dp(this@MainActivity, 14), Ui.dp(this@MainActivity, 18), Ui.dp(this@MainActivity, 6)) }
        header.addView(Ui.text(this, "Nyx", 34f, Ui.INK, display = true))
        root.addView(header, Ui.lp(Ui.MATCH, Ui.WRAP))
        content = FrameLayout(this)
        root.addView(content, Ui.lp(Ui.MATCH, 0, 1f))
        tabBar = Ui.hbox(this).apply { setBackgroundColor(Ui.PANEL); gravity = Gravity.CENTER }
        root.addView(tabBar, Ui.lp(Ui.MATCH, Ui.WRAP))
        setContentView(root)

        wallpapers = WallpapersTab(this) { open(it) }
        home = HomeTab(this)
        tabs += wallpapers!!; tabs += home!!; tabs += SettingsTab(this)
        current = savedInstanceState?.getInt("tab") ?: 0
        show(current)
    }

    private fun show(i: Int) {
        current = i
        content.removeAllViews()
        content.addView(tabs[i], FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH))
        tabBar.removeAllViews()
        listOf("Wallpapers", "Icons & Home", "Settings").forEachIndexed { idx, label ->
            val t = TextView(this).apply {
                text = label
                setTextColor(if (idx == i) Ui.AMBER else Ui.MUTE)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, Ui.dp(this@MainActivity, 14), 0, Ui.dp(this@MainActivity, 14))
                isClickable = true; isFocusable = true
                setOnClickListener { show(idx) }
            }
            tabBar.addView(t, Ui.lp(0, Ui.WRAP, 1f))
        }
    }

    private fun open(s: SceneMeta) = startActivity(Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_ID, s.id))

    override fun onResume() {
        super.onResume()
        HomeMode.apply(this)
        wallpapers?.refresh()
        home?.refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); outState.putInt("tab", current) }
}
