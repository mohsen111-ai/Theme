package com.nyx.themes

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import androidx.test.core.app.ApplicationProvider
import com.nyx.themes.data.AutoMode
import com.nyx.themes.data.Prefs
import com.nyx.themes.home.HomeActivity
import com.nyx.themes.home.HomeMode
import com.nyx.themes.service.NyxWallpaperService
import com.nyx.themes.service.SceneController
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** a surface that draws into a bitmap and counts frames */
private class FakeHolder(val bmp: Bitmap) : SurfaceHolder {
    @Volatile var posted = 0
    override fun addCallback(callback: SurfaceHolder.Callback?) {}
    override fun removeCallback(callback: SurfaceHolder.Callback?) {}
    override fun isCreating() = false
    @Deprecated("") override fun setType(type: Int) {}
    override fun setFixedSize(width: Int, height: Int) {}
    override fun setSizeFromLayout() {}
    override fun setFormat(format: Int) {}
    override fun setKeepScreenOn(screenOn: Boolean) {}
    override fun lockCanvas(): Canvas = Canvas(bmp)
    override fun lockCanvas(dirty: Rect?): Canvas = Canvas(bmp)
    override fun unlockCanvasAndPost(canvas: Canvas?) { posted++ }
    override fun getSurface(): Surface = Surface(android.graphics.SurfaceTexture(0))
    override fun getSurfaceFrame() = Rect(0, 0, bmp.width, bmp.height)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class BehaviourTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<Context>()

    @Before fun fresh() { SceneController.resetForTests() }
    @After fun cleanup() { SceneController.resetForTests() }

