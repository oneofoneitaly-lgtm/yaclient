// /download -> redirects straight to the latest Windows installer (.exe), no GitHub page in between.
export async function onRequest() {
  const repo = 'adrianmaftean09-commits/yaclient';
  try {
    const r = await fetch('https://api.github.com/repos/' + repo + '/releases/latest', { headers: { 'User-Agent': 'yaclient-site', Accept: 'application/vnd.github+json' }, cf: { cacheTtl: 120, cacheEverything: true } });
    if (r.ok) {
      const j = await r.json();
      const a = (j.assets || []).find(x => /setup.*\.exe$/i.test(x.name)) || (j.assets || []).find(x => /\.exe$/i.test(x.name));
      if (a) return Response.redirect(a.browser_download_url, 302);
    }
  } catch (e) {}
  return Response.redirect('https://github.com/' + repo + '/releases/latest', 302);
}
