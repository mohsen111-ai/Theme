/* Nyx scene engine.
 * Shared by the preview page, the content pipeline (Playwright) and, through the JSON it emits, the Android app.
 * Logical canvas is 360x780. A scene is: static layer(s) drawn once (baked with grain) + declarative effects ("fx")
 * that are replayed every frame. The effect maths below is mirrored 1:1 in app/.../FxRenderer.kt. Keep them in sync.
 */
const NYX = (() => {
"use strict";
const W = 360, H = 780, TAU = Math.PI * 2;
const rng = s => { let a = s >>> 0; return () => { a = (a + 0x6D2B79F5) >>> 0; let t = a; t = Math.imul(t ^ t >>> 15, t | 1); t ^= t + Math.imul(t ^ t >>> 7, t | 61); return ((t ^ t >>> 14) >>> 0) / 4294967296 } };
const lerp = (a, b, t) => a + (b - a) * t, clamp = (x, a = 0, b = 1) => Math.min(b, Math.max(a, x)), smooth = t => t * t * (3 - 2 * t);
const mod = (a, b) => ((a % b) + b) % b;
const hex = h => { h = h.replace('#', ''); if (h.length === 3) h = h.split('').map(x => x + x).join(''); return [parseInt(h.slice(0, 2), 16), parseInt(h.slice(2, 4), 16), parseInt(h.slice(4, 6), 16)] };
const mixh = (a, b, t) => { const A = hex(a), B = hex(b); return '#' + A.map((v, i) => Math.round(lerp(v, B[i], t)).toString(16).padStart(2, '0')).join('') };
const ramp2 = (keys, p) => { for (let i = 0; i < keys.length - 1; i++) { const [a, ca] = keys[i], [b, cb] = keys[i + 1]; if (p >= a && p <= b) return mixh(ca, cb, smooth((p - a) / (b - a))) } return keys[keys.length - 1][1] };
function parseCss(s) { s = s.trim(); if (s[0] === '#') { const [r, g, b] = hex(s); return [r, g, b, 1] } const m = s.match(/rgba?\(([^)]+)\)/); const p = m[1].split(',').map(Number); return [p[0], p[1], p[2], p.length > 3 ? p[3] : 1] }

/* ---------- pen (sketchy ink look) ---------- */
function sub(pts, step) { const o = []; for (let i = 0; i < pts.length - 1; i++) { const [x0, y0] = pts[i], [x1, y1] = pts[i + 1]; const n = Math.max(1, Math.round(Math.hypot(x1 - x0, y1 - y0) / step)); for (let k = 0; k < n; k++)o.push([lerp(x0, x1, k / n), lerp(y0, y1, k / n)]) } o.push(pts[pts.length - 1]); return o }
function path(c, q, close) { c.moveTo(q[0][0], q[0][1]); for (let i = 1; i < q.length - 1; i++) { c.quadraticCurveTo(q[i][0], q[i][1], (q[i][0] + q[i + 1][0]) / 2, (q[i][1] + q[i + 1][1]) / 2) } const l = q[q.length - 1]; c.lineTo(l[0], l[1]); if (close) c.closePath() }
function wob(r, pts, a, step = 14) { return sub(pts, step).map(p => [p[0] + (r() - .5) * a, p[1] + (r() - .5) * a]) }
function ink(c, r, pts, col, w = 1.2, a = 1.6, close = false, passes = 2) { for (let k = 0; k < passes; k++) { c.beginPath(); path(c, wob(r, close ? pts.concat([pts[0]]) : pts, a), close); c.strokeStyle = col; c.lineWidth = k ? w * .55 : w; c.lineCap = c.lineJoin = 'round'; c.globalAlpha = k ? .55 : 1; c.stroke() } c.globalAlpha = 1 }
function blob(c, r, pts, fill, line, w = 1.2, a = 1.4) { c.beginPath(); path(c, wob(r, pts.concat([pts[0]]), a * .6), true); c.fillStyle = fill; c.fill(); if (line) ink(c, r, pts, line, w, a, true) }
function hatch(c, r, clip, x0, y0, x1, y1, ang, gap, col, w = .8, al = .5) { c.save(); c.beginPath(); clip(); c.clip(); c.strokeStyle = col; c.lineWidth = w; c.globalAlpha = al; c.lineCap = 'round'; const cx = (x0 + x1) / 2, cy = (y0 + y1) / 2, R = Math.hypot(x1 - x0, y1 - y0) / 2, ca = Math.cos(ang), sa = Math.sin(ang); for (let d = -R; d < R; d += gap) { const j = (r() - .5) * gap * .5, px = cx - sa * (d + j), py = cy + ca * (d + j), l = R * (.75 + r() * .25); c.beginPath(); c.moveTo(px - ca * l, py - sa * l); c.lineTo(px + ca * l + (r() - .5) * 3, py + sa * l + (r() - .5) * 3); c.stroke() } c.restore() }
const circ = (x, y, rad, n = 18) => { const p = []; for (let i = 0; i <= n; i++) { const a = i / n * TAU; p.push([x + Math.cos(a) * rad, y + Math.sin(a) * rad]) } return p };
function glow(c, x, y, rad, col, al = 1) { c.save(); c.globalCompositeOperation = 'screen'; c.globalAlpha = al; const g = c.createRadialGradient(x, y, 0, x, y, rad); g.addColorStop(0, col); g.addColorStop(1, 'rgba(0,0,0,0)'); c.fillStyle = g; c.fillRect(x - rad, y - rad, rad * 2, rad * 2); c.restore() }
function sky(c, stops) { const g = c.createLinearGradient(0, 0, 0, H); stops.forEach(([p, col]) => g.addColorStop(p, col)); c.fillStyle = g; c.fillRect(0, 0, W, H) }
function poly(c, pts, fill) { c.beginPath(); c.moveTo(pts[0][0], pts[0][1]); for (let i = 1; i < pts.length; i++)c.lineTo(pts[i][0], pts[i][1]); c.closePath(); c.fillStyle = fill; c.fill() }
function ridge(r, y, amp, sc) { const f = [[1, r() * TAU], [2.3, r() * TAU], [5.1, r() * TAU]], pts = []; for (let x = -10; x <= W + 10; x += 12) { const u = x / W * TAU * sc; pts.push([x, y + (Math.sin(u * f[0][0] + f[0][1]) + Math.sin(u * f[1][0] + f[1][1]) * .5 + Math.sin(u * f[2][0] + f[2][1]) * .22) * amp]) } return pts }
/* smooth filled ridge down to the bottom edge; returns the ridge points (use ridgeY to sit things on it) */
function fillRidge(c, p, col) { c.beginPath(); c.moveTo(p[0][0], p[0][1]); for (let i = 1; i < p.length - 1; i++)c.quadraticCurveTo(p[i][0], p[i][1], (p[i][0] + p[i + 1][0]) / 2, (p[i][1] + p[i + 1][1]) / 2); c.lineTo(W + 10, H + 10); c.lineTo(-10, H + 10); c.closePath(); c.fillStyle = col; c.fill() }
function fridge(c, r, y, amp, sc, col) { const p = ridge(r, y, amp, sc); c.beginPath(); c.moveTo(p[0][0], p[0][1]); for (let i = 1; i < p.length - 1; i++)c.quadraticCurveTo(p[i][0], p[i][1], (p[i][0] + p[i + 1][0]) / 2, (p[i][1] + p[i + 1][1]) / 2); c.lineTo(W + 10, H + 10); c.lineTo(-10, H + 10); c.closePath(); c.fillStyle = col; c.fill(); return p }
/* y of a ridge polyline at x (linear interpolation) - used to stand objects on the ground */
function ridgeY(p, x) { for (let i = 0; i < p.length - 1; i++) if (x >= p[i][0] && x <= p[i + 1][0]) return lerp(p[i][1], p[i + 1][1], (x - p[i][0]) / (p[i + 1][0] - p[i][0])); return p[p.length - 1][1] }
function hill(c, r, pts, fill, line, hcol, band = 46) { const poly_ = pts.concat([[W + 10, H + 10], [-10, H + 10]]); blob(c, r, poly_, fill, null); ink(c, r, pts, line, 1.3, 2, false, 2); if (hcol) { const lo = pts.map(p => [p[0], p[1] + band]).reverse(), b = pts.concat(lo); const ys = pts.map(p => p[1]); hatch(c, r, () => path(c, b, true), 0, Math.min(...ys), W, Math.max(...ys) + band, -.7, 6, hcol, .7, .3) } }
function pine(c, r, x, y, h, fill, line) { const w = h * .3, t = 4, L = [[x, y - h]], R = []; for (let i = 1; i <= t; i++) { const yy = y - h + h * i / t * .92, ww = w * i / t; L.push([x - ww * .55, yy - h * .04], [x - ww, yy]); R.push([x + ww * .55, yy - h * .04], [x + ww, yy]) } blob(c, r, L.concat([[x - 2, y], [x + 2, y]]).concat(R.reverse()), fill, line, 1, 1) }
function fpine(c, x, y, h, col) { const w = h * .3, t = 5, L = [], R = []; for (let i = 1; i <= t; i++) { const yy = y - h + h * i / t, ww = w * i / t; L.push([x - ww * .5, yy - h * .12], [x - ww, yy]); R.push([x + ww * .5, yy - h * .12], [x + ww, yy]) } poly(c, [[x, y - h]].concat(L, [[x - 3, y + 2], [x + 3, y + 2]], R.reverse()), col) }
function roundTree(c, r, x, y, h, fill, line, trunk) { ink(c, r, [[x, y], [x + 1, y - h * .5]], trunk, 3, .8, false, 1); const rad = h * .34; blob(c, r, circ(x, y - h * .66, rad, 12).map((p, i) => [p[0] + Math.sin(i * 2.1) * rad * .12, p[1] + Math.cos(i * 1.7) * rad * .1]), fill, line, 1, 1.2) }
/* flat (no outline) round tree for the minimal style */
function ftree(c, x, y, h, col, trunk) { c.fillStyle = trunk || col; c.fillRect(x - 2, y - h * .45, 4, h * .45 + 1); c.fillStyle = col; [[0, -.68, .3], [-.16, -.58, .22], [.17, -.6, .23], [0, -.82, .2]].forEach(([a, b, k]) => { c.beginPath(); c.arc(x + a * h, y + b * h, k * h, 0, TAU); c.fill() }) }
function stars(r, n, ymax) { return Array.from({ length: n }, () => ({ x: r() * W, y: r() * ymax, s: .4 + r() * 1.1, p: r() * TAU, sp: .6 + r() * 1.8, big: r() < .1 })) }
function drawStar(c, s, col, al) { c.globalAlpha = al; c.fillStyle = col; if (s.big) { c.strokeStyle = col; c.lineWidth = .7; c.beginPath(); c.moveTo(s.x - s.s * 3.2, s.y); c.lineTo(s.x + s.s * 3.2, s.y); c.moveTo(s.x, s.y - s.s * 3.2); c.lineTo(s.x, s.y + s.s * 3.2); c.stroke() } c.beginPath(); c.arc(s.x, s.y, s.s * .8, 0, TAU); c.fill(); c.globalAlpha = 1 }
/* static starfield */
function fstars(c, r, n, ymax, a = .6) { for (let i = 0; i < n; i++) { c.fillStyle = 'rgba(235,240,255,' + (.2 + r() * a) + ')'; c.beginPath(); c.arc(r() * W, r() * ymax, .4 + r() * .9, 0, TAU); c.fill() } }
/* star list for the twinkle effect: [x,y,size,phase,speed,big]; avoid(x,y) returns true to skip a point */
function starPts(seed, n, ymax, avoid) { const r = rng(seed), o = []; while (o.length < n) { const x = r() * W, y = r() * ymax, s = +(.4 + r() * 1.1).toFixed(2), p = +(r() * TAU).toFixed(2), sp = +(.6 + r() * 1.8).toFixed(2), b = r() < .1 ? 1 : 0; if (avoid && avoid(x, y)) continue; o.push([+x.toFixed(1), +y.toFixed(1), s, p, sp, b]) } return o }
function fsky(c, a, b) { const g = c.createLinearGradient(0, 0, 0, H); g.addColorStop(0, a); g.addColorStop(1, b); c.fillStyle = g; c.fillRect(0, 0, W, H) }
function fmoon(c, x, y, R, tone = '#e4e9f6', gl = 'rgba(170,190,235,.32)') { glow(c, x, y, R * 3.6, gl, 1); const g = c.createRadialGradient(x - R * .3, y - R * .3, R * .05, x, y, R); g.addColorStop(0, '#f4f7fd'); g.addColorStop(1, tone); c.fillStyle = g; c.beginPath(); c.arc(x, y, R, 0, TAU); c.fill(); c.fillStyle = 'rgba(120,135,180,.07)';[[.3, -.2, .22], [-.25, .25, .3], [.1, .45, .14], [-.4, -.3, .12]].forEach(([a, b, k]) => { c.beginPath(); c.arc(x + a * R, y + b * R, k * R, 0, TAU); c.fill() }) }
function moon(c, r, x, y, R, col, gl, line) { glow(c, x, y, R * 4, gl, 1); c.beginPath(); c.arc(x, y, R, 0, TAU); c.fillStyle = col; c.fill(); for (let i = 0; i < 7; i++) { const a = r() * TAU, d = r() * R * .62, rr = R * (.07 + r() * .13); c.beginPath(); c.arc(x + Math.cos(a) * d, y + Math.sin(a) * d, rr, 0, TAU); c.fillStyle = 'rgba(70,60,100,.12)'; c.fill() } for (let k = 0; k < 2; k++)ink(c, r, circ(x, y, R + k * 1.6, 26), line, 1, 1.6, true, 1); hatch(c, r, () => c.arc(x, y, R, 0, TAU), x - R, y - R, x + R, y + R, .8, 4, line, .6, .2) }
function cloud(c, r, x, y, w, fill, line) { const n = Math.max(3, Math.round(w / 24)), pts = []; c.fillStyle = fill; for (let i = 0; i < n; i++) { const cx = x + (i + .5) / n * w, rad = w / n * (.75 + r() * .35) * (1 - Math.abs(i / (n - 1) - .5) * .5), cy = y - rad * .35; c.beginPath(); c.arc(cx, cy, rad, 0, TAU); c.fill(); for (let k = (i > 0 ? 2 : 0); k <= (i < n - 1 ? 6 : 8); k++) { const a = Math.PI + k / 8 * Math.PI; pts.push([cx + Math.cos(a) * rad, cy + Math.sin(a) * rad]) } } c.fillRect(x, y - 4, w, 10); ink(c, r, pts, line, 1, 1.2, false, 1) }
function cottage(c, r, x, y, s, wall, roof, line, win, snow) { const w = 60 * s, h = 38 * s, rh = 30 * s, L = x - w / 2, R = x + w / 2, T = y - h;
  blob(c, r, [[L, y], [L, T], [R, T], [R, y]], wall, line);
  blob(c, r, [[R - 16 * s, T - rh * .45], [R - 16 * s, T - rh * .95], [R - 8 * s, T - rh * .95], [R - 8 * s, T - rh * .3]], wall, line);
  blob(c, r, [[L - 6 * s, T + 2], [x, T - rh], [R + 6 * s, T + 2]], roof, line, 1.3);
  if (snow) blob(c, r, [[L - 6 * s, T + 2], [x, T - rh], [R + 6 * s, T + 2], [R + 2 * s, T + 6 * s], [x, T - rh + 8 * s], [L - 2 * s, T + 6 * s]], '#d6def2', null);
  const dw = 9 * s; blob(c, r, [[x - dw / 2, y], [x - dw / 2, y - 18 * s], [x + dw / 2, y - 18 * s], [x + dw / 2, y]], roof, line);
  const wins = [{ x: L + 8 * s, y: T + 9 * s, w: 11 * s, h: 12 * s }, { x: R - 19 * s, y: T + 9 * s, w: 11 * s, h: 12 * s }];
  wins.forEach(o => { if (win) { c.fillStyle = win; c.fillRect(o.x, o.y, o.w, o.h) } ink(c, r, [[o.x, o.y], [o.x + o.w, o.y], [o.x + o.w, o.y + o.h], [o.x, o.y + o.h]], line, 1, .8, true, 1); ink(c, r, [[o.x + o.w / 2, o.y], [o.x + o.w / 2, o.y + o.h]], line, .7, .4, false, 1); ink(c, r, [[o.x, o.y + o.h / 2], [o.x + o.w, o.y + o.h / 2]], line, .7, .4, false, 1) });
  return { wins, chim: [R - 12 * s, T - rh * .95] } }
/* row of houses standing on baseY. flat=[indices forced to flat roofs]. returns lit windows; .tops has roof geometry for placing things on roofs */
function rooftops(c, r, baseY, col, line, win, hmin, hmax, lit, flat = []) { let x = -10, idx = 0; const wins = [], tops = []; while (x < W + 10) { const w = 46 + r() * 44, h = hmin + r() * (hmax - hmin), rh = 18 + r() * 24, top = baseY - h, isFlat = flat.includes(idx), pitched = !isFlat && r() < .72;
  const pts = pitched ? [[x, baseY + 420], [x, top], [x + w / 2, top - rh], [x + w, top], [x + w, baseY + 420]] : [[x, baseY + 420], [x, top], [x + w, top], [x + w, baseY + 420]]; blob(c, r, pts, col, line, 1.1, 1.2); tops.push({ x, y: top, w, pitched, apexY: top - rh, idx });
  if (!isFlat && r() < .5) { const cx = x + w * (.2 + r() * .5); blob(c, r, [[cx, top - 4], [cx, top - rh - 4], [cx + 8, top - rh - 4], [cx + 8, top - 4]], col, line, 1, .8); tops[tops.length - 1].chim = [cx + 4, top - rh - 4] }
  const cs = Math.max(1, Math.floor((w - 8) / 16)), rs = Math.floor((h - 14) / 26); for (let j = 0; j < rs; j++)for (let i = 0; i < cs; i++) { const wx = x + 6 + i * 16, wy = top + 10 + j * 26; if (r() < lit) { if (win) { c.fillStyle = win; c.fillRect(wx, wy, 8, 11) } wins.push({ x: wx, y: wy, w: 8, h: 11 }) } else if (line) ink(c, r, [[wx, wy], [wx + 8, wy], [wx + 8, wy + 11], [wx, wy + 11]], line, .7, .5, true, 1) }
  x += w - 2 + r() * 6; idx++ } wins.tops = tops; return wins }
function crow(c, r, x, y, s, col, flip, rim) { c.save(); c.translate(x, y); c.scale(s * flip, s);
  blob(c, r, [[-24, 0], [-12, -12], [-4, -20], [4, -24], [9, -31], [16, -33], [24, -29], [17, -28], [13, -24], [10, -14], [4, -6], [-2, -2], [-10, 0]], col, null, 1, .6);
  ink(c, r, [[-12, -12], [-4, -20], [4, -24], [9, -31], [16, -33]], rim, .8, .5, false, 1); ink(c, r, [[0, -4], [0, 4]], col, 1.2, .3, false, 1); ink(c, r, [[5, -8], [5, 2]], col, 1.2, .3, false, 1);
  c.fillStyle = rim; c.beginPath(); c.arc(14, -29, .9, 0, TAU); c.fill(); c.restore() }
function cat(c, r, x, y, s, col, rim, eye) { c.save(); c.translate(x, y); c.scale(s, s);
  blob(c, r, [[-5, -20], [-9, -10], [-12, 0], [12, 0], [9, -10], [5, -20]], col, null, 1, .6);
  blob(c, r, [[-7, -24], [-8, -33], [-3, -29], [3, -29], [8, -33], [7, -24], [5, -20], [-5, -20]], col, null, 1, .6);
  ink(c, r, [[10, -1], [18, -3], [22, -10], [20, -18], [16, -22]], col, 3.2, .5, false, 1);
  ink(c, r, [[-8, -33], [-3, -29], [3, -29], [8, -33]], rim, .7, .4, false, 1);
  if (eye) { glow(c, -3, -25, 5, eye, .8); glow(c, 3, -25, 5, eye, .8) } c.restore() }
/* dead tree rooted at (x,y); returns perch point on a near-horizontal branch */
function bareTree(c, r, x, y, h, col) { const lean = 14, tx = f => x + lean * f, ty = f => y - h * f;
  blob(c, r, [[x - 10, y + 4], [x - 5, ty(.4)], [tx(1) - 3, ty(1)], [tx(1) + 3, ty(1)], [x + 5, ty(.4)], [x + 10, y + 4]], col, null, 1, .6);
  const B = [[.34, 1, -.16, 130, 6], [.52, -1, -.5, 95, 5], [.7, 1, -.62, 80, 4], [.84, -1, -.85, 60, 3]]; let perch = null;
  B.forEach(([f, sd, an, len, w], k) => { const a = sd > 0 ? an : Math.PI - an, x0 = tx(f), y0 = ty(f), x1 = x0 + Math.cos(a) * len, y1 = y0 + Math.sin(a) * len;
    ink(c, r, [[x0, y0], [(x0 + x1) / 2, (y0 + y1) / 2 + (r() - .5) * 6], [x1, y1]], col, w, .6, false, 1);
    for (const q of [.45, .75]) { const bx = lerp(x0, x1, q), by = lerp(y0, y1, q), ta = a + (r() < .5 ? -.7 : .7); ink(c, r, [[bx, by], [bx + Math.cos(ta) * 34, by + Math.sin(ta) * 34]], col, w * .55, .6, false, 1) }
    if (k === 0) perch = { x: lerp(x0, x1, .7), y: lerp(y0, y1, .7) - w * .35 } });
  return { perch } }

/* ---------- effects (declarative; mirrored in FxRenderer.kt) ----------
 * Common fields: z (0 = behind the background image, 1 = in front; default 1), vis [[p,alpha],...] over the scene loop, a (alpha).
 * Colors are CSS strings (#rgb, #rrggbb, rgb(), rgba()). Coordinates are logical (360x780).
 * Shapes (mover/spin): ['r',x,y,w,h,c] ['c',x,y,r,c] ['p',[x,y,x,y,...],c] ['l',x1,y1,x2,y2,w,c] ['g',x,y,r,c,a] glow ['e',x,y,rx,ry,rot,c] */
function visAt(vis, p) { if (!vis) return 1; if (p <= vis[0][0]) return vis[0][1]; for (let i = 0; i < vis.length - 1; i++) { const [a, va] = vis[i], [b, vb] = vis[i + 1]; if (p <= b) return va + (vb - va) * ((p - a) / (b - a)) } return vis[vis.length - 1][1] }
function drawShapes(c, shapes) { for (const s of shapes) { switch (s[0]) {
  case 'r': c.fillStyle = s[5]; c.fillRect(s[1], s[2], s[3], s[4]); break;
  case 'c': c.fillStyle = s[4]; c.beginPath(); c.arc(s[1], s[2], s[3], 0, TAU); c.fill(); break;
  case 'p': { const q = s[1]; c.fillStyle = s[2]; c.beginPath(); c.moveTo(q[0], q[1]); for (let i = 2; i < q.length; i += 2)c.lineTo(q[i], q[i + 1]); c.closePath(); c.fill(); break }
  case 'l': c.strokeStyle = s[6]; c.lineWidth = s[5]; c.lineCap = 'round'; c.beginPath(); c.moveTo(s[1], s[2]); c.lineTo(s[3], s[4]); c.stroke(); break;
  case 'g': glow(c, s[1], s[2], s[3], s[4], s[5] == null ? 1 : s[5]); break;
  case 'e': c.fillStyle = s[6]; c.beginPath(); c.ellipse(s[1], s[2], s[3], s[4], s[5], 0, TAU); c.fill(); break } } }
const FX = {
  stars(c, e, t, x) { const va = visAt(e.vis, x.p) * (e.a == null ? 1 : e.a); if (va <= .003) return; for (const [X, Y, S, P, SP, B] of e.pts) { c.globalAlpha = (.3 + .6 * (.5 + .5 * Math.sin(t * SP + P))) * va; c.fillStyle = e.c; if (B) { c.strokeStyle = e.c; c.lineWidth = .7; c.beginPath(); c.moveTo(X - S * 3.2, Y); c.lineTo(X + S * 3.2, Y); c.moveTo(X, Y - S * 3.2); c.lineTo(X, Y + S * 3.2); c.stroke() } c.beginPath(); c.arc(X, Y, S * .8, 0, TAU); c.fill() } c.globalAlpha = 1 },
  glows(c, e, t, x) { const va = visAt(e.vis, x.p) * (e.a == null ? 1 : e.a); if (va <= .003) return; for (const g of e.pts) { const [X, Y, R, P, A] = g, fl = .5 + .5 * Math.sin(t * 7 + P) * Math.sin(t * 3.1 + P); glow(c, X, Y, R, (e.cols && g[5] != null) ? e.cols[g[5]] : e.c, va * (1 - A + A * fl)) } },
  clouds(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; c.globalAlpha = va; c.fillStyle = e.c; for (const [X0, Y, w, sp] of e.items) { const X = mod(X0 + t * sp + w, W + 2 * w) - w, h = w * .22;[[.28, -.1, .62], [.52, -.5, .9], [.76, -.12, .66]].forEach(([a, b, k]) => { c.beginPath(); c.arc(X + w * a, Y + h * b, h * k, 0, TAU); c.fill() }); c.beginPath(); c.ellipse(X + w * .5, Y + h * .1, w * .5, h * .42, 0, 0, TAU); c.fill() } c.globalAlpha = 1 },
  birds(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; c.globalAlpha = va; c.strokeStyle = e.c; c.lineCap = 'round'; for (const [X0, Y, sp, ph, s] of e.items) { const X = mod(X0 + t * sp, W + 80) - 40, f = Math.sin(t * 7 + ph) * 7; c.save(); c.translate(X, Y); c.scale(s, s); c.lineWidth = 1.4 / s; c.beginPath(); c.moveTo(-10, f * -.4); c.quadraticCurveTo(-5, -5 - f, 0, 0); c.quadraticCurveTo(5, -5 - f, 10, f * -.4); c.stroke(); c.restore() } c.globalAlpha = 1 },
  /* particles: m = snow | rain | rise | petal. pts [x,y,r,v,sw,ph] */
  fall(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; c.globalAlpha = va;
    for (const [X0, Y0, R, V, SW, PH] of e.pts) {
      if (e.m === 'snow') { const Y = mod(Y0 + t * V, 790) - 5, X = mod(X0 + Math.sin(t * .8 + SW + Y * .01) * 14, W); c.fillStyle = e.c; c.beginPath(); c.arc(X, Y, R, 0, TAU); c.fill() }
      else if (e.m === 'rain') { const k = e.k == null ? .18 : e.k, top = e.top == null ? -20 : e.top, Y = top + mod(Y0 + t * V, e.span == null ? 820 : e.span), X = X0 - Y * k; c.strokeStyle = e.c; c.lineWidth = 1; c.lineCap = 'round'; c.beginPath(); c.moveTo(X, Y); c.lineTo(X - R * k, Y + R); c.stroke() }
      else if (e.m === 'rise') { const Y = mod(Y0 - t * V + 30, 840) - 30, X = X0 + Math.sin(t * .6 + PH) * 10; glow(c, X, Y, R * 4, e.g || e.c, va); c.globalAlpha = va; c.fillStyle = e.c; c.beginPath(); c.arc(X, Y, R, 0, TAU); c.fill() }
      else if (e.m === 'petal') { const Y = mod(Y0 + t * V, 800) - 10, X = mod(X0 + t * 8 + Math.sin(t * .7 + PH) * 30, W + 20) - 10; c.fillStyle = e.c; c.beginPath(); c.ellipse(X, Y, R * 1.2, R * .7, t + PH, 0, TAU); c.fill() } }
    c.globalAlpha = 1 },
  flies(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; for (const [X0, Y0, AX, AY, SP, PH, BL] of e.pts) { const X = X0 + Math.sin(t * SP + PH) * AX, Y = Y0 + Math.cos(t * SP * 1.3 + PH) * AY, b = Math.max(0, Math.sin(t * BL + PH)) * va; glow(c, X, Y, 16, e.c, b); c.globalAlpha = b; c.fillStyle = e.c2 || '#ffffc8'; c.beginPath(); c.arc(X, Y, 1.3, 0, TAU); c.fill(); c.globalAlpha = 1 } },
  smoke(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; const a0 = e.a == null ? .22 : e.a; c.fillStyle = e.c; for (let i = 0; i < e.n; i++) { const p = mod(t * e.speed + i / e.n, 1); c.globalAlpha = a0 * (1 - p) * va; c.beginPath(); c.arc(e.x + Math.sin(p * 6 + i) * 8 + p * e.drift, e.y - p * e.rise, e.r0 + p * e.grow, 0, TAU); c.fill() } c.globalAlpha = 1 },
  shoot(c, e, t, x) { for (const [period, off, dur, X, Y, DX, DY, len] of e.items) { const q = mod(t + off, period) / period; if (q >= dur) continue; const u = q / dur, px = X + DX * u, py = Y + DY * u, a = Math.sin(u * Math.PI), d = Math.hypot(DX, DY), tx = px - DX / d * len, ty = py - DY / d * len; const g = c.createLinearGradient(px, py, tx, ty); g.addColorStop(0, 'rgba(255,255,255,' + a + ')'); g.addColorStop(1, 'rgba(255,255,255,0)'); c.strokeStyle = g; c.lineWidth = 1.8; c.lineCap = 'round'; c.beginPath(); c.moveTo(px, py); c.lineTo(tx, ty); c.stroke() } },
  shimmer(c, e, t, x) { c.fillStyle = e.c; for (let i = 0; i < e.n; i++) { const w = lerp(e.w0, e.w1, i / e.n) * (.85 + .15 * Math.sin(t * 1.6 + i * .9)), X = e.x + Math.sin(t * .9 + i * .7) * (3 + i * .25), a = Math.max(0, e.a0 - i * e.dec); c.globalAlpha = a; c.fillRect(X - w / 2, e.y + i * e.step, w, 2.2) } c.globalAlpha = 1 },
  flame(c, e, t, x) { const fl = (X, Y, w, h, col, sw) => { c.fillStyle = col; c.beginPath(); c.moveTo(X - w, Y); c.quadraticCurveTo(X - w * 1.1, Y - h * .55, X + sw, Y - h); c.quadraticCurveTo(X + w * 1.1, Y - h * .5, X + w, Y); c.closePath(); c.fill() }, s = e.s || 1;
    [[0, 16, 54, '#e8531c', 0], [-9, 11, 38, '#f08a2a', 1.6], [9, 11, 36, '#f08a2a', -1.4]].forEach(([dx, w, h, col, sw], i) => fl(e.x + dx * s, e.y, w * .7 * s, h * s * (.85 + .15 * Math.sin(t * 10 + i * 2)), col, (Math.sin(t * 6 + i) * 3 + sw) * s)); fl(e.x, e.y, 6 * s, 26 * s * (.85 + .15 * Math.sin(t * 12)), '#ffd870', Math.sin(t * 7) * 2 * s) },
  aurora(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; c.save(); c.globalCompositeOperation = 'screen'; for (const [k, by, col, hh] of e.bands) { for (let X = 0; X <= W; X += 6) { const Y = by + Math.sin(X * .016 + t * .4 + k * 1.9) * 34 + Math.sin(X * .05 - t * .6 + k) * 10, a = (.26 + .16 * Math.sin(X * .03 + t * .5 + k)) * va, g = c.createLinearGradient(0, Y - hh * .2, 0, Y + hh); g.addColorStop(0, 'rgba(' + col + ',0)'); g.addColorStop(.25, 'rgba(' + col + ',' + a + ')'); g.addColorStop(1, 'rgba(' + col + ',0)'); c.fillStyle = g; c.fillRect(X, Y - hh * .2, 6.5, hh * 1.2) } } c.restore() },
  mover(c, e, t, x) { const va = visAt(e.vis, x.p); if (va <= .003) return; const [x0, y0, x1, y1] = e.path, u = mod(t + (e.off || 0), e.dur) / e.dur, px = lerp(x0, x1, u), py = lerp(y0, y1, u), bob = e.bob ? Math.sin(t * e.bob[1] + (e.bob[2] || 0)) * e.bob[0] : 0, tilt = e.tilt ? Math.sin(t * (e.bob ? e.bob[1] : 1) * .8) * e.tilt : 0; c.save(); c.globalAlpha = va; c.translate(px, py + bob); if (tilt) c.rotate(tilt); drawShapes(c, e.shapes); c.restore() },
  spin(c, e, t, x) { c.save(); c.translate(e.x, e.y); c.rotate(t * e.speed + (e.phase || 0)); drawShapes(c, e.shapes); c.restore() },
  /* shapes carried around a circle without rotating themselves (ferris wheel gondolas) */
  orbit(c, e, t, x) { for (let i = 0; i < e.n; i++) { const a = t * e.speed + (e.phase || 0) + TAU * i / e.n; c.save(); c.translate(e.x + Math.cos(a) * e.r, e.y + Math.sin(a) * e.r); drawShapes(c, e.shapes); c.restore() } },
  orb(c, e, t, x) { const p = x.p; if (p < e.a || p > e.b) return; const q = lerp(e.q0 == null ? 0 : e.q0, e.q1 == null ? 1 : e.q1, (p - e.a) / (e.b - e.a)), k = Math.sin(q * Math.PI), X = lerp(e.x0, e.x1, q), Y = e.y0 - k * e.yAmp, col = e.warm ? mixh(e.c, e.warm, Math.pow(1 - k, 3)) : e.c; glow(c, X, Y, e.r * 3.6, e.glow, 1); const g = c.createRadialGradient(X - e.r * .3, Y - e.r * .3, e.r * .05, X, Y, e.r); g.addColorStop(0, e.hi || '#f4f7fd'); g.addColorStop(1, col); c.fillStyle = g; c.beginPath(); c.arc(X, Y, e.r, 0, TAU); c.fill(); if (e.craters) { c.fillStyle = 'rgba(120,135,180,.07)';[[.3, -.2, .22], [-.25, .25, .3], [.1, .45, .14], [-.4, -.3, .12]].forEach(([a, b, kk]) => { c.beginPath(); c.arc(X + a * e.r, Y + b * e.r, kk * e.r, 0, TAU); c.fill() }) } },
  tint(c, e, t, x) { const K = e.keys, p = x.p; let i = 0; while (i < K.length - 2 && p > K[i + 1][0]) i++; const [a, ca] = K[i], [b, cb] = K[i + 1], u = clamp((p - a) / (b - a)), A = parseCss(ca), B = parseCss(cb), m = A.map((v, j) => lerp(v, B[j], u)); if (m[3] <= .003) return; c.save(); if (e.m === 'screen') c.globalCompositeOperation = 'screen'; c.fillStyle = 'rgba(' + Math.round(m[0]) + ',' + Math.round(m[1]) + ',' + Math.round(m[2]) + ',' + m[3] + ')'; c.fillRect(0, 0, W, H); c.restore() }
};
function drawFx(c, list, t, p, z) { const x = { p }; for (const e of list) { if ((e.z == null ? 1 : e.z) !== z) continue; FX[e.t](c, e, t, x) } }

/* ---------- scenes ---------- */
const SCENES = [];
/* def: {id,name,mode:'still'|'live'|'long',theme:'dark'|'light',style:'min'|'ink',loop,seed,st(c,r),fx(),skyFrames:[{p,draw(c,r)}]}
 * long scenes: skyFrames are opaque full-screen frames cross-faded over the loop; st draws the (transparent) land on top. */
function defScene(d) { d.loop = d.loop || (d.mode === 'long' ? 45 : 0); SCENES.push(d); return d }

/* bake film grain + vignette into a canvas (CPU pass, deterministic). alpha layers only get grain where they are opaque. */
function bake(cv, light, vignette) { const c = cv.getContext('2d'), w = cv.width, h = cv.height, id = c.getImageData(0, 0, w, h), d = id.data, r = rng(99), amp = light ? 7 : 8, cx = w / 2, cy = h * .5, r0 = h * .22, r1 = h * .78, vmax = light ? .28 : .45, vc = light ? [40, 20, 60] : [0, 0, 8];
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) { const i = (y * w + x) * 4, a = d[i + 3]; if (a === 0) { r(); continue } const n = (r() - .5) * 2 * amp; let R = d[i] + n, G = d[i + 1] + n, B = d[i + 2] + n;
    if (vignette) { const dd = Math.hypot(x - cx, y - cy), k = clamp((dd - r0) / (r1 - r0)) * vmax; R = R + (vc[0] - R) * k; G = G + (vc[1] - G) * k; B = B + (vc[2] - B) * k }
    d[i] = R < 0 ? 0 : R > 255 ? 255 : R; d[i + 1] = G < 0 ? 0 : G > 255 ? 255 : G; d[i + 2] = B < 0 ? 0 : B > 255 ? 255 : B }
  c.putImageData(id, 0, 0) }
