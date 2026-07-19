# Herbcraft / 百草可食

Herbcraft is a Fabric mod suite for Minecraft Java Edition `26.2`. It turns vanilla plants, odd natural materials, cuisine, alchemy, and knowledge discovery into a layered survival system.

**Author:** q14433686-arch · **Co-developer:** Arena.ai Agent

## Modules

This repository is a three-module Gradle project:

| Module | Mod ID | Current version | Role |
|---|---|---:|---|
| `core` | `herbcraft` | `1.0.0` | Edible herb definitions, raw eating effects, Herb Codex / 百草经, tattered pages, knowledge API, effect immunity, animal food tags. |
| `alchemy` | `herbcraft_alchemy` | `1.0.0` | Essences, herbal potions, clarified/murky potion lines, potion quality/bonus components, weapon coating, witch integration, catalyst brewing experiments. |
| `cuisine` | `herbcraft_cuisine` | `1.0.0` | Water bowl, curated dishes, raw-to-cooked medicinal dishes, Herbal Hodgepodge / 百草盅, Hundred-Flower Feast / 百花宴, Deadly Hodgepodge / 剧毒蛊. |

## Locked environment

- Minecraft `26.2`
- Fabric Loader `0.19.3`
- Fabric API `0.155.2+26.2`
- Java `25`
- Gradle Wrapper `9.5.1`
- Fabric Loom `1.17.16` (26.2 requires Loom 1.17)
- Mojang official mappings

## Design philosophy

1. **Eating herbs is gambling; brewing potions is contract; cuisine is craft.**
2. **Information is earned.** Unknown Codex entries, hidden recipes, Jade/tooltip hints, and recipe hints must not spoil full information too early.
3. **Data first, code second.** Core herb values live in `data/herbcraft/herbs.json`; recipes, trades, tags, loot, and language should stay data-driven unless behavior truly needs Java.

## Development discipline

- Keep stable IDs stable. Do not rename existing item, potion, recipe, component, knowledge, or trade IDs without explicit author approval.
- Prefer vanilla/Fabric data systems: tags, recipes, loot events, data-driven villager trades.
- Do not use old `TradeOfferHelper`; MC 26.1 villager trading is data-driven.
- Do not replace vanilla loot tables. Use Fabric loot table modify events and guard with `source.isBuiltin()`.
- Do not add Mixins casually. Existing approved Mixins are for short-use projectiles, special bucket handling, effect immunity, potion trade matching, witch throws, and brewing stand catalyst experiments.
- Keep modules removable: `core` must run alone; `cuisine` and `alchemy` must not be required by core.

## Build

Use Java 25:

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

## Important current warning

The repository has recently suffered from source/release desynchronization. Before trusting any jar, run a clean build and inspect `herbcraft_alchemy.mixins.json` against actual class files. See `PROJECT_STATE.md` and `BUGS_TODO.md`.

## Release guide for the author

Upload release jars through **GitHub Releases**. Do **not** put release jars in the source repository.

For the next AI handoff, the author only needs to say:

> 请先读 AI_ONBOARDING.md
