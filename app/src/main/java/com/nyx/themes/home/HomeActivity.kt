package com.nyx.themes.home

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nyx.themes.data.Prefs
import com.nyx.themes.ui.Ui

/**
 * Nyx Home: a deliberately simple launcher. Clock, pinned apps, a dock, and an app drawer. Every icon comes from the
 * selected icon pack. It only exists as a home app when "Wallpapers only" is off (see [HomeMode]).
 */
class HomeActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    private lateinit var themer: IconThemer
    private var apps: List<AppEntry> = emptyList()
    private lateinit var pinnedAdapter: AppAdapter
    private lateinit var drawerAdapter: AppAdapter
    private lateinit var dockRow: LinearLayout
    private lateinit var drawer: FrameLayout
    private lateinit var searchBox: EditText
    private var drawerOpen = false
    private var dock: List<AppEntry> = emptyList()
    private val iconPx get() = Ui.dp(this, 56)

    private val pkgReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { reload() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        themer = IconThemer(this, prefs.iconPack)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val root = FrameLayout(this)
        val column = Ui.vbox(this)
        root.addView(column, Ui.fl(Ui.MATCH, Ui.MATCH))

        // clock
        val clock = Ui.vbox(this).apply { setPadding(Ui.dp(this@HomeActivity, 26), Ui.dp(this@HomeActivity, 28), Ui.dp(this@HomeActivity, 26), Ui.dp(this@HomeActivity, 10)) }
        val time = TextClock(this).apply {
            format12Hour = "h:mm"; format24Hour = "HH:mm"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 64f); setTextColor(Color.WHITE); typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
            setShadowLayer(8f, 0f, 2f, 0x99000000.toInt())
        }
        val date = TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"; format24Hour = "EEEE, d MMMM"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f); setTextColor(0xE6FFFFFF.toInt()); setShadowLayer(6f, 0f, 1f, 0x99000000.toInt())
        }
        clock.addView(time); clock.addView(date)
        column.addView(clock, Ui.lp(Ui.MATCH, Ui.WRAP))

        // pinned grid
        pinnedAdapter = AppAdapter(compact = false)
        val pinned = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 4)
            adapter = pinnedAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
            setPadding(Ui.dp(this@HomeActivity, 10), 0, Ui.dp(this@HomeActivity, 10), 0)
            clipToPadding = false
        }
        column.addView(pinned, Ui.lp(Ui.MATCH, 0, 1f))

        // swipe-up handle + dock
        val handle = TextView(this).apply {
            text = "⌃"; gravity = Gravity.CENTER; setTextColor(0xCCFFFFFF.toInt()); textSize = 22f
            setShadowLayer(6f, 0f, 1f, 0x99000000.toInt()); setOnClickListener { openDrawer() }
            contentDescription = "All apps"
        }
        column.addView(handle, Ui.lp(Ui.MATCH, Ui.dp(this, 34)))
        dockRow = Ui.hbox(this).apply {
            gravity = Gravity.CENTER
            setPadding(Ui.dp(this@HomeActivity, 12), Ui.dp(this@HomeActivity, 10), Ui.dp(this@HomeActivity, 12), Ui.dp(this@HomeActivity, 12))
            background = Ui.round(this@HomeActivity, 0x66090B1E, 28f)
        }
        column.addView(dockRow, Ui.lp(Ui.MATCH, Ui.WRAP).apply { setMargins(Ui.dp(this@HomeActivity, 14), 0, Ui.dp(this@HomeActivity, 14), Ui.dp(this@HomeActivity, 10)) })

        // drawer
        drawer = FrameLayout(this).apply { setBackgroundColor(0xF2090A1A.toInt()); visibility = View.GONE }
        val dcol = Ui.vbox(this).apply { setPadding(Ui.dp(this@HomeActivity, 12), Ui.dp(this@HomeActivity, 40), Ui.dp(this@HomeActivity, 12), 0) }
        searchBox = EditText(this).apply {
            hint = "Search apps"; setHintTextColor(Ui.MUTE); setTextColor(Ui.INK); textSize = 16f; isSingleLine = true
            background = Ui.round(this@HomeActivity, Ui.PANEL_HI, 26f, Ui.LINE)
            setPadding(Ui.dp(this@HomeActivity, 18), Ui.dp(this@HomeActivity, 12), Ui.dp(this@HomeActivity, 18), Ui.dp(this@HomeActivity, 12))
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { drawerAdapter.submit(filtered(s?.toString().orEmpty())) }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        dcol.addView(searchBox, Ui.lp(Ui.MATCH, Ui.WRAP))
        drawerAdapter = AppAdapter(compact = false)
        val dlist = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 4); adapter = drawerAdapter
            clipToPadding = false; setPadding(0, Ui.dp(this@HomeActivity, 12), 0, Ui.dp(this@HomeActivity, 24))
        }
        dcol.addView(dlist, Ui.lp(Ui.MATCH, 0, 1f))
        drawer.addView(dcol, Ui.fl(Ui.MATCH, Ui.MATCH))
        root.addView(drawer, Ui.fl(Ui.MATCH, Ui.MATCH))
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            column.setPadding(0, bars.top, 0, bars.bottom)
            dcol.setPadding(Ui.dp(this, 12), bars.top + Ui.dp(this, 12), Ui.dp(this, 12), bars.bottom)
            insets
        }

        // swipe up on the home area opens the drawer
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (vy < -900f) { openDrawer(); return true }
                return false
            }
            override fun onLongPress(e: MotionEvent) {
                runCatching { startActivity(Intent(Intent.ACTION_SET_WALLPAPER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        })
        clock.setOnTouchListener { _, ev -> detector.onTouchEvent(ev) }
        pinned.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean { detector.onTouchEvent(e); return false }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (drawerOpen) closeDrawer() }
        })

        ContextCompat.registerReceiver(this, pkgReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED); addAction(Intent.ACTION_PACKAGE_REMOVED); addAction(Intent.ACTION_PACKAGE_REPLACED); addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        reload()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (drawerOpen) closeDrawer()
    }

    override fun onResume() {
        super.onResume()
        if (themer.pack != prefs.iconPack) { themer = IconThemer(this, prefs.iconPack) }
        reload()
    }

    override fun onDestroy() { runCatching { unregisterReceiver(pkgReceiver) }; super.onDestroy() }

    private fun reload() {
        apps = AppRepo.load(this)
        dock = AppRepo.defaultDock(this, apps).take(4)
        if (!prefs.homePinsInitialised) { prefs.homePins = AppRepo.defaultPins(apps, dock); prefs.homePinsInitialised = true }
        val byKey = apps.associateBy { it.key }
        pinnedAdapter.submit(prefs.homePins.mapNotNull { byKey[it] })
        drawerAdapter.submit(filtered(searchBox.text?.toString().orEmpty()))
        dockRow.removeAllViews()
        dock.forEach { app ->
            val cell = appCell(app)
            dockRow.addView(cell, Ui.lp(0, Ui.WRAP, 1f))
        }
    }

    private fun filtered(q: String): List<AppEntry> = if (q.isBlank()) apps else apps.filter { it.label.contains(q.trim(), ignoreCase = true) }

    private fun openDrawer() {
        if (drawerOpen) return
        drawerOpen = true
        drawer.visibility = View.VISIBLE
        drawer.translationY = resources.displayMetrics.heightPixels.toFloat()
        ObjectAnimator.ofFloat(drawer, View.TRANSLATION_Y, 0f).setDuration(220).start()
    }

    private fun closeDrawer() {
        drawerOpen = false
        searchBox.setText("")
        searchBox.clearFocus()
        (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(searchBox.windowToken, 0)
        ObjectAnimator.ofFloat(drawer, View.TRANSLATION_Y, resources.displayMetrics.heightPixels.toFloat()).setDuration(200).apply {
            addListener(object : android.animation.AnimatorListenerAdapter() { override fun onAnimationEnd(a: android.animation.Animator) { if (!drawerOpen) drawer.visibility = View.GONE } })
        }.start()
    }

    private fun launch(app: AppEntry) {
        try {
            startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(app.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED))
            if (drawerOpen) closeDrawer()
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun menu(anchor: View, app: AppEntry) {
        val pinned = app.key in prefs.homePins
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, if (pinned) "Remove from home" else "Pin to home")
            menu.add(0, 2, 1, "App info")
            menu.add(0, 3, 2, "Uninstall")
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> { prefs.homePins = if (pinned) prefs.homePins - app.key else (prefs.homePins + app.key).distinct(); reload() }
                    2 -> runCatching { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.pkg}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    3 -> runCatching { startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.pkg}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
                true
            }
            show()
        }
    }

    private fun appCell(app: AppEntry): View {
        val cell = Ui.vbox(this).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(Ui.dp(this@HomeActivity, 2), Ui.dp(this@HomeActivity, 8), Ui.dp(this@HomeActivity, 2), Ui.dp(this@HomeActivity, 8))
            isClickable = true; isFocusable = true
            contentDescription = app.label
        }
        val iv = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        themer.load(app, iconPx, iv)
        cell.addView(iv, Ui.lp(iconPx, iconPx))
        cell.addView(TextView(this).apply {
            text = app.label; setTextColor(Color.WHITE); textSize = 12f; maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END; gravity = Gravity.CENTER
            setShadowLayer(5f, 0f, 1f, 0xCC000000.toInt()); setPadding(0, Ui.dp(this@HomeActivity, 4), 0, 0)
        }, Ui.lp(Ui.MATCH, Ui.WRAP))
        cell.setOnClickListener { launch(app) }
        cell.setOnLongClickListener { menu(cell, app); true }
        return cell
    }

    private inner class AppAdapter(val compact: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var items: List<AppEntry> = emptyList()
        fun submit(l: List<AppEntry>) { items = l; notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(FrameLayout(parent.context)) {}
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val frame = holder.itemView as FrameLayout
            frame.removeAllViews()
            frame.addView(appCell(items[position]), FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
            frame.layoutParams = RecyclerView.LayoutParams(Ui.MATCH, Ui.WRAP)
        }
        override fun getItemCount() = items.size
    }
}
