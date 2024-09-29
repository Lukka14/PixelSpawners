package me.chalmano.pixelSpawners.utils;

import eu.decentsoftware.holograms.api.DHAPI;
import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.models.SpawnerData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
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
        removeHologram(getBlockHologramName(spawnerBlock));
    }

    public static void removeHologram(String hologramName) {
        try{
            DHAPI.removeHologram(hologramName);
        }catch (Exception e) {
            PixelSpawners.getInstance().getLogger().warning("Failed to remove hologram " + hologramName);
        }
    }

    public static void createHologramForSpawner(Block spawnerBlock) {
        CreatureSpawner creatureSpawner = (CreatureSpawner) spawnerBlock.getState();
        EntityType spawnedType = creatureSpawner.getSpawnedType();

        List<String> lines = new ArrayList<>();

        String hologramText = "§a" + CommonUtils.normalizeName(SpawnerUtils.getSpawnerName(spawnedType)) + " Spawner";
        lines.add(hologramText);

        SpawnerData spawnerDataFor = SpawnerUtils.getSpawnerDataFor(creatureSpawner);

        if(spawnerDataFor != null) {
            String hologramLine2 = "§aSpawn time: " + CommonUtils.getSpawnTime(spawnerDataFor.getSpawn_time());
            lines.add(hologramLine2);
        }

        String name = getBlockHologramName(spawnerBlock);
        Location loc = spawnerBlock.getLocation().toCenterLocation().add(0, 1, 0);

        DHAPI.createHologram(name, loc, true, lines);
    }

}
