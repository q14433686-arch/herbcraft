# BUGS_TODO

## Known bugs / risks

| Priority | Area | Item |
|---:|---|---|
| P0 | Alchemy | Source/jar mismatch has caused Mixin crashes. **2026-06-12 P0 pass:** `BrewingStandCatalystMixin.java` restored; built jar now contains the class. Still verify `herbcraft_alchemy.mixins.json` references only existing built classes before every release. |
| P0 | Alchemy | Hundred-Flower Feast / Deadly Hodgepodge catalyst brewing has been reconnected to `BrewingStandBlockEntity` via `BrewingStandCatalystMixin`; server smoke passes. Still needs real interactive client verification in the brewing stand. |
| P0 | Cuisine | Hidden dish items/recipes have disappeared from source during rollback. Verify source and built jar both contain `hundred_flower_feast`, `raw_hundred_flower_feast`, `deadly_hodgepodge`, `raw_deadly_hodgepodge`. |
| P1 | Codex | `百草经总纲` has been requested; verify it is actually registered in `HerbalChapter`, not only present in lang files. |
| P1 | Advancements | Advancement JSON exists in some builds but may be placeholder-like or incorrectly keyed. Needs a dedicated rebuild/trigger pass. |
| P2 | Compat | Jade/REI compatibility is not implemented. |

## TODO / new feature ideas

| Priority | Idea |
|---:|---|
| P0 | Run a real client smoke test, not only Gradle build. |
| P1 | Add exact custom advancement criteria instead of impossible placeholders for complex Herbcraft events. |
| P1 | Implement optional Jade support that respects Codex knowledge tiers. |
| P1 | Add in-game diagnostics or GameTest-style checks for hodgepodge and catalyst brewing. |
| P2 | Improve Codex usage pages after the overview is stable. |
| P2 | Add configurable values for witch potion chance, herb stack thresholds, and immunity duration multipliers. |

## Recently addressed in P1.5

- Fixed the knowledge unlock model that could treat item-use stats as mastery. Raw herb `tasted` now comes from finished consumption only.
- Added migration from old enum knowledge save data into multi-flag knowledge data.

## New follow-up risks after P1.5

- Claim marker hitboxes are intentionally lightweight/self-drawn inside the existing Codex screen; do a real client pass to tune icon placement and scrolling feel.
- Villager-trade-specific provenance is still represented conservatively as acquisition/heard through inventory scanning unless a future dedicated trade hook is added.
- Potion "practiced" provenance remains conservative where vanilla brewing does not expose a clean owner/crafting stat.

## Addressed in P1.6

- Fixed the high-priority issue where Herbcraft settlement could be reached from the broad item finish hook without sufficient guarding. The hook is now post-finish and herb-registry guarded.
- Filled out core Herb Codex display-state content for encountered/heard/tasted/mastered entries.
- Expanded the knowledge rumor data file to all current `herbs.json` raw herb entries.

## Remaining follow-up after P1.6

- Real-client UX pass for Codex claim hitboxes is still recommended; server smoke/build validate wiring but not mouse feel.
- Brewing/catalyst player provenance remains conservative because vanilla brewing does not expose a clean owner event in current code. A future mixin can mark practiced when a player removes brewed output.
- Hidden feast/deadly hodgepodge and weapon coating practice routes are partially represented by ingredient/essence routes but can be made more exact in a dedicated provenance pass.

## Addressed in P1.7

- Jade tooltip state labels now use the multi-state display ordinal instead of old heard/mastered thresholds.
- Dropped item duplicate Herbcraft/Jade tooltip sections are mitigated by not registering the separate Herbcraft item-entity Jade component.
- Herb stack threshold/fade ActionBar messages are delayed through `HerbcraftOverlayMessages`, reducing same-tick overwrite by seven-emotions prompts.

## Follow-up after P1.7

- Real client verification is recommended for dropped item Jade behavior because Jade may vary in whether ItemStack tooltips are shown for item entities.
- If future systems add more ActionBar prompts, consider replacing the lightweight delayed queue with a full priority queue and coalescing policy.

## Addressed in P1.8

- Fixed ENCOUNTERED-state information leakage in Jade and inventory tooltips. Seen-but-unidentified herbs no longer reveal actual nature/flavors/usages.
- Fixed herb_stack stage/fade prompt channel conflict by moving those prompts to private chat/system messages. Seven-emotions remains ActionBar.
- Removed the P1.7 delayed overlay queue approach in favor of channel separation.

## Follow-up after P1.8

- Real-client verification is still recommended for Jade dropped item behavior and exact tooltip composition because Jade may combine item-stack tooltips differently by config/version.

## Addressed in P1.9

- Fixed the semantic conflict where mouthfeel `bitter` and pharmacological `bitter` used the same field and caused excessive herb_stack reduction.
- Added explicit `taste_flavors` data for all bundled herb nature entries.

## Follow-up after P1.9

- Future flavor-driven mechanics must read `NatureProfile.flavors()` only.
- Future mouthfeel/poetic taste text should read `NatureProfile.tasteFlavors()` or `HerbNatureAPI.tasteFlavors()`.
- Additional balance passes may revisit non-bitter flavor choices, but this pass intentionally only addressed the bitter conflict.

## Addressed in P1.10

- Fixed plant-specific Encountered wording for non-plant Herbcraft objects such as ice, eggs, resin, fungi, and powders.
- Fixed tooltip/Jade information leakage in Encountered state by gating content, not only the state label.
- Fixed dropped ItemEntity duplicate Herbcraft tooltip source by choosing the Jade ItemEntity provider as the single dropped-item path and suppressing world-overlay ItemStack tooltip injection.
- Verified seven-emotions pharmacology result consumption by `EffectResolver`; updated actionbar wording to make the affected property visible.
- Moved herb_stack stage/fade prompts to private system chat to avoid ActionBar competition.

## Remaining follow-up after P1.10

- Real client validation is recommended for Jade ItemEntity + ItemStack tooltip interaction because Jade configs can affect whether vanilla item tooltip lines are included in the overlay.
- If future pharmacology mechanics become more complex, consider optional debug logging behind a config flag.

## Addressed in P1.11

- Fixed Codex rumor/claim truth-marker click area mismatch: the interactive hitbox now follows the displayed claim line instead of being offset onto earlier detail lines.
