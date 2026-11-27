// YA CLIENT store API on Cloudflare Pages Functions. Storage: KV namespace bound as DB. Secret: ADMIN_KEY.
// Same endpoints as the Node backend, so the game client and launcher need no changes.
const ITEMS = {
  ya_cape: { slot: 'cape', price: 'Beta' }, ya_wings: { slot: 'wings', price: 'Beta' },
  ya_bandana: { slot: 'bandana', price: 'Beta' }, ya_pet: { slot: 'pet', price: 'Beta' },
  mono_cape: { slot: 'cape', price: 'Exclusive' }, mono_wings: { slot: 'wings', price: 'Exclusive' },
  mono_bandana: { slot: 'bandana', price: 'Exclusive' }, mono_pet: { slot: 'pet', price: 'Exclusive' },
  qlux_cape: { slot: 'cape', price: 'Partner' }, qlux_wings: { slot: 'wings', price: 'Partner' },
  qlux_bandana: { slot: 'bandana', price: 'Partner' }, qlux_pet: { slot: 'pet', price: 'Partner' },
  sakura_cape: { slot: 'cape', price: 'Exclusive' }, sakura_wings: { slot: 'wings', price: 'Exclusive' },
  sakura_bandana: { slot: 'bandana', price: 'Exclusive' }, sakura_pet: { slot: 'pet', price: 'Exclusive' },
  shinobi_cape: { slot: 'cape', price: 'Exclusive' }, shinobi_wings: { slot: 'wings', price: 'Exclusive' },
  shinobi_bandana: { slot: 'bandana', price: 'Exclusive' }, shinobi_pet: { slot: 'pet', price: 'Exclusive' },
  kitsune_cape: { slot: 'cape', price: 'Exclusive' }, kitsune_wings: { slot: 'wings', price: 'Exclusive' },
  kitsune_bandana: { slot: 'bandana', price: 'Exclusive' }, kitsune_pet: { slot: 'pet', price: 'Exclusive' },
  santa_cape: { slot: 'cape', price: 'Exclusive' }, santa_wings: { slot: 'wings', price: 'Exclusive' },
  santa_bandana: { slot: 'bandana', price: 'Exclusive' }, santa_pet: { slot: 'pet', price: 'Exclusive' },
  frost_cape: { slot: 'cape', price: 'Exclusive' }, frost_wings: { slot: 'wings', price: 'Exclusive' },
  frost_bandana: { slot: 'bandana', price: 'Exclusive' }, frost_pet: { slot: 'pet', price: 'Exclusive' },
  clown_cape: { slot: 'cape', price: 'Exclusive' }, clown_wings: { slot: 'wings', price: 'Exclusive' },
  clown_bandana: { slot: 'bandana', price: 'Exclusive' }, clown_pet: { slot: 'pet', price: 'Exclusive' },
  luck_cape: { slot: 'cape', price: 'Exclusive' }, luck_wings: { slot: 'wings', price: 'Exclusive' },
  luck_bandana: { slot: 'bandana', price: 'Exclusive' }, luck_pet: { slot: 'pet', price: 'Exclusive' },
  sky_cape: { slot: 'cape', price: 'Exclusive' },
  ocean_cape: { slot: 'cape', price: 'Exclusive' },
  sunset_cape: { slot: 'cape', price: 'Exclusive' },
  forest_cape: { slot: 'cape', price: 'Exclusive' },
  space_cape: { slot: 'cape', price: 'Exclusive' },
  dragon_wings: { slot: 'wings', price: 'Exclusive' },
  butterfly_wings: { slot: 'wings', price: 'Exclusive' },
  azure_wings: { slot: 'wings', price: 'Exclusive' },
  venom_wings: { slot: 'wings', price: 'Exclusive' },
  dragonfly_wings: { slot: 'wings', price: 'Exclusive' },
  ninja_bandana: { slot: 'bandana', price: 'Exclusive' },
  crown_bandana: { slot: 'bandana', price: 'Exclusive' },
  halo_bandana: { slot: 'bandana', price: 'Exclusive' },
  cat_bandana: { slot: 'bandana', price: 'Exclusive' },
  rainbow_bandana: { slot: 'bandana', price: 'Exclusive' },
  drake_pet: { slot: 'pet', price: 'Exclusive' },
  bunny_pet: { slot: 'pet', price: 'Exclusive' },
  penguin_pet: { slot: 'pet', price: 'Exclusive' },
  panda_pet: { slot: 'pet', price: 'Exclusive' },
  axolotl_pet: { slot: 'pet', price: 'Exclusive' },
  vampire_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_crimson_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_scarlet_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_orange_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_amber_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_gold_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_lime_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_green_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_emerald_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_teal_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_cyan_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_sky_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_blue_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_indigo_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_violet_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_purple_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_magenta_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_pink_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_rose_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_white_wings: { slot: 'wings', price: 'Exclusive' },
  vamp_silver_wings: { slot: 'wings', price: 'Exclusive' },
  vampire_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_crimson_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_scarlet_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_orange_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_amber_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_gold_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_lime_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_green_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_emerald_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_teal_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_cyan_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_sky_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_blue_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_indigo_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_violet_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_purple_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_magenta_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_pink_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_rose_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_white_cape: { slot: 'cape', price: 'Exclusive' },
  vamp_silver_cape: { slot: 'cape', price: 'Exclusive' },
  storm_cape: { slot: 'cape', price: 'Exclusive' },
  puppy_pet: { slot: 'pet', price: 'Exclusive' }
};
// Bundles unlock several items at once. "beta" = Beta Tester bundle (free, granted by the admin to testers).
const BUNDLES = {
  beta: { price: 'Free', items: ['ya_cape', 'ya_wings', 'ya_bandana', 'ya_pet'] },
  mono: { price: 'Exclusive', items: ['mono_cape', 'mono_wings', 'mono_bandana', 'mono_pet'] },
  // partner (streamer) bundles
  qlux: { price: 'Partner', partner: 'Qlux', items: ['qlux_cape', 'qlux_wings', 'qlux_bandana', 'qlux_pet'] },
  sakura: { price: 'Exclusive', items: ['sakura_cape', 'sakura_wings', 'sakura_bandana', 'sakura_pet'] },
  shinobi: { price: 'Exclusive', items: ['shinobi_cape', 'shinobi_wings', 'shinobi_bandana', 'shinobi_pet'] },
  kitsune: { price: 'Exclusive', items: ['kitsune_cape', 'kitsune_wings', 'kitsune_bandana', 'kitsune_pet'] },
  santa: { price: 'Exclusive', items: ['santa_cape', 'santa_wings', 'santa_bandana', 'santa_pet'] },
  frost: { price: 'Exclusive', items: ['frost_cape', 'frost_wings', 'frost_bandana', 'frost_pet'] },
  clown: { price: 'Exclusive', items: ['clown_cape', 'clown_wings', 'clown_bandana', 'clown_pet'] },
  luck: { price: 'Exclusive', items: ['luck_cape', 'luck_wings', 'luck_bandana', 'luck_pet'] }
};
const SLOTS = ['cape', 'wings', 'bandana', 'pet'];
// UUID that a cracked server gives a name (md5 of "OfflinePlayer:<name>"): premium players are seen under it on such servers
async function offlineUuid(name) {
  const b = new Uint8Array(await crypto.subtle.digest('MD5', new TextEncoder().encode('OfflinePlayer:' + name)));
  b[6] = (b[6] & 0x0f) | 0x30; b[8] = (b[8] & 0x3f) | 0x80;
  return [...b].map((x) => x.toString(16).padStart(2, '0')).join('');
}
const norm = (u) => String(u || '').replace(/-/g, '').toLowerCase();

