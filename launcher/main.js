const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');
const http = require('http');
const { Client, Authenticator } = require('minecraft-launcher-core');
const AdmZip = require('adm-zip');
const cfg = require('./config.json');

const dataDir = path.join(app.getPath('appData'), '.yaclient');
const gameDir = path.join(dataDir, 'game');
const weaveModsDir = path.join(os.homedir(), '.weave', 'mods');
const stateFile = path.join(dataDir, 'state.json');
const propsFile = path.join(os.homedir(), '.yaclient', 'config.properties'); // shared with the game client
fs.mkdirSync(gameDir, { recursive: true });
fs.mkdirSync(weaveModsDir, { recursive: true });

// GC tuning for smooth frame times (G1, short pauses, big young generation); valid on Java 8 and newer
const JVM_TUNING = ['-XX:+UnlockExperimentalVMOptions', '-XX:+UseG1GC', '-XX:G1NewSizePercent=20', '-XX:G1ReservePercent=20', '-XX:MaxGCPauseMillis=50', '-XX:G1HeapRegionSize=16M', '-XX:+ParallelRefProcEnabled', '-XX:+DisableExplicitGC'];
let state = { account: null, ram: 2048, javaPath: 'java', minimizeOnLaunch: false, lang: 'en', version: '' };
try { state = { ...state, ...JSON.parse(fs.readFileSync(stateFile, 'utf8')) }; } catch (_) {}
const save = () => fs.writeFileSync(stateFile, JSON.stringify(state, null, 2));

let win;
function createWindow() {
  win = new BrowserWindow({
    width: 1120, height: 700, minWidth: 980, minHeight: 620, frame: false, backgroundColor: '#0a0a0c',
    title: 'YA CLIENT',
    icon: path.join(__dirname, 'assets', 'icon.png'),
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true }
  });
  win.removeMenu();
  win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}
const showSplash = () => {}, splashSet = () => {}, closeSplash = () => {};
app.whenReady().then(() => {
  createWindow();
  checkLauncherUpdate();
});

// The launcher updates itself: it checks the latest GitHub release at start, downloads the new
// installer in the background and installs it when the launcher is closed. Players never reinstall by hand.
function checkLauncherUpdate() {
  if (!app.isPackaged) return;
  try {
    const { autoUpdater } = require('electron-updater');
    autoUpdater.autoDownload = true;
    autoUpdater.autoInstallOnAppQuit = true;
    autoUpdater.on('update-available', (i) => send('log', `Launcher update ${i.version} found, downloading...`));
    autoUpdater.on('update-downloaded', (i) => send('log', `Launcher ${i.version} downloaded: it installs when you close the launcher.`));
    autoUpdater.on('error', (e) => send('log', 'Launcher update check: ' + (e && e.message)));
    autoUpdater.checkForUpdates().catch(() => {});
  } catch (e) { /* electron-updater missing: skip */ }
}
app.on('window-all-closed', () => app.quit());

ipcMain.on('win', (_e, a) => {
  if (!win) return;
  if (a === 'min') win.minimize();
  if (a === 'max') (win.isMaximized() ? win.unmaximize() : win.maximize());
  if (a === 'close') win.close();
});
const send = (ch, d) => win && !win.isDestroyed() && win.webContents.send(ch, d);
// never show the access token in the launcher console
const clean = (s) => String(s).replace(/token:[^\s)]+/g, 'token:***');

