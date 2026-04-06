import puppeteer from 'puppeteer';
import { readFileSync, mkdirSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));
const RAW = join(__dirname, 'screenshots', 'raw');
const FINAL = join(__dirname, 'screenshots', 'final');
mkdirSync(FINAL, { recursive: true });

const frames = [
  { id: '01', headline: 'your life in color', desc: 'beautiful palettes from your photo gallery', img: '01-hero.png' },
  { id: '02', headline: 'week. month. year.', desc: 'see your colors at every scale', img: '02-month.png' },
  { id: '03', headline: 'your year in five colors', desc: 'a fingerprint of how you see the world', img: '03-year.png' },
  { id: '04', headline: 'share your palette', desc: 'beautiful 9:16 story cards, ready to post', img: '04-share.png' },
  { id: '05', headline: '31,000 color names', desc: 'every color gets an evocative name', img: '05-colors.png' },
  { id: '06', headline: 'your colors, your way', desc: 'depth, bands, and folder control', img: '06-settings.png' },
];

function buildHtml({ headline, desc, img }) {
  const imgBuffer = readFileSync(join(RAW, img));
  const imgBase64 = `data:image/png;base64,${imgBuffer.toString('base64')}`;
  return `<!DOCTYPE html>
<html><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600&display=swap" rel="stylesheet">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{background:#F8F7F5;width:1080px;height:1920px;overflow:hidden}
.frame{width:1080px;height:1920px;background:#F8F7F5;display:flex;flex-direction:column;align-items:center}
.top{flex-shrink:0;display:flex;flex-direction:column;align-items:center;padding-top:80px;padding-bottom:40px}
.wm{font-family:'Outfit',sans-serif;font-weight:600;font-size:28px;letter-spacing:6px;text-transform:uppercase;color:#1a1a1a;margin-bottom:32px}
.hl{font-family:'Outfit',sans-serif;font-weight:300;font-size:60px;color:#1a1a1a;text-align:center;letter-spacing:-0.5px;padding:0 60px;line-height:1.15}
.ds{font-family:'Outfit',sans-serif;font-weight:400;font-size:30px;color:#999;text-align:center;margin-top:16px;padding:0 80px}
.pw{flex:1;display:flex;align-items:flex-start;justify-content:center;padding-top:20px;overflow:hidden}
.ph{width:640px;height:1388px;border-radius:40px;overflow:hidden;border:2px solid rgba(0,0,0,0.12);box-shadow:0 16px 48px rgba(0,0,0,0.08),0 4px 16px rgba(0,0,0,0.04);background:#F8F7F5;flex-shrink:0;position:relative}
.ph img{width:100%;height:auto;position:absolute;top:-30px;left:0}
</style></head><body>
<div class="frame">
  <div class="top">
    <div class="wm">hued</div>
    <div class="hl">${headline}</div>
    <div class="ds">${desc}</div>
  </div>
  <div class="pw"><div class="ph"><img src="${imgBase64}"></div></div>
</div>
</body></html>`;
}

const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox'] });

for (const frame of frames) {
  const page = await browser.newPage();
  await page.setViewport({ width: 1080, height: 1920, deviceScaleFactor: 1 });
  await page.setContent(buildHtml(frame), { waitUntil: 'networkidle0' });
  // Wait for font to load
  await page.evaluate(() => document.fonts.ready);
  await page.screenshot({ path: join(FINAL, `${frame.id}.png`), fullPage: false });
  await page.close();
  console.log(`Rendered ${frame.id} -> ${frame.headline}`);
}

await browser.close();
console.log('\nDone! Check store/screenshots/final/');
