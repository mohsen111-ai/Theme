/* Generated scene families. Every family builds {init, st, fx} from a palette, an ink flag and a live flag.
 * Rule used everywhere: anything that stands on the ground is drawn BEFORE the ridge that is its ground, so the ridge hides its base. */
(() => {
const { W, H, TAU, rng, lerp, mixh, glow, poly, ridge, fillRidge, ridgeY, fpine, blob, ink, circ, rooftops, defScene } = NYX;
const GN = NYX.GEN, { rr, pick, lay, tone, rgba, fillLayer, tree, sky, starsStatic, sky_body, house, twinkle, clouds, birds, flies, shoot, smoke, winGlow, petals, snow, rain, lanterns, at } = GN;
const far = (x, y, R) => (px, py) => Math.hypot(px - x, py - y) < R * 1.7;
const skyStuff = (P, r, live, body, ymax = 430) => P.light ? [clouds(P, r, 4, 90, 330), ...(live ? [birds(P, r, 3, 150, 300)] : [])] : [twinkle(r() * 1e4 | 0, 120, ymax, far(body.x, body.y, body.R)), clouds(P, r, 3, 120, 380), ...(live ? [shoot(r)] : [])];

const FAM = {};

/* ---- rolling hills with pines under a moon (or low sun) ---- */
FAM.hills = { nouns: ['Pine Moon', 'Quiet Ridge', 'Soft Hills', 'Far Slopes', 'Night Meadow', 'Hill Light', 'Slow Evening'],
  make(P, ink_, live, r) { const body = { x: rr(r, 60, 300), y: rr(r, 110, 240), R: rr(r, 28, 52) }, dens = rr(r, 14, 26), ys = [rr(r, 390, 480), rr(r, 480, 560), rr(r, 570, 640), rr(r, 670, 720)], am = ys.map((_, k) => rr(r, 14, 34) - k * 2), sc = rr(r, .9, 1.7);
    return { init(g) { return { rs: ys.map((y, k) => ridge(g, y, am[k], sc + k * .1)) } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 150, 430); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        Gm.rs.forEach((g, k) => { fillLayer(c, rg, P, ink_, g, lay(P, k, 4)); if (k >= 1) for (let x = rr(rg, -4, 14); x < W + 10; x += dens * (k === 3 ? 1.5 : 1) * (ink_ ? 1.7 : 1) + rg() * dens) tree(c, rg, P, ink_, x, at(g, x), (k + 1) * (6 + rg() * 8) + 8, tone(P, k, 4, .45 + .12 * k)) }) },
      fx(Gm) { if (!live) return []; const out = skyStuff(P, r, true, body); if (!P.light) out.push(flies(r, 14, 560, 700)); return out } } } };

/* ---- a lone cabin on the hill: smoke, warm window ---- */
FAM.cabin = { nouns: ['Hilltop Cabin', 'Warm Window', 'Lone Cabin', 'Smoke Signal', 'Last Light Home', 'Cabin Glow', 'Quiet House'],
  make(P, ink_, live, r) { const body = { x: rr(r, 50, 310), y: rr(r, 110, 220), R: rr(r, 26, 46) }, hx = rr(r, 90, 270), hs = rr(r, 1.7, 2.6), hl = pick(r, [1, 2]), ys = [rr(r, 450, 490), rr(r, 540, 580), rr(r, 620, 650), rr(r, 705, 730)];
    let hinfo = null;
    return { init(g) { return { rs: ys.map((y, k) => ridge(g, y, 16 - k, 1 + k * .15)) } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 130, 430); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        Gm.rs.forEach((g, k) => { if (k === hl) { const wall = P.light ? mixh('#f0e4cc', P.bot, .2) : tone(P, k, 4, .72), roof = P.light ? mixh(P.ground, '#a2705a', .5) : tone(P, k, 4, .9); hinfo = house(c, rg, P, ink_, hx, at(g, hx, 4), hs, wall, roof) }
          fillLayer(c, rg, P, ink_, g, lay(P, k, 4)); if (k >= 1) for (let x = rr(rg, 0, 16); x < W + 10; x += 20 * (ink_ ? 1.8 : 1) + rg() * 22) if (Math.abs(x - hx) > 30 * hs * (k === hl ? 1 : .3) || k !== hl) tree(c, rg, P, ink_, x, at(g, x), (k + 1) * (5 + rg() * 7) + 8, tone(P, k, 4, .5 + .1 * k)) }) },
      fx(Gm) { if (!live) return []; const out = skyStuff(P, r, true, body); out.push(smoke(P, hinfo.chim[0], hinfo.chim[1] - 2)); if (!P.light) { out.push(winGlow(hinfo.lit, .6)); out.push(flies(r, 10, 600, 720)) } return out } } } };

