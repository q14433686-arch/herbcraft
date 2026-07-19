package com.herbcraft.alchemy.coating;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CoatingSystem {
   public static final long EXPIRY_TICKS = 12000L;
   public static final long REFINED_EXPIRY_TICKS = 14400L;
   private static final int BONUS_DURATION_SECONDS = 30;
   public static final TagKey<Item> COATABLE_WEAPONS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("herbcraft", "coatable_weapons"));
   public static final Holder<MobEffect> ECHO_SIGNATURE = MobEffects.DARKNESS;
   public static final Holder<MobEffect> AMETHYST_SIGNATURE = MobEffects.REGENERATION;
   public static final int SIGNATURE_DURATION_SECONDS = 5;
   public static final int RANDOM_DURATION_SECONDS = 4;
   public static final int RANDOM_MIN_AMPLIFIER = 1;
   public static final int RANDOM_MAX_AMPLIFIER = 3;
   public static final List<Holder<MobEffect>> ECHO_RANDOM_POOL = List.of(
      MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.SLOWNESS, MobEffects.NAUSEA, MobEffects.MINING_FATIGUE, MobEffects.BLINDNESS, MobEffects.LEVITATION
   );
   public static final List<Holder<MobEffect>> AMETHYST_RANDOM_POOL = List.of(
      MobEffects.SPEED, MobEffects.RESISTANCE, MobEffects.STRENGTH, MobEffects.HASTE, MobEffects.JUMP_BOOST, MobEffects.ABSORPTION
   );

   private CoatingSystem() {
   }

   public static boolean isCoatable(ItemStack stack) {
      return !stack.isEmpty() && stack.is(COATABLE_WEAPONS);
   }

   public static long expiryTicksFor(WeaponCoating.Mode mode) {
      return mode == WeaponCoating.Mode.FULL ? 12000L : 14400L;
   }

   public static List<AlchemyRegistries.EssenceEffect> effectsFor(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      if (mode == WeaponCoating.Mode.FULL) {
         return info.effects();
      } else {
         boolean positive = mode == WeaponCoating.Mode.POSITIVE;
         List<AlchemyRegistries.EssenceEffect> kept = new ArrayList<>(info.effects().stream().filter(e -> e.positive() == positive).toList());
         Holder<MobEffect> signature = positive ? AMETHYST_SIGNATURE : ECHO_SIGNATURE;
         kept.removeIf(e -> e.effect().equals(signature));
         kept.add(new AlchemyRegistries.EssenceEffect(signature, 0, 50, positive));
         return List.copyOf(kept);
      }
   }

   public static AlchemyRegistries.EssenceEffect rollRandomEffect(WeaponCoating.Mode mode, RandomSource random) {
      boolean positive = mode == WeaponCoating.Mode.POSITIVE;
      List<Holder<MobEffect>> pool = positive ? AMETHYST_RANDOM_POOL : ECHO_RANDOM_POOL;
      Holder<MobEffect> effect = pool.get(random.nextInt(pool.size()));
      int amplifier = 1 + random.nextInt(3);
      if (effect.equals(MobEffects.LEVITATION)) {
         amplifier = Math.min(amplifier, 1);
      }

      return new AlchemyRegistries.EssenceEffect(effect, amplifier, 40, positive);
   }

   public static int usesFor(AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      int score = info.effects().stream().mapToInt(e -> e.amplifier() + 1).sum();
      int base;
      if (score <= 1) {
         base = 24;
      } else if (score == 2) {
         base = 20;
      } else if (score == 3) {
         base = 16;
      } else {
         base = 12;
      }

      return mode == WeaponCoating.Mode.FULL ? base : base * 2;
   }

   public static int hitDurationTicks(AlchemyRegistries.EssenceEffect effect) {
      int seconds = Math.round(effect.durationSeconds() * 0.1F);
      return Math.max(2, Math.min(8, seconds)) * 20;
   }

   public static void applyCoating(ItemStack weapon, AlchemyRegistries.EssenceInfo info, WeaponCoating.Mode mode) {
      WeaponCoating coating = new WeaponCoating(info.id(), mode, usesFor(info, mode), -1L);
      weapon.set(CoatingComponents.WEAPON_COATING, coating);
   }

   public static void removeCoating(ItemStack weapon) {
      weapon.remove(CoatingComponents.WEAPON_COATING);
   }
}
