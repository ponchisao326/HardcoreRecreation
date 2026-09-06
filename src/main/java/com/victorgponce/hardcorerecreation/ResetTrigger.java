package com.victorgponce.hardcorerecreation;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ResetTrigger {

    private static final AtomicBoolean PENDING = new AtomicBoolean();

    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    private ResetTrigger() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(ResetTrigger::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(ResetTrigger::onEndTick);
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer) {
            PENDING.set(true);
        }
    }

    private static void onEndTick(MinecraftServer server) {
        if (!PENDING.get() || !RUNNING.compareAndSet(false, true)) {
            return;
        }
        try {
            WorldReset.run(server);
        } finally {
            PENDING.set(false);
            RUNNING.set(false);
        }
    }
}