/* ---- lighthouse on a cliff over a quiet sea ---- */
FAM.lighthouse = { nouns: ['Lighthouse', 'Harbour Light', 'Sea Watch', 'Cliff Beacon', 'Night Keeper', 'Last Beacon', 'Tide Light'],
  make(P, ink_, live, r) { const side = r() < .5 ? 1 : 0, cx = side ? rr(r, 240, 300) : rr(r, 60, 120), peak = rr(r, 150, 200), body = { x: side ? rr(r, 60, 150) : rr(r, 210, 300), y: rr(r, 120, 230), R: rr(r, 28, 46) }, hz = rr(r, 430, 470);
    return { init(g) { const base = ridge(g, 715, 12, 1.2), cl = base.map(([x, y]) => [x, y - peak * Math.exp(-Math.pow((x - cx) / 74, 2))]); return { cl, isl: ridge(g, hz + 2, 4, 1.8) } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 120, hz - 10); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        fillLayer(c, rg, P, ink_, Gm.isl, lay(P, 0, 4));
        const g = c.createLinearGradient(0, hz, 0, H); g.addColorStop(0, mixh(P.bot, P.top, .15)); g.addColorStop(1, lay(P, 2, 4)); c.fillStyle = g; c.fillRect(0, hz + 3, W, H);
        glow(c, body.x, hz + 6 + (hz - body.y) * .15, body.R * 3, P.light ? 'rgba(255,240,210,.4)' : 'rgba(170,190,235,.3)', .8);
        for (let i = 0; i < 34; i++) { const y = hz + 14 + i * 8 + rg() * 4, x = rg() * W; c.fillStyle = P.light ? 'rgba(255,255,255,.16)' : 'rgba(160,180,230,' + (.05 + rg() * .1) + ')'; c.fillRect(x, y, 14 + rg() * 40, 1.4) }
        // tower stands on the cliff top; the cliff fill below hides its footing
        const ty = at(Gm.cl, cx, 6), th = 116, bw = 22, tw = 13, top = ty - th, wall = P.light ? '#f4eee2' : tone(P, 3, 4, .55), band = P.light ? '#b0605a' : tone(P, 3, 4, .8);
        poly(c, [[cx - bw, ty], [cx - tw, top], [cx + tw, top], [cx + bw, ty]], wall); poly(c, [[cx - lerp(bw, tw, .62), ty - th * .62], [cx - lerp(bw, tw, .38), ty - th * .38], [cx + lerp(bw, tw, .38), ty - th * .38], [cx + lerp(bw, tw, .62), ty - th * .62]], band);
        c.fillStyle = band; c.fillRect(cx - 19, top - 3, 38, 5); c.fillStyle = P.light ? '#ffe9a8' : '#ffdf8e'; c.fillRect(cx - 9, top - 20, 18, 17); poly(c, [[cx - 13, top - 20], [cx, top - 36], [cx + 13, top - 20]], band); c.fillStyle = wall; c.fillRect(cx - 1, top - 44, 2, 9);
        if (!P.light) glow(c, cx, top - 12, 46, 'rgba(255,215,140,.55)', .9);
        fillLayer(c, rg, P, ink_, Gm.cl, lay(P, 3, 4)); for (let x = cx - 86; x < cx + 86; x += 30 + rg() * 20) if (Math.abs(x - cx) > 28) c.fillStyle = tone(P, 3, 4, .6), c.fillRect(x, at(Gm.cl, x, 2) - 4, 8 + rg() * 6, 6) },
      fx(Gm) { if (!live) return []; const ty = at(Gm.cl, cx, 6) - 116 - 12, bm = rgba('#fff0c0', .075), out = skyStuff(P, r, true, body, hz - 20);
        out.push({ t: 'shimmer', x: body.x, y: hz + 12, n: 26, step: 11, w0: 76, w1: 14, c: P.light ? '#fff6e2' : '#e1e8f8', a0: P.light ? .5 : .45, dec: .014 });
        if (!P.light) out.push({ t: 'spin', z: 1, x: cx, y: ty, speed: .5, shapes: [460, 330, 200].flatMap(L => [['p', [0, -5, L, -L * .075 - 4, L, L * .075 + 4, 0, 5], bm], ['p', [0, -5, -L, -L * .075 - 4, -L, L * .075 + 4, 0, 5], bm]]) });
        const sb = (k, hull, sl) => [['p', [-20 * k, 0, 20 * k, 0, 13 * k, 10 * k, -13 * k, 10 * k], hull], ['p', [-2 * k, -1, -2 * k, -28 * k, 17 * k, -1], sl], ['p', [-4 * k, -1, -4 * k, -19 * k, -17 * k, -1], hull]];
        const hc = P.light ? '#5a5a78' : '#05070f', sl = P.light ? '#f4efe6' : tone(P, 2, 4, .75);
        out.push({ t: 'mover', path: [side ? -60 : 430, hz + 16, side ? 430 : -60, hz + 16], dur: 170, off: r() * 100, bob: [2, 1, 0], tilt: .04, shapes: sb(.6, hc, sl) }, { t: 'mover', path: [side ? 430 : -60, hz + 28, side ? -60 : 430, hz + 28], dur: 130, off: r() * 100, bob: [2.5, 1.2, 2], tilt: .05, shapes: sb(.85, hc, sl) }); return out } } } };

