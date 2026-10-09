(() => {
const { W, H, TAU, rng, lerp, mixh, ink, blob, hatch, circ, glow, poly, ridge, fillRidge, fridge, ridgeY, fpine, ftree, fsky, fmoon, fstars, starPts, defScene } = NYX;
const rot = (pts, a) => { const o = [], c = Math.cos(a), s = Math.sin(a); for (let i = 0; i < pts.length; i += 2)o.push(+(pts[i] * c - pts[i + 1] * s).toFixed(2), +(pts[i] * s + pts[i + 1] * c).toFixed(2)); return o };
const sc = (pts, k) => pts.map(v => +(v * k).toFixed(2));
const awayFrom = (x, y, r) => (px, py) => Math.hypot(px - x, py - y) < r;

/* hot-air balloon (origin = top of the basket) */
function balloon(k, body, panel) { return [
  ['c', 0, -60 * k, 36 * k, body], ['p', sc([-31, -45, 31, -45, 9, -5, -9, -5], k), body], ['e', 0, -62 * k, 11 * k, 33 * k, 0, panel],
  ['l', -8 * k, -6 * k, -6 * k, 1 * k, 1, body], ['l', 8 * k, -6 * k, 6 * k, 1 * k, 1, body], ['r', -7 * k, 1 * k, 14 * k, 9 * k, body],
  ['g', 0, -9 * k, 36 * k, 'rgba(255,170,70,.85)', .9]] }

defScene({ id: 'balloons', name: 'Drifting Balloons', mode: 'live', theme: 'dark', style: 'min', seed: 1101,
  st(c, r) { fsky(c, '#070c1e', '#1b2347'); fmoon(c, 82, 170, 40); const a = ridge(r, 650, 22, 1.1), b = ridge(r, 715, 16, 1.5), d = ridge(r, 765, 10, 1.2);
    fillRidge(c, a, '#0e1833'); fillRidge(c, b, '#091128'); fillRidge(c, d, '#04081a');
    for (let x = 14; x < W; x += 24 + r() * 18) { const y = ridgeY(b, x) + 3; fpine(c, x, y, 26 + r() * 30, '#060b1c') } },
  fx() { const mk = (x0, x1, dur, u0, k, panel) => ({ t: 'mover', path: [x0, 900, x1, -170], dur, off: u0 * dur, bob: [5, .7, x0], tilt: .03, shapes: balloon(k, '#05070f', panel) });
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(11, 120, 560, awayFrom(82, 170, 60)) },
      mk(250, 210, 110, .46, 1.5, '#12204a'), mk(120, 170, 150, .33, 1, '#17284f'), mk(300, 280, 190, .72, .62, '#1a2a52')] } });

defScene({ id: 'windmill', name: 'Windmill Hill', mode: 'live', theme: 'dark', style: 'min', seed: 1102,
  init(r) { const far = ridge(r, 600, 12, 1.4), near = ridge(r, 650, 16, 1.1), fg = ridge(r, 735, 10, 1.3); return { far, near, fg, b1: ridgeY(near, 120), b2: ridgeY(far, 292) - 4 } },
  st(c, r, G) { fsky(c, '#070c1e', '#1a2346'); fmoon(c, 252, 210, 70);
    const mill = (x, base, k) => { poly(c, [[x - 16 * k, base + 24], [x - 10 * k, base - 96 * k], [x + 10 * k, base - 96 * k], [x + 16 * k, base + 24]], '#05070f'); poly(c, [[x - 13 * k, base - 96 * k], [x, base - 118 * k], [x + 13 * k, base - 96 * k]], '#05070f'); c.fillStyle = '#ffcf80'; c.fillRect(x - 3 * k, base - 52 * k, 6 * k, 9 * k) };
    mill(292, G.b2, .55); fillRidge(c, G.far, '#0b142d'); mill(120, G.b1, 1); fillRidge(c, G.near, '#08102a'); fillRidge(c, G.fg, '#04081a');
    c.fillStyle = '#05070f'; c.beginPath(); c.arc(120, G.b1 - 96, 5, 0, TAU); c.fill(); glow(c, 120, G.b1 - 47, 26, 'rgba(255,190,100,.5)', .8);
    for (let x = 20; x < W; x += 20 + r() * 20) if (Math.abs(x - 120) > 30) fpine(c, x, ridgeY(G.fg, x) + 3, 20 + r() * 22, '#02050f') },
  fx(G) { const blades = k => { const sh = []; for (let i = 0; i < 4; i++) { const a = i * Math.PI / 2; sh.push(['l', 0, 0, ...rot([68 * k, 0], a), 2.2 * k, '#05070f'], ['p', rot([14 * k, -2 * k, 66 * k, -2 * k, 66 * k, -14 * k, 14 * k, -8 * k], a), '#0a1022']) } return sh };
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(12, 130, 540, awayFrom(252, 210, 80)) }, { t: 'spin', x: 120, y: G.b1 - 96, speed: .38, shapes: blades(1) }, { t: 'spin', x: 292, y: G.b2 - 96 * .55, speed: .3, phase: 1, shapes: blades(.55) }] } });

