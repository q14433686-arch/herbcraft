package com.herbcraft.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class CodexBookItem extends Item {
   public CodexBookItem(Properties properties) {
      super(properties);
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         HerbcraftAdvancements.grant(serverPlayer, "open_codex");
         ServerPlayNetworking.send(serverPlayer, CodexSnapshotPayload.build(serverPlayer));
      }

      return InteractionResult.SUCCESS;
   }
}
