package com.aura.study

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

enum class Phase(val label: String) { FOCUS("Focus"), SHORT("Short break"), LONG("Long break") }

/** Timer state shared between the service and the screens. Main thread only. */
object TimerState {
    var phase = Phase.FOCUS
    var running = false
    var totalMs = 25 * 60_000L
    var remainingAtPause = 25 * 60_000L
    var endAt = 0L
    var focusDone = 0
    private val listeners = ArrayList<() -> Unit>()

    fun remaining(): Long = if (running) (endAt - SystemClock.elapsedRealtime()).coerceAtLeast(0) else remainingAtPause
    fun progress(): Float = if (totalMs <= 0) 0f else 1f - remaining().toFloat() / totalMs
    fun listen(l: () -> Unit) { listeners.add(l) }
    fun unlisten(l: () -> Unit) { listeners.remove(l) }
    fun changed() { listeners.toList().forEach { it() } }
    fun reset(prefs: Prefs) { phase = Phase.FOCUS; running = false; totalMs = prefs.focusMin * 60_000L; remainingAtPause = totalMs; focusDone = 0 }
}

class TimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs
    private lateinit var store: Store
    private val player = SoundPlayer()
    private val tick = object : Runnable {
        override fun run() {
            if (!TimerState.running) return
            if (TimerState.remaining() <= 0) complete() else { TimerState.changed(); handler.postDelayed(this, 500) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this); store = Store(this)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH_TIMER, "Timer", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel(CH_DONE, "Session finished", NotificationManager.IMPORTANCE_HIGH))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
            ACTION_SKIP -> { advance(); if (TimerState.running) begin() else pauseUi() }
            else -> if (!TimerState.running) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        if (TimerState.remainingAtPause <= 0) { TimerState.remainingAtPause = TimerState.totalMs }
        TimerState.running = true
        begin()
    }

    private fun begin() {
        TimerState.endAt = SystemClock.elapsedRealtime() + TimerState.remainingAtPause
        showForeground()
        if (TimerState.phase == Phase.FOCUS && prefs.sound != "off") player.play(prefs.sound, prefs.volume) else player.stop()
        handler.removeCallbacks(tick); handler.post(tick); TimerState.changed()
    }

    private fun pause() {
        TimerState.remainingAtPause = TimerState.remaining(); TimerState.running = false
        handler.removeCallbacks(tick); player.stop(); pauseUi()
    }

    private fun pauseUi() { TimerState.changed(); showForeground(); }

    private fun stop() {
        handler.removeCallbacks(tick); player.stop()
        TimerState.reset(prefs); TimerState.changed()
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
    }

    private fun advance() {
        TimerState.phase = if (TimerState.phase == Phase.FOCUS) { if (TimerState.focusDone > 0 && TimerState.focusDone % 4 == 0) Phase.LONG else Phase.SHORT } else Phase.FOCUS
        TimerState.totalMs = when (TimerState.phase) { Phase.FOCUS -> prefs.focusMin; Phase.SHORT -> prefs.breakMin; Phase.LONG -> prefs.longBreakMin } * 60_000L
        TimerState.remainingAtPause = TimerState.totalMs
    }

    private fun complete() {
        if (TimerState.phase == Phase.FOCUS) {
            store.addSession(Days.today(), (TimerState.totalMs / 60_000L).toInt(), prefs.subject)
            TimerState.focusDone++
        }
        val finished = TimerState.phase
        advance()
        alert(finished)
        if (prefs.autoNext) { TimerState.running = true; begin() } else { TimerState.running = false; player.stop(); handler.removeCallbacks(tick); TimerState.changed(); stopForeground(STOP_FOREGROUND_DETACH) }
    }

    private fun alert(finished: Phase) {
        val text = if (finished == Phase.FOCUS) "Focus session done. Time for a ${TimerState.phase.label.lowercase()}." else "Break over. Ready to focus?"
        val n = NotificationCompat.Builder(this, CH_DONE).setSmallIcon(R.drawable.ic_stat).setContentTitle("Aura").setContentText(text).setAutoCancel(true)
            .setContentIntent(open()).setDefaults(Notification.DEFAULT_ALL).build()
        runCatching { getSystemService(NotificationManager::class.java).notify(2, n) }
        runCatching {
            val v = getSystemService(Vibrator::class.java)
            if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 120, 200), -1)) else @Suppress("DEPRECATION") v.vibrate(400)
        }
    }

    private fun open() = PendingIntent.getActivity(this, 0, Intent(this, com.aura.study.ui.MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    private fun action(label: String, a: String) = NotificationCompat.Action(0, label, PendingIntent.getService(this, a.hashCode(), Intent(this, TimerService::class.java).setAction(a), PendingIntent.FLAG_IMMUTABLE))

    private fun showForeground() {
        val running = TimerState.running
        val b = NotificationCompat.Builder(this, CH_TIMER).setSmallIcon(R.drawable.ic_stat).setContentTitle(TimerState.phase.label).setOngoing(true).setOnlyAlertOnce(true).setContentIntent(open())
        if (running) b.setUsesChronometer(true).setChronometerCountDown(true).setWhen(System.currentTimeMillis() + TimerState.remaining()).setContentText(prefs.subject)
        else b.setContentText("Paused")
        b.addAction(if (running) action("Pause", ACTION_PAUSE) else action("Resume", ACTION_START)).addAction(action("Stop", ACTION_STOP))
        val n = b.build()
        runCatching { if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(1, n) }
    }

    override fun onDestroy() { handler.removeCallbacks(tick); player.stop(); super.onDestroy() }

    companion object {
        const val ACTION_START = "com.aura.study.START"; const val ACTION_PAUSE = "com.aura.study.PAUSE"; const val ACTION_STOP = "com.aura.study.STOP"; const val ACTION_SKIP = "com.aura.study.SKIP"
        private const val CH_TIMER = "timer"; private const val CH_DONE = "done"
        fun send(ctx: Context, action: String) {
            val i = Intent(ctx, TimerService::class.java).setAction(action)
            if (action == ACTION_START) androidx.core.content.ContextCompat.startForegroundService(ctx, i) else ctx.startService(i)
        }
    }
}