// ---------- shared config file (~/.yaclient/config.properties) ----------
function readProps() {
  const out = {};
  try {
    for (const line of fs.readFileSync(propsFile, 'utf8').split(/\r?\n/)) {
      if (!line || line[0] === '#' || line[0] === '!') continue;
      const i = line.search(/[=:]/);
      if (i < 0) continue;
      out[line.slice(0, i).trim()] = line.slice(i + 1).trim().replace(/\\([:=#!])/g, '$1');
    }
  } catch (_) {}
  return out;
}
function writeProps(updates) {
  let lines = [];
  try { lines = fs.readFileSync(propsFile, 'utf8').split(/\r?\n/); } catch (_) {}
  const done = new Set();
  lines = lines.map((l) => {
    const i = l.search(/[=:]/);
    if (l && l[0] !== '#' && l[0] !== '!' && i > 0) {
      const k = l.slice(0, i).trim();
      if (Object.prototype.hasOwnProperty.call(updates, k)) { done.add(k); return k + '=' + updates[k]; }
    }
    return l;
  });
  for (const k of Object.keys(updates)) if (!done.has(k)) lines.push(k + '=' + updates[k]);
  fs.mkdirSync(path.dirname(propsFile), { recursive: true });
  fs.writeFileSync(propsFile, lines.filter((l, i, a) => l !== '' || i < a.length - 1).join('\n') + '\n');
}

// ---------- content (news, servers, ads, store) ----------
function getJson(url) {
  return new Promise((resolve, reject) => {
    const mod = url.startsWith('https') ? https : http;
    mod.get(url, { headers: { 'User-Agent': 'YA-CLIENT-Launcher' }, timeout: 8000 }, (r) => {
      if (r.statusCode >= 300 && r.statusCode < 400 && r.headers.location) return resolve(getJson(r.headers.location));
      if (r.statusCode !== 200) { r.resume(); return reject(new Error('HTTP ' + r.statusCode)); }
      let b = ''; r.on('data', (c) => (b += c)); r.on('end', () => {
        try { resolve(JSON.parse(b)); } catch (e) { reject(e); }
      });
    }).on('timeout', function () { this.destroy(new Error('timeout')); }).on('error', reject);
  });
}
let contentCache = null;
async function loadContent() {
  let c = {};
  try { c = JSON.parse(fs.readFileSync(path.join(__dirname, 'content.json'), 'utf8')); } catch (_) {}
  if (c.remote) { try { c = { ...c, ...(await getJson(c.remote)) }; } catch (_) {} }
  contentCache = c;
  return c;
}
ipcMain.handle('content', () => loadContent());
ipcMain.handle('catalog', async () => {
  const c = contentCache || (await loadContent());
  if (!c.apiBase) return { error: 'not_configured' };
  try { return { data: await getJson(c.apiBase.replace(/\/+$/, '') + '/v1/catalog'), store: c.storeUrl || '' }; }
  catch (_) { return { error: 'offline' }; }
});
ipcMain.handle('plus', async () => {
  const c = contentCache || (await loadContent());
  const uuid = state.account && state.account.auth && String(state.account.auth.uuid || '').replace(/-/g, '');
  if (!c.apiBase || !uuid) return { plus: !!state.plus, offline: true };
  try {
    const r = await getJson(c.apiBase.replace(/\/+$/, '') + '/v1/players/' + uuid);
    state.plus = !!r && r.plus === '1';
    save();
    return { plus: state.plus };
  } catch (_) { return { plus: !!state.plus, offline: true }; }
});
ipcMain.handle('open-external', (_e, url) => {
  if (/^https?:\/\//i.test(String(url || ''))) shell.openExternal(url);
});

// ---------- versions and mods ----------
const FALLBACK_VERSIONS = ['1.21.4', '1.21.1', '1.20.6', '1.20.4', '1.20.1', '1.19.4', '1.19.2', '1.18.2', '1.17.1', '1.16.5', '1.15.2',
  '1.14.4', '1.13.2', '1.12.2', '1.11.2', '1.10.2', '1.9.4', '1.8.9', '1.8', '1.7.10'];
let versionsCache = null;
ipcMain.handle('versions', async () => {
  if (versionsCache) return versionsCache;
  try {
    const m = await getJson('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json');
    versionsCache = { online: true, latest: m.latest, versions: m.versions.map((v) => ({ id: v.id, type: v.type, time: v.releaseTime })) };
  } catch (_) {
    versionsCache = { online: false, latest: { release: FALLBACK_VERSIONS[0], snapshot: FALLBACK_VERSIONS[0] },
      versions: FALLBACK_VERSIONS.map((id) => ({ id, type: 'release', time: '' })) };
    setTimeout(() => { versionsCache = null; }, 30000);   // try again later
  }
  return versionsCache;
});
// official key art for each Minecraft line (Mojang launcher patch notes), fetched at runtime
let artCache = null;
ipcMain.handle('art', async () => {
  if (artCache) return artCache;
  try {
    const j = await getJson('https://launchercontent.mojang.com/v2/javaPatchNotes.json');
    const out = {};
    (j.entries || []).forEach((e) => {
      if (e.type !== 'release' || !e.image || !e.image.url) return;
      const m = /^(\d+\.\d+)/.exec(e.version || '');
      if (!m || out[m[1]]) return;
      out[m[1]] = { img: 'https://launchercontent.mojang.com' + e.image.url, title: e.title || '', text: e.shortText || '' };
    });
    artCache = out; return out;
  } catch (_) { return {}; }
});
ipcMain.handle('mods', () => {
  try { return JSON.parse(fs.readFileSync(path.join(__dirname, 'mods.json'), 'utf8')); } catch (_) { return { mods: [] }; }
});
function versionType(id) {
  const v = versionsCache && versionsCache.versions.find((x) => x.id === id);
  return v ? v.type : 'release';
}
// Java major version a Minecraft version needs
function javaMajorFor(v) {
  const nv = String(v).match(/^(\d+)\.\d+/);
  if (nv && +nv[1] >= 26) return 25;   // new year-based versions (26.x) need Java 25
  const sn = String(v).match(/^(\d\d)w/);
  if (sn && +sn[1] >= 26) return 25;
  const m = String(v).match(/^1\.(\d+)(?:\.(\d+))?/);
  if (!m) return sn ? 21 : 8;
  const minor = +m[1], patch = +(m[2] || 0);
  if (minor <= 16) return 8;
  if (minor <= 19 || (minor === 20 && patch < 5)) return 17;
  return 21;
}

// ---------- Java ----------
function findJava(major) {
  const roots = [
    process.env['ProgramFiles'], process.env['ProgramFiles(x86)'],
    process.env['LOCALAPPDATA'] && path.join(process.env['LOCALAPPDATA'], 'Programs')
  ].filter(Boolean);
  const vendors = ['Eclipse Adoptium', 'Java', 'Zulu', 'Amazon Corretto', 'Microsoft', 'BellSoft', 'AdoptOpenJDK', 'Eclipse Foundation', 'Semeru'];
  const re = major === 8
    ? /(jdk|jre)[-_ ]?(1\.)?8([._-]|$)|zulu-?8|jdk8|jre8|1\.8\.0/i
    : new RegExp('(jdk|jre|zulu|java)[-_ ]?' + major + '([._-]|$)|(^|[-_ ])' + major + '([._]|$)', 'i');
  for (const r of roots) {
    for (const v of vendors) {
      const dir = path.join(r, v);
      let names;
      try { names = fs.readdirSync(dir); } catch (_) { continue; }
      for (const n of names) {
        if (!re.test(n)) continue;
        const exe = path.join(dir, n, 'bin', 'java.exe');
        if (fs.existsSync(exe)) return exe;
      }
    }
  }
  return null;
}
const findJava8 = () => findJava(8) || findBundledJava(8);
// Java runtimes the launcher downloaded itself live in %APPDATA%/.yaclient/java/<major>
function findBundledJava(major) {
  const root = path.join(dataDir, 'java', String(major));
  const walk = (dir, depth) => {
    let names; try { names = fs.readdirSync(dir); } catch (_) { return null; }
    if (names.indexOf('bin') >= 0) { const exe = path.join(dir, 'bin', 'java.exe'); if (fs.existsSync(exe)) return exe; }
    if (depth <= 0) return null;
    for (const n of names) { const r = walk(path.join(dir, n), depth - 1); if (r) return r; }
    return null;
  };
  return walk(root, 2);
}
// Finds a Java of the right version or downloads one (Eclipse Temurin JRE) so the player never has to.
async function ensureJava(major) {
  if (major === 8 && state.javaPath && state.javaPath !== 'java' && fs.existsSync(state.javaPath)) return state.javaPath;
  const have = findJava(major) || findBundledJava(major);
  if (have) return have;
  const dir = path.join(dataDir, 'java', String(major));
  const zipFile = path.join(dataDir, `java-${major}.zip`);
  try {
    send('log', `Java ${major} not found: downloading it automatically...`);
    send('status', { k: 'java_dl', pct: 5 });
    fs.mkdirSync(dir, { recursive: true });
    await download(`https://api.adoptium.net/v3/binary/latest/${major}/ga/windows/x64/jre/hotspot/normal/eclipse`, zipFile);
    send('status', { k: 'java_dl', pct: 8 });
    new AdmZip(zipFile).extractAllTo(dir, true);
    try { fs.unlinkSync(zipFile); } catch (_) {}
    const exe = findBundledJava(major);
    if (exe) { send('log', `Java ${major} installed: ${exe}`); return exe; }
  } catch (e) {
    send('log', `Automatic Java ${major} download failed: ${e.message}`);
    try { fs.unlinkSync(zipFile); } catch (_) {}
  }
  return major === 8 ? 'java' : null;   // last resort for 8: whatever "java" is on the PATH
}
function resolveJava(major) {
  if (major === 8) {
    if (state.javaPath && state.javaPath !== 'java' && fs.existsSync(state.javaPath)) return state.javaPath;
    return findJava(8) || 'java';
  }
  return findJava(major);
}

// ---------- state / account ----------
ipcMain.handle('state', () => ({ ...state, dir: dataDir, version: state.version || cfg.minecraftVersion, ya: cfg.minecraftVersion, yaFabric: ['26.2', '1.21.11', '1.21.8', '1.21.4', '1.21.1'], gameDir }));
ipcMain.handle('settings', (_e, s) => { state = { ...state, ...s }; save(); return state; });
ipcMain.handle('logout', () => { state.account = null; save(); return true; });
ipcMain.handle('detect-java', () => findJava8());
// server latency: time to open a TCP connection (what the launcher shows next to a server)
ipcMain.handle('server-status', (_e, host, port) => new Promise((resolve) => {
  const hs = String(host || '').slice(0, 253);
  if (!/^[A-Za-z0-9._-]+$/.test(hs)) return resolve({ online: false });
  const pt = Math.min(65535, +port || 25565);
  const vi = (n) => { const b = []; do { let x = n & 127; n >>>= 7; if (n) x |= 128; b.push(x); } while (n); return Buffer.from(b); };
  const pk = (id, body) => { const p = Buffer.concat([vi(id), body]); return Buffer.concat([vi(p.length), p]); };
  const hb = Buffer.from(hs, 'utf8'); const pb = Buffer.alloc(2); pb.writeUInt16BE(pt);
  const t0 = Date.now(); let buf = Buffer.alloc(0); let done = false;
  const sock = require('net').connect({ host: hs, port: pt });
  const end = (v) => { if (done) return; done = true; try { sock.destroy(); } catch (_) {} resolve(v); };
  const rd = (b, o) => { let r = 0, s = 0, i = o; for (;;) { if (i >= b.length) return null; const x = b[i++]; r |= (x & 127) << s; if (!(x & 128)) break; s += 7; if (s > 35) return null; } return [r, i]; };
  const flat = (d) => { if (d == null) return ''; if (typeof d === 'string') return d; return (d.text || '') + (d.extra || []).map(flat).join(''); };
  sock.setTimeout(4500, () => end({ online: false }));
  sock.on('error', () => end({ online: false }));
  sock.on('connect', () => { sock.write(pk(0, Buffer.concat([vi(47), vi(hb.length), hb, pb, vi(1)]))); sock.write(pk(0, Buffer.alloc(0))); });
  sock.on('data', (d) => {
    buf = Buffer.concat([buf, d]); if (buf.length > 600000) return end({ online: false });
    const l = rd(buf, 0); if (!l || buf.length < l[1] + l[0]) return;
    const id = rd(buf, l[1]); const sl = id && rd(buf, id[1]); if (!sl) return end({ online: false });
    try {
      const j = JSON.parse(buf.slice(sl[1], sl[1] + sl[0]).toString('utf8'));
      const motd = flat(j.description).replace(/§./g, '').slice(0, 200);
      const fav = typeof j.favicon === 'string' && /^data:image\/png;base64,[A-Za-z0-9+\/=]+$/.test(j.favicon) && j.favicon.length < 60000 ? j.favicon : '';
      end({ online: true, ping: Date.now() - t0, players: j.players ? j.players.online : 0, max: j.players ? j.players.max : 0, version: j.version ? String(j.version.name).slice(0, 40) : '', motd, favicon: fav });
    } catch (_) { end({ online: false }); }
  });
}));
ipcMain.handle('ping', (_e, host, port) => new Promise((resolve) => {
  const h = String(host || '').slice(0, 253);
  if (!/^[A-Za-z0-9._-]+$/.test(h)) return resolve(-1);
  const t0 = Date.now(); const sock = require('net').connect({ host: h, port: Math.min(65535, +port || 25565) });
  const end = (v) => { try { sock.destroy(); } catch (_) {} resolve(v); };
  sock.setTimeout(3500, () => end(-1)); sock.on('connect', () => end(Date.now() - t0)); sock.on('error', () => end(-1));
}));
ipcMain.handle('open-folder', () => shell.openPath(gameDir));
ipcMain.handle('set-lang', (_e, code) => {
  state.lang = code; save();
  try { writeProps({ lang: code }); } catch (_) {}
  return true;
});

// module toggles (same file the game reads)
ipcMain.handle('mods-get', () => {
  const p = readProps(); const out = {};
  for (const k of Object.keys(p)) {
    const m = k.match(/^mod\.(.+)\.enabled$/);
    if (m) out[m[1]] = p[k] === 'true';
  }
  return out;
});
ipcMain.handle('mods-set', (_e, id, on) => {
  if (!/^[a-z_]+$/.test(String(id))) return false;
  writeProps({ ['mod.' + id + '.enabled']: on ? 'true' : 'false' });
  return true;
});

// Non-premium players get the same UUID a cracked server gives them (md5 of "OfflinePlayer:<name>"), so other YA CLIENT players
// can find their logo and cosmetics, and the identity stays the same on every launch.
function offlineUuid(name) {
  const b = require('crypto').createHash('md5').update('OfflinePlayer:' + name, 'utf8').digest();
  b[6] = (b[6] & 0x0f) | 0x30; b[8] = (b[8] & 0x3f) | 0x80;
  const h = b.toString('hex');
  return h.slice(0, 8) + '-' + h.slice(8, 12) + '-' + h.slice(12, 16) + '-' + h.slice(16, 20) + '-' + h.slice(20);
}
function fixOfflineUuid() {
  const a = state.account;
  if (a && a.type === 'offline' && a.auth && a.name) a.auth.uuid = offlineUuid(a.name);
}

ipcMain.handle('login-offline', (_e, name) => {
  const n = String(name || '').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16);
  if (n.length < 3) return { error: 'name_short' };
  state.account = { type: 'offline', name: n, auth: Authenticator.getAuth(n) };
  fixOfflineUuid();
  save();
  return { name: n, type: 'offline' };
});

ipcMain.handle('login-ms', async () => {
  try {
    const { Auth } = require('msmc');
    const auth = new Auth('select_account');
    const xbox = await auth.launch('electron');
    const mc = await xbox.getMinecraft();
    let refresh = '';
    try { refresh = xbox.save(); } catch (_) {}
    state.account = { type: 'microsoft', name: mc.profile.name, auth: mc.mclc(), refresh };
    save();
    return { name: mc.profile.name, type: 'microsoft' };
  } catch (e) {
    return { error: 'ms_failed' };
  }
});

// ---------- friends (backend /v1/friends; identity proven through Mojang's session server) ----------
function request(method, url, body, type) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const mod = u.protocol === 'https:' ? https : http;
    const r = mod.request(u, { method, timeout: 9000, headers: { 'User-Agent': 'YA-CLIENT-Launcher', ...(body ? { 'Content-Type': type, 'Content-Length': Buffer.byteLength(body) } : {}) } }, (res) => {
      let b = ''; res.on('data', (c) => (b += c)); res.on('end', () => {
        let j = null; try { j = JSON.parse(b); } catch (_) {}
        resolve({ status: res.statusCode, json: j });
      });
    });
    r.on('timeout', () => r.destroy(new Error('timeout')));
    r.on('error', reject);
    if (body) r.write(body);
    r.end();
  });
}
let apiTok = null, apiTokExp = 0, apiTokFor = '';
async function apiToken() {
  const c = contentCache || (await loadContent());
  const a = state.account;
  if (!c.apiBase || !a || a.type !== 'microsoft' || !a.auth || !a.auth.access_token) return null;
  if (apiTok && Date.now() < apiTokExp && apiTokFor === a.name) return apiTok;
  const uuid = String(a.auth.uuid || '').replace(/-/g, '');
  let secret = readProps()['api.secret'] || '';
  if (!/^[0-9a-f]{32}$/.test(secret)) { secret = require('crypto').randomBytes(16).toString('hex'); try { writeProps({ 'api.secret': secret }); } catch (_) {} }
  const r = await request('POST', c.apiBase.replace(/\/+$/, '') + '/v1/auth',
    'username=' + encodeURIComponent(a.name) + '&uuid=' + uuid + '&secret=' + secret, 'application/x-www-form-urlencoded');
  if (r.status !== 200 || !r.json || !r.json.token) return null;
  apiTok = r.json.token; apiTokExp = Date.now() + 50 * 60 * 1000; apiTokFor = a.name;
  return apiTok;
}
const parseFriends = (s) => String(s || '').split(';').filter(Boolean).map((x) => {
  const p = x.split(','); return { uuid: p[0], name: p[1], online: p[2] === '1', where: p[3] || '' };
});
async function friendsCall(method, pathq, form) {
  const c = contentCache || (await loadContent());
  if (!c.apiBase) return { error: 'not_configured' };
  if (!state.account || state.account.type !== 'microsoft') return { error: 'premium' };
  try {
    const tok = await apiToken();
    if (!tok) return { error: 'login' };
    const base = c.apiBase.replace(/\/+$/, '') + '/v1/friends';
    const r = method === 'GET'
      ? await request('GET', base + '?where=launcher&token=' + tok)
      : await request('POST', base + '/' + pathq, 'token=' + tok + '&' + form, 'application/x-www-form-urlencoded');
    if (r.status === 401) { apiTok = null; return { error: 'login' }; }
    if (r.status === 404) return { error: 'not_found' };
    if (r.status === 409) return { error: 'already' };
    if (r.status >= 400 || !r.json) return { error: r.status === 400 ? 'self' : 'offline' };
    if (method === 'GET') return { friends: parseFriends(r.json.friends), incoming: parseFriends(r.json.incoming), outgoing: parseFriends(r.json.outgoing) };
    return { ok: true, status: r.json.status || 'ok' };
  } catch (_) { return { error: 'offline' }; }
}
ipcMain.handle('friends-list', () => friendsCall('GET'));
ipcMain.handle('friends-act', (_e, act, arg) => {
  if (act === 'add') return friendsCall('POST', 'add', 'name=' + encodeURIComponent(String(arg || '')));
  if (['accept', 'decline', 'remove'].includes(act)) return friendsCall('POST', act, 'uuid=' + encodeURIComponent(String(arg || '')));
  return { error: 'bad' };
});
// stay "online" for friends while the launcher is open
setInterval(() => { if (state.account && state.account.type === 'microsoft') friendsCall('GET').catch(() => {}); }, 40000);