function mkCanvas(scale) { const cv = document.createElement('canvas'); cv.width = Math.round(W * scale); cv.height = Math.round(H * scale); const c = cv.getContext('2d', { willReadFrequently: true }); c.scale(scale, scale); return [cv, c] }
/* render the static layers of a scene at `scale` (3 for the app assets, ~1.5 for the preview) */
function geom(sc) { if (sc._g === undefined) sc._g = sc.init ? sc.init(rng(sc.seed + 7)) : null; return sc._g }
function renderLayers(sc, scale) { const G = geom(sc); const light = sc.theme === 'light', out = { sky: [], bg: null };
  if (sc.mode === 'long') { sc.skyFrames.forEach((f, i) => { const [cv, c] = mkCanvas(scale); f.draw(c, rng(sc.seed + 100 + i), G); bake(cv, light, true); out.sky.push({ p: f.p, cv }) }); const [cv, c] = mkCanvas(scale); sc.st(c, rng(sc.seed), G); bake(cv, light, false); out.bg = cv }
  else { const [cv, c] = mkCanvas(scale); sc.st(c, rng(sc.seed), G); bake(cv, light, true); out.bg = cv }
  return out }
function sceneFx(sc) { if (!sc._fx) sc._fx = sc.fx ? sc.fx(geom(sc)) : []; return sc._fx }
/* draw one frame. ctx must already be scaled so 1 unit = 1 logical px. */
function drawFrame(c, sc, layers, t) { const L = sc.loop, p = L ? mod(t, L) / L : 0, fx = sceneFx(sc);
  if (layers.sky.length) { const S = layers.sky, n = S.length; let i = 0; for (let k = 0; k < n; k++)if (mod(p, 1) >= S[k].p) i = k; const j = (i + 1) % n, a = S[i].p, b = j === 0 ? S[j].p + 1 : S[j].p, pp = j === 0 && p < a ? p + 1 : p, u = smooth(clamp((pp - a) / (b - a))); c.globalAlpha = 1; c.drawImage(S[i].cv, 0, 0, W, H); if (u > .003 && n > 1) { c.globalAlpha = u; c.drawImage(S[j].cv, 0, 0, W, H); c.globalAlpha = 1 } }
  drawFx(c, fx, t, p, 0); c.drawImage(layers.bg, 0, 0, W, H); drawFx(c, fx, t, p, 1) }

return { W, H, TAU, rng, lerp, clamp, smooth, mod, hex, mixh, ramp2, parseCss, sub, path, wob, ink, blob, hatch, circ, glow, sky, poly, ridge, fillRidge, fridge, ridgeY, hill, pine, fpine, roundTree, ftree, stars, drawStar, fstars, starPts, fsky, fmoon, moon, cloud, cottage, rooftops, crow, cat, bareTree, drawShapes, FX, drawFx, visAt, SCENES, defScene, bake, mkCanvas, geom, renderLayers, sceneFx, drawFrame };
})();