/* ---- desert dunes with cacti and a camp fire ---- */
const cactus = (c, x, y, h, col) => { const w = h * .13; c.fillStyle = col; c.fillRect(x - w / 2, y - h, w, h + 2); c.beginPath(); c.arc(x, y - h, w / 2, 0, TAU); c.fill(); const arm = (dx, ay, up) => { c.fillRect(Math.min(x, x + dx), y - ay, Math.abs(dx), w * .8); c.fillRect(x + dx - (dx > 0 ? 0 : w * .8), y - ay - up, w * .8, up + w * .8); c.beginPath(); c.arc(x + dx + w * .4 - (dx > 0 ? 0 : w * .8) + w * .0, y - ay - up, w * .4, 0, TAU); c.fill() }; arm(h * .26, h * .45, h * .28); arm(-h * .24, h * .6, h * .22) };
FAM.desert = { nouns: ['Dune Camp', 'Cactus Moon', 'Sand Sea', 'Desert Night', 'Dry Wind', 'Star Dunes', 'Camp Fire'],
  make(P, ink_, live, r) { const body = { x: rr(r, 60, 300), y: rr(r, 120, 250), R: rr(r, 30, 56) }, ys = [rr(r, 420, 470), rr(r, 510, 550), rr(r, 600, 640), rr(r, 690, 725)], camp = r() < .6, cx = rr(r, 100, 260), cacti = [];
    for (let i = 0; i < 5; i++) cacti.push({ x: rr(r, 20, 340), k: 1 + Math.floor(r() * 2), h: rr(r, 40, 70) });
    let fire = null;
    return { init(g) { return { rs: ys.map((y, k) => ridge(g, y, 20 + k * 4, .6 + k * .12)) } },
      st(c, rg, Gm) { sky(c, P, false); starsStatic(c, rg, P, 160, 440); sky_body(c, rg, P, false, body.x, body.y, body.R);
        Gm.rs.forEach((g, k) => { cacti.filter(q => q.k === k).forEach(q => cactus(c, q.x, at(g, q.x, 4), q.h * (1.0 + .3 * k), tone(P, k, 4, .6)));
          if (camp && k === 2) { const y = at(g, cx, 5); poly(c, [[cx - 42, y], [cx - 6, y - 54], [cx + 30, y]], tone(P, 2, 4, .8)); poly(c, [[cx - 6, y - 54], [cx + 30, y], [cx + 8, y]], tone(P, 2, 4, .95)); fire = { x: cx + 62, y: at(g, cx + 62, 2) } }
          fillLayer(c, rg, P, false, g, lay(P, k, 4)) }); if (camp) { c.fillStyle = tone(P, 2, 4, .9); c.fillRect(fire.x - 9, fire.y - 2, 18, 3) } },
      fx(Gm) { if (!live) return []; const out = skyStuff(P, r, true, body, 440); if (camp && !P.light) out.push({ t: 'flame', x: fire.x, y: fire.y - 2, s: .55 }, { t: 'glows', c: 'rgba(255,170,80,.55)', pts: [[fire.x, fire.y - 12, 60, 0, .35]] }, { t: 'smoke', c: '#cfc8d8', x: fire.x, y: fire.y - 26, n: 5, drift: 10, rise: 80, r0: 2, grow: 6, speed: .1, a: .14 }); else out.push(flies(r, 8, 560, 700)); return out } } } };

