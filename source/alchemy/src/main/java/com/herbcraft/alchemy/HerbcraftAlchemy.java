package com.herbcraft.alchemy;

import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.CoatingHitHandler;
import com.herbcraft.alchemy.coating.CoatingItems;
import com.herbcraft.alchemy.coating.CoatingRecipes;
import com.herbcraft.alchemy.knowledge.AlchemyAcquisitionHooks;
import com.herbcraft.alchemy.knowledge.AlchemyChapter;
import com.herbcraft.alchemy.knowledge.AlchemyCodexHooks;
import com.herbcraft.alchemy.knowledge.AlchemyUsageHints;
import com.herbcraft.alchemy.potion.AdvancedPotionInventoryHooks;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HerbcraftAlchemy implements ModInitializer {
   public static final String MOD_ID = "herbcraft_alchemy";
   public static final Logger LOGGER = LoggerFactory.getLogger("herbcraft_alchemy");

   public void onInitialize() {
      CoatingComponents.initialize();
      AlchemyPotionComponents.initialize();
      CoatingItems.initialize();
      CoatingRecipes.initialize();
      AlchemyRegistries.initialize();
      CoatingHitHandler.register();
      AlchemyChapter.register();
      AlchemyCodexHooks.register();
      AlchemyUsageHints.register();
      AlchemyAcquisitionHooks.register();
      AdvancedPotionInventoryHooks.register();
      LOGGER.info("Herbcraft: Alchemy loaded. Registered essences, brewing mixes and the v3.4 weapon coating system.");
   }
}
