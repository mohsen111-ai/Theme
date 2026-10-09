package com.aura.study.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Toast
import com.aura.study.*

/** Flashcards with spaced repetition. Cards can be typed in or generated from notes with the AI. */
class CardsTab(ctx: Context, private val store: Store, private val ai: Ai, private val onChange: () -> Unit) : android.widget.ScrollView(ctx) {
    private val body = Ui.vbox(ctx)
    private var queue: List<Card> = emptyList()
    private var index = 0
    private var revealed = false
    private var reviewed = 0
    private var reviewing = false

    init {
        isFillViewport = true
        body.setPadding(Ui.dp(ctx, 20), Ui.dp(ctx, 8), Ui.dp(ctx, 20), Ui.dp(ctx, 28))
        addView(body, android.widget.FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
        overview()
    }

    fun refresh() { if (!reviewing) overview() }

    private fun overview() {
        reviewing = false
        val c = context; body.removeAllViews()
        val today = Days.today(); val all = store.cards(); val due = all.filter { it.due <= today }
        val card = Ui.card(c)
        card.addView(Ui.text(c, "${due.size}", 48f, if (due.isEmpty()) Ui.MUTE else Ui.MINT, true))
        card.addView(Ui.text(c, if (due.isEmpty()) "Nothing due. Nice." else "cards due today", 15f, Ui.INK))
        card.addView(Ui.text(c, "${all.size} cards in ${store.decks().size} deck${if (store.decks().size == 1) "" else "s"}", 13f, Ui.MUTE))
        if (due.isNotEmpty()) card.addView(Ui.button(c, "Start review", true) { startReview(due) }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 14, 0, 0, c))
        body.addView(card)
        val row = Ui.hbox(c)
        row.addView(Ui.button(c, "Add card") { addCard() }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        row.addView(Ui.button(c, "Cards from notes (AI)") { fromNotes() }, Ui.lp(0, Ui.WRAP, 1.4f))
        body.addView(row, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
        if (all.isNotEmpty()) {
            body.addView(Ui.title(c, "All cards"))
            all.sortedBy { it.due }.forEach { k ->
                val r = Ui.vbox(c).apply { setPadding(Ui.dp(c, 14), Ui.dp(c, 10), Ui.dp(c, 14), Ui.dp(c, 10)); background = Ui.round(c, Ui.PANEL, 14, Ui.LINE) }
                r.addView(Ui.text(c, k.front, 15f, Ui.INK, true)); r.addView(Ui.text(c, k.back, 13f, Ui.MUTE))
                r.addView(Ui.text(c, "${k.deck} · " + (if (k.due <= today) "due now" else "next ${Days.label(k.due)}"), 11f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 4), 0, 0) })
                r.setOnLongClickListener { AlertDialog.Builder(c).setTitle("Delete this card?").setPositiveButton("Delete") { _, _ -> store.deleteCard(k.id); onChange(); overview() }.setNegativeButton("Keep", null).show(); true }
                body.addView(r, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8, c))
            }
            body.addView(Ui.note(c, "Long press a card to delete it."))
        }
    }

    private fun startReview(due: List<Card>) { queue = due.shuffled(); index = 0; reviewed = 0; reviewing = true; showCard() }

    private fun showCard() {
        val c = context; body.removeAllViews()
        if (index >= queue.size) {
            body.addView(Ui.title(c, "Round done")); body.addView(Ui.text(c, "You reviewed $reviewed card${if (reviewed == 1) "" else "s"}. It counts toward your streak.", 15f, Ui.INK))
            body.addView(Ui.button(c, "Back", true) { onChange(); overview() }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 16, 0, 0, c)); return
        }
        val k = queue[index]; revealed = false
        body.addView(Ui.note(c, "${index + 1} of ${queue.size} · ${k.deck}"))
        val face = Ui.card(c).apply { minimumHeight = Ui.dp(c, 220); gravity = Gravity.CENTER }
        face.addView(Ui.text(c, k.front, 22f, Ui.INK, true).apply { gravity = Gravity.CENTER })
        val ans = Ui.text(c, k.back, 18f, Ui.MINT).apply { gravity = Gravity.CENTER; visibility = android.view.View.GONE; setPadding(0, Ui.dp(c, 18), 0, 0) }
        face.addView(ans)
        body.addView(face, Ui.lp(Ui.MATCH, Ui.WRAP))
        val actions = Ui.vbox(c); body.addView(actions, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 14, 0, 0, c))
        actions.addView(Ui.button(c, "Show answer", true) {
            ans.visibility = android.view.View.VISIBLE; actions.removeAllViews()
            val g = Ui.hbox(c)
            listOf("Again" to 0, "Hard" to 1, "Good" to 2, "Easy" to 3).forEach { (label, grade) ->
                g.addView(Ui.button(c, label, grade == 2) { store.gradeCard(k, grade, Days.today()); reviewed++; index++; if (grade == 0) queue = queue + k.copy(due = Days.today()); onChange(); showCard() }, Ui.lp(0, Ui.WRAP, 1f).apply { setMargins(Ui.dp(c, 3), 0, Ui.dp(c, 3), 0) })
            }
            actions.addView(g)
        })
        actions.addView(Ui.button(c, "Stop") { onChange(); overview() }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0, c))
    }

    private fun addCard() {
        val c = context; val box = Ui.vbox(c).apply { setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), 0) }
        val deck = Ui.input(c, "Deck (e.g. Biology)").apply { setText(store.decks().firstOrNull() ?: "General") }; val f = Ui.input(c, "Front: question or term", 2); val b = Ui.input(c, "Back: answer", 2)
        box.addView(deck); box.addView(Ui.gap(c, 8)); box.addView(f); box.addView(Ui.gap(c, 8)); box.addView(b)
        AlertDialog.Builder(c).setTitle("New card").setView(box).setPositiveButton("Save") { _, _ ->
            if (f.text.isNotBlank() && b.text.isNotBlank()) { store.addCard(deck.text.toString().ifBlank { "General" }.trim(), f.text.toString(), b.text.toString(), Days.today()); onChange(); overview() } }.setNegativeButton("Cancel", null).show()
    }

    private fun fromNotes() {
        val c = context
        if (!ai.ready) { Toast.makeText(c, "Add your API key in More > Settings first.", Toast.LENGTH_LONG).show(); return }
        val box = Ui.vbox(c).apply { setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), 0) }
        val deck = Ui.input(c, "Deck name").apply { setText(store.decks().firstOrNull() ?: "General") }; val notes = Ui.input(c, "Paste your notes here", 6)
        box.addView(deck); box.addView(Ui.gap(c, 8)); box.addView(notes)
        AlertDialog.Builder(c).setTitle("Cards from notes").setView(box).setPositiveButton("Make cards") { _, _ ->
            val text = notes.text.toString(); val name = deck.text.toString().ifBlank { "General" }.trim()
            if (text.isBlank()) return@setPositiveButton
            Toast.makeText(c, "Making cards…", Toast.LENGTH_SHORT).show()
            ai.run({ ai.cards(text, 12) }) { r -> post {
                r.onSuccess { list -> list.forEach { store.addCard(name, it.front, it.back, Days.today()) }; onChange(); overview(); Toast.makeText(c, "Added ${list.size} cards", Toast.LENGTH_SHORT).show() }
                r.onFailure { Toast.makeText(c, it.message ?: "Something went wrong", Toast.LENGTH_LONG).show() } } }
        }.setNegativeButton("Cancel", null).show()
    }
}
