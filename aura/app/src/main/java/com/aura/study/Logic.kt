package com.aura.study

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Days {
    fun today(): Long = LocalDate.now().toEpochDay()
    fun date(day: Long): LocalDate = LocalDate.ofEpochDay(day)
    fun label(day: Long): String = date(day).let { "${it.dayOfMonth} ${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }.take(3)} ${it.year}" }
}

/** Streaks, XP and levels. Pure functions so they are easy to test. */
object Gamify {
    /** consecutive active days ending today (or yesterday, so the streak is still alive until the day is over) */
    fun streak(active: Set<Long>, today: Long): Int {
        var d = if (today in active) today else today - 1
        var n = 0
        while (d in active) { n++; d-- }
        return n
    }

    fun bestStreak(active: Set<Long>): Int {
        var best = 0; var cur = 0; var prev = Long.MIN_VALUE
        for (d in active.sorted()) { cur = if (d == prev + 1) cur + 1 else 1; best = max(best, cur); prev = d }
        return best
    }

    /** one streak freeze is earned for every 7 days of the best streak */
    fun freezesEarned(best: Int) = best / 7

    /** total XP needed to reach level [l] (level 1 = 0) */
    fun xpFor(l: Int): Int = 50 * (l - 1) * l
    fun level(xp: Int): Int { var l = 1; while (xpFor(l + 1) <= xp) l++; return l }
    fun levelProgress(xp: Int): Float { val l = level(xp); return (xp - xpFor(l)).toFloat() / (xpFor(l + 1) - xpFor(l)) }
    fun title(level: Int) = when { level < 3 -> "Spark"; level < 6 -> "Glow"; level < 10 -> "Flame"; level < 15 -> "Aurora"; else -> "Nova" }
}

/** SM-2 style spaced repetition. grade: 0 again, 1 hard, 2 good, 3 easy. */
object Srs {
    data class State(val interval: Int, val ease: Double, val reps: Int, val due: Long)

    fun next(s: State, grade: Int, today: Long): State {
        var ease = s.ease
        var reps = s.reps
        val interval: Int
        when (grade) {
            0 -> { reps = 0; ease = max(1.3, ease - .2); interval = 0 }
            1 -> { ease = max(1.3, ease - .15); reps += 1; interval = max(1, (max(s.interval, 1) * 1.2).roundToInt()) }
            2 -> { reps += 1; interval = when (reps) { 1 -> 1; 2 -> 3; else -> (s.interval * ease).roundToInt().coerceAtLeast(s.interval + 1) } }
            else -> { ease += .15; reps += 1; interval = if (reps == 1) 4 else (s.interval * ease * 1.3).roundToInt().coerceAtLeast(s.interval + 2) }
        }
        return State(interval, ease, reps, today + interval)
    }
}

/** Offline text clean-ups: used on their own when there is no AI key, and before the AI pass. */
object TextFix {
    private val misspell = mapOf("teh" to "the", "recieve" to "receive", "definately" to "definitely", "seperate" to "separate", "occured" to "occurred", "alot" to "a lot",
        "wich" to "which", "becuase" to "because", "untill" to "until", "adress" to "address", "tommorow" to "tomorrow", "wierd" to "weird", "thier" to "their", "freind" to "friend",
        "dont" to "don't", "cant" to "can't", "wont" to "won't", "doesnt" to "doesn't", "didnt" to "didn't", "isnt" to "isn't", "im" to "I'm", "ive" to "I've", "youre" to "you're")

    fun grammar(text: String): String {
        var t = text.replace(' ', ' ').replace(Regex("[ \t]+"), " ").replace(Regex(" +([,.;:!?])"), "$1").replace(Regex("([,;])(?=[A-Za-z])"), "$1 ")
        t = t.replace(Regex("(?i)\\b(\\w+)(\\s+\\1\\b)+")) { it.groupValues[1] }
        t = t.replace(Regex("\\bi\\b"), "I")
        t = Regex("[A-Za-z']+").replace(t) { m -> misspell[m.value.lowercase()]?.let { fixed -> if (m.value[0].isUpperCase()) fixed.replaceFirstChar { c -> c.uppercase() } else fixed } ?: m.value }
        t = Regex("(^|[.!?]\\s+|\\n)([a-z])").replace(t) { it.groupValues[1] + it.groupValues[2].uppercase() }
        return t.trim()
    }
}

