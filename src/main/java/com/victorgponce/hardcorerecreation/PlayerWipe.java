package com.victorgponce.hardcorerecreation;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerWipe {

    private static final int FULL_FOOD_LEVEL = 20;

    private static final float FULL_SATURATION = 5.0F;

    private PlayerWipe() {
    }

    public static void wipe(ServerPlayer player) {
        player.getInventory().clearContent();
        player.getEnderChestInventory().clearContent();
        player.removeAllEffects();
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.totalExperience = 0;
        player.experienceProgress = 0.0F;
        player.setScore(0);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(FULL_FOOD_LEVEL);
        player.getFoodData().setSaturation(FULL_SATURATION);
        player.setRespawnPosition(null, true);
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }
}
