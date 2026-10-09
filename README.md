# Nyx

Moonlit wallpapers for Android (built and tested for the Infinix GT 30 Pro, XOS 15 / Android 15).

* **41 original wallpapers** in three kinds: still, live (things move), and long (a full day or night cycle).
  Clean minimal style, an ink-sketch style, and a muted light set.
* **Nyx live wallpaper** with automatic change: on every unlock, or every 5 min to 6 h, from any playlist
  (all, dark, light, favorites, live, still, minimal, ink). Follows the phone's light/dark mode. Pauses in Battery Saver.
* Or set any scene as a plain **image** on the home screen, lock screen, or both.
* **Wallpapers only** switch (default on): Nyx Home does not exist as a launcher, so you keep your own launcher and icons.
  Switch it off to use **Nyx Home** with three painted icon packs (Paper watercolor, Night ink, Moon silhouette).

## Layout

| path | what |
|---|---|
| `app/` | the Android app (Kotlin, no UI libraries beyond AppCompat/RecyclerView) |
| `tools/` | content pipeline: `engine.js` (scene engine), `scenes/*.js` (the wallpapers), `icons.js`, `build.js` |
| `preview/index.html` | single-file gallery of every scene, drawn live in the browser |

## Build the APK

```
./gradlew assembleRelease     # app/build/outputs/apk/release/app-release.apk
./gradlew testDebugUnitTest   # Robolectric tests, they also render every scene to app/build/test-renders
```

The release key is `app/nyx-release.jks` (personal sideload key, same on every machine so updates install over old builds).

## Add or change wallpapers

Scenes are drawn in code. A scene is a static layer (baked to webp with film grain) plus a list of declarative effects
(twinkling stars, drifting clouds, spinning wheels, movers, particles...). The same effect maths exists in JS
(`tools/engine.js`, used by the preview) and Kotlin (`FxRenderer.kt`, used by the app).

```
cd tools && node build.js     # needs node + playwright (chromium); rewrites app/src/main/assets/scenes and icons
```

Rules the scenes follow: things stand on what they stand on (ground is painted over their feet, creatures are placed on
roofs/branches by geometry, not by hand), and every scene is looked at full size before it ships.
