# Updating without players re-downloading anything by hand

## 1. The client jar (mods, cosmetics models)
Every time a player presses LAUNCH the launcher downloads the newest `YAClient.jar` from the **latest GitHub release**
(only if it changed). So to ship a new mod or a new cosmetic design: build, publish a new tag `vX.Y.Z`
(the GitHub Action attaches the jar), done. Players get it on their next launch, automatically.

## 2. The launcher itself (auto-update)
The launcher now contains `electron-updater`. At start it checks the latest release; if a newer installer exists
it downloads it in the background and installs it when the launcher is closed.
For this the release must contain, next to `YA-CLIENT-Setup.exe`, the files `latest.yml` and `YA-CLIENT-Setup.exe.blockmap`
(electron-builder creates all three in `launcher/dist`). In your workflow:

    - run: npx electron-builder --win nsis --publish never
      working-directory: launcher
    - uses: softprops/action-gh-release@v2
      with:
        files: |
          launcher/dist/YA-CLIENT-Setup.exe
          launcher/dist/YA-CLIENT-Setup.exe.blockmap
          launcher/dist/latest.yml
          client/build/libs/*.jar

IMPORTANT: bump `"version"` in `launcher/package.json` for every launcher change (1.0.1, 1.0.2 ...),
otherwise the updater thinks nothing changed. Players need to install the exe by hand only ONE last time
(the first version that contains the updater).

## 3. New cosmetics
* **Catalog / ownership / prices** live on the server (`site/functions/v1/[[path]].js`, tables `ITEMS` and `BUNDLES`):
  adding an item id there and redeploying the site needs no client update. You grant it with the admin page.
* **The look** of a new cosmetic (its 3D model) is code in the client jar, so a *new design* ships with a new
  jar release (step 1, automatic for players). Items already in the jar can be sold, granted or removed at any time
  from the server without touching the client.
