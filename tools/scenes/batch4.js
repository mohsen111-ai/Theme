(() => {
const { W, H, TAU, rng, lerp, mixh, ink, blob, hatch, circ, glow, sky, poly, ridge, fillRidge, fridge, ridgeY, fpine, ftree, fsky, fmoon, fstars, starPts, defScene } = NYX;
const away = (x, y, r) => (px, py) => Math.hypot(px - x, py - y) < r;
const S = (pts, k) => pts.map(v => +(v * k).toFixed(2));
const gauss = r => (r() + r() + r() + r() - 2) / 2;

/* ---- tent under the milky way ---- */
defScene({ id: 'milkyway', name: 'Tent Under the Milky Way', mode: 'still', theme: 'dark', style: 'min', seed: 1401,
  init(r) { return { g: ridge(r, 650, 10, 1.1) } },
  st(c, r, G) { fsky(c, '#050818', '#141c3e'); const rr = rng(4);
    c.save(); c.translate(190, 250); c.rotate(-.55); const g = c.createRadialGradient(0, 0, 0, 0, 0, 120); g.addColorStop(0, 'rgba(170,185,240,.30)'); g.addColorStop(1, 'rgba(170,185,240,0)'); c.save(); c.scale(5, 1); c.fillStyle = g; c.beginPath(); c.arc(0, 0, 120, 0, TAU); c.fill(); c.restore();
    for (let i = 0; i < 1500; i++) { const x = (rr() - .5) * 900, y = gauss(rr) * 62, a = Math.max(0, .55 - Math.abs(y) / 120); c.fillStyle = 'rgba(225,232,255,' + (a * (.3 + rr() * .7)) + ')'; c.beginPath(); c.arc(x, y, .35 + rr() * .8, 0, TAU); c.fill() } c.restore();
    fstars(c, r, 140, 560, .8);
    const x = 170, gy = ridgeY(G.g, x);
    // tent (drawn before the ground so the grass covers its hem)
    poly(c, [[x - 64, gy + 6], [x, gy - 62], [x + 64, gy + 6]], '#0a1030'); poly(c, [[x - 10, gy + 6], [x, gy - 30], [x + 12, gy + 6]], '#ffcf80'); poly(c, [[x, gy - 62], [x - 64, gy + 6], [x - 30, gy + 6]], '#070b22');
    glow(c, x, gy - 8, 70, 'rgba(255,190,100,.55)', .9);
    fillRidge(c, G.g, '#0a1130'); const f = ridge(r, 735, 8, 1.4); fillRidge(c, f, '#04081a');
    for (let i = 0; i < 12; i++) { const px = 230 + i * 12 + r() * 10; if (px < W) fpine(c, px, ridgeY(G.g, px) + 4, 40 + r() * 50, '#03061a') } } });

/* ---- desert stars ---- */
function saguaro(c, x, y, h, col) { c.strokeStyle = col; c.lineCap = 'round'; c.lineJoin = 'round'; const w = h * .11;
  c.lineWidth = w; c.beginPath(); c.moveTo(x, y + 6); c.lineTo(x, y - h); c.stroke();
  c.lineWidth = w * .75; c.beginPath(); c.moveTo(x, y - h * .45); c.lineTo(x - h * .26, y - h * .45); c.lineTo(x - h * .26, y - h * .72); c.stroke();
  c.beginPath(); c.moveTo(x, y - h * .6); c.lineTo(x + h * .24, y - h * .6); c.lineTo(x + h * .24, y - h * .84); c.stroke() }
defScene({ id: 'cactus', name: 'Desert Stars', mode: 'still', theme: 'dark', style: 'min', seed: 1402,
  init(r) { return { g: ridge(r, 665, 8, 1.3), mesa: ridge(r, 600, 26, 1.7) } },
  st(c, r, G) { fsky(c, '#070b1f', '#2a2a5a'); fstars(c, r, 170, 520, .8); fmoon(c, 80, 130, 26);
    fillRidge(c, G.mesa, '#12183a'); poly(c, [[210, 610], [214, 520], [300, 516], [304, 610]], '#12183a');
    saguaro(c, 92, ridgeY(G.g, 92), 170, '#05070f'); saguaro(c, 280, ridgeY(G.g, 280) - 6, 92, '#070a18'); saguaro(c, 190, ridgeY(G.g, 190) - 4, 56, '#0a0e22');
    fillRidge(c, G.g, '#0c1030'); const f = ridge(r, 740, 7, 1.6); fillRidge(c, f, '#04061a');
    for (let i = 0; i < 9; i++) { const x = r() * W, y = ridgeY(f, x) + 4; poly(c, [[x - 10 - r() * 6, y], [x - 4, y - 8 - r() * 8], [x + 6, y - 6 - r() * 8], [x + 12 + r() * 6, y]], '#02040f') } } });

/* ---- deer on a hilltop ---- */
function deer(c, x, gy, k, col) { c.save(); c.translate(x, gy - 38 * k); c.scale(k, k); c.fillStyle = col; c.strokeStyle = col; c.lineCap = 'round'; c.lineJoin = 'round';
  c.beginPath(); c.ellipse(0, 0, 24, 11, 0, 0, TAU); c.fill();                                   // body
  poly(c, [[10, -6], [22, -4], [33, -26], [25, -31]], col);                                     // neck
  c.save(); c.translate(33, -30); c.rotate(.45); c.beginPath(); c.ellipse(0, 0, 9, 5, 0, 0, TAU); c.fill(); c.restore(); // head
  poly(c, [[38, -29], [48, -24], [46, -20], [37, -24]], col);                                   // snout
  poly(c, [[27, -34], [24, -42], [31, -36]], col);                                              // ear
  c.lineWidth = 2.2; [[[32, -34], [29, -48], [23, -60]], [[29, -48], [38, -57]], [[27, -53], [18, -54]], [[35, -34], [40, -48], [47, -59]], [[40, -48], [49, -51]]].forEach(l => { c.beginPath(); c.moveTo(l[0][0], l[0][1]); for (let i = 1; i < l.length; i++)c.lineTo(l[i][0], l[i][1]); c.stroke() });
  c.lineWidth = 3.4;[[[12, 6], [14, 22], [12, 38]], [[7, 8], [8, 22], [6, 38]], [[-14, 6], [-15, 22], [-13, 38]], [[-19, 6], [-21, 20], [-19, 38]]].forEach(l => { c.beginPath(); c.moveTo(l[0][0], l[0][1]); c.lineTo(l[1][0], l[1][1]); c.lineTo(l[2][0], l[2][1]); c.stroke() });
  poly(c, [[-22, -4], [-30, -10], [-24, 2]], col); c.restore() }
defScene({ id: 'deerhill', name: 'Meadow Deer', mode: 'still', theme: 'dark', style: 'min', seed: 1403,
  init(r) { return { g: ridge(r, 600, 20, .9), f: ridge(r, 720, 10, 1.3) } },
  st(c, r, G) { fsky(c, '#070b20', '#232c5c'); fmoon(c, 250, 200, 72); fstars(c, r, 140, 480, .7);
    fillRidge(c, ridge(r, 540, 22, 1.4), '#0f1737'); const dx = 150; fillRidge(c, G.g, '#09102b');
    deer(c, dx, ridgeY(G.g, dx) + 4, 1.5, '#03050f'); // hooves rest just inside the ground line
    fillRidge(c, G.f, '#04071a'); for (let i = 0; i < 90; i++) { const x = r() * W, y = ridgeY(G.g, x) + 6 + r() * 50; ink(c, r, [[x, y], [x + (r() - .5) * 8, y - 8 - r() * 8]], '#0d1536', 1.2, .6, false, 1) } } });

/* ---- ringed planet ---- */
defScene({ id: 'ringplanet', name: 'Ringed Planet', mode: 'still', theme: 'dark', style: 'min', seed: 1404,
  st(c, r) { fsky(c, '#04061a', '#141a46'); fstars(c, r, 180, 600, .8); const px = 200, py = 470, R = 132;
    glow(c, px, py, R * 2.2, 'rgba(120,100,220,.28)', 1);
    const ring = (front) => { c.save(); c.translate(px, py); c.rotate(-.28); c.lineWidth = 12; for (let k = 0; k < 3; k++) { const rad = 190 + k * 20; c.strokeStyle = ['rgba(210,196,240,.55)', 'rgba(170,150,215,.4)', 'rgba(210,196,240,.3)'][k]; c.lineWidth = [12, 8, 5][k]; c.beginPath(); c.ellipse(0, 0, rad, rad * .26, 0, front ? 0 : Math.PI, front ? Math.PI : TAU); c.stroke() } c.restore() };
    ring(false);
    const g = c.createLinearGradient(px, py - R, px, py + R); g.addColorStop(0, '#6a58b8'); g.addColorStop(.5, '#4a3f95'); g.addColorStop(1, '#241d58'); c.fillStyle = g; c.beginPath(); c.arc(px, py, R, 0, TAU); c.fill();
    c.save(); c.beginPath(); c.arc(px, py, R, 0, TAU); c.clip(); [[-80, 14, .18], [-40, 22, .12], [10, 16, .2], [52, 24, .14], [96, 12, .16]].forEach(([y, h, a]) => { c.fillStyle = 'rgba(20,12,60,' + a + ')'; c.fillRect(px - R, py + y, R * 2, h) }); const sh = c.createRadialGradient(px - R * .4, py - R * .5, R * .1, px, py, R * 1.1); sh.addColorStop(0, 'rgba(255,255,255,.18)'); sh.addColorStop(1, 'rgba(0,0,30,.55)'); c.fillStyle = sh; c.fillRect(px - R, py - R, R * 2, R * 2); c.restore();
    ring(true);
    c.fillStyle = '#d8d4f0'; c.beginPath(); c.arc(60, 150, 14, 0, TAU); c.fill(); c.fillStyle = '#a79ed0'; c.beginPath(); c.arc(318, 250, 8, 0, TAU); c.fill();
    fillRidge(c, ridge(r, 650, 14, 1.3), '#070a22'); fillRidge(c, ridge(r, 720, 10, 1.7), '#03051a'); for (let x = 8; x < W; x += 20 + r() * 16) fpine(c, x, 740 + r() * 8, 30 + r() * 30, '#02030f') } });

/* ---- glowing mushrooms ---- */
defScene({ id: 'mushrooms', name: 'Glow Mushrooms', mode: 'live', theme: 'dark', style: 'min', seed: 1405,
  init(r) { return { g: ridge(r, 680, 8, 1.6), caps: [[84, 600, 62, '#5ae0c8'], [170, 662, 34, '#ff8fc0'], [262, 590, 70, '#7ac8ff'], [336, 664, 26, '#5ae0c8'], [20, 690, 24, '#ff8fc0'], [210, 700, 20, '#7ac8ff']] } },
  st(c, r, G) { fsky(c, '#04100f', '#0a2a2a'); for (let i = 0; i < 6; i++) { const x = 20 + i * 66 + r() * 20, w = 12 + r() * 12; poly(c, [[x - w, 700], [x - w * .6, 120 + r() * 120], [x + w * .6, 120 + r() * 120], [x + w, 700]], i % 2 ? '#04161a' : '#031014') }
    G.caps.forEach(([x, y, w, col]) => { const k = w / 40; poly(c, [[x - 5 * k, y + 40 * k], [x - 4 * k, y], [x + 4 * k, y], [x + 5 * k, y + 40 * k]], '#cfe8e0'); c.fillStyle = '#0c2a30'; c.beginPath(); c.ellipse(x, y, w, w * .62, 0, Math.PI, TAU); c.fill(); c.fillStyle = col; c.globalAlpha = .75; c.beginPath(); c.ellipse(x, y - w * .05, w * .86, w * .46, 0, Math.PI, TAU); c.fill(); c.globalAlpha = 1; c.fillStyle = 'rgba(255,255,255,.55)';[[-.4, -.3], [.1, -.45], [.45, -.2], [-.05, -.15]].forEach(([a, b]) => { c.beginPath(); c.arc(x + a * w, y + b * w * .6, 2 + k, 0, TAU); c.fill() }) });
    fillRidge(c, G.g, '#031016'); fillRidge(c, ridge(r, 745, 6, 1.8), '#02080c'); for (let i = 0; i < 50; i++) { const x = r() * W, y = 735 + r() * 40; ink(c, r, [[x, y], [x + (r() - .5) * 14, y - 18 - r() * 22]], '#052028', 1.6, .8, false, 1) } },
  fx(G) { const rr = rng(8), flies = Array.from({ length: 30 }, () => [rr() * W, 300 + rr() * 380, 12 + rr() * 26, 8 + rr() * 20, .2 + rr() * .5, rr() * TAU, .6 + rr() * 1.4]);
    return [{ t: 'glows', c: 'rgba(90,224,200,.7)', cols: ['rgba(90,224,200,.65)', 'rgba(255,143,192,.6)', 'rgba(122,200,255,.65)'], pts: G.caps.map(([x, y, w, col], i) => [x, y - w * .15, w * 2.6, i * 1.3, .45, col === '#5ae0c8' ? 0 : col === '#ff8fc0' ? 1 : 2]) },
      { t: 'flies', c: 'rgba(200,255,150,.85)', pts: flies }] } });

/* ---- string lights between two houses ---- */
defScene({ id: 'fairylights', name: 'String Lights', mode: 'live', theme: 'dark', style: 'min', seed: 1406,
  init(r) { const a = [58, 408], b = [304, 426], sag = 44, bulbs = []; for (let i = 1; i < 13; i++) { const t = i / 13, x = lerp(a[0], b[0], t), y = lerp(a[1], b[1], t) + Math.sin(t * Math.PI) * sag; bulbs.push([x, y + 4]) } return { a, b, sag, bulbs } },
  st(c, r, G) { fsky(c, '#080b22', '#202854'); fmoon(c, 190, 150, 34); fstars(c, r, 120, 360, .7); const gy = 690;
    // houses: left and right, standing on the street; the string is tied to their eaves
    poly(c, [[-10, gy], [-10, 420], [0, 400], [70, 400], [80, 420], [80, gy]], '#0b1030'); poly(c, [[290, gy], [290, 440], [300, 418], [370, 418], [370, gy]], '#0b1030');
    poly(c, [[-14, 408], [34, 372], [86, 408]], '#060a1e'); poly(c, [[286, 428], [330, 392], [374, 428]], '#060a1e');
    [[14, 470], [14, 560], [320, 490], [320, 580]].forEach(([x, y]) => { c.fillStyle = '#ffcf80'; c.fillRect(x, y, 22, 28) });
    c.strokeStyle = '#05070f'; c.lineWidth = 1.4; c.beginPath(); c.moveTo(G.a[0], G.a[1]); c.quadraticCurveTo((G.a[0] + G.b[0]) / 2, (G.a[1] + G.b[1]) / 2 + G.sag * 2, G.b[0], G.b[1]); c.stroke();
    G.bulbs.forEach(([x, y], i) => { c.fillStyle = ['#ffd27a', '#ff9ab0', '#9ad0ff', '#b9f0a0'][i % 4]; c.beginPath(); c.arc(x, y, 3, 0, TAU); c.fill() });
    fillRidge(c, ridge(r, gy, 4, 1.2), '#0a1030'); fillRidge(c, ridge(r, 745, 4, 1.4), '#04061a') },
  fx(G) { const cols = ['rgba(255,210,122,.7)', 'rgba(255,154,176,.7)', 'rgba(154,208,255,.7)', 'rgba(185,240,160,.7)'];
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(61, 100, 340, away(190, 150, 44)) }, { t: 'glows', c: '#fff', cols, pts: G.bulbs.map(([x, y], i) => [x, y, 20, i * 1.7, .85, i % 4]) }] } });