// ---------- downloads ----------
function download(url, dest) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'YA-CLIENT-Launcher' } }, (r) => {
      if (r.statusCode >= 300 && r.statusCode < 400 && r.headers.location) return resolve(download(r.headers.location, dest));
      if (r.statusCode !== 200) return reject(new Error('HTTP ' + r.statusCode));
      const f = fs.createWriteStream(dest);
      r.pipe(f); f.on('finish', () => f.close(resolve)); f.on('error', reject);
    }).on('error', reject);
  });
}
async function fetchRelease(repo, matcher, dest) {
  const rel = await getJson(`https://api.github.com/repos/${repo}/releases/latest`);
  const asset = (rel.assets || []).find(matcher);
  if (!asset) throw new Error(`No file found in the release of ${repo}`);
  await download(asset.browser_download_url, dest);
  return rel.tag_name;
}

// Replaces the Mojang logo of the loading screen with the YA CLIENT one.
// (The game uses the top-left pixel of the image as the full-screen background color.)
function patchSplash() {
  const jar = path.join(gameDir, 'versions', cfg.minecraftVersion, cfg.minecraftVersion + '.jar');
  if (!fs.existsSync(jar)) return;
  const png = fs.readFileSync(path.join(__dirname, 'assets', 'splash.png'));
  const name = 'assets/minecraft/textures/gui/title/mojang.png';
  const zip = new AdmZip(jar);
  const entry = zip.getEntry(name);
  const sigs = zip.getEntries().filter((e) => /^META-INF\/[^/]+\.(SF|DSA|RSA)$/i.test(e.entryName));
  if (entry && sigs.length === 0 && Buffer.compare(entry.getData(), png) === 0) return;
  // the original jar is signed: without removing the signature Java rejects the modified file
  sigs.forEach((e) => zip.deleteFile(e.entryName));
  if (entry) zip.deleteFile(name);
  zip.addFile(name, png);
  zip.writeZip(jar);
}

