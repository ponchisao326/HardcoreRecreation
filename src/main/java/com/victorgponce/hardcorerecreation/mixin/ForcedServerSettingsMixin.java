package com.victorgponce.hardcorerecreation.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class ForcedServerSettingsMixin {

    @Inject(method = "forceDifficulty()V", at = @At("HEAD"), cancellable = true)
    private void hardcorerecreation$forceHardDifficulty(CallbackInfo ci) {
        ((MinecraftServer) (Object) this).setDifficulty(Difficulty.HARD, true);
        ci.cancel();
    }

    @Inject(method = "forceSynchronousWrites()Z", at = @At("HEAD"), cancellable = true)
    private void hardcorerecreation$disableSyncChunkWrites(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "allowFlight()Z", at = @At("HEAD"), cancellable = true)
    private void hardcorerecreation$allowFlight(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "spawnProtectionRadius()I", at = @At("HEAD"), cancellable = true)
    private void hardcorerecreation$disableSpawnProtection(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
