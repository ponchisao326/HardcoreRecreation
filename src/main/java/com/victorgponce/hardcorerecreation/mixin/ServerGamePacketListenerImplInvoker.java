package com.victorgponce.hardcorerecreation.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplInvoker {

    @Invoker("restartClientLoadTimerAfterRespawn")
    void hardcorerecreation$restartClientLoadTimerAfterRespawn();
}
