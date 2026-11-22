// YA CLIENT store API on Cloudflare Pages Functions. Storage: KV namespace bound as DB. Secret: ADMIN_KEY.
// Same endpoints as the Node backend, so the game client and launcher need no changes.
const ITEMS = {
  ya_cape: { slot: 'cape', price: 'Beta' }, ya_wings: { slot: 'wings', price: 'Beta' },
  ya_bandana: { slot: 'bandana', price: 'Beta' }, ya_pet: { slot: 'pet', price: 'Beta' },
  mono_cape: { slot: 'cape', price: 'Exclusive' }, mono_wings: { slot: 'wings', price: 'Exclusive' },
  mono_bandana: { slot: 'bandana', price: 'Exclusive' }, mono_pet: { slot: 'pet', price: 'Exclusive' }
};
// Bundles unlock several items at once. "beta" = Beta Tester bundle (free, granted by the admin to testers).
const BUNDLES = {
  beta: { price: 'Free', items: ['ya_cape', 'ya_wings', 'ya_bandana', 'ya_pet'] },
  mono: { price: 'Exclusive', items: ['mono_cape', 'mono_wings', 'mono_bandana', 'mono_pet'] }
};
const SLOTS = ['cape', 'wings', 'bandana', 'pet'];
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
async function loadPlayer(env, uuid) {
  const v = (await env.DB.get('p:' + uuid, 'json')) || { owned: [], equipped: {} };
  if (env.BETA_OPEN === '1') v.owned = Object.keys(ITEMS);   // open beta: everyone owns everything
  return v;
}
const savePlayer = (env, uuid, p) => env.DB.put('p:' + uuid, JSON.stringify(p));
function plusActive(p) { return !!p.plusUntil && p.plusUntil > Date.now(); }
function equippedMap(p) {
  const out = { plus: plusActive(p) ? '1' : '0' };   // YA Plus subscription: animated cape movement
  for (const s of SLOTS) { const i = p.equipped[s]; out[s] = i && p.owned.includes(i) ? i : 'none'; }
  return out;
}
async function who(env, token) {
  if (!token || !/^[0-9a-f]{48}$/.test(token)) return null;
  return env.DB.get('s:' + token);
}
function hex(n) { return [...crypto.getRandomValues(new Uint8Array(n))].map((b) => b.toString(16).padStart(2, '0')).join(''); }


