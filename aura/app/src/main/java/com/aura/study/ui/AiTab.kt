package com.aura.study.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.aura.study.*
import java.io.ByteArrayOutputStream

/** Explain, Quiz, Grammar and Humanize. Needs the AI key, except for the offline fixes. */
class AiTab(ctx: Context, private val prefs: Prefs, private val store: Store, private val ai: Ai, private val pickImage: ((ByteArray?) -> Unit) -> Unit, private val onChange: () -> Unit) : android.widget.ScrollView(ctx) {
    private val body = Ui.vbox(ctx)
    private val panel = Ui.vbox(ctx)
    private var section = 0
    private var level = "simple"
    private var count = 5
    private var tone = Ai.TONES[1].second
    private var strength = 1
    private var offlineFirst = true
    private var photo: ByteArray? = null

    init {
        isFillViewport = true
        body.setPadding(Ui.dp(ctx, 20), Ui.dp(ctx, 8), Ui.dp(ctx, 20), Ui.dp(ctx, 28))
        addView(body, android.widget.FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
        body.addView(Ui.choice(ctx, listOf("Explain" to 0, "Quiz" to 1, "Grammar" to 2, "Humanize" to 3), { section }) { section = it; show() })
        body.addView(panel, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0, ctx))
        show()
    }

    fun refresh() { show() }

