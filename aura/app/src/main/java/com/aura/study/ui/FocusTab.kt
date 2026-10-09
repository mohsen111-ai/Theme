package com.aura.study.ui

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.aura.study.*

/** Pomodoro timer with ambient sound. */
class FocusTab(ctx: Context, private val prefs: Prefs, private val store: Store) : android.widget.ScrollView(ctx) {
    private val ring = RingView(ctx)
    private val startBtn = Ui.button(ctx, "Start", true) { toggle() }
    private val today = Ui.text(ctx, "", 14f, Ui.MUTE)
    private val body = Ui.vbox(ctx)
    private val listener: () -> Unit = { refresh() }

    init {
        isFillViewport = true
        val c = ctx
        body.setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), Ui.dp(c, 28))
        addView(body, android.widget.FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
        body.addView(ring, Ui.lpm(Ui.WRAP, Ui.WRAP, 28, 8, 28, 8, c).apply { gravity = Gravity.CENTER_HORIZONTAL })
        val row = Ui.hbox(c)
        row.addView(Ui.button(c, "Reset") { TimerService.send(c, TimerService.ACTION_STOP) }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        row.addView(startBtn, Ui.lp(0, Ui.WRAP, 1.6f))
        row.addView(Ui.button(c, "Skip") { if (TimerState.running || TimerState.focusDone > 0 || TimerState.phase != Phase.FOCUS) TimerService.send(c, TimerService.ACTION_SKIP) }, Ui.lp(0, Ui.WRAP, 1f).apply { leftMargin = Ui.dp(c, 8) })
        body.addView(row)
        today.gravity = Gravity.CENTER; body.addView(today, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 14, 0, 0, c))

        body.addView(Ui.title(c, "What are you studying?"))
        body.addView(subjects())
        body.addView(Ui.title(c, "Session length"))
        body.addView(lengths())
        body.addView(Ui.title(c, "Background sound"))
        body.addView(Ui.choice(c, Sounds.KINDS.map { it.second to it.first }, { prefs.sound }) { prefs.sound = it; if (TimerState.running && TimerState.phase == Phase.FOCUS) TimerService.send(c, TimerService.ACTION_START) })
        val vol = Ui.hbox(c); vol.addView(Ui.text(c, "Volume", 13f, Ui.MUTE), Ui.lp(Ui.WRAP, Ui.WRAP).apply { rightMargin = Ui.dp(c, 12) })
        vol.addView(SeekBar(c).apply { max = 100; progress = prefs.volume
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) { prefs.volume = p }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) { if (TimerState.running && TimerState.phase == Phase.FOCUS) TimerService.send(c, TimerService.ACTION_START) }
            }) }, Ui.lp(0, Ui.WRAP, 1f))
        body.addView(vol, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0, c))
        body.addView(Ui.note(c, "Sounds are made on your phone, so they work offline and loop forever.").apply { setPadding(0, Ui.dp(c, 6), 0, 0) })
        refresh()
    }

    private fun toggle() { TimerService.send(context, if (TimerState.running) TimerService.ACTION_PAUSE else TimerService.ACTION_START) }

    private fun subjects(): android.view.View {
        val c = context; val (sv, row) = Ui.chipRow(c)
        fun draw() {
            row.removeAllViews()
            prefs.subjects.forEach { s -> row.addView(Ui.chip(c, s, s == prefs.subject) { prefs.subject = s; draw() }, Ui.lp(Ui.WRAP, Ui.WRAP).apply { rightMargin = Ui.dp(c, 8) }) }
            row.addView(Ui.chip(c, "+ Add", false) {
                val et = Ui.input(c, "Subject name")
                AlertDialog.Builder(c).setTitle("New subject").setView(et).setPositiveButton("Add") { _, _ -> val n = et.text.toString().trim(); if (n.isNotEmpty()) { prefs.subjects = (prefs.subjects + n).distinct(); prefs.subject = n; draw() } }.setNegativeButton("Cancel", null).show()
            })
        }
        draw(); return sv
    }

    private fun lengths(): android.view.View {
        val c = context
        val presets = listOf("25 / 5" to (25 to 5), "50 / 10" to (50 to 10), "15 / 3" to (15 to 3), "90 / 15" to (90 to 15))
        val (sv, row) = Ui.chipRow(c)
        fun draw() {
            row.removeAllViews()
            presets.forEach { (label, v) -> row.addView(Ui.chip(c, label, prefs.focusMin == v.first && prefs.breakMin == v.second) { set(v.first, v.second); draw() }, Ui.lp(Ui.WRAP, Ui.WRAP).apply { rightMargin = Ui.dp(c, 8) }) }
            row.addView(Ui.chip(c, "Custom", presets.none { prefs.focusMin == it.second.first && prefs.breakMin == it.second.second }) {
                val box = Ui.vbox(c).apply { setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), 0) }
                val f = Ui.input(c, "Focus minutes").apply { inputType = InputType.TYPE_CLASS_NUMBER; setText("${prefs.focusMin}") }
                val b = Ui.input(c, "Break minutes").apply { inputType = InputType.TYPE_CLASS_NUMBER; setText("${prefs.breakMin}") }
                box.addView(f); box.addView(Ui.gap(c, 10)); box.addView(b)
                AlertDialog.Builder(c).setTitle("Custom length").setView(box).setPositiveButton("Set") { _, _ ->
                    set((f.text.toString().toIntOrNull() ?: 25).coerceIn(1, 240), (b.text.toString().toIntOrNull() ?: 5).coerceIn(1, 60)); draw() }.setNegativeButton("Cancel", null).show()
            })
        }
        draw(); return sv
    }

    private fun set(f: Int, b: Int) {
        prefs.focusMin = f; prefs.breakMin = b
        if (!TimerState.running) { TimerState.reset(prefs); TimerState.changed() }
    }

    fun refresh() {
        val rem = TimerState.remaining() / 1000
        ring.label = "%02d:%02d".format(rem / 60, rem % 60)
        ring.sub = TimerState.phase.label
        ring.accent = if (TimerState.phase == Phase.FOCUS) Ui.MINT else Ui.WARM
        ring.progress = TimerState.progress()
        startBtn.text = if (TimerState.running) "Pause" else if (TimerState.remainingAtPause < TimerState.totalMs) "Resume" else "Start"
        val mins = store.minutesByDay(Days.today(), Days.today())[Days.today()] ?: 0
        today.text = "Today: $mins min focused  ·  ${TimerState.focusDone} session${if (TimerState.focusDone == 1) "" else "s"} in this round"
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); TimerState.listen(listener); refresh() }
    override fun onDetachedFromWindow() { TimerState.unlisten(listener); super.onDetachedFromWindow() }
}