/* ---- town rooftops: windows, chimneys, birds, rain variants ---- */
FAM.town = { nouns: ['Rooftops', 'Lit Windows', 'Little Town', 'Chimney Row', 'Night Streets', 'Old Quarter', 'Tiled Roofs'],
  make(P, ink_, live, r, kind) { const body = { x: rr(r, 50, 310), y: rr(r, 110, 230), R: rr(r, 26, 44) }, lit = rr(r, .35, .6), rainy = kind === 'rain', wins = [];
    return { init(g) { return {} },
      st(c, rg, Gm) { sky(c, P, ink_); if (!rainy) { starsStatic(c, rg, P, 130, 430); sky_body(c, rg, P, ink_, body.x, body.y, body.R) } else { glow(c, body.x, body.y, 120, 'rgba(160,170,210,.18)', 1) }
        const l = ink_ ? P.line : null; rooftops(c, rg, 575, lay(P, 1, 4), l, null, 40, 105, 0); const nw = rooftops(c, rg, 670, lay(P, 3, 4), l, P.light ? null : P.win, 70, 170, P.light ? 0 : lit); wins.length = 0; wins.push(...nw); wins.tops = nw.tops;
        if (!P.light) wins.slice(0, 40).forEach(o => glow(c, o.x + 4, o.y + 5, 14, 'rgba(255,190,100,.4)', .7)) },
      fx(Gm) { if (!live) return []; const out = rainy ? [clouds(P, r, 4, 90, 300), rain(r, 90, 'rgba(190,205,240,.35)'), rain(r, 60, 'rgba(190,205,240,.2)')] : skyStuff(P, r, true, body, 440);
        const tops = (wins.tops || []).filter(t => t.chim).slice(0, 4); tops.forEach((t, i) => out.push(smoke(P, t.chim[0] + 4, t.chim[1] - 2, i)));
        if (!P.light) out.push(winGlow(wins.filter((_, i) => i % 3 === 0).slice(0, 12).map(o => [o.x + 4, o.y + 5]), .6)); if (!rainy && !P.light && r() < .5) out.push(lanterns(r, 10)); return out } } } };

/* ---- lake with reflection and a sail boat ---- */
FAM.lake = { nouns: ['Still Lake', 'Moon Water', 'Sail Home', 'Reed Shore', 'Calm Water', 'Lake Light', 'Far Shore'],
  make(P, ink_, live, r) { const body = { x: rr(r, 70, 290), y: rr(r, 120, 240), R: rr(r, 28, 50) }, hz = rr(r, 470, 510);
    return { init(g) { return { m1: ridge(g, hz - 70, 26, 1.2), m2: ridge(g, hz - 12, 10, 1.6), bank: ridge(g, 735, 8, 1.2) } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 130, hz - 40); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        fillLayer(c, rg, P, ink_, Gm.m1, lay(P, 0, 4)); fillLayer(c, rg, P, ink_, Gm.m2, lay(P, 1, 4)); for (let x = 0; x < W; x += 18 + rg() * 14) tree(c, rg, P, ink_, x, at(Gm.m2, x, 2), 10 + rg() * 14, tone(P, 1, 4, .6));
        const g = c.createLinearGradient(0, hz, 0, H); g.addColorStop(0, mixh(P.bot, P.top, .1)); g.addColorStop(1, lay(P, 2, 4)); c.fillStyle = g; c.fillRect(0, hz, W, H - hz);
        glow(c, body.x, hz + (hz - body.y) * .2, body.R * 3, P.light ? 'rgba(255,240,210,.4)' : 'rgba(170,190,235,.28)', .9);
        for (let i = 0; i < 30; i++) { const y = hz + 10 + i * 8.5 + rg() * 4; c.fillStyle = P.light ? 'rgba(255,255,255,.18)' : 'rgba(150,170,225,' + (.05 + rg() * .1) + ')'; c.fillRect(rg() * W, y, 14 + rg() * 46, 1.4) }
        fillLayer(c, rg, P, ink_, Gm.bank, lay(P, 3, 4)); const reed = tone(P, 3, 4, .7); for (let x = 6; x < W; x += 12 + rg() * 16) { const y = at(Gm.bank, x, 4), h = 22 + rg() * 30; c.strokeStyle = reed; c.lineWidth = 1.4; c.beginPath(); c.moveTo(x, y); c.lineTo(x + (rg() - .5) * 4, y - h); c.stroke(); c.fillStyle = reed; c.fillRect(x - 1.5 + 0, y - h - 7, 3, 8) } },
      fx(Gm) { if (!live) return []; const out = skyStuff(P, r, true, body, hz - 40), sb = (k, hull, sl) => [['p', [-20 * k, 0, 20 * k, 0, 13 * k, 10 * k, -13 * k, 10 * k], hull], ['p', [-2 * k, -1, -2 * k, -28 * k, 17 * k, -1], sl], ['p', [-4 * k, -1, -4 * k, -19 * k, -17 * k, -1], hull]];
        out.push({ t: 'shimmer', x: body.x, y: hz + 12, n: 28, step: 10, w0: 76, w1: 16, c: P.light ? '#fff6e2' : '#e1e8f8', a0: .45, dec: .014 }, { t: 'mover', path: [-60, hz + 90, 430, hz + 90], dur: 150, off: r() * 90, bob: [3, 1.1, 0], tilt: .05, shapes: sb(1.4, P.light ? '#5a5a78' : '#05070f', P.light ? '#f4efe6' : tone(P, 2, 4, .7)) });
        if (!P.light) out.push(flies(r, 10, 640, 730)); return out } } } };