// ---------- Fabric (Minecraft 1.16+) ----------
const vcmp = (a, b) => {
  const pa = String(a).split('.').map((n) => parseInt(n, 10) || 0), pb = String(b).split('.').map((n) => parseInt(n, 10) || 0);
  for (let i = 0; i < 3; i++) { const d = (pa[i] || 0) - (pb[i] || 0); if (d) return d; }
  return 0;
};
const fabricSupported = (v) => /^\d+\.\d+(\.\d+)?$/.test(v) && vcmp(v, '1.16') >= 0;

// Installs the Fabric profile for this Minecraft version and, when published, the YA CLIENT Fabric jar.
async function setupFabric(mc, withSodium) {
  const loaders = await getJson(`https://meta.fabricmc.net/v2/versions/loader/${mc}`);
  const pick = (Array.isArray(loaders) ? loaders : []).find((l) => l.loader && l.loader.stable) || (Array.isArray(loaders) ? loaders[0] : null);
  if (!pick) throw new Error(`Fabric is not available for Minecraft ${mc}`);
  const profile = await getJson(`https://meta.fabricmc.net/v2/versions/loader/${mc}/${pick.loader.version}/profile/json`);
  const id = profile.id;
  const dir = path.join(gameDir, 'versions', id);
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, id + '.json'), JSON.stringify(profile));

  const inst = path.join(gameDir, 'instances', mc);
  const mods = path.join(inst, 'mods');
  fs.mkdirSync(mods, { recursive: true });
  let hasYa = false;
  try {
    const yaJar = path.join(mods, 'YAClient-fabric.jar');
    const tag = await fetchRelease(cfg.repo, (a) => a.name === `yaclient-fabric-${mc}.jar`, yaJar);
    send('log', `YA CLIENT (Fabric ${mc}) ${tag} updated.`);
    hasYa = true;
    // the mod needs Fabric API
    const api = await getJson(`https://api.modrinth.com/v2/project/fabric-api/version?game_versions=${encodeURIComponent(JSON.stringify([mc]))}&loaders=${encodeURIComponent(JSON.stringify(['fabric']))}`);
    const f = Array.isArray(api) && api[0] && api[0].files && (api[0].files.find((x) => x.primary) || api[0].files[0]);
    if (f) {
      fs.readdirSync(mods).filter((n) => /^fabric-api/i.test(n)).forEach((n) => fs.unlinkSync(path.join(mods, n)));
      await download(f.url, path.join(mods, f.filename));
    } else send('log', `Fabric API for ${mc} not found.`);
  } catch (e) {
    send('log', `YA CLIENT for ${mc} is not published yet (${e.message}). Starting Fabric without it.`);
    try { fs.unlinkSync(path.join(mods, 'YAClient-fabric.jar')); } catch (_) {}
  }
  // optional performance mod
  try {
    fs.readdirSync(mods).filter((n) => /^sodium/i.test(n)).forEach((n) => fs.unlinkSync(path.join(mods, n)));
    if (withSodium) {
      const so = await getJson(`https://api.modrinth.com/v2/project/sodium/version?game_versions=${encodeURIComponent(JSON.stringify([mc]))}&loaders=${encodeURIComponent(JSON.stringify(['fabric']))}`);
      const sf = Array.isArray(so) && so[0] && so[0].files && (so[0].files.find((x) => x.primary) || so[0].files[0]);
      if (sf) await download(sf.url, path.join(mods, sf.filename));
      else send('log', `Sodium for ${mc} not found.`);
    }
  } catch (e) { send('log', `Sodium: ${e.message}`); }
  return { id, dir: inst, hasYa };
}