// ---------- friends: f:<uuid> = { name, friends:[{u,n}], incoming:[..], outgoing:[..] }, n:<lowername> = uuid, o:<uuid> = "where|time" ----------
async function getF(env, uuid) {
  const v = (await env.DB.get('f:' + uuid, 'json')) || {};
  return { name: v.name || '', friends: v.friends || [], incoming: v.incoming || [], outgoing: v.outgoing || [] };
}
const putF = (env, uuid, f) => env.DB.put('f:' + uuid, JSON.stringify(f));
const without = (list, u) => list.filter((x) => x.u !== u);
const ONLINE_MS = 100000;
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
    if (age > 45000 || (pw === 'launcher' && w === 'game')) await env.DB.put('o:' + me, w + '|' + Date.now(), { expirationTtl: 120 });   // heartbeat, written about once a minute
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
    const other = await env.DB.get('n:' + low);
    if (!other) return send(404, { error: 'not found' });
    if (other === me) return send(400, { error: 'self' });
    const theirs = await getF(env, other);
    const otherE = { u: other, n: theirs.name || low };
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
    if (route === 'GET /v1/catalog') {
      const out = { _store: url.origin + '/store' };
      for (const [id, it] of Object.entries(ITEMS)) out[id] = it.price;
      for (const [id, b] of Object.entries(BUNDLES)) out[id] = b.price;
      return send(200, out);
    }
    if (route === 'POST /v1/auth') {
      const f = await form(request);
      if (!f.username || !f.serverId) return send(400, { error: 'missing fields' });
      const ip = request.headers.get('cf-connecting-ip') || 'x';
      const rk = 'r:' + ip + ':' + Math.floor(Date.now() / 60000);
      const n = parseInt((await env.DB.get(rk)) || '0', 10) + 1;
      if (n > 20) return send(429, { error: 'rate limited' });
      await env.DB.put(rk, String(n), { expirationTtl: 120 });
      const check = await getJson('https://sessionserver.mojang.com/session/minecraft/hasJoined?username=' + encodeURIComponent(f.username) + '&serverId=' + encodeURIComponent(f.serverId));
      if (!check || !check.id) return send(401, { error: 'not verified' });
      const token = hex(24);
      await env.DB.put('s:' + token, norm(check.id), { expirationTtl: 3600 });
      if (check.name) {   // friends: remember who this account is (written only when it changes)
        const id = norm(check.id), low = String(check.name).toLowerCase();
        const fr = await getF(env, id);
        if (fr.name !== check.name) { fr.name = check.name; await env.DB.put('f:' + id, JSON.stringify(fr)); }
        if ((await env.DB.get('n:' + low)) !== id) await env.DB.put('n:' + low, id);
      }
      return send(200, { token, uuid: norm(check.id) });
    }
    if (route === 'GET /v1/me') {
      const uuid = await who(env, url.searchParams.get('token'));
      if (!uuid) return send(401, { error: 'bad token' });
      const p = await loadPlayer(env, uuid);
      await env.DB.put('c:' + uuid, '1', { expirationTtl: 1500 });   // heartbeat: this player is running YA CLIENT
      return send(200, Object.assign({ owned: p.owned.join(',') }, equippedMap(p), { client: '1' }));
    }
    if (route === 'POST /v1/equip') {
      const f = await form(request);
      const uuid = await who(env, f.token);
      if (!uuid) return send(401, { error: 'bad token' });
      if (!SLOTS.includes(f.slot)) return send(400, { error: 'bad slot' });
      const p = await loadPlayer(env, uuid);
      if (f.item === 'none') p.equipped[f.slot] = 'none';
      else {
        const it = ITEMS[f.item];
        if (!it || it.slot !== f.slot) return send(400, { error: 'bad item' });
        if (!p.owned.includes(f.item)) return send(403, { error: 'not owned' });
        p.equipped[f.slot] = f.item;
      }
      await savePlayer(env, uuid, p);
      return send(200, { ok: 1 });
    }
    const m = url.pathname.match(/^\/v1\/players\/([0-9a-fA-F-]{32,36})$/);
    if (request.method === 'GET' && m) {
      const pid = norm(m[1]);
      const out = equippedMap(await loadPlayer(env, pid));
      out.client = (await env.DB.get('c:' + pid)) ? '1' : '0';   // shown as a logo in the tab list
      return send(200, out);
    }

    if (route === 'POST /v1/admin/grant') {
      if (!env.ADMIN_KEY || request.headers.get('x-admin-key') !== env.ADMIN_KEY) return send(403, { error: 'forbidden' });
      const f = await form(request);
      let uuid = norm(f.uuid);
      if (!uuid && f.username) {
        const prof = await getJson('https://api.mojang.com/users/profiles/minecraft/' + encodeURIComponent(f.username));
        if (!prof || !prof.id) return send(404, { error: 'unknown username' });
        uuid = norm(prof.id);
      }
      if (!uuid) return send(400, { error: 'uuid or username required' });
      if (f.item === 'plus') {
        const p0 = await loadPlayer(env, uuid);
        const days = Math.max(1, Math.min(3650, parseInt(f.days || '30', 10) || 30));
        if (f.revoke === '1') p0.plusUntil = 0;
        else p0.plusUntil = Math.max(Date.now(), p0.plusUntil || 0) + days * 86400000;
        await savePlayer(env, uuid, p0);
        return send(200, { ok: 1, uuid, plus_until: new Date(p0.plusUntil).toISOString() });
      }
      let items = f.item === '*' ? Object.keys(ITEMS) : String(f.item || '').split(',').filter(Boolean);
      items = items.flatMap((i) => (BUNDLES[i] ? BUNDLES[i].items : [i]));
      if (!items.length || items.some((i) => !ITEMS[i])) return send(400, { error: 'unknown item' });
      const p = await loadPlayer(env, uuid);
      for (const i of items) {
        if (f.revoke === '1') p.owned = p.owned.filter((x) => x !== i);
        else if (!p.owned.includes(i)) p.owned.push(i);
      }
      await savePlayer(env, uuid, p);
      return send(200, { ok: 1, uuid, owned: p.owned.join(',') });
    }
    if (url.pathname.startsWith('/v1/friends')) { const r = await friendsRoute(env, request, url, route); if (r) return r; }
    return send(404, { error: 'not found' });
  } catch (e) {
    return send(500, { error: 'server error' });
  }
}


// Worker entry (Workers + static assets): /v1/* is the API, everything else is the website.
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname.startsWith('/v1/')) return onRequest({ request, env });
    return env.ASSETS.fetch(request);
  }
};
