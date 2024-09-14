package me.chalmano.pixelSpawners.utils;

import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.List;

public class HologramUtils {

    // BLOCK MUST BE A SPAWNER
    public static String getBlockHologramName(Block block) {
        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        EntityType spawnedType = creatureSpawner.getSpawnedType();

        String locationStr = block.getLocation().toString().
                replace('{', '_')
                .replace('}', '_')
                .replace('=', '_')
                .replace(',', '_')
                .replace('.', '_')
                .replace(' ', '_');

        return SpawnerUtils.getSpawnerName(spawnedType) + "_SPAWNER_" + locationStr;
    }

    public static void removeHologramForSpawner(Block spawnerBlock) {
        DHAPI.removeHologram(getBlockHologramName(spawnerBlock));
    }

    public static void removeHologram(String hologramName) {
        DHAPI.removeHologram(hologramName);
    }

    public static void createHologramForSpawner(Block spawnerBlock) {
        CreatureSpawner creatureSpawner = (CreatureSpawner) spawnerBlock.getState();
        EntityType spawnedType = creatureSpawner.getSpawnedType();
        String hologramText = "§a" + SpawnerUtils.getSpawnerName(spawnedType) + " Spawner";
        String name = getBlockHologramName(spawnerBlock);

        Location loc = spawnerBlock.getLocation().toCenterLocation().add(0, 1, 0);
        DHAPI.createHologram(name, loc, true, List.of(hologramText));
    }

    public static void updateHologramForSpawner(Block spawnerBlock) {
        HologramUtils.removeHologramForSpawner(spawnerBlock);
        HologramUtils.createHologramForSpawner(spawnerBlock);
    }

}
