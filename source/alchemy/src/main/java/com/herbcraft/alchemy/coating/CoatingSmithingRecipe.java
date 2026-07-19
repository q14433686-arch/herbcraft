package com.herbcraft.alchemy.coating;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class CoatingSmithingRecipe extends SimpleSmithingRecipe {
   public static final MapCodec<CoatingSmithingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(CoatingSmithingRecipe.RecipeMode.CODEC.fieldOf("mode").forGetter(recipe -> recipe.mode))
         .apply(instance, CoatingSmithingRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, CoatingSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
      CoatingSmithingRecipe.RecipeMode.STREAM_CODEC, recipe -> recipe.mode, CoatingSmithingRecipe::new
   );
   private final CoatingSmithingRecipe.RecipeMode mode;

   public CoatingSmithingRecipe(CoatingSmithingRecipe.RecipeMode mode) {
      super(new CommonInfo(false));
      this.mode = mode;
   }

   public RecipeSerializer<CoatingSmithingRecipe> getSerializer() {
      return CoatingRecipes.COATING_SMITHING;
   }

   public boolean isSpecial() {
      return true;
   }

   public Optional<Ingredient> templateIngredient() {
      return Optional.of(
         this.mode == CoatingSmithingRecipe.RecipeMode.APPLY
            ? Ingredient.of(CoatingItems.BLANK_ADHESIVE)
            : Ingredient.of(new ItemLike[]{CoatingItems.AMETHYST_ADHESIVE, CoatingItems.ECHO_ADHESIVE})
      );
   }

   public Ingredient baseIngredient() {
      return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(CoatingSystem.COATABLE_WEAPONS));
   }

   public Optional<Ingredient> additionIngredient() {
      return this.mode == CoatingSmithingRecipe.RecipeMode.APPLY ? Optional.of(Ingredient.of(AlchemyRegistries.essenceItems().stream())) : Optional.empty();
   }

   public boolean matches(SmithingRecipeInput input, Level level) {
      ItemStack template = input.template();
      ItemStack base = input.base();
      ItemStack addition = input.addition();
      if (!CoatingSystem.isCoatable(base)) {
         return false;
      } else if (this.mode == CoatingSmithingRecipe.RecipeMode.APPLY) {
         return template.is(CoatingItems.BLANK_ADHESIVE) && AlchemyRegistries.essenceInfo(addition.getItem()).isPresent();
      } else {
         boolean amethyst = template.is(CoatingItems.AMETHYST_ADHESIVE);
         boolean echo = template.is(CoatingItems.ECHO_ADHESIVE);
         if ((amethyst || echo) && addition.isEmpty()) {
            WeaponCoating coating = (WeaponCoating)base.get(CoatingComponents.WEAPON_COATING);
            return coating != null && coating.mode() == WeaponCoating.Mode.FULL
               ? AlchemyRegistries.essenceInfoById(coating.essenceId())
                  .map(info -> info.effects().stream().anyMatch(e -> e.positive() == amethyst))
                  .orElse(false)
               : false;
         } else {
            return false;
         }
      }
   }

   public ItemStack assemble(SmithingRecipeInput input) {
      ItemStack result = input.base().copyWithCount(1);
      if (this.mode == CoatingSmithingRecipe.RecipeMode.APPLY) {
         Optional<AlchemyRegistries.EssenceInfo> info = AlchemyRegistries.essenceInfo(input.addition().getItem());
         if (info.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            CoatingSystem.applyCoating(result, info.get(), WeaponCoating.Mode.FULL);
            return result;
         }
      } else {
         WeaponCoating coating = (WeaponCoating)result.get(CoatingComponents.WEAPON_COATING);
         if (coating == null) {
            return ItemStack.EMPTY;
         } else {
            Optional<AlchemyRegistries.EssenceInfo> info = AlchemyRegistries.essenceInfoById(coating.essenceId());
            if (info.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               WeaponCoating.Mode newMode = input.template().is(CoatingItems.AMETHYST_ADHESIVE) ? WeaponCoating.Mode.POSITIVE : WeaponCoating.Mode.NEGATIVE;
               CoatingSystem.applyCoating(result, info.get(), newMode);
               return result;
            }
         }
      }
   }

   protected PlacementInfo createPlacementInfo() {
      return PlacementInfo.createFromOptionals(List.of(this.templateIngredient(), Optional.of(this.baseIngredient()), this.additionIngredient()));
   }

   public static enum RecipeMode implements StringRepresentable {
      APPLY("apply"),
      REFINE("refine");

      public static final Codec<CoatingSmithingRecipe.RecipeMode> CODEC = StringRepresentable.fromEnum(CoatingSmithingRecipe.RecipeMode::values);
      public static final StreamCodec<ByteBuf, CoatingSmithingRecipe.RecipeMode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
         .map(CoatingSmithingRecipe.RecipeMode::byName, CoatingSmithingRecipe.RecipeMode::getSerializedName);
      private final String serializedName;

      private RecipeMode(String serializedName) {
         this.serializedName = serializedName;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      public static CoatingSmithingRecipe.RecipeMode byName(String name) {
         return "refine".equals(name) ? REFINE : APPLY;
      }
   }
}
