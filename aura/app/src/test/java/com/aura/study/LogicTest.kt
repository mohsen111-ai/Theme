package com.aura.study

import org.junit.Assert.*
import org.junit.Test

class LogicTest {
    @Test fun streakCountsBackFromTodayOrYesterday() {
        assertEquals(3, Gamify.streak(setOf(8L, 9L, 10L), 10L))
        assertEquals(3, Gamify.streak(setOf(7L, 8L, 9L), 10L))   // today not done yet: streak still alive
        assertEquals(0, Gamify.streak(setOf(5L, 6L), 10L))
        assertEquals(0, Gamify.streak(emptySet(), 10L))
        assertEquals(4, Gamify.bestStreak(setOf(1L, 2L, 4L, 5L, 6L, 7L, 20L)))
        assertEquals(1, Gamify.freezesEarned(7)); assertEquals(0, Gamify.freezesEarned(6))
    }

    @Test fun levelsGrowAndProgressStaysInRange() {
        assertEquals(1, Gamify.level(0)); assertEquals(2, Gamify.level(100)); assertEquals(3, Gamify.level(300))
        for (xp in 0..5000 step 37) { val p = Gamify.levelProgress(xp); assertTrue(p in 0f..1f) }
    }

    @Test fun srsSpacesCardsOut() {
        var s = Srs.State(0, 2.5, 0, 100)
        s = Srs.next(s, 2, 100); assertEquals(1, s.interval); assertEquals(101L, s.due)
        s = Srs.next(s, 2, 101); assertEquals(3, s.interval)
        s = Srs.next(s, 2, 104); assertTrue(s.interval >= 7)
        val again = Srs.next(s, 0, 200); assertEquals(0, again.interval); assertEquals(200L, again.due); assertEquals(0, again.reps); assertTrue(again.ease < s.ease)
        val easy = Srs.next(Srs.State(0, 2.5, 0, 0), 3, 0); assertEquals(4, easy.interval)
        var hard = Srs.State(10, 1.35, 5, 0); hard = Srs.next(hard, 1, 0); assertTrue(hard.ease >= 1.3)
    }

    @Test fun grammarFixesBasics() {
        assertEquals("I think the cat is here. It is nice.", TextFix.grammar("i think the the cat is here .  it is nice."))
        assertEquals("I don't know, really.", TextFix.grammar("i dont know,really."))
        assertEquals("See https://example.com/a", TextFix.grammar("See https://example.com/a"))
        assertEquals("Definitely a lot", TextFix.grammar("Definately alot"))
    }

    @Test fun humanizeRemovesStockPhrasesAndAddsContractions() {
        val robot = "It is important to note that we cannot utilize this. Furthermore, it is a rich tapestry of ideas — and we do not delve into it."
        val out = Humanize.cleanup(robot)
        assertFalse(out.contains("important to note")); assertFalse(out.contains("tapestry")); assertFalse(out.contains("—")); assertFalse(out.lowercase().contains("utilize"))
        assertTrue(out.contains("can't")); assertTrue(out.contains("don't")); assertTrue(out.startsWith("We "))
        assertTrue(Humanize.roboticScore(robot) > Humanize.roboticScore(out))
        assertEquals("", Humanize.cleanup(""))
        assertEquals("Hello there.", Humanize.cleanup("Hello there."))
    }

    @Test fun soundGeneratorsMakeBoundedNonSilentAudio() {
        for ((kind, _) in Sounds.KINDS) {
            val buf = ShortArray(SAMPLE_RATE * 2); Generator(kind, 3).fill(buf)
            val peak = buf.maxOf { kotlin.math.abs(it.toInt()) }
            if (kind == "off") assertEquals(0, peak) else { assertTrue("$kind silent", peak > 500); assertTrue("$kind clips", peak < 32767) }
        }
    }
}
