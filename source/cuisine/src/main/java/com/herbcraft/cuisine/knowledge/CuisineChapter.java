package com.herbcraft.cuisine.knowledge;

import com.herbcraft.api.HerbConsumeEvents;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.api.KnowledgeAPI.Entry;
import com.herbcraft.api.KnowledgeAPI.State;
import com.herbcraft.cuisine.registry.DishRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

public final class CuisineChapter {
   public static final String CHAPTER_ID = "cuisine";

   private CuisineChapter() {
   }

   public static void register() {
      KnowledgeAPI.registerChapter("cuisine", Component.translatable("book.herbcraft_cuisine.chapter"), 100);

      for (String section : List.of("cold", "soups", "medicinal")) {
         KnowledgeAPI.registerSection("cuisine", section, Component.translatable("book.herbcraft_cuisine.section." + section));
      }

      for (DishRegistry.Dish dish : DishRegistry.dishes()) {
         KnowledgeAPI.registerEntry(
            "cuisine",
            new Entry(
               dish.id(),
               sectionOf(dish),
               Component.translatable(dish.item().getDescriptionId()),
               (player, state) -> lines(player, dish, state),
               player -> mastered(player, dish),
               player -> anyIngredientEaten(player, dish)
            )
         );
      }

      HerbConsumeEvents.register(CuisineChapter::onHerbEaten);
   }

   private static String sectionOf(DishRegistry.Dish dish) {
      if (dish.clears().isEmpty() && !dish.clearFire()) {
         return dish.rawItem() != null ? "soups" : "cold";
      } else {
         return "medicinal";
      }
   }

   private static void onHerbEaten(ServerPlayer player, Item eaten) {
      List<ResourceKey<Recipe<?>>> keys = new ArrayList<>();

      for (DishRegistry.Dish dish : DishRegistry.dishes()) {
         boolean contains = dish.ingredients().stream().anyMatch(spec -> spec.matches(eaten));
         if (contains) {
            keys.add(recipeKey(dish.rawItem() != null ? "raw_" + dish.id() : dish.id()));
         }
      }

      KnowledgeManager.awardRecipes(player, keys);
   }

   private static ResourceKey<Recipe<?>> recipeKey(String path) {
      return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("herbcraft_cuisine", path));
   }

   private static boolean mastered(ServerPlayer player, DishRegistry.Dish dish) {
      return player.getStats().getValue(Stats.ITEM_USED.get(dish.item())) > 0 || player.getStats().getValue(Stats.ITEM_CRAFTED.get(dish.item())) > 0;
   }

   private static boolean anyIngredientEaten(ServerPlayer player, DishRegistry.Dish dish) {
      for (DishRegistry.IngredientSpec spec : dish.ingredients()) {
         if (spec.item() != null && player.getStats().getValue(Stats.ITEM_USED.get(spec.item())) > 0) {
            return true;
         }
      }

      return false;
   }

   private static List<Component> lines(ServerPlayer player, DishRegistry.Dish dish, State state) {
      List<Component> lines = new ArrayList<>();
      MutableComponent ingredients = Component.empty();
      boolean first = true;

      for (DishRegistry.IngredientSpec spec : dish.ingredients()) {
         if (!first) {
            ingredients.append(Component.literal(" + "));
         }

         if (spec.tag() != null) {
            ingredients.append(Component.literal("#" + spec.tag().location().getPath()));
         } else if (state != State.MASTERED && player.getStats().getValue(Stats.ITEM_USED.get(spec.item())) <= 0) {
            ingredients.append(Component.translatable("tooltip.herbcraft.page.unknown"));
         } else {
            ingredients.append(Component.translatable(spec.item().getDescriptionId()));
         }

         first = false;
      }

      lines.add(ingredients.withStyle(ChatFormatting.DARK_GRAY));
      if (state == State.HEARD) {
         lines.add(
            Component.translatable("book.herbcraft_cuisine.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
         );
         return lines;
      } else {
         for (DishRegistry.DishEffect effect : dish.effects()) {
            MutableComponent name = Component.translatable(((MobEffect)effect.effect().value()).getDescriptionId());
            if (effect.amplifier() > 0) {
               name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + effect.amplifier())});
            }

            int seconds = effect.durationSeconds();
            MutableComponent line = Component.translatable("potion.withDuration", new Object[]{name, String.format("%d:%02d", seconds / 60, seconds % 60)});
            if (effect.chance() < 1.0F) {
               line = line.append(Component.literal(" " + Math.round(effect.chance() * 100.0F) + "%"));
            }

            boolean positive = ((MobEffect)effect.effect().value()).isBeneficial();
            lines.add(line.withStyle(positive ? ChatFormatting.DARK_BLUE : ChatFormatting.DARK_RED));
         }

         return lines;
      }
   }
}
