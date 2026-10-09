package com.aura.study.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.aura.study.*

/** Streak, level, habit check-ins and the activity heat map. */
class HabitsTab(ctx: Context, private val prefs: Prefs, private val store: Store, private val onChange: () -> Unit) : android.widget.ScrollView(ctx) {
    private val body = Ui.vbox(ctx)
    init {
        isFillViewport = true
        body.setPadding(Ui.dp(ctx, 20), Ui.dp(ctx, 8), Ui.dp(ctx, 20), Ui.dp(ctx, 28))
        addView(body, android.widget.FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
        build()
    }

    fun build() {
        val c = context; body.removeAllViews()
        val today = Days.today(); val active = store.activeDays(); val streak = Gamify.streak(active, today); val best = Gamify.bestStreak(active)
        val xp = store.xp(); val level = Gamify.level(xp)

        val top = Ui.card(c)
        val r = Ui.hbox(c)
        r.addView(Ui.text(c, "$streak", 56f, Ui.WARM, true), Ui.lp(Ui.WRAP, Ui.WRAP).apply { rightMargin = Ui.dp(c, 12) })
        val side = Ui.vbox(c); side.addView(Ui.text(c, if (streak == 1) "day streak" else "day streak", 16f, Ui.INK, true)); side.addView(Ui.text(c, "Best: $best days", 13f, Ui.MUTE)); r.addView(side)
        top.addView(r)
        top.addView(Ui.gap(c, 10))
        top.addView(Ui.text(c, "Level $level · ${Gamify.title(level)}", 15f, Ui.INK, true))
        val bar = object : View(c) {
            val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            override fun onDraw(cv: android.graphics.Canvas) { val h = height.toFloat(); p.color = Ui.PANEL_HI; cv.drawRoundRect(0f, 0f, width.toFloat(), h, h / 2, h / 2, p); p.color = Ui.MINT; cv.drawRoundRect(0f, 0f, width * Gamify.levelProgress(xp).coerceAtLeast(.03f), h, h / 2, h / 2, p) }
        }
        top.addView(bar, Ui.lpm(Ui.MATCH, Ui.dp(c, 10), 0, 8, 0, 4, c).apply { height = Ui.dp(c, 10) })
        top.addView(Ui.text(c, "$xp XP · ${Gamify.xpFor(level + 1) - xp} to level ${level + 1}", 12f, Ui.MUTE))
        val freezes = Gamify.freezesEarned(best) - prefs.freezesUsed
        if (freezes > 0 && (today - 1) !in active && today !in active) top.addView(Ui.button(c, "Use a streak freeze for yesterday ($freezes left)") { store.freeze(today - 1); prefs.freezesUsed = prefs.freezesUsed + 1; onChange(); build() }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
        else top.addView(Ui.text(c, "Freezes available: ${freezes.coerceAtLeast(0)} (earn one for every 7 day streak)", 12f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 8), 0, 0) })
        body.addView(top)

        body.addView(Ui.title(c, "Last 12 weeks"))
        val heat = HeatmapView(c); heat.today = today
        val mins = store.minutesByDay(today - 100, today); val habitCount = HashMap<Long, Int>()
        store.habits().forEach { h -> store.habitDays(h.id).forEach { habitCount[it] = (habitCount[it] ?: 0) + 1 } }
        heat.levels = active.associateWith { d -> val score = (mins[d] ?: 0) + (habitCount[d] ?: 0) * 15; when { score >= 120 -> 4; score >= 60 -> 3; score >= 25 -> 2; else -> 1 } }
        body.addView(heat, Ui.lp(Ui.MATCH, Ui.WRAP))

        body.addView(Ui.title(c, "Daily habits"))
        val habits = store.habits()
        if (habits.isEmpty()) body.addView(Ui.note(c, "Add a small habit you want to keep, like \"Read 10 pages\". Tick it each day to grow your streak."))
        habits.forEach { h ->
            val days = store.habitDays(h.id); val done = today in days
            val row = Ui.hbox(c).apply { setPadding(Ui.dp(c, 14), Ui.dp(c, 12), Ui.dp(c, 14), Ui.dp(c, 12)); background = Ui.round(c, Ui.PANEL, 16, Ui.LINE) }
            val box = Ui.text(c, if (done) "✓" else "", 18f, Ui.ON_ACCENT, true).apply { gravity = Gravity.CENTER; background = Ui.round(c, if (done) Ui.MINT else 0, 14, if (done) Ui.MINT else Ui.MUTE) }
            row.addView(box, Ui.lp(Ui.dp(c, 30), Ui.dp(c, 30)).apply { rightMargin = Ui.dp(c, 12) })
            val mid = Ui.vbox(c); mid.addView(Ui.text(c, h.name, 16f, Ui.INK, true))
            mid.addView(Ui.text(c, (6 downTo 0).joinToString(" ") { if ((today - it) in days) "●" else "○" } + "   ${Gamify.streak(days, today)} day run", 12f, Ui.MUTE))
            row.addView(mid, Ui.lp(0, Ui.WRAP, 1f))
            row.setOnClickListener { store.toggleHabit(h.id, today); onChange(); build() }
            row.setOnLongClickListener { AlertDialog.Builder(c).setTitle("Delete \"${h.name}\"?").setPositiveButton("Delete") { _, _ -> store.deleteHabit(h.id); onChange(); build() }.setNegativeButton("Keep", null).show(); true }
            body.addView(row, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8, c))
        }
        val add = Ui.hbox(c); val et = Ui.input(c, "New habit")
        add.addView(et, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        add.addView(Ui.button(c, "Add", true) { val n = et.text.toString().trim(); if (n.isNotEmpty()) { store.addHabit(n); build() } })
        body.addView(add, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 6, 0, 0, c))
        body.addView(Ui.note(c, "Long press a habit to delete it.").apply { setPadding(0, Ui.dp(c, 8), 0, 0) })
    }
}