// ---------- launch ----------
ipcMain.handle('play', async (_e, opts) => {
  if (!state.account) return { error: 'need_login' };
  // Microsoft tokens expire: renew the session before every launch
  if (state.account.type === 'microsoft') {
    if (!state.account.refresh) return { error: 'session_expired' };
    try {
      const { Auth } = require('msmc');
      const xbox = await new Auth('select_account').refresh(state.account.refresh);
      const mc = await xbox.getMinecraft();
      let r2 = state.account.refresh;
      try { r2 = xbox.save(); } catch (_) {}
      state.account = { type: 'microsoft', name: mc.profile.name, auth: mc.mclc(), refresh: r2 };
      save();
    } catch (e) { return { error: 'session_expired' }; }
  }
  try {
    const content = contentCache || (await loadContent());
    // tell the game where the store is and which language to use
    try {
      writeProps({ lang: state.lang || 'en', 'api.base': (content.apiBase || '').replace(/\/+$/, ''), 'store.url': content.storeUrl || '' });
    } catch (_) {}

    const version = state.version || cfg.minecraftVersion;
    const yaMode = version === cfg.minecraftVersion;   // YA CLIENT (Weave + mods) only exists for this version
    const loaderJar = path.join(dataDir, 'weave-loader.jar');
    let fabric = null;
    if (yaMode) {
      send('status', { k: 'updating', pct: 2 });
      const clientJar = path.join(weaveModsDir, 'YAClient.jar');
      const tag = await fetchRelease(cfg.repo, (a) => a.name === cfg.jarAsset, clientJar);
      send('log', `YA CLIENT ${tag} updated.`);

      send('status', { k: 'loader', pct: 6 });
      if (!fs.existsSync(loaderJar)) {
        await fetchRelease(cfg.weaveRepo,
          (a) => /\.jar$/.test(a.name) && !/sources|javadoc/i.test(a.name), loaderJar);
      }
    } else if (fabricSupported(version) && (state.loader || 'fabric') !== 'vanilla') {
      send('status', { k: 'loader', pct: 6 });
      fabric = await setupFabric(version, state.loader === 'sodium');
    } else {
      send('log', `Minecraft ${version}: vanilla launch (YA CLIENT mods are available on ${cfg.minecraftVersion}).`);
    }

    const major = yaMode ? 8 : javaMajorFor(version);
    const javaPath = await ensureJava(major);
    if (!javaPath) return { error: 'java_missing', detail: String(major) };
    send('log', `Java ${major}: ${javaPath}`);

    const launcher = new Client();
    showSplash(); splashSet('Starting Minecraft', 8);
    const startedAt = Date.now();
    launcher.on('progress', (p) => { splashSet(p.type === 'assets' ? 'Downloading assets' : 'Downloading Minecraft', 8 + Math.round((p.task / p.total) * 60)); send('status', { k: 'download', arg: p.type, pct: 6 + Math.round((p.task / p.total) * 70) }); });
    launcher.on('debug', (m) => send('log', clean(m)));
    launcher.on('data', (m) => {
      const l = clean(m); send('log', l);
    });
    launcher.on('arguments', () => {
      if (!yaMode) return;
      try { patchSplash(); } catch (e) { send('log', 'splash: ' + e.message); }
    });
    launcher.on('close', (code) => {
      closeSplash();
      if (win && !win.isDestroyed() && win.isMinimized()) win.restore();
      const crashed = code !== 0 && Date.now() - startedAt < 60000;
      send('status', { k: crashed ? 'crashed' : 'closed', arg: code, pct: 0, done: true, crashed });
    });

    const launchOpts = {
      authorization: (fixOfflineUuid(), state.account.auth),
      root: gameDir,
      javaPath,
      version: fabric ? { number: version, type: versionType(version), custom: fabric.id } : { number: version, type: versionType(version) },
      overrides: fabric ? { gameDirectory: fabric.dir } : {},
      window: { width: String(state.width || 1280), height: String(state.height || 720), fullscreen: !!state.fullscreen },
      memory: { max: `${state.ram}M`, min: '1024M' },
      customArgs: (yaMode ? [`-javaagent:${loaderJar}`] : []).concat(state.account.type === 'offline' ? ['-Dyaclient.offline=true'] : []).concat(JVM_TUNING)
    };
    if (opts && opts.server && opts.server.host) {
      launchOpts.server = { host: String(opts.server.host), port: String(opts.server.port || 25565) };
    }
    send('status', { k: 'starting', pct: 80 });
    const proc = await launcher.launch(launchOpts);
    if (!proc) { closeSplash(); return { error: 'launch_failed' }; }
    send('status', { k: 'ingame', pct: 100, playing: true });
    if (state.minimizeOnLaunch && win) win.minimize();
    return { ok: true };
  } catch (e) {
    closeSplash();
    return { error: 'launch_error', detail: e.message };
  }
});
