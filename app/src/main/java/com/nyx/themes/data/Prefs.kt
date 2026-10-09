package com.nyx.themes.data

import android.content.Context
import android.content.SharedPreferences

enum class AutoMode { OFF, UNLOCK, INTERVAL }

enum class Source(val label: String) {
    ALL("All wallpapers"), DARK("Dark only"), LIGHT("Light only"), FAVORITES("Favorites"),
    LIVE("Live and long only"), STILL("Still only"), MINIMAL("Minimal style"), INK("Ink sketch style"),
}

enum class IconPackId(val label: String, val dir: String) {
    PAPER("Paper watercolor", "paper"), NIGHT("Night ink", "night"), MOON("Moon silhouette", "moon"),
}

/** All user settings, backed by SharedPreferences. Safe to read from any thread. */
class Prefs(context: Context) {
    private val sp: SharedPreferences = context.applicationContext.getSharedPreferences("nyx", Context.MODE_PRIVATE)

    var sceneId: String?
        get() = sp.getString("scene_id", null)
        set(v) = sp.edit().putString("scene_id", v).apply()

    var autoMode: AutoMode
        get() = runCatching { AutoMode.valueOf(sp.getString("auto_mode", null) ?: "OFF") }.getOrDefault(AutoMode.OFF)
        set(v) = sp.edit().putString("auto_mode", v.name).apply()

    var intervalMinutes: Int
        get() = sp.getInt("interval_min", 30).coerceAtLeast(1)
        set(v) = sp.edit().putInt("interval_min", v.coerceAtLeast(1)).apply()

    var source: Source
        get() = runCatching { Source.valueOf(sp.getString("source", null) ?: "ALL") }.getOrDefault(Source.ALL)
        set(v) = sp.edit().putString("source", v.name).apply()

    var shuffle: Boolean
        get() = sp.getBoolean("shuffle", true)
        set(v) = sp.edit().putBoolean("shuffle", v).apply()

    var followDarkMode: Boolean
        get() = sp.getBoolean("follow_dark", false)
        set(v) = sp.edit().putBoolean("follow_dark", v).apply()

    var pauseOnBatterySaver: Boolean
        get() = sp.getBoolean("pause_saver_v2", false)
        set(v) = sp.edit().putBoolean("pause_saver_v2", v).apply()

    /** 0 = auto (hardware canvas, falls back by itself), 1 = compatible (software canvas) */
    var renderMode: Int
        get() = sp.getInt("render_mode", 0)
        set(v) = sp.edit().putInt("render_mode", v).apply()

    /** 0 = sharp, 1 = balanced, 2 = light (lower drawing resolution = smoother) */
    var quality: Int
        get() = sp.getInt("quality", 1)
        set(v) = sp.edit().putInt("quality", v.coerceIn(0, 3)).apply()

    var fps: Int
        get() = sp.getInt("fps", 30).coerceIn(10, 60)
        set(v) = sp.edit().putInt("fps", v.coerceIn(10, 60)).apply()

    /** true = Nyx Home (the launcher with painted icons) is switched off and only wallpapers are used */
    var wallpapersOnly: Boolean
        get() = sp.getBoolean("wallpapers_only", true)
        set(v) = sp.edit().putBoolean("wallpapers_only", v).apply()

    var iconPack: IconPackId
        get() = runCatching { IconPackId.valueOf(sp.getString("icon_pack", null) ?: "PAPER") }.getOrDefault(IconPackId.PAPER)
        set(v) = sp.edit().putString("icon_pack", v.name).apply()

    var lastChangeMs: Long
        get() = sp.getLong("last_change", 0L)
        set(v) = sp.edit().putLong("last_change", v).apply()

    var favorites: Set<String>
        get() = sp.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()
        set(v) = sp.edit().putStringSet("favorites", v.toSet()).apply()

    fun toggleFavorite(id: String): Boolean {
        val f = favorites.toMutableSet()
        val now = if (f.remove(id)) false else { f.add(id); true }
        favorites = f
        return now
    }

    /** ordered package names pinned on the Nyx Home screen */
    var homePins: List<String>
        get() = (sp.getString("home_pins", null) ?: "").split('\n').filter { it.isNotBlank() }
        set(v) = sp.edit().putString("home_pins", v.joinToString("\n")).apply()

    var homePinsInitialised: Boolean
        get() = sp.getBoolean("home_pins_init", false)
        set(v) = sp.edit().putBoolean("home_pins_init", v).apply()

    fun registerListener(l: SharedPreferences.OnSharedPreferenceChangeListener) = sp.registerOnSharedPreferenceChangeListener(l)
    fun unregisterListener(l: SharedPreferences.OnSharedPreferenceChangeListener) = sp.unregisterOnSharedPreferenceChangeListener(l)
}
