package com.victorgponce.hardcorerecreation;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public final class MobNaming {

    private static final Component ENDERMAN_NAME = Component.literal(MobNames.ENDERMAN);

    private static final Component CHICKEN_NAME = Component.literal(MobNames.CHICKEN);

    private MobNaming() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(MobNaming::onEntityLoad);
    }

    private static void onEntityLoad(Entity entity, ServerLevel level) {
        EntityType<?> type = entity.getType();
        if (type == EntityTypes.ENDERMAN) {
            entity.setCustomName(ENDERMAN_NAME);
        } else if (type == EntityTypes.CHICKEN) {
            entity.setCustomName(CHICKEN_NAME);
        }
    }
}
