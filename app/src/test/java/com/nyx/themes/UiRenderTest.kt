package com.nyx.themes

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.nyx.themes.data.Prefs
import com.nyx.themes.home.HomeActivity
import com.nyx.themes.ui.DetailActivity
import com.nyx.themes.ui.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/** Opens every screen for real (Robolectric) and draws it to a PNG so the layout can be checked. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h780dp-xxhdpi")
class UiRenderTest {
    private val out = File(System.getProperty("nyx.renders") ?: "build/test-renders").apply { mkdirs() }

    private fun settle() { Thread.sleep(400); shadowOf(Looper.getMainLooper()).idle() }

    private fun snapshot(activity: Activity, name: String): Bitmap {
        val w = 1080
        val h = 2340
        val v = activity.window.decorView
        v.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
        v.layout(0, 0, w, h)
        settle()
        v.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
        v.layout(0, 0, w, h)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        v.draw(Canvas(bmp))
        FileOutputStream(File(out, "ui_$name.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return bmp
    }

    private fun findText(root: View, text: String): View? {
        if (root is TextView && root.text.toString() == text) return root
        if (root is ViewGroup) for (i in 0 until root.childCount) findText(root.getChildAt(i), text)?.let { return it }
        return null
    }

    @Test
    fun mainScreens() {
        val ctrl = Robolectric.buildActivity(MainActivity::class.java).setup()
        val a = ctrl.get()
        snapshot(a, "main_wallpapers")
        val root = a.window.decorView
        for ((label, name) in listOf("Icons & Home" to "main_home", "Settings" to "main_settings")) {
            val tab = findText(root, label)
            assertNotNull("tab $label missing", tab)
            tab!!.performClick()
            snapshot(a, name)
        }
    }

    @Test
    fun detailScreen() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        for (id in listOf("ferris", "valleydawn")) {
            val i = Intent(ctx, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_ID, id)
            val a = Robolectric.buildActivity(DetailActivity::class.java, i).setup().get()
            val bmp = snapshot(a, "detail_$id")
            // the preview must have drawn something
            var colourful = 0
            for (y in 0 until bmp.height step 61) for (x in 0 until bmp.width step 53) if (bmp.getPixel(x, y) != 0xFF000000.toInt()) colourful++
            assertTrue("detail $id is blank", colourful > 200)
        }
    }

    @Test
    fun homeScreenAndDrawer() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = shadowOf(ctx.packageManager)
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("Phone" to "com.android.dialer", "Messages" to "com.google.android.apps.messaging", "Chrome" to "com.android.chrome", "Camera" to "com.android.camera",
            "Gallery" to "com.google.android.apps.photos", "Maps" to "com.google.android.apps.maps", "Clock" to "com.google.android.deskclock", "Settings" to "com.android.settings",
            "YouTube" to "com.google.android.youtube", "WhatsApp" to "com.whatsapp", "Calculator" to "com.google.android.calculator", "Proton VPN" to "ch.protonvpn.android",
            "Some Game" to "com.studio.somegame", "Odd App" to "org.example.oddapp").forEach { (label, pkg) ->
            val ai = ActivityInfo().apply { packageName = pkg; name = "$pkg.Main"; nonLocalizedLabel = label; applicationInfo = android.content.pm.ApplicationInfo().apply { packageName = pkg } }
            val ri = ResolveInfo().apply { activityInfo = ai; nonLocalizedLabel = label }
            pm.addOrUpdateActivity(ai)
            pm.addResolveInfoForIntent(main, ri)
        }
        Prefs(ctx).wallpapersOnly = false
        val a = Robolectric.buildActivity(HomeActivity::class.java).setup().get()
        snapshot(a, "home")
    }
}