    private fun show() {
        panel.removeAllViews()
        if (!ai.ready && section != 2 && section != 3) panel.addView(Ui.card(context).apply { addView(Ui.text(context, "Add your API key in More > Settings to turn on the AI. Grammar and Humanize also have offline buttons that work without it.", 13f, Ui.WARM)) }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 8, 0, 8, context))
        when (section) { 0 -> explain(); 1 -> quiz(); 2 -> grammar(); else -> humanize() }
    }

    private fun out(): Triple<TextView, TextView, LinearLayout> {
        val c = context
        val status = Ui.text(c, "", 13f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 10), 0, 0) }
        val result = Ui.text(c, "", 15f, Ui.INK).apply { setTextIsSelectable(true); setLineSpacing(0f, 1.2f) }
        val actions = Ui.hbox(c).apply { visibility = View.GONE }
        actions.addView(Ui.button(c, "Copy") { (c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Aura", result.text)); Toast.makeText(c, "Copied", Toast.LENGTH_SHORT).show() }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        actions.addView(Ui.button(c, "Share") { c.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, result.text.toString()), "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }, Ui.lp(0, Ui.WRAP, 1f))
        val card = Ui.card(c).apply { visibility = View.GONE }; card.addView(result); card.addView(actions, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
        panel.addView(status); panel.addView(card, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0, c))
        result.tag = card; actions.tag = actions
        return Triple(status, result, actions)
    }
    private fun showResult(status: TextView, result: TextView, actions: View, text: String, note: String = "") { status.text = note; result.text = text; (result.tag as View).visibility = View.VISIBLE; actions.visibility = View.VISIBLE }
    private fun fail(status: TextView, e: Throwable) { status.text = e.message ?: "Something went wrong"; status.setTextColor(Ui.RED) }
    private fun busy(status: TextView, msg: String) { status.setTextColor(Ui.MUTE); status.text = msg }

    // ---- explain ----
    private fun explain() {
        val c = context
        panel.addView(Ui.title(c, "Explain it to me")); panel.addView(Ui.note(c, "Type a topic or paste a question. You can also add a photo of a page or problem."))
        val input = Ui.input(c, "e.g. How does photosynthesis work?", 5); panel.addView(input)
        panel.addView(Ui.gap(c, 10)); panel.addView(Ui.choice(c, listOf("Simple" to "simple", "Normal" to "normal", "Advanced" to "advanced"), { level }) { level = it })
        val photoBtn = Ui.button(c, if (photo == null) "Add photo" else "Photo added (tap to remove)") { }
        photoBtn.setOnClickListener { if (photo != null) { photo = null; photoBtn.text = "Add photo" } else pickImage { bytes -> post { photo = bytes; photoBtn.text = if (bytes != null) "Photo added (tap to remove)" else "Add photo" } } }
        panel.addView(photoBtn, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 10, 0, 0, c))
        val (status, result, actions) = out()
        panel.addView(Ui.button(c, "Explain", true) {
            if (!ai.ready) { Toast.makeText(c, "Add your API key in More > Settings first.", Toast.LENGTH_LONG).show(); return@button }
            val q = input.text.toString(); if (q.isBlank() && photo == null) return@button
            busy(status, "Thinking…"); val img = photo
            ai.run({ ai.explain(q, level, img) }) { r -> post { r.onSuccess { showResult(status, result, actions, it) }.onFailure { fail(status, it) } } }
        }, panel.childCount - 2, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
    }

    // ---- quiz ----
    private fun quiz() {
        val c = context
        panel.addView(Ui.title(c, "Quiz me")); panel.addView(Ui.note(c, "Paste notes or name a topic. Questions you miss can become flashcards."))
        val input = Ui.input(c, "Topic or notes", 5); panel.addView(input)
        panel.addView(Ui.gap(c, 10)); panel.addView(Ui.choice(c, listOf("5 questions" to 5, "10 questions" to 10), { count }) { count = it })
        val area = Ui.vbox(c); val status = Ui.text(c, "", 13f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 10), 0, 0) }
        panel.addView(Ui.button(c, "Make quiz", true) {
            if (!ai.ready) { Toast.makeText(c, "Add your API key in More > Settings first.", Toast.LENGTH_LONG).show(); return@button }
            val q = input.text.toString(); if (q.isBlank()) return@button
            busy(status, "Writing questions…"); area.removeAllViews()
            ai.run({ ai.quiz(q, count) }) { r -> post { r.onSuccess { status.text = ""; runQuiz(area, it, q.take(24)) }.onFailure { fail(status, it) } } }
        }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
        panel.addView(status); panel.addView(area)
    }

    private fun runQuiz(area: LinearLayout, items: List<QuizItem>, deckHint: String) {
        val c = context; var i = 0; var score = 0; val missed = ArrayList<QuizItem>()
        fun next() {
            area.removeAllViews()
            if (i >= items.size) {
                area.addView(Ui.title(c, "Score: $score / ${items.size}"))
                if (missed.isNotEmpty()) area.addView(Ui.button(c, "Add ${missed.size} missed to flashcards") {
                    missed.forEach { store.addCard("Quiz: $deckHint", it.q, it.options[it.answer] + (if (it.why.isNotBlank()) "\n${it.why}" else ""), Days.today()) }; onChange(); Toast.makeText(c, "Added to Cards", Toast.LENGTH_SHORT).show() })
                return
            }
            val q = items[i]; val card = Ui.card(c); card.addView(Ui.note(c, "Question ${i + 1} of ${items.size}")); card.addView(Ui.text(c, q.q, 17f, Ui.INK, true)); area.addView(card)
            var answered = false
            q.options.forEachIndexed { oi, o ->
                val b = Ui.button(c, o) { }
                b.setOnClickListener {
                    if (answered) return@setOnClickListener; answered = true
                    if (oi == q.answer) { score++; b.background = Ui.round(c, 0x552E9C86, 26, Ui.MINT) } else { missed.add(q); b.background = Ui.round(c, 0x55FF7A8A, 26, Ui.RED) }
                    area.addView(Ui.text(c, (if (oi == q.answer) "Correct. " else "Not quite. The answer is: ${q.options[q.answer]}. ") + q.why, 14f, Ui.MUTE).apply { setPadding(0, Ui.dp(c, 10), 0, 0) })
                    area.addView(Ui.button(c, if (i + 1 >= items.size) "See score" else "Next", true) { i++; next() }, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 10, 0, 0, c))
                }
                area.addView(b, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0, c))
            }
        }
        next()
    }

    // ---- grammar ----
    private fun grammar() {
        val c = context
        panel.addView(Ui.title(c, "Grammar fixer")); panel.addView(Ui.note(c, "Fixes spelling, grammar and punctuation and keeps your meaning and voice."))
        val input = Ui.input(c, "Paste your text", 7); panel.addView(input)
        val (status, result, actions) = out()
        val row = Ui.hbox(c)
        row.addView(Ui.button(c, "Quick fix (offline)") { val t = input.text.toString(); if (t.isNotBlank()) showResult(status, result, actions, TextFix.grammar(t), "Basic fixes made on your phone.") }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        row.addView(Ui.button(c, "Fix with AI", true) {
            val t = input.text.toString(); if (t.isBlank()) return@button
            if (!ai.ready) { Toast.makeText(c, "Add your API key in More > Settings first.", Toast.LENGTH_LONG).show(); return@button }
            busy(status, "Checking…")
            ai.run({ ai.grammar(t) }) { r -> post { r.onSuccess { (fixed, changes) -> showResult(status, result, actions, fixed, if (changes.isBlank() || changes.equals("None", true)) "No changes needed." else "Changes:\n$changes") }.onFailure { fail(status, it) } } }
        }, Ui.lp(0, Ui.WRAP, 1.3f))
        panel.addView(row, panel.childCount - 2, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
    }

    // ---- humanize ----
    private fun humanize() {
        val c = context
        panel.addView(Ui.title(c, "Make it sound human")); panel.addView(Ui.note(c, "Turns stiff, robotic writing into natural writing. Great for polishing AI drafts of stories, messages and posts."))
        val input = Ui.input(c, "Paste the text to rewrite", 8); panel.addView(input)
        panel.addView(Ui.gap(c, 10)); panel.addView(Ui.choice(c, Ai.TONES.map { it.first to it.second }, { tone }) { tone = it })
        panel.addView(Ui.gap(c, 8)); panel.addView(Ui.choice(c, listOf("Light touch" to 1, "Bold rewrite" to 2), { strength }) { strength = it })
        panel.addView(Ui.gap(c, 8)); panel.addView(Ui.choice(c, listOf("Clean stock phrases first" to true, "Send as is" to false), { offlineFirst }) { offlineFirst = it })
        val (status, result, actions) = out()
        val row = Ui.hbox(c)
        row.addView(Ui.button(c, "Offline cleanup") { val t = input.text.toString(); if (t.isNotBlank()) { val r = Humanize.cleanup(t); showResult(status, result, actions, r, "Robotic score: ${Humanize.roboticScore(t)} → ${Humanize.roboticScore(r)} (a rough hint, lower reads more natural)") } }, Ui.lp(0, Ui.WRAP, 1f).apply { rightMargin = Ui.dp(c, 8) })
        row.addView(Ui.button(c, "Humanize with AI", true) {
            val t = input.text.toString(); if (t.isBlank()) return@button
            if (!ai.ready) { Toast.makeText(c, "Add your API key in More > Settings first.", Toast.LENGTH_LONG).show(); return@button }
            busy(status, "Rewriting…"); val src = if (offlineFirst) Humanize.cleanup(t) else t
            ai.run({ ai.humanize(src, tone, strength) }) { r -> post { r.onSuccess { showResult(status, result, actions, it, "Robotic score: ${Humanize.roboticScore(t)} → ${Humanize.roboticScore(it)} (a rough hint, lower reads more natural)") }.onFailure { fail(status, it) } } }
        }, Ui.lp(0, Ui.WRAP, 1.5f))
        panel.addView(row, panel.childCount - 2, Ui.lpm(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0, c))
    }

    companion object {
        /** shrink a picked photo to something small enough to send */
        fun compress(ctx: Context, uri: Uri): ByteArray? = runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1; while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2000) sample *= 2
            val bmp = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: return@runCatching null
            val scale = 1400f / maxOf(bmp.width, bmp.height); val b2 = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
            ByteArrayOutputStream().also { b2.compress(Bitmap.CompressFormat.JPEG, 82, it) }.toByteArray()
        }.getOrNull()
    }
}
