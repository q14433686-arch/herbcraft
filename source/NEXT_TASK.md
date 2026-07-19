# NEXT_TASK

## Recommended next task: stabilize source state and fix alchemy catalyst correctly

The next AI should not add new features first. The immediate priority is to make the repository internally consistent again.

## Why this is the next task

Recent work caused repeated source/jar desynchronization. The author observed real problems:

- hidden dishes missing in-game;
- catalyst brewing not upgrading potions;
- untranslated `item.minecraft.lingering_potion.effect.witch...` keys;
- Mixin crash due missing `BrewingStandCatalystMixin` class;
- Codex overview / 总纲 and advancement tree not matching what was claimed.

## Required work

1. Cleanly inspect current source tree.
2. Fix alchemy mixin class/source mismatch.
3. Verify Hundred-Flower Feast and Deadly Hodgepodge are present in **source and built jars**.
4. Verify catalyst items can be inserted into brewing stand ingredient slot.
5. Verify catalyst brewing preserves stack components and only sets `potion_quality = EXCEPTIONAL`.
6. Rebuild advancement tree using correct `advancements.*` translation keys and a single `herbcraft:root` tab.
7. Confirm Codex `百草经总纲` is actually registered in `HerbalChapter` and visible through the Codex.

## Acceptance criteria

- `./gradlew build --rerun-tasks` passes.
- `:alchemy:runServer` or a client run reaches mod initialization without Mixin errors.
- `herbcraft_alchemy.mixins.json` references only classes that exist in the built jar.
- `/give @p herbcraft_cuisine:hundred_flower_feast` and `/give @p herbcraft_cuisine:deadly_hodgepodge` work.
- Brewing stand accepts `hundred_flower_feast` and `deadly_hodgepodge` in the ingredient slot.
- A clarified potion + Hundred-Flower Feast becomes exceptional and keeps existing components.
- A murky potion + Deadly Hodgepodge becomes exceptional and keeps existing components.
- No raw translation keys like `item.minecraft.lingering_potion.effect.witch_*` appear.
- Codex shows `百草经总纲`.
- Advancement tree is one Herbcraft page, not fragmented roots.
