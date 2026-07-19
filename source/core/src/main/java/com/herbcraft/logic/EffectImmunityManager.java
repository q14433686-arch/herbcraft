package com.herbcraft.logic;

import com.herbcraft.HerbcraftPlayerData;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class EffectImmunityManager {
   private EffectImmunityManager() {
   }

   public static void grant(ServerPlayer player, Identifier effectId, int durationTicks) {
      if (durationTicks <= 0) {
         return;
      }
      long now = player.level().getGameTime();
      long expiresAt = now + durationTicks;
      Map<String, Long> immunities = ((HerbcraftPlayerData)player).herbcraft$effectImmunities();
      cleanup(immunities, now);
      immunities.merge(effectId.toString(), expiresAt, Math::max);
   }

   public static boolean isImmune(ServerPlayer player, Identifier effectId) {
      long now = player.level().getGameTime();
      Map<String, Long> immunities = ((HerbcraftPlayerData)player).herbcraft$effectImmunities();
      Long expiresAt = immunities.get(effectId.toString());
      if (expiresAt == null) {
         return false;
      }
      if (expiresAt <= now) {
         immunities.remove(effectId.toString());
         return false;
      }
      return true;
   }

   public static void cleanup(Map<String, Long> immunities, long now) {
      Iterator<Map.Entry<String, Long>> iterator = immunities.entrySet().iterator();
      while (iterator.hasNext()) {
         if (iterator.next().getValue() <= now) {
            iterator.remove();
         }
      }
   }
}