function send(code, obj) {
  const flat = {};
  for (const k of Object.keys(obj)) flat[k] = String(obj[k]);
  return new Response(JSON.stringify(flat), { status: code, headers: { 'content-type': 'application/json; charset=utf-8', 'access-control-allow-origin': '*', 'cache-control': 'no-store' } });
}
async function form(request) {
  const txt = await request.text();
  if (txt.length > 10000) return {};
  return Object.fromEntries(new URLSearchParams(txt));
}
async function getJson(url) {
  try { const r = await fetch(url, { headers: { 'user-agent': 'YA-CLIENT-Store' } }); return r.ok ? await r.json() : null; } catch (_) { return null; }
}
// name -> uuid. Mojang sometimes refuses requests that come from Cloudflare, so several services are tried in turn.
async function resolveMc(name) {
  const e = encodeURIComponent(name);
  const tries = [
    async () => { const j = await getJson('https://api.mojang.com/users/profiles/minecraft/' + e); return j && j.id ? { id: j.id, name: j.name } : null; },
    async () => { const j = await getJson('https://playerdb.co/api/player/minecraft/' + e); const pl = j && j.data && j.data.player; return pl && pl.id ? { id: pl.id, name: pl.username } : null; },
    async () => { const j = await getJson('https://api.ashcon.app/mojang/v2/user/' + e); return j && j.uuid ? { id: j.uuid, name: j.username } : null; },
    async () => { const j = await getJson('https://api.minetools.eu/uuid/' + e); return j && j.id && j.id !== 'null' ? { id: j.id, name: j.name } : null; }
  ];
  for (const t of tries) { try { const r = await t(); if (r) return r; } catch (_) {} }
  return null;
}
async function loadPlayer(env, uuid) {
  const v = (await env.DB.get('p:' + uuid, { type: 'json', cacheTtl: 30 })) || { owned: [], equipped: {} };
  v._tmp = [];
  // Plus monthly bundle: owned only while Plus is active (virtual ownership, never saved)
  if (plusActive(v) && v.plusBundle && v.plusBundle.until > Date.now() && BUNDLES[v.plusBundle.id]) {
    for (const i of BUNDLES[v.plusBundle.id].items) if (!v.owned.includes(i)) { v.owned.push(i); v._tmp.push(i); }
  }
  if (env.BETA_OPEN === '1') v.owned = Object.keys(ITEMS);   // open beta: everyone owns everything
  return v;
}
function savePlayer(env, uuid, p) {
  const o = Object.assign({}, p);
  if (p._tmp && p._tmp.length) o.owned = p.owned.filter((x) => !p._tmp.includes(x));
  delete o._tmp;
  return env.DB.put('p:' + uuid, JSON.stringify(o));
}
const pickRandom = (arr) => arr[crypto.getRandomValues(new Uint32Array(1))[0] % arr.length];
async function applyGrant(env, uuid, f) {
      if (f.item === 'staff') {   // staff logo: only the admin panel can set it
        const p0 = await loadPlayer(env, uuid);
        p0.staff = f.revoke === '1' ? 0 : 1;
        await savePlayer(env, uuid, p0);
        return { status: 200, body: { ok: 1, uuid, staff: p0.staff } };
      }
      if (f.item === 'plus') {
        const p0 = await loadPlayer(env, uuid);
        const days = Math.max(1, Math.min(3650, parseInt(f.days || '30', 10) || 30));
        if (f.revoke === '1') p0.plusUntil = 0;
        else {
          const wasActive = plusActive(p0);
          p0.plusUntil = Math.max(Date.now(), p0.plusUntil || 0) + days * 86400000;
          // perk 1: one random store cosmetic, kept forever (given once)
          if (!p0.plusGift) {
            const pool = Object.keys(ITEMS).filter((i) => ITEMS[i].price === 'Exclusive' && !p0.owned.includes(i));
            if (pool.length) { p0.plusGift = pickRandom(pool); p0.owned.push(p0.plusGift); }
          }
          // perk 2: one random bundle for as long as Plus lasts (a new one each time Plus starts again, same one when extending)
          if (!wasActive || !p0.plusBundle || !BUNDLES[p0.plusBundle.id]) {
            const pool = Object.keys(BUNDLES).filter((b) => BUNDLES[b].price === 'Exclusive' && BUNDLES[b].items.some((i) => !p0.owned.includes(i)));
            const prev = p0.plusBundle && p0.plusBundle.id;
            const pool2 = pool.filter((b) => b !== prev);
            const id = pickRandom(pool2.length ? pool2 : pool);
            if (id) p0.plusBundle = { id, until: p0.plusUntil };
          } else p0.plusBundle.until = p0.plusUntil;
        }
        await savePlayer(env, uuid, p0);
        return { status: 200, body: { ok: 1, uuid, plus_until: new Date(p0.plusUntil).toISOString(), gift: p0.plusGift || '', bundle: p0.plusBundle ? p0.plusBundle.id : '' } };
      }
      let items = f.item === '*' ? Object.keys(ITEMS) : String(f.item || '').split(',').filter(Boolean);
      items = items.flatMap((i) => (BUNDLES[i] ? BUNDLES[i].items : [i]));
      if (!items.length || items.some((i) => !ITEMS[i])) return { status: 400, body: { error: 'unknown item' } };
      const p = await loadPlayer(env, uuid);
      for (const i of items) {
        if (f.revoke === '1') p.owned = p.owned.filter((x) => x !== i);
        else if (!p.owned.includes(i)) p.owned.push(i);
      }
      await savePlayer(env, uuid, p);
      return { status: 200, body: { ok: 1, uuid, owned: p.owned.join(',') } };
}


