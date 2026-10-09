package com.nyx.themes.scene

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONObject

enum class Mode { STILL, LIVE, LONG }

/** One wallpaper. [fx] is the raw effect list; it is turned into renderers by [SceneRenderer]. */
class SceneMeta(
    val id: String,
    val name: String,
    val mode: Mode,
    val theme: String,
    val style: String,
    val loop: Float,
    val poster: Float,
    val bg: String,
    val thumb: String,
    val sky: List<Pair<Float, String>>,
    internal val fx: JSONArray,
) {
    val isDark get() = theme == "dark"
    val isAnimated get() = mode != Mode.STILL
    override fun toString() = "SceneMeta($id)"
}

class SceneRepo private constructor(val scenes: List<SceneMeta>) {
    private val byId = scenes.associateBy { it.id }
    fun get(id: String?): SceneMeta? = if (id == null) null else byId[id]
    fun first(): SceneMeta = scenes.first()

    companion object {
        @Volatile private var instance: SceneRepo? = null

        fun get(context: Context): SceneRepo = instance ?: synchronized(this) {
            instance ?: load(context.applicationContext).also { instance = it }
        }

        fun parse(json: String): SceneRepo {
            val arr = JSONArray(json)
            val list = ArrayList<SceneMeta>(arr.length())
            for (i in 0 until arr.length()) list.add(parseScene(arr.getJSONObject(i)))
            require(list.isNotEmpty()) { "no scenes" }
            return SceneRepo(list)
        }

        private fun parseScene(o: JSONObject): SceneMeta {
            val sky = o.optJSONArray("sky")
            return SceneMeta(
                id = o.getString("id"),
                name = o.getString("name"),
                mode = Mode.valueOf(o.getString("mode").uppercase()),
                theme = o.getString("theme"),
                style = o.getString("style"),
                loop = o.optDouble("loop", 0.0).toFloat(),
                poster = o.optDouble("poster", 2.5).toFloat(),
                bg = o.getString("bg"),
                thumb = o.getString("thumb"),
                sky = if (sky == null) emptyList() else List(sky.length()) { sky.getJSONObject(it).let { s -> s.getDouble("p").toFloat() to s.getString("img") } },
                fx = o.optJSONArray("fx") ?: JSONArray(),
            )
        }

        private fun load(context: Context): SceneRepo =
            parse(context.assets.open("scenes/index.json").bufferedReader().use { it.readText() })

        fun decodeAsset(context: Context, path: String, maxWidth: Int = 0, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
            val opts = BitmapFactory.Options().apply { inPreferredConfig = config }
            if (maxWidth > 0) {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open("scenes/$path").use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= maxWidth * 0.9f) sample *= 2
                opts.inSampleSize = sample
            }
            return context.assets.open("scenes/$path").use { BitmapFactory.decodeStream(it, null, opts) }
                ?: throw IllegalStateException("cannot decode $path")
        }
    }
}
