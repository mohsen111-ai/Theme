package com.nyx.themes.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.nyx.themes.data.AutoMode
import com.nyx.themes.data.Playlist
import com.nyx.themes.data.Prefs
import com.nyx.themes.scene.SceneMeta
import com.nyx.themes.scene.SceneRepo

/**
 * One per process. Decides which scene is current and when it changes (on unlock, on a timer, when the app asks).
 * Wallpaper engines (home + lock) listen to it so both always show the same scene.
 */
class SceneController private constructor(private val app: Context) {
    interface Listener { fun onSceneChanged(scene: SceneMeta, animate: Boolean) }

    val repo = SceneRepo.get(app)
    val prefs = Prefs(app)
    private val listeners = LinkedHashSet<Listener>()
    private val main = Handler(Looper.getMainLooper())
    private var receiverRegistered = false
    private var pendingUnlock = false
    private var lastRotateUptime = 0L

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) pendingUnlock = true
        }
    }

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "scene_id") main.post { current().let { s -> listeners.toList().forEach { it.onSceneChanged(s, true) } } }
    }

    fun current(): SceneMeta = repo.get(prefs.sceneId) ?: repo.first()

    private fun systemDark(): Boolean =
        (app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun candidates() = Playlist.candidates(repo.scenes, prefs.source, prefs.favorites, prefs.followDarkMode, systemDark())

    /** the user picked a scene in the app */
    fun select(id: String) {
        prefs.lastChangeMs = System.currentTimeMillis()
        prefs.sceneId = id
    }

    /** advance the playlist; returns the new scene */
    fun next(): SceneMeta {
        val n = Playlist.next(candidates(), current().id, prefs.shuffle) ?: current()
        prefs.lastChangeMs = System.currentTimeMillis()
        prefs.sceneId = n.id
        lastRotateUptime = SystemClock.elapsedRealtime()
        return n
    }

    /** Called by an engine whenever it becomes visible. Applies any pending automatic change. */
    fun onVisible() {
        val now = System.currentTimeMillis()
        when (prefs.autoMode) {
            AutoMode.UNLOCK -> if (pendingUnlock) next()
            AutoMode.INTERVAL -> if (now - prefs.lastChangeMs >= prefs.intervalMinutes * 60_000L) next()
            AutoMode.OFF -> {}
        }
        pendingUnlock = false
        if (prefs.followDarkMode && current().isDark != systemDark()) next()
    }

    /** Called about once a second by a visible engine; handles the interval timer while the screen stays on. */
    fun tick() {
        if (prefs.autoMode == AutoMode.INTERVAL && System.currentTimeMillis() - prefs.lastChangeMs >= prefs.intervalMinutes * 60_000L) next()
    }

    fun attach(l: Listener) {
        listeners.add(l)
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(app, unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT), ContextCompat.RECEIVER_NOT_EXPORTED)
            prefs.registerListener(prefListener)
            receiverRegistered = true
        }
    }

    fun detach(l: Listener) {
        listeners.remove(l)
        if (listeners.isEmpty() && receiverRegistered) {
            runCatching { app.unregisterReceiver(unlockReceiver) }
            prefs.unregisterListener(prefListener)
            receiverRegistered = false
        }
    }

    companion object {
        @Volatile private var instance: SceneController? = null
        /** tests build a fresh application per test, so the cached controller must be dropped */
        internal fun resetForTests() { instance = null }
        fun get(context: Context): SceneController = instance ?: synchronized(this) {
            instance ?: SceneController(context.applicationContext).also { instance = it }
        }
    }
}
