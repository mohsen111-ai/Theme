package com.nyx.themes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nyx.themes.data.AutoMode
import com.nyx.themes.data.Playlist
import com.nyx.themes.data.Prefs
import com.nyx.themes.data.Source
import com.nyx.themes.home.IconMap
import com.nyx.themes.scene.Css
import com.nyx.themes.scene.Mode
import com.nyx.themes.scene.SceneRepo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LogicTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun cssColours() {
        assertEquals(0xFF112233.toInt(), Css.parse("#123"[0].let { "#112233" }))
        assertEquals(0xFFAABBCC.toInt(), Css.parse("#abc"))
        val c = Css.parse("rgba(255, 190, 90, .5)")
        assertEquals(128, c ushr 24)
        assertEquals(255, (c shr 16) and 0xFF)
        assertEquals(0xFF000000.toInt(), Css.parse("rgb(0,0,0)"))
        assertEquals(64, Css.withAlpha(0xFF000000.toInt(), .25f) ushr 24)
        assertEquals(0xFF808080.toInt(), Css.mix(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), .5f))
    }

    @Test
    fun catalogueIsConsistent() {
        val repo = SceneRepo.get(ctx)
        val ids = repo.scenes.map { it.id }
        assertEquals("scene ids must be unique", ids.size, ids.toSet().size)
        assertTrue("expected a full catalogue", repo.scenes.size >= 40)
        for (s in repo.scenes) {
            for (path in listOf(s.bg, s.thumb) + s.sky.map { it.second }) {
                ctx.assets.open("scenes/$path").close()
            }
            if (s.mode == Mode.LONG) {
                assertTrue("${s.id}: long scenes need a loop over 5s", s.loop > 5f)
                assertTrue("${s.id}: long scenes need sky frames", s.sky.size >= 2)
            }
            if (s.mode == Mode.STILL) assertEquals("${s.id}: still scenes have no effects", 0, s.fx.length())
            assertTrue("${s.id}: live scenes need effects", s.mode == Mode.STILL || s.fx.length() > 0)
        }
        assertTrue(repo.scenes.any { !it.isDark })
        assertTrue(repo.scenes.count { it.mode == Mode.LIVE } >= 15)
        assertTrue(repo.scenes.count { it.mode == Mode.LONG } >= 3)
    }

    @Test
    fun iconPacksShipEveryGlyph() {
        for (pack in com.nyx.themes.data.IconPackId.values()) {
            for (g in IconMap.GLYPHS + "tile") ctx.assets.open("icons/${pack.dir}/$g.png").close()
        }
    }

    @Test
    fun playlistFiltersAndAdvances() {
        val all = SceneRepo.get(ctx).scenes
        val dark = Playlist.candidates(all, Source.DARK, emptySet(), false, true)
        assertTrue(dark.isNotEmpty() && dark.all { it.isDark })
        val light = Playlist.candidates(all, Source.LIGHT, emptySet(), false, false)
        assertTrue(light.isNotEmpty() && light.none { it.isDark })
        val favs = setOf(all[0].id, all[3].id)
        assertEquals(favs, Playlist.candidates(all, Source.FAVORITES, favs, false, true).map { it.id }.toSet())
        // empty favourites never leaves the phone with nothing to show
        assertEquals(all.size, Playlist.candidates(all, Source.FAVORITES, emptySet(), false, true).size)
        // follow dark mode picks matching themes even if the source is "all"
        assertTrue(Playlist.candidates(all, Source.ALL, emptySet(), true, false).none { it.isDark })
        assertTrue(Playlist.candidates(all, Source.ALL, emptySet(), true, true).all { it.isDark })
        assertTrue(Playlist.candidates(all, Source.LIVE, emptySet(), false, true).none { it.mode == Mode.STILL })
        // shuffle never repeats the current scene; sequential wraps
        val rnd = Random(1)
        repeat(50) { assertTrue(Playlist.next(dark, dark[2].id, true, rnd)!!.id != dark[2].id) }
        assertEquals(dark[0].id, Playlist.next(dark, dark.last().id, false)!!.id)
        assertEquals(dark[1].id, Playlist.next(dark, dark[0].id, false)!!.id)
        assertEquals(dark[0].id, Playlist.next(listOf(dark[0]), dark[0].id, true)!!.id)
        assertNull(Playlist.next(emptyList(), null, true))
    }

    @Test
    fun prefsRoundTrip() {
        val p = Prefs(ctx)
        assertTrue("wallpapers only is the safe default", p.wallpapersOnly)
        assertEquals(AutoMode.OFF, p.autoMode)
        p.autoMode = AutoMode.INTERVAL; p.intervalMinutes = 15; p.source = Source.INK; p.shuffle = false; p.fps = 45
        val q = Prefs(ctx)
        assertEquals(AutoMode.INTERVAL, q.autoMode); assertEquals(15, q.intervalMinutes); assertEquals(Source.INK, q.source); assertFalse(q.shuffle); assertEquals(45, q.fps)
        assertTrue(q.toggleFavorite("a")); assertTrue("a" in q.favorites); assertFalse(q.toggleFavorite("a")); assertFalse("a" in q.favorites)
        p.fps = 500; assertEquals(60, p.fps)
        p.intervalMinutes = 0; assertEquals(1, p.intervalMinutes)
    }

    @Test
    fun iconMapping() {
        fun g(pkg: String, label: String) = IconMap.glyphFor(pkg, label)
        assertEquals("settings", g("com.android.settings", "Settings"))
        assertEquals("phone", g("com.google.android.dialer", "Phone"))
        assertEquals("camera", g("com.android.camera", "Camera"))
        assertEquals("browser", g("com.android.chrome", "Chrome"))
        assertEquals("video", g("com.google.android.youtube", "YouTube"))
        assertEquals("music", g("com.google.android.apps.youtube.music", "YT Music"))
        assertEquals("chat", g("com.whatsapp", "WhatsApp"))
        assertEquals("chat", g("org.telegram.messenger", "Telegram"))
        assertEquals("social", g("com.instagram.android", "Instagram"))
        assertEquals("mail", g("com.google.android.gm", "Gmail"))
        assertEquals("shield", g("ch.protonvpn.android", "Proton VPN"))
        assertEquals("assistant", g("com.openai.chatgpt", "ChatGPT"))
        assertEquals("maps", g("com.google.android.apps.maps", "Maps"))
        assertEquals("gallery", g("com.google.android.apps.photos", "Photos"))
        assertEquals("store", g("com.android.vending", "Play Store"))
        assertNull("unknown apps fall back to a tile", g("org.example.oddapp", "Odd App"))
        assertNotNull(g("com.studio.somegame", "Some Game"))
    }
}
