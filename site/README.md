# YA CLIENT website (Cloudflare Pages)

Static site + API (Pages Functions, folder `functions/`). No server needed.

## Deploy
1. Cloudflare dashboard > Workers & Pages > Create > Pages > **Connect to Git** > pick the `yaclient` repo.
   Build command: none. Build output directory: `site`. (Drag-and-drop upload does NOT support the `functions` folder; use Git or `npx wrangler pages deploy site`.)
2. Create the KV namespace: Cloudflare dashboard > Storage & Databases > KV > Create (name `ya-client`).
3. In the Pages project: Settings > Bindings > Add > KV namespace, variable name `DB` (exactly), pick `ya-client`. Do it for Production (and Preview).
4. Settings > Variables and Secrets > add secret `ADMIN_KEY` = a long random password only you know.
5. Redeploy (Deployments > Retry, or upload again) so bindings apply.
6. `config.js` only holds the Discord/GitHub links (PayPal is not used for the free beta).

## Connect game and launcher
In `launcher/content.json` of the client project set:
  "apiBase": "https://YOUR-SITE.pages.dev"   (or your own domain)
  "storeUrl": "https://YOUR-SITE.pages.dev/store"
Then publish a new tag so the launcher is rebuilt.

## Beta Tester flow
The bundle is free for beta testers. The tester writes the username on /store and opens a Discord ticket.
You open /admin, enter your admin key, the username and `beta`, press Grant. It unlocks in game immediately
(Right Shift > Cosmetics > equip).
To give EVERYONE the cosmetics during an open beta, add the variable `BETA_OPEN` = `1` in Pages > Settings > Variables.

## Items
`beta` = bundle (cape, wings, bandana, pet). Single items: ya_cape, ya_wings, ya_bandana, ya_pet.

## Add or change items
Items live in `functions/v1/[[path]].js` (repo root) (ITEMS, BUNDLES) and in `store.html`. New models also need code in the client.

## YA Plus
In /admin use item `plus` and the number of days (default 30) to give or extend a subscription.
Plus members see the cape cloth wave and glow (also visible to other players). Revoke removes it.

## Friends
Friend requests and online status live in the same Function (`/v1/friends`, KV keys `f:`, `n:`, `o:`).
Players appear as "online" for about 100 seconds after their last heartbeat. A player can only be added by name after
they have launched YA CLIENT once. KV free plan allows 1000 writes/day; each online player writes about once a minute,
so for many players use the Workers Paid plan ($5/month) or ask to move presence to D1.

## Deploy as a Worker with static assets (new Cloudflare dashboard)
`wrangler.jsonc` + `worker.js` make the same site + API work as a Worker (KV binding `DB` is declared in the config, so no
dashboard binding is needed). Connect the repo under Settings > Builds, root directory `site`, deploy command
`npx wrangler deploy`. Then add the secret `ADMIN_KEY` in Settings > Variables and Secrets.
If the deploy complains about the KV namespace, copy the id of your `ya-client` namespace into `kv_namespaces` (`"id": "..."`).