defScene({ id: 'observatory', name: 'Observatory', mode: 'still', theme: 'dark', style: 'min', seed: 1103,
  st(c, r) { fsky(c, '#060a1c', '#16203f'); const px = 250, py = 130; fstars(c, r, 150, 560, .7);
    for (let k = 0; k < 70; k++) { const rad = 14 + r() * 560, a0 = r() * TAU, span = .25 + r() * .5; c.strokeStyle = 'rgba(210,222,255,' + (.1 + r() * .34) + ')'; c.lineWidth = .5 + r() * .8; c.beginPath(); c.arc(px, py, rad, a0, a0 + span); c.stroke() }
    c.fillStyle = '#f4f7ff'; c.beginPath(); c.arc(px, py, 2.4, 0, TAU); c.fill(); glow(c, px, py, 22, 'rgba(210,225,255,.6)', .9);
    const g = ridge(r, 650, 14, 1.2), gy = ridgeY(g, 120);
    // observatory: drum + dome + telescope, drawn before the ground so the ground hides its footing
    const bx = 120, top = gy - 62; poly(c, [[bx - 40, gy + 20], [bx - 40, top], [bx + 40, top], [bx + 40, gy + 20]], '#060a18');
    c.fillStyle = '#060a18'; c.beginPath(); c.arc(bx, top, 40, Math.PI, TAU); c.fill(); poly(c, [[bx + 4, top - 40], [bx + 15, top - 38], [bx + 15, top], [bx + 4, top]], '#ffd48a');
    c.strokeStyle = '#04060f'; c.lineWidth = 8; c.lineCap = 'round'; c.beginPath(); c.moveTo(bx + 9, top - 14); c.lineTo(bx + 52, top - 74); c.stroke(); c.lineWidth = 11; c.beginPath(); c.moveTo(bx + 49, top - 70); c.lineTo(bx + 54, top - 77); c.stroke();
    glow(c, bx + 10, top - 20, 40, 'rgba(255,200,120,.45)', .8); c.fillStyle = '#ffcf80'; c.fillRect(bx - 26, top + 26, 9, 12); c.fillRect(bx + 14, top + 26, 9, 12);
    fillRidge(c, g, '#0b1531'); const f = ridge(r, 735, 10, 1.3); fillRidge(c, f, '#04081a'); for (let x = 12; x < W; x += 22 + r() * 18) if (Math.abs(x - 120) > 60) fpine(c, x, ridgeY(f, x) + 3, 26 + r() * 30, '#02050f') } });

