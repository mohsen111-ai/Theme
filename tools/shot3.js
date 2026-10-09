const { chromium } = require('/opt/node22/lib/node_modules/playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--no-sandbox'] });
  const p = await b.newPage({ viewport: { width: 420, height: 900 } });
  const errs = []; p.on('pageerror', e => errs.push(e.message)); p.on('console', m => { if (m.type() === 'error') errs.push(m.text()) });
  await p.goto('file://' + __dirname + '/out/wallpapers.html'); await p.waitForTimeout(1500);
  const n = await p.evaluate(() => ({ cards: document.querySelectorAll('.card').length, h2: [...document.querySelectorAll('h2')].map(x => x.textContent), icons: !!document.querySelector('#icons') }));
  await p.screenshot({ path: '/tmp/wp_top.png' }); console.log(JSON.stringify(n), 'errors', errs); await b.close();
})();