/* ---- moonlit waterfall ---- */
defScene({ id: 'moonfalls', name: 'Moon Falls', mode: 'live', theme: 'dark', style: 'min', seed: 1407,
  st(c, r) { fsky(c, '#070b20', '#1d2a58'); fmoon(c, 180, 130, 40); fstars(c, r, 120, 280, .7);
    poly(c, [[-10, 600], [-10, 300], [60, 280], [150, 312], [150, 560], [150, 760], [-10, 780]], '#080d24'); poly(c, [[370, 600], [370, 300], [300, 286], [212, 314], [212, 560], [212, 760], [370, 780]], '#080d24');
    c.fillStyle = 'rgba(205,225,255,.14)'; c.fillRect(152, 314, 58, 270); const g = c.createLinearGradient(0, 600, 0, H); g.addColorStop(0, '#15234a'); g.addColorStop(1, '#070c24'); c.fillStyle = g; c.fillRect(0, 590, W, H - 590); poly(c, [[-10, 600], [150, 584], [212, 584], [370, 600], [370, 640], [-10, 640]], '#080d24');
    for (let i = 0; i < 22; i++) { c.fillStyle = 'rgba(180,205,255,' + (.06 + r() * .12) + ')'; c.fillRect(r() * 300, 610 + r() * 160, 12 + r() * 50, 1.4) } },
  fx() { const rr = rng(21), pts = Array.from({ length: 46 }, () => [152 + rr() * 56, rr() * 270, 22 + rr() * 38, 120 + rr() * 150, 0, 0]);
    return [{ t: 'stars', c: '#eaf0ff', pts: starPts(62, 100, 280, away(180, 130, 50)) }, { t: 'fall', m: 'rain', c: 'rgba(215,232,255,.55)', k: 0, top: 314, span: 270, pts }, { t: 'glows', c: 'rgba(200,225,255,.45)', pts: [[181, 596, 80, 0, .25], [150, 600, 46, 1, .3], [212, 600, 46, 2, .3]] }] } });

