package com.aura.study

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Habit(val id: Long, val name: String)
data class Card(val id: Long, val deck: String, val front: String, val back: String, val due: Long, val interval: Int, val ease: Double, val reps: Int) {
    val state get() = Srs.State(interval, ease, reps, due)
}
data class Exam(val id: Long, val name: String, val day: Long)

/** All local data in one small SQLite file. */
class Store(context: Context) : SQLiteOpenHelper(context.applicationContext, "aura.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE sessions(id INTEGER PRIMARY KEY AUTOINCREMENT, day INTEGER, minutes INTEGER, subject TEXT)")
        db.execSQL("CREATE TABLE habits(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)")
        db.execSQL("CREATE TABLE habit_log(habit INTEGER, day INTEGER, PRIMARY KEY(habit, day))")
        db.execSQL("CREATE TABLE cards(id INTEGER PRIMARY KEY AUTOINCREMENT, deck TEXT, front TEXT, back TEXT, due INTEGER, interval INTEGER, ease REAL, reps INTEGER)")
        db.execSQL("CREATE TABLE reviews(day INTEGER PRIMARY KEY, n INTEGER)")
        db.execSQL("CREATE TABLE frozen(day INTEGER PRIMARY KEY)")
        db.execSQL("CREATE TABLE exams(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, day INTEGER)")
    }
    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {}

    // ---- focus sessions ----
    fun addSession(day: Long, minutes: Int, subject: String) { writableDatabase.insert("sessions", null, ContentValues().apply { put("day", day); put("minutes", minutes); put("subject", subject) }) }
    fun minutesByDay(from: Long, to: Long): Map<Long, Int> = query("SELECT day, SUM(minutes) FROM sessions WHERE day BETWEEN ? AND ? GROUP BY day", from, to) { it.getLong(0) to it.getInt(1) }.toMap()
    fun minutesBySubject(from: Long, to: Long): List<Pair<String, Int>> = query("SELECT subject, SUM(minutes) FROM sessions WHERE day BETWEEN ? AND ? GROUP BY subject ORDER BY 2 DESC", from, to) { it.getString(0) to it.getInt(1) }
    fun sessionCount(): Int = scalar("SELECT COUNT(*) FROM sessions")

    // ---- habits ----
    fun habits(): List<Habit> = query("SELECT id, name FROM habits ORDER BY id") { Habit(it.getLong(0), it.getString(1)) }
    fun addHabit(name: String): Long = writableDatabase.insert("habits", null, ContentValues().apply { put("name", name.trim()) })
    fun deleteHabit(id: Long) { writableDatabase.delete("habits", "id=?", arrayOf("$id")); writableDatabase.delete("habit_log", "habit=?", arrayOf("$id")) }
    fun habitDays(id: Long): Set<Long> = query("SELECT day FROM habit_log WHERE habit=?", id) { it.getLong(0) }.toSet()
    fun toggleHabit(id: Long, day: Long): Boolean {
        val had = habitDays(id).contains(day)
        if (had) writableDatabase.delete("habit_log", "habit=? AND day=?", arrayOf("$id", "$day"))
        else writableDatabase.insertWithOnConflict("habit_log", null, ContentValues().apply { put("habit", id); put("day", day) }, SQLiteDatabase.CONFLICT_IGNORE)
        return !had
    }

    // ---- cards ----
    fun addCard(deck: String, front: String, back: String, today: Long): Long = writableDatabase.insert("cards", null, ContentValues().apply {
        put("deck", deck); put("front", front.trim()); put("back", back.trim()); put("due", today); put("interval", 0); put("ease", 2.5); put("reps", 0) })
    fun cards(): List<Card> = query("SELECT id, deck, front, back, due, interval, ease, reps FROM cards ORDER BY id") { Card(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getLong(4), it.getInt(5), it.getDouble(6), it.getInt(7)) }
    fun dueCards(today: Long, deck: String? = null): List<Card> = cards().filter { it.due <= today && (deck == null || it.deck == deck) }
    fun decks(): List<String> = query("SELECT DISTINCT deck FROM cards ORDER BY deck") { it.getString(0) }
    fun deleteCard(id: Long) { writableDatabase.delete("cards", "id=?", arrayOf("$id")) }
    fun gradeCard(card: Card, grade: Int, today: Long) {
        val s = Srs.next(card.state, grade, today)
        writableDatabase.update("cards", ContentValues().apply { put("due", s.due); put("interval", s.interval); put("ease", s.ease); put("reps", s.reps) }, "id=?", arrayOf("${card.id}"))
        writableDatabase.execSQL("INSERT INTO reviews(day, n) VALUES(?, 1) ON CONFLICT(day) DO UPDATE SET n = n + 1", arrayOf(today))
    }
    fun reviewCount(): Int = scalar("SELECT COALESCE(SUM(n), 0) FROM reviews")

    // ---- exams ----
    fun exams(): List<Exam> = query("SELECT id, name, day FROM exams ORDER BY day") { Exam(it.getLong(0), it.getString(1), it.getLong(2)) }
    fun addExam(name: String, day: Long) { writableDatabase.insert("exams", null, ContentValues().apply { put("name", name.trim()); put("day", day) }) }
    fun deleteExam(id: Long) { writableDatabase.delete("exams", "id=?", arrayOf("$id")) }

    // ---- streak and xp ----
    fun freeze(day: Long) { writableDatabase.insertWithOnConflict("frozen", null, ContentValues().apply { put("day", day) }, SQLiteDatabase.CONFLICT_IGNORE) }
    fun activeDays(): Set<Long> = query("SELECT day FROM sessions UNION SELECT day FROM habit_log UNION SELECT day FROM reviews UNION SELECT day FROM frozen") { it.getLong(0) }.toSet()
    fun xp(): Int = scalar("SELECT COALESCE(SUM(minutes), 0) FROM sessions") + 5 * sessionCount() + 15 * scalar("SELECT COUNT(*) FROM habit_log") + 2 * reviewCount()

    private fun scalar(sql: String): Int = readableDatabase.rawQuery(sql, null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    private fun <T> query(sql: String, vararg args: Long, map: (android.database.Cursor) -> T): List<T> =
        readableDatabase.rawQuery(sql, args.map { it.toString() }.toTypedArray()).use { c -> val out = ArrayList<T>(); while (c.moveToNext()) out.add(map(c)); out }
}
