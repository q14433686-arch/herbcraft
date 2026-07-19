package com.herbcraft.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public final class HerbConsumeEvents {
   private static final List<BiConsumer<ServerPlayer, Item>> LISTENERS = new ArrayList<>();

   private HerbConsumeEvents() {
   }

   public static synchronized void register(BiConsumer<ServerPlayer, Item> listener) {
      LISTENERS.add(listener);
   }

   public static void fire(ServerPlayer player, Item item) {
      for (BiConsumer<ServerPlayer, Item> listener : LISTENERS) {
         listener.accept(player, item);
      }
   }
}
