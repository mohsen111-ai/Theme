/* Long scenes: a full day and night cycle. Sky frames cross-fade over the loop, sun and moon cross the sky, the land stays put. */
(() => {
const { W, H, TAU, rng, lerp, mixh, glow, poly, ridge, fillRidge, ridgeY, fpine, hill, pine, rooftops, defScene, fsky, starPts } = NYX;
const GN = NYX.GEN, { rr, rgba, smoke } = GN;
const NIGHT = [[0, 1], [.2, 1], [.36, 0], [.8, 0], [.9, 1], [1, 1]], DAY = [[.3, 0], [.4, 1], [.62, 1], [.72, 0]];
/* sky sets: six frames = night, pre-dawn, sunrise, noon, sunset, dusk. land = [far, mid, near, front] silhouettes that read at every time of day */
const SETS = [
  { n: 'Classic', f: [['#070b1c', '#18223f'], ['#10183a', '#5a3a62'], ['#4a5a96', '#f0a77a'], ['#6f93c4', '#f6dca8'], ['#6a7aaa', '#f2b27c'], ['#2a2a5a', '#a05a78']], land: ['#2a3262', '#1c2348', '#0e1330', '#080b20'], sun: '#fff3d0' },
  { n: 'Rose', f: [['#140a22', '#3a2248'], ['#2a1840', '#a0507a'], ['#7a4a8a', '#ffb89a'], ['#9aa4d4', '#ffe4cc'], ['#8a5a8a', '#ff9a86'], ['#3a1f4a', '#a8486a']], land: ['#4a2a5a', '#34204a', '#1e1230', '#100820'], sun: '#fff0d8' },
  { n: 'Teal', f: [['#041a22', '#12404a'], ['#0a2c3a', '#3a6a7a'], ['#4a8a9a', '#f4c892'], ['#78b8c8', '#f4eed2'], ['#5a8a9a', '#f2b27c'], ['#0e3040', '#7a4a5a']], land: ['#1e4a58', '#143846', '#0a2230', '#04121a'], sun: '#fff4d8' },
  { n: 'Golden', f: [['#0e0e24', '#2a2840'], ['#26224a', '#8a5a66'], ['#8a6a8a', '#ffc27a'], ['#8ab0d0', '#fbe8b8'], ['#a8766a', '#ffb060'], ['#32244a', '#a05a4a']], land: ['#4a3a56', '#34284a', '#201830', '#120c1e'], sun: '#fff0c0' }];
const mkSky = SET => SET.f.map((f, i) => ({ p: [0, .22, .36, .5, .72, .86][i], draw: c => fsky(c, f[0], f[1]) }));
const LANDS = {
  valley: { nm: 'Valley', init(r) { const far = ridge(r, 520, 26, 1.1), mid = ridge(r, 600, 20, 1.5), near = ridge(r, 690, 14, 1.2), homes = []; for (let x = 150; x < 340; x += 30 + r() * 16) homes.push({ x, w: 20 + r() * 8, h: 14 + r() * 8 }); return { far, mid, near, homes } },
    st(c, r, G, S, ink) { const L = S.land; ink ? hill(c, r, G.far, L[0], 'rgba(200,205,245,.55)', 'rgba(200,205,245,.4)') : fillRidge(c, G.far, L[0]); ink ? hill(c, r, G.mid, L[1], 'rgba(200,205,245,.55)', 'rgba(200,205,245,.4)') : fillRidge(c, G.mid, L[1]);
      G.homes.forEach(h => { const y = ridgeY(G.near, h.x) + 4; poly(c, [[h.x, y], [h.x, y - h.h], [h.x + h.w / 2, y - h.h - 9], [h.x + h.w, y - h.h], [h.x + h.w, y]], L[3]); c.fillStyle = L[3]; c.fillRect(h.x + h.w * .65, y - h.h - 12, 4, 8) });
      ink ? hill(c, r, G.near, L[2], 'rgba(200,205,245,.55)', 'rgba(200,205,245,.4)') : fillRidge(c, G.near, L[2]); for (let x = 10; x < 150; x += 16 + r() * 12) ink ? pine(c, r, x, ridgeY(G.near, x) + 3, 30 + r() * 30, L[3], 'rgba(170,185,235,.5)') : fpine(c, x, ridgeY(G.near, x) + 3, 30 + r() * 30, L[3]) },
    lights(G) { return G.homes.map((h, i) => [h.x + h.w * .5, ridgeY(G.near, h.x) + 4 - h.h * .5, 18, h.x, .25]) }, smokes(G, S) { return G.homes.slice(0, 2).map((h, i) => ({ t: 'smoke', c: '#d8d0e8', x: h.x + h.w * .65 + 2, y: ridgeY(G.near, h.x) + 4 - h.h - 12, n: 6, drift: 18, rise: 70, r0: 3, grow: 8, speed: .08 + i * .01, a: .2 })) } },
  peaks: { nm: 'Peaks', init(r) { const mk = (n, a, b) => Array.from({ length: n }, (_, i) => ({ x: (i + .5) / n * W + rr(r, -30, 30), h: rr(r, a, b), w: rr(r, 90, 140) })); return { b: mk(4, 150, 250), m: mk(4, 90, 170), fr: ridge(r, 712, 14, 1.3) } },
    st(c, r, G, S, ink) { const L = S.land, py = (base, ps) => { const pts = []; for (let x = -10; x <= W + 10; x += 9) { let h = 0; ps.forEach(p => { h = Math.max(h, p.h * (1 - Math.abs(x - p.x) / p.w)) }); pts.push([x, base - h + Math.sin(x * .31) * 1.4]) } return pts };
      const f = (pts, col) => ink ? hill(c, r, pts, col, 'rgba(200,205,245,.5)', 'rgba(200,205,245,.35)') : fillRidge(c, pts, col); f(py(560, G.b), L[0]); f(py(640, G.m), L[1]); f(G.fr, L[3]); for (let x = 0; x < W + 10; x += 12 + r() * 12) ink ? pine(c, r, x, ridgeY(G.fr, x) + 3, 22 + r() * 34, L[3], 'rgba(170,185,235,.5)') : fpine(c, x, ridgeY(G.fr, x) + 3, 22 + r() * 34, L[3]) },
    lights() { return [] }, smokes() { return [] } },
  town: { nm: 'Town', init() { return {} }, st(c, r, G, S, ink) { const L = S.land, l = ink ? 'rgba(200,205,245,.5)' : null; rooftops(c, r, 575, L[0], l, null, 40, 105, 0); G.wins = rooftops(c, r, 670, L[2], l, null, 70, 170, .5); G.tops = G.wins.tops }, lights(G) { return (G.wins || []).filter((_, i) => i % 2 === 0).slice(0, 22).map((o, i) => [o.x + 4, o.y + 5, 11, i * 1.3, .18]) }, smokes(G) { return (G.tops || []).filter(t => t.chim).slice(0, 3).map((t, i) => ({ t: 'smoke', c: '#d8d0e8', x: t.chim[0] + 4, y: t.chim[1] - 2, n: 6, drift: 18, rise: 70, r0: 3, grow: 8, speed: .08 + i * .01, a: .2 })) } },
  dunes: { nm: 'Dunes', init(r) { return { rs: [rr(r, 470, 520), rr(r, 560, 600), rr(r, 640, 670), rr(r, 715, 740)].map((y, k) => ridge(r, y, 20 + k * 4, .6 + k * .12)) } },
    st(c, r, G, S, ink) { G.rs.forEach((g, k) => { if (k === 2) { const x = 150, y = ridgeY(g, x) + 5; poly(c, [[x - 42, y], [x - 6, y - 54], [x + 30, y]], S.land[3]) } ink ? hill(c, r, g, S.land[k], 'rgba(200,205,245,.5)', null) : fillRidge(c, g, S.land[k]) }) },
    lights() { return [] }, smokes() { return [] } },
  pines: { nm: 'Pines', init(r) { return { rs: [440, 505, 570, 640, 715].map((y, k) => ridge(r, y + rr(r, -10, 10), 10 + k * 2, 1 + k * .2)) } },
    st(c, r, G, S, ink) { const L = S.land; G.rs.forEach((g, k) => { const col = mixh(L[0], L[3], k / 4); ink ? hill(c, r, g, col, 'rgba(200,205,245,.45)', null) : fillRidge(c, g, col); for (let x = rr(r, -4, 8); x < W + 10; x += (7 + (4 - k) * 1.5) * (ink ? 2.2 : 1) + r() * 8) ink ? pine(c, r, x, ridgeY(g, x) + 3, 16 + k * 12 + r() * 18, mixh(col, L[3], .5), 'rgba(170,185,235,.45)') : fpine(c, x, ridgeY(g, x) + 3, 16 + k * 12 + r() * 18, mixh(col, L[3], .5)) }) },
    lights() { return [] }, smokes() { return [] } } };
let n = 0;
const plan = [['valley', 0, 0], ['peaks', 0, 0], ['town', 0, 1], ['dunes', 0, 0], ['pines', 0, 1], ['valley', 1, 0], ['peaks', 1, 1], ['town', 1, 0], ['dunes', 1, 0], ['pines', 1, 0], ['valley', 2, 1], ['peaks', 2, 0], ['town', 2, 0], ['dunes', 2, 1], ['pines', 2, 0], ['valley', 3, 0], ['peaks', 3, 0], ['town', 3, 1], ['dunes', 3, 0], ['pines', 3, 1]];
plan.forEach(([land, si, ink]) => { const LD = LANDS[land], S = SETS[si], seed = 7000 + n++;
  defScene({ id: 'long_' + land + '_' + S.n.toLowerCase(), name: S.n + ' ' + LD.nm + ' Day and Night', mode: 'long', theme: 'dark', style: ink ? 'ink' : 'min', seed, loop: 60, init: r => LD.init(r), skyFrames: mkSky(S), st: (c, r, G) => LD.st(c, r, G, S, ink),
    fx(G) { const sun = { t: 'orb', z: 0, r: 34, c: S.sun, glow: 'rgba(255,214,150,.75)', hi: '#fffbea', warm: '#ffb070', x0: -40, x1: 400, y0: 640, yAmp: 470, a: .3, b: .88 };
      const moonA = { t: 'orb', z: 0, r: 40, c: '#e4e9f6', glow: 'rgba(170,190,235,.32)', craters: 1, x0: 400, x1: -40, y0: 600, yAmp: 400, a: .82, b: 1, q0: 0, q1: .5 }, moonB = { ...moonA, a: 0, b: .22, q0: .5, q1: .86 };
      const out = [{ t: 'stars', z: 0, c: '#eaf0ff', vis: NIGHT, pts: starPts(seed, 140, 480) }, sun, moonA, moonB, { t: 'clouds', z: 0, c: 'rgba(255,255,255,.28)', vis: [[0, 0], [.4, 0], [.5, .5], [.7, .6], [.85, 0]], items: [[20, 160, 120, 6], [220, 250, 150, 4]] },
        { t: 'birds', c: '#1a1d3a', vis: DAY, items: [[40, 230, 18, 0, .9], [75, 250, 18, 1, .8], [110, 238, 18, 2, .7]] }];
      const lg = LD.lights(G); if (lg.length) out.push({ t: 'glows', c: 'rgba(255,190,100,.7)', vis: NIGHT, pts: lg }); LD.smokes(G, S).forEach(s => out.push(s)); return out } }) });
})();
