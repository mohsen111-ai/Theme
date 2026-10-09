package com.aura.study

import android.content.Context

/** User settings. The AI key lives only in this app's private storage on the phone. */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("aura", Context.MODE_PRIVATE)

    var apiKey: String get() = sp.getString("api_key", "") ?: ""; set(v) = sp.edit().putString("api_key", v.trim()).apply()
    var model: String get() = sp.getString("model", MODELS[0].second) ?: MODELS[0].second; set(v) = sp.edit().putString("model", v).apply()
    var focusMin: Int get() = sp.getInt("focus", 25); set(v) = sp.edit().putInt("focus", v).apply()
    var breakMin: Int get() = sp.getInt("break", 5); set(v) = sp.edit().putInt("break", v).apply()
    var longBreakMin: Int get() = sp.getInt("long_break", 15); set(v) = sp.edit().putInt("long_break", v).apply()
    var autoNext: Boolean get() = sp.getBoolean("auto_next", true); set(v) = sp.edit().putBoolean("auto_next", v).apply()
    var sound: String get() = sp.getString("sound", "off") ?: "off"; set(v) = sp.edit().putString("sound", v).apply()
    var volume: Int get() = sp.getInt("volume", 60); set(v) = sp.edit().putInt("volume", v.coerceIn(0, 100)).apply()
    var subject: String get() = sp.getString("subject", "General") ?: "General"; set(v) = sp.edit().putString("subject", v).apply()
    var subjects: List<String> get() = (sp.getString("subjects", "General\nMath\nReading\nCoding") ?: "").split('\n').filter { it.isNotBlank() }; set(v) = sp.edit().putString("subjects", v.joinToString("\n")).apply()
    var freezesUsed: Int get() = sp.getInt("freezes_used", 0); set(v) = sp.edit().putInt("freezes_used", v).apply()

    companion object {
        val MODELS = listOf("Fast" to "claude-haiku-5-5", "Balanced" to "claude-sonnet-5-5", "Best" to "claude-opus-5-5")
    }
}
