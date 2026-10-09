package com.nyx.themes.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.nyx.themes.scene.SceneMeta
import com.nyx.themes.scene.SceneRenderer
import com.nyx.themes.service.NyxWallpaperService
import com.nyx.themes.service.SceneController
import java.util.concurrent.Executors

/** Ways of putting a scene on the phone. */
object WallpaperActions {
    private val pool = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    fun isNyxLiveActive(ctx: Context): Boolean {
        val info = WallpaperManager.getInstance(ctx).wallpaperInfo ?: return false
        return info.packageName == ctx.packageName && info.serviceName == NyxWallpaperService::class.java.name
    }

    /** Selects [scene] and, if Nyx is not yet the live wallpaper, opens the system confirmation screen. */
    fun applyLive(ctx: Context, scene: SceneMeta) {
        SceneController.get(ctx).select(scene.id)
        if (isNyxLiveActive(ctx)) {
            Toast.makeText(ctx, "Wallpaper changed to ${scene.name}", Toast.LENGTH_SHORT).show()
            return
        }
        openLivePicker(ctx)
    }

    fun openLivePicker(ctx: Context) {
        val change = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(ctx, NyxWallpaperService::class.java))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(change)
        } catch (e: Exception) {
            try {
                ctx.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e2: Exception) {
                Toast.makeText(ctx, "Open Settings > Wallpaper and choose Nyx", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Renders the scene's poster frame and sets it as a normal image wallpaper. [which] = WallpaperManager.FLAG_SYSTEM / FLAG_LOCK / both. */
    fun applyImage(ctx: Context, scene: SceneMeta, which: Int, done: (Boolean) -> Unit) {
        val app = ctx.applicationContext
        pool.execute {
            val ok = try {
                val dm = app.resources.displayMetrics
                val w = dm.widthPixels.coerceAtLeast(720)
                val h = dm.heightPixels.coerceAtLeast(1280)
                val r = SceneRenderer(app, scene, w, h)
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                r.draw(Canvas(bmp), scene.poster)
                r.release()
                WallpaperManager.getInstance(app).setBitmap(bmp, null, true, which)
                bmp.recycle()
                true
            } catch (e: Throwable) {
                false
            }
            main.post { done(ok) }
        }
    }
}
