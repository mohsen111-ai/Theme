package com.nyx.themes

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.nyx.themes.scene.SceneRenderer
import com.nyx.themes.scene.SceneRepo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class RenderTest {
    @Test
    fun rendersEveryScene() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = SceneRepo.get(ctx)
        val out = File(System.getProperty("nyx.renders") ?: "build/test-renders").apply { mkdirs() }
        for (scene in repo.scenes) {
            val w = 540
            val h = 1170
            val r = SceneRenderer(ctx, scene, w, h)
            for (t in listOf(scene.poster, scene.poster + 7.3f)) {
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                r.draw(Canvas(bmp), t)
                var nonBlank = 0
                for (y in 0 until h step 37) for (x in 0 until w step 29) if ((bmp.getPixel(x, y) ushr 24) == 255) nonBlank++
                assertTrue("${scene.id} drew nothing", nonBlank > 100)
                FileOutputStream(File(out, "${scene.id}_${t.toInt()}.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
            r.release()
        }
    }
}
