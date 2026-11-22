# YA CLIENT

Client PvP per Minecraft 1.8.9, senza Forge, con launcher .exe (rosso / bianco / nero).

## Funzioni (nessun cheat)
FPS, CPS, Coordinate, Keystrokes, Armatura e durabilità, Toggle Sprint, Zoom (tasto C).
In gioco: **Shift destro** apre il menu per attivare/disattivare i moduli.

## Come funziona
- `client/`   mod Java che gira su **Weave Loader** (loader open source per 1.8.9 vanilla, senza Forge).
- `launcher/` launcher Electron: login Microsoft o offline, scarica `YAClient.jar` dalle Release di GitHub, avvia Minecraft 1.8.9.
- `.github/workflows/build.yml` compila jar e installer su GitHub.

## Pubblicare su GitHub (passi)
1. Crea un repository (es. `ya-client`) e carica tutto il contenuto di questa cartella.
2. In `launcher/config.json` metti `"repo": "tuoutente/ya-client"` (il workflow lo fa da solo, ma meglio averlo giusto).
3. Dal tuo PC, nella cartella del repo:
   ```
   git tag v1.0.0
   git push origin v1.0.0
   ```
4. Dopo qualche minuto, in **Releases** trovi `YA-CLIENT-Setup.exe` (da scaricare e installare) e `YAClient.jar`.
5. Ogni nuova versione: nuovo tag (`v1.0.1`...). Il launcher scarica sempre l'ultimo jar da solo.

## Requisiti sul PC di chi gioca
Java 8 installato (oppure indica il percorso in Impostazioni).

## Se la build fallisce
Il progetto usa le API di Weave (plugin Gradle e eventi). Se una versione è cambiata,
confronta `client/build.gradle.kts` e gli import in `YAClient.java` con il template ufficiale:
https://github.com/Weave-MC/Weave-Mod-Template