/* ---- space: nebula, planets, orbiting moon, comets (no ground needed) ---- */
FAM.space = { nouns: ['Ringed Planet', 'Far Nebula', 'Quiet Orbit', 'Comet Night', 'Deep Field', 'Twin Moons', 'Star Drift'],
  make(P, ink_, live, r) { const pl = { x: rr(r, 60, 300), y: rr(r, 230, 520), R: rr(r, 50, 100) }, neb = [mixh(P.bot, '#ff7ab0', .4), mixh(P.bot, '#6a8cff', .5), mixh(P.bot, '#7affd0', .3)], ring = r() < .6, sat = { r: pl.R * 1.9, R: rr(r, 6, 11) };
    return { init() { return {} },
      st(c, rg) { sky(c, P, false); for (let i = 0; i < 4; i++) glow(c, rg() * W, 80 + rg() * 560, 140 + rg() * 160, rgba(neb[i % 3], .2 + rg() * .22), 1); NYX.fstars(c, rg, 230, H - 60, .7);
        for (let i = 0; i < 28; i++) { c.fillStyle = 'rgba(255,255,255,.85)'; c.beginPath(); c.arc(rg() * W, rg() * H, .9 + rg() * .7, 0, TAU); c.fill() }
        if (ring) { c.save(); c.translate(pl.x, pl.y); c.rotate(-.35); c.strokeStyle = rgba(mixh('#e0d2b8', P.bot, .3), .5); c.lineWidth = pl.R * .18; c.beginPath(); c.ellipse(0, 0, pl.R * 1.7, pl.R * .42, 0, Math.PI, TAU); c.stroke(); c.restore() }
        glow(c, pl.x, pl.y, pl.R * 2.4, rgba(mixh(P.bot, '#aab6ff', .5), .3), 1); const g = c.createRadialGradient(pl.x - pl.R * .4, pl.y - pl.R * .4, pl.R * .1, pl.x, pl.y, pl.R); g.addColorStop(0, mixh(P.bot, '#f2e4d0', .75)); g.addColorStop(1, mixh(P.top, P.bot, .5)); c.fillStyle = g; c.beginPath(); c.arc(pl.x, pl.y, pl.R, 0, TAU); c.fill();
        c.save(); c.beginPath(); c.arc(pl.x, pl.y, pl.R, 0, TAU); c.clip(); for (let i = 0; i < 6; i++) { c.fillStyle = 'rgba(' + (i % 2 ? '255,255,255' : '10,10,40') + ',.07)'; c.fillRect(pl.x - pl.R, pl.y - pl.R + i * pl.R * .34 + rg() * 8, pl.R * 2, 6 + rg() * 12) } c.restore();
        if (ring) { c.save(); c.translate(pl.x, pl.y); c.rotate(-.35); c.strokeStyle = rgba(mixh('#e0d2b8', P.bot, .3), .75); c.lineWidth = pl.R * .18; c.beginPath(); c.ellipse(0, 0, pl.R * 1.7, pl.R * .42, 0, 0, Math.PI); c.stroke(); c.restore() } },
      fx() { const out = [twinkle(r() * 1e4 | 0, 90, 780)]; if (!live) return []; out.push(shoot(r), { t: 'orbit', x: pl.x, y: pl.y, r: sat.r, n: 1, speed: .09, phase: r() * 6, shapes: [['g', 0, 0, sat.R * 3.4, 'rgba(210,225,255,.4)', .9], ['c', 0, 0, sat.R, '#e4e9f6'], ['c', -sat.R * .3, -sat.R * .3, sat.R * .45, '#f8fbff']] },
        { t: 'mover', path: [rr(r, 300, 460), -40, rr(r, -120, 20), 540], dur: 70, off: r() * 60, shapes: [['p', [0, 0, 60, -26, 62, -20], 'rgba(200,225,255,.12)'], ['p', [0, 0, 40, -30, 44, -24], 'rgba(200,225,255,.1)'], ['g', 0, 0, 20, 'rgba(210,235,255,.8)', 1], ['c', 0, 0, 2.6, '#ffffff']] }); return out } } } };

