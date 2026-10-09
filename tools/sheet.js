// usage: node sheet.js out.png prefix [cols] [T]  -> contact sheet of scenes whose id starts with prefix (or 'all')
const { chromium } = require('/opt/node22/lib/node_modules/playwright');
const fs = require('fs');
(async () => {
  require('child_process').execSync('node ' + __dirname + '/make-preview.js', { stdio: 'ignore' });
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--no-sandbox'] });
  const p = await b.newPage({ viewport: { width: 800, height: 800 } });
  const errs = []; p.on('pageerror', e => errs.push('PAGEERR ' + e.message)); await p.goto('file://' + __dirname + '/out/preview.html'); await p.waitForTimeout(400);
  const [out, prefix, cols = 8, T = 3] = process.argv.slice(2);
  const url = await p.evaluate(({ prefix, cols, T }) => { const list = NYX.SCENES.filter(s => prefix === 'all' || s.id.startsWith(prefix)); const cw = 150, ch = 325, rows = Math.ceil(list.length / cols); const big = document.createElement('canvas'); big.width = cw * cols; big.height = ch * rows; const g = big.getContext('2d');
    list.forEach((sc, i) => { const sk = cw / 360, cv = document.createElement('canvas'); cv.width = cw; cv.height = ch; const c = cv.getContext('2d'); c.scale(sk, sk); const L = NYX.renderLayers(sc, sk); NYX.drawFrame(c, sc, L, +T); g.drawImage(cv, (i % cols) * cw, Math.floor(i / cols) * ch); g.fillStyle = 'rgba(0,0,0,.6)'; g.fillRect((i % cols) * cw, Math.floor(i / cols) * ch, cw, 12); g.fillStyle = '#fff'; g.font = '10px sans-serif'; g.fillText(sc.id + ' ' + sc.theme[0] + sc.style[0] + sc.mode[0], (i % cols) * cw + 2, Math.floor(i / cols) * ch + 10) });
    return big.toDataURL('image/jpeg', .85) }, { prefix, cols: +cols, T });
  fs.writeFileSync(out, Buffer.from(url.split(',')[1], 'base64')); console.log('errors', errs); await b.close();
})();
