package com.nyx.themes

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.nyx.themes.scene.SceneRenderer
import com.nyx.themes.scene.SceneRepo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class PerfTest {
    @Test
    fun frameCost() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val w = 810; val h = 1755
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val rows = ArrayList<String>()
        for (s in SceneRepo.get(ctx).scenes) {
            val r = SceneRenderer(ctx, s, w, h)
            r.draw(c, 1f)
            val t0 = System.nanoTime()
            for (i in 0 until 10) r.draw(c, 2f + i * .1f)
            val ms = (System.nanoTime() - t0) / 1e7
            rows.add("%6.1f ms  %s %s fx=%d sky=%d".format(ms, s.id, s.mode, s.fx.length(), s.sky.size))
            r.release()
        }
        rows.sortDescending()
        java.io.File("build/perf.txt").writeText(rows.joinToString("\n"))
    }
}
