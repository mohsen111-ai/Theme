(() => {
const { SCENES, W, H, renderLayers, drawFrame } = NYX;
const $ = s => document.querySelector(s);
const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
const live = [];
let last = 0;
const modal = { rt: null };
function makeRT(canvas, sc, scale) { canvas.width = Math.round(W * scale); canvas.height = Math.round(H * scale); const c = canvas.getContext('2d'), layers = renderLayers(sc, scale); return { draw(t) { c.setTransform(scale, 0, 0, scale, 0, 0); c.clearRect(0, 0, W, H); drawFrame(c, sc, layers, t) } } }
function loop(ts) { requestAnimationFrame(loop); if (ts - last < 33) return; last = ts; const t = ts / 1000; for (const v of live) if (v.on && v.rt) v.rt.draw(t); if (modal.rt) modal.rt.draw(t) }
const FILTERS = [['All', () => true], ['Dark', s => s.theme === 'dark'], ['Light', s => s.theme === 'light'], ['Still', s => s.mode === 'still'], ['Live', s => s.mode === 'live'], ['Long', s => s.mode === 'long'], ['Minimal', s => s.style === 'min'], ['Ink sketch', s => s.style === 'ink']];
const cards = [];
function grid() {
  const gr = $('#grid'), io = new IntersectionObserver(es => es.forEach(e => { const v = e.target._v; v.on = e.isIntersecting; if (v.on && !v.rt) { v.rt = makeRT(v.cv, v.sc, 1.5); v.rt.draw(reduce ? 3 : performance.now() / 1000) } }), { rootMargin: '200px' });
  SCENES.forEach(sc => { const b = document.createElement('button'); b.type = 'button'; b.className = 'card'; b.setAttribute('aria-label', sc.name + ', ' + sc.mode + ', ' + sc.theme);
    const ph = document.createElement('div'); ph.className = 'ph'; const cv = document.createElement('canvas'); ph.appendChild(cv); b.appendChild(ph);
    const tg = document.createElement('div'); tg.className = 'tags'; [[sc.style === 'min' ? 'Minimal' : 'Ink sketch', ''], [sc.theme === 'dark' ? 'Dark' : 'Light', ''], [sc.mode === 'long' ? 'Long' : sc.mode === 'live' ? 'Live' : 'Still', sc.mode === 'still' ? '' : 'm']].forEach(([t, k]) => { const sp = document.createElement('span'); sp.className = 'tag ' + k; sp.textContent = t; tg.appendChild(sp) }); b.appendChild(tg);
    const nm = document.createElement('b'); nm.textContent = sc.name; b.appendChild(nm); gr.appendChild(b);
    const v = { cv, sc, on: false, rt: null }; b._v = v; cards.push([b, v]); if (sc.mode !== 'still') live.push(v); io.observe(b); b.addEventListener('click', () => openModal(sc)) });
  const ch = $('#chips'); FILTERS.forEach(([n, fn], i) => { const b = document.createElement('button'); b.type = 'button'; b.className = 'chip'; b.textContent = n; b.setAttribute('aria-pressed', i === 0); b.addEventListener('click', () => { ch.querySelectorAll('.chip').forEach(x => x.setAttribute('aria-pressed', 'false')); b.setAttribute('aria-pressed', 'true'); cards.forEach(([el, v]) => el.hidden = !fn(v.sc)) }); ch.appendChild(b) });
  const rows = [['Dark', s => s.theme === 'dark'], ['Light, muted', s => s.theme === 'light'], ['All', () => true]], tb = $('#counts');
  rows.forEach(([n, f]) => { const L = SCENES.filter(f), tr = document.createElement('tr'); [n, L.filter(s => s.mode === 'still').length, L.filter(s => s.mode === 'live').length, L.filter(s => s.mode === 'long').length, L.length].forEach(v => { const td = document.createElement('td'); td.textContent = v; tr.appendChild(td) }); tb.appendChild(tr) }) }
const dlg = $('#dlg'), big = $('#big');
function openModal(sc) { modal.rt = makeRT(big, sc, 2); modal.rt.draw(performance.now() / 1000); if (!dlg.open) dlg.showModal(); $('#closeBtn').focus() }
function closeModal() { modal.rt = null; dlg.close() }
$('#closeBtn').addEventListener('click', closeModal); dlg.addEventListener('click', e => { if (e.target === dlg) closeModal() }); dlg.addEventListener('close', () => { modal.rt = null });
window.NYXVIEW = { grid };
if (typeof NYXICONS !== 'undefined') { NYXICONS.showPacks('#icons'); NYXICONS.showAppIcon('#appicon') }
grid(); if (!reduce) requestAnimationFrame(loop);
})();
