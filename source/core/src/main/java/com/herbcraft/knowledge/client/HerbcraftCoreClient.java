package com.herbcraft.knowledge.client;

import com.herbcraft.knowledge.CodexSnapshotPayload;
import com.herbcraft.knowledge.KnowledgeSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class HerbcraftCoreClient implements ClientModInitializer {
   public void onInitializeClient() {
      CodexClientTooltips.register();
      HerbNatureItemTooltips.register();
      ClientPlayNetworking.registerGlobalReceiver(CodexSnapshotPayload.TYPE, (payload, context) -> context.client().gui.setScreen(new CodexScreen(payload)));
      ClientPlayNetworking.registerGlobalReceiver(KnowledgeSyncPayload.TYPE, (payload, context) -> KnowledgeClientCache.update(payload.states()));
   }
}
