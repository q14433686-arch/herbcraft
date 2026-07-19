package com.herbcraft.alchemy.potion;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.nature.NatureProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class AdvancedPotionStackInitializer {
   private static final int MAX_DURATION_TICKS = 9600;

   private AdvancedPotionStackInitializer() {
   }

   public static boolean initializeIfNeeded(ItemStack stack, RandomSource random) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      if (contents != null && isAdvanced(contents)) {
         Optional<String> path = advancedPotionPath(contents);
         path.ifPresent(p -> stack.set(AlchemyPotionComponents.POTION_SOURCE, inferSourceId(p)));
         AlchemyPotionComponents.PotionQuality quality = (AlchemyPotionComponents.PotionQuality)stack.get(AlchemyPotionComponents.POTION_QUALITY);
         if (quality == null) {
            quality = AlchemyPotionComponents.PotionQuality.roll(random, path.map(AdvancedPotionStackInitializer::exceptionalBonus).orElse(0.0F));
            stack.set(AlchemyPotionComponents.POTION_QUALITY, quality);
         }

         Boolean initialized = (Boolean)stack.get(AlchemyPotionComponents.ADVANCED_POTION_INITIALIZED);
         if (Boolean.TRUE.equals(initialized)) {
            return false;
         } else {
            applyBonusIfNeeded(stack, contents, random);
            contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
               applyQuality(stack, contents, quality);
            }

            stack.set(AlchemyPotionComponents.ADVANCED_POTION_INITIALIZED, Boolean.TRUE);
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isAdvanced(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      return contents != null && isAdvanced(contents);
   }

   public static boolean isAdvanced(PotionContents contents) {
      return advancedPotionPath(contents).isPresent();
   }

   public static Optional<String> advancedPotionPath(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      return contents == null ? Optional.empty() : advancedPotionPath(contents);
   }

   public static Optional<String> advancedPotionPath(PotionContents contents) {
      Optional<String> potionPath = contents.potion()
         .flatMap(Holder::unwrapKey)
         .<Identifier>map(ResourceKey::identifier)
         .filter(id -> "herbcraft".equals(id.getNamespace()))
         .map(Identifier::getPath);
      return potionPath.isPresent()
         ? potionPath.filter(AdvancedPotionStackInitializer::isAdvancedPotionPath)
         : contents.customName().filter(AdvancedPotionStackInitializer::isAdvancedPotionPath);
   }

   public static boolean isAdvancedPotionPath(String path) {
      return path.endsWith("_clarified")
         || path.endsWith("_murky")
         || path.equals("undead_venom")
         || path.equals("cardiac_toxin")
         || path.equals("wither_venom_iii");
   }

   private static String inferSourceId(String path) {
      String source = path;
      for (String suffix : new String[]{"_clarified", "_murky", "_long", "_strong"}) {
         if (source.endsWith(suffix)) {
            source = source.substring(0, source.length() - suffix.length());
         }
      }
      if ("undead_venom".equals(source)) {
         source = "flowering_azalea";
      } else if ("cardiac_toxin".equals(source)) {
         source = "lily_of_the_valley";
      } else if ("wither_venom_iii".equals(source)) {
         source = "wither_rose";
      }
      return "minecraft:" + source;
   }

   private static float exceptionalBonus(String path) {
      String sourceId = inferSourceId(path);
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(Identifier.parse(sourceId));
      if (path.endsWith("_murky") && (nature.traits().contains("fungal") || nature.traits().contains("toxic"))) {
         return 0.10F;
      }
      if (path.endsWith("_clarified") && (nature.traits().contains("nourishing") || nature.traits().contains("clearing"))) {
         return 0.10F;
      }
      return 0.0F;
   }

   public static String highTierTypeKey(String path) {
      if (path.endsWith("_clarified")) {
         return "clarified";
      } else if (!path.endsWith("_murky") && !path.equals("undead_venom") && !path.equals("cardiac_toxin") && !path.equals("wither_venom_iii")) {
         return "advanced";
      } else {
         return path.equals("wither_venom_iii") ? "t3" : "murky";
      }
   }

   private static void applyBonusIfNeeded(ItemStack stack, PotionContents contents, RandomSource random) {
      if (stack.get(AlchemyPotionComponents.POTION_BONUS) == null && !(random.nextFloat() >= 0.5F)) {
         String path = advancedPotionPath(contents).orElse("");
         List<AdvancedPotionStackInitializer.BonusSpec> pool = bonusPool(path, contents);
         if (!pool.isEmpty()) {
            AdvancedPotionStackInitializer.BonusSpec spec = pool.get(random.nextInt(pool.size()));
            Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)spec.effect().value());
            if (effectId != null) {
               PotionBonusComponent bonus = new PotionBonusComponent(effectId, spec.durationTicks(), spec.amplifier());
               stack.set(AlchemyPotionComponents.POTION_BONUS, bonus);
               stack.set(DataComponents.POTION_CONTENTS, contents.withEffectAdded(new MobEffectInstance(spec.effect(), spec.durationTicks(), spec.amplifier())));
            }
         }
      }
   }

   private static List<AdvancedPotionStackInitializer.BonusSpec> bonusPool(String path, PotionContents contents) {
      boolean harmful = "murky".equals(highTierTypeKey(path)) || "t3".equals(highTierTypeKey(path));
      if (harmful) {
         if (hasAny(contents, MobEffects.POISON, MobEffects.WITHER)) {
            return List.of(debuff(MobEffects.SLOWNESS), debuff(MobEffects.WEAKNESS), effect(MobEffects.DARKNESS, 10, 0));
         } else if (hasAny(contents, MobEffects.BLINDNESS, MobEffects.DARKNESS, MobEffects.NAUSEA)) {
            return List.of(effect(MobEffects.NAUSEA, 10, 0), effect(MobEffects.GLOWING, 10, 0), debuff(MobEffects.SLOWNESS));
         } else if (hasAny(contents, MobEffects.HUNGER)) {
            return List.of(debuff(MobEffects.SLOWNESS), debuff(MobEffects.WEAKNESS), effect(MobEffects.NAUSEA, 10, 0));
         } else {
            return hasAny(contents, MobEffects.WEAKNESS, MobEffects.SLOWNESS, MobEffects.MINING_FATIGUE)
               ? List.of(debuff(MobEffects.MINING_FATIGUE), debuff(MobEffects.SLOWNESS), debuff(MobEffects.WEAKNESS))
               : List.of(effect(MobEffects.NAUSEA, 10, 0), debuff(MobEffects.SLOWNESS), debuff(MobEffects.WEAKNESS));
         }
      } else if (hasAny(contents, MobEffects.REGENERATION, MobEffects.INSTANT_HEALTH)) {
         return List.of(effect(MobEffects.SATURATION, 5, 0), effect(MobEffects.ABSORPTION, 10, 0), effect(MobEffects.RESISTANCE, 10, 0));
      } else if (hasAny(contents, MobEffects.SPEED, MobEffects.JUMP_BOOST, MobEffects.HASTE)) {
         return List.of(effect(MobEffects.JUMP_BOOST, 10, 0), effect(MobEffects.SPEED, 10, 0), effect(MobEffects.HASTE, 10, 0));
      } else if (hasAny(contents, MobEffects.NIGHT_VISION, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING, MobEffects.SLOW_FALLING)) {
         return List.of(effect(MobEffects.NIGHT_VISION, 20, 0), effect(MobEffects.SLOW_FALLING, 10, 0), effect(MobEffects.WATER_BREATHING, 15, 0));
      } else {
         return hasAny(contents, MobEffects.STRENGTH, MobEffects.RESISTANCE, MobEffects.ABSORPTION)
            ? List.of(effect(MobEffects.STRENGTH, 10, 0), effect(MobEffects.RESISTANCE, 10, 0), effect(MobEffects.ABSORPTION, 10, 0))
            : List.of(effect(MobEffects.SATURATION, 5, 0), effect(MobEffects.SPEED, 10, 0), effect(MobEffects.RESISTANCE, 10, 0));
      }
   }

   @SafeVarargs
   private static boolean hasAny(PotionContents contents, Holder<MobEffect>... effects) {
      for (MobEffectInstance instance : contents.getAllEffects()) {
         for (Holder<MobEffect> effect : effects) {
            if (instance.is(effect)) {
               return true;
            }
         }
      }

      return false;
   }

   private static AdvancedPotionStackInitializer.BonusSpec debuff(Holder<MobEffect> effect) {
      return effect(effect, 10, 0);
   }

   private static AdvancedPotionStackInitializer.BonusSpec effect(Holder<MobEffect> effect, int durationSeconds, int amplifier) {
      return new AdvancedPotionStackInitializer.BonusSpec(effect, durationSeconds * 20, amplifier);
   }

   private static void applyQuality(ItemStack stack, PotionContents contents, AlchemyPotionComponents.PotionQuality quality) {
      float multiplier = quality.durationMultiplier();
      if (!(multiplier <= 1.0F) && !canUseVanillaDurationScale(contents, multiplier)) {
         List<MobEffectInstance> scaled = new ArrayList<>();

         for (MobEffectInstance effect : contents.getAllEffects()) {
            scaled.add(scaleAndCap(effect, multiplier));
         }

         Optional<String> customName = contents.customName()
            .or(() -> contents.potion().<ResourceKey>flatMap(Holder::unwrapKey).map(key -> key.identifier().getPath()));
         PotionContents rewritten = new PotionContents(Optional.empty(), contents.customColor(), List.copyOf(scaled), customName);
         stack.set(DataComponents.POTION_CONTENTS, rewritten);
         stack.set(DataComponents.POTION_DURATION_SCALE, 1.0F);
      } else {
         stack.set(DataComponents.POTION_DURATION_SCALE, multiplier);
      }
   }

   private static boolean canUseVanillaDurationScale(PotionContents contents, float multiplier) {
      for (MobEffectInstance effect : contents.getAllEffects()) {
         Holder<MobEffect> effectType = effect.getEffect();
         if (!((MobEffect)effectType.value()).isInstantaneous() && !effect.isInfiniteDuration() && Math.round(effect.getDuration() * multiplier) > 9600) {
            return false;
         }
      }

      return true;
   }

   private static MobEffectInstance scaleAndCap(MobEffectInstance effect, float multiplier) {
      Holder<MobEffect> effectType = effect.getEffect();
      if (!((MobEffect)effectType.value()).isInstantaneous() && !effect.isInfiniteDuration()) {
         int duration = Math.min(9600, Math.max(1, Math.round(effect.getDuration() * multiplier)));
         return new MobEffectInstance(effectType, duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
      } else {
         return new MobEffectInstance(effect);
      }
   }

   private record BonusSpec(Holder<MobEffect> effect, int durationTicks, int amplifier) {
   }
}
