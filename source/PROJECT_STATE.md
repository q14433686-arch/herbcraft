# PROJECT_STATE

This file records the current repository state as honestly as possible after the recent source/jar desynchronization problems.

## Versions

| Module | Mod ID | Version |
|---|---|---:|
| core | `herbcraft` | `1.0.0` |
| alchemy | `herbcraft_alchemy` | `1.0.0` |
| cuisine | `herbcraft_cuisine` | `1.0.0` |

## Implemented / present in source

### Core

- `data/herbcraft/herbs.json` drives raw edible herb definitions.
- Effect immunity system exists:
  - `EffectImmunityManager`
  - `LivingEntityMixin`
  - `HerbcraftAPI.grantEffectImmunity`
  - `HerbcraftAPI.isImmuneToEffect`
- Herb Codex framework exists, with dynamic chapters and tattered pages.
- Animal food/tempt tag validator script exists; actual tag files must be checked before release because this area has been lost during rollbacks before.
- Herb stack code exists; actionbar threshold/fade/overload feedback has been added in some versions, but must be rechecked before release.

### Cuisine

- Water bowl and hodgepodge systems exist.
- Recipe isolation validator exists and should be treated as mandatory.
- Hidden dishes are designed as:
  - `hundred_flower_feast`
  - `raw_hundred_flower_feast`
  - `deadly_hodgepodge`
  - `raw_deadly_hodgepodge`
- Hidden dishes should be water-bowl raw recipes followed by smelting/smoking/campfire cooking.
- Recent issue: hidden dish files have disappeared from source in prior rollbacks even when they existed in release jars. Always inspect source and jar contents.

### Alchemy

- Essence/potion/coating systems exist.
- High-tier potion quality and bonus components exist.
- `ItemCostAdvancedPotionMixin` exists for randomized potion villager trade matching.
- Witch potion and hidden cuisine catalyst work has been attempted.
- 2026-06-12 P0 pass: `BrewingStandCatalystMixin.java` has been restored in source and reconnects hidden cuisine catalysts to brewing stand `isBrewable` / `doBrew`; alchemy server smoke reaches `Done`. Continue checking mixin JSON against built classes before every release.

## Known current risk / not fully trustworthy areas

- Alchemy catalyst brewing is not yet proven in a real client after final source cleanup.
- Advancement files have been generated several times; some are still placeholder-like and need a dedicated correctness pass.
- The Codex overview / 总纲 has been requested but must be verified in source before claiming it exists.
- Release jars and source have gone out of sync multiple times; next AI must build fresh from source and inspect jar contents.

## Validation scripts present

- `scripts/check_lang_diff.py`
- `scripts/validate_recipe_isolation.py`
- `scripts/validate_item_textures.py`
- `scripts/validate_task_abcd.py`
- `scripts/validate_animal_food_tags.py`
- `scripts/validate_extended_features.py`
- `scripts/verify_built_jars.py`

These scripts are useful but not perfect. If source state changes, update validators rather than trusting stale assumptions.

## P1.5 Knowledge system v1.1 update

- Knowledge is now backed by per-player multi-flag cognition data (`encountered`, `heard`, `tasted`, `practiced`, `eatCount`) instead of using the old single enum as the source of truth.
- Old `HerbcraftKnowledge` enum-style save data is migrated on load into the new flags; a compatibility mirror is still written to reduce rollback/module conflicts.
- Core herb `tasted` is granted only from the finished consumption pipeline (`EffectResolver` via `finishUsingItem`), not from `Stats.ITEM_USED`, fixing the right-click-start unlock class of bugs.
- `experiencedEffects` are stored per player and per entry when an effect actually triggers during raw herb consumption.
- Tattered pages now support `page_kind` (`common`, `trade`, `rare`, `errata`) on the same `tattered_page` item.
- `data/herbcraft/knowledge_rumors.json` provides hand-authored claims, including suspicious/false claims; no random fake-effect generation is used.
- Codex entries can display claim lines with player truth marks, and the client can click claim markers to cycle personal notes via a C2S payload.
- Errata pages resolve one unresolved claim and store verified true/false claim state.
- Mastery is now based on flags plus practical mastery or a core-only fallback, with external practice routes registered by alchemy/cuisine where available.

## P1.6 knowledge/codex bugfix pass

