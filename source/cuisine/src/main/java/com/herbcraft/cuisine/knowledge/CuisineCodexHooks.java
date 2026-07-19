package com.herbcraft.cuisine.knowledge;

import com.herbcraft.knowledge.CodexLoot;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public final class CuisineCodexHooks {
   private static final Map<ResourceKey<LootTable>, Float> CUISINE_TABLES = Map.ofEntries(
      Map.entry(BuiltInLootTables.VILLAGE_BUTCHER, 0.18F),
      Map.entry(BuiltInLootTables.VILLAGE_FISHER, 0.18F),
      Map.entry(BuiltInLootTables.VILLAGE_SHEPHERD, 0.14F),
      Map.entry(BuiltInLootTables.VILLAGE_TAIGA_HOUSE, 0.12F),
      Map.entry(BuiltInLootTables.VILLAGE_PLAINS_HOUSE, 0.12F),
      Map.entry(BuiltInLootTables.VILLAGE_SAVANNA_HOUSE, 0.12F),
      Map.entry(BuiltInLootTables.VILLAGE_DESERT_HOUSE, 0.12F),
      Map.entry(BuiltInLootTables.VILLAGE_SNOWY_HOUSE, 0.12F),
      Map.entry(BuiltInLootTables.SHIPWRECK_SUPPLY, 0.14F),
      Map.entry(BuiltInLootTables.SHIPWRECK_TREASURE, 0.1F),
      Map.entry(BuiltInLootTables.BURIED_TREASURE, 0.1F),
      Map.entry(BuiltInLootTables.JUNGLE_TEMPLE, 0.08F),
      Map.entry(BuiltInLootTables.WOODLAND_MANSION, 0.08F)
   );

   private CuisineCodexHooks() {
   }

   public static void register() {
      LootTableEvents.MODIFY.register((Modify)(key, tableBuilder, source, registries) -> {
         Float chance = CUISINE_TABLES.get(key);
         if (chance != null && source.isBuiltin()) {
            CodexLoot.addTatteredPagePool(tableBuilder, chance, "cuisine", "");
         }
      });
   }
}
