package com.aura.study

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class AiException(message: String) : Exception(message)

data class QuizItem(val q: String, val options: List<String>, val answer: Int, val why: String)
data class CardDraft(val front: String, val back: String)

/** Talks to the Claude API with the key the user typed into Settings. Everything runs off the main thread. */
class Ai(private val prefs: Prefs, private val baseUrl: String = "https://api.anthropic.com") {
    private val pool = Executors.newSingleThreadExecutor()

    val ready get() = prefs.apiKey.isNotBlank()

    /** blocking call, used by tests and by [run] */
    fun complete(system: String, user: String, image: ByteArray? = null, maxTokens: Int = 1800): String {
        if (!ready) throw AiException("Add your API key in More > Settings first.")
        val content = JSONArray()
        if (image != null) content.put(JSONObject().put("type", "image").put("source", JSONObject().put("type", "base64").put("media_type", "image/jpeg").put("data", Base64.encodeToString(image, Base64.NO_WRAP))))
        content.put(JSONObject().put("type", "text").put("text", user))
        val body = JSONObject().put("model", prefs.model).put("max_tokens", maxTokens).put("system", system)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", content)))
        val conn = (URL("$baseUrl/v1/messages").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15000; readTimeout = 90000; doOutput = true
            setRequestProperty("content-type", "application/json"); setRequestProperty("x-api-key", prefs.apiKey); setRequestProperty("anthropic-version", "2023-06-01")
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) throw AiException(errorMessage(code, text))
            val parts = JSONObject(text).getJSONArray("content")
            return buildString { for (i in 0 until parts.length()) parts.getJSONObject(i).optString("text").let { append(it) } }.trim()
        } catch (e: AiException) { throw e
        } catch (e: java.io.IOException) { throw AiException("No connection. Check your internet and try again.")
        } finally { conn.disconnect() }
    }

    private fun errorMessage(code: Int, body: String): String {
        val detail = runCatching { JSONObject(body).getJSONObject("error").getString("message") }.getOrNull()
        return when (code) {
            401 -> "The API key was rejected. Check it in Settings."
            403 -> "This key is not allowed to use that model."
            404 -> "That model is not available for your key. Pick another in Settings."
            429 -> "Too many requests or no credit left. Wait a moment, or check your account."
            529, 503 -> "The AI service is busy. Try again in a minute."
            else -> "AI error ($code)${detail?.let { ": $it" } ?: ""}"
        }
    }

    /** run [block] in the background and hand the result back through [done] (call it from the UI thread yourself) */
    fun <T> run(block: () -> T, done: (Result<T>) -> Unit) { pool.execute { done(runCatching(block)) } }

    // ---- features ----
    fun explain(topic: String, level: String, image: ByteArray?): String = complete(
        "You are a patient tutor. Explain at a $level level. Start with the idea in two plain sentences, then explain step by step with one concrete example, then finish with three short questions to check understanding. Use simple words, no filler.",
        topic.ifBlank { "Explain what is shown in the image." }, image)

    fun quiz(topic: String, n: Int): List<QuizItem> = parseQuiz(complete(
        "You write multiple choice quizzes. Reply with ONLY a JSON array of $n objects, each {\"q\": string, \"options\": [4 strings], \"answer\": index 0-3, \"why\": one short sentence}. No other text.", "Topic or notes:\n$topic", maxTokens = 2500))

    fun cards(notes: String, n: Int): List<CardDraft> = parseCards(complete(
        "You turn study notes into flashcards. Reply with ONLY a JSON array of up to $n objects {\"front\": question or term, \"back\": short answer}. Keep each side under 25 words. No other text.", notes, maxTokens = 2500))

    /** returns the corrected text and a short list of what changed */
    fun grammar(text: String): Pair<String, String> = parseGrammar(complete(
        "You fix spelling, grammar and punctuation without changing the meaning, voice or length. Reply in exactly this format:\nCORRECTED:\n<the corrected text>\nCHANGES:\n<up to 6 short bullet lines, or 'None'>", text))

    fun humanize(text: String, tone: String, strength: Int): String = complete(
        "Rewrite the user's text so it reads like a real person wrote it, not a machine. $tone Vary sentence length and rhythm, mix short and long sentences, use plain everyday words and natural contractions, drop filler openers, stock phrases and lists of three, and avoid em dashes. " +
            (if (strength >= 2) "Be bold: reorder sentences and rephrase freely, an occasional fragment is fine. " else "Stay close to the original structure. ") +
            "Keep every fact and the meaning exactly, add nothing new, keep the original language and any formatting. Reply with only the rewritten text.", text)

    companion object {
        val TONES = listOf("Casual" to "Tone: relaxed and conversational.", "Neutral" to "Tone: clear and natural, neither formal nor slangy.", "Warm" to "Tone: friendly and warm.", "Punchy" to "Tone: direct, short and confident.", "Story" to "Tone: natural storytelling voice, good for fiction and dialogue.")

        private fun stripFence(s: String): String {
            val t = s.trim()
            val a = t.indexOf('['); val b = t.lastIndexOf(']')
            return if (a >= 0 && b > a) t.substring(a, b + 1) else t
        }

        fun parseQuiz(raw: String): List<QuizItem> {
            val arr = try { JSONArray(stripFence(raw)) } catch (e: Exception) { throw AiException("The AI answer could not be read. Try again.") }
            val out = ArrayList<QuizItem>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val opts = o.optJSONArray("options") ?: continue
                val list = List(opts.length()) { opts.optString(it) }
                val ans = o.optInt("answer", -1)
                if (o.optString("q").isNotBlank() && list.size >= 2 && ans in list.indices) out.add(QuizItem(o.getString("q"), list, ans, o.optString("why")))
            }
            if (out.isEmpty()) throw AiException("No questions came back. Try different notes.")
            return out
        }

        fun parseCards(raw: String): List<CardDraft> {
            val arr = try { JSONArray(stripFence(raw)) } catch (e: Exception) { throw AiException("The AI answer could not be read. Try again.") }
            val out = (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }.filter { it.optString("front").isNotBlank() && it.optString("back").isNotBlank() }.map { CardDraft(it.getString("front"), it.getString("back")) }
            if (out.isEmpty()) throw AiException("No cards came back. Try different notes.")
            return out
        }

        fun parseGrammar(raw: String): Pair<String, String> {
            val i = raw.indexOf("CORRECTED:"); val j = raw.indexOf("CHANGES:")
            if (i < 0) return raw.trim() to ""
            val fixed = raw.substring(i + 10, if (j > i) j else raw.length).trim()
            val changes = if (j > i) raw.substring(j + 8).trim() else ""
            return fixed to changes
        }
    }
}
