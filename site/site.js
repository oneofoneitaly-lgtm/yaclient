(function () {
  var Y = window.YA || {};
  document.querySelectorAll('[data-dc]').forEach(function (a) { a.href = Y.discord; a.target = '_blank'; a.rel = 'noopener'; });
  document.querySelectorAll('[data-dl]').forEach(function (a) {
    if (Y.downloadSoon) { a.textContent = 'Soon'; a.removeAttribute('href'); a.style.pointerEvents = 'none'; a.style.opacity = '.6'; a.setAttribute('aria-disabled', 'true'); return; }
    a.href = '/download'; a.removeAttribute('target');
  });
  var eur = function (n) { return '€' + (Math.round(n * 100) / 100).toFixed(n % 1 ? 2 : 0); };
  window.YAeur = eur;

  // ---- checkout modal: username -> order text for a Discord ticket (+ optional PayPal.Me link) ----
  var mo = document.createElement('div'); mo.className = 'mo';
  mo.innerHTML = '<div class="md"><button class="x" aria-label="Close">×</button><div id="mdb"></div></div>';
  document.addEventListener('DOMContentLoaded', function () { document.body.appendChild(mo); });
  if (document.body) document.body.appendChild(mo);
  mo.addEventListener('click', function (e) { if (e.target === mo || e.target.className === 'x') mo.classList.remove('show'); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') mo.classList.remove('show'); });

  /* order = { title, lines:[names], total:number, ids:'mono_cape,...' | 'plus', note } */
  if (/[?&]paid=1/.test(location.search)) document.addEventListener('DOMContentLoaded', function () {
    var d = document.createElement('div'); d.textContent = 'Payment received. Your items are unlocked on your account within a few seconds: open YA CLIENT to use them.';
    d.style.cssText = 'position:fixed;left:50%;top:18px;transform:translateX(-50%);background:#fff;color:#000;padding:12px 18px;border-radius:12px;font-weight:600;z-index:99;max-width:90vw;text-align:center';
    document.body.appendChild(d); setTimeout(function () { d.remove(); }, 9000);
  });
  window.YABuy = function (order) {
    var b = mo.querySelector('#mdb');
    var free = !order.total;
    b.innerHTML = '<h3>' + (free ? 'Get ' : 'Buy ') + order.title + '</h3><p>' + (free ? 'Free for beta testers.' : 'Delivered to your Minecraft account.') + '</p>' +
      '<div class="sum">' + order.lines.map(function (l) { return '• ' + l; }).join('<br>') + '<br><br>Total: <b>' + (free ? 'Free' : eur(order.total)) + '</b></div>' +
      '<input id="mdu" placeholder="Your Minecraft username" maxlength="16" autocomplete="off" spellcheck="false"><div class="err" id="mde"></div>' +
      '<div class="stack"><button class="btn pri block" id="mdgo">Continue</button></div>';
    mo.classList.add('show');
    var u = b.querySelector('#mdu'); u.focus();
    function go() {
      var name = u.value.trim();
      if (!/^[A-Za-z0-9_]{3,16}$/.test(name)) { b.querySelector('#mde').textContent = 'Enter your exact Minecraft username (3-16 letters, numbers or _).'; return; }
      if (!free) {
        var btn = b.querySelector('#mdgo'); btn.disabled = true; btn.textContent = 'Opening secure payment...';
        fetch('/v1/checkout', { method: 'POST', headers: { 'content-type': 'application/x-www-form-urlencoded' }, body: new URLSearchParams({ username: name, ids: order.ids }).toString() })
          .then(function (r) { return r.json(); })
          .then(function (j) { if (j && j.url) { location.href = j.url; return; } if (j && j.error && j.error !== 'not_configured') { btn.disabled = false; btn.textContent = 'Continue'; b.querySelector('#mde').textContent = j.error; return; } manual(name); })
          .catch(function () { manual(name); });
        return;
      }
      manual(name);
    }
    function manual(name) {
      var ticket = 'Order: ' + order.title + '\nItems: ' + order.ids + '\nMinecraft username: ' + name + '\nTotal: ' + (free ? 'Free' : eur(order.total)) + (order.note ? '\n' + order.note : '');
      var pay = '';
      if (!free && Y.paypalMe) pay = '<a class="btn pri block" target="_blank" rel="noopener" href="https://paypal.me/' + encodeURIComponent(Y.paypalMe) + '/' + order.total.toFixed(2) + (Y.currency || 'EUR') + '">1. Pay ' + eur(order.total) + ' with PayPal</a>';
      b.innerHTML = '<h3>Almost done</h3><p>' + (free ? 'Open a ticket on Discord and paste this text.' : (pay ? 'Pay, then open a ticket on Discord and paste this text with your payment receipt.' : 'Open a ticket on Discord and paste this text. We will send you the payment options.')) + '</p>' +
        '<code id="mdt"></code><div class="stack">' + pay + '<button class="btn block" id="mdc">Copy text</button><a class="btn ' + (pay ? '' : 'pri ') + 'block" target="_blank" rel="noopener" href="' + Y.discord + '">' + (pay ? '2. ' : '') + 'Open Discord</a></div>';
      b.querySelector('#mdt').textContent = ticket;
      b.querySelector('#mdc').onclick = function () { try { navigator.clipboard.writeText(ticket); this.textContent = 'Copied'; } catch (e) { var r = document.createRange(); r.selectNode(b.querySelector('#mdt')); getSelection().removeAllRanges(); getSelection().addRange(r); } };
    }
    b.querySelector('#mdgo').onclick = go;
    u.onkeydown = function (e) { if (e.key === 'Enter') go(); };
  };
})();

/* product photos were re-rendered (cape thickness): bypass old cached copies */
(function(){try{var V='?v=21';function fix(i){var s=i.getAttribute&&i.getAttribute('src');if(s&&s.indexOf('/b/')===0&&s.indexOf('?')<0)i.setAttribute('src',s+V)}
function all(r){var l=(r.querySelectorAll?r.querySelectorAll('img[src^="/b/"]'):[]);for(var k=0;k<l.length;k++)fix(l[k])}
new MutationObserver(function(ms){ms.forEach(function(m){if(m.type==='attributes')fix(m.target);else m.addedNodes.forEach(function(n){if(n.nodeType===1){if(n.tagName==='IMG')fix(n);all(n)}})})}).observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['src']});document.addEventListener('DOMContentLoaded',function(){all(document)})}catch(e){}})();