// ---------- automatic card payments (Stripe Checkout) ----------
// Secrets (Cloudflare Pages > Settings > Variables): STRIPE_SECRET (sk_live_... or sk_test_...), STRIPE_WEBHOOK_SECRET (whsec_...).
const PRICE = { item: 3.49, mono: 10.99, qlux: 8.99, sakura: 10.99, shinobi: 10.99, kitsune: 10.99, santa: 10.99, frost: 10.99, clown: 10.99, luck: 10.99, plus: 5.49 };   // includes the Stripe fee (about 1.5% + 0.25)
const EXTRA_IDS = ['sky_cape', 'ocean_cape', 'sunset_cape', 'forest_cape', 'space_cape', 'dragon_wings', 'butterfly_wings', 'azure_wings', 'venom_wings', 'dragonfly_wings', 'ninja_bandana', 'crown_bandana', 'halo_bandana', 'cat_bandana', 'rainbow_bandana', 'drake_pet', 'bunny_pet', 'penguin_pet', 'panda_pet', 'axolotl_pet', 'vampire_wings', 'vamp_crimson_wings', 'vamp_scarlet_wings', 'vamp_orange_wings', 'vamp_amber_wings', 'vamp_gold_wings', 'vamp_lime_wings', 'vamp_green_wings', 'vamp_emerald_wings', 'vamp_teal_wings', 'vamp_cyan_wings', 'vamp_sky_wings', 'vamp_blue_wings', 'vamp_indigo_wings', 'vamp_violet_wings', 'vamp_purple_wings', 'vamp_magenta_wings', 'vamp_pink_wings', 'vamp_rose_wings', 'vamp_white_wings', 'vamp_silver_wings', 'vampire_cape', 'vamp_crimson_cape', 'vamp_scarlet_cape', 'vamp_orange_cape', 'vamp_amber_cape', 'vamp_gold_cape', 'vamp_lime_cape', 'vamp_green_cape', 'vamp_emerald_cape', 'vamp_teal_cape', 'vamp_cyan_cape', 'vamp_sky_cape', 'vamp_blue_cape', 'vamp_indigo_cape', 'vamp_violet_cape', 'vamp_purple_cape', 'vamp_magenta_cape', 'vamp_pink_cape', 'vamp_rose_cape', 'vamp_white_cape', 'vamp_silver_cape', 'storm_cape', 'puppy_pet'];   // single items (3 each, not in a bundle)
const DISABLED = ['qlux'];   // sets that cannot be bought for now
const STORE_HIDDEN = ['qlux'];   // bundles switched off in the store (keep in sync with site/config.js 'hidden')
const PAID_SETS = ['mono', 'qlux', 'sakura', 'shinobi', 'kitsune', 'santa', 'frost', 'clown', 'luck'];
async function resolveName(env, nm) {
  nm = String(nm || '').trim();
  if (!/^[A-Za-z0-9_]{3,16}$/.test(nm)) return '';
  let uuid = (await env.DB.get('n:' + nm.toLowerCase())) || '';
  if (!uuid) { const prof = await getJson('https://api.mojang.com/users/profiles/minecraft/' + encodeURIComponent(nm)); if (prof && prof.id) uuid = norm(prof.id); }
  if (!uuid) { const pdb = await getJson('https://playerdb.co/api/player/minecraft/' + encodeURIComponent(nm)); const id = pdb && pdb.data && pdb.data.player && pdb.data.player.raw_id; if (id) uuid = norm(id); }
  return uuid;
}
// price in cents, computed here so the browser can never choose it
function orderCents(items, plusOn) {
  let t = 0;
  const left = items.filter((i) => !i.startsWith('ya_'));
  for (const st of PAID_SETS) {
    const m = left.filter((i) => i.startsWith(st + '_')).length;
    t += m === 4 ? PRICE[st] : m * PRICE.item;
  }
  for (const i of left) if (EXTRA_IDS.includes(i)) t += PRICE.item;
  return Math.round(t * (plusOn ? 0.5 : 1) * 100);
}
async function stripeCheckout(env, request, url, f) {
  if (!env.STRIPE_SECRET) return send(501, { error: 'not_configured' });
  const uuid = await resolveName(env, f.username);
  if (!uuid) return send(400, { error: 'Minecraft username not found' });
  const isPlus = f.ids === 'plus';
  let items = [], cents, name;
  if (isPlus) { cents = Math.round(PRICE.plus * 100); name = 'YA Plus (30 days)'; }
  else {
    items = String(f.ids || '').split(',').filter(Boolean);
    if (!items.length || items.length > 40 || items.some((i) => !ITEMS[i])) return send(400, { error: 'unknown item' });
    if (items.some((i) => DISABLED.some((d) => i.startsWith(d + '_')))) return send(400, { error: 'This bundle is not available right now.' });
    if (items.some((i) => STORE_HIDDEN.some((h) => i.startsWith(h + '_')))) return send(400, { error: 'These items are not on sale right now.' });
    const p = await loadPlayer(env, uuid);
    items = items.filter((i) => !p.owned.includes(i));
    if (!items.length) return send(400, { error: 'You already own these items.' });
    cents = orderCents(items, plusActive(p));
    name = items.length === 1 ? items[0] : items.length + ' YA CLIENT cosmetics';
  }
  if (cents < 50) return send(400, { error: 'nothing to pay' });
  const body = new URLSearchParams({
    mode: 'payment', 'line_items[0][quantity]': '1', 'line_items[0][price_data][currency]': 'eur',
    'line_items[0][price_data][unit_amount]': String(cents), 'line_items[0][price_data][product_data][name]': name + ' - ' + f.username,
    success_url: url.origin + '/store?paid=1', cancel_url: url.origin + '/store',
    'metadata[uuid]': uuid, 'metadata[items]': isPlus ? 'plus' : items.join(','), 'metadata[username]': String(f.username).slice(0, 16)
  });
  const r = await fetch('https://api.stripe.com/v1/checkout/sessions', { method: 'POST', headers: { authorization: 'Bearer ' + env.STRIPE_SECRET, 'content-type': 'application/x-www-form-urlencoded' }, body });
  const j = await r.json().catch(() => ({}));
  if (!r.ok || !j.url) return send(502, { error: (j.error && j.error.message) || 'payment provider error' });
  return send(200, { ok: 1, url: j.url });
}
async function stripeWebhook(env, request) {
  const raw = await request.text();
  const sig = request.headers.get('stripe-signature') || '';
  const parts = Object.fromEntries(sig.split(',').map((x) => x.split('=')).filter((x) => x.length === 2));
  if (!env.STRIPE_WEBHOOK_SECRET || !parts.t || !parts.v1) return send(400, { error: 'bad signature' });
  const key = await crypto.subtle.importKey('raw', new TextEncoder().encode(env.STRIPE_WEBHOOK_SECRET), { name: 'HMAC', hash: 'SHA-256' }, false, ['sign']);
  const mac = new Uint8Array(await crypto.subtle.sign('HMAC', key, new TextEncoder().encode(parts.t + '.' + raw)));
  const good = [...mac].map((b) => b.toString(16).padStart(2, '0')).join('');
  if (good !== parts.v1 || Math.abs(Date.now() / 1000 - Number(parts.t)) > 600) return send(400, { error: 'bad signature' });
  const ev = JSON.parse(raw);
  if (ev.type !== 'checkout.session.completed') return send(200, { ok: 1 });
  const s = ev.data.object;
  if (s.payment_status !== 'paid') return send(200, { ok: 1 });
  if (await env.DB.get('o:' + s.id)) return send(200, { ok: 1, dup: 1 });   // already delivered
  const m = s.metadata || {};
  const res = await applyGrant(env, m.uuid, m.items === 'plus' ? { item: 'plus', days: '30' } : { item: m.items });
  if (res.status !== 200) return send(500, { error: 'grant failed' });   // Stripe retries
  await env.DB.put('o:' + s.id, JSON.stringify({ uuid: m.uuid, items: m.items, user: m.username, amount: s.amount_total, at: Date.now() }));
  return send(200, { ok: 1 });
}

