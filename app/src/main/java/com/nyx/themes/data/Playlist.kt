package com.nyx.themes.data

import com.nyx.themes.scene.Mode
import com.nyx.themes.scene.SceneMeta
import kotlin.random.Random

/** Pure selection logic for which wallpapers can come up and which one is next. */
object Playlist {
    fun candidates(all: List<SceneMeta>, source: Source, favorites: Set<String>, followDark: Boolean, systemDark: Boolean): List<SceneMeta> {
        var list = when (source) {
            Source.ALL -> all
            Source.DARK -> all.filter { it.isDark }
            Source.LIGHT -> all.filter { !it.isDark }
            Source.FAVORITES -> all.filter { it.id in favorites }
            Source.LIVE -> all.filter { it.mode != Mode.STILL }
            Source.STILL -> all.filter { it.mode == Mode.STILL }
            Source.MINIMAL -> all.filter { it.style == "min" }
            Source.INK -> all.filter { it.style == "ink" }
        }
        if (followDark) {
            val matching = list.filter { it.isDark == systemDark }
            list = if (matching.isNotEmpty()) matching else all.filter { it.isDark == systemDark }.ifEmpty { list }
        }
        return list.ifEmpty { all }
    }

    /** next scene after [currentId]; shuffle never repeats the current one when there is a choice */
    fun next(cands: List<SceneMeta>, currentId: String?, shuffle: Boolean, rnd: Random = Random.Default): SceneMeta? {
        if (cands.isEmpty()) return null
        if (cands.size == 1) return cands[0]
        return if (shuffle) {
            val others = cands.filter { it.id != currentId }
            others[rnd.nextInt(others.size)]
        } else {
            val i = cands.indexOfFirst { it.id == currentId }
            cands[(i + 1).mod(cands.size)]
        }
    }
}
