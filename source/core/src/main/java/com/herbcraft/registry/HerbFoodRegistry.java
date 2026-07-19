package com.herbcraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import com.herbcraft.api.HerbDefinition;
import com.herbcraft.api.HerbEffectEntry;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents.ModifyContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HerbFoodRegistry {
   private static final IdentityHashMap<Item, HerbEntry> ENTRIES = new IdentityHashMap<>();

   private HerbFoodRegistry() {
   }

   public static void loadFromBundledJson() {
      ENTRIES.clear();

      try {
         try (InputStream input = HerbFoodRegistry.class.getClassLoader().getResourceAsStream("data/herbcraft/herbs.json")) {
            if (input == null) {
               throw new IllegalStateException("Missing data/herbcraft/herbs.json");
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();

            for (JsonElement element : root.getAsJsonArray("herbs")) {
               loadEntry(element.getAsJsonObject());
            }

            Herbcraft.LOGGER.info("Loaded {} herb definitions from herbs.json", ENTRIES.size());
         }
      } catch (Exception var61) {
         throw new RuntimeException("Failed to load Herbcraft herbs.json", var61);
      }
   }

   private static void loadEntry(JsonObject json) {
      Identifier itemId = Identifier.parse(json.get("item").getAsString());
      Item item = (Item)BuiltInRegistries.ITEM.getValue(itemId);
      if (item == null) {
         Herbcraft.LOGGER.warn("Skipping unknown item in herbs.json: {}", itemId);
      } else {
         HerbEntry entry = new HerbEntry(
            item,
            json.get("nutrition").getAsInt(),
            json.get("saturation_modifier").getAsFloat(),
            json.get("consume_seconds").getAsFloat(),
            json.get("medicinal").getAsBoolean(),
            json.get("stack_group").getAsBoolean()
         );
         if (json.has("remove_effects")) {
            for (JsonElement effect : json.getAsJsonArray("remove_effects")) {
               resolveEffect(effect.getAsString(), itemId).ifBound(entry.removeEffects::add);
            }
         }

         if (json.has("effects")) {
            for (JsonElement effectElement : json.getAsJsonArray("effects")) {
               JsonObject effectJson = effectElement.getAsJsonObject();
               resolveEffect(effectJson.get("effect").getAsString(), itemId)
                  .ifBound(
                     effect -> {
                        int durationTicks = effectJson.has("duration_ticks")
                           ? effectJson.get("duration_ticks").getAsInt()
                           : effectJson.get("duration_seconds").getAsInt() * 20;
                        entry.effects
                           .add(
                              new HerbEntry.Effect(
                                 effect,
                                 effectJson.get("chance").getAsFloat(),
                                 effectJson.get("amplifier").getAsInt(),
                                 durationTicks,
                                 effectJson.get("positive").getAsBoolean()
                              )
                           );
                     }
                  );
            }
         }

         if (json.has("damage_events")) {
            for (JsonElement event : json.getAsJsonArray("damage_events")) {
               JsonObject eventJson = event.getAsJsonObject();
               entry.damageEvents.add(new HerbEntry.DamageEvent(eventJson.get("chance").getAsFloat(), eventJson.get("amount").getAsFloat()));
            }
         }

         if (json.has("heal_events")) {
            for (JsonElement event : json.getAsJsonArray("heal_events")) {
               JsonObject eventJson = event.getAsJsonObject();
               entry.healEvents.add(new HerbEntry.HealEvent(eventJson.get("chance").getAsFloat(), eventJson.get("amount").getAsFloat()));
            }
         }

         if (json.has("flags")) {
            for (JsonElement flag : json.getAsJsonArray("flags")) {
               String var18 = flag.getAsString();
               switch (var18) {
                  case "clearPositiveEffects":
                     entry.clearPositiveEffects = true;
                     break;
                  case "extinguish":
                     entry.extinguish = true;
                     break;
                  case "bucketRemainder":
                     entry.bucketRemainder = true;
                     break;
                  case "drink":
                     entry.drink = true;
                     break;
                  case "shortUseProjectile":
                     entry.shortUseProjectile = true;
                     break;
                  default:
                     Herbcraft.LOGGER.warn("Unknown Herbcraft flag '{}' on {}", flag.getAsString(), itemId);
               }
            }
         }

         if (json.has("counter")) {
            entry.counter = json.get("counter").getAsString();
         }

         ENTRIES.put(item, entry);
      }
   }

   private static HerbFoodRegistry.EffectLookup resolveEffect(String effectId, Identifier sourceItem) {
      Identifier id = Identifier.parse(effectId);
      return new HerbFoodRegistry.EffectLookup((Reference<MobEffect>)BuiltInRegistries.MOB_EFFECT.get(id).orElse(null), id, sourceItem);
   }

   public static void registerDefaultComponents(ModifyContext context) {
      ENTRIES.values().forEach(entry -> context.modify(entry.item, components -> {
         components.set(DataComponents.FOOD, entry.foodProperties());
         components.set(DataComponents.CONSUMABLE, entry.consumable());
      }));
   }

   public static HerbEntry get(Item item) {
      return ENTRIES.get(item);
   }

   public static HerbEntry get(ItemStack stack) {
      return get(stack.getItem());
   }

   public static Map<Item, HerbDefinition> getHerbDefinitions() {
      IdentityHashMap<Item, HerbDefinition> definitions = new IdentityHashMap<>();
      ENTRIES.forEach((item, entry) -> definitions.put(item, toDefinition(entry)));
      return Collections.unmodifiableMap(definitions);
   }

   private static HerbDefinition toDefinition(HerbEntry entry) {
      List<HerbEffectEntry> effects = entry.effects
         .stream()
         .map(effect -> new HerbEffectEntry(effect.effect(), effect.amplifier(), effect.durationTicks(), effect.chance(), !effect.positive()))
         .toList();
      return new HerbDefinition(entry.item, entry.nutrition, entry.saturation, entry.consumeSeconds, entry.medicinal, entry.stackGroup, effects);
   }

   private record EffectLookup(Reference<MobEffect> holder, Identifier id, Identifier sourceItem) {
      private void ifBound(Consumer<Holder<MobEffect>> consumer) {
         if (this.holder == null) {
            Herbcraft.LOGGER.warn("Skipping unknown effect {} referenced by {}", this.id, this.sourceItem);
         } else {
            consumer.accept(this.holder);
         }
      }
   }
}