function plusActive(p) { return !!p.plusUntil && p.plusUntil > Date.now(); }
function equippedMap(p) {
  const out = { plus: plusActive(p) ? '1' : '0', staff: p.staff ? '1' : '0' };   // YA Plus subscription / staff badge (staff is only set from the admin panel)
  for (const s of SLOTS) { const i = p.equipped[s]; out[s] = i && p.owned.includes(i) ? i : 'none'; }
  return out;
}
const MEMO = (globalThis.__memo = globalThis.__memo || new Map());
function memoGet(k) { const e = MEMO.get(k); return e && e.t > Date.now() ? e.v : undefined; }
function memoSet(k, v, ms) { if (MEMO.size > 2000) MEMO.clear(); MEMO.set(k, { v, t: Date.now() + ms }); return v; }
async function who(env, token) {
  if (!token || !/^[0-9a-f]{48}$/.test(token)) return null;
  const m = memoGet('s:' + token);
  if (m !== undefined) return m;
  const v = await env.DB.get('s:' + token);
  return v ? memoSet('s:' + token, v, 120000) : v;
}
function hex(n) { return [...crypto.getRandomValues(new Uint8Array(n))].map((b) => b.toString(16).padStart(2, '0')).join(''); }


// ---------- friends: f:<uuid> = { name, friends:[{u,n}], incoming:[..], outgoing:[..] }, n:<lowername> = uuid, o:<uuid> = "where|time" ----------
async function getF(env, uuid) {
  const v = (await env.DB.get('f:' + uuid, 'json')) || {};
  return { name: v.name || '', friends: v.friends || [], incoming: v.incoming || [], outgoing: v.outgoing || [] };
}
const putF = (env, uuid, f) => env.DB.put('f:' + uuid, JSON.stringify(f));
const without = (list, u) => list.filter((x) => x.u !== u);
const ONLINE_MS = 200000;
async function presenceOf(env, uuid) {
  const v = await env.DB.get('o:' + uuid);
  if (!v) return null;
  const [where, ts] = v.split('|');
  return Date.now() - parseInt(ts, 10) < ONLINE_MS ? where : null;
}
async function packFriends(env, list, sort) {
  const out = await Promise.all(list.map(async (x) => ({ u: x.u, n: x.n, w: await presenceOf(env, x.u) })));
  if (sort) out.sort((a, b) => (b.w ? 1 : 0) - (a.w ? 1 : 0) || a.n.toLowerCase().localeCompare(b.n.toLowerCase()));
  return out.map((e) => [e.u, e.n, e.w ? 1 : 0, e.w || ''].join(',')).join(';');
}
async function friendsRoute(env, request, url, route) {
  if (route === 'GET /v1/friends') {
    const me = await who(env, url.searchParams.get('token'));
    if (!me) return send(401, { error: 'bad token' });
    const w = url.searchParams.get('where') === 'launcher' ? 'launcher' : 'game';
    const prev = (await env.DB.get('o:' + me)) || '';
    const [pw, pts] = prev.split('|');
    const age = Date.now() - (parseInt(pts, 10) || 0);
    if (age > 120000 || (pw === 'launcher' && w === 'game')) { try { await env.DB.put('o:' + me, w + '|' + Date.now(), { expirationTtl: 260 }); } catch (_) { } }   // heartbeat, written about every 2 minutes
    const f = await getF(env, me);
    return send(200, { friends: await packFriends(env, f.friends, true), incoming: await packFriends(env, f.incoming, false), outgoing: await packFriends(env, f.outgoing, false) });
  }
  if (!route.startsWith('POST /v1/friends/')) return null;
  const b = await form(request);
  const me = await who(env, b.token);
  if (!me) return send(401, { error: 'bad token' });
  const act = url.pathname.replace(/\/+$/, '').slice('/v1/friends/'.length);
  const mine = await getF(env, me);
  const meE = { u: me, n: mine.name || 'Player' };
  if (act === 'add') {
    const low = String(b.name || '').trim().toLowerCase();
    if (!/^[a-z0-9_]{3,16}$/.test(low)) return send(404, { error: 'not found' });
    let other = await env.DB.get('n:' + low), canon = '';
    if (!other) {   // never opened YA CLIENT yet: look the player up, the request waits in their list until they do
      const pdb = await getJson('https://playerdb.co/api/player/minecraft/' + encodeURIComponent(low));
      const pl = pdb && pdb.data && pdb.data.player;
      if (pl && pl.raw_id) { other = norm(pl.raw_id); canon = pl.username || ''; }
    }
    if (!other) return send(404, { error: 'not found' });
    if (other === me) return send(400, { error: 'self' });
    const theirs = await getF(env, other);
    const otherE = { u: other, n: theirs.name || canon || low };
    if (mine.friends.some((x) => x.u === other)) return send(409, { error: 'already' });
    if (mine.incoming.some((x) => x.u === other)) {   // they already asked you: becomes a friendship
      mine.incoming = without(mine.incoming, other); mine.friends.push(otherE);
      theirs.outgoing = without(theirs.outgoing, me); theirs.friends.push(meE);
      await putF(env, me, mine); await putF(env, other, theirs);
      return send(200, { ok: 1, status: 'accepted' });
    }
    if (mine.outgoing.some((x) => x.u === other)) return send(409, { error: 'pending' });
    if (mine.friends.length >= 200) return send(400, { error: 'full' });
    mine.outgoing.push(otherE); theirs.incoming.push(meE);
    await putF(env, me, mine); await putF(env, other, theirs);
    return send(200, { ok: 1, status: 'sent' });
  }
  const other = norm(b.uuid);
  if (!/^[0-9a-f]{32}$/.test(other)) return send(400, { error: 'bad uuid' });
  const theirs = await getF(env, other);
  if (act === 'accept') {
    const req = mine.incoming.find((x) => x.u === other);
    if (!req) return send(400, { error: 'no request' });
    mine.incoming = without(mine.incoming, other); mine.friends.push(req);
    theirs.outgoing = without(theirs.outgoing, me); theirs.friends.push(meE);
  } else if (act === 'decline' || act === 'remove') {
    for (const [a, id] of [[mine, other], [theirs, me]]) { a.friends = without(a.friends, id); a.incoming = without(a.incoming, id); a.outgoing = without(a.outgoing, id); }
  } else return send(404, { error: 'not found' });
  await putF(env, me, mine); await putF(env, other, theirs);
  return send(200, { ok: 1 });
}

