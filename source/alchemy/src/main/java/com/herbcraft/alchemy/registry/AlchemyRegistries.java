package com.herbcraft.alchemy.registry;

import com.herbcraft.alchemy.coating.CoatingItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder.BuildCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public final class AlchemyRegistries {
   public static final String NAMESPACE = "herbcraft";
   private static final int NORMAL_POSITIVE_MIN_SECONDS = 15;
   private static final int NORMAL_NEGATIVE_MIN_SECONDS = 10;
   private static final int ADVANCED_MIN_SECONDS = 30;
   private static final int MAX_DURATION_SECONDS = 480;
   private static final int MAX_NORMAL_AMPLIFIER = 1;
   private static final List<AlchemyRegistries.EssenceDefinition> ESSENCES = new ArrayList<>();
   private static final List<AlchemyRegistries.ModifierMix> MODIFIER_MIXES = new ArrayList<>();
   private static final Map<Item, AlchemyRegistries.EssenceInfo> INFO_BY_ITEM = new LinkedHashMap<>();
   private static final Map<String, AlchemyRegistries.EssenceInfo> INFO_BY_ID = new LinkedHashMap<>();
   public static final Item ESSENCE_DANDELION = essence("dandelion", "dandelion", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 5, true));
   public static final Item ESSENCE_GOLDEN_DANDELION = essence(
      "golden_dandelion",
      "golden_dandelion",
      new AlchemyRegistries.EffectSpec(MobEffects.ABSORPTION, 0, 240, true),
      new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 1, 5, true)
   );
   public static final Item ESSENCE_POPPY = essence(
      "poppy",
      "poppy",
      new AlchemyRegistries.EffectSpec(MobEffects.RESISTANCE, 0, 95, true),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 65, false),
      new AlchemyRegistries.EffectSpec(MobEffects.SLOWNESS, 0, 35, false)
   );
   public static final Item ESSENCE_BLUE_ORCHID = essence(
      "blue_orchid",
      "blue_orchid",
      new AlchemyRegistries.EffectSpec(MobEffects.SATURATION, 0, 5, true),
      new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 25, true)
   );
   public static final Item ESSENCE_AZURE_BLUET = essence("azure_bluet", "azure_bluet", new AlchemyRegistries.EffectSpec(MobEffects.BLINDNESS, 0, 25, false));
   public static final Item ESSENCE_TULIP = essence("tulip", "tulip", new AlchemyRegistries.EffectSpec(MobEffects.WEAKNESS, 0, 25, false));
   public static final Item ESSENCE_OXEYE_DAISY = essence("oxeye_daisy", "oxeye_daisy", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 20, true));
   public static final Item ESSENCE_CORNFLOWER = essence("cornflower", "cornflower", new AlchemyRegistries.EffectSpec(MobEffects.JUMP_BOOST, 0, 25, true));
   public static final Item ESSENCE_LILY_OF_THE_VALLEY = essence(
      "lily_of_the_valley",
      "lily_of_the_valley",
      new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 25, true),
      new AlchemyRegistries.EffectSpec(MobEffects.POISON, 1, 80, false),
      new AlchemyRegistries.EffectSpec(MobEffects.WEAKNESS, 0, 15, false)
   );
   public static final Item ESSENCE_WITHER_ROSE = essence(
      "wither_rose",
      "wither_rose",
      new AlchemyRegistries.EffectSpec(MobEffects.WITHER, 1, 65, false),
      new AlchemyRegistries.EffectSpec(MobEffects.HUNGER, 1, 120, false)
   );
   public static final Item ESSENCE_TORCHFLOWER = essence(
      "torchflower",
      "torchflower",
      new AlchemyRegistries.EffectSpec(MobEffects.GLOWING, 0, 240, true),
      new AlchemyRegistries.EffectSpec(MobEffects.NIGHT_VISION, 0, 95, true)
   );
   public static final Item ESSENCE_CLOSED_EYEBLOSSOM = essence(
      "closed_eyeblossom",
      "closed_eyeblossom",
      new AlchemyRegistries.EffectSpec(MobEffects.DARKNESS, 0, 80, false),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 25, false),
      new AlchemyRegistries.EffectSpec(MobEffects.RESISTANCE, 0, 15, true)
   );
   public static final Item ESSENCE_OPEN_EYEBLOSSOM = essence(
      "open_eyeblossom",
      "open_eyeblossom",
      new AlchemyRegistries.EffectSpec(MobEffects.GLOWING, 0, 160, true),
      new AlchemyRegistries.EffectSpec(MobEffects.WITHER, 0, 10, false)
   );
   public static final Item ESSENCE_CACTUS_FLOWER = essence(
      "cactus_flower", "cactus_flower", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 10, true)
   );
   public static final Item ESSENCE_PINK_PETALS = essence("pink_petals", "pink_petals", new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 10, true));
   public static final Item ESSENCE_SUNFLOWER = essence("sunflower", "sunflower", new AlchemyRegistries.EffectSpec(MobEffects.ABSORPTION, 0, 50, true));
   public static final Item ESSENCE_LILAC = essence("lilac", "lilac", new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 65, true));
   public static final Item ESSENCE_ROSE_BUSH = essence("rose_bush", "rose_bush", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 10, true));
   public static final Item ESSENCE_PEONY = essence("peony", "peony", new AlchemyRegistries.EffectSpec(MobEffects.RESISTANCE, 0, 35, true));
   public static final Item ESSENCE_PITCHER_PLANT = essence("pitcher_plant", "pitcher_plant", new AlchemyRegistries.EffectSpec(MobEffects.HUNGER, 0, 15, false));
   public static final Item ESSENCE_FERN = essence("fern", "fern", new AlchemyRegistries.EffectSpec(MobEffects.POISON, 0, 5, false));
   public static final Item ESSENCE_SPORE_BLOSSOM = essence(
      "spore_blossom",
      "spore_blossom",
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 65, false),
      new AlchemyRegistries.EffectSpec(MobEffects.SLOW_FALLING, 0, 50, true)
   );
   public static final Item ESSENCE_COCOA_BEANS = essence(
      "cocoa_beans",
      "cocoa_beans",
      new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 15, true),
      new AlchemyRegistries.EffectSpec(MobEffects.HASTE, 0, 15, true)
   );
   public static final Item ESSENCE_OAK_LEAVES = essence("oak_leaves", "oak_leaves", new AlchemyRegistries.EffectSpec(MobEffects.RESISTANCE, 0, 10, true));
   public static final Item ESSENCE_SPRUCE_LEAVES = essence(
      "spruce_leaves", "spruce_leaves", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 10, true)
   );
   public static final Item ESSENCE_BIRCH_LEAVES = essence("birch_leaves", "birch_leaves", new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 25, true));
   public static final Item ESSENCE_ACACIA_LEAVES = essence(
      "acacia_leaves", "acacia_leaves", new AlchemyRegistries.EffectSpec(MobEffects.RESISTANCE, 0, 10, true)
   );
   public static final Item ESSENCE_MANGROVE_LEAVES = essence(
      "mangrove_leaves", "mangrove_leaves", new AlchemyRegistries.EffectSpec(MobEffects.WATER_BREATHING, 0, 25, true)
   );
   public static final Item ESSENCE_CHERRY_LEAVES = essence(
      "cherry_leaves",
      "cherry_leaves",
      new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 10, true),
      new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 10, true)
   );
   public static final Item ESSENCE_PALE_OAK_LEAVES = essence(
      "pale_oak_leaves",
      "pale_oak_leaves",
      new AlchemyRegistries.EffectSpec(MobEffects.DARKNESS, 0, 10, false),
      new AlchemyRegistries.EffectSpec(MobEffects.NIGHT_VISION, 0, 25, true)
   );
   public static final Item ESSENCE_AZALEA_LEAVES = essence(
      "azalea_leaves",
      "azalea_leaves",
      new AlchemyRegistries.EffectSpec(MobEffects.POISON, 0, 65, false),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 10, false)
   );
   public static final Item ESSENCE_FLOWERING_AZALEA_LEAVES = essence(
      "flowering_azalea_leaves",
      "flowering_azalea_leaves",
      new AlchemyRegistries.EffectSpec(MobEffects.POISON, 1, 50, false),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 25, false)
   );
   public static final Item ESSENCE_CHERRY_SAPLING = essence(
      "cherry_sapling", "cherry_sapling", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 5, true)
   );
   public static final Item ESSENCE_AZALEA = essence("azalea", "azalea", new AlchemyRegistries.EffectSpec(MobEffects.POISON, 0, 20, false));
   public static final Item ESSENCE_FLOWERING_AZALEA = essence(
      "flowering_azalea",
      "flowering_azalea",
      new AlchemyRegistries.EffectSpec(MobEffects.POISON, 0, 40, false),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 10, false)
   );
   public static final Item ESSENCE_KELP = essence("kelp", "kelp", new AlchemyRegistries.EffectSpec(MobEffects.WATER_BREATHING, 0, 20, true));
   public static final Item ESSENCE_SEA_PICKLE = essence(
      "sea_pickle",
      "sea_pickle",
      new AlchemyRegistries.EffectSpec(MobEffects.GLOWING, 0, 240, true),
      new AlchemyRegistries.EffectSpec(MobEffects.WATER_BREATHING, 0, 30, true)
   );
   public static final Item ESSENCE_CRIMSON_FUNGUS = essence(
      "crimson_fungus",
      "crimson_fungus",
      new AlchemyRegistries.EffectSpec(MobEffects.STRENGTH, 0, 80, true),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 15, false)
   );
   public static final Item ESSENCE_WARPED_FUNGUS = essence(
      "warped_fungus",
      "warped_fungus",
      new AlchemyRegistries.EffectSpec(MobEffects.SPEED, 0, 80, true),
      new AlchemyRegistries.EffectSpec(MobEffects.FIRE_RESISTANCE, 0, 120, true),
      new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 20, false)
   );
   public static final Item ESSENCE_BROWN_MUSHROOM = essence(
      "brown_mushroom", "brown_mushroom", new AlchemyRegistries.EffectSpec(MobEffects.REGENERATION, 0, 5, true)
   );
   private static Reference<Potion> FLOWERING_AZALEA_MURKY;
   private static Reference<Potion> LILY_OF_THE_VALLEY_MURKY;
   private static Reference<Potion> WITHER_ROSE_BASE;
   private static Reference<Potion> UNDEAD_VENOM = registerPotion(
      "undead_venom",
      new AlchemyRegistries.EffectSpec(MobEffects.WITHER, 1, 50, false),
      new AlchemyRegistries.EffectSpec(MobEffects.POISON, 1, 50, false),
      new AlchemyRegistries.EffectSpec(MobEffects.HUNGER, 1, 60, false)
   );
   private static Reference<Potion> CARDIAC_TOXIN = registerPotion(
      "cardiac_toxin",
      new AlchemyRegistries.EffectSpec(MobEffects.POISON, 1, 120, false),
      new AlchemyRegistries.EffectSpec(MobEffects.WEAKNESS, 0, 22, false),
      new AlchemyRegistries.EffectSpec(MobEffects.DARKNESS, 0, 40, false)
   );
   private static Reference<Potion> WITHER_VENOM_III;

   public static final Reference<Potion> WITCH_TULIP_MURKY = registerPotion("witch_tulip_murky", new AlchemyRegistries.EffectSpec(MobEffects.WEAKNESS, 0, 20, false));
   public static final Reference<Potion> WITCH_AZURE_BLUET_MURKY = registerPotion("witch_azure_bluet_murky", new AlchemyRegistries.EffectSpec(MobEffects.BLINDNESS, 0, 4, false), new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 5, false));
   public static final Reference<Potion> WITCH_PITCHER_PLANT_MURKY = registerPotion("witch_pitcher_plant_murky", new AlchemyRegistries.EffectSpec(MobEffects.HUNGER, 0, 20, false));
   public static final Reference<Potion> WITCH_AZALEA_MURKY = registerPotion("witch_azalea_murky", new AlchemyRegistries.EffectSpec(MobEffects.POISON, 0, 12, false), new AlchemyRegistries.EffectSpec(MobEffects.NAUSEA, 0, 4, false));
   public static final Reference<Potion> WITCH_CLOSED_EYEBLOSSOM_MURKY = registerPotion("witch_closed_eyeblossom_murky", new AlchemyRegistries.EffectSpec(MobEffects.DARKNESS, 0, 5, false));
   public static final CreativeModeTab ALCHEMY_TAB = (CreativeModeTab)Registry.register(
      BuiltInRegistries.CREATIVE_MODE_TAB,
      Identifier.fromNamespaceAndPath("herbcraft", "alchemy"),
      CreativeModeTab.builder(Row.TOP, 0)
         .title(Component.translatable("itemGroup.herbcraft.alchemy"))
         .icon(() -> new ItemStack(ESSENCE_DANDELION))
         .displayItems((parameters, output) -> {
            ESSENCES.forEach(definition -> output.accept(definition.item));
            output.accept(CoatingItems.BLANK_ADHESIVE);
            output.accept(CoatingItems.AMETHYST_ADHESIVE);
            output.accept(CoatingItems.ECHO_ADHESIVE);
         })
         .build()
   );

   private AlchemyRegistries() {
   }

   public static List<Item> essenceItems() {
      buildInfoMaps();
      return List.copyOf(INFO_BY_ITEM.keySet());
   }

   public static Optional<AlchemyRegistries.EssenceInfo> essenceInfo(Item item) {
      buildInfoMaps();
      return Optional.ofNullable(INFO_BY_ITEM.get(item));
   }

   public static Optional<AlchemyRegistries.EssenceInfo> essenceInfoById(String id) {
      buildInfoMaps();
      return Optional.ofNullable(INFO_BY_ID.get(id));
   }

   private static synchronized void buildInfoMaps() {
      if (INFO_BY_ITEM.isEmpty()) {
         for (AlchemyRegistries.EssenceDefinition definition : ESSENCES) {
            List<AlchemyRegistries.EssenceEffect> effects = definition.effects
               .stream()
               .map(e -> new AlchemyRegistries.EssenceEffect(e.effect(), e.amplifier(), e.durationSeconds(), e.positive()))
               .toList();
            AlchemyRegistries.EssenceInfo info = new AlchemyRegistries.EssenceInfo(definition.id, definition.item, effects);
            INFO_BY_ITEM.put(definition.item, info);
            INFO_BY_ID.put(definition.id, info);
         }
      }
   }

   public static void initialize() {
      FabricPotionBrewingBuilder.BUILD.register((BuildCallback)builder -> {
         for (AlchemyRegistries.EssenceDefinition definition : ESSENCES) {
            builder.addMix(Potions.AWKWARD, definition.item, definition.basePotion);
         }

         for (AlchemyRegistries.ModifierMix mix : MODIFIER_MIXES) {
            builder.addMix(mix.from, mix.ingredient, mix.to);
         }

         builder.addMix(FLOWERING_AZALEA_MURKY, ESSENCE_WITHER_ROSE, UNDEAD_VENOM);
         builder.addMix(LILY_OF_THE_VALLEY_MURKY, ESSENCE_CLOSED_EYEBLOSSOM, CARDIAC_TOXIN);
      });
   }

   private static Item essence(String id, String potionName, AlchemyRegistries.EffectSpec... effects) {
      String itemPath = "essence_" + id;
      Item item = registerItem(itemPath, Item::new, new Properties().stacksTo(64));
      Reference<Potion> basePotion = registerPotion(potionName, stabilizedNormal(effects));
      AlchemyRegistries.EssenceDefinition definition = new AlchemyRegistries.EssenceDefinition(id, item, basePotion, List.of(effects));
      ESSENCES.add(definition);
      registerModifiers(definition);
      return item;
   }

   private static void registerModifiers(AlchemyRegistries.EssenceDefinition definition) {
      List<AlchemyRegistries.EffectSpec> normalEffects = List.of(stabilizedNormal(definition.effects.toArray(AlchemyRegistries.EffectSpec[]::new)));
      List<AlchemyRegistries.EffectSpec> positive = normalEffects.stream().filter(AlchemyRegistries.EffectSpec::positive).toList();
      List<AlchemyRegistries.EffectSpec> negative = normalEffects.stream().filter(e -> !e.positive()).toList();
      if (!positive.isEmpty()) {
         Reference<Potion> clarified = registerPotion(definition.id + "_clarified", clarifiedEffects(positive));
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.HONEYCOMB, clarified));
      }

      if (!negative.isEmpty()) {
         Reference<Potion> murky = registerPotion(definition.id + "_murky", murkyEffects(negative));
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.FERMENTED_SPIDER_EYE, murky));
         if (definition.id.equals("flowering_azalea")) {
            FLOWERING_AZALEA_MURKY = murky;
         }

         if (definition.id.equals("lily_of_the_valley")) {
            LILY_OF_THE_VALLEY_MURKY = murky;
         }
      }

      Reference<Potion> extended = registerPotion(definition.id + "_long", multiplyDuration(normalEffects, 2.0F));
      MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.REDSTONE, extended));
      if (definition.id.equals("wither_rose")) {
         WITHER_ROSE_BASE = definition.basePotion;
         WITHER_VENOM_III = registerPotion(
            "wither_venom_iii",
            new AlchemyRegistries.EffectSpec(MobEffects.WITHER, 2, 30, false),
            new AlchemyRegistries.EffectSpec(MobEffects.HUNGER, 2, 60, false)
         );
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.GLOWSTONE_DUST, WITHER_VENOM_III));
      } else {
         Reference<Potion> strong = registerPotion(definition.id + "_strong", strengthen(normalEffects));
         MODIFIER_MIXES.add(new AlchemyRegistries.ModifierMix(definition.basePotion, Items.GLOWSTONE_DUST, strong));
      }
   }

   private static AlchemyRegistries.EffectSpec[] stabilizedNormal(AlchemyRegistries.EffectSpec... effects) {
      return List.of(effects).stream().map(AlchemyRegistries::stabilizeNormal).toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static AlchemyRegistries.EffectSpec stabilizeNormal(AlchemyRegistries.EffectSpec effect) {
      int amplifier = Math.min(effect.amplifier, 1);
      if (((MobEffect)effect.effect.value()).isInstantaneous()) {
         return new AlchemyRegistries.EffectSpec(effect.effect, amplifier, effect.durationSeconds, effect.positive);
      } else {
         int minSeconds = effect.positive ? 15 : 10;
         return new AlchemyRegistries.EffectSpec(effect.effect, amplifier, clampSeconds(Math.max(effect.durationSeconds, minSeconds)), effect.positive);
      }
   }

   private static AlchemyRegistries.EffectSpec[] clarifiedEffects(List<AlchemyRegistries.EffectSpec> effects) {
      return effects.stream()
         .map(e -> new AlchemyRegistries.EffectSpec(e.effect, Math.min(e.amplifier, 1), advancedDuration(e, 3.0F), true))
         .toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static AlchemyRegistries.EffectSpec[] murkyEffects(List<AlchemyRegistries.EffectSpec> effects) {
      boolean singleNegative = effects.size() == 1;
      return effects.stream().map(e -> {
         int amplifier = Math.min(e.amplifier, 1);
         if (singleNegative && amplifier == 0) {
            amplifier = 1;
         }

         return new AlchemyRegistries.EffectSpec(e.effect, amplifier, advancedDuration(e, 3.0F), false);
      }).toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static int advancedDuration(AlchemyRegistries.EffectSpec effect, float multiplier) {
      return ((MobEffect)effect.effect.value()).isInstantaneous()
         ? effect.durationSeconds
         : clampSeconds(Math.max(30, Math.round(effect.durationSeconds * multiplier)));
   }

   private static AlchemyRegistries.EffectSpec[] multiplyDuration(List<AlchemyRegistries.EffectSpec> effects, float multiplier) {
      return effects.stream()
         .map(e -> new AlchemyRegistries.EffectSpec(e.effect, e.amplifier, scaleDurationSeconds(e, multiplier), e.positive))
         .toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static AlchemyRegistries.EffectSpec[] strengthen(List<AlchemyRegistries.EffectSpec> effects) {
      return effects.stream()
         .map(
            e -> new AlchemyRegistries.EffectSpec(e.effect, e.positive ? Math.min(e.amplifier + 1, 1) : e.amplifier, scaleDurationSeconds(e, 0.5F), e.positive)
         )
         .toArray(AlchemyRegistries.EffectSpec[]::new);
   }

   private static int scaleDurationSeconds(AlchemyRegistries.EffectSpec effect, float multiplier) {
      return ((MobEffect)effect.effect.value()).isInstantaneous() ? effect.durationSeconds : clampSeconds(Math.round(effect.durationSeconds * multiplier));
   }

   private static int clampSeconds(int seconds) {
      return Math.max(1, Math.min(480, seconds));
   }

   private static <T extends Item> T registerItem(String name, Function<Properties, T> factory, Properties properties) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      T item = (T)factory.apply(properties.setId(key));
      Registry.register(BuiltInRegistries.ITEM, key, item);
      return item;
   }

   private static Reference<Potion> registerPotion(String name, AlchemyRegistries.EffectSpec... effects) {
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", name);
      ResourceKey<Potion> key = ResourceKey.create(Registries.POTION, id);
      MobEffectInstance[] instances = new MobEffectInstance[effects.length];

      for (int i = 0; i < effects.length; i++) {
         AlchemyRegistries.EffectSpec effect = effects[i];
         instances[i] = new MobEffectInstance(effect.effect, effect.durationSeconds * 20, effect.amplifier);
      }

      return Registry.registerForHolder(BuiltInRegistries.POTION, key, new Potion(name, instances));
   }

   private record EffectSpec(Holder<MobEffect> effect, int amplifier, int durationSeconds, boolean positive) {
   }

   private record EssenceDefinition(String id, Item item, Reference<Potion> basePotion, List<AlchemyRegistries.EffectSpec> effects) {
   }

   public record EssenceEffect(Holder<MobEffect> effect, int amplifier, int durationSeconds, boolean positive) {
   }

   public record EssenceInfo(String id, Item item, List<AlchemyRegistries.EssenceEffect> effects) {
   }

   private record ModifierMix(Holder<Potion> from, Item ingredient, Holder<Potion> to) {
   }
}
