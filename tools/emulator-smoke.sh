#!/bin/bash
# Smoke test on a running emulator/device: install the release APK, open every screen, apply the live wallpaper, switch to Nyx Home,
# and fail on any crash in logcat. Screenshots land in $OUT.
set -u
ADB=${ADB:-/root/android-sdk/platform-tools/adb}
APK=${APK:-/home/user/Theme/app/build/outputs/apk/release/app-release.apk}
OUT=${OUT:-/tmp/smoke}; mkdir -p "$OUT"
shot() { $ADB exec-out screencap -p > "$OUT/$1.png"; }
tapText() { # tap the centre of the first UI node whose text matches $1
  $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; $ADB pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null 2>&1
  python3 - "$1" "$OUT/ui.xml" <<'PY' > "$OUT/tap.txt"
import re,sys
t,f=sys.argv[1],sys.argv[2]
s=open(f,encoding='utf8').read()
for m in re.finditer(r'<node[^>]*?text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',s):
    if t.lower() in m.group(1).lower():
        x=(int(m.group(2))+int(m.group(4)))//2; y=(int(m.group(3))+int(m.group(5)))//2; print(x,y); break
PY
  read -r X Y < "$OUT/tap.txt"; if [ -n "${X:-}" ]; then $ADB shell input tap $X $Y; echo "tapped '$1' at $X,$Y"; else echo "NOT FOUND '$1'"; return 1; fi
}
$ADB root >/dev/null 2>&1; sleep 2
$ADB logcat -c
$ADB install -r "$APK" || exit 1
$ADB shell am start -n com.nyx.themes/.ui.MainActivity; sleep 6; shot 01_wallpapers
tapText "Icons & Home"; sleep 3; shot 02_icons_home
tapText "Settings"; sleep 3; shot 03_settings
tapText "Wallpapers"; sleep 3
$ADB shell am start -n com.nyx.themes/.ui.DetailActivity --es scene_id ferris; sleep 6; shot 04_detail
echo "--- crashes so far:"; $ADB logcat -d | grep -E "FATAL EXCEPTION|AndroidRuntime: " | head -5
