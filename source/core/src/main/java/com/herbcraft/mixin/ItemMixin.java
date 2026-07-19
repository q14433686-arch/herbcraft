package com.herbcraft.mixin;

import com.herbcraft.Herbcraft;
import com.herbcraft.registry.HerbFoodRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
   @Inject(method = "finishUsingItem", at = @At("RETURN"))
   private void herbcraft$applyCustomFoodLogicOnlyAfterFinished(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
      if (HerbFoodRegistry.get(stack.getItem()) != null) {
         Herbcraft.applyAfterConsume(stack, level, entity);
      }
   }

   @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
   private void herbcraft$releaseShortUseProjectile(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfoReturnable<Boolean> cir) {
      if (Herbcraft.isShortUseProjectile(stack)) {
         cir.setReturnValue(Herbcraft.releaseShortUseProjectile(stack, level, entity, timeLeft));
      }
   }
}
