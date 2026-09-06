package com.victorgponce.hardcorerecreation;

import com.victorgponce.hardcorerecreation.mixin.MinecraftServerAccessor;
import com.victorgponce.hardcorerecreation.mixin.PlayerListAccessor;
import com.victorgponce.hardcorerecreation.mixin.ServerGamePacketListenerImplInvoker;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Stream;

public final class WorldReset {

    private static final Logger LOGGER = LoggerFactory.getLogger("HardcoreRecreation");

    private static final Component ANNOUNCEMENT =
            Component.literal("Alguien ha muerto. El mundo se regenera.");

    private static final String DIMENSIONS_DIR = "dimensions";

    private static final String PLAYERS_DIR = "players";

    private static final String TRASH_PREFIX = "hcr-trash-";

    private WorldReset() {
    }

    public static void run(MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        List<ServerPlayer> players = List.copyOf(playerList.getPlayers());

        playerList.broadcastSystemMessage(ANNOUNCEMENT, false);
        server.setAutoSave(false);

        detachPlayers(players);
        forgetPlayerProgress(playerList, players);
        closeLevels(server);

        Path root = server.getWorldPath(LevelResource.ROOT);
        List<Path> trash = new ArrayList<>();
        trash.add(discard(root, DIMENSIONS_DIR));
        trash.add(discard(root, PLAYERS_DIR));
        recreateDirectories(root);

        reseed(server);
        resetLevelMetadata(server);

        ((MinecraftServerAccessor) server).hardcorerecreation$loadLevel();

        for (ServerLevel level : server.getAllLevels()) {
            level.noSave = false;
        }

        ServerLevel overworld = server.overworld();
        for (ServerPlayer player : players) {
            replacePlayer(playerList, overworld, player);
        }

        server.setAutoSave(true);
        deleteLater(trash);
    }

    private static void detachPlayers(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            PlayerWipe.wipe(player);
            player.level().removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
        }
    }

    private static void forgetPlayerProgress(PlayerList playerList, List<ServerPlayer> players) {
        PlayerListAccessor accessor = (PlayerListAccessor) playerList;
        Map<UUID, PlayerAdvancements> advancements = accessor.hardcorerecreation$advancements();
        for (ServerPlayer player : players) {
            UUID id = player.getUUID();
            PlayerAdvancements playerAdvancements = advancements.remove(id);
            if (playerAdvancements != null) {
                playerAdvancements.clearTriggers();
            }
            accessor.hardcorerecreation$stats().remove(id);
        }
    }

    private static void closeLevels(MinecraftServer server) {
        Map<ResourceKey<Level>, ServerLevel> levels = ((MinecraftServerAccessor) server).hardcorerecreation$levels();
        for (ServerLevel level : levels.values()) {
            level.noSave = true;
        }
        for (ServerLevel level : List.copyOf(levels.values())) {
            try {
                level.close();
            } catch (IOException e) {
                LOGGER.error("No se pudo cerrar el nivel {}", level.dimension().identifier(), e);
            }
        }
        levels.clear();
    }

    private static Path discard(Path root, String name) {
        Path source = root.resolve(name);
        if (!Files.isDirectory(source)) {
            return null;
        }
        Path target = root.resolve(TRASH_PREFIX + name + "-" + System.nanoTime());
        try {
            Files.move(source, target);
            return target;
        } catch (IOException e) {
            LOGGER.error("No se pudo apartar {}", source, e);
            return null;
        }
    }

    private static void recreateDirectories(Path root) {
        createDirectory(root.resolve(DIMENSIONS_DIR));
        createDirectory(root.resolve(LevelResource.PLAYER_DATA_DIR.id()));
        createDirectory(root.resolve(LevelResource.PLAYER_STATS_DIR.id()));
        createDirectory(root.resolve(LevelResource.PLAYER_ADVANCEMENTS_DIR.id()));
    }

    private static void createDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            LOGGER.error("No se pudo recrear {}", directory, e);
        }
    }

    private static void reseed(MinecraftServer server) {
        WorldGenSettings current = server.getWorldGenSettings();
        WorldOptions options = current.options().withSeed(OptionalLong.of(WorldOptions.randomSeed()));
        WorldGenSettings fresh = new WorldGenSettings(options, current.dimensions());
        ((MinecraftServerAccessor) server).hardcorerecreation$setWorldGenSettings(fresh);
        server.getDataStorage().set(WorldGenSettings.TYPE, fresh);
        LOGGER.info("Nueva seed: {}", options.seed());
    }

    private static void resetLevelMetadata(MinecraftServer server) {
        ServerLevelData overworldData = server.getWorldData().overworldData();
        overworldData.setInitialized(false);
        overworldData.setSpawn(LevelData.RespawnData.DEFAULT);
        overworldData.setGameTime(0L);
        server.setRespawnData(LevelData.RespawnData.DEFAULT);
    }

    private static void replacePlayer(PlayerList playerList, ServerLevel overworld, ServerPlayer player) {
        player.setServerLevel(overworld);
        ServerPlayer respawned = playerList.respawn(player, false, Entity.RemovalReason.KILLED);
        respawned.connection.player = respawned;
        respawned.connection.resetPosition();
        ((ServerGamePacketListenerImplInvoker) respawned.connection)
                .hardcorerecreation$restartClientLoadTimerAfterRespawn();
        PlayerWipe.wipe(respawned);
    }

    private static void deleteLater(List<Path> trash) {
        List<Path> targets = trash.stream().filter(Objects::nonNull).toList();
        if (targets.isEmpty()) {
            return;
        }
        Util.ioPool().execute(() -> targets.forEach(WorldReset::deleteRecursively));
    }

    private static void deleteRecursively(Path target) {
        try (Stream<Path> walk = Files.walk(target)) {
            walk.sorted(Comparator.reverseOrder()).forEach(WorldReset::deleteQuietly);
        } catch (IOException e) {
            LOGGER.warn("No se pudo recorrer {}", target, e);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.warn("No se pudo borrar {}", path, e);
        }
    }
}