export async function onRequest({ request, env }) {
  if (request.method === 'OPTIONS') return new Response(null, { status: 204, headers: { 'access-control-allow-origin': '*', 'access-control-allow-headers': 'x-admin-key,content-type', 'access-control-allow-methods': 'GET,POST' } });
  if (!env.DB) return send(500, { error: 'KV binding DB missing' });
  const url = new URL(request.url);
  const route = request.method + ' ' + url.pathname.replace(/\/+$/, '');
  try {
    if (route === 'POST /v1/checkout') return stripeCheckout(env, request, url, await form(request));
    if (route === 'POST /v1/stripe/webhook') return stripeWebhook(env, request);
    if (route === 'GET /v1/catalog') {
      const out = { _store: url.origin + '/store' };
      for (const [id, it] of Object.entries(ITEMS)) out[id] = it.price;
      for (const [id, b] of Object.entries(BUNDLES)) out[id] = b.price;
      return send(200, out);
    }
    if (route === 'POST /v1/auth') {
      const f = await form(request);
      if (!f.username) return send(400, { error: 'missing fields' });
      const ip = request.headers.get('cf-connecting-ip') || 'x';
      const rl = (globalThis.__rl = globalThis.__rl || new Map()), bucket = ip + ':' + Math.floor(Date.now() / 60000);
      const n = (rl.get(bucket) || 0) + 1; rl.set(bucket, n); if (rl.size > 500) rl.clear();
      if (n > 30) return send(429, { error: 'rate limited' });
      // Mojang blocks Cloudflare's servers (403), so the join can't be verified here. Instead the first device that
      // claims an account locks it with a secret (kept in ~/.yaclient/config.properties); other devices are refused.
      const uuid0 = norm(f.uuid), secret = String(f.secret || '');
      if (!/^[0-9a-f]{32}$/.test(uuid0) || !/^[0-9a-f]{16,64}$/.test(secret) || !/^[A-Za-z0-9_]{1,16}$/.test(String(f.username))) return send(400, { error: 'bad request' });
      const sh = [...new Uint8Array(await crypto.subtle.digest('SHA-256', new TextEncoder().encode('ya:' + secret)))].map((x) => x.toString(16).padStart(2, '0')).join('');
      const kk = await env.DB.get('k:' + uuid0);
      if (!kk) await env.DB.put('k:' + uuid0, sh);
      else if (kk !== sh) return send(403, { error: 'account linked to another device - ask an admin to reset it' });
      const check = { id: uuid0, name: String(f.username) };
      const token = hex(24);
      await env.DB.put('s:' + token, norm(check.id), { expirationTtl: 3600 });   // required: the login itself
      if (check.name && f.offline !== '1') {   // friends (never for non-premium players, so they cannot take a premium name): remember who this account is (written only when it changes)
        const id = norm(check.id), low = String(check.name).toLowerCase();
        try {
          const fr = await getF(env, id);
          if (fr.name !== check.name) { fr.name = check.name; await env.DB.put('f:' + id, JSON.stringify(fr)); }
          if ((await env.DB.get('n:' + low)) !== id) await env.DB.put('n:' + low, id);
          const pend = await env.DB.get('g:' + low, 'json');   // grants queued by name from /admin
          if (pend && pend.length) { for (const g of pend) await applyGrant(env, id, g); await env.DB.delete('g:' + low); }
        } catch (_) { }
      }
      if (f.offline !== '1') {   // premium player on a cracked server: others see him under the offline UUID of his name -> point it to his real account
        try {
          const ou = await offlineUuid(String(check.name));
          if (ou !== uuid0 && !(await env.DB.get('k:' + ou)) && (await env.DB.get('al:' + ou)) !== uuid0) await env.DB.put('al:' + ou, uuid0);
        } catch (_) { }
      }
      if (f.offline === '1') {   // non-premium (SP) players: separate list for the admin only (never mixed with premium names, no cosmetics by name)
        try {
          const id = norm(check.id), low = String(check.name).toLowerCase();
          if ((await env.DB.get('sn:' + id)) !== check.name) await env.DB.put('sn:' + id, check.name);
          if ((await env.DB.get('sp:' + low)) !== id) await env.DB.put('sp:' + low, id);
        } catch (_) { }
      }
      return send(200, { token, uuid: norm(check.id) });
    }
    if (route === 'GET /v1/me') {
      const uuid = await who(env, url.searchParams.get('token'));
      if (!uuid) return send(401, { error: 'bad token' });
      const p = await loadPlayer(env, uuid);
      try {   // heartbeat: this player is running YA CLIENT (rewritten at most every 10 minutes)
        const c0 = parseInt((await env.DB.get('c:' + uuid)) || '0', 10) || 0;
        if (Date.now() - c0 > 600000) await env.DB.put('c:' + uuid, String(Date.now()), { expirationTtl: 1500 });
      } catch (_) { }
      return send(200, Object.assign({ owned: p.owned.join(',') }, equippedMap(p), { client: '1' }));
    }
    if (route === 'POST /v1/equip') {
      const f = await form(request);
      const uuid = await who(env, f.token);
      if (!uuid) return send(401, { error: 'bad token' });
      // one slot (slot + item) or several at once (set = "cape:none,wings:mono_wings,...") so a bundle is one atomic write
      const pairs = f.set ? String(f.set).split(',').map((x) => x.split(':')) : [[f.slot, f.item]];
      const p = await loadPlayer(env, uuid);
      for (const [slot, item] of pairs) {
        if (!SLOTS.includes(slot)) return send(400, { error: 'bad slot' });
        if (item === 'none') p.equipped[slot] = 'none';
        else {
          const it = ITEMS[item];
          if (!it || it.slot !== slot) return send(400, { error: 'bad item' });
          if (!p.owned.includes(item)) return send(403, { error: 'not owned' });
          p.equipped[slot] = item;
        }
      }
      await savePlayer(env, uuid, p);
      return send(200, { ok: 1 });
    }
    const m = url.pathname.match(/^\/v1\/players\/([0-9a-fA-F-]{32,36})$/);
    if (request.method === 'GET' && m) {
      const pid0 = norm(m[1]);
      const hit = memoGet('pl:' + pid0);   // many clients ask about the same players every few seconds: answer from memory for 30 s
      if (hit) return send(200, hit);
      let pid = pid0;
      try { pid = (await env.DB.get('al:' + pid)) || pid; } catch (_) { }
      const out = equippedMap(await loadPlayer(env, pid));
      out.client = (await env.DB.get('c:' + pid)) ? '1' : '0';   // shown as a logo in the tab list
      memoSet('pl:' + pid0, out, 30000);
      return send(200, out);
    }

    if (route === 'GET /v1/plus') {   // public: does this player have YA Plus? (the site uses it for the Plus shop discount)
      const nm = String(url.searchParams.get('name') || '').trim().toLowerCase();
      if (!/^[a-z0-9_]{3,16}$/.test(nm)) return send(400, { error: 'bad name' });
      const id = await env.DB.get('n:' + nm);
      if (!id) return send(200, { plus: '0', known: '0' });
      return send(200, { plus: plusActive(await loadPlayer(env, id)) ? '1' : '0', known: '1' });
    }
    if (route === 'GET /v1/selftest') {   // can this server reach Mojang? (a join that was never made should answer 204)
      const out = {};
      const UA = { none: {}, ya: { 'user-agent': 'YA-CLIENT-Store' }, java: { 'user-agent': 'Java/1.8.0_321' }, browser: { 'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36', accept: 'application/json' } };
      const T = [['hasJoined', 'https://sessionserver.mojang.com/session/minecraft/hasJoined?username=Notch&serverId=ya-selftest'],
        ['sessionProfile', 'https://sessionserver.mojang.com/session/minecraft/profile/069a79f444e94726a5befca90e38aaf5'],
        ['apiProfile', 'https://api.mojang.com/users/profiles/minecraft/Notch'],
        ['msProfile', 'https://api.minecraftservices.com/minecraft/profile']];
      for (const [k, u] of T) for (const [un, h] of Object.entries(UA)) {
        try { const r = await fetch(u, { headers: h }); out[k + '_' + un] = String(r.status); } catch (e) { out[k + '_' + un] = 'unreachable'; }
      }
      try { await env.DB.get('selftest'); out.kv_read = 'ok'; } catch (e) { out.kv_read = 'fail'; }
      try { await env.DB.put('selftest', String(Date.now()), { expirationTtl: 60 }); out.kv_write = 'ok'; } catch (e) { out.kv_write = 'FAIL'; }
      return send(200, out);
    }
    if (route === 'GET /v1/admin/status') {
      if (!env.ADMIN_KEY || url.searchParams.get('key') !== env.ADMIN_KEY) return send(403, { error: 'forbidden' });
      if (url.searchParams.get('probe')) {   // can this server reach Mojang at all?
        const out = {};
        for (const [k, u] of [['sessionserver_hasJoined', 'https://sessionserver.mojang.com/session/minecraft/hasJoined?username=Notch&serverId=probe'], ['api_profile', 'https://api.mojang.com/users/profiles/minecraft/Notch'], ['playerdb', 'https://playerdb.co/api/player/minecraft/Notch']]) {
          try { const r = await fetch(u, { headers: { 'user-agent': 'YA-CLIENT-Store' } }); out[k] = 'HTTP ' + r.status + ' ' + (await r.text()).slice(0, 60).replace(/[\r\n{}"]/g, ' '); } catch (e) { out[k] = 'unreachable: ' + e.message; }
        }
        out.expected = 'sessionserver_hasJoined should be HTTP 204; api_profile and playerdb HTTP 200';
        return send(200, out);
      }
      const nm = String(url.searchParams.get('name') || '').trim().toLowerCase();
      let uuid = norm(url.searchParams.get('uuid')) || (nm ? (await env.DB.get('n:' + nm)) || '' : '');
      const out = { name: nm, registered: uuid ? 'yes' : 'no (this player never logged in from YA CLIENT/launcher)', pending_grants: nm ? String((await env.DB.get('g:' + nm)) || 'none') : '' };
      if (uuid) {
        const p = await loadPlayer(env, uuid);
        out.uuid = uuid; out.owned = p.owned.join(',') || 'none'; out.plus = plusActive(p) ? 'yes until ' + new Date(p.plusUntil).toISOString() : 'no';
        out.equipped = JSON.stringify(p.equipped || {}); out.client_flag = (await env.DB.get('c:' + uuid)) ? 'on' : 'off (no recent in-game login)';
      }
      return send(200, out);
    }
    if (route.startsWith('GET /v1/admin/') && ['overview', 'players', 'online', 'player'].includes(url.pathname.split('/').pop())) {   // dashboard data (read only)
      if (!env.ADMIN_KEY || request.headers.get('x-admin-key') !== env.ADMIN_KEY) return send(403, { error: 'forbidden' });
      const J = (o) => new Response(JSON.stringify(o), { headers: { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store' } });
      const what = url.pathname.split('/').pop();
      const count = async (prefix) => { let n = 0, cur, pages = 0; do { const r = await env.DB.list({ prefix, cursor: cur, limit: 1000 }); n += r.keys.length; cur = r.list_complete ? null : r.cursor; pages++; } while (cur && pages < 5); return { n, more: !!cur }; };
      if (what === 'overview') {
        const [reg, linked, online, sp] = await Promise.all([count('n:'), count('k:'), count('o:'), count('sp:')]);
        let kv = 'ok';
        try { await env.DB.get('selftest'); } catch (e) { kv = 'FAIL'; }   // read only: a write per refresh would eat the daily write quota
        return J({ registered: reg, linked, online, sp, kv, beta_open: env.BETA_OPEN === '1', items: Object.keys(ITEMS).length, bundles: Object.keys(BUNDLES), time: new Date().toISOString() });
      }
      if (what === 'players') {
        const q = String(url.searchParams.get('q') || '').trim().toLowerCase().replace(/[^a-z0-9_]/g, '');
        const pf = url.searchParams.get('sp') === '1' ? 'sp:' : 'n:';
        const r = await env.DB.list({ prefix: pf + q, cursor: url.searchParams.get('cursor') || undefined, limit: 50 });
        return J({ players: r.keys.map((k) => k.name.slice(pf.length)), cursor: r.list_complete ? '' : r.cursor });
      }
      if (what === 'online') {
        const r = await env.DB.list({ prefix: 'o:', limit: 40 });
        const out = [];
        for (const k of r.keys) {
          const id = k.name.slice(2), pr = await presenceOf(env, id);
          if (!pr) continue;
          const f = await getF(env, id);
          const spn = f.name ? '' : await env.DB.get('sn:' + id);
          out.push({ uuid: id, name: f.name || (spn ? spn + ' (SP)' : id.slice(0, 8)), where: pr });
        }
        return J({ online: out });
      }
      const nm = String(url.searchParams.get('name') || '').trim().toLowerCase();
      const uuid = norm(url.searchParams.get('uuid')) || (nm ? (await env.DB.get('n:' + nm)) || (await env.DB.get('sp:' + nm)) || '' : '');
      if (!uuid) return J({ found: false });
      const p = await loadPlayer(env, uuid), f = await getF(env, uuid);
      const pr = await presenceOf(env, uuid);
      return J({ found: true, uuid, name: f.name || nm, owned: p.owned, equipped: p.equipped || {}, plus: plusActive(p), staff: !!p.staff, plus_until: p.plusUntil ? new Date(p.plusUntil).toISOString() : '',
        linked: !!(await env.DB.get('k:' + uuid)), online: !!pr, where: pr || '', friends: f.friends.length, pending_grants: (await env.DB.get('g:' + (f.name || nm).toLowerCase())) ? 'yes' : 'no' });
    }
    if (route === 'POST /v1/admin/unclaim') {   // forget which device owns an account (lost PC, reinstall)
      if (!env.ADMIN_KEY || request.headers.get('x-admin-key') !== env.ADMIN_KEY) return send(403, { error: 'forbidden' });
      const f = await form(request);
      const id = norm(f.uuid) || (f.username ? (await env.DB.get('n:' + String(f.username).trim().toLowerCase())) || '' : '');
      if (!id) return send(404, { error: 'unknown username' });
      await env.DB.delete('k:' + id);
      return send(200, { ok: 1, uuid: id });
    }
    if (route === 'POST /v1/claim') {
      // free items anybody can claim by typing their Minecraft name (only the first YA Cape for now)
      const FREE = ['ya_cape'];
      const f = await form(request);
      const name = String(f.username || '').trim();
      const item = f.item || 'ya_cape';
      if (!/^[A-Za-z0-9_]{3,16}$/.test(name)) return send(400, { error: 'invalid name' });
      if (!FREE.includes(item)) return send(400, { error: 'not free' });
      const ip = request.headers.get('cf-connecting-ip') || 'x';
      const rk = 'r:claim:' + ip;
      const n = parseInt((await env.DB.get(rk)) || '0', 10) + 1;
      if (n > 15) return send(429, { error: 'too many tries, wait a few minutes' });
      await env.DB.put(rk, String(n), { expirationTtl: 600 });
      let uuid = await resolveName(env, name);
      let shown = name;
      if (!uuid) { const alt = await resolveMc(name); if (alt) { uuid = norm(alt.id); shown = alt.name || name; } }
      if (!uuid) return send(404, { error: 'unknown username' });
      uuid = norm(uuid);
      const prof = { name: shown };
      const p = await loadPlayer(env, uuid);
      if (!p.owned.includes(item)) p.owned.push(item);
      if (!p.equipped.cape) p.equipped.cape = item;
      await savePlayer(env, uuid, p);
      return send(200, { ok: 1, name: prof.name || name, uuid, item });
    }
    if (route === 'POST /v1/admin/grant') {
      if (!env.ADMIN_KEY || request.headers.get('x-admin-key') !== env.ADMIN_KEY) return send(403, { error: 'forbidden' });
      const f = await form(request);
      let uuid = norm(f.uuid);
      if (!uuid && /^[0-9a-fA-F-]{32,36}$/.test(String(f.username || '').trim())) uuid = norm(String(f.username).trim());   // a UUID pasted in the name field
      if (!uuid && f.username) {
        const nm = String(f.username).trim();
        uuid = (await env.DB.get('n:' + nm.toLowerCase())) || '';   // known from the launcher/client
        if (!uuid) {
          const prof = await getJson('https://api.mojang.com/users/profiles/minecraft/' + encodeURIComponent(nm));
          if (prof && prof.id) uuid = norm(prof.id);
        }
        if (!uuid) {
          const pdb = await getJson('https://playerdb.co/api/player/minecraft/' + encodeURIComponent(nm));
          const id = pdb && pdb.data && pdb.data.player && pdb.data.player.raw_id;
          if (id) uuid = norm(id);
        }
        if (!uuid) {   // name not resolvable right now: keep the grant and apply it when this player next opens YA CLIENT
          const key = 'g:' + nm.toLowerCase();
          const q = (await env.DB.get(key, 'json')) || [];
          q.push({ item: f.item, days: f.days, revoke: f.revoke });
          await env.DB.put(key, JSON.stringify(q.slice(-20)));
          return send(200, { ok: 1, pending: 1, note: 'saved, applies when ' + nm + ' next opens YA CLIENT' });
        }
      }
      if (!uuid) return send(400, { error: 'uuid or username required' });
      const res = await applyGrant(env, uuid, f);
      return send(res.status, res.body);
    }
    if (url.pathname.startsWith('/v1/friends')) { const r = await friendsRoute(env, request, url, route); if (r) return r; }
    return send(404, { error: 'not found' });
  } catch (e) {
    return send(500, { error: 'server error' });
  }
}
