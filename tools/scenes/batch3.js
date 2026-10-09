(() => {
const { W, H, TAU, rng, lerp, mixh, ink, blob, hatch, circ, glow, sky, poly, ridge, fillRidge, fridge, ridgeY, fpine, ftree, fsky, fmoon, fstars, starPts, defScene } = NYX;
const away = (x, y, r) => (px, py) => Math.hypot(px - x, py - y) < r;
const S = (pts, k) => pts.map(v => +(v * k).toFixed(2));
const dome = (r, n = 12) => { const o = []; for (let i = 0; i <= n; i++) { const a = Math.PI + i / n * Math.PI; o.push(+(Math.cos(a) * r).toFixed(2), +(Math.sin(a) * r * .8).toFixed(2)) } return o };

/* ---- sky whale ---- */
function whale(k, col, dots) { const body = [64, 0, 56, -10, 40, -20, 14, -26, -14, -22, -40, -12, -60, -6, -70, -5, -84, -9, -98, -22, -100, -14, -90, -1, -100, 12, -98, 20, -84, 8, -70, 5, -48, 11, -20, 18, 10, 20, 34, 14, 52, 8, 62, 3];
  const sh = [['p', S(body, k), col], ['p', S([6, 16, 26, 36, 12, 20], k), col], ['c', 46 * k, -6 * k, 1.6 * k, 'rgba(220,235,255,.8)']];
  for (let i = 0; i < dots; i++) { const x = -34 + i * (70 / Math.max(1, dots - 1)); sh.push(['g', x * k, (14 - Math.abs(x) * .04) * k, 9 * k, 'rgba(140,200,255,.7)', .9]) } return sh }
defScene({ id: 'skywhale', name: 'Sky Whale', mode: 'live', theme: 'dark', style: 'min', seed: 1301, poster: 17,
  st(c, r) { fsky(c, '#070a1e', '#1d2a55'); fmoon(c, 280, 130, 34); fridge(c, r, 650, 16, 3.2, '#19224b'); fridge(c, r, 700, 14, 4.1, '#0e1537'); fridge(c, r, 750, 10, 5, '#070b22') },
  fx() { return [{ t: 'stars', c: '#eaf0ff', pts: starPts(31, 130, 560, away(280, 130, 50)) },
    { t: 'mover', path: [-160, 360, 520, 330], dur: 150, off: 60, bob: [9, .5, 0], tilt: .035, shapes: whale(1.5, '#05071a', 7) },
    { t: 'mover', path: [-100, 230, 460, 215], dur: 230, off: 150, bob: [5, .4, 2], tilt: .03, shapes: whale(.55, '#0a0f2a', 4) },
    { t: 'mover', path: [480, 480, -140, 500], dur: 190, off: 20, bob: [6, .45, 4], tilt: .03, shapes: whale(.8, '#080c24', 5).map(s => s[0] === 'p' && s[1].length > 20 ? ['p', s[1].map((v, i) => i % 2 ? v : -v), s[2]] : s) }] } });

/* ---- deep sea jellyfish ---- */
function jelly(k, col, glowc) { const sh = [['p', [...S(dome(26), k), ...S([20, 4, 0, 9, -20, 4], k)], col], ['g', 0, -8 * k, 60 * k, glowc, .55]]; for (let i = -3; i <= 3; i++) sh.push(['l', i * 7 * k, 6 * k, (i * 8 + (i % 2 ? 6 : -6)) * k, (44 + Math.abs(i) * 4) * k, 1.6 * k, col]); return sh }
defScene({ id: 'jellies', name: 'Deep Sea Lights', mode: 'live', theme: 'dark', style: 'min', seed: 1302,
  st(c, r) { fsky(c, '#041424', '#0a3a48'); c.save(); c.globalCompositeOperation = 'screen'; [[40, 0, 120, 360], [140, 0, 220, 380], [250, 0, 330, 340]].forEach(([x0, , x1, ln]) => { const g = c.createLinearGradient(0, 0, 0, ln); g.addColorStop(0, 'rgba(120,220,255,.22)'); g.addColorStop(1, 'rgba(120,220,255,0)'); c.fillStyle = g; c.beginPath(); c.moveTo(x0 - 20, 0); c.lineTo(x0 + 24, 0); c.lineTo(x1 + 60, ln); c.lineTo(x1 - 60, ln); c.closePath(); c.fill() }); c.restore();
    fridge(c, r, 690, 20, 2.2, '#062332'); const g = fridge(c, r, 735, 12, 3, '#03121c');
    for (let i = 0; i < 16; i++) { const x = 10 + i * 23 + r() * 10, y = ridgeY(g, x) + 4, h = 40 + r() * 70; c.strokeStyle = '#04202c'; c.lineWidth = 3 + r() * 2; c.lineCap = 'round'; c.beginPath(); c.moveTo(x, y); c.quadraticCurveTo(x + (r() - .5) * 26, y - h * .5, x + (r() - .5) * 30, y - h); c.stroke() } },
  fx() { const rr = rng(77), bubbles = Array.from({ length: 34 }, () => [rr() * W, rr() * H, 1 + rr() * 2.4, 10 + rr() * 22, 0, rr() * TAU]);
    return [{ t: 'fall', m: 'rise', c: 'rgba(200,240,255,.55)', g: 'rgba(120,200,255,.25)', pts: bubbles },
      { t: 'mover', path: [90, 330, 90, 330], dur: 60, bob: [14, .55, 0], tilt: .05, shapes: jelly(1.6, 'rgba(255,150,205,.85)', 'rgba(255,120,190,.55)') },
      { t: 'mover', path: [250, 470, 250, 470], dur: 60, bob: [11, .7, 2], tilt: .06, shapes: jelly(1.05, 'rgba(120,225,255,.85)', 'rgba(80,200,255,.55)') },
      { t: 'mover', path: [300, 230, 300, 230], dur: 60, bob: [9, .8, 4], tilt: .05, shapes: jelly(.7, 'rgba(190,160,255,.85)', 'rgba(160,120,255,.5)') }] } });

/* ---- sky lanterns over a river ---- */
defScene({ id: 'skylanterns', name: 'Sky Lanterns', mode: 'live', theme: 'dark', style: 'min', seed: 1303,
  st(c, r) { fsky(c, '#070a1e', '#202a58'); fmoon(c, 90, 140, 32); const far = fridge(c, r, 600, 14, 1.4, '#0e1636'); c.fillStyle = '#0a1230'; c.fillRect(0, 640, W, H - 640);
    for (let i = 0; i < 40; i++) { const y = 650 + i * 3.4 + r() * 2; c.fillStyle = 'rgba(255,190,110,' + (.04 + r() * .1) + ')'; c.fillRect(r() * W, y, 10 + r() * 40, 1.3) }
    for (let x = 6; x < W; x += 12 + r() * 14) { const y = ridgeY(far, x) + 2; poly(c, [[x, y], [x, y - 8 - r() * 8], [x + 6, y - 14 - r() * 8], [x + 12, y - 8 - r() * 8], [x + 12, y]], '#05081a') } },
  fx() { const rr = rng(33), pts = Array.from({ length: 34 }, () => { const near = rr(); return [rr() * W, rr() * H, 2 + near * 3.2, 7 + near * 14, 0, rr() * TAU] });
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(32, 100, 420, away(90, 140, 44)) }, { t: 'fall', m: 'rise', c: '#ffcf80', g: 'rgba(255,160,60,.6)', pts }] } });

