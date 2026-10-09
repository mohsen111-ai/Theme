const { chromium } = require('/opt/node22/lib/node_modules/playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--no-sandbox'] });
  const p = await b.newPage({ viewport: { width: 1100, height: 900 } });
  const errs = []; p.on('pageerror', e => errs.push(e.message)); await p.goto('file://' + __dirname + '/out/preview.html'); await p.waitForTimeout(2500);
  await p.screenshot({ path: process.argv[2] || '/tmp/page.png', clip: { x: 0, y: 0, width: 1100, height: +(process.argv[3] || 2400) }, fullPage: true }); console.log('errors', errs); await b.close();
})();