    private fun waitFor(ms: Long = 8000, what: String, cond: () -> Boolean) {
        val end = System.currentTimeMillis() + ms
        while (!cond()) {
            shadowOf(Looper.getMainLooper()).idle()
            // Robolectric freezes SystemClock; delayed frames on the render thread only fire when it moves
            org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(40))
            if (System.currentTimeMillis() > end) throw AssertionError("timed out waiting for $what\n" + org.robolectric.shadows.ShadowLog.getLogs().joinToString("\n") { "${it.tag}: ${it.msg} ${it.throwable?.let { t -> android.util.Log.getStackTraceString(t) } ?: ""}" })
            Thread.sleep(20)
        }
    }

    @Test
    fun unlockRotatesOnNextVisibility() {
        val c = SceneController.get(ctx)
        c.prefs.autoMode = AutoMode.UNLOCK; c.prefs.shuffle = false
        val first = c.current().id
        c.onVisible()
        assertEquals("no unlock yet, nothing changes", first, c.current().id)
        val l = object : SceneController.Listener { override fun onSceneChanged(scene: com.nyx.themes.scene.SceneMeta, animate: Boolean) {} }
        c.attach(l)
        ctx.sendBroadcast(Intent(Intent.ACTION_USER_PRESENT))
        shadowOf(Looper.getMainLooper()).idle()
        c.onVisible()
        assertNotEquals("unlock must pick the next wallpaper", first, c.current().id)
        val second = c.current().id
        c.onVisible()
        assertEquals("one unlock changes the wallpaper once", second, c.current().id)
        c.detach(l)
    }

    @Test
    fun timerRotatesOnlyWhenDue() {
        val c = SceneController.get(ctx)
        c.prefs.autoMode = AutoMode.INTERVAL; c.prefs.intervalMinutes = 30; c.prefs.shuffle = false
        c.prefs.lastChangeMs = System.currentTimeMillis()
        val first = c.current().id
        c.tick(); c.onVisible()
        assertEquals("not due yet", first, c.current().id)
        c.prefs.lastChangeMs = System.currentTimeMillis() - 31 * 60_000L
        c.tick()
        assertNotEquals("due after 31 minutes", first, c.current().id)
    }

    @Test
    fun offModeNeverRotates() {
        val c = SceneController.get(ctx)
        c.prefs.autoMode = AutoMode.OFF
        val first = c.current().id
        c.prefs.lastChangeMs = 0
        c.tick(); c.onVisible()
        assertEquals(first, c.current().id)
    }

    @Test
    fun wallpaperEngineDrawsAndSwitchesScenes() {
        val prefs = Prefs(ctx)
        val repo = SceneController.get(ctx).repo
        val a = repo.scenes.first { it.id == "ferris" }
        val b = repo.scenes.first { it.id == "valleydawn" }
        prefs.sceneId = a.id
        val service = Robolectric.setupService(NyxWallpaperService::class.java)
        val engine = service.onCreateEngine()
        val bmp = Bitmap.createBitmap(540, 1170, Bitmap.Config.ARGB_8888)
        val holder = FakeHolder(bmp)
        NyxWallpaperService.debugFrames = 0
        engine.onCreate(holder)
        engine.onSurfaceCreated(holder)
        engine.onSurfaceChanged(holder, 0, 540, 1170)
        engine.onVisibilityChanged(true)
        waitFor(what = "first frames") { NyxWallpaperService.debugFrames >= 5 }
        assertEquals(a.id, NyxWallpaperService.debugLastSceneId)
        var opaque = 0
        for (y in 0 until 1170 step 40) for (x in 0 until 540 step 30) if ((bmp.getPixel(x, y) ushr 24) == 255) opaque++
        assertTrue("surface must be fully painted", opaque > 300)

        // the app picks another wallpaper: the engine follows
        SceneController.get(ctx).select(b.id)
        waitFor(what = "scene switch") { NyxWallpaperService.debugLastSceneId == b.id }
        // hidden engine stops drawing
        engine.onVisibilityChanged(false)
        Thread.sleep(300)
        val frozen = NyxWallpaperService.debugFrames
        Thread.sleep(300)
        assertEquals("no frames while hidden", frozen, NyxWallpaperService.debugFrames)
        engine.onVisibilityChanged(true)
        waitFor(what = "resume") { NyxWallpaperService.debugFrames > frozen + 3 }
        engine.onDestroy()
    }

    @Test
    fun stillSceneStopsAfterOneFrame() {
        val prefs = Prefs(ctx)
        prefs.sceneId = "observatory"
        val service = Robolectric.setupService(NyxWallpaperService::class.java)
        val engine = service.onCreateEngine()
        val holder = FakeHolder(Bitmap.createBitmap(540, 1170, Bitmap.Config.ARGB_8888))
        NyxWallpaperService.debugFrames = 0
        engine.onCreate(holder); engine.onSurfaceCreated(holder); engine.onSurfaceChanged(holder, 0, 540, 1170); engine.onVisibilityChanged(true)
        waitFor(what = "still frame") { NyxWallpaperService.debugFrames >= 1 }
        Thread.sleep(400)
        assertTrue("a still scene must not keep redrawing (battery)", NyxWallpaperService.debugFrames <= 3)
        engine.onDestroy()
    }

    private fun addHomeResolver(pkg: String) {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val ai = ActivityInfo().apply { packageName = pkg; name = "$pkg.Home"; applicationInfo = android.content.pm.ApplicationInfo().apply { packageName = pkg } }
        shadowOf(ctx.packageManager).addOrUpdateActivity(ai)
        shadowOf(ctx.packageManager).addResolveInfoForIntent(home, ResolveInfo().apply { activityInfo = ai })
    }

    @Test
    fun wallpapersOnlySwitchesTheLauncherOnAndOff() {
        val pm = ctx.packageManager
        val comp = ComponentName(ctx, HomeActivity::class.java)
        addHomeResolver("com.other.launcher") // the phone's own launcher is the default home app
        HomeMode.apply(ctx) // default: wallpapers only
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, pm.getComponentEnabledSetting(comp))
        assertEquals(HomeMode.Result.OK, HomeMode.setWallpapersOnly(ctx, false))
        assertFalse(Prefs(ctx).wallpapersOnly)
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, pm.getComponentEnabledSetting(comp))
        assertEquals(HomeMode.Result.OK, HomeMode.setWallpapersOnly(ctx, true))
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, pm.getComponentEnabledSetting(comp))
    }

    @Test
    fun wallpapersOnlyRefusesWhileNyxIsTheHomeApp() {
        HomeMode.setWallpapersOnly(ctx, false)
        addHomeResolver(ctx.packageName)
        assertTrue(HomeMode.isDefaultHome(ctx))
        assertEquals(HomeMode.Result.NYX_IS_DEFAULT_HOME, HomeMode.setWallpapersOnly(ctx, true))
        assertFalse("must stay off so the launcher is not pulled away", Prefs(ctx).wallpapersOnly)
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, ctx.packageManager.getComponentEnabledSetting(ComponentName(ctx, HomeActivity::class.java)))
    }
}
