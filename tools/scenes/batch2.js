(() => {
const { W, H, TAU, rng, lerp, mixh, ink, blob, hatch, circ, glow, sky, poly, ridge, fillRidge, fridge, ridgeY, fpine, ftree, fsky, fmoon, fstars, starPts, defScene } = NYX;
const away = (x, y, r) => (px, py) => Math.hypot(px - x, py - y) < r;
const NIGHT = [[0, 1], [.2, 1], [.36, 0], [.8, 0], [.92, 1], [1, 1]];

/* ---- long: one night-to-day-to-night cycle over a valley village ---- */
defScene({ id: 'valleydawn', name: 'Valley Day and Night', mode: 'long', theme: 'dark', style: 'min', seed: 1201, loop: 60,
  init(r) { const far = ridge(r, 520, 26, 1.1), mid = ridge(r, 600, 20, 1.5), near = ridge(r, 690, 14, 1.2);
    const homes = []; for (let x = 150; x < 340; x += 30 + r() * 16) homes.push({ x, w: 20 + r() * 8, h: 14 + r() * 8 }); return { far, mid, near, homes } },
  skyFrames: [
    { p: 0, draw: (c) => fsky(c, '#070b1c', '#18223f') }, { p: .22, draw: (c) => fsky(c, '#10183a', '#5a3a62') }, { p: .36, draw: (c) => fsky(c, '#4a5a96', '#f0a77a') },
    { p: .5, draw: (c) => fsky(c, '#6f93c4', '#f6dca8') }, { p: .72, draw: (c) => fsky(c, '#6a7aaa', '#f2b27c') }, { p: .86, draw: (c) => fsky(c, '#2a2a5a', '#a05a78') }],
  st(c, r, G) { fillRidge(c, G.far, '#2a3262'); fillRidge(c, G.mid, '#1c2348');
    G.homes.forEach(h => { const y = ridgeY(G.near, h.x) + 4; poly(c, [[h.x, y], [h.x, y - h.h], [h.x + h.w / 2, y - h.h - 9], [h.x + h.w, y - h.h], [h.x + h.w, y]], '#0b1028'); c.fillStyle = '#0b1028'; c.fillRect(h.x + h.w * .65, y - h.h - 12, 4, 8) });
    fillRidge(c, G.near, '#0e1330'); for (let x = 10; x < 150; x += 16 + r() * 12) fpine(c, x, ridgeY(G.near, x) + 3, 30 + r() * 30, '#080b20') },
  fx(G) { const sun = { t: 'orb', z: 0, r: 34, c: '#fff3d0', glow: 'rgba(255,214,150,.75)', hi: '#fffbea', warm: '#ffb070', x0: -40, x1: 400, y0: 640, yAmp: 470, a: .3, b: .88 };
    const moonA = { t: 'orb', z: 0, r: 40, c: '#e4e9f6', glow: 'rgba(170,190,235,.32)', craters: 1, x0: 400, x1: -40, y0: 600, yAmp: 400, a: .82, b: 1, q0: 0, q1: .5 }, moonB = { ...moonA, a: 0, b: .22, q0: .5, q1: .86 };
    const win = []; G.homes.forEach(h => { const y = ridgeY(G.near, h.x) + 4; win.push([h.x + h.w * .5, y - h.h * .5, 18, h.x, .25]) });
    return [{ t: 'stars', z: 0, c: '#eaf0ff', vis: NIGHT, pts: starPts(21, 140, 480) }, sun, moonA, moonB,
      { t: 'clouds', z: 0, c: '#8a8fb8', vis: [[0, 0], [.4, 0], [.5, .5], [.7, .6], [.85, 0]], items: [[20, 160, 120, 6], [220, 250, 150, 4]] },
      { t: 'birds', c: '#1a1d3a', vis: [[.3, 0], [.4, 1], [.62, 1], [.72, 0]], items: [[40, 230, 18, 0, .9], [75, 250, 18, 1, .8], [110, 238, 18, 2, .7]] },
      { t: 'glows', c: 'rgba(255,190,100,.7)', vis: [[0, 1], [.25, 1], [.36, 0], [.78, 0], [.9, 1], [1, 1]], pts: win }] } });

/* ---- ink style: bookshop front ---- */
defScene({ id: 'bookshop', name: 'Late Bookshop', mode: 'still', theme: 'dark', style: 'ink', seed: 1202,
  st(c, r) { const L = 'rgba(190,190,235,.7)'; sky(c, [[0, '#0b1030'], [.5, '#212857'], [1, '#2a2450']]); fstars(c, r, 90, 360, .6); fmoon(c, 300, 130, 30);
    const gy = 650;
    // neighbours
    blob(c, r, [[-10, gy], [-10, 420], [40, 394], [92, 420], [92, gy]], '#161a40', L, 1.1, 1.2); blob(c, r, [[290, gy], [290, 440], [330, 414], [372, 440], [372, gy]], '#161a40', L, 1.1, 1.2);
    [[10, 450], [10, 520], [310, 470], [310, 540]].forEach(([x, y]) => { c.fillStyle = '#ffd890'; c.fillRect(x, y, 14, 20); ink(c, r, [[x, y], [x + 14, y], [x + 14, y + 20], [x, y + 20]], L, .9, .6, true, 1) });
    // shop
    blob(c, r, [[78, gy], [78, 380], [292, 380], [292, gy]], '#1e2250', L, 1.3, 1.4); blob(c, r, [[70, 380], [185, 340], [300, 380]], '#141738', L, 1.3, 1.2);
    [[100, 410], [236, 410]].forEach(([x, y]) => { c.fillStyle = '#ffd890'; c.fillRect(x, y, 34, 46); ink(c, r, [[x, y], [x + 34, y], [x + 34, y + 46], [x, y + 46]], L, 1.1, .6, true, 1); ink(c, r, [[x + 17, y], [x + 17, y + 46]], L, .8, .4, false, 1); ink(c, r, [[x, y + 23], [x + 34, y + 23]], L, .8, .4, false, 1); glow(c, x + 17, y + 23, 40, 'rgba(255,190,100,.4)', .8) });
    // sign + awning
    blob(c, r, [[130, 466], [240, 466], [240, 492], [130, 492]], '#2a1f3a', L, 1.1, .8); c.fillStyle = '#ffd890'; c.fillRect(150, 475, 28, 12); c.fillRect(182, 475, 28, 12); c.fillStyle = '#2a1f3a'; c.fillRect(178, 473, 4, 16);
    const stripes = 8, ax = 88, aw = 194; for (let i = 0; i < stripes; i++) { poly(c, [[ax + aw / stripes * i, 506], [ax + aw / stripes * (i + 1), 506], [ax + aw / stripes * (i + 1) + 3, 540], [ax + aw / stripes * i + 3, 540]], i % 2 ? '#d9cdb0' : '#7a2f45') } ink(c, r, [[ax, 506], [ax + aw, 506], [ax + aw + 3, 540], [ax + 3, 540]], L, 1.2, 1, true, 1);
    // window with shelves, door
    blob(c, r, [[100, 546], [100, 640], [210, 640], [210, 546]], '#3a2a2a', L, 1.2, 1); c.fillStyle = '#ffdf9a'; c.fillRect(104, 550, 102, 86); glow(c, 155, 600, 90, 'rgba(255,200,120,.45)', .9);
    [574, 604].forEach(y => { ink(c, r, [[104, y], [206, y]], '#4a321e', 2, .6, false, 1); let x = 108; while (x < 200) { const w = 4 + r() * 5, h = 14 + r() * 10; c.fillStyle = ['#8a3a3a', '#3a6a8a', '#5a8a4a', '#c9a24a', '#6a4a8a'][Math.floor(r() * 5)]; c.fillRect(x, y - h, w, h); x += w + 1 } });
    blob(c, r, [[222, 640], [222, 560], [262, 560], [262, 640]], '#5a2f3a', L, 1.2, 1); c.fillStyle = '#ffdf9a'; c.fillRect(228, 568, 28, 36); ink(c, r, [[228, 568], [256, 568], [256, 604], [228, 604]], L, .9, .5, true, 1);
    // pavement + lamp
    blob(c, r, [[-10, gy], [370, gy], [370, 700], [-10, 700]], '#2a2d5c', L, 1.2, 1.2); blob(c, r, [[-10, 700], [370, 700], [370, H + 10], [-10, H + 10]], '#10142e', null);
    ink(c, r, [[40, 660], [40, 560]], '#05060f', 3, .6, false, 1); blob(c, r, [[32, 560], [48, 560], [50, 540], [30, 540]], '#ffd890', '#05060f', 1.2, .5); glow(c, 40, 548, 80, 'rgba(255,200,110,.7)', .9);
    for (let i = 0; i < 6; i++) ink(c, r, [[60 + i * 52 + r() * 10, 730 + r() * 30], [90 + i * 52 + r() * 10, 732 + r() * 30]], 'rgba(255,214,150,.18)', 2, 1, false, 1) } });
})();
