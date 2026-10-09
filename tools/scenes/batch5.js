(() => {
const { W, H, TAU, rng, lerp, mixh, ink, blob, hatch, circ, glow, sky, poly, ridge, fillRidge, fridge, ridgeY, hill, pine, fpine, roundTree, stars, drawStar, fstars, starPts, moon, cloud, cottage, rooftops, cat, fsky, fmoon, path, defScene } = NYX;
const L = 'rgba(190,190,235,.7)';
const away = (x, y, r) => (px, py) => Math.hypot(px - x, py - y) < r;
const night = (c, r, ymax = 380) => { sky(c, [[0, '#0b1030'], [.5, '#212857'], [1, '#2a2450']]); fstars(c, r, 90, ymax, .6) };
const post = (c, r, x, y, h, glowCol) => { ink(c, r, [[x, y], [x, y - h]], '#05060f', 3, .6, false, 1); blob(c, r, [[x - 6, y - h], [x + 6, y - h], [x + 8, y - h - 18], [x - 8, y - h - 18]], '#ffd890', '#05060f', 1.1, .5); glow(c, x, y - h - 10, 80, glowCol || 'rgba(255,200,110,.7)', .9) };

/* ---- sleeping cat on a window seat ---- */
defScene({ id: 'sleepcat', name: 'Sleeping Cat', mode: 'still', theme: 'dark', style: 'ink', seed: 1501,
  st(c, r) { c.fillStyle = '#1b1842'; c.fillRect(0, 0, W, H); hatch(c, r, () => c.rect(0, 0, W, H), 0, 0, W, H, .5, 9, '#2e2860', .7, .35);
    const win = [[40, 640], [40, 250], [90, 160], [180, 130], [270, 160], [320, 250], [320, 640]];
    c.save(); c.beginPath(); path(c, win, true); c.clip(); sky(c, [[0, '#0a1030'], [.6, '#262a5c'], [1, '#5a3e68']]); fstars(c, r, 70, 460, .7); moon(c, r, 240, 250, 38, '#f4e9c9', 'rgba(255,225,170,.35)', '#d0c096');
    rooftops(c, r, 560, '#10132e', L, '#ffcc70', 50, 100, .4).forEach(o => glow(c, o.x + 4, o.y + 5, 12, 'rgba(255,190,90,.5)', .6)); c.restore();
    ink(c, r, win.concat([win[0]]), '#07060f', 9, 2, true, 2); ink(c, r, [[180, 130], [180, 640]], '#07060f', 4, 1.2, false, 2); ink(c, r, [[40, 380], [320, 380]], '#07060f', 4, 1.2, false, 2);
    blob(c, r, [[16, 640], [344, 640], [352, 680], [8, 680]], '#2a2146', L, 1.4, 1.5);                 // sill
    blob(c, r, [[80, 640], [92, 604], [270, 604], [284, 640]], '#7a3a5a', L, 1.2, 1);                  // cushion on the sill
    // cat curled on the cushion: body, head tucked in front, tail wrapped round
    c.fillStyle = '#05050c'; c.beginPath(); c.ellipse(178, 592, 56, 24, 0, 0, TAU); c.fill(); c.beginPath(); c.arc(132, 596, 20, 0, TAU); c.fill();
    poly(c, [[118, 582], [116, 560], [130, 574]], '#05050c'); poly(c, [[134, 576], [142, 558], [148, 580]], '#05050c'); ink(c, r, [[225, 598], [240, 606], [212, 612], [160, 612], [126, 606]], '#05050c', 9, .6, false, 1);
    ink(c, r, [[122, 598], [128, 600], [134, 598]], 'rgba(190,190,235,.6)', 1, .2, false, 1); ink(c, r, [[130, 576], [172, 572], [220, 580]], 'rgba(190,190,235,.35)', 1, .5, false, 1);
    // lamp on the sill, right
    blob(c, r, [[300, 640], [320, 640], [318, 626], [302, 626]], '#2a2a52', L, 1.2, .6); ink(c, r, [[310, 626], [310, 560]], '#05060f', 3, .5, false, 1); blob(c, r, [[292, 560], [328, 560], [320, 534], [300, 534]], '#ffd890', '#05060f', 1.2, .6); glow(c, 310, 548, 150, 'rgba(255,190,100,.55)', 1);
    // plant at left
    blob(c, r, [[52, 640], [48, 606], [78, 606], [74, 640]], '#2a2146', L, 1.1, .8); for (let i = 0; i < 6; i++) ink(c, r, [[63, 606], [63 + (i - 2.5) * 12, 574 - Math.abs(i - 2.5) * -5]], '#3aa07a', 3, 1, false, 1) } });

/* ---- garden gate ---- */
defScene({ id: 'gardengate', name: 'Garden Gate', mode: 'live', theme: 'dark', style: 'ink', seed: 1502,
  st(c, r) { night(c, r, 330); moon(c, r, 270, 130, 30, '#f3e8c8', 'rgba(255,225,170,.3)', '#d4c69c'); hill(c, r, ridge(r, 470, 22, 1.1), '#1a1a45', L, L); hill(c, r, ridge(r, 540, 14, 1.4), '#101030', L, L);
    const ct = cottage(c, r, 180, 540, 1.2, '#161338', '#0a0a22', L, '#ffcf80'); ct.wins.forEach(o => glow(c, o.x + o.w / 2, o.y + o.h / 2, 46, 'rgba(255,190,90,.6)', .9));
    poly(c, [[160, 540], [200, 540], [330, 800], [20, 800]], '#2a2c5c'); hatch(c, r, () => path(c, [[160, 540], [200, 540], [330, 800], [20, 800]], true), 20, 540, 330, 800, -.3, 8, 'rgba(120,120,200,.5)', .7, .35);
    for (const [x, d] of [[-20, 1], [380, -1]]) { for (let i = 0; i < 5; i++) blob(c, r, circ(x + d * -i * 18 - d * 10, 640 + i * 24, 40 - i * 4, 10).map(p => [p[0], p[1]]), '#0e1a2e', L, 1, 1.5) }
    // gate pillars on the ground at y=700, iron bars between them
    [92, 268].forEach(x => { blob(c, r, [[x - 16, 700], [x - 16, 600], [x + 16, 600], [x + 16, 700]], '#1c1e48', L, 1.3, 1); blob(c, r, [[x - 20, 600], [x - 20, 590], [x + 20, 590], [x + 20, 600]], '#252858', L, 1.2, .8); post(c, r, x, 590, 0, 'rgba(255,200,110,.7)') });
    for (let i = 0; i < 9; i++) ink(c, r, [[116 + i * 15, 700], [116 + i * 15, 612]], '#06070f', 2.4, .5, false, 1); ink(c, r, [[110, 632], [250, 632]], '#06070f', 2.4, .5, false, 1); ink(c, r, [[110, 680], [250, 680]], '#06070f', 2.4, .5, false, 1) },
  fx() { const rr = rng(19), flies = Array.from({ length: 28 }, () => [rr() * W, 420 + rr() * 330, 10 + rr() * 26, 8 + rr() * 20, .2 + rr() * .5, rr() * TAU, .6 + rr() * 1.4]);
    return [{ t: 'stars', c: '#f2ebd8', pts: starPts(81, 90, 330, away(270, 130, 40)) }, { t: 'flies', c: 'rgba(220,255,150,.9)', pts: flies }, { t: 'glows', c: 'rgba(255,200,110,.5)', pts: [[92, 580, 34, 0, .3], [268, 580, 34, 1.3, .3]] }] } });

/* ---- little harbour ---- */
function dinghy(k, hull, sail) { return [['p', [-26 * k, 0, 26 * k, 0, 18 * k, 10 * k, -18 * k, 10 * k], hull], ['l', 0, 0, 0, -44 * k, 2 * k, hull], ['p', [2 * k, -42 * k, 2 * k, -6 * k, 22 * k, -6 * k], sail], ['g', 0, -16 * k, 26 * k, 'rgba(255,215,150,.25)', .8]] }
defScene({ id: 'littleharbour', name: 'Little Harbour', mode: 'live', theme: 'dark', style: 'ink', seed: 1503,
  st(c, r) { night(c, r, 330); moon(c, r, 90, 140, 34, '#f3e8c8', 'rgba(255,225,170,.3)', '#d4c69c');
    rooftops(c, r, 470, '#14173f', L, '#ffcf7a', 50, 110, .45).forEach(o => glow(c, o.x + 4, o.y + 5, 12, 'rgba(255,190,90,.5)', .6));
    const g = c.createLinearGradient(0, 480, 0, H); g.addColorStop(0, '#10183c'); g.addColorStop(1, '#070a1e'); c.fillStyle = g; c.fillRect(0, 480, W, H - 480);
    for (let i = 0; i < 44; i++) { const y = 500 + r() * 270, x = r() * W; ink(c, r, [[x, y], [x + 16 + r() * 40, y]], 'rgba(140,160,220,.18)', 1, .8, false, 1) }
    blob(c, r, [[-10, 660], [370, 660], [370, 690], [-10, 690]], '#2a2d5c', L, 1.3, 1.5);                 // quay top
    blob(c, r, [[-10, 690], [370, 690], [370, H + 10], [-10, H + 10]], '#161a40', L, 1.2, 1.2); for (let i = 0; i < 9; i++) ink(c, r, [[i * 42 + 6, 692], [i * 42 + 6, 790]], 'rgba(190,190,235,.2)', 1, .5, false, 1);
    [60, 180, 300].forEach(x => { post(c, r, x, 660, 70) ; ink(c, r, [[x + 22, 660], [x + 22, 644]], '#05060f', 6, .3, false, 1) }) },
  fx() { return [{ t: 'stars', c: '#f2ebd8', pts: starPts(82, 90, 330, away(90, 140, 44)) }, { t: 'shimmer', x: 90, y: 492, n: 22, step: 9, w0: 60, w1: 14, c: '#e1e8f8', a0: .4, dec: .016 },
    { t: 'mover', path: [120, 600, 120, 600], dur: 60, bob: [3, 1.1, 0], tilt: .05, shapes: dinghy(1.4, '#05060f', '#c9c2dc') }, { t: 'mover', path: [270, 566, 270, 566], dur: 60, bob: [2.5, 1.3, 2], tilt: .06, shapes: dinghy(1, '#05060f', '#b8b2d0') }, { t: 'mover', path: [40, 540, 40, 540], dur: 60, bob: [2, 1.4, 4], tilt: .06, shapes: dinghy(.7, '#070812', '#a8a2c0') }] } });

/* ---- treehouse ---- */
defScene({ id: 'treehouse', name: 'Treehouse', mode: 'still', theme: 'dark', style: 'ink', seed: 1504,
  init(r) { return { g: ridge(r, 690, 8, 1.2) } },
  st(c, r, G) { night(c, r, 360); moon(c, r, 60, 120, 32, '#f3e8c8', 'rgba(255,225,170,.3)', '#d4c69c'); const tx = 210, gy = ridgeY(G.g, tx), tc = '#0b0820';
    poly(c, [[tx - 26, gy + 12], [tx - 16, gy - 120], [tx - 12, gy - 260], [tx + 12, gy - 260], [tx + 18, gy - 120], [tx + 30, gy + 12]], tc);
    [[-12, -250, -120, -330], [12, -240, 110, -330], [-8, -180, -90, -240], [14, -170, 96, -230]].forEach(([a, b, d, e]) => { ink(c, r, [[tx + a, gy + b + 260 - 260 + 0], [tx + d, gy + e + 0]].map(p => p), tc, 8, 1.2, false, 1) });
    const rr = rng(6); for (let i = 0; i < 26; i++) { const a = rr() * TAU, d = Math.sqrt(rr()), px = tx + Math.cos(a) * d * 150, py = gy - 300 + Math.sin(a) * d * 95; blob(c, r, circ(px, py, 26 + rr() * 22, 9), i % 3 ? '#0e1a34' : '#122340', null, 1, 3) }
    // platform resting on two branches + trunk, hut on it
    const py = gy - 150; poly(c, [[tx - 96, py], [tx + 100, py], [tx + 100, py + 8], [tx - 96, py + 8]], '#241a2a'); ink(c, r, [[tx - 70, py + 8], [tx - 14, py + 60]], '#241a2a', 5, .5, false, 1); ink(c, r, [[tx + 70, py + 8], [tx + 14, py + 60]], '#241a2a', 5, .5, false, 1);
    blob(c, r, [[tx - 70, py], [tx - 70, py - 52], [tx + 10, py - 52], [tx + 10, py]], '#3a2a3a', L, 1.2, 1); blob(c, r, [[tx - 80, py - 52], [tx - 30, py - 86], [tx + 20, py - 52]], '#1a1226', L, 1.3, 1);
    c.fillStyle = '#ffcf80'; c.fillRect(tx - 52, py - 40, 22, 22); ink(c, r, [[tx - 52, py - 40], [tx - 30, py - 40], [tx - 30, py - 18], [tx - 52, py - 18]], L, 1, .5, true, 1); glow(c, tx - 41, py - 29, 70, 'rgba(255,190,100,.55)', .9);
    ink(c, r, [[tx + 60, py + 8], [tx + 60, py + 40]], '#05060f', 1.5, .3, false, 1); blob(c, r, [[tx + 52, py + 40], [tx + 68, py + 40], [tx + 68, py + 44], [tx + 52, py + 44]], '#5a3a2a', null, 1, .3);
    // rope ladder from the platform down to the ground
    ink(c, r, [[tx + 88, py + 8], [tx + 92, gy + 2]], '#8a6a4a', 1.6, .6, false, 1); ink(c, r, [[tx + 100, py + 8], [tx + 104, gy + 2]], '#8a6a4a', 1.6, .6, false, 1); for (let y = py + 24; y < gy; y += 20) ink(c, r, [[tx + 89 + (y - py) * .02, y], [tx + 102 + (y - py) * .02, y]], '#8a6a4a', 1.6, .3, false, 1);
    fillRidge(c, G.g, '#0a1030'); fillRidge(c, ridge(r, 745, 6, 1.4), '#04061a'); for (let i = 0; i < 60; i++) { const x = r() * W; ink(c, r, [[x, ridgeY(G.g, x) + 4 + r() * 50], [x + (r() - .5) * 6, ridgeY(G.g, x) - 4 + r() * 50]], '#0f1a40', 1.2, .5, false, 1) } } });

/* ---- night market ---- */
defScene({ id: 'nightmarket', name: 'Night Market', mode: 'live', theme: 'dark', style: 'ink', seed: 1505,
  init(r) { const bulbs = []; for (let k = 0; k < 3; k++) for (let i = 0; i < 9; i++) { const t = i / 8, y = 330 + k * 46; bulbs.push([lerp(10, 350, t), y + Math.sin(t * Math.PI) * 26 - 8]) } return { bulbs } },
  st(c, r, G) { night(c, r, 250); rooftops(c, r, 520, '#14173f', L, '#ffcf7a', 120, 200, .35); const gy = 700;
    G.bulbs.forEach(([x, y], i) => { c.fillStyle = ['#ffd27a', '#ff9ab0', '#9ad0ff'][i % 3]; c.beginPath(); c.arc(x, y, 3.2, 0, TAU); c.fill() });
    [0, 1, 2].forEach(k => { ink(c, r, [[10, 322 + k * 46], [180, 322 + k * 46 + 52], [350, 322 + k * 46]], 'rgba(5,6,15,.8)', 1.2, .6, false, 1) });
    [[40, 90, '#8a2f45'], [180, 100, '#2f6a7a'], [312, 90, '#a8803a']].forEach(([x, w, col], i) => { const top = 590; blob(c, r, [[x - w / 2, gy], [x - w / 2, top], [x + w / 2, top], [x + w / 2, gy]], '#1d2050', L, 1.2, 1); for (let s = 0; s < 6; s++) poly(c, [[x - w / 2 - 6 + s * (w + 12) / 6, top - 34], [x - w / 2 - 6 + (s + 1) * (w + 12) / 6, top - 34], [x - w / 2 - 10 + (s + 1) * (w + 20) / 6, top + 4], [x - w / 2 - 10 + s * (w + 20) / 6, top + 4]], s % 2 ? '#e6dcc0' : col); c.fillStyle = '#ffcf80'; c.fillRect(x - w / 2 + 8, top + 14, w - 16, 44); const rr2 = rng(i + 3); for (let q = 0; q < 6; q++) { c.fillStyle = ['#8a3a3a', '#3a6a8a', '#5a8a4a', '#c9a24a'][Math.floor(rr2() * 4)]; c.fillRect(x - w / 2 + 12 + q * (w - 28) / 6, top + 40, (w - 28) / 6 - 2, 14) } });
    poly(c, [[-10, gy], [370, gy], [370, H + 10], [-10, H + 10]], '#1a1c42'); for (let i = 0; i < 70; i++) { const x = r() * W, y = gy + 4 + r() * 70; ink(c, r, circ(x, y, 5 + r() * 4, 8), 'rgba(190,190,235,.25)', .8, .6, true, 1) } },
  fx(G) { return [{ t: 'stars', c: '#f2ebd8', pts: starPts(83, 70, 250) }, { t: 'glows', c: '#fff', cols: ['rgba(255,210,122,.75)', 'rgba(255,154,176,.7)', 'rgba(154,208,255,.7)'], pts: G.bulbs.map(([x, y], i) => [x, y, 22, i * 1.3, .5, i % 3]) },
    { t: 'glows', c: 'rgba(255,190,100,.5)', pts: [[40, 650, 90, 0, .25], [180, 650, 100, 1, .25], [312, 650, 90, 2, .25]] }, { t: 'smoke', c: '#d8d0e8', x: 180, y: 590, n: 7, drift: 18, rise: 70, r0: 3, grow: 9, speed: .14, a: .2 }] } });

/* ---- chimney roofs ---- */
defScene({ id: 'chimneyroofs', name: 'Chimney Smoke', mode: 'live', theme: 'dark', style: 'ink', seed: 1506,
  init(r) { const seed = 7771, dummy = document.createElement('canvas').getContext('2d'); const back = rooftops(dummy, rng(seed), 600, '#14173f', L, '#ffcf7a', 80, 150, .4, [3]); return { seed, tops: back.tops } },
  st(c, r, G) { night(c, r, 400); moon(c, r, 80, 150, 38, '#f1e6c6', 'rgba(255,225,170,.3)', '#cdbf96'); cloud(c, r, 150, 300, 150, 'rgba(48,38,92,.55)', 'rgba(210,200,240,.35)');
    const wa = rooftops(c, r.constructor === Function ? r : rng(G.seed), 600, '#14173f', L, '#ffcf7a', 80, 150, .4, [3]); wa.forEach(o => glow(c, o.x + 4, o.y + 5, 12, 'rgba(255,190,90,.5)', .6));
    const wb = rooftops(c, r, 735, '#0a0b24', L, '#ffc060', 90, 160, .4); wb.forEach(o => glow(c, o.x + 4, o.y + 5, 14, 'rgba(255,180,80,.55)', .7)); const f = G.tops[3]; cat(c, r, f.x + f.w * .5, f.y, 1.5, '#05050e', '#c9c9f0', '#ffc860') },
  fx(G) { const sm = G.tops.filter(t => t.chim).slice(0, 5).map((t, i) => ({ t: 'smoke', c: '#d0c8e6', x: t.chim[0], y: t.chim[1], n: 7, drift: 22, rise: 90, r0: 3, grow: 11, speed: .1 + i * .012, a: .24 }));
    return [{ t: 'stars', c: '#f2ebd8', pts: starPts(84, 100, 400, away(80, 150, 46)) }, ...sm] } });

/* ---- snow lane ---- */
defScene({ id: 'snowlane', name: 'Snow Lane', mode: 'live', theme: 'dark', style: 'ink', seed: 1507,
  st(c, r) { sky(c, [[0, '#0a1432'], [.5, '#27386a'], [.8, '#51507e'], [1, '#7a6a90']]); fstars(c, r, 90, 330, .6); moon(c, r, 270, 130, 32, '#f4eedc', 'rgba(220,235,255,.3)', '#cfd0c0');
    hill(c, r, ridge(r, 470, 22, 1.1), '#27325e', L, L); for (let i = 0; i < 14; i++) pine(c, r, r() * W, 540 + r() * 16, 50 + r() * 40, '#101a3c', 'rgba(150,170,230,.5)');
    // lane converging to the far cottage, snow banks both sides
    poly(c, [[170, 560], [190, 560], [400, 800], [-40, 800]], '#c9d3ec'); hatch(c, r, () => path(c, [[170, 560], [190, 560], [400, 800], [-40, 800]], true), -40, 560, 400, 800, -.3, 8, 'rgba(90,110,170,.5)', .7, .4);
    poly(c, [[-10, 560], [170, 560], [-40, 800], [-10, 800]], '#dbe3f5'); poly(c, [[190, 560], [370, 560], [370, 800], [400, 800]], '#dbe3f5');
    cottage(c, r, 180, 562, .8, '#3b3668', '#262250', 'rgba(70,80,140,.8)', '#ffcf7a', true).wins.forEach(o => glow(c, o.x + o.w / 2, o.y + o.h / 2, 30, 'rgba(255,180,80,.55)', .8));
    cottage(c, r, 56, 640, 1.5, '#3b3668', '#262250', 'rgba(70,80,140,.8)', '#ffcf7a', true).wins.forEach(o => glow(c, o.x + o.w / 2, o.y + o.h / 2, 40, 'rgba(255,180,80,.55)', .8));
    cottage(c, r, 310, 660, 1.6, '#3b3668', '#262250', 'rgba(70,80,140,.8)', '#ffcf7a', true).wins.forEach(o => glow(c, o.x + o.w / 2, o.y + o.h / 2, 40, 'rgba(255,180,80,.55)', .8));
    [[150, 620, .5], [212, 700, .8], [124, 780, 1.1]].forEach(([x, y, k]) => { ink(c, r, [[x, y], [x, y - 70 * k]], '#121028', 3 * k, .6, false, 1); blob(c, r, [[x - 6 * k, y - 70 * k], [x + 6 * k, y - 70 * k], [x + 7 * k, y - 84 * k], [x - 7 * k, y - 84 * k]], '#ffd88a', '#121028', 1, .5); glow(c, x, y - 77 * k, 70 * k, 'rgba(255,205,120,.75)', .9) }) },
  fx() { const rr = rng(9), pts = Array.from({ length: 150 }, () => [rr() * W, rr() * H, .8 + rr() * 2.2, 14 + rr() * 42, rr() * TAU, 0]); return [{ t: 'fall', m: 'snow', c: 'rgba(240,246,255,.8)', pts }] } });

/* ---- orchard ---- */
defScene({ id: 'orchard', name: 'Orchard Night', mode: 'still', theme: 'dark', style: 'ink', seed: 1508,
  init(r) { return { g: ridge(r, 650, 8, 1.1) } },
  st(c, r, G) { night(c, r, 380); moon(c, r, 290, 140, 36, '#f3e8c8', 'rgba(255,225,170,.3)', '#d4c69c'); hill(c, r, ridge(r, 540, 18, 1.2), '#1a1a45', L, L);
    const trees = [[70, 640, 150], [190, 660, 130], [300, 645, 142]]; trees.forEach(([x, y, h]) => { roundTree(c, r, x, ridgeY(G.g, x) + 8, h, '#0f2a2a', 'rgba(150,200,190,.45)', '#2a1c20'); for (let i = 0; i < 9; i++) { const a = r() * TAU, d = Math.sqrt(r()) * h * .3; c.fillStyle = '#c9503e'; c.beginPath(); c.arc(x + Math.cos(a) * d, ridgeY(G.g, x) + 8 - h * .66 + Math.sin(a) * d, 3.4, 0, TAU); c.fill() } });
    // ladder leaning on the left tree trunk, feet on the ground
    const gy1 = ridgeY(G.g, 100) + 6; ink(c, r, [[100, gy1], [74, gy1 - 120]], '#8a6a4a', 3, .5, false, 1); ink(c, r, [[118, gy1], [86, gy1 - 120]], '#8a6a4a', 3, .5, false, 1); for (let i = 1; i < 6; i++) { const t = i / 6; ink(c, r, [[lerp(100, 74, t), gy1 - 120 * t], [lerp(118, 86, t), gy1 - 120 * t]], '#8a6a4a', 2, .3, false, 1) }
    fillRidge(c, G.g, '#0b1233'); fillRidge(c, ridge(r, 740, 7, 1.4), '#05071c');
    // picket fence along the front, posts standing on the ground
    for (let x = 6; x < W; x += 16) { const y = ridgeY(G.g, x) + 40; blob(c, r, [[x, y + 30], [x, y], [x + 5, y - 8], [x + 10, y], [x + 10, y + 30]], '#cfc8d8', 'rgba(60,50,90,.8)', 1, .5) } ink(c, r, [[0, ridgeY(G.g, 0) + 30], [W, ridgeY(G.g, W) + 30]], '#cfc8d8', 2, .5, false, 1) } });

/* ---- lake dock ---- */
defScene({ id: 'lakedock', name: 'Lake Dock', mode: 'live', theme: 'dark', style: 'ink', seed: 1509,
  init(r) { return { shore: ridge(r, 470, 14, 1.3) } },
  st(c, r, G) { night(c, r, 330); moon(c, r, 240, 140, 40, '#f3e8c8', 'rgba(255,225,170,.3)', '#d4c69c'); fillRidge(c, ridge(r, 440, 16, 1.6), '#161a40');
    const lake = c.createLinearGradient(0, 480, 0, H); lake.addColorStop(0, '#10183c'); lake.addColorStop(1, '#070a1e'); const sp = G.shore;
    c.beginPath(); c.moveTo(-10, 480); c.lineTo(W + 10, 480); c.lineTo(W + 10, H + 10); c.lineTo(-10, H + 10); c.closePath(); c.fillStyle = lake; c.fill();
    blob(c, r, [[-10, 480], [-10, 470], [100, 466], [150, 478], [150, 492], [-10, 492]], '#0b1030', L, 1.1, 1);           // little headland with the cabin
    const ct = cottage(c, r, 56, 470, .9, '#161338', '#0a0a22', L, '#ffcf80'); ct.wins.forEach(o => glow(c, o.x + o.w / 2, o.y + o.h / 2, 40, 'rgba(255,190,90,.6)', .9));
    for (let i = 0; i < 34; i++) { const y = 500 + r() * 270, x = r() * W; ink(c, r, [[x, y], [x + 16 + r() * 40, y]], 'rgba(140,160,220,.16)', 1, .8, false, 1) }
    // dock: deck planks in perspective, posts standing in the water
    poly(c, [[150, 800], [240, 800], [210, 560], [186, 560]], '#3a2a30'); for (let i = 0; i < 12; i++) { const t = i / 12, y = lerp(800, 560, t), hw = lerp(45, 12, t); ink(c, r, [[180 - hw - 6, y], [180 + hw + 6, y]], 'rgba(20,12,16,.8)', 1.2, .5, false, 1) }
    [[150, 760], [240, 760], [174, 640], [216, 640]].forEach(([x, y]) => ink(c, r, [[x, y], [x, y + 36]], '#1a1216', 6, .4, false, 1)); post(c, r, 186, 568, 40, 'rgba(255,200,110,.7)'); ink(c, r, [[216, 640], [232, 650]], '#8a6a4a', 1.6, .5, false, 1) },
  fx() { const rr = rng(2), flies = Array.from({ length: 18 }, () => [rr() * W, 420 + rr() * 200, 10 + rr() * 20, 8 + rr() * 14, .2 + rr() * .4, rr() * TAU, .6 + rr() * 1.2]);
    return [{ t: 'stars', c: '#f2ebd8', pts: starPts(85, 90, 330, away(240, 140, 50)) }, { t: 'shimmer', x: 240, y: 490, n: 24, step: 10, w0: 70, w1: 16, c: '#e1e8f8', a0: .45, dec: .016 }, { t: 'flies', c: 'rgba(220,255,150,.9)', pts: flies },
      { t: 'mover', path: [268, 650, 268, 650], dur: 60, bob: [3, 1, 0], tilt: .05, shapes: [['p', [-40, 0, 40, 0, 28, 12, -28, 12], '#2a1c20'], ['p', [-40, 0, 40, 0, 36, -2, -36, -2], '#4a3a3a'], ['g', 0, -4, 24, 'rgba(255,200,120,.3)', .8]] }] } });

/* ---- clock tower ---- */
defScene({ id: 'clocktower', name: 'Clock Tower Square', mode: 'still', theme: 'dark', style: 'ink', seed: 1510,
  st(c, r) { night(c, r, 330); moon(c, r, 70, 130, 30, '#f3e8c8', 'rgba(255,225,170,.3)', '#d4c69c'); const gy = 680;
    rooftops(c, r, 600, '#14173f', L, '#ffcf7a', 80, 140, .4).forEach(o => glow(c, o.x + 4, o.y + 5, 12, 'rgba(255,190,90,.5)', .6));
    blob(c, r, [[130, gy], [130, 300], [230, 300], [230, gy]], '#1e2250', L, 1.4, 1.2); blob(c, r, [[122, 300], [180, 190], [238, 300]], '#141738', L, 1.4, 1.2); ink(c, r, [[180, 190], [180, 160]], L, 2, .4, false, 1);
    blob(c, r, circ(180, 350, 40, 22), '#f6e7b8', '#05060f', 2.4, 1); glow(c, 180, 350, 100, 'rgba(255,225,150,.55)', 1); for (let i = 0; i < 12; i++) { const a = i / 12 * TAU; ink(c, r, [[180 + Math.cos(a) * 33, 350 + Math.sin(a) * 33], [180 + Math.cos(a) * 37, 350 + Math.sin(a) * 37]], '#2a2230', 1.6, .3, false, 1) }
    ink(c, r, [[180, 350], [180 - Math.sin(.3) * 20, 350 - Math.cos(.3) * 20]], '#2a2230', 3, .3, false, 1); ink(c, r, [[180, 350], [180 + Math.sin(2.1) * 28, 350 - Math.cos(2.1) * 28]], '#2a2230', 2.2, .3, false, 1);
    [[160, 470], [200, 470], [160, 560], [200, 560]].forEach(([x, y]) => { c.fillStyle = '#ffcf80'; c.fillRect(x - 8, y, 16, 26); ink(c, r, [[x - 8, y], [x + 8, y], [x + 8, y + 26], [x - 8, y + 26]], L, 1, .4, true, 1) });
    poly(c, [[-10, gy], [370, gy], [370, H + 10], [-10, H + 10]], '#1b1e46'); for (let i = 0; i < 90; i++) { const x = r() * W, y = gy + 6 + r() * 90; ink(c, r, circ(x, y, 6 + r() * 4, 8), 'rgba(190,190,235,.22)', .8, .6, true, 1) }
    [60, 300].forEach(x => post(c, r, x, gy + 20, 90)) } });

/* ---- long: ink village from night to dawn to night ---- */
const LOOP = 70;
const inkSky = (t, b) => c => { const g = c.createLinearGradient(0, 0, 0, H); g.addColorStop(0, t); g.addColorStop(1, b); c.fillStyle = g; c.fillRect(0, 0, W, H) };
defScene({ id: 'inkvillage', name: 'Village Dawn', mode: 'long', theme: 'dark', style: 'ink', seed: 1511, loop: LOOP,
  init(r) { return { far: ridge(r, 520, 26, 1.1), near: ridge(r, 620, 18, 1.3), houses: [[110, 0], [200, 0], [270, 0]] } },
  skyFrames: [{ p: 0, draw: inkSky('#0b1030', '#262a58') }, { p: .22, draw: inkSky('#10183a', '#6a4466') }, { p: .36, draw: inkSky('#4a5a96', '#f0a77a') }, { p: .5, draw: inkSky('#6f93c4', '#f6dca8') }, { p: .72, draw: inkSky('#6a7aaa', '#f2b27c') }, { p: .86, draw: inkSky('#2a2a5a', '#a05a78') }],
  st(c, r, G) { hill(c, r, G.far, '#2a3262', 'rgba(200,205,245,.6)', 'rgba(200,205,245,.5)'); hill(c, r, G.near, '#1a2048', 'rgba(200,205,245,.6)', 'rgba(200,205,245,.5)');
    G.houses.forEach(([x]) => { const y = ridgeY(G.near, x) + 8; cottage(c, r, x, y, 1.1, '#141838', '#0a0b24', 'rgba(190,190,235,.7)', null) }); for (let i = 0; i < 10; i++) { const x = r() * 90; pine(c, r, x + 6, ridgeY(G.near, x + 6) + 6, 40 + r() * 36, '#080b20', 'rgba(150,170,230,.5)') } },
  fx(G) { const sun = { t: 'orb', z: 0, r: 32, c: '#fff3d0', glow: 'rgba(255,214,150,.75)', hi: '#fffbea', warm: '#ffb070', x0: -40, x1: 400, y0: 640, yAmp: 470, a: .3, b: .88 };
    const moonA = { t: 'orb', z: 0, r: 38, c: '#e4e9f6', glow: 'rgba(170,190,235,.32)', craters: 1, x0: 400, x1: -40, y0: 600, yAmp: 400, a: .82, b: 1, q0: 0, q1: .5 }, moonB = { ...moonA, a: 0, b: .22, q0: .5, q1: .86 };
    const wins = [], smoke = []; G.houses.forEach(([x], i) => { const y = ridgeY(G.near, x) + 8; wins.push([x - 9, y - 28, 24, i * 1.3, .25], [x + 9, y - 28, 24, i * 1.9, .25]); smoke.push({ t: 'smoke', c: '#d8d0e8', x: x + 20, y: y - 56, n: 6, drift: 20, rise: 80, r0: 3, grow: 9, speed: .08 + i * .01, a: .22 }) });
    return [{ t: 'stars', z: 0, c: '#f2ebd8', vis: [[0, 1], [.2, 1], [.36, 0], [.8, 0], [.92, 1], [1, 1]], pts: starPts(91, 120, 480) }, sun, moonA, moonB, { t: 'glows', c: 'rgba(255,190,100,.7)', vis: [[0, 1], [.25, 1], [.36, 0], [.78, 0], [.9, 1], [1, 1]], pts: wins }, ...smoke] } });
})();
