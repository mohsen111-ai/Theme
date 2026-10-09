/* Shared pieces for the generated scene families: palettes, small helpers, drawing wrappers that switch between flat "minimal" and ink-sketch rendering. */
(() => {
const { W, H, TAU, rng, lerp, mixh, glow, poly, ridge, fillRidge, ridgeY, fpine, fsky, fmoon, hill, pine, cottage, blob, ink, circ, starPts } = NYX;
const G = NYX.GEN = {};

/* [key, adjective, top, horizon, ground, window, moon tone] */
const D = [['indigo', 'Indigo', '#070c1e', '#1b2347', '#04081a'], ['plum', 'Plum', '#120a22', '#3a2447', '#0a0614'], ['teal', 'Teal', '#041a22', '#12404a', '#020d12'],
  ['forest', 'Forest', '#06140f', '#16382c', '#030a07'], ['ember', 'Ember', '#1a0b0b', '#4a2420', '#0c0505'], ['slate', 'Slate', '#0c1018', '#2a3446', '#06080e'],
  ['rose', 'Rosewood', '#1c0c1e', '#52294a', '#0e0610'], ['ocean', 'Deep Sea', '#04101f', '#0f3a5a', '#020a14'], ['violet', 'Violet', '#0e0a2a', '#3a2f7a', '#06041a'],
  ['olive', 'Olive', '#12140a', '#3a4224', '#080a04'], ['copper', 'Copper', '#1a0f08', '#5a3820', '#0c0704'], ['lav', 'Lavender', '#14102a', '#4a4278', '#0a081c'],
  ['jade', 'Jade', '#051418', '#1d4a46', '#030c0e'], ['wine', 'Wine', '#1a0610', '#4e1c34', '#0c030a']];
const L = [['peach', 'Peach', '#cdbfdc', '#f7dcc6', '#7b6a8c'], ['sage', 'Sage', '#c5d3d0', '#eee8d0', '#5f7a6c'], ['lilac', 'Lilac', '#c8c2e2', '#f0dcea', '#6e6492'],
  ['sand', 'Sand', '#d9cdb8', '#f4e6c8', '#8a7058'], ['powder', 'Powder', '#bcd0e4', '#f0e8e0', '#5a7292'], ['blush', 'Blush', '#e2c4cc', '#f8e4d8', '#8a5f72'],
  ['mint', 'Mint', '#bcd8d2', '#f0f0dc', '#5c8478'], ['apricot', 'Apricot', '#e2c8b4', '#f8e0b8', '#8c6a52'], ['mist', 'Mist', '#c4ccd8', '#ecece6', '#66748a']];
const mk = (a, light) => ({ key: a[0], adj: a[1], top: a[2], bot: a[3], ground: a[4], light, win: light ? '#f4d89a' : '#ffd48a', star: '#eaf0ff', line: light ? 'rgba(60,50,80,.55)' : 'rgba(190,195,240,.62)', hatch: light ? 'rgba(50,45,80,.5)' : 'rgba(190,195,240,.4)' });
G.dark = D.map(a => mk(a, false)); G.lightP = L.map(a => mk(a, true));

/* ink sketches use the same palettes but a little more paper-like (lighter, warmer horizon) */
G.pal = (light, i) => { const a = light ? G.lightP : G.dark; return a[((i % a.length) + a.length) % a.length] };

const rr = (r, a, b) => a + r() * (b - a);
G.rr = rr; G.pick = (r, a) => a[Math.floor(r() * a.length)];
G.lay = (P, k, n) => mixh(P.bot, P.ground, P.light ? .12 + .8 * (k + 1) / n : (k + 1) / n * .95);
G.tone = (P, k, n, d) => mixh(G.lay(P, k, n), P.ground, d);
const rgba = (hex, a) => { const m = hex.slice(1).match(/../g).map(h => parseInt(h, 16)); return 'rgba(' + m.join(',') + ',' + a + ')' };
G.rgba = rgba;

/* wrappers: flat vs ink */
G.fillLayer = (c, r, P, ink_, g, col) => ink_ ? hill(c, r, g, col, P.line, P.hatch) : fillRidge(c, g, col);
G.tree = (c, r, P, ink_, x, y, h, col) => ink_ ? pine(c, r, x, y, h, col, P.line) : fpine(c, x, y, h, col);
G.sky = (c, P, ink_) => { if (ink_) { const g = c.createLinearGradient(0, 0, 0, H); g.addColorStop(0, P.top); g.addColorStop(.6, mixh(P.top, P.bot, .8)); g.addColorStop(1, P.bot); c.fillStyle = g; c.fillRect(0, 0, W, H) } else fsky(c, P.top, P.bot) };
G.sun = (c, x, y, R, col = '#fff2d2') => { glow(c, x, y, R * 3.8, 'rgba(255,236,200,.5)', 1); const g = c.createRadialGradient(x - R * .3, y - R * .3, R * .05, x, y, R); g.addColorStop(0, '#fffaf0'); g.addColorStop(1, col); c.fillStyle = g; c.beginPath(); c.arc(x, y, R, 0, TAU); c.fill() };
G.sky_body = (c, r, P, ink_, x, y, R) => { if (P.light) G.sun(c, x, y, R * .8); else if (ink_) NYX.moon(c, r, x, y, R, '#f4eedc', 'rgba(210,225,255,.3)', 'rgba(205,205,190,.8)'); else fmoon(c, x, y, R) };
G.starsStatic = (c, r, P, n, ymax) => { if (!P.light) NYX.fstars(c, r, n, ymax, .6) };
G.house = (c, r, P, ink_, x, y, s, wall, roof) => { // base is at y+6 so the ground fill drawn afterwards hides the footing
  if (ink_) { const h = cottage(c, r, x, y + 4, s, wall, roof, P.line, P.light ? null : P.win); return { wins: h.wins, chim: h.chim, lit: h.wins.map(o => [o.x + o.w / 2, o.y + o.h / 2]) } }
  poly(c, [[x - 15 * s, y + 8], [x - 15 * s, y - 20 * s], [x + 15 * s, y - 20 * s], [x + 15 * s, y + 8]], wall);
  poly(c, [[x - 20 * s, y - 19 * s], [x, y - 38 * s], [x + 20 * s, y - 19 * s]], roof); c.fillStyle = wall; c.fillRect(x + 7 * s, y - 40 * s, 5 * s, 14 * s);
  c.fillStyle = P.light ? '#e6cf9a' : P.win; c.fillRect(x - 10 * s, y - 14 * s, 7 * s, 8 * s); c.fillRect(x + 3 * s, y - 14 * s, 7 * s, 8 * s);
  return { chim: [x + 9.5 * s, y - 40 * s], lit: [[x - 6.5 * s, y - 10 * s], [x + 6.5 * s, y - 10 * s]] } };

/* fx builders */
G.twinkle = (seed, n, ymax, avoid) => ({ t: 'stars', z: 1, c: '#eaf0ff', pts: starPts(seed, n, ymax, avoid) });
G.clouds = (P, r, n, y0, y1) => ({ t: 'clouds', z: 1, c: P.light ? 'rgba(255,248,240,.55)' : rgba(mixh(P.bot, '#8a90c0', .35), .3), items: Array.from({ length: n }, () => [r() * W, rr(r, y0, y1), rr(r, 90, 170), rr(r, 2.5, 7)]) });
G.birds = (P, r, n, y0, y1) => ({ t: 'birds', c: P.light ? '#4a4660' : '#0a0e22', items: Array.from({ length: n }, (_, i) => [r() * W, rr(r, y0, y1), rr(r, 12, 20), i * 1.3, rr(r, .6, 1)]) });
G.flies = (r, n, y0, y1) => ({ t: 'flies', c: 'rgba(210,255,150,.7)', c2: '#f4ffc8', pts: Array.from({ length: n }, () => [r() * W, rr(r, y0, y1), rr(r, 8, 24), rr(r, 6, 14), rr(r, .3, .7), r() * TAU, rr(r, .8, 2)]) });
G.shoot = (r) => ({ t: 'shoot', items: [[rr(r, 14, 22), r() * 10, .09, rr(r, 40, 220), rr(r, 50, 200), -90, 50, 54], [rr(r, 24, 34), r() * 20, .08, rr(r, 200, 340), rr(r, 80, 240), -80, 44, 48]] });
G.smoke = (P, x, y, i = 0) => ({ t: 'smoke', c: P.light ? '#cfc8d8' : '#d8d0e8', x, y, n: 6, drift: 18, rise: 70, r0: 3, grow: 8, speed: .08 + i * .01, a: .22 });
G.winGlow = (pts, a = .65) => ({ t: 'glows', c: 'rgba(255,190,100,' + a + ')', pts: pts.map((p, i) => [p[0], p[1], 20, i * 1.3, .22]) });
G.petals = (r, n, col) => ({ t: 'fall', m: 'petal', c: col, pts: Array.from({ length: n }, () => [r() * W, r() * 800, rr(r, 2, 4), rr(r, 14, 32), 0, r() * TAU]) });
G.snow = (r, n, col = 'rgba(240,246,255,.85)') => ({ t: 'fall', m: 'snow', c: col, pts: Array.from({ length: n }, () => [r() * W, r() * 800, rr(r, .8, 2.4), rr(r, 14, 42), r() * TAU, 0]) });
G.rain = (r, n, col) => ({ t: 'fall', m: 'rain', c: col, pts: Array.from({ length: n }, () => [r() * (W + 160), r() * 820, rr(r, 14, 26), rr(r, 320, 520), 0, 0]) });
G.lanterns = (r, n) => ({ t: 'fall', m: 'rise', c: '#ffd890', g: 'rgba(255,170,80,.7)', pts: Array.from({ length: n }, () => [r() * W, r() * 800, rr(r, 1.8, 3.2), rr(r, 10, 24), 0, r() * TAU]) });
G.at = (ridgeArr, x, d = 3) => ridgeY(ridgeArr, x) + d;
})();
