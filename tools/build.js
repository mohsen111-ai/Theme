// Content pipeline: renders every scene to webp + fx json for the Android app.
//   node build.js            -> app/src/main/assets/scenes/
const { chromium } = require('/opt/node22/lib/node_modules/playwright');
const fs = require('fs'), path = require('path');
const root = path.resolve(__dirname, '..'), outDir = path.join(root, 'app/src/main/assets/scenes');
(async () => {
  require('child_process').execSync('node ' + path.join(__dirname, 'make-preview.js'), { stdio: 'inherit' });
  fs.rmSync(outDir, { recursive: true, force: true }); for (const d of ['bg', 'thumb']) fs.mkdirSync(path.join(outDir, d), { recursive: true });
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--no-sandbox'] });
  const p = await b.newPage({ viewport: { width: 800, height: 800 } });
  const errs = []; p.on('pageerror', e => errs.push(e.message)); await p.goto('file://' + path.join(__dirname, 'out/preview.html')); await p.waitForTimeout(300);
  const ids = await p.evaluate(() => NYX.SCENES.map(s => s.id)); const index = []; let bytes = 0;
  const save = (rel, url) => { const buf = Buffer.from(url.split(',')[1], 'base64'); fs.writeFileSync(path.join(outDir, rel), buf); bytes += buf.length; return buf.length };
  for (const id of ids) {
    const r = await p.evaluate(({ id }) => { const sc = NYX.SCENES.find(s => s.id === id), L = NYX.renderLayers(sc, 3), q = .8, enc = cv => cv.toDataURL('image/webp', q);
      const poster = sc.mode === 'long' ? sc.loop * (sc.poster == null ? .4 : sc.poster) : (sc.poster == null ? 2.5 : sc.poster), th = document.createElement('canvas'); th.width = 270; th.height = 585; const c = th.getContext('2d'); c.scale(.75, .75); NYX.drawFrame(c, sc, L, poster);
      return { meta: { id: sc.id, name: sc.name, mode: sc.mode, theme: sc.theme, style: sc.style, loop: sc.loop, poster }, bg: enc(L.bg), sky: L.sky.map(s => ({ p: s.p, url: enc(s.cv) })), thumb: th.toDataURL('image/webp', .75), fx: NYX.sceneFx(sc) } }, { id });
    const m = r.meta, n = save('bg/' + id + '.webp', r.bg); m.bg = 'bg/' + id + '.webp'; m.sky = r.sky.map((s, i) => { save('bg/' + id + '_s' + i + '.webp', s.url); return { p: s.p, img: 'bg/' + id + '_s' + i + '.webp' } }); m.thumb = 'thumb/' + id + '.webp'; save(m.thumb, r.thumb); m.fx = r.fx; index.push(m);
    console.log(id.padEnd(16), m.mode.padEnd(5), (n / 1024).toFixed(0) + 'KB', 'fx:' + r.fx.length);
  }
  // icon packs + app icon
  const icons = await p.evaluate(() => NYXICONS.exportAll()), assets = path.join(root, 'app/src/main/assets'), res = path.join(root, 'app/src/main/res');
  fs.rmSync(path.join(assets, 'icons'), { recursive: true, force: true }); let ib = 0;
  for (const [rel, url] of Object.entries(icons)) { const buf = Buffer.from(url.split(',')[1], 'base64'); ib += buf.length;
    if (rel.startsWith('appicon/')) { const dst = path.join(res, 'drawable-nodpi', { 'appicon/bg.png': 'ic_launcher_background.png', 'appicon/fg.png': 'ic_launcher_foreground.png', 'appicon/mono.png': 'ic_launcher_monochrome.png' }[rel]); fs.mkdirSync(path.dirname(dst), { recursive: true }); fs.writeFileSync(dst, buf) }
    else { const dst = path.join(assets, rel); fs.mkdirSync(path.dirname(dst), { recursive: true }); fs.writeFileSync(dst, buf) } }
  console.log('icons', Object.keys(icons).length, (ib / 1048576).toFixed(1) + 'MB');
  // wallpaper picker thumbnail = first scene's poster
  fs.mkdirSync(path.join(res, 'drawable-nodpi'), { recursive: true }); fs.copyFileSync(path.join(outDir, index[0].thumb), path.join(res, 'drawable-nodpi', 'wallpaper_thumb.webp'));
  fs.writeFileSync(path.join(outDir, 'index.json'), JSON.stringify(index)); console.log('scenes', index.length, 'total', (bytes / 1048576).toFixed(1) + 'MB', 'errors', errs); await b.close();
})();
