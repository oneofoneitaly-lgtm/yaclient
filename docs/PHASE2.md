# YA CLIENT - Phase 2 (Mixin features)

Phase 1 (this release) only uses the Weave event API (`TickEvent`, `RenderGameOverlayEvent`).
Some features need to change Minecraft's own code. Weave supports this through **Mixin** (and
access wideners): the Weave Gradle plugin accepts `mixinConfigs` and `accessWideners` in the
`weave { configure { ... } }` block (see the official template: github.com/Weave-MC/ExampleMod).

The files in `client/phase2/` are **skeletons**. They are stored as `.txt` so the build ignores
them. They were written without being able to compile against Minecraft, so expect to adjust
class/method names (check them against the template and your mappings).

## How to enable Phase 2
1. Rename the skeletons: remove `.txt`, put the Java files in
   `client/src/main/java/dev/yaclient/mixin/` and the JSON in `client/src/main/resources/`.
2. In `client/build.gradle.kts`, inside `weave { configure { ... } }` add:
   `mixinConfigs = listOf("yaclient.mixins.json")`
   (keep `compileOnly("org.spongepowered:mixin:0.8.5")` and the spongepowered maven repository,
   both are already in the template).
3. Build one feature at a time and test it in game.

## Feature map
| Feature            | Needs                                   | Where to hook                                                    |
|--------------------|-----------------------------------------|------------------------------------------------------------------|
| Cosmetics (render) | Mixin                                   | `RenderPlayer.<init>`: `addLayer(new CosmeticLayer(this))`       |
| 1.7 animations     | Mixin                                   | `ItemRenderer.renderItemInFirstPerson` (BLOCK case)              |
| Hit color          | Mixin                                   | `RendererLivingEntity.setBrightness` (the red hurt overlay)      |
| Hide vanilla crosshair | Mixin                               | `GuiIngame.showCrosshair` -> return false when our crosshair is on |
| AutoGG             | Mixin (chat packet)                     | `NetHandlerPlayClient.handleChat` HEAD, match the server's end-of-game message |
| Levelhead          | Mixin or render event + HTTP API        | name tag rendering; cache API results in a background thread     |
| Minimap            | Render event only                       | sample `MapColor` of the top block every few ticks into a `DynamicTexture` |
| 3D waypoints       | Render-world hook                       | project waypoint positions to the screen, draw label + distance  |
| Motion blur strength | Access widener                        | `ShaderGroup` uniforms of the phosphor shader                    |

## Cosmetics design
* Catalog and selection already exist: `Cosmetics.equipped(slot)` returns the item id or `none`.
* Textures go in `src/main/resources/assets/yaclient/textures/cosmetics/<item>.png`.
* One `LayerRenderer<AbstractClientPlayer>` renders all four slots. Each slot has a small
  `ModelBase` (wings = two flapping planes, cape = a plane that follows body movement, hat = boxes
  on the head, bandana = a thin box around the head).
* Only the local player is rendered in this design. Showing cosmetics to *other* YA CLIENT
  players needs a server that maps player UUID -> cosmetics. That is a separate backend project.

## Intentionally not included
* "Hit delay fix": it changes attack timing, which many servers treat as a combat advantage.
  YA CLIENT only ships features that are information, visuals or quality of life.
