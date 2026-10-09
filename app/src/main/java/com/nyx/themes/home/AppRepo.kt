package com.nyx.themes.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.net.Uri

data class AppEntry(val pkg: String, val component: ComponentName, val label: String) {
    val key get() = component.flattenToShortString()
}

/** Launchable apps installed on the phone. */
object AppRepo {
    fun load(ctx: Context): List<AppEntry> {
        val pm = ctx.packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val infos = try { pm.queryIntentActivities(main, 0) } catch (e: Exception) { emptyList() }
        return infos.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            if (ai.packageName == ctx.packageName && ai.name.endsWith("HomeActivity")) return@mapNotNull null
            AppEntry(ai.packageName, ComponentName(ai.packageName, ai.name), ri.loadLabel(pm)?.toString() ?: ai.packageName)
        }.distinctBy { it.key }.sortedBy { it.label.lowercase() }
    }

    /** the phone's default apps for the dock: dialer, messages, browser, camera */
    fun defaultDock(ctx: Context, apps: List<AppEntry>): List<AppEntry> {
        val pm = ctx.packageManager
        val intents = listOf(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:")),
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
        )
        val out = ArrayList<AppEntry>()
        for ((i, intent) in intents.withIndex()) {
            val pkg = runCatching { pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName }.getOrNull()
            val glyph = listOf("phone", "messages", "browser", "camera")[i]
            val hit = apps.firstOrNull { it.pkg == pkg && pkg != "android" } ?: apps.firstOrNull { IconMap.glyphFor(it.pkg, it.label) == glyph }
            if (hit != null && hit !in out) out.add(hit)
        }
        return out
    }

    /** first-run pinned apps: one app per common glyph, in a friendly order */
    fun defaultPins(apps: List<AppEntry>, dock: List<AppEntry>): List<String> {
        val order = listOf("gallery", "maps", "clock", "settings", "music", "mail", "calendar", "notes", "files", "store", "video", "weather")
        val out = ArrayList<String>()
        for (g in order) {
            val hit = apps.firstOrNull { it !in dock && it.key !in out && IconMap.glyphFor(it.pkg, it.label) == g }
            if (hit != null) out.add(hit.key)
        }
        if (out.size < 8) apps.filter { it !in dock && it.key !in out }.take(8 - out.size).forEach { out.add(it.key) }
        return out
    }
}
