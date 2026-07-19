package com.herbcraft.logic;

import com.herbcraft.HerbcraftPlayerData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class SpecialCounters {
   private static final int SECOND = 20;
   private static final int WINDOW = 1200;

   private SpecialCounters() {
   }

   public static void apply(Player player, Item item, long gameTime) {
      HerbcraftPlayerData data = (HerbcraftPlayerData)player;
      if (item == Items.POPPY) {
         int count = data.herbcraft$incrementWindowCounter("poppy", gameTime, 1200);
         player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 240, 0));
         if (count >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
         }
      }

      if (item == Items.TORCHFLOWER && data.herbcraft$incrementWindowCounter("torchflower", gameTime, 1200) >= 3) {
         player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
      }

      if ((item == Items.SUGAR || item == Items.HONEY_BLOCK)
         && data.herbcraft$incrementWindowCounter("sugar", gameTime, 1200) >= (item == Items.HONEY_BLOCK ? 2 : 4)
         && EffectResolver.roll(player, 0.5F)) {
         player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
      }

      if (item == Items.SLIME_BALL && data.herbcraft$incrementWindowCounter("slime", gameTime, 1200) >= 3) {
         player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0));
      }
   }
}