- The global `Item.finishUsingItem` mixin now runs Herbcraft raw-herb settlement only after vanilla finish returns and only if the item is registered in `herbs.json`; right-click/start-use paths are not settlement triggers.
- Knowledge flags (`encountered/heard/tasted/practiced/eatCount`), experienced effects, player claim marks, and verified claim sets are persisted on player data with migration from legacy `HerbcraftKnowledge`.
- Herb Codex display states now render distinct Unknown / Encountered / Heard / Tasted / Mastered content for core herb entries.
- Heard entries render data-driven claims from `data/herbcraft/knowledge_rumors.json`; claim marks are clickable in the Codex via `ClaimMarkPayload`.
- `knowledge_rumors.json` now covers all 96 registered raw herb entries with deterministic hand-authored-style claims based on actual nature data and plausible suspicious claims.
- Alchemy essence and cuisine fixed-dish acquisition hooks register practice routes and mark practiced where crafting stats prove player-made output; trade/provenance-specific acquisition remains TODO-commented and conservative.

## P1.7 Jade/overlay consistency fix

- Jade now reads the same `KnowledgeAPI.displayState` ordinal as the Codex/knowledge sync layer and renders `knowledge.herbcraft.state.*` labels, so ENCOUNTERED/HEARD/TASTED/MASTERED no longer collapse into the old heard/mastered wording.
- Dropped item Jade entity provider registration is disabled to avoid duplicate Herbcraft tooltip sections when Jade also displays the ItemStack tooltip. Block Jade support remains registered.
- Added `HerbcraftOverlayMessages`, a lightweight server-side delayed overlay queue. Herb stack stage/fade messages are delayed so seven-emotions/pharmacology messages no longer overwrite them in the same tick.

## P1.8 information-gating and prompt-channel fix

- Jade and inventory ItemStack tooltips now gate content by the five-state display model, not just by state name. `ENCOUNTERED` shows only unknown placeholders and never reveals `NatureProfile`; `HEARD` remains conservative; `TASTED` may show personally verified nature; `MASTERED` shows full nature/usages.
- Herb stack threshold/fade prompts have moved from ActionBar to private system chat messages, so they no longer compete with seven-emotions ActionBar prompts. Herb stack overload still uses ActionBar and also sends a private chat warning.
- The previous lightweight delayed ActionBar queue was removed because P1.8 uses separated display channels instead of delayed same-channel messages.

## P1.9 flavor/taste split

- `NatureProfile` now separates pharmacological `flavors` from descriptive `tasteFlavors` / JSON `taste_flavors`.
- `HerbNatureRegistry` parses `taste_flavors` with fallback to `flavors` for older data; all bundled `herb_natures.json` entries now explicitly include `taste_flavors`.
- Pharmacology and cuisine mechanics continue to read `flavors` only; tasted-state mouthfeel text now reads `tasteFlavors`.
- Pharmacological bitter was narrowed to 16 entries with clearing/toxic/alchemy-core semantics. 29 entries retain bitter only as taste, and 16 absurd bitter entries were removed from both layers.

## P1.10 non-bitter legacy defect fixes

- Encountered-state wording is now object-neutral instead of plant-specific, shared through `knowledge.herbcraft.encountered.description` across Codex/Jade/inventory tooltip paths.
- Jade and inventory tooltip content gates now branch by exact five-state display state. `ENCOUNTERED` returns after unknown placeholders and does not reveal true `NatureProfile`; `HEARD` is conservative; `TASTED` shows personally verified nature; `MASTERED` shows full nature/usages.
- Dropped item information uses the Jade ItemEntity provider as the single Herbcraft dropped-item source. The inventory ItemStack tooltip callback skips when no screen is open, preventing the Jade world overlay from also injecting the ItemStack tooltip path.
- Herb stack stage/fade prompts now use private system chat messages. Seven-emotions remains ActionBar; overload remains ActionBar plus private chat.
- Seven-emotions effect multipliers were audited and are connected to `EffectResolver`; ActionBar texts now name the affected property for better observability.

## P1.11 Codex claim hitbox alignment fix

- Codex claim marker click hitboxes now align to the actual rendered claim lines. `CodexSnapshotPayload.ClaimSnap` carries the display line index of each claim, and `CodexScreen` places the clickable area on that line instead of assuming claims begin at line 0.
- No gameplay logic, Jade, knowledge state, data, or pharmacology behavior was changed in this pass.
