/**
 * YA CLIENT store backend. No dependencies, Node 18+.
 *
 * Players:  identity is proven with Mojang's session server (the client calls joinServer, this
 *           server checks hasJoined), so nobody can equip items for another account.
 * Website:  after a payment succeeds, your site calls POST /v1/admin/grant with the secret key.
 *
 * Endpoints (every response is a flat JSON object of strings, which the client parses):
 *   GET  /v1/catalog                      public   item prices + store URL
 *   POST /v1/auth      username,serverId  player   returns a session token (1 hour)
 *   GET  /v1/me?token=                    player   owned items + equipped items
 *   POST /v1/equip     token,slot,item    player   equip an owned item ("none" to unequip)
 *   GET  /v1/players/:uuid                public   what a player wears (other players see this)
 *   GET  /v1/friends?token=&where=        player   friends (online first), incoming + outgoing requests; also a presence heartbeat
 *   POST /v1/friends/add     token,name   player   send a request (or accept if they already asked you)
 *   POST /v1/friends/accept  token,uuid   player
 *   POST /v1/friends/decline token,uuid   player   decline an incoming request / cancel an outgoing one
 *   POST /v1/friends/remove  token,uuid   player
 *   POST /v1/admin/grant  (x-admin-key)   website  uuid|username, item|"*" [, revoke=1]
 */
const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = process.env.PORT || 3000;
const ADMIN_KEY = process.env.ADMIN_KEY || '';
const DATA_FILE = process.env.DATA_FILE || path.join(__dirname, 'data', 'db.json');
const catalog = JSON.parse(fs.readFileSync(path.join(__dirname, 'catalog.json'), 'utf8'));
const SLOTS = ['wings', 'cape', 'hat', 'bandana'];