/* ---- rainy bus stop ---- */
defScene({ id: 'busstop', name: 'Rainy Bus Stop', mode: 'live', theme: 'dark', style: 'min', seed: 1304,
  st(c, r) { fsky(c, '#09101f', '#1b2a3c'); const wins = [];
    [[0, 90, 380], [90, 70, 340], [160, 80, 400], [240, 70, 360], [310, 70, 330]].forEach(([x, w, top]) => { poly(c, [[x, 640], [x, top], [x + w, top], [x + w, 640]], '#0b1322'); for (let y = top + 14; y < 600; y += 28) for (let xx = x + 10; xx < x + w - 10; xx += 20) if (r() < .32) { c.fillStyle = '#ffcf80'; c.fillRect(xx, y, 8, 12); wins.push([xx + 4, y + 6]) } });
    // pavement, kerb, wet road
    poly(c, [[-10, 640], [370, 640], [370, 700], [-10, 700]], '#162234'); poly(c, [[-10, 700], [370, 700], [370, H + 10], [-10, H + 10]], '#0a111d'); c.fillStyle = '#26354a'; c.fillRect(0, 698, W, 3);
    // bus shelter on the pavement: posts, roof, back panel
    const x0 = 120, x1 = 300; poly(c, [[x0, 640], [x0, 560], [x0 + 3, 560], [x0 + 3, 640]], '#05080f'); poly(c, [[x1 - 3, 640], [x1 - 3, 560], [x1, 560], [x1, 640]], '#05080f'); poly(c, [[x0 - 8, 560], [x1 + 8, 560], [x1 + 8, 552], [x0 - 8, 552]], '#05080f');
    poly(c, [[x0 + 12, 640], [x0 + 12, 574], [x1 - 12, 574], [x1 - 12, 640]], '#1a3a5a'); c.fillStyle = '#d8ecff'; c.fillRect(x0 + 18, 580, 60, 46); glow(c, x0 + 48, 603, 70, 'rgba(180,220,255,.5)', .9); poly(c, [[x0 + 90, 640], [x0 + 90, 624], [x1 - 20, 624], [x1 - 20, 640]], '#05080f');
    // street lamp on the pavement
    c.fillStyle = '#05080f'; c.fillRect(54, 470, 4, 172); poly(c, [[56, 470], [56, 462], [92, 462], [92, 468]], '#05080f'); c.fillStyle = '#ffe2a0'; c.fillRect(82, 468, 14, 5); glow(c, 89, 474, 110, 'rgba(255,215,140,.55)', 1);
    c.save(); c.globalCompositeOperation = 'screen'; const g = c.createLinearGradient(0, 474, 0, 700); g.addColorStop(0, 'rgba(255,215,140,.22)'); g.addColorStop(1, 'rgba(255,215,140,0)'); c.fillStyle = g; c.beginPath(); c.moveTo(80, 474); c.lineTo(98, 474); c.lineTo(170, 700); c.lineTo(20, 700); c.closePath(); c.fill(); c.restore();
    for (let i = 0; i < 14; i++) { c.fillStyle = 'rgba(255,215,150,' + (.04 + r() * .1) + ')'; c.fillRect(10 + r() * 330, 704 + r() * 70, 20 + r() * 50, 1.6) } },
  fx() { const rr = rng(61), pts = Array.from({ length: 160 }, () => [rr() * (W + 80) - 40, rr() * H, 10 + rr() * 16, 380 + rr() * 300, 0, 0]);
    return [{ t: 'fall', m: 'rain', c: 'rgba(185,205,235,.30)', k: .18, pts }, { t: 'glows', c: 'rgba(255,200,120,.5)', pts: [[89, 474, 60, 0, .08]] }] } });

