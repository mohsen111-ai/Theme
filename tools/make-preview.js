// Inline template + engine + scenes (+ optional icons/appicon) + viewer into one html file.
const fs = require('fs'), path = require('path');
const dir = __dirname, out = path.join(dir, 'out'); fs.mkdirSync(out, { recursive: true });
const read = f => fs.readFileSync(path.join(dir, f), 'utf8');
const order = ['engine.js', ...fs.readdirSync(path.join(dir, 'scenes')).filter(f => f.endsWith('.js')).sort().map(f => 'scenes/' + f)];
const only = process.argv.includes('--wallpapers-only');
if (!only) for (const opt of ['icons.js', 'appicon.js']) if (fs.existsSync(path.join(dir, opt))) order.push(opt);
order.push('viewer.js');
const scripts = order.map(f => '/* ' + f + ' */\n' + read(f)).join('\n').replace(/<\/script>/g, '<\\/script>');
let html = read(only ? 'template-wallpapers.html' : 'template.html').replace('/*__EYEBROW__*/', process.argv.find((a, i) => i > 1 && !a.startsWith('--')) || 'Look preview 3').replace('/*__SCRIPTS__*/', () => scripts);
const outName = only ? 'wallpapers.html' : 'preview.html'; fs.writeFileSync(path.join(out, outName), html); console.log('wrote', path.join(out, outName), (html.length / 1024).toFixed(0) + 'KB');
