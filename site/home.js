(function () {
  'use strict';
  var d = document, de = d.documentElement;
  var reduce = window.matchMedia && matchMedia('(prefers-reduced-motion: reduce)').matches;

  // reveal on scroll
  var rv = [].slice.call(d.querySelectorAll('.rv'));
  if ('IntersectionObserver' in window && !reduce) {
    var io = new IntersectionObserver(function (es) {
      es.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
    }, { rootMargin: '0px 0px -8% 0px', threshold: 0.08 });
    rv.forEach(function (n) { io.observe(n); });
  } else rv.forEach(function (n) { n.classList.add('in'); });

  // nav: solid after scroll + scroll spy on the anchor links
  var nav = d.querySelector('nav.top'), links = [].slice.call(d.querySelectorAll('.nl a[href^="#"]:not([data-dl]):not([data-dc])'));
  var secs = links.map(function (a) { return d.querySelector(a.getAttribute('href')); });
  var ticking = false;
  function onScroll() {
    ticking = false;
    var y = window.scrollY || de.scrollTop;
    if (nav) nav.classList.toggle('sc', y > 24);
    var cur = -1;
    secs.forEach(function (s, i) { if (s && s.getBoundingClientRect().top < window.innerHeight * 0.4) cur = i; });
    links.forEach(function (a, i) { a.classList.toggle('spy', i === cur); });
  }
  window.addEventListener('scroll', function () { if (!ticking) { ticking = true; requestAnimationFrame(onScroll); } }, { passive: true });
  onScroll();

  // showcase tabs with auto-advance
  var sc = d.querySelector('.sc');
  if (sc) {
    var tabs = [].slice.call(sc.querySelectorAll('.sc-t button')), imgs = [].slice.call(sc.querySelectorAll('.sc-v img')), cap = sc.querySelector('.sc-c');
    var cur = 0, timer = 0, T = 6000;
    var show = function (i) {
      cur = (i + tabs.length) % tabs.length;
      tabs.forEach(function (b, k) { b.classList.toggle('on', k === cur); b.setAttribute('aria-selected', k === cur); });
      imgs.forEach(function (m, k) { m.classList.toggle('on', k === cur); });
      if (cap) cap.textContent = tabs[cur].getAttribute('data-c') || '';
      // restart the progress bar animation
      var bar = tabs[cur].querySelector('i'); if (bar) { bar.style.animation = 'none'; void bar.offsetWidth; bar.style.animation = ''; }
      clearTimeout(timer);
      if (!reduce && !sc.classList.contains('paused')) timer = setTimeout(function () { show(cur + 1); }, T);
    };
    tabs.forEach(function (b, k) { b.addEventListener('click', function () { sc.classList.add('stop'); show(k); }); });
    sc.addEventListener('mouseenter', function () { sc.classList.add('paused'); clearTimeout(timer); });
    sc.addEventListener('mouseleave', function () { sc.classList.remove('paused'); if (!sc.classList.contains('stop')) show(cur); });
    sc.style.setProperty('--t', (T / 1000) + 's');
    // only run while on screen
    if ('IntersectionObserver' in window) new IntersectionObserver(function (es) {
      if (es[0].isIntersecting) { if (!timer && !sc.classList.contains('stop')) show(cur); } else { clearTimeout(timer); timer = 0; }
    }).observe(sc);
    show(0);
  }

  // cosmetics strip: drag with the mouse
  var cs = d.querySelector('.cs');
  if (cs) {
    var down = false, sx = 0, sl = 0, moved = false;
    cs.addEventListener('pointerdown', function (e) { if (e.pointerType !== 'mouse') return; down = true; moved = false; sx = e.clientX; sl = cs.scrollLeft; });
    window.addEventListener('pointermove', function (e) {
      if (!down) return;
      var dx = e.clientX - sx;
      if (Math.abs(dx) > 5) { moved = true; cs.classList.add('drag'); }
      cs.scrollLeft = sl - dx;
    });
    window.addEventListener('pointerup', function () { down = false; setTimeout(function () { cs.classList.remove('drag'); }, 0); });
    cs.addEventListener('click', function (e) { if (moved) { e.preventDefault(); moved = false; } }, true);
  }
})();
