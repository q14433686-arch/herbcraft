package com.herbcraft.alchemy.knowledge;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.api.HerbUsageAPI;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Pushes "可萃精华" usage hints into core's HerbUsageAPI for every raw material
 * that has an essence extraction recipe. Mapping is derived from essence ids:
 * essence_<id> is crafted from 8x minecraft:<id>, with the documented variant
 * exceptions (tulip colors share one essence, fern/large fern share one).
 */
public final class AlchemyUsageHints {
   private static final String KEY = "usage.herbcraft.essence";
   private static final Map<String, List<String>> VARIANT_SOURCES = Map.of(
      "tulip", List.of("minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip"),
      "fern", List.of("minecraft:fern", "minecraft:large_fern")
   );

   private AlchemyUsageHints() {
   }

   public static void register() {
      for (Item essenceItem : AlchemyRegistries.essenceItems()) {
         AlchemyRegistries.essenceInfo(essenceItem).ifPresent(info -> {
            List<String> sources = VARIANT_SOURCES.getOrDefault(info.id(), List.of("minecraft:" + info.id()));
            for (String source : sources) {
               registerSource(source);
            }
         });
      }
   }

   private static void registerSource(String idString) {
      Identifier id = Identifier.parse(idString);
      if (BuiltInRegistries.ITEM.containsKey(id)) {
         HerbUsageAPI.registerUsage((Item)BuiltInRegistries.ITEM.getValue(id), KEY);
      }
   }
}