/* ---- blossom tree at night ---- */
defScene({ id: 'blossomnight', name: 'Night Blossom', mode: 'live', theme: 'dark', style: 'min', seed: 1305,
  init(r) { return { g: ridge(r, 650, 14, 1.2) } },
  st(c, r, G) { fsky(c, '#0a0d26', '#222a58'); fmoon(c, 250, 220, 78); const x = 130, gy = ridgeY(G.g, x);
    // trunk + branches first (ground is painted over their feet)
    const tr = '#120c1c'; poly(c, [[x - 12, gy + 14], [x - 7, gy - 90], [x - 4, gy - 150], [x + 5, gy - 150], [x + 8, gy - 90], [x + 14, gy + 14]], tr); c.strokeStyle = tr; c.lineCap = 'round';
    [[x, gy - 110, x - 90, gy - 190, 6], [x, gy - 130, x + 86, gy - 205, 6], [x, gy - 150, x - 30, gy - 250, 5], [x - 50, gy - 160, x - 120, gy - 215, 3.5], [x + 50, gy - 175, x + 120, gy - 250, 3.5], [x - 10, gy - 200, x + 30, gy - 285, 3]].forEach(([a, b, d, e, w]) => { c.lineWidth = w; c.beginPath(); c.moveTo(a, b); c.quadraticCurveTo((a + d) / 2 + 6, (b + e) / 2 - 8, d, e); c.stroke() });
    const rr = rng(9); for (let i = 0; i < 300; i++) { const a = rr() * TAU, d = Math.pow(rr(), .62), px = x + Math.cos(a) * d * 150, py = gy - 220 + Math.sin(a) * d * 92; c.fillStyle = ['rgba(255,196,218,.9)', 'rgba(250,170,200,.9)', 'rgba(255,220,232,.9)'][Math.floor(rr() * 3)]; c.beginPath(); c.arc(px, py, 3 + rr() * 5, 0, TAU); c.fill() }
    fillRidge(c, G.g, '#0b1030'); fillRidge(c, ridge(r, 720, 10, 1.3), '#050818'); for (let i = 0; i < 70; i++) { const gx = r() * W; ink(c, r, [[gx, ridgeY(G.g, gx) + 6 + r() * 60], [gx + (r() - .5) * 6, ridgeY(G.g, gx) - 2 + r() * 60]], '#0b1030', 1, .5, false, 1) } },
  fx(G) { const rr = rng(14), pts = Array.from({ length: 36 }, () => [rr() * W, rr() * H, 2 + rr() * 2.4, 14 + rr() * 26, 0, rr() * TAU]);
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(33, 110, 480, away(250, 220, 90)) }, { t: 'fall', m: 'petal', c: 'rgba(255,196,218,.9)', pts }] } });