/* ---- moon phases ---- */
function phase(c, x, y, R, p) { c.fillStyle = '#0d1332'; c.beginPath(); c.arc(x, y, R, 0, TAU); c.fill(); c.strokeStyle = 'rgba(200,215,250,.25)'; c.lineWidth = 1.2; c.beginPath(); c.arc(x, y, R, 0, TAU); c.stroke();
  if (p <= 0) return; glow(c, x, y, R * 2.6, 'rgba(170,190,235,' + (.1 + .25 * p) + ')', 1); const g = c.createRadialGradient(x - R * .3, y - R * .3, R * .05, x, y, R); g.addColorStop(0, '#f4f7fd'); g.addColorStop(1, '#dde3f3'); c.fillStyle = g; c.beginPath(); c.moveTo(x, y - R); c.arc(x, y, R, -Math.PI / 2, Math.PI / 2, false);
  const rx = Math.abs(R * Math.cos(Math.PI * p)); if (p < .5) c.ellipse(x, y, rx, R, 0, Math.PI / 2, -Math.PI / 2, true); else c.ellipse(x, y, rx, R, 0, Math.PI / 2, Math.PI * 1.5, false); c.closePath(); c.fill() }
defScene({ id: 'phases', name: 'Moon Phases', mode: 'still', theme: 'dark', style: 'min', seed: 1408,
  st(c, r) { fsky(c, '#060a1c', '#141c40'); fstars(c, r, 130, 780, .6); [[0, 190], [.22, 320], [.5, 450], [.78, 580], [1, 710]].forEach(([p, y]) => phase(c, 180, y, 34, p));
    c.strokeStyle = 'rgba(200,215,250,.12)'; c.lineWidth = 1; c.beginPath(); c.moveTo(180, 70); c.lineTo(180, 760); c.stroke() } });

