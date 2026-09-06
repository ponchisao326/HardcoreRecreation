package com.victorgponce.hardcorerecreation.mixin;

import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public class ForcedHardcoreMixin {

    @Inject(method = "isHardcore()Z", at = @At("HEAD"), cancellable = true)
    private void hardcorerecreation$alwaysHardcore(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
