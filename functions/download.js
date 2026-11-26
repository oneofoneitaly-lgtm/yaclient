// /download -> streams the newest launcher .exe from the skyfxmod GitHub release through this site (no GitHub page).
async function findExe(repo) {
  const H = { 'User-Agent': 'yaclient-site', Accept: 'application/vnd.github+json' };
  for (const u of ['releases/tags/latest', 'releases/latest']) {
    try {
      const r = await fetch('https://api.github.com/repos/' + repo + '/' + u, { headers: H, cf: { cacheTtl: 120, cacheEverything: true } });
      if (!r.ok) continue;
      const j = await r.json();
      const as = j.assets || [];
      const a = as.find((x) => /^ya[-_ ]?client\.exe$/i.test(x.name)) || as.find((x) => /setup.*\.exe$/i.test(x.name)) || as.find((x) => /\.exe$/i.test(x.name));
      if (a) return a;
    } catch (e) {}
  }
  return null;
}

async function downloadExe() {
  const repo = 'oneofoneitaly-lgtm/skyfxmod'; // the repository where the launcher is built and released
  const a = await findExe(repo);
  if (a) {
    try {
      const r = await fetch(a.browser_download_url, { redirect: 'follow', cf: { cacheTtl: 300, cacheEverything: true } });
      if (r.ok && r.body) {
        const h = new Headers({
          'Content-Type': 'application/octet-stream',
          'Content-Disposition': 'attachment; filename="' + a.name.replace(/"/g, '') + '"',
          'Cache-Control': 'public, max-age=300'
        });
        const len = r.headers.get('Content-Length');
        if (len) h.set('Content-Length', len);
        return new Response(r.body, { status: 200, headers: h });
      }
    } catch (e) {}
  }
  return new Response('<!doctype html><meta charset="utf-8"><title>YA CLIENT</title><body style="font:20px monospace;background:#111;color:#fff;display:grid;place-items:center;height:100vh;margin:0;text-align:center"><div>The launcher is being prepared.<br>Try again in a few minutes or ask on Discord.<br><br><a style="color:#fff" href="/">Back</a></div>', { status: 503, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
}


export async function onRequest() {
  return downloadExe();
}
