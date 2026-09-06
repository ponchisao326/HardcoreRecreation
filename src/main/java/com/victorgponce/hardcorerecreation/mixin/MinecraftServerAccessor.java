package com.victorgponce.hardcorerecreation.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor("levels")
    Map<ResourceKey<Level>, ServerLevel> hardcorerecreation$levels();

    @Mutable
    @Accessor("worldGenSettings")
    void hardcorerecreation$setWorldGenSettings(WorldGenSettings settings);

    @Invoker("loadLevel")
    void hardcorerecreation$loadLevel();
}