/** Stock phrases that make text read as machine written, swapped for what a person would say. */
object Humanize {
    private val phrases = listOf(
        "it is important to note that " to "", "it's important to note that " to "", "it is worth noting that " to "", "it's worth noting that " to "", "it should be noted that " to "",
        "in today's fast-paced world, " to "", "in today's digital age, " to "", "in the realm of " to "in ", "when it comes to " to "with ",
        "in conclusion, " to "So, ", "to sum up, " to "So, ", "furthermore, " to "Also, ", "moreover, " to "Also, ", "additionally, " to "Also, ", "however, " to "But ", "subsequently, " to "Then ",
        "delves into" to "digs into", "delve into" to "dig into", "delving into" to "digging into", "rich tapestry of" to "mix of", "tapestry of" to "mix of", "a testament to" to "proof of",
        "plethora of" to "lot of", "myriad of" to "lot of", "a myriad" to "a lot", "navigate the complexities of" to "deal with", "in order to" to "to", "a wide range of" to "many",
        "utilizing" to "using", "utilize" to "use", "utilise" to "use", "leveraging" to "using", "leverage" to "use", "seamlessly" to "smoothly", "robust" to "solid", "pivotal" to "key",
        "crucial" to "important", "endeavor" to "try", "commence" to "start", "facilitate" to "help", "numerous" to "many", "subsequently" to "then", "therefore" to "so", "ensure that" to "make sure",
        "unlock the potential" to "make the most", "game-changer" to "big deal", "embark on" to "start", "journey" to "path")
    private val contractions = listOf("do not" to "don't", "does not" to "doesn't", "did not" to "didn't", "cannot" to "can't", "will not" to "won't", "would not" to "wouldn't", "could not" to "couldn't",
        "should not" to "shouldn't", "is not" to "isn't", "are not" to "aren't", "was not" to "wasn't", "were not" to "weren't", "has not" to "hasn't", "have not" to "haven't",
        "it is" to "it's", "that is" to "that's", "there is" to "there's", "I am" to "I'm", "you are" to "you're", "we are" to "we're", "they are" to "they're", "I have" to "I've", "I will" to "I'll", "let us" to "let's")

    private fun swap(text: String, from: String, to: String): String = Regex("(?i)\\b" + Regex.escape(from.trimEnd()) + (if (from.endsWith(" ")) "\\s+" else "\\b")).replace(text) { m ->
        if (to.isEmpty()) "" else if (m.value[0].isUpperCase()) to.replaceFirstChar { c -> c.uppercase() } else to }

    fun cleanup(text: String, contract: Boolean = true): String {
        var t = text
        for ((a, b) in phrases) t = swap(t, a, b)
        if (contract) for ((a, b) in contractions) t = swap(t, a, b)
        t = t.replace(" — ", ", ").replace("—", ", ").replace(" – ", ", ").replace(Regex(" {2,}"), " ")
        t = Regex("(^|[.!?]\\s+|\\n)([a-z])").replace(t) { it.groupValues[1] + it.groupValues[2].uppercase() }
        return t.trim()
    }

    /** a rough "how robotic does this read" score 0..100 from stock phrases, dashes and uniform sentence length (just a hint, not a detector) */
    fun roboticScore(text: String): Int {
        if (text.isBlank()) return 0
        val lower = text.lowercase()
        var hits = 0
        for ((a, _) in phrases) if (a.length > 6 && lower.contains(a.trim())) hits++
        hits += Regex("—").findAll(text).count() / 2
        val lens = text.split(Regex("(?<=[.!?])\\s+")).map { it.split(Regex("\\s+")).size }.filter { it > 0 }
        var uniform = 0.0
        if (lens.size >= 4) { val mean = lens.average(); val sd = sqrt(lens.sumOf { (it - mean) * (it - mean) } / lens.size); uniform = if (sd < mean * .25) 25.0 else 0.0 }
        return (hits * 12 + uniform).toInt().coerceIn(0, 100)
    }
}
