package com.aura.study

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.aura.study.ui.MainActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSystemClock
import java.io.File
import java.net.ServerSocket
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AppTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<Application>()
    @Before fun clean() { ctx.deleteDatabase("aura.db"); ctx.getSharedPreferences("aura", 0).edit().clear().commit(); TimerState.reset(Prefs(ctx)) }

    @Test fun storeTracksSessionsHabitsCardsAndXp() {
        val s = Store(ctx); val d = Days.today()
        s.addSession(d, 25, "Math"); s.addSession(d - 1, 50, "Reading")
        assertEquals(mapOf(d to 25, d - 1 to 50), s.minutesByDay(d - 6, d))
        assertEquals("Reading", s.minutesBySubject(d - 6, d).first().first)
        val h = s.addHabit("Read"); assertTrue(s.toggleHabit(h, d)); assertFalse(s.toggleHabit(h, d)); s.toggleHabit(h, d)
        assertEquals(setOf(d), s.habitDays(h))
        s.addCard("Bio", "Q1", "A1", d); s.addCard("Bio", "Q2", "A2", d)
        val due = s.dueCards(d); assertEquals(2, due.size)
        s.gradeCard(due[0], 2, d); assertEquals(1, s.dueCards(d).size); assertEquals(1, s.reviewCount())
        assertTrue(d in s.activeDays() && (d - 1) in s.activeDays())
        assertEquals(75 + 10 + 15 + 2, s.xp())
        s.addExam("Chem", d + 5); assertEquals(1, s.exams().size)
        s.deleteHabit(h); assertTrue(s.habits().isEmpty())
    }

    /** tiny HTTP server on localhost: records "key|body" of every request and answers with [status] and [reply] */
    class FakeServer(val port: Int, private val ss: ServerSocket) { fun stop(n: Int) { ss.close() } }
    private fun fakeApi(status: Int, reply: String, seen: MutableList<String> = ArrayList()): Pair<FakeServer, String> {
        val ss = ServerSocket(0, 5, java.net.InetAddress.getByName("127.0.0.1"))
        Thread {
            while (!ss.isClosed) {
                val sock = try { ss.accept() } catch (e: Exception) { break }
                sock.use {
                    val inp = it.getInputStream().buffered(); var len = 0; var key = ""
                    while (true) { val line = readLine(inp); if (line.isEmpty()) break; val l = line.lowercase(); if (l.startsWith("content-length:")) len = line.substring(15).trim().toInt(); if (l.startsWith("x-api-key:")) key = line.substring(10).trim() }
                    val body = ByteArray(len).also { b -> var n = 0; while (n < len) { val r = inp.read(b, n, len - n); if (r < 0) break; n += r } }
                    seen.add(key + "|" + String(body))
                    val out = reply.toByteArray()
                    it.getOutputStream().apply { write("HTTP/1.1 $status X\r\ncontent-type: application/json\r\ncontent-length: ${out.size}\r\nconnection: close\r\n\r\n".toByteArray()); write(out); flush() }
                }
            }
        }.apply { isDaemon = true; start() }
        return FakeServer(ss.localPort, ss) to "http://127.0.0.1:${ss.localPort}"
    }
    private fun readLine(i: java.io.InputStream): String { val sb = StringBuilder(); while (true) { val c = i.read(); if (c < 0 || c == '\n'.code) break; if (c != '\r'.code) sb.append(c.toChar()) }; return sb.toString() }
    private fun textReply(t: String) = org.json.JSONObject().put("content", org.json.JSONArray().put(org.json.JSONObject().put("type", "text").put("text", t))).toString()

    @Test fun aiClientSendsKeyAndParsesAnswers() {
        val prefs = Prefs(ctx); prefs.apiKey = "sk-test"
        val seen = ArrayList<String>()
        val quiz = """```json
[{"q":"2+2?","options":["3","4","5","6"],"answer":1,"why":"Basic sum."},{"q":"bad","options":["a"],"answer":9}]
```"""
        val (srv, url) = fakeApi(200, textReply(quiz), seen)
        try {
            val ai = Ai(prefs, url)
            val items = ai.quiz("math", 5); assertEquals(1, items.size); assertEquals(1, items[0].answer)
            assertTrue(seen[0].startsWith("sk-test|")); assertTrue(seen[0].contains("claude-haiku-5-5")); assertTrue(seen[0].contains("\"max_tokens\""))
        } finally { srv.stop(0) }
        val (s2, u2) = fakeApi(200, textReply("CORRECTED:\nHello there.\nCHANGES:\n- fixed hello"))
        try { val (fixed, ch) = Ai(prefs, u2).grammar("helo there"); assertEquals("Hello there.", fixed); assertTrue(ch.contains("fixed")) } finally { s2.stop(0) }
        val (s3, u3) = fakeApi(200, textReply("[{\"front\":\"A\",\"back\":\"B\"}]"))
        try { assertEquals(listOf(CardDraft("A", "B")), Ai(prefs, u3).cards("notes", 5)) } finally { s3.stop(0) }
    }

    @Test fun aiClientReportsFriendlyErrors() {
        val prefs = Prefs(ctx)
        assertTrue(runCatching { Ai(prefs).complete("s", "u") }.exceptionOrNull()!!.message!!.contains("API key"))
        prefs.apiKey = "bad"
        val (srv, url) = fakeApi(401, """{"error":{"message":"invalid x-api-key"}}""")
        try { assertTrue(runCatching { Ai(prefs, url).humanize("hi", "t", 1) }.exceptionOrNull()!!.message!!.contains("rejected")) } finally { srv.stop(0) }
        assertTrue(runCatching { Ai.parseQuiz("not json") }.exceptionOrNull() is AiException)
    }

    @Test fun timerServiceRunsAFocusSessionAndStartsABreak() {
        val prefs = Prefs(ctx); prefs.focusMin = 1; prefs.breakMin = 1; prefs.autoNext = false; prefs.sound = "off"; prefs.subject = "Math"
        TimerState.reset(prefs)
        val svc = Robolectric.buildService(TimerService::class.java).create().get()
        svc.onStartCommand(Intent(TimerService.ACTION_START), 0, 1)
        assertTrue(TimerState.running)
        ShadowSystemClock.advanceBy(Duration.ofSeconds(61)); shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(Phase.SHORT, TimerState.phase); assertFalse(TimerState.running); assertEquals(1, TimerState.focusDone)
        assertEquals(1, Store(ctx).sessionCount()); assertEquals("Math", Store(ctx).minutesBySubject(0, Days.today() + 1)[0].first)
        svc.onStartCommand(Intent(TimerService.ACTION_STOP), 0, 2); assertEquals(Phase.FOCUS, TimerState.phase)
    }

    @Test fun screensBuildAndRender() {
        val act = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().visible().get()
        val out = File("build/test-renders").apply { mkdirs() }
        val root = act.findViewById<View>(android.R.id.content)
        fun shot(name: String) { val w = 1080; val h = 2200; root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)); root.layout(0, 0, w, h)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); root.draw(Canvas(bmp)); java.io.FileOutputStream(File(out, "aura_$name.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        // seed some data so the screens are not empty
        val s = Store(act); val d = Days.today(); for (i in 0..20 step 2) s.addSession(d - i, 30 + i * 3, listOf("Math", "Reading", "Coding")[i % 3]); val h = s.addHabit("Read 10 pages"); s.toggleHabit(h, d); s.addHabit("Stretch"); s.addCard("Bio", "What is ATP?", "The cell's energy currency", d); s.addExam("Chemistry", d + 9)
        val nav = (act.findViewById<View>(android.R.id.content) as android.view.ViewGroup).getChildAt(0) as android.view.ViewGroup
        val bar = nav.getChildAt(2) as android.view.ViewGroup
        for (i in 0 until bar.childCount) { bar.getChildAt(i).performClick(); shot("tab$i") }
        assertEquals(5, bar.childCount)
    }
}