/* ---- balloons and rising lanterns over the hills ---- */
const balloon = (k, body, panel) => [['c', 0, -60 * k, 36 * k, body], ['p', [-31 * k, -45 * k, 31 * k, -45 * k, 9 * k, -5 * k, -9 * k, -5 * k], body], ['e', 0, -62 * k, 11 * k, 33 * k, 0, panel], ['l', -8 * k, -6 * k, -6 * k, 1 * k, 1, body], ['l', 8 * k, -6 * k, 6 * k, 1 * k, 1, body], ['r', -7 * k, 1 * k, 14 * k, 9 * k, body], ['g', 0, -9 * k, 36 * k, 'rgba(255,170,70,.85)', .9]];
FAM.balloons = { nouns: ['Drifting Balloons', 'Lantern Sky', 'Slow Flight', 'Night Balloons', 'Floating Lights', 'Sky Drift'],
  make(P, ink_, live, r, kind) { const body = { x: rr(r, 60, 300), y: rr(r, 110, 220), R: rr(r, 28, 46) }, lant = kind === 'lantern';
    return { init(g) { return { rs: [ridge(g, rr(r, 600, 640), 20, 1.2), ridge(g, rr(r, 680, 710), 14, 1.5), ridge(g, 760, 8, 1.2)] } },
      st(c, rg, Gm) { sky(c, P, false); starsStatic(c, rg, P, 140, 500); sky_body(c, rg, P, false, body.x, body.y, body.R);
        if (!live) for (let i = 0; i < 3; i++) { const k = [1.5, 1, .65][i], bx = [110, 250, 60 + rg() * 240][i], by = [300, 200, 440][i]; c.save(); c.translate(bx, by); NYX.drawShapes(c, balloon(k, P.light ? ['#a85a5a', '#b88a3a', '#5a7aa8'][i] : '#05070f', P.light ? ['#c98a8a', '#d9b36a', '#8aa8c9'][i] : ['#12204a', '#17284f', '#1a2a52'][i])); c.restore() }
        Gm.rs.forEach((g, k) => { fillLayer(c, rg, P, false, g, lay(P, k + 1, 4)); for (let x = rr(rg, 0, 14); x < W + 10; x += 22 + rg() * 20) tree(c, rg, P, false, x, at(g, x), 14 + k * 8 + rg() * 20, tone(P, k + 1, 4, .55 + k * .1)) }) },
      fx(Gm) { const out = skyStuff(P, r, live, body, 480); if (!live) return []; const pc = P.light ? ['#c98a8a', '#d9b36a', '#8aa8c9'] : ['#12204a', '#17284f', '#1a2a52'], bc = P.light ? ['#a85a5a', '#b88a3a', '#5a7aa8'] : ['#05070f', '#05070f', '#05070f'];
        if (lant) out.push(lanterns(r, 22)); else for (let i = 0; i < 3; i++) { const x0 = rr(r, 60, 300), k = [1.4, 1, .6][i]; out.push({ t: 'mover', path: [x0, 900, x0 + rr(r, -60, 60), -170], dur: [110, 150, 190][i], off: r() * 150, bob: [5, .7, x0], tilt: .03, shapes: balloon(k, bc[i], pc[i]) }) } return out } } } };

