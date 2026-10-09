package com.nyx.themes.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import com.nyx.themes.data.IconPackId
import java.util.concurrent.Executors

/** Produces the themed icon bitmap for an app in the chosen pack. */
class IconThemer(context: Context, val pack: IconPackId) {
    private val app = context.applicationContext
    private val cache = object : LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    private val assetCache = HashMap<String, Bitmap?>()
    private val pool = Executors.newFixedThreadPool(2)
    private val main = Handler(Looper.getMainLooper())

    @Synchronized
    private fun asset(name: String): Bitmap? = assetCache.getOrPut(name) {
        runCatching { app.assets.open("icons/${pack.dir}/$name.png").use { BitmapFactory.decodeStream(it) } }.getOrNull()
    }

    fun icon(entry: AppEntry, sizePx: Int): Bitmap {
        val key = "${pack.dir}/${entry.key}/$sizePx"
        cache.get(key)?.let { return it }
        val bmp = build(entry, sizePx)
        cache.put(key, bmp)
        return bmp
    }

    /** loads asynchronously into [view]; safe with recycled views */
    fun load(entry: AppEntry, sizePx: Int, view: ImageView) {
        val key = "${pack.dir}/${entry.key}/$sizePx"
        view.tag = key
        cache.get(key)?.let { view.setImageBitmap(it); return }
        view.setImageDrawable(null)
        pool.execute {
            val b = runCatching { icon(entry, sizePx) }.getOrNull() ?: return@execute
            main.post { if (view.tag == key) view.setImageBitmap(b) }
        }
    }

    private fun build(entry: AppEntry, size: Int): Bitmap {
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val glyph = IconMap.glyphFor(entry.pkg, entry.label)?.let { asset(it) }
        if (glyph != null) {
            c.drawBitmap(glyph, null, RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
            return out
        }
        // no painted icon for this app: the app's own icon inside the pack's tile
        asset("tile")?.let { c.drawBitmap(it, null, RectF(0f, 0f, size.toFloat(), size.toFloat()), paint) }
        val inset = when (pack) { IconPackId.PAPER -> .24f; IconPackId.NIGHT -> .22f; IconPackId.MOON -> .24f } * size
        val drawable = runCatching { app.packageManager.getActivityIcon(entry.component) }.getOrNull() ?: return out
        c.save()
        if (pack == IconPackId.MOON) {
            val p = Path(); p.addCircle(size / 2f, size / 2f, size / 2f - inset, Path.Direction.CW); c.clipPath(p)
        }
        drawable.setBounds(inset.toInt(), inset.toInt(), (size - inset).toInt(), (size - inset).toInt())
        drawable.draw(c)
        c.restore()
        return out
    }
}
