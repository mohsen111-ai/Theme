/* Icon packs + app icon. Drawn with the same pen as the wallpapers. Exported to PNG by build.js. */
const NYXICONS = (() => {
const { rng, lerp, mixh, ink, blob, circ, glow, wob, path, TAU } = NYX;
const GLYPHS = ['phone', 'messages', 'contacts', 'camera', 'browser', 'music', 'video', 'maps', 'clock', 'settings', 'gallery', 'mail', 'calendar', 'notes', 'calculator', 'files', 'weather', 'store', 'games', 'wallet', 'health', 'cloud', 'shield', 'assistant', 'recorder', 'book', 'download', 'chat', 'social'];
const darker = (h, k) => mixh(h, '#2a1c14', k), lighter = (h, k) => mixh(h, '#fff6e0', k);
/* watercolor shape: flat colour, pigment pooled at the edge, blotches, thin ink outline */
function wc(c, r, pts, col, line) { c.save(); c.beginPath(); path(c, wob(r, pts.concat([pts[0]]), .8), true); c.fillStyle = col; c.fill(); c.clip();
  c.lineWidth = 5; c.strokeStyle = darker(col, .3); c.globalAlpha = .32; c.stroke();
  const xs = pts.map(p => p[0]), ys = pts.map(p => p[1]), x0 = Math.min(...xs), x1 = Math.max(...xs), y0 = Math.min(...ys), y1 = Math.max(...ys);
  for (let i = 0; i < 8; i++) { c.globalAlpha = .17; c.fillStyle = r() < .5 ? lighter(col, .55) : darker(col, .3); c.beginPath(); c.arc(lerp(x0, x1, r()), lerp(y0, y1, r()), 3 + r() * 8, 0, TAU); c.fill() }
  c.globalAlpha = 1; c.restore(); ink(c, r, pts, line || darker(col, .62), 1.3, .9, true, 1) }
const gear = (cx, cy, ro, ri, n) => { const p = []; for (let i = 0; i < n * 4; i++) { const a = i / (n * 4) * TAU, rad = [ro, ro, ri, ri][i % 4]; p.push([cx + Math.cos(a) * rad, cy + Math.sin(a) * rad]) } return p };
const HEART = [[0, 22], [-22, 0], [-24, -10], [-16, -18], [-8, -16], [0, -8], [8, -16], [16, -18], [24, -10], [22, 0]];
const SPARK = [[0, -26], [5, -6], [24, 0], [5, 6], [0, 26], [-5, 6], [-24, 0], [-5, -6]];
const CLOUD = [[-22, 18], [-26, 10], [-18, 2], [-10, 4], [-4, -4], [8, -2], [12, 6], [22, 8], [26, 16], [20, 20]];
const sc = (pts, k) => pts.map(p => [p[0] * k, p[1] * k]);
const rrect = (x0, y0, x1, y1, k = 4) => [[x0, y0 + k], [x0 + k, y0], [x1 - k, y0], [x1, y0 + k], [x1, y1 - k], [x1 - k, y1], [x0 + k, y1], [x0, y1 - k]];

const PAPER = {
  phone(c, r) { wc(c, r, [[-24, 8], [-24, 22], [24, 22], [24, 8], [16, -4], [-16, -4]], '#7aa06a'); wc(c, r, [[-27, -8], [-21, -15], [21, -15], [27, -8], [23, -3], [14, -8], [-14, -8], [-23, -3]], '#6a8e5c'); wc(c, r, circ(0, 9, 9, 12), '#efe6c8'); for (let i = 0; i < 6; i++) { const a = i / 6 * TAU; wc(c, r, circ(Math.cos(a) * 5.2, 9 + Math.sin(a) * 5.2, 1.5, 6), '#7a6a50') } },
  messages(c, r) { wc(c, r, [[-24, -14], [-16, -22], [16, -22], [24, -14], [24, 6], [16, 14], [2, 14], [-10, 24], [-8, 14], [-16, 14], [-24, 6]], '#79b0d8'); wc(c, r, [[-16, -12], [16, -12], [16, 2], [-16, 2]], '#a9d0ea') },
  contacts(c, r) { wc(c, r, [[-20, -24], [20, -24], [20, 24], [-20, 24]], '#5f93c0'); wc(c, r, [[-25, -24], [-18, -24], [-18, 24], [-25, 24]], '#3f6f9a'); wc(c, r, circ(3, -7, 7, 10), '#f3ecd6'); wc(c, r, [[-8, 14], [-5, 3], [11, 3], [14, 14]], '#f3ecd6'); wc(c, r, [[20, -14], [27, -14], [27, -6], [20, -6]], '#e9a85c'); wc(c, r, [[20, 2], [27, 2], [27, 10], [20, 10]], '#c9503e') },
  camera(c, r) { wc(c, r, [[-12, -8], [-8, -17], [8, -17], [12, -8]], '#9a4f3c'); wc(c, r, [[-27, -8], [27, -8], [27, 19], [-27, 19]], '#b5604a'); wc(c, r, circ(0, 5, 12, 14), '#555a60'); wc(c, r, circ(0, 5, 7, 12), '#8fb1c4'); wc(c, r, circ(-2, 3, 2, 6), '#f6efd8'); wc(c, r, [[16, -12], [22, -12], [22, -9], [16, -9]], '#e8d9a8') },
  browser(c, r) { wc(c, r, circ(0, 0, 20, 16), '#6fa3cf'); wc(c, r, [[-12, -10], [-4, -14], [0, -6], [-6, 2], [-14, 0]], '#7fa86a'); wc(c, r, [[4, 2], [14, 0], [16, 8], [8, 14], [2, 8]], '#7fa86a'); const e = []; for (let i = 0; i <= 24; i++) { const a = i / 24 * TAU; e.push([Math.cos(a) * 27, Math.sin(a) * 8]) } ink(c, r, e.map(p => [p[0] * Math.cos(-.5) - p[1] * Math.sin(-.5), p[0] * Math.sin(-.5) + p[1] * Math.cos(-.5)]), '#4a3f30', 1.6, .8, true, 1) },
  music(c, r) { wc(c, r, circ(0, 0, 23, 20), '#4a3f3a'); ink(c, r, circ(0, 0, 17, 18), 'rgba(255,240,210,.35)', 1, .6, true, 1); ink(c, r, circ(0, 0, 13, 16), 'rgba(255,240,210,.3)', 1, .6, true, 1); wc(c, r, circ(0, 0, 8, 12), '#c9704a'); wc(c, r, circ(0, 0, 2, 6), '#f3ecd6') },
  video(c, r) { wc(c, r, rrect(-27, -17, 27, 17, 5), '#c9604a'); wc(c, r, [[-7, -10], [12, 0], [-7, 10]], '#f6efd8') },
  maps(c, r) { wc(c, r, [[-26, -14], [-9, -19], [9, -14], [26, -19], [26, 16], [9, 21], [-9, 16], [-26, 21]], '#e8d8a8'); ink(c, r, [[-9, -19], [-9, 16]], '#7a6a50', 1, .6, false, 1); ink(c, r, [[9, -14], [9, 21]], '#7a6a50', 1, .6, false, 1); wc(c, r, [[0, 14], [-9, 0], [-9, -6], [-4, -12], [4, -12], [9, -6], [9, 0]], '#c9503e'); wc(c, r, circ(0, -5, 3, 8), '#f3ecd6') },
  clock(c, r) { wc(c, r, circ(0, 0, 24, 20), '#555a60'); wc(c, r, circ(0, 0, 20, 20), '#f3ecd6'); for (let i = 0; i < 12; i++) { const a = i / 12 * TAU; ink(c, r, [[Math.cos(a) * 16, Math.sin(a) * 16], [Math.cos(a) * 18, Math.sin(a) * 18]], '#4a3f30', 1.2, .3, false, 1) } ink(c, r, [[0, 0], [0, -13]], '#2e2620', 2, .4, false, 1); ink(c, r, [[0, 0], [9, 5]], '#2e2620', 2, .4, false, 1) },
  settings(c, r) { wc(c, r, gear(-6, 6, 19, 14, 8), '#6b6a68'); wc(c, r, circ(-6, 6, 6, 12), '#cfc8b8'); wc(c, r, gear(12, -12, 12, 8.5, 6), '#55544f'); wc(c, r, circ(12, -12, 3.5, 10), '#cfc8b8') },
  gallery(c, r) { wc(c, r, [[-24, -22], [24, -22], [24, 22], [-24, 22]], '#f6f0e0'); wc(c, r, [[-19, -17], [19, -17], [19, 9], [-19, 9]], '#9cc6e0'); wc(c, r, [[-19, 9], [-7, -3], [3, 5], [10, -1], [19, 6], [19, 9]], '#7fa86a'); wc(c, r, circ(9, -8, 4, 10), '#f0c466') },
  mail(c, r) { wc(c, r, [[-16, -18], [16, -18], [16, 4], [-16, 4]], '#f6efd8'); wc(c, r, [[-26, -12], [26, -12], [26, 18], [-26, 18]], '#e9d9a8'); ink(c, r, [[-26, -12], [0, 6], [26, -12]], '#7a6a50', 1.2, .6, false, 1); wc(c, r, circ(0, 6, 3.4, 8), '#c9503e') },
  calendar(c, r) { wc(c, r, [[-22, -18], [22, -18], [22, 22], [-22, 22]], '#f3ecd6'); wc(c, r, [[-22, -18], [22, -18], [22, -6], [-22, -6]], '#b5604a'); ink(c, r, [[-10, -24], [-10, -14]], '#4a3f30', 2, .3, false, 1); ink(c, r, [[10, -24], [10, -14]], '#4a3f30', 2, .3, false, 1);[-12, 0, 12].forEach(x => [4, 13].forEach(y => wc(c, r, circ(x, y, 2.4, 6), '#8a6a50'))) },
  notes(c, r) { c.save(); c.rotate(-.18); wc(c, r, [[-16, -16], [14, -16], [14, 12], [-16, 12]], '#e9a85c'); c.restore(); c.save(); c.rotate(.1); wc(c, r, [[-14, -14], [16, -14], [16, 14], [-14, 14]], '#e8d07a');[-6, 0, 6].forEach(y => ink(c, r, [[-8, y], [9, y]], '#7a6a50', 1, .3, false, 1)); c.restore() },
  calculator(c, r) { wc(c, r, rrect(-17, -25, 17, 25, 3), '#6e6c68'); wc(c, r, [[-12, -20], [12, -20], [12, -9], [-12, -9]], '#e8d9a8'); for (let j = 0; j < 3; j++)for (let i = 0; i < 3; i++)wc(c, r, [[-12 + i * 9, -3 + j * 9], [-6 + i * 9, -3 + j * 9], [-6 + i * 9, 3 + j * 9], [-12 + i * 9, 3 + j * 9]], i === 2 ? '#c9503e' : (i + j) % 2 ? '#cfc8b8' : '#e9a85c') },
  files(c, r) { wc(c, r, [[-26, -18], [-8, -18], [-3, -12], [26, -12], [26, 20], [-26, 20]], '#c9a56a'); wc(c, r, [[-26, -5], [26, -5], [26, 20], [-26, 20]], '#e6c68e') },
  weather(c, r) { wc(c, r, circ(-6, -9, 12, 14), '#f0c466'); wc(c, r, CLOUD, '#f6efe0') },
  store(c, r) { wc(c, r, [[-20, -8], [20, -8], [24, 24], [-24, 24]], '#d8b080'); ink(c, r, [[-8, -8], [-8, -18], [8, -18], [8, -8]], '#7a5a30', 2, .6, false, 1); wc(c, r, [[-8, 4], [11, 4], [-8, 13]], '#5f93c0'); wc(c, r, [[-8, 13], [11, 13], [-8, 22]], '#7fa86a'); wc(c, r, [[-8, 4], [-8, 22], [11, 13]], '#c9503e') },
  games(c, r) { wc(c, r, [[-26, -6], [-18, -14], [18, -14], [26, -6], [28, 10], [22, 16], [14, 10], [-14, 10], [-22, 16], [-28, 10]], '#8a6ad0'); wc(c, r, [[-17, -6], [-13, -6], [-13, -2], [-9, -2], [-9, 2], [-13, 2], [-13, 6], [-17, 6], [-17, 2], [-21, 2], [-21, -2], [-17, -2]], '#f3ecd6'); wc(c, r, circ(14, -4, 3.4, 8), '#f0c466'); wc(c, r, circ(20, 2, 3.4, 8), '#c9503e') },
  wallet(c, r) { wc(c, r, [[-26, -14], [24, -14], [24, 18], [-26, 18]], '#9a6a42'); wc(c, r, [[-26, -14], [24, -14], [24, -5], [-26, -5]], '#b98456'); wc(c, r, circ(15, 6, 5.5, 10), '#e8d9a8') },
  health(c, r) { wc(c, r, HEART, '#d0585a'); ink(c, r, [[-18, 2], [-8, 2], [-4, -8], [2, 12], [6, 2], [18, 2]], '#f6efd8', 2, .5, false, 1) },
  cloud(c, r) { wc(c, r, sc(CLOUD, 1.15), '#9cc6e0'); ink(c, r, [[0, 16], [0, 0]], '#f6efd8', 2.4, .4, false, 1); ink(c, r, [[-7, 7], [0, 0], [7, 7]], '#f6efd8', 2.4, .4, false, 1) },
  shield(c, r) { wc(c, r, [[0, -26], [22, -18], [22, 2], [0, 26], [-22, 2], [-22, -18]], '#7fa86a'); wc(c, r, [[4, -14], [-8, 2], [0, 2], [-4, 16], [10, -2], [2, -2]], '#f0c466') },
  assistant(c, r) { wc(c, r, SPARK, '#8a6ad0'); wc(c, r, [[16, -22], [18, -15], [25, -13], [18, -11], [16, -4], [14, -11], [7, -13], [14, -15]], '#f0c466') },
  recorder(c, r) { wc(c, r, [[-9, 3], [-9, -16], [-5, -22], [5, -22], [9, -16], [9, 3], [5, 9], [-5, 9]], '#8a8884'); ink(c, r, [[-16, -2], [-16, 8], [-8, 16], [0, 18], [8, 16], [16, 8], [16, -2]], '#4a3f30', 2, .6, false, 1); ink(c, r, [[0, 18], [0, 26]], '#4a3f30', 2, .4, false, 1); ink(c, r, [[-8, 26], [8, 26]], '#4a3f30', 2, .4, false, 1) },
  book(c, r) { wc(c, r, [[-27, -16], [0, -12], [27, -16], [27, 20], [0, 24], [-27, 20]], '#b5604a'); wc(c, r, [[-23, -14], [-1, -10], [-1, 18], [-23, 15]], '#f3ecd6'); wc(c, r, [[1, -10], [23, -14], [23, 15], [1, 18]], '#f6efd8');[-6, 2, 10].forEach(y => { ink(c, r, [[-19, y - 2], [-5, y]], '#7a6a50', .9, .3, false, 1); ink(c, r, [[5, y], [19, y - 2]], '#7a6a50', .9, .3, false, 1) }) },
  download(c, r) { wc(c, r, [[-24, 8], [-24, 22], [24, 22], [24, 8], [18, 8], [18, 16], [-18, 16], [-18, 8]], '#7a6a50'); wc(c, r, [[-6, -24], [6, -24], [6, -4], [14, -4], [0, 10], [-14, -4], [-6, -4]], '#7fa86a') },
  chat(c, r) { wc(c, r, [[-26, -12], [-16, -22], [6, -22], [16, -12], [16, 4], [6, 14], [-8, 14], [-16, 22], [-14, 12], [-22, 8]], '#7fa86a'); wc(c, r, [[6, 2], [24, 2], [28, 10], [26, 20], [18, 20], [14, 26], [14, 20], [6, 20], [2, 12]], '#f3ecd6') },
  social(c, r) { wc(c, r, rrect(-23, -23, 23, 23, 7), '#d8708a'); wc(c, r, sc(HEART, .62), '#f6efd8') },
  tile(c, r) { c.save(); c.rotate(-.07); wc(c, r, [[-33, -31], [31, -31], [33, 31], [-31, 33]], '#e4d8b8'); c.restore(); c.save(); c.rotate(.04); wc(c, r, [[-31, -30], [31, -31], [31, 31], [-31, 30]], '#f1e8cf'); c.restore() },
};

/* line glyphs: g(points, closed, fill) where fill 0 = outline only, 1 = solid, in between = tinted */
const LINE = {
  phone(g) { g([[-16, -20], [-8, -24], [-4, -14], [-10, -8], [-2, 8], [8, 14], [12, 8], [22, 12], [18, 22], [6, 24], [-16, 6], [-24, -8]], true, 1) },
  messages(g) { g([[-22, -14], [-14, -20], [14, -20], [22, -14], [22, 6], [14, 12], [0, 12], [-10, 22], [-8, 12], [-14, 12], [-22, 6]], true, .4);[-8, 0, 8].forEach(x => g(circ(x, -4, 2), true, 1)) },
  contacts(g) { g([[-20, -24], [20, -24], [20, 24], [-20, 24]], true, 0); g(circ(0, -8, 7), true, 1); g([[-13, 18], [-10, 4], [10, 4], [13, 18]], true, 1) },
  camera(g) { g([[-24, -10], [-24, 16], [24, 16], [24, -10], [12, -10], [8, -18], [-8, -18], [-12, -10]], true, .2); g(circ(0, 3, 9), true, 0); g(circ(0, 3, 4), true, 1) },
  browser(g) { g(circ(0, 0, 22, 24), true, 0); g([[0, -22], [-9, 0], [0, 22]], false, 0); g([[0, -22], [9, 0], [0, 22]], false, 0); g([[-22, 0], [22, 0]], false, 0) },
  music(g) { g(circ(0, 0, 22, 24), true, 0); g(circ(0, 0, 7), true, 1); g(circ(0, 0, 14, 20), false, 0) },
  video(g) { g(rrect(-24, -16, 24, 16, 4), true, 0); g([[-6, -8], [10, 0], [-6, 8]], true, 1) },
  maps(g) { g([[0, 24], [-14, 2], [-14, -10], [-8, -20], [0, -24], [8, -20], [14, -10], [14, 2]], true, .3); g(circ(0, -9, 5), true, 1) },
  clock(g) { g(circ(0, 0, 22, 24), true, 0); g([[0, 0], [0, -14]], false, 0); g([[0, 0], [10, 6]], false, 0) },
  settings(g) { g(gear(0, 0, 24, 17, 6), true, .2); g(circ(0, 0, 6), true, 1) },
  gallery(g) { g([[-22, -18], [22, -18], [22, 18], [-22, 18]], true, 0); g([[-22, 14], [-8, -2], [2, 8], [10, 0], [22, 14]], false, 0); g(circ(10, -8, 4), true, 1) },
  mail(g) { g([[-24, -16], [24, -16], [24, 16], [-24, 16]], true, 0); g([[-24, -16], [0, 2], [24, -16]], false, 0) },
  calendar(g) { g([[-20, -18], [20, -18], [20, 22], [-20, 22]], true, 0); g([[-20, -8], [20, -8]], false, 0); g([[-10, -24], [-10, -14]], false, 0); g([[10, -24], [10, -14]], false, 0);[-8, 2, 12].forEach(x => [4, 13].forEach(y => g(circ(x, y, 1.6), true, 1))) },
  notes(g) { g([[-16, -22], [16, -22], [16, 22], [-16, 22]], true, 0);[-10, -2, 6, 14].forEach((y, i) => g([[-9, y], [9 - (i === 3 ? 8 : 0), y]], false, 0)) },
  calculator(g) { g([[-18, -24], [18, -24], [18, 24], [-18, 24]], true, 0); g([[-12, -18], [12, -18], [12, -8], [-12, -8]], true, 0);[-9, 0, 9].forEach(x => [0, 9, 18].forEach(y => g(circ(x, y, 1.9), true, 1))) },
  files(g) { g([[-24, -14], [-8, -14], [-3, -8], [24, -8], [24, 18], [-24, 18]], true, 0) },
  weather(g) { g(circ(-7, -9, 9), true, 0); g(CLOUD, true, 0.15) },
  store(g) { g([[-18, -6], [18, -6], [22, 22], [-22, 22]], true, 0); g([[-8, -6], [-8, -16], [8, -16], [8, -6]], false, 0); g([[-5, 4], [9, 11], [-5, 18]], true, 1) },
  games(g) { g([[-26, -6], [-18, -14], [18, -14], [26, -6], [28, 10], [22, 16], [14, 10], [-14, 10], [-22, 16], [-28, 10]], true, 0); g([[-16, -2], [-8, -2]], false, 0); g([[-12, -6], [-12, 2]], false, 0); g(circ(14, -3, 2.6), true, 1); g(circ(20, 2, 2.6), true, 1) },
  wallet(g) { g([[-24, -12], [22, -12], [22, 18], [-24, 18]], true, 0); g([[-24, -12], [18, -20], [18, -12]], false, 0); g(circ(14, 3, 3), true, 1) },
  health(g) { g(HEART, true, 0); g([[-16, 2], [-8, 2], [-4, -6], [2, 10], [6, 2], [16, 2]], false, 0) },
  cloud(g) { g(sc(CLOUD, 1.1), true, 0); g([[0, 14], [0, -2]], false, 0); g([[-6, 4], [0, -2], [6, 4]], false, 0) },
  shield(g) { g([[0, -24], [20, -17], [20, 2], [0, 24], [-20, 2], [-20, -17]], true, 0); g([[3, -12], [-7, 2], [0, 2], [-3, 14], [8, -2], [1, -2]], true, 1) },
  assistant(g) { g(SPARK, true, 1); g([[16, -22], [18, -15], [25, -13], [18, -11], [16, -4], [14, -11], [7, -13], [14, -15]], true, 1) },
  recorder(g) { g([[-8, 2], [-8, -14], [-4, -20], [4, -20], [8, -14], [8, 2], [4, 8], [-4, 8]], true, 0); g([[-14, -2], [-14, 8], [-7, 15], [0, 17], [7, 15], [14, 8], [14, -2]], false, 0); g([[0, 17], [0, 24]], false, 0) },
  book(g) { g([[-24, -14], [0, -10], [24, -14], [24, 16], [0, 20], [-24, 16]], true, 0); g([[0, -10], [0, 20]], false, 0) },
  download(g) { g([[-22, 8], [-22, 20], [22, 20], [22, 8]], false, 0); g([[0, -22], [0, 8]], false, 0); g([[-9, -1], [0, 8], [9, -1]], false, 0) },
  chat(g) { g([[-22, -12], [-14, -20], [14, -20], [22, -12], [22, 6], [14, 14], [-4, 14], [-14, 22], [-12, 14], [-14, 14], [-22, 6]], true, 0);[-9, 0, 9].forEach(x => g(circ(x, -3, 2), true, 1)) },
  social(g) { g(rrect(-22, -22, 22, 22, 7), true, 0); g(sc(HEART, .6), true, 1) },
};

const SIZE = 96, S = 3;
function mk(draw) { const cv = document.createElement('canvas'); cv.width = cv.height = SIZE * S; const c = cv.getContext('2d'); c.scale(S, S); c.translate(SIZE / 2, SIZE / 2); draw(c); return cv }
const PACKS = {
  paper: { name: 'Paper watercolor', note: 'Painted objects on paper. Colours pool at the edges like real watercolor.', paper: true,
    glyph: (c, name, r) => PAPER[name](c, r), tile: (c, r) => PAPER.tile(c, r) },
  night: { name: 'Night ink', note: 'Cream ink lines on a deep navy tile with a warm glow.',
    base(c, r) { const g = c.createLinearGradient(-48, -48, -48, 48); g.addColorStop(0, '#2a2458'); g.addColorStop(1, '#0c0b22'); c.fillStyle = g; c.beginPath(); rrectPath(c, -44, -44, 44, 44, 20); c.fill(); glow(c, 22, 28, 60, 'rgba(240,165,74,.22)', 1); ink(c, r, [[-40, -40], [40, -41], [41, 40], [-41, 41]], 'rgba(205,200,245,.4)', 1.4, 2, true, 2) },
    glyph(c, name, r) { this.base(c, r); LINE[name]((pts, close, fill) => { if (fill) blob(c, r, pts, fill >= 1 ? 'rgba(240,165,74,.9)' : 'rgba(240,165,74,' + fill * .45 + ')', null, 1, .8); ink(c, r, pts, '#f1e6c8', 2.2, 1.4, close, 2) }) },
    tile(c, r) { this.base(c, r) } },
  moon: { name: 'Moon silhouette', note: 'Solid cream shapes on a dark round tile.',
    base(c) { c.fillStyle = '#0b1228'; c.beginPath(); c.arc(0, 0, 46, 0, TAU); c.fill(); glow(c, 0, -6, 56, 'rgba(170,190,235,.28)', 1) },
    glyph(c, name, r) { this.base(c); LINE[name]((pts, close, fill) => { c.beginPath(); c.moveTo(pts[0][0], pts[0][1]); for (let i = 1; i < pts.length; i++)c.lineTo(pts[i][0], pts[i][1]); if (close) c.closePath(); c.lineJoin = c.lineCap = 'round'; if (close && fill >= 1) { c.fillStyle = '#e8ecf6'; c.fill() } else { c.strokeStyle = '#e8ecf6'; c.lineWidth = 3; c.stroke() } }) },
    tile(c, r) { this.base(c) } },
};
function rrectPath(c, x0, y0, x1, y1, k) { c.moveTo(x0 + k, y0); c.lineTo(x1 - k, y0); c.quadraticCurveTo(x1, y0, x1, y0 + k); c.lineTo(x1, y1 - k); c.quadraticCurveTo(x1, y1, x1 - k, y1); c.lineTo(x0 + k, y1); c.quadraticCurveTo(x0, y1, x0, y1 - k); c.lineTo(x0, y0 + k); c.quadraticCurveTo(x0, y0, x0 + k, y0); c.closePath() }
function render(pack, name) { const pk = PACKS[pack]; return mk(c => { const r = rng(name.length * 17 + pack.length * 5 + name.charCodeAt(0)); if (name === 'tile') pk.tile(c, r); else pk.glyph(c, name, r) }) }

/* ---- app icon: crescent moon with a star on a night sky. Adaptive layers are 108dp -> 432px ---- */
function crescent(c, k, mono) { // k = px per dp
  c.save(); c.scale(k, k);
  if (!mono) { glow(c, 52, 54, 46, 'rgba(255,226,170,.45)', 1) }
  const g = c.createLinearGradient(34, 34, 70, 74); g.addColorStop(0, mono ? '#ffffff' : '#fbf1d6'); g.addColorStop(1, mono ? '#ffffff' : '#e0cd9c');
  c.fillStyle = g; c.beginPath(); c.arc(50, 56, 21, 0, TAU); c.fill();
  c.globalCompositeOperation = 'destination-out'; c.beginPath(); c.arc(59, 49, 18.5, 0, TAU); c.fill(); c.globalCompositeOperation = 'source-over';
  const star = (x, y, R, col) => { c.fillStyle = col; c.beginPath(); for (let i = 0; i < 8; i++) { const a = i / 8 * TAU - Math.PI / 2, rad = i % 2 ? R * .28 : R; c.lineTo(x + Math.cos(a) * rad, y + Math.sin(a) * rad) } c.closePath(); c.fill() };
  star(62, 41, 8, mono ? '#ffffff' : '#ffd98a'); if (!mono) { star(72, 55, 3.4, '#fbf1d6'); star(55, 31, 2.6, '#fbf1d6') }
  c.restore() }
function appIcon(kind) { const cv = document.createElement('canvas'); cv.width = cv.height = 432; const c = cv.getContext('2d'); const k = 4;
  if (kind === 'bg') { const g = c.createRadialGradient(150, 120, 20, 216, 216, 330); g.addColorStop(0, '#2c3578'); g.addColorStop(.6, '#141a46'); g.addColorStop(1, '#090b22'); c.fillStyle = g; c.fillRect(0, 0, 432, 432); const r = rng(5); for (let i = 0; i < 46; i++) { c.fillStyle = 'rgba(240,244,255,' + (.25 + r() * .6) + ')'; c.beginPath(); c.arc(r() * 432, r() * 432, .8 + r() * 1.8, 0, TAU); c.fill() } }
  else crescent(c, k, kind === 'mono'); return cv }

function showPacks(sel) { const box = document.querySelector(sel); if (!box) return; Object.entries(PACKS).forEach(([id, pk]) => { const h = document.createElement('h3'); h.className = 'pack'; h.textContent = pk.name; const p = document.createElement('p'); p.className = 'sec-note'; p.textContent = pk.note; const row = document.createElement('div'); row.className = 'icons' + (pk.paper ? ' paper' : ''); box.append(h, p, row);
  GLYPHS.forEach(g => { const f = document.createElement('figure'), cv = render(id, g); f.appendChild(cv); const cap = document.createElement('figcaption'); cap.textContent = g; f.appendChild(cap); row.appendChild(f) }) }) }
function showAppIcon(sel) { const el = document.querySelector(sel); if (!el) return; el.width = el.height = 432; const c = el.getContext('2d'); c.drawImage(appIcon('bg'), 0, 0); c.drawImage(appIcon('fg'), 0, 0) }
function exportAll() { const out = {}; for (const id of Object.keys(PACKS)) { for (const g of GLYPHS) out['icons/' + id + '/' + g + '.png'] = render(id, g).toDataURL('image/png'); out['icons/' + id + '/tile.png'] = render(id, 'tile').toDataURL('image/png') } for (const k of ['bg', 'fg', 'mono']) out['appicon/' + k + '.png'] = appIcon(k).toDataURL('image/png'); return out }
return { GLYPHS, PACKS, render, appIcon, showPacks, showAppIcon, exportAll };
})();
