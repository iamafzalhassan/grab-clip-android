const fs = require('fs');
const [, , launcherOut, splashOut, previewOut] = process.argv;
const shapes = [
  { d: [['M', 32.4, 718], ['A', 966, 0, 1, 1899.6, 718], ['L', 1615.2, 718], ['A', 695, 0, 0, 316.8, 718], ['Z']],
    fills: [{ s: [60, 760], e: [1880, 300], stops: [[0, 'FFFBB46F'], [0.18, 'FFFCD75C'], [0.38, 'FFD8E28A'], [0.55, 'FFA6EBBE'], [0.78, 'FF6FFBE8'], [1, 'FF6CFDF6']] }] },
  { d: [['M', 0, 841], ['L', 1098, 841], ['L', 1098, 1925], ['L', 827, 1925], ['L', 827, 1288], ['L', 373, 1741], ['L', 190, 1558], ['L', 650, 1098], ['L', 0, 1098], ['Z']],
    fills: [{ s: [0, 841], e: [300, 1741], stops: [[0, 'FFFCA684'], [0.45, 'FFFF8AA0'], [1, 'FFFF5FCF']] },
            { s: [350, 0], e: [1098, 0], stops: [[0, '00F5D6E8'], [0.6, 'B3EBD2E2'], [1, 'E6E0E4D9']] }] },
  { d: [['M', 1274, 841], ['L', 1923.9, 841], ['A', 966, 0, 1, 1220, 1898], ['L', 1220, 1612.9], ['A', 695, 0, 0, 1648.3, 1098], ['L', 1274, 1098], ['Z']],
    fills: [{ s: [1700, 841], e: [1500, 1900], stops: [[0, 'FF94FFF7'], [0.45, 'FFC8F8F6'], [0.75, 'FFEEF0F6'], [1, 'FFF6E4F0']] }] },
];
const n = (v) => String(Math.round(v * 100) / 100);
function pathData(cmds, k, t) {
  const p = (x, y) => `${n(x * k + t)},${n(y * k + t)}`;
  return cmds.map((c) => c[0] === 'Z' ? 'Z' : c[0] === 'A' ? `A${n(c[1] * k)},${n(c[1] * k)} 0 ${c[2]} ${c[3]} ${p(c[4], c[5])}` : `${c[0]}${p(c[1], c[2])}`).join('');
}
function vector(dp, viewport, k) {
  const t = viewport / 2 - 966 * k;
  const body = shapes.flatMap((sh) => sh.fills.map((f) => {
    const items = f.stops.map(([o, c]) => `                <item\n                    android:color="#${c}"\n                    android:offset="${o}" />`).join('\n');
    return `    <path android:pathData="${pathData(sh.d, k, t)}">\n        <aapt:attr name="android:fillColor">\n            <gradient\n                android:endX="${n(f.e[0] * k + t)}"\n                android:endY="${n(f.e[1] * k + t)}"\n                android:startX="${n(f.s[0] * k + t)}"\n                android:startY="${n(f.s[1] * k + t)}"\n                android:type="linear">\n${items}\n            </gradient>\n        </aapt:attr>\n    </path>`;
  })).join('\n');
  return `<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:aapt="http://schemas.android.com/aapt"\n    android:width="${dp}dp"\n    android:height="${dp}dp"\n    android:viewportWidth="${viewport}"\n    android:viewportHeight="${viewport}">\n${body}\n</vector>\n`;
}
function svg(viewport, k, bg, mask) {
  const t = viewport / 2 - 966 * k;
  let defs = '', body = '', i = 0;
  for (const sh of shapes) for (const f of sh.fills) {
    const stops = f.stops.map(([o, c]) => `<stop offset="${o}" stop-color="#${c.slice(2)}" stop-opacity="${parseInt(c.slice(0, 2), 16) / 255}"/>`).join('');
    defs += `<linearGradient id="g${viewport}_${i}" gradientUnits="userSpaceOnUse" x1="${f.s[0] * k + t}" y1="${f.s[1] * k + t}" x2="${f.e[0] * k + t}" y2="${f.e[1] * k + t}">${stops}</linearGradient>`;
    body += `<path d="${pathData(sh.d, k, t)}" fill="url(#g${viewport}_${i++})"/>`;
  }
  return `<svg xmlns="http://www.w3.org/2000/svg" width="360" height="360" viewBox="0 0 ${viewport} ${viewport}"><defs>${defs}</defs><circle cx="${viewport / 2}" cy="${viewport / 2}" r="${mask}" fill="${bg}"/>${body}</svg>`;
}
fs.writeFileSync(launcherOut, vector(108, 108, 0.027));
fs.writeFileSync(splashOut, vector(288, 1000, 0.28));
if (previewOut) fs.writeFileSync(previewOut, `<html><body style="margin:0;background:#888;display:flex;gap:16px;padding:16px">${svg(108, 0.027, '#F5F3F0', 36)}${svg(1000, 0.28, '#F5F3F0', 333)}</body></html>`);