// ---------- storage (a JSON file is enough for a small store; swap for a database later) ----------
let db = { players: {} };
try { db = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8')); } catch (_) {}
let saveTimer = null;
function save() {
  clearTimeout(saveTimer);
  saveTimer = setTimeout(() => {
    fs.mkdirSync(path.dirname(DATA_FILE), { recursive: true });
    fs.writeFileSync(DATA_FILE, JSON.stringify(db));
  }, 300);
}
function player(uuid) {
  if (!db.players[uuid]) db.players[uuid] = { owned: [], equipped: {} };
  const p = db.players[uuid];
  if (!p.friends) { p.friends = []; p.incoming = []; p.outgoing = []; }
  return p;
}
const norm = (u) => String(u || '').replace(/-/g, '').toLowerCase();

// ---------- helpers ----------
const sessions = new Map(); // token -> { uuid, exp }
const hits = new Map();     // ip -> [timestamps] (rate limit for /v1/auth)

function send(res, code, obj) {
  const flat = {};
  for (const k of Object.keys(obj)) flat[k] = String(obj[k]);
  res.writeHead(code, { 'Content-Type': 'application/json; charset=utf-8', 'Access-Control-Allow-Origin': '*' });
  res.end(JSON.stringify(flat));
}
function body(req) {
  return new Promise((resolve) => {
    let data = '';
    req.on('data', (c) => { data += c; if (data.length > 10000) req.destroy(); });
    req.on('end', () => resolve(Object.fromEntries(new URLSearchParams(data))));
  });
}
function getJson(url) {
  return new Promise((resolve) => {
    https.get(url, { headers: { 'User-Agent': 'YA-CLIENT-Backend' } }, (r) => {
      if (r.statusCode !== 200) { r.resume(); return resolve(null); }
      let b = '';
      r.on('data', (c) => (b += c));
      r.on('end', () => { try { resolve(JSON.parse(b)); } catch (_) { resolve(null); } });
    }).on('error', () => resolve(null));
  });
}
function rateLimited(ip) {
  const now = Date.now();
  const list = (hits.get(ip) || []).filter((t) => now - t < 60000);
  list.push(now);
  hits.set(ip, list);
  return list.length > 20;
}
function who(token) {
  const s = sessions.get(token);
  if (!s || s.exp < Date.now()) { sessions.delete(token); return null; }
  return s.uuid;
}
function equippedMap(p) {
  const out = {};
  for (const slot of SLOTS) {
    const item = p.equipped[slot];
    out[slot] = item && p.owned.includes(item) ? item : 'none';
  }
  return out;
}

// ---------- friends ----------
const presence = new Map(); // uuid -> { t, where }
const ONLINE_MS = 75 * 1000;
const MAX_FRIENDS = 200;
function entry(uuid) {
  const p = db.players[uuid];
  const pr = presence.get(uuid);
  const on = !!pr && Date.now() - pr.t < ONLINE_MS;
  return { uuid, name: (p && p.name) || 'Player', on: on ? 1 : 0, where: on ? pr.where : '' };
}
function pack(uuids, sort) {
  let list = uuids.map(entry);
  if (sort) list.sort((a, b) => b.on - a.on || a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
  return list.map((e) => [e.uuid, e.name, e.on, e.where].join(',')).join(';');
}
function unlink(a, b) {
  const pa = player(a), pb = player(b);
  pa.friends = pa.friends.filter((x) => x !== b); pb.friends = pb.friends.filter((x) => x !== a);
  pa.incoming = pa.incoming.filter((x) => x !== b); pb.incoming = pb.incoming.filter((x) => x !== a);
  pa.outgoing = pa.outgoing.filter((x) => x !== b); pb.outgoing = pb.outgoing.filter((x) => x !== a);
}
function befriend(a, b) {
  unlink(a, b);
  player(a).friends.push(b); player(b).friends.push(a);
}
function findByName(name) {
  const q = String(name || '').trim().toLowerCase();
  if (!/^[a-z0-9_]{3,16}$/.test(q)) return null;
  for (const [uuid, p] of Object.entries(db.players)) if (p.name && p.name.toLowerCase() === q) return uuid;
  return null;
}

// ---------- routes ----------
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://x');
  const route = req.method + ' ' + url.pathname;
  try {
    if (route === 'GET /health') return send(res, 200, { ok: 1 });

    if (route === 'GET /v1/catalog') {
      const out = { _store: catalog.store };
      for (const [id, it] of Object.entries(catalog.items)) out[id] = it.price;
      return send(res, 200, out);
    }

    if (route === 'POST /v1/auth') {
      const ip = req.socket.remoteAddress || '';
      if (rateLimited(ip)) return send(res, 429, { error: 'rate limited' });
      const f = await body(req);
      if (!f.username || !f.serverId) return send(res, 400, { error: 'missing fields' });
      const check = await getJson('https://sessionserver.mojang.com/session/minecraft/hasJoined?username='
        + encodeURIComponent(f.username) + '&serverId=' + encodeURIComponent(f.serverId));
      if (!check || !check.id) return send(res, 401, { error: 'not verified' });
      const token = crypto.randomBytes(24).toString('hex');
      sessions.set(token, { uuid: norm(check.id), exp: Date.now() + 3600 * 1000 });
      if (check.name) { player(norm(check.id)).name = String(check.name); save(); }
      return send(res, 200, { token, uuid: norm(check.id) });
    }

    if (route === 'GET /v1/me') {
      const uuid = who(url.searchParams.get('token'));
      if (!uuid) return send(res, 401, { error: 'bad token' });
      const p = player(uuid);
      return send(res, 200, Object.assign({ owned: p.owned.join(',') }, equippedMap(p)));
    }

    if (route === 'POST /v1/equip') {
      const f = await body(req);
      const uuid = who(f.token);
      if (!uuid) return send(res, 401, { error: 'bad token' });
      if (!SLOTS.includes(f.slot)) return send(res, 400, { error: 'bad slot' });
      const p = player(uuid);
      if (f.item === 'none') {
        p.equipped[f.slot] = 'none';
      } else {
        const it = catalog.items[f.item];
        if (!it || it.slot !== f.slot) return send(res, 400, { error: 'bad item' });
        if (!p.owned.includes(f.item)) return send(res, 403, { error: 'not owned' });
        p.equipped[f.slot] = f.item;
      }
      save();
      return send(res, 200, { ok: 1 });
    }

    const m = url.pathname.match(/^\/v1\/players\/([0-9a-fA-F-]{32,36})$/);
    if (req.method === 'GET' && m) {
      return send(res, 200, equippedMap(player(norm(m[1]))));
    }

    if (route === 'GET /v1/friends') {
      const uuid = who(url.searchParams.get('token'));
      if (!uuid) return send(res, 401, { error: 'bad token' });
      const w = url.searchParams.get('where') === 'launcher' ? 'launcher' : 'game';
      const prev = presence.get(uuid);
      // being in the game wins over the launcher for a few seconds
      if (!(prev && prev.where === 'game' && w === 'launcher' && Date.now() - prev.t < 40000)) presence.set(uuid, { t: Date.now(), where: w });
      else presence.set(uuid, { t: Date.now(), where: 'game' });
      const p = player(uuid);
      return send(res, 200, { friends: pack(p.friends, true), incoming: pack(p.incoming, false), outgoing: pack(p.outgoing, false) });
    }

    if (route.startsWith('POST /v1/friends/')) {
      const f = await body(req);
      const me = who(f.token);
      if (!me) return send(res, 401, { error: 'bad token' });
      const act = url.pathname.slice('/v1/friends/'.length);
      const mine = player(me);
      if (act === 'add') {
        const other = findByName(f.name);
        if (!other) return send(res, 404, { error: 'not found' });
        if (other === me) return send(res, 400, { error: 'self' });
        if (mine.friends.includes(other)) return send(res, 409, { error: 'already' });
        if (mine.incoming.includes(other)) { befriend(me, other); save(); return send(res, 200, { ok: 1, status: 'accepted' }); }
        if (mine.outgoing.includes(other)) return send(res, 409, { error: 'pending' });
        if (mine.friends.length >= MAX_FRIENDS) return send(res, 400, { error: 'full' });
        mine.outgoing.push(other); player(other).incoming.push(me);
        save();
        return send(res, 200, { ok: 1, status: 'sent' });
      }
      const other = norm(f.uuid);
      if (!db.players[other]) return send(res, 404, { error: 'not found' });
      if (act === 'accept') {
        if (!mine.incoming.includes(other)) return send(res, 400, { error: 'no request' });
        if (mine.friends.length >= MAX_FRIENDS) return send(res, 400, { error: 'full' });
        befriend(me, other);
      } else if (act === 'decline' || act === 'remove') {
        unlink(me, other);
      } else return send(res, 404, { error: 'not found' });
      save();
      return send(res, 200, { ok: 1 });
    }

    if (route === 'POST /v1/admin/grant') {
      if (!ADMIN_KEY || req.headers['x-admin-key'] !== ADMIN_KEY) return send(res, 403, { error: 'forbidden' });
      const f = await body(req);
      let uuid = norm(f.uuid);
      if (!uuid && f.username) {
        const prof = await getJson('https://api.mojang.com/users/profiles/minecraft/' + encodeURIComponent(f.username));
        if (!prof || !prof.id) return send(res, 404, { error: 'unknown username' });
        uuid = norm(prof.id);
      }
      if (!uuid) return send(res, 400, { error: 'uuid or username required' });
      const items = f.item === '*' ? Object.keys(catalog.items) : [f.item];
      if (items.some((i) => !catalog.items[i])) return send(res, 400, { error: 'unknown item' });
      const p = player(uuid);
      for (const i of items) {
        const has = p.owned.includes(i);
        if (f.revoke === '1') { p.owned = p.owned.filter((x) => x !== i); }
        else if (!has) { p.owned.push(i); }
      }
      save();
      return send(res, 200, { ok: 1, uuid, owned: p.owned.join(',') });
    }

    send(res, 404, { error: 'not found' });
  } catch (e) {
    send(res, 500, { error: 'server error' });
  }
});

server.listen(PORT, () => console.log('YA CLIENT store backend on port ' + PORT));
