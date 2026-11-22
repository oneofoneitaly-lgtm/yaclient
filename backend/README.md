# YA CLIENT store backend

Small dependency-free Node.js (18+) server that connects your website to the game:

```
 your website  --(payment OK)-->  POST /v1/admin/grant  -->  ownership saved
 game client   --(Mojang login)--> /v1/auth, /v1/me, /v1/equip
 other players --(public)-------> GET /v1/players/<uuid>   (what someone wears)
```

## Run it
```
cd backend
ADMIN_KEY=choose-a-long-random-secret node server.js
```
Put it behind HTTPS (Render, Railway, Fly.io, or a VPS with Caddy/nginx). Then set the public URL in
`launcher/content.json` -> `"apiBase": "https://api.your-domain.example"`. The launcher writes it into
the game config, so every player gets it automatically.

Edit `catalog.json` to change items, slots and prices (`store` = the page where players buy).
The item ids must match the client: angel, dragon, classic, crimson, midnight, tophat, crown, red, black.
To add a new cosmetic, add it to `catalog.json` and to the client (`Cosmetics.java`, `CosmeticTextures`/models).

## Connect your payment provider
After a successful payment, call the grant endpoint from your site's server (never from the browser):
```
curl -X POST https://api.your-domain.example/v1/admin/grant \
  -H "x-admin-key: $ADMIN_KEY" \
  -d "username=PlayerName" -d "item=angel"
```
* `username` (resolved through Mojang) or `uuid`; `item` can be `*` to grant everything.
* Add `-d revoke=1` to take an item back (refunds).
* Stripe: do it inside your `checkout.session.completed` webhook; collect the Minecraft username
  as a custom field at checkout. PayPal / Tebex / Ko-fi work the same way: webhook -> grant.

## Security notes
* Players prove their identity with Mojang's session server (hasJoined), so offline/cracked
  accounts cannot own or equip store items.
* `/v1/admin/grant` is protected by `ADMIN_KEY`; keep it only on your servers.
* Data is stored in `data/db.json`. Back it up, or move to a database when the store grows.
