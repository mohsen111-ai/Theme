package com.aura.study.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.aura.study.*

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    private lateinit var store: Store
    private lateinit var ai: Ai
    private lateinit var tabs: List<View>
    private lateinit var navItems: List<TextView>
    private lateinit var content: FrameLayout
    private lateinit var streakChip: TextView
    private var current = 0
    private var imageCallback: ((ByteArray?) -> Unit)? = null

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val cb = imageCallback; imageCallback = null
        if (cb != null) { if (uri == null) cb(null) else Thread { cb(AiTab.compress(this, uri)) }.start() }
    }
    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this); store = Store(this); ai = Ai(prefs)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!TimerState.running && TimerState.focusDone == 0 && TimerState.phase == Phase.FOCUS) TimerState.reset(prefs)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Ui.BG) }
        val header = Ui.hbox(this).apply { setPadding(Ui.dp(this@MainActivity, 20), Ui.dp(this@MainActivity, 14), Ui.dp(this@MainActivity, 20), Ui.dp(this@MainActivity, 6)) }
        header.addView(Ui.text(this, "Aura", 26f, Ui.INK, true), Ui.lp(0, Ui.WRAP, 1f))
        streakChip = Ui.text(this, "", 14f, Ui.WARM, true).apply { setPadding(Ui.dp(this@MainActivity, 12), Ui.dp(this@MainActivity, 6), Ui.dp(this@MainActivity, 12), Ui.dp(this@MainActivity, 6)); background = Ui.round(this@MainActivity, Ui.PANEL, 16, Ui.LINE) }
        header.addView(streakChip)
        root.addView(header)
        content = FrameLayout(this); root.addView(content, Ui.lp(Ui.MATCH, 0, 1f))

        lateinit var cards: CardsTab; lateinit var habits: HabitsTab; lateinit var more: MoreTab
        val changed = { updateStreak(); habits.build(); more.refresh(); cards.refresh() }
        habits = HabitsTab(this, prefs, store) { updateStreak(); more.refresh(); cards.refresh() }
        cards = CardsTab(this, store, ai) { updateStreak(); habits.build(); more.refresh() }
        more = MoreTab(this, prefs, store, ai)
        val aiTab = AiTab(this, prefs, store, ai, { cb -> imageCallback = cb; picker.launch("image/*") }) { changed() }
        tabs = listOf(FocusTab(this, prefs, store), habits, cards, aiTab, more)
        tabs.forEach { content.addView(it, FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH)) }

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setBackgroundColor(Ui.PANEL); setPadding(0, Ui.dp(this@MainActivity, 6), 0, Ui.dp(this@MainActivity, 6)) }
        val names = listOf("Focus", "Habits", "Cards", "AI", "More")
        navItems = names.mapIndexed { i, n -> Ui.text(this, n, 13f, Ui.MUTE, true).apply { gravity = Gravity.CENTER; setPadding(0, Ui.dp(this@MainActivity, 12), 0, Ui.dp(this@MainActivity, 12)); setOnClickListener { select(i) } }.also { nav.addView(it, Ui.lp(0, Ui.WRAP, 1f)) } }
        root.addView(nav)
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets -> val b = insets.getInsets(WindowInsetsCompat.Type.systemBars()); v.setPadding(0, b.top, 0, b.bottom); insets }
        select(0); updateStreak()

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        TimerState.listen { updateStreak() }
    }

    private fun select(i: Int) { current = i; tabs.forEachIndexed { k, v -> v.visibility = if (k == i) View.VISIBLE else View.GONE }; navItems.forEachIndexed { k, t -> t.setTextColor(if (k == i) Ui.MINT else Ui.MUTE) }
        (tabs[i] as? AiTab)?.refresh(); (tabs[i] as? HabitsTab)?.build(); (tabs[i] as? MoreTab)?.refresh() }

    private fun updateStreak() { val s = Gamify.streak(store.activeDays(), Days.today()); streakChip.text = "🔥 $s" }

    override fun onResume() { super.onResume(); updateStreak() }
}