/* ---- long: harbour through a day and night ---- */
const LOOP = 75;
function waterSky(top, hor, wtop, wbot) { return c => { const g = c.createLinearGradient(0, 0, 0, 540); g.addColorStop(0, top); g.addColorStop(1, hor); c.fillStyle = g; c.fillRect(0, 0, W, 540); const w = c.createLinearGradient(0, 520, 0, H); w.addColorStop(0, wtop); w.addColorStop(1, wbot); c.fillStyle = w; c.fillRect(0, 520, W, H - 520) } }
function sailboat(c, x, wl, k, hull, sail) { poly(c, [[x - 24 * k, wl - 6 * k], [x + 26 * k, wl - 6 * k], [x + 18 * k, wl + 3 * k], [x - 16 * k, wl + 3 * k]], hull); c.fillStyle = hull; c.fillRect(x - 1 * k, wl - 62 * k, 2 * k, 56 * k); poly(c, [[x + 2 * k, wl - 60 * k], [x + 2 * k, wl - 10 * k], [x + 24 * k, wl - 10 * k]], sail); poly(c, [[x - 3 * k, wl - 52 * k], [x - 3 * k, wl - 10 * k], [x - 20 * k, wl - 10 * k]], sail) }
defScene({ id: 'harbour', name: 'Harbour Day and Night', mode: 'long', theme: 'dark', style: 'min', seed: 1409, loop: LOOP, poster: .08,
  init(r) { return { shore: ridge(r, 520, 8, 1.5), lamps: [[40, 640], [110, 650], [180, 660]] } },
  skyFrames: [{ p: 0, draw: waterSky('#070b1c', '#18223f', '#0e1833', '#04081a') }, { p: .22, draw: waterSky('#10183a', '#5a3a62', '#1a2048', '#0a0c26') }, { p: .36, draw: waterSky('#4a5a96', '#f0a77a', '#6a5a86', '#2a2a52') },
    { p: .5, draw: waterSky('#6f93c4', '#f6dca8', '#4a7a9a', '#1f3a58') }, { p: .72, draw: waterSky('#6a7aaa', '#f2b27c', '#5a6a8a', '#2a2a50') }, { p: .86, draw: waterSky('#2a2a5a', '#a05a78', '#2a2650', '#0c0c26') }],
  st(c, r, G) { const p = G.shore; c.beginPath(); c.moveTo(p[0][0], p[0][1]); for (let i = 1; i < p.length - 1; i++)c.quadraticCurveTo(p[i][0], p[i][1], (p[i][0] + p[i + 1][0]) / 2, (p[i][1] + p[i + 1][1]) / 2); c.lineTo(W + 10, 542); c.lineTo(-10, 542); c.closePath(); c.fillStyle = '#0a1028'; c.fill();
    // far shore town
    for (let x = 200; x < W; x += 12 + r() * 8) { const y = ridgeY(G.shore, x) + 2; poly(c, [[x, y], [x, y - 8 - r() * 8], [x + 5, y - 13 - r() * 7], [x + 10, y - 8 - r() * 8], [x + 10, y]], '#060a1c') }
        sailboat(c, 250, 610, 1.1, '#04060f', '#0b1230'); sailboat(c, 90, 580, .8, '#05081a', '#0d1436'); sailboat(c, 320, 560, .55, '#070b20', '#101a3c');
    // pier: planks on posts that go into the water
    poly(c, [[-10, 676], [210, 676], [210, 684], [-10, 684]], '#05070f'); for (let x = 6; x < 210; x += 36) poly(c, [[x, 684], [x, 740], [x + 5, 740], [x + 5, 684]], '#05070f'); },
  fx(G) { const sun = { t: 'orb', z: 0, r: 30, c: '#fff3d0', glow: 'rgba(255,214,150,.75)', hi: '#fffbea', warm: '#ffb070', x0: -40, x1: 400, y0: 530, yAmp: 400, a: .3, b: .88 };
    const moonA = { t: 'orb', z: 0, r: 36, c: '#e4e9f6', glow: 'rgba(170,190,235,.32)', craters: 1, x0: 400, x1: -40, y0: 530, yAmp: 360, a: .82, b: 1, q0: 0, q1: .5 }, moonB = { ...moonA, a: 0, b: .22, q0: .5, q1: .86 };
    return [{ t: 'stars', z: 0, c: '#eaf0ff', vis: [[0, 1], [.2, 1], [.36, 0], [.8, 0], [.92, 1], [1, 1]], pts: starPts(71, 130, 480) }, sun, moonA, moonB,
      { t: 'glows', c: 'rgba(255,200,120,.7)', vis: [[0, 1], [.25, 1], [.36, 0], [.78, 0], [.9, 1], [1, 1]], pts: G.lamps.map(([x, y], i) => [x, 668, 26, i * 1.1, .2]) }] } });
})();
