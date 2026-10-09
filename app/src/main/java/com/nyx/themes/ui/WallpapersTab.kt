package com.nyx.themes.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nyx.themes.data.Prefs
import com.nyx.themes.scene.Mode
import com.nyx.themes.scene.SceneMeta
import com.nyx.themes.scene.SceneRepo

/** The wallpaper browser: filter chips and a grid of thumbnails. */
class WallpapersTab(ctx: Context, private val onOpen: (SceneMeta) -> Unit) : LinearLayout(ctx) {
    private val repo = SceneRepo.get(ctx)
    private val prefs = Prefs(ctx)
    private val filters: List<Pair<String, (SceneMeta) -> Boolean>> = listOf(
        "All" to { _ -> true }, "Dark" to { s -> s.isDark }, "Light" to { s -> !s.isDark },
        "Still" to { s -> s.mode == Mode.STILL }, "Live" to { s -> s.mode == Mode.LIVE }, "Long" to { s -> s.mode == Mode.LONG },
        "Minimal" to { s -> s.style == "min" }, "Ink sketch" to { s -> s.style == "ink" }, "Favorites" to { s -> s.id in prefs.favorites },
    )
    private var selected = 0
    private val chipRow = Ui.hbox(ctx)
    private val adapter = Adapter()
    private val list = RecyclerView(ctx)

    init {
        orientation = VERTICAL
        val scroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false; addView(chipRow) }
        chipRow.setPadding(Ui.dp(ctx, 12), Ui.dp(ctx, 4), Ui.dp(ctx, 12), Ui.dp(ctx, 10))
        addView(scroll, Ui.lp(Ui.MATCH, Ui.WRAP))
        val span = (ctx.resources.displayMetrics.widthPixels / Ui.dp(ctx, 170f)).coerceIn(2, 5)
        list.layoutManager = GridLayoutManager(ctx, span)
        list.adapter = adapter
        list.clipToPadding = false
        list.setPadding(Ui.dp(ctx, 10), 0, Ui.dp(ctx, 10), Ui.dp(ctx, 16))
        addView(list, Ui.lp(Ui.MATCH, 0, 1f))
        refresh()
    }

    fun refresh() {
        chipRow.removeAllViews()
        filters.forEachIndexed { i, (label, _) ->
            val chip = Ui.chip(context, label, i == selected) { selected = i; refresh() }
            chipRow.addView(chip, Ui.lp(Ui.WRAP, Ui.WRAP).apply { rightMargin = Ui.dp(context, 8) })
        }
        adapter.submit(repo.scenes.filter(filters[selected].second))
    }

    private inner class VH(val root: LinearLayout, val img: ImageView, val badge: TextView, val heart: TextView, val name: TextView, val tags: TextView) : RecyclerView.ViewHolder(root)

    private inner class Adapter : RecyclerView.Adapter<VH>() {
        private var items: List<SceneMeta> = emptyList()
        fun submit(l: List<SceneMeta>) { items = l; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val c = parent.context
            val root = Ui.vbox(c).apply { setPadding(Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 10)); isClickable = true; isFocusable = true }
            val frame = AspectFrame(c, 780f / 360f).apply { background = Ui.round(c, Ui.PANEL, 16f, Ui.LINE) }
            val img = ImageView(c).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
            frame.addView(img, Ui.fl(Ui.MATCH, Ui.MATCH))
            Ui.clipRounded(frame, 16f)
            val badge = Ui.text(c, "", 10f, Ui.AMBER, bold = true).apply {
                setPadding(Ui.dp(c, 8), Ui.dp(c, 3), Ui.dp(c, 8), Ui.dp(c, 3)); background = Ui.round(c, 0xCC090B14.toInt(), 12f, 0x66F0A54A)
            }
            frame.addView(badge, Ui.fl(Ui.WRAP, Ui.WRAP).apply { gravity = Gravity.TOP or Gravity.START; setMargins(Ui.dp(c, 8), Ui.dp(c, 8), 0, 0) })
            val heart = Ui.text(c, "♥", 18f, 0xFFFF7A8A.toInt())
            frame.addView(heart, Ui.fl(Ui.WRAP, Ui.WRAP).apply { gravity = Gravity.TOP or Gravity.END; setMargins(0, Ui.dp(c, 6), Ui.dp(c, 10), 0) })
            root.addView(frame, Ui.lp(Ui.MATCH, Ui.WRAP))
            val name = Ui.text(c, "", 17f, Ui.INK, display = true).apply { setPadding(Ui.dp(c, 4), Ui.dp(c, 8), 0, 0) }
            val tags = Ui.text(c, "", 11f, Ui.MUTE)
            tags.setPadding(Ui.dp(c, 4), 0, 0, 0)
            root.addView(name, Ui.lp(Ui.MATCH, Ui.WRAP)); root.addView(tags, Ui.lp(Ui.MATCH, Ui.WRAP))
            return VH(root, img, badge, heart, name, tags)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val s = items[position]
            ThumbLoader.load(h.root.context, "scenes/${s.thumb}", h.img)
            h.name.text = s.name
            h.tags.text = (if (s.isDark) "Dark" else "Light") + " · " + (if (s.style == "min") "Minimal" else "Ink sketch")
            h.badge.visibility = if (s.mode == Mode.STILL) View.GONE else View.VISIBLE
            h.badge.text = if (s.mode == Mode.LONG) "LONG" else "LIVE"
            h.heart.visibility = if (s.id in prefs.favorites) View.VISIBLE else View.GONE
            h.root.contentDescription = "${s.name}, ${s.mode.name.lowercase()}"
            h.root.setOnClickListener { onOpen(s) }
        }

        override fun getItemCount() = items.size
    }
}
