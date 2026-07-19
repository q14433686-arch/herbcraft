# CHANGELOG

## 2026-07-19

### Changed

- Ported the full three-module suite from Minecraft `26.1.2` to `26.2`:
  - `minecraft_version` `26.1.2` → `26.2`; Fabric API `0.151.0+26.1.2` → `0.155.2+26.2`; Fabric Loader stays `0.19.3`.
  - Fabric Loom `1.16-SNAPSHOT` → `1.17.16` and Gradle Wrapper `9.4.1` → `9.5.1` (required toolchain for 26.2).
  - Jade compat pin `26.1.0+fabric` → `26.2.9+fabric`.
  - All three `fabric.mod.json` files now declare `minecraft: "~26.2"` and `fabric-api: ">=0.155.2+26.2"`.

### Fixed (26.2 breaking changes)

- Core client: 26.2 moved the current screen off `Minecraft` into `Gui` — `context.client().setScreen(...)` → `context.client().gui.setScreen(...)` and `Minecraft.getInstance().screen` → `Minecraft.getInstance().gui.screen()`.
- Alchemy: vanilla renamed `MobEffect#isInstantenous` to `isInstantaneous` (7 call sites in `AdvancedPotionTooltips`, `AdvancedPotionStackInitializer`, `AlchemyRegistries`).

### Notes

- No mixin targets, datapack JSON, recipes, loot, or networking code needed changes. See `docs/PORTING_26.2.md` for the full investigation.

## 2026-06-12

### Added

- Added responsive Herb Codex text layout with wrapping, scrolling and left-panel ellipsis trimming.
- Added creative tab variants for tattered pages in earlier stage, preserved in this release line.
- Added last meal death flavor message.
- Added centralized consume sound resolver for herbs and cuisine items.
- Added validation scripts for villager trade tags and language key diffs.

### Fixed

- Fixed long Codex text overflowing in English and other long-text languages.
- Fixed language key parity between `zh_cn` and `en_us`.
- Verified all data-driven villager trades are referenced by trade tags with `replace=false`.

### Builds

- `herbcraft-1.1.3.jar`
- `herbcraft-alchemy-1.0.0.jar`
- `herbcraft-cuisine-0.3.3.jar`
