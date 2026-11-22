(function () {
  // Launch gate: shows a countdown instead of the home until LAUNCH. Add ?preview to the URL to see the real home.
  var LAUNCH = Date.parse('2026-11-19T00:00:00+01:00');
  try { if (/[?&]preview\b/.test(location.search)) sessionStorage.setItem('ya_preview', '1'); if (sessionStorage.getItem('ya_preview')) return; } catch (e) {}
  if (Date.now() >= LAUNCH) return;
  var dc = (window.YA && window.YA.discord) || 'https://discord.gg/yaproject';
  var st = document.createElement('style');
  st.textContent = '#soon{position:fixed;inset:0;z-index:99999;background:#000;display:flex;align-items:center;justify-content:center;text-align:center;padding:24px;overflow:hidden;font-family:Inter,"Segoe UI",system-ui,sans-serif;color:#f4f4f5}' +
    '#soon:before{content:"";position:absolute;left:50%;top:46%;width:1100px;height:1100px;max-width:160vw;max-height:160vw;transform:translate(-50%,-50%);background:radial-gradient(circle,rgba(225,6,0,.38) 0%,rgba(225,6,0,.14) 32%,rgba(0,0,0,0) 65%);animation:sg 4s ease-in-out infinite}' +
    '@keyframes sg{0%,100%{opacity:.75;transform:translate(-50%,-50%) scale(.96)}50%{opacity:1;transform:translate(-50%,-50%) scale(1.04)}}' +
    '#soon .in{position:relative;max-width:720px;width:100%}#soon img{display:block;height:64px;margin:0 auto 26px}' +
    '#soon h1{font-size:clamp(34px,7vw,64px);font-weight:900;letter-spacing:-.02em;line-height:1.05;margin:0 0 10px}#soon h1 b{color:#e10600}' +
    '#soon p{color:#9a9aa5;font-size:17px;margin:0 auto 34px;max-width:520px}' +
    '#soon .cd{display:flex;gap:12px;justify-content:center;margin-bottom:38px}' +
    '#soon .cd div{min-width:78px;flex:1;max-width:130px;padding:18px 6px 14px;border:1px solid #30303a;border-radius:16px;background:rgba(16,16,20,.72);box-shadow:0 0 40px rgba(225,6,0,.12)}' +
    '#soon .cd b{display:block;font-size:clamp(30px,7vw,52px);font-weight:900;font-variant-numeric:tabular-nums;line-height:1}' +
    '#soon .cd span{display:block;margin-top:8px;font-size:11px;letter-spacing:.16em;text-transform:uppercase;color:#6b6b76;font-weight:700}' +
    '#soon a.b{display:inline-block;padding:15px 30px;border-radius:12px;background:#f4f4f5;color:#09090b;font-weight:800;text-decoration:none;font-size:16px}#soon a.b:hover{background:#fff}' +
    '#soon .cp{position:absolute;left:0;right:0;bottom:22px;font-size:12px;color:#6b6b76;letter-spacing:.04em}' +
    '#soon small{display:block;margin-top:16px;color:#6b6b76;font-size:13px}';
  document.head.appendChild(st);
  var d = document.createElement('div'); d.id = 'soon';
  d.innerHTML = '<div class="in"><h1>YA <b>CLIENT</b></h1><p>Something big is coming. The new PvP client opens soon.</p>' +
    '<div class="cd"><div><b id="sd">00</b><span>Days</span></div><div><b id="sh">00</b><span>Hours</span></div><div><b id="sm">00</b><span>Minutes</span></div><div><b id="ss">00</b><span>Seconds</span></div></div>' +
    '<a class="b" href="' + dc + '" target="_blank" rel="noopener">Join the Discord</a><small>Join the Discord in the meantime to get the news first.</small></div>' +
    '<div class="cp">© ' + new Date().getFullYear() + ' YA CLIENT · Powered by YA Projects · All rights reserved.</div>';
  document.body.appendChild(d);
  document.documentElement.style.overflow = 'hidden';
  function p(n) { return n < 10 ? '0' + n : '' + n; }
  function tick() {
    var t = Math.max(0, LAUNCH - Date.now()), s = Math.floor(t / 1000);
    document.getElementById('sd').textContent = p(Math.floor(s / 86400));
    document.getElementById('sh').textContent = p(Math.floor(s % 86400 / 3600));
    document.getElementById('sm').textContent = p(Math.floor(s % 3600 / 60));
    document.getElementById('ss').textContent = p(s % 60);
    if (t <= 0) location.reload();
  }
  tick(); setInterval(tick, 1000);
})();