/* ---- mountain range with snow caps and aurora ---- */
const peaksY = (base, ps) => { const pts = []; for (let x = -10; x <= W + 10; x += 9) { let h = 0; ps.forEach(p => { h = Math.max(h, p.h * (1 - Math.abs(x - p.x) / p.w)) }); pts.push([x, base - h + Math.sin(x * .31) * 1.4]) } return pts };
FAM.mountains = { nouns: ['Snow Peaks', 'Aurora Range', 'High Ridge', 'Cold Summit', 'Peak Light', 'Glacier Night', 'Far Mountains'],
  make(P, ink_, live, r) { const body = { x: rr(r, 60, 300), y: rr(r, 110, 220), R: rr(r, 26, 44) }, mkp = (n, hmin, hmax) => Array.from({ length: n }, (_, i) => ({ x: (i + .5) / n * W + rr(r, -30, 30), h: rr(r, hmin, hmax), w: rr(r, 90, 140) })), back = mkp(4, 140, 250), mid = mkp(4, 90, 170);
    return { init(g) { return { fr: ridge(g, 712, 14, 1.3) } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 150, 430); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        const cap = (ps, base, col) => ps.forEach(p => { if (p.h < 120) return; const top = base - p.h, w = p.w; poly(c, [[p.x, top], [p.x + w * .28, top + p.h * .28], [p.x + w * .14, top + p.h * .2], [p.x, top + p.h * .31], [p.x - w * .13, top + p.h * .2], [p.x - w * .28, top + p.h * .28]], col) });
        const sn = P.light ? '#fbf6ee' : mixh(P.bot, '#c8d4ee', .5);
        fillLayer(c, rg, P, ink_, peaksY(560, back), lay(P, 0, 4)); cap(back, 560, sn); fillLayer(c, rg, P, ink_, peaksY(640, mid), lay(P, 1, 4)); cap(mid, 640, mixh(sn, P.bot, .25));
        fillLayer(c, rg, P, ink_, Gm.fr, lay(P, 3, 4)); for (let x = rr(rg, 0, 10); x < W + 10; x += (ink_ ? 20 : 12) + rg() * 14) tree(c, rg, P, ink_, x, at(Gm.fr, x), 22 + rg() * 34, tone(P, 3, 4, .6)) },
      fx() { const out = skyStuff(P, r, live, body, 460); if (!live) return []; if (!P.light) out.push({ t: 'aurora', z: 1, bands: [[0, 190, pick(r, ['90,255,200', '120,255,170']), 200], [1, 250, pick(r, ['120,160,255', '170,130,255']), 170], [2, 150, '70,230,190', 150]] }); return out } } } };

/* ---- dense pine forest in layers, mist and fireflies ---- */
FAM.forest = { nouns: ['Pine Forest', 'Misty Pines', 'Deep Woods', 'Forest Moon', 'Mist Layers', 'Evergreen', 'Dark Pines'],
  make(P, ink_, live, r) { const body = { x: rr(r, 70, 290), y: rr(r, 120, 250), R: rr(r, 30, 52) }, ys = [440, 505, 570, 640, 715];
    return { init(g) { return { rs: ys.map((y, k) => ridge(g, y + rr(r, -10, 10), 10 + k * 2, 1 + k * .2)) } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 130, 400); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        Gm.rs.forEach((g, k) => { fillLayer(c, rg, P, ink_, g, lay(P, k, 5)); for (let x = rr(rg, -4, 8); x < W + 10; x += (7 + (4 - k) * 1.5) * (ink_ ? 2.2 : 1) + rg() * 8) tree(c, rg, P, ink_, x, at(g, x), 16 + k * 12 + rg() * 18, tone(P, k, 5, .4 + k * .1)) }) },
      fx() { const out = skyStuff(P, r, live, body, 400); if (!live) return []; out.push({ t: 'clouds', z: 1, c: P.light ? 'rgba(255,250,240,.2)' : rgba(mixh(P.bot, '#9aa0c8', .4), .13), items: [[20, 520, 190, 5], [200, 600, 210, 3.5], [100, 680, 180, 4.5]] }); if (!P.light) out.push(flies(r, 18, 520, 720)); return out } } } };

/* ---- blossom tree on a hill, falling petals ---- */
FAM.blossom = { nouns: ['Blossom Hill', 'Petal Fall', 'Spring Tree', 'Cherry Night', 'Soft Petals', 'Pink Tree'],
  make(P, ink_, live, r) { const body = { x: rr(r, 50, 310), y: rr(r, 110, 220), R: rr(r, 26, 44) }, tx = rr(r, 90, 270), th = rr(r, 150, 200), hue = P.light ? mixh('#f4b6c8', P.bot, .15) : mixh('#e48aa8', P.bot, .35), hue2 = mixh(hue, '#ffffff', .25);
    return { init(g) { return { rs: [ridge(g, 520, 22, 1.2), ridge(g, 600, 18, 1.3), ridge(g, 670, 12, 1.2), ridge(g, 740, 8, 1.4)] } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 120, 380); sky_body(c, rg, P, ink_, body.x, body.y, body.R);
        Gm.rs.forEach((g, k) => { if (k === 2) { const gy = at(g, tx, 6), tr = tone(P, 2, 4, .85); poly(c, [[tx - 18, gy], [tx - 9, gy - th * .5], [tx - 5, gy - th * .82], [tx + 5, gy - th * .82], [tx + 10, gy - th * .5], [tx + 19, gy]], tr);
            [[-60, -.72, 1], [55, -.78, 1], [0, -.95, .9]].forEach(([dx, dy], i) => { ink(c, rg, [[tx, gy - th * .5], [tx + dx, gy + dy * th]], tr, 5, .5, false, 1) });
            for (let i = 0; i < 9; i++) { const a = rg() * TAU, d = rg() * 60, cx = tx + Math.cos(a) * d * 1.15, cy = gy - th * .86 + Math.sin(a) * d * .6; c.fillStyle = i % 3 ? hue : hue2; c.beginPath(); c.arc(cx, cy, 26 + rg() * 24, 0, TAU); c.fill() } }
          fillLayer(c, rg, P, ink_, g, lay(P, k, 4)); if (k === 3) for (let i = 0; i < 40; i++) { c.fillStyle = hue; c.beginPath(); c.arc(rg() * W, at(g, 0, 10) + rg() * 30, 1.6 + rg() * 1.6, 0, TAU); c.fill() } }) },
      fx() { const out = skyStuff(P, r, live, body, 380); if (!live) return []; out.push(petals(r, 34, rgba(hue, .9))); return out } } } };