/* ---- comet over a hill village ---- */
defScene({ id: 'comet', name: 'Slow Comet', mode: 'live', theme: 'dark', style: 'min', seed: 1306, poster: 15,
  init(r) { const g = ridge(r, 640, 22, 1.1); const homes = []; for (let x = 20; x < 340; x += 26 + r() * 14) homes.push({ x, w: 16 + r() * 8, h: 12 + r() * 8, lit: r() < .6 }); return { g, homes } },
  st(c, r, G) { fsky(c, '#060a1c', '#1a2450'); fillRidge(c, ridge(r, 600, 12, 1.4), '#0b1330');
    G.homes.forEach(h => { const y = ridgeY(G.g, h.x + h.w / 2) + 3; poly(c, [[h.x, y], [h.x, y - h.h], [h.x + h.w / 2, y - h.h - 8], [h.x + h.w, y - h.h], [h.x + h.w, y]], '#04060f'); if (h.lit) { c.fillStyle = '#ffcf80'; c.fillRect(h.x + h.w / 2 - 2, y - h.h / 2 - 1, 4, 5) } }); fillRidge(c, G.g, '#080d24');
    for (let x = 6; x < W; x += 16 + r() * 16) fpine(c, x, ridgeY(G.g, x) + 14, 24 + r() * 22, '#03050f') },
  fx(G) { const ang = Math.atan2(-250, 670), rot = (pts) => { const o = [], c = Math.cos(ang), s = Math.sin(ang); for (let i = 0; i < pts.length; i += 2)o.push(+(pts[i] * c - pts[i + 1] * s).toFixed(2), +(pts[i] * s + pts[i + 1] * c).toFixed(2)); return o };
    const wins = G.homes.filter(h => h.lit).map(h => [h.x + h.w / 2, ridgeY(G.g, h.x + h.w / 2) - h.h / 2, 16, h.x, .3]);
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(34, 150, 560) }, { t: 'mover', path: [430, 80, -240, 330], dur: 90, off: 30, shapes: [['p', rot([0, 0, 260, -26, 260, 26]), 'rgba(160,210,255,.13)'], ['p', rot([0, 0, 200, -14, 200, 14]), 'rgba(190,225,255,.17)'], ['p', rot([0, 0, 130, -7, 130, 7]), 'rgba(220,240,255,.22)'], ['g', 0, 0, 34, 'rgba(210,235,255,.9)', 1], ['c', 0, 0, 4.5, '#ffffff']] }, { t: 'glows', c: 'rgba(255,190,100,.6)', pts: wins }] } });
})();
