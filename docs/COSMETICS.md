# Cosmetics (Beta Tester bundle)

Items: ya_cape (static cape, glitching logo), ya_wings, ya_bandana, ya_pet. Bundle id: `beta`.
YA Plus = subscription: the cape cloth also waves and glows (without Plus the cape is still and only the logo glitches).

Your logo: client/src/main/resources/assets/yaclient/logo.png (square, transparent, 512x512).
Nametag in F5: vanilla look + logo. Turn off with `nametag.self=false` in ~/.yaclient/config.properties.
While a YA cape is worn the vanilla cape is hidden.

Unlock: website /admin -> item `beta` (bundle) or `plus` + days (subscription, default 30).

## Bundle Monochrome (id `mono`)
Oggetti: `mono_cape` (mantello nero con infinito bianco animato), `mono_wings` (ali da drago nere), `mono_bandana` (bandana animata), `mono_pet` (yin‑yang che gira).
Sul server: `site/functions/v1/[[path]].js` (ITEMS/BUNDLES). Per assegnarlo: pagina admin → oggetto `mono`.
Nel gioco: Shift destro → mini menu al centro → icona maglietta = menu Cosmetici (separato dal menu Mod).

## Aggiungere nuovi cosmetici
1. Server (senza far riscaricare nulla): aggiungi voci in ITEMS/BUNDLES, ridistribuisci il sito.
2. Grafica nuova: codice in `client/.../cosmetics`, poi nuovo tag `vX.Y.Z` → il launcher scarica il nuovo jar a ogni avvio.