/* ---- snowy village ---- */
FAM.snow = { nouns: ['Snow Village', 'Winter Lights', 'Snowed In', 'Frost Roofs', 'White Hollow', 'Cold Chimneys'],
  make(P, ink_, live, r) { const body = { x: rr(r, 60, 300), y: rr(r, 110, 230), R: rr(r, 26, 44) }, snowc = k => P.light ? mixh('#f6f2ee', P.bot, .15 + k * .12) : mixh(lay(P, k + 1, 4), '#c8d4f0', .4 + k * .1), hs = [rr(r, 70, 120), rr(r, 180, 250), rr(r, 270, 320)], hinfo = [];
    return { init(g) { return { rs: [ridge(g, 520, 16, 1.2), ridge(g, 610, 14, 1.4), ridge(g, 690, 10, 1.3)] } },
      st(c, rg, Gm) { sky(c, P, ink_); starsStatic(c, rg, P, 130, 400); sky_body(c, rg, P, ink_, body.x, body.y, body.R); hinfo.length = 0;
        Gm.rs.forEach((g, k) => { if (k === 1) hs.forEach((x, i) => hinfo.push(house(c, rg, P, ink_, x, at(g, x, 5), 1.1 + i * .12, P.light ? '#efe6d4' : tone(P, 1, 4, .7), P.light ? '#8f6a64' : '#cfd8f0')));
          fillLayer(c, rg, P, ink_, g, snowc(k)); for (let x = rr(rg, 0, 16); x < W + 10; x += (16 + rg() * 20) * (ink_ ? 1.6 : 1)) if (k > 0 || rg() < .5) tree(c, rg, P, ink_, x, at(g, x), 14 + k * 12 + rg() * 20, tone(P, 2, 4, .55)) }) },
      fx() { const out = skyStuff(P, r, live, body, 400); if (!live) return []; out.push(snow(r, 90)); hinfo.forEach((h, i) => out.push(smoke(P, h.chim[0], h.chim[1] - 2, i))); if (!P.light) out.push(winGlow(hinfo.flatMap(h => h.lit), .6)); return out } } } };

/* ---------------- registration ---------------- */
const names = new Set(NYX.SCENES.map(s => s.name));
const plan = [['hills', 21], ['cabin', 21], ['lighthouse', 21], ['desert', 15], ['town', 21], ['lake', 21], ['space', 21], ['balloons', 15], ['mountains', 21], ['forest', 21], ['blossom', 21], ['snow', 21]];
const NOINK = new Set(['desert', 'space', 'balloons']), NOLIGHT = new Set(['space']);
let fi = 0;
plan.forEach(([fam, n]) => { const F = FAM[fam]; let dk = fi * 3, lt = fi * 2;
  for (let i = 0; i < n; i++) {
    const light = !NOLIGHT.has(fam) && (i % 7 === 3 || i % 7 === 6), inkv = !NOINK.has(fam) && i % 3 === 2, live = (i * 3 + fi) % 5 < 3;
    const P = GN.pal(light, light ? lt++ : dk++), seed = 5000 + fi * 100 + i, kind = fam === 'town' ? (i % 4 === 1 && !light ? 'rain' : 'clear') : fam === 'balloons' ? (i % 2 ? 'lantern' : 'balloon') : '';
    let nm = P.adj + ' ' + F.nouns[i % F.nouns.length]; if (kind === 'rain') nm = P.adj + ' Rainy Roofs'; if (kind === 'lantern') nm = P.adj + ' Lantern Sky'; while (names.has(nm)) nm += ' II'; names.add(nm);
    const def = F.make(P, inkv, live, rng(seed * 3 + 1), kind);
    defScene({ id: fam + '_' + i, name: nm, mode: live ? 'live' : 'still', theme: light ? 'light' : 'dark', style: inkv ? 'ink' : 'min', seed, init: def.init, st: def.st, fx: def.fx });
  } fi++ });
})();