function paperBoat(k, hull, sail) { return [['p', sc([-20, 0, 20, 0, 13, 10, -13, 10], k), hull], ['p', sc([-2, -1, -2, -26, 16, -1], k), sail], ['p', sc([-4, -1, -4, -18, -17, -1], k), hull], ['g', 0, -8 * k, 30 * k, 'rgba(255,225,170,.28)', .8]] }
defScene({ id: 'paperboats', name: 'Paper Boats', mode: 'live', theme: 'dark', style: 'min', seed: 1104,
  st(c, r) { fsky(c, '#070c1e', '#17213f'); fmoon(c, 266, 170, 56); fridge(c, r, 418, 6, 2, '#0a1229'); const g = c.createLinearGradient(0, 430, 0, H); g.addColorStop(0, '#0e1833'); g.addColorStop(1, '#04081a'); c.fillStyle = g; c.fillRect(0, 428, W, H - 428);
    for (let i = 0; i < 40; i++) { const y = 440 + i * 8.5 + r() * 4, x = r() * W, l = 14 + r() * 50; c.fillStyle = 'rgba(140,160,220,' + (.05 + r() * .12) + ')'; c.fillRect(x, y, l, 1.4) } },
  fx() { return [{ t: 'stars', c: '#eaf0ff', pts: starPts(13, 120, 400, awayFrom(266, 170, 66)) },
    { t: 'shimmer', x: 266, y: 440, n: 30, step: 10, w0: 80, w1: 18, c: '#e1e8f8', a0: .5, dec: .014 },
    { t: 'mover', path: [-60, 560, 430, 560], dur: 150, off: 70, bob: [3, 1.1, 0], tilt: .05, shapes: paperBoat(1.2, '#c9d0e6', '#e6eaf6') },
    { t: 'mover', path: [-60, 640, 430, 640], dur: 110, off: 20, bob: [4, 1.3, 2], tilt: .06, shapes: paperBoat(1.9, '#ccd3e8', '#eef1fa') },
    { t: 'mover', path: [-60, 488, 430, 488], dur: 200, off: 120, bob: [2, 1, 4], tilt: .04, shapes: paperBoat(.7, '#aab3d0', '#c9d0e6') }] } });

defScene({ id: 'ferris', name: 'Night Fair', mode: 'live', theme: 'dark', style: 'min', seed: 1105,
  init(r) { return { g: ridge(r, 668, 8, 1.2) } },
  st(c, r, G) { fsky(c, '#080b21', '#1c2149'); fmoon(c, 70, 150, 36); const g = G.g;
    c.strokeStyle = '#05070f'; c.lineWidth = 5; c.lineCap = 'round'; c.beginPath(); c.moveTo(200, 420); c.lineTo(150, ridgeY(g, 150) + 8); c.moveTo(200, 420); c.lineTo(250, ridgeY(g, 250) + 8); c.stroke(); c.fillStyle = '#05070f'; c.beginPath(); c.arc(200, 420, 7, 0, TAU); c.fill();
    fillRidge(c, g, '#0a1230');
    [[40, 56], [300, 50]].forEach(([x, w], i) => { const y = ridgeY(g, x) + 4; poly(c, [[x - w / 2, y], [x - w / 2, y - 34], [x + w / 2, y - 34], [x + w / 2, y]], '#060a1a'); poly(c, [[x - w / 2 - 4, y - 34], [x, y - 52], [x + w / 2 + 4, y - 34]], i ? '#7a2a3e' : '#2a5a7a'); c.fillStyle = '#ffd48a'; c.fillRect(x - w / 2 + 6, y - 28, w - 12, 12) });
    fillRidge(c, ridge(r, 745, 6, 1.4), '#04081a') },
  fx(G) { const rim = [], R = 112; for (let i = 0; i < 24; i++) { const a = i / 24 * TAU, b = (i + 1) / 24 * TAU; rim.push(['l', Math.cos(a) * R, Math.sin(a) * R, Math.cos(b) * R, Math.sin(b) * R, 2.2, '#05070f']) } for (let i = 0; i < 12; i++) { const a = i / 12 * TAU; rim.push(['l', 0, 0, Math.cos(a) * R, Math.sin(a) * R, 1, '#05070f']); rim.push(['c', Math.cos(a) * R, Math.sin(a) * R, 2.4, '#ffe2a0']) }
    const gond = [['l', 0, 0, 0, 9, 1, '#05070f'], ['r', -7, 9, 14, 11, '#05070f'], ['r', -4, 12, 8, 5, '#ffcf80'], ['g', 0, 16, 22, 'rgba(255,190,100,.55)', .9]];
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(15, 110, 380, awayFrom(70, 150, 50)) }, { t: 'spin', x: 200, y: 420, speed: .22, shapes: rim }, { t: 'orbit', x: 200, y: 420, r: R, n: 12, speed: .22, shapes: gond },
      { t: 'glows', c: 'rgba(255,200,120,.55)', pts: [[40, ridgeY(G.g, 40) - 22, 44, 0, .3], [300, ridgeY(G.g, 300) - 22, 44, 1.7, .3]] }] } });
})();
