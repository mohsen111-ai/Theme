// usage: node shot.js out.png [ids...]  -> screenshots modal canvases for scenes (t in seconds via env T)
const { chromium } = require('/opt/node22/lib/node_modules/playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--no-sandbox'] });
  const p = await b.newPage({ viewport: { width: 1000, height: 1000 }, deviceScaleFactor: 1 });
  const errs = []; p.on('pageerror', e => errs.push('PAGEERR ' + e.message)); p.on('console', m => { if (m.type() === 'error') errs.push(m.text()) });
  await p.goto('file://' + __dirname + '/out/preview.html'); await p.waitForTimeout(500);
  const ids = process.argv.slice(3); const T = +(process.env.T || 3);
  const shots = [];
  for (const id of ids) {
    const url = await p.evaluate(({ id, T }) => { const sc = NYX.SCENES.find(s => s.id === id); if (!sc) return null; const cv = document.createElement('canvas'), sc2 = 1.4; cv.width = Math.round(360 * sc2); cv.height = Math.round(780 * sc2); const c = cv.getContext('2d'); c.scale(sc2, sc2); const L = NYX.renderLayers(sc, sc2); NYX.drawFrame(c, sc, L, T); return cv.toDataURL('image/png') }, { id, T });
    if (!url) { console.log('missing', id); continue } shots.push([id, url]);
  }
  require('fs').writeFileSync('/tmp/shots.json', JSON.stringify(shots)); console.log('errors', errs); await b.close();
})();
