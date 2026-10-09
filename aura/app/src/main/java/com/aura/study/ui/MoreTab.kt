package com.aura.study.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import com.aura.study.*
import java.time.LocalDate

/** Stats, exam countdowns and settings. */
class MoreTab(ctx: Context, private val prefs: Prefs, private val store: Store, private val ai: Ai) : android.widget.ScrollView(ctx) {
    private val body = Ui.vbox(ctx)
    init {
        isFillViewport = true
        body.setPadding(Ui.dp(ctx, 20), Ui.dp(ctx, 8), Ui.dp(ctx, 20), Ui.dp(ctx, 28))
        addView(body, android.widget.FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
        build()
    }

    fun refresh() = build()

    private fun build() {
        val c = context; body.removeAllViews(); val today = Days.today()
        body.addView(Ui.title(c, "This week"))
        val byDay = store.minutesByDay(today - 6, today)
        val bars = BarsView(c).apply { values = (6 downTo 0).map { d -> Days.date(today - d).dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } to (byDay[today - d] ?: 0) } }
        body.addView(bars, Ui.lp(Ui.MATCH, Ui.dp(c, 150)))
        val all = store.minutesByDay(0, today).values.sum(); val week = byDay.values.sum()
        body.addView(Ui.text(c, "$week min this week  ·  ${all / 60}h ${all % 60}m all time  ·  ${store.sessionCount()} sessions", 13f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 8), 0, 0) })
        val subs = store.minutesBySubject(today - 6, today)
        if (subs.isNotEmpty()) { body.addView(Ui.title(c, "By subject")); subs.forEach { (s, m) -> body.addView(Ui.text(c, "$s  —  $m min", 15f, Ui.INK).apply { setPadding(0, Ui.dp(c, 3), 0, Ui.dp(c, 3)) }) } }

        body.addView(Ui.title(c, "Exams"))
        val exams = store.exams()
        if (exams.isEmpty()) body.addView(Ui.note(c, "Add an exam date and Aura counts the days down."))
        exams.forEach { e ->
            val left = e.day - today
            val row = Ui.hbox(c).apply { setPadding(Ui.dp(c, 14), Ui.dp(c, 12), Ui.dp(c, 14), Ui.dp(c, 12)); background = Ui.round(c, Ui.PANEL, 16, Ui.LINE) }
            val mid = Ui.vbox(c); mid.addView(Ui.text(c, e.name, 16f, Ui.INK, true)); mid.addView(Ui.text(c, Days.label(e.day), 12f, Ui.MUTE)); row.addView(mid, Ui.lp(0, Ui.WRAP, 1f))
            row.addView(Ui.text(c, if (left < 0) "past" else if (left == 0L) "today" else "$left d", 18f, if (left in 0..7) Ui.WARM else Ui.MINT, true))
            row.setOnLongClickListener { AlertDialog.Builder(c).setTitle("Delete \"${e.name}\"?").setPositiveButton("Delete") { _, _ -> store.deleteExam(e.id); build() }.setNegativeButton("Keep", null).show(); true }
            body.addView(row, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8, c))
        }
        body.addView(Ui.button(c, "Add exam") {
            val et = Ui.input(c, "Exam name (e.g. Chemistry)")
            AlertDialog.Builder(c).setTitle("New exam").setView(et).setPositiveButton("Pick date") { _, _ ->
                val n = et.text.toString().trim().ifEmpty { "Exam" }; val t = LocalDate.now().plusDays(14)
                DatePickerDialog(c, { _, y, m, d -> store.addExam(n, LocalDate.of(y, m + 1, d).toEpochDay()); build() }, t.year, t.monthValue - 1, t.dayOfMonth).show()
            }.setNegativeButton("Cancel", null).show()
        }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 4, 0, 0, c))

        body.addView(Ui.title(c, "AI key"))
        body.addView(Ui.note(c, "Paste your Claude API key from console.anthropic.com. It is saved only on this phone and sent only to the Claude API. AI features use your own account credit."))
        val key = Ui.input(c, if (ai.ready) "Key saved (paste a new one to replace)" else "sk-ant-…", password = true); body.addView(key)
        val status = Ui.text(c, "", 13f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 8), 0, 0) }
        val row = Ui.hbox(c)
        row.addView(Ui.button(c, "Save and test", true) {
            val k = key.text.toString().trim(); if (k.isNotEmpty()) prefs.apiKey = k
            if (!ai.ready) { status.text = "Enter a key first."; return@button }
            status.setTextColor(Ui.MUTE); status.text = "Testing…"
            ai.run({ ai.complete("Reply with the single word: ok", "ping", maxTokens = 10) }) { r -> post { r.onSuccess { status.setTextColor(Ui.MINT); status.text = "Works. The AI tools are on." }.onFailure { status.setTextColor(Ui.RED); status.text = it.message } } }
        }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        row.addView(Ui.button(c, "Remove key") { prefs.apiKey = ""; key.setText(""); status.text = "Key removed."; Toast.makeText(c, "Key removed", Toast.LENGTH_SHORT).show() }, Ui.lp(0, Ui.WRAP, 1f))
        body.addView(row, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 10, 0, 0, c)); body.addView(status)
        body.addView(Ui.note(c, "Model: Fast is cheapest, Best writes the most careful explanations.").apply { setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 6)) })
        body.addView(Ui.choice(c, Prefs.MODELS, { prefs.model }) { prefs.model = it })

        body.addView(Ui.title(c, "Timer"))
        body.addView(Ui.choice(c, listOf("Auto-start next round" to true, "Wait for me" to false), { prefs.autoNext }) { prefs.autoNext = it })
        body.addView(Ui.note(c, "\nOn some phones you need to allow notifications and set Aura to Unrestricted battery use so the timer keeps running with the screen off.").apply { setPadding(0, Ui.dp(c, 8), 0, 0) })
    }
}
