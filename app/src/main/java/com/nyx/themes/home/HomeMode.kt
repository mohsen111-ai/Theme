package com.nyx.themes.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.nyx.themes.data.Prefs

/**
 * "Wallpapers only" switch. When on, the Nyx Home launcher component is disabled, so it does not exist as a home app
 * and the phone keeps its own launcher and icons. When off, Nyx Home can be chosen as the home app.
 */
object HomeMode {
    private fun component(ctx: Context) = ComponentName(ctx, HomeActivity::class.java)

    fun isHomeComponentEnabled(ctx: Context): Boolean =
        ctx.packageManager.getComponentEnabledSetting(component(ctx)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    /** is Nyx Home currently the phone's default home app? */
    fun isDefaultHome(ctx: Context): Boolean {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val ri = ctx.packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)
        return ri?.activityInfo?.packageName == ctx.packageName
    }

    enum class Result { OK, NYX_IS_DEFAULT_HOME }

    /** Applies the switch. Refuses to disable Nyx Home while it is the default home app (that would strand the user). */
    fun setWallpapersOnly(ctx: Context, on: Boolean): Result {
        val prefs = Prefs(ctx)
        if (on && isDefaultHome(ctx)) return Result.NYX_IS_DEFAULT_HOME
        prefs.wallpapersOnly = on
        apply(ctx)
        return Result.OK
    }

    /** brings the component state in line with the saved preference (also called at app start) */
    fun apply(ctx: Context) {
        val wantEnabled = !Prefs(ctx).wallpapersOnly
        if (!wantEnabled && isDefaultHome(ctx)) return // never pull the launcher out from under the user
        val state = if (wantEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        if (ctx.packageManager.getComponentEnabledSetting(component(ctx)) != state) {
            ctx.packageManager.setComponentEnabledSetting(component(ctx), state, PackageManager.DONT_KILL_APP)
        }
    }
}
