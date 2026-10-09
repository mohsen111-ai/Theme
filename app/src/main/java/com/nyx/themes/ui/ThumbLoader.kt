package com.nyx.themes.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.util.concurrent.Executors

/** Loads small bitmaps from assets off the main thread with a memory cache. */
object ThumbLoader {
    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    private val pool = Executors.newFixedThreadPool(2)
    private val main = Handler(Looper.getMainLooper())

    fun load(ctx: Context, assetPath: String, into: ImageView) {
        into.tag = assetPath
        cache.get(assetPath)?.let { into.setImageBitmap(it); return }
        into.setImageDrawable(null)
        val app = ctx.applicationContext
        pool.execute {
            val bmp = runCatching { app.assets.open(assetPath).use { BitmapFactory.decodeStream(it) } }.getOrNull() ?: return@execute
            cache.put(assetPath, bmp)
            main.post { if (into.tag == assetPath) into.setImageBitmap(bmp) }
        }
    }

    fun get(ctx: Context, assetPath: String): Bitmap? {
        cache.get(assetPath)?.let { return it }
        val bmp = runCatching { ctx.applicationContext.assets.open(assetPath).use { BitmapFactory.decodeStream(it) } }.getOrNull() ?: return null
        cache.put(assetPath, bmp)
        return bmp
    }
}
