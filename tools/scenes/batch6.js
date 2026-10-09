(() => {
const { W, H, TAU, rng, lerp, mixh, ink, blob, hatch, circ, glow, sky, poly, ridge, fillRidge, fridge, ridgeY, hill, pine, fpine, roundTree, ftree, cloud, cottage, fsky, fcloudUnused, path, starPts, defScene } = NYX;
const LN = 'rgba(58,46,84,.72)';
const rot = (pts, a) => { const o = [], c = Math.cos(a), s = Math.sin(a); for (let i = 0; i < pts.length; i += 2)o.push(+(pts[i] * c - pts[i + 1] * s).toFixed(2), +(pts[i] * s + pts[i + 1] * c).toFixed(2)); return o };
const S = (pts, k) => pts.map(v => +(v * k).toFixed(2));
const sunAt = (c, x, y, R, col = '#fff3dc') => { glow(c, x, y, R * 5, 'rgba(255,240,215,.7)', 1); c.fillStyle = col; c.beginPath(); c.arc(x, y, R, 0, TAU); c.fill() };
const lightClouds = (x, y, w, sp) => [x, y, w, sp];

/* ---- blossom hill ---- */
defScene({ id: 'blossomhill', name: 'Blossom Hill', mode: 'still', theme: 'light', style: 'ink', seed: 1601,
  init(r) { return { g: ridge(r, 600, 16, 1.0) } },
  st(c, r, G) { sky(c, [[0, '#c9bde0'], [.5, '#ecc6cc'], [1, '#f4dcc0']]); sunAt(c, 250, 420, 40); cloud(c, r, 20, 220, 150, 'rgba(248,226,232,.85)', LN); cloud(c, r, 200, 300, 130, 'rgba(250,232,226,.85)', LN);
    hill(c, r, ridge(r, 500, 24, 1.3), '#b6a6cf', LN, 'rgba(50,40,86,.7)'); hill(c, r, G.g, '#9aa8c4', LN, 'rgba(40,50,80,.6)');
    cottage(c, r, 290, ridgeY(G.g, 290) + 4, 1.1, '#efe2c4', '#a2705a', LN, '#f6d78a');
    const x = 110, gy = ridgeY(G.g, x) + 6, tr = '#5a4252'; poly(c, [[x - 12, gy], [x - 8, gy - 90], [x - 5, gy - 150], [x + 5, gy - 150], [x + 9, gy - 90], [x + 14, gy]], tr); ink(c, r, [[x - 12, gy], [x - 8, gy - 90], [x - 5, gy - 150]], LN, 1.2, .8, false, 1);
    [[x, gy - 110, x - 70, gy - 170, 5], [x, gy - 130, x + 72, gy - 180, 5], [x, gy - 150, x - 10, gy - 215, 4]].forEach(([a, b, d, e, w]) => ink(c, r, [[a, b], [d, e]], tr, w, .8, false, 1));
    const rr = rng(4); for (let i = 0; i < 6; i++) blob(c, r, circ(x + (rr() - .5) * 130, gy - 190 + (rr() - .5) * 60, 40 + rr() * 22, 10), ['#f4b6c8', '#f0a0b8', '#f8cada'][i % 3], LN, 1, 2);
    for (let i = 0; i < 90; i++) { const px = x - 90 + rr() * 180, py = gy - 230 + rr() * 110; c.fillStyle = ['rgba(250,200,214,.95)', 'rgba(246,176,196,.95)', 'rgba(255,226,234,.95)'][Math.floor(rr() * 3)]; c.beginPath(); c.arc(px, py, 3 + rr() * 5, 0, TAU); c.fill() }
    fillRidge(c, ridge(r, 720, 8, 1.4), '#8a9a7e'); for (let i = 0; i < 40; i++) { const px = r() * W; c.fillStyle = 'rgba(250,214,226,.9)'; c.beginPath(); c.arc(px, 720 + r() * 60, 2 + r() * 2, 0, TAU); c.fill() } } });

/* ---- windmill meadow ---- */
defScene({ id: 'windmillday', name: 'Windmill Meadow', mode: 'live', theme: 'light', style: 'min', seed: 1602,
  init(r) { return { far: ridge(r, 560, 18, 1.2), near: ridge(r, 640, 14, 1.0), fg: ridge(r, 730, 8, 1.4), b: 0 } },
  st(c, r, G) { fsky(c, '#b4c8e0', '#efe3cf'); sunAt(c, 80, 170, 26); G.b = ridgeY(G.near, 240);
    fillRidge(c, G.far, '#a3b8a0'); const x = 240, base = ridgeY(G.near, x); poly(c, [[x - 18, base + 24], [x - 11, base - 104], [x + 11, base - 104], [x + 18, base + 24]], '#ece0c6'); poly(c, [[x - 14, base - 104], [x, base - 128], [x + 14, base - 104]], '#a2705a'); c.fillStyle = '#7a5a4a'; c.fillRect(x - 4, base - 30, 8, 14);
    fillRidge(c, G.near, '#88a884'); fillRidge(c, G.fg, '#6f9272'); c.fillStyle = '#6a5848'; c.beginPath(); c.arc(x, base - 98, 5, 0, TAU); c.fill();
    for (let i = 0; i < 80; i++) { const px = r() * W, py = ridgeY(G.fg, px) + 8 + r() * 48; c.strokeStyle = 'rgba(70,100,70,.5)'; c.lineWidth = 1.3; c.beginPath(); c.moveTo(px, py); c.lineTo(px + (r() - .5) * 5, py - 7 - r() * 7); c.stroke() }
    for (let i = 0; i < 26; i++) { const px = r() * W, py = ridgeY(G.fg, px) + 14 + r() * 40; c.fillStyle = ['#f3d56a', '#f6f0e0', '#e79ab0'][i % 3]; c.beginPath(); c.arc(px, py, 2, 0, TAU); c.fill() } },
  fx(G) { const blades = []; for (let i = 0; i < 4; i++) { const a = i * Math.PI / 2; blades.push(['l', 0, 0, ...rot([70, 0], a), 2.4, '#6a5848'], ['p', rot([14, -2, 68, -2, 68, -15, 14, -8], a), '#efe6d2']) }
    return [{ t: 'clouds', z: 0, c: '#fbf6ec', items: [[10, 150, 150, 6], [220, 230, 120, 4], [90, 330, 170, 8]] }, { t: 'spin', x: 240, y: ridgeY(G.near, 240) - 98, speed: .45, shapes: blades }, { t: 'birds', c: '#5a4a68', items: [[40, 210, 16, 0, 1], [70, 228, 16, 1.3, .8], [110, 214, 16, 2.4, .7]] }] } });

/* ---- balloon morning ---- */
function balloonL(k, a, b) { return [['c', 0, -60 * k, 36 * k, a], ['p', S([-31, -45, 31, -45, 9, -5, -9, -5], k), a], ['e', 0, -62 * k, 12 * k, 34 * k, 0, b], ['l', -8 * k, -6 * k, -6 * k, 1 * k, 1, '#6a5848'], ['l', 8 * k, -6 * k, 6 * k, 1 * k, 1, '#6a5848'], ['r', -7 * k, 1 * k, 14 * k, 9 * k, '#8a6a50']] }
defScene({ id: 'balloonday', name: 'Balloon Morning', mode: 'live', theme: 'light', style: 'min', seed: 1603,
  st(c, r) { fsky(c, '#c6bce0', '#f6dcc6'); sunAt(c, 270, 560, 44); fridge(c, r, 590, 20, 1.2, '#b4a4cc'); fridge(c, r, 650, 16, 1.5, '#9a8cb8'); fridge(c, r, 715, 12, 1.2, '#7c789e'); for (let x = 10; x < W; x += 22 + r() * 18) { const y = 715 + r() * 6; const base = ridge; fpine(c, x, y + 8, 22 + r() * 24, '#5a5c80') } },
  fx() { const mk = (x0, x1, dur, u0, k, a, b) => ({ t: 'mover', path: [x0, 900, x1, -170], dur, off: u0 * dur, bob: [5, .7, x0], tilt: .03, shapes: balloonL(k, a, b) });
    return [{ t: 'clouds', z: 0, c: '#fbf3ea', items: [[0, 200, 150, 5], [190, 320, 130, 4]] }, mk(240, 200, 110, .5, 1.5, '#d97a6a', '#f2c9b0'), mk(110, 160, 150, .36, 1, '#4a9a9a', '#b8dcd4'), mk(300, 270, 190, .74, .62, '#d9a94a', '#f0dca8')] } });

/* ---- cliffs and sails ---- */
function sailboatL(k, hull, sail) { return [['p', S([-22, 0, 24, 0, 16, 8, -14, 8], k), hull], ['l', 0, 0, 0, -52 * k, 1.8 * k, '#6a5848'], ['p', S([2, -50, 2, -6, 24, -6], k), sail], ['p', S([-3, -44, -3, -6, -18, -6], k), '#f2ece0']] }
defScene({ id: 'cliffsails', name: 'Cliffs and Sails', mode: 'live', theme: 'light', style: 'min', seed: 1604,
  st(c, r) { fsky(c, '#a9c4dc', '#e8e2d6'); sunAt(c, 250, 200, 28); const g = c.createLinearGradient(0, 470, 0, H); g.addColorStop(0, '#9cc0cc'); g.addColorStop(1, '#5a8aa0'); c.fillStyle = g; c.fillRect(0, 470, W, H - 470);
    for (let i = 0; i < 50; i++) { c.fillStyle = 'rgba(255,255,255,' + (.1 + r() * .22) + ')'; c.fillRect(r() * W, 480 + r() * 290, 12 + r() * 40, 1.6) }
    poly(c, [[-10, 780], [-10, 380], [50, 360], [110, 400], [150, 520], [130, 640], [160, 780]], '#8a8a98'); poly(c, [[-10, 380], [50, 360], [110, 400], [112, 412], [50, 380], [-10, 396]], '#7fa07a'); poly(c, [[-10, 780], [-10, 600], [60, 640], [80, 780]], '#6e7084');
    poly(c, [[370, 780], [370, 500], [320, 480], [290, 540], [300, 700], [260, 780]], '#8a8a98'); poly(c, [[370, 500], [320, 480], [296, 520], [370, 520]], '#7fa07a') },
  fx() { return [{ t: 'clouds', z: 0, c: '#fbf6ee', items: [[0, 120, 150, 5], [200, 300, 140, 4]] }, { t: 'birds', c: '#4a4a62', items: [[60, 250, 16, 0, 1.1], [95, 270, 16, 1.7, .8], [140, 240, 16, 3, .7]] },
    { t: 'mover', path: [-60, 600, 430, 600], dur: 160, off: 80, bob: [3, 1.1, 0], tilt: .05, shapes: sailboatL(1.5, '#c9604a', '#f6f0e4') }, { t: 'mover', path: [430, 540, -60, 540], dur: 200, off: 40, bob: [2.5, 1.3, 2], tilt: .05, shapes: sailboatL(1, '#3a7a8a', '#f2ece0').map(s => s) },
    { t: 'mover', path: [-60, 500, 430, 500], dur: 260, off: 130, bob: [2, 1.4, 4], tilt: .04, shapes: sailboatL(.6, '#d9a94a', '#f6f0e4') }] } });

/* ---- dandelion wind ---- */
defScene({ id: 'dandelion', name: 'Dandelion Wind', mode: 'live', theme: 'light', style: 'ink', seed: 1605,
  init(r) { return { g: ridge(r, 640, 10, 1.1) } },
  st(c, r, G) { sky(c, [[0, '#bcd0e4'], [.6, '#e8e4d6'], [1, '#f2e2c4']]); sunAt(c, 80, 190, 30); cloud(c, r, 180, 150, 150, 'rgba(250,246,238,.9)', LN); hill(c, r, ridge(r, 540, 22, 1.2), '#aab8a0', LN, 'rgba(40,60,40,.5)'); fillRidge(c, G.g, '#88a878'); hatch(c, r, () => c.rect(0, 640, W, 160), 0, 640, W, 800, -.5, 7, 'rgba(40,70,40,.5)', .7, .35);
    [[70, 330], [200, 390], [290, 300]].forEach(([x, hy], i) => { const gy = ridgeY(G.g, x) + 20; ink(c, r, [[x, gy], [x - 4, (gy + hy) / 2], [x + 3, hy]], '#4a7a4a', 2.4, 1, false, 1); for (let k = 0; k < 2; k++) blob(c, r, [[x, gy - 20 - k * 20], [x - 22 - k * 6, gy - 6 - k * 20], [x - 6, gy - 22 - k * 20]], '#6a9a5a', LN, 1, .6);
      for (let k = 0; k < 34; k++) { const a = k / 34 * TAU; ink(c, r, [[x + 3, hy], [x + 3 + Math.cos(a) * 22, hy + Math.sin(a) * 22]], 'rgba(255,255,255,.9)', 1, .3, false, 1); c.fillStyle = 'rgba(255,255,255,.95)'; c.beginPath(); c.arc(x + 3 + Math.cos(a) * 23, hy + Math.sin(a) * 23, 2.2, 0, TAU); c.fill() } c.fillStyle = '#d9b84a'; c.beginPath(); c.arc(x + 3, hy, 4, 0, TAU); c.fill() }) },
  fx() { const rr = rng(5), pts = Array.from({ length: 50 }, () => [rr() * W, rr() * H, 1.6 + rr() * 1.8, 8 + rr() * 16, 0, rr() * TAU]); return [{ t: 'fall', m: 'petal', c: 'rgba(255,255,255,.95)', pts }, { t: 'clouds', z: 0, c: '#fbf6ee', items: [[10, 100, 130, 4], [230, 260, 110, 3]] }] } });

/* ---- cloud sea ---- */
defScene({ id: 'cloudsea', name: 'Cloud Sea', mode: 'still', theme: 'light', style: 'min', seed: 1606,
  st(c, r) { fsky(c, '#bfb4dc', '#f6d6c0'); sunAt(c, 220, 400, 40); const peak = (x, y, w, h, col, hi) => { poly(c, [[x - w, y + h], [x, y], [x + w, y + h]], col); poly(c, [[x, y], [x + w, y + h], [x + w * .1, y + h]], hi) };
    peak(90, 330, 150, 300, '#8a82b0', '#767099'); peak(280, 420, 120, 220, '#9a92bc', '#8680a8'); peak(190, 480, 110, 150, '#a89ec6', '#968cb6');
    const row = (y, col, n, rad) => { c.fillStyle = col; for (let i = 0; i < n; i++) { const x = i * (W / (n - 1)) + (r() - .5) * 20, rr = rad * (.7 + r() * .6); c.beginPath(); c.arc(x, y + (r() - .5) * 10, rr, 0, TAU); c.fill() } c.fillRect(0, y, W, H - y) };
    row(560, '#ece0e4', 9, 46); row(620, '#e0d4dc', 8, 52); row(690, '#d0c4d2', 8, 58); row(750, '#bcb0c6', 7, 64) } });

/* ---- lavender rows ---- */
defScene({ id: 'lavender', name: 'Lavender Rows', mode: 'still', theme: 'light', style: 'min', seed: 1607,
  st(c, r) { fsky(c, '#b8aed8', '#f2d4c8'); sunAt(c, 240, 470, 36); fillRidge(c, ridge(r, 470, 16, 1.4), '#a79ac4'); const vx = 180, vy = 500;
    poly(c, [[-10, vy], [370, vy], [370, H + 10], [-10, H + 10]], '#cdb8a0');
    for (let i = -7; i < 7; i++) { const x0 = vx + i * 70 - 18, x1 = vx + i * 70 + 18, hx0 = vx + i * 5 - 1.2, hx1 = vx + i * 5 + 1.2; poly(c, [[hx0, vy], [hx1, vy], [x1, H + 10], [x0, H + 10]], i % 2 ? '#8a68b0' : '#9a7cc0') }
    for (let i = 0; i < 700; i++) { const rowi = Math.floor(r() * 14) - 7, t = Math.pow(r(), 1.8), y = vy + t * (H - vy), cx = vx + rowi * (5 + 65 * t) + (r() - .5) * (30 * t + 2); c.strokeStyle = r() < .5 ? 'rgba(90,60,130,.45)' : 'rgba(190,160,230,.5)'; c.lineWidth = .8 + t * 2; c.beginPath(); c.moveTo(cx, y); c.lineTo(cx + (r() - .5) * 3, y - 3 - t * 12); c.stroke() }
    poly(c, [[168, vy + 2], [168, vy - 10], [176, vy - 16], [184, vy - 10], [192, vy - 10], [192, vy + 2]], '#efe2c4'); poly(c, [[165, vy - 10], [176, vy - 18], [188, vy - 10]], '#a2705a'); [[150, 30], [208, 24]].forEach(([x, h]) => { c.fillStyle = '#4a6a58'; c.beginPath(); c.ellipse(x, vy - h / 2 + 2, 4, h / 2, 0, 0, TAU); c.fill() }) } });

/* ---- long: golden village through a bright day ---- */
const LOOP = 70;
const day = (t, b) => c => { const g = c.createLinearGradient(0, 0, 0, H); g.addColorStop(0, t); g.addColorStop(1, b); c.fillStyle = g; c.fillRect(0, 0, W, H) };
defScene({ id: 'goldenvillage', name: 'Golden Village', mode: 'long', theme: 'light', style: 'ink', seed: 1608, loop: LOOP, poster: .4,
  init(r) { return { far: ridge(r, 520, 24, 1.1), near: ridge(r, 620, 16, 1.3), houses: [[100, 0], [190, 0], [280, 0]] } },
  skyFrames: [{ p: 0, draw: day('#c4d6e6', '#f1e3cf') }, { p: .3, draw: day('#9fc0da', '#e9e8dc') }, { p: .6, draw: day('#a9a6c4', '#f0c590') }, { p: .82, draw: day('#8a78a6', '#dc9a8c') }],
  st(c, r, G) { hill(c, r, G.far, '#b4b0cc', LN, 'rgba(50,40,86,.55)'); hill(c, r, G.near, '#8fa88e', LN, 'rgba(40,60,44,.55)');
    G.houses.forEach(([x]) => cottage(c, r, x, ridgeY(G.near, x) + 8, 1.1, '#efe2c4', '#a2705a', LN, null)); for (let i = 0; i < 8; i++) roundTree(c, r, 20 + i * 11 + r() * 8, ridgeY(G.near, 30) + 6, 50 + r() * 30, '#6f9272', LN, '#4a3a48') },
  fx(G) { const sun = { t: 'orb', z: 0, r: 30, c: '#fff6dc', glow: 'rgba(255,236,190,.7)', hi: '#fffdf2', warm: '#ffb070', x0: -40, x1: 400, y0: 560, yAmp: 420, a: .02, b: .9 };
    const wins = G.houses.map(([x]) => [x, ridgeY(G.near, x) - 22, 24, x, .2]), smoke = G.houses.map(([x], i) => ({ t: 'smoke', c: '#9a8fae', x: x + 22, y: ridgeY(G.near, x) - 56, n: 6, drift: 18, rise: 70, r0: 3, grow: 9, speed: .08 + i * .01, a: .3 }));
    return [sun, { t: 'clouds', z: 0, c: '#fbf6ee', vis: [[0, 1], [.7, 1], [.9, .3], [1, 1]], items: [[10, 150, 150, 6], [210, 240, 130, 4], [80, 340, 170, 8]] }, { t: 'birds', c: '#4a4062', vis: [[0, 1], [.5, 1], [.7, 0], [1, 1]], items: [[40, 200, 16, 0, 1], [75, 218, 16, 1.3, .8], [110, 204, 16, 2.4, .7]] },
      { t: 'glows', c: 'rgba(255,200,120,.7)', vis: [[0, 0], [.55, 0], [.75, 1], [.95, 1], [1, 0]], pts: wins }, ...smoke] } });
})();
