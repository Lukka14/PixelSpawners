package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.data.SpawnersReader;
import me.chalmano.pixelSpawners.models.SpawnerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpawnerUtils {

    /**
     * @param block possible spawner block
     * @return true if spawner block is in the spawners.json file
     */
    public static boolean isValidSpawner(Block block) {
        return getSpawnerDataFor(block) == null;
    }

    /**
     * @param block possible spawner block
     * @return null if not found
     */
    public static SpawnerData getSpawnerDataFor(Block block) {
        if (block.getType() != Material.SPAWNER) {
            Logger.info("Block is not a valid spawner #getSpawnerDataFor()");
            return null;
        }

        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        return getSpawnerDataFor(creatureSpawner);
    }

    /**
     * @param creatureSpawner creatureSpawner
     * @return null if not found
     */
    public static SpawnerData getSpawnerDataFor(@NotNull CreatureSpawner creatureSpawner) {
        return getSpawnerDataFor(creatureSpawner.getSpawnedType());
    }

    /**
     * @param spawnedType spawnedType
     * @return null if not found
     */
    public static SpawnerData getSpawnerDataFor(EntityType spawnedType) {

        if(spawnedType == null){
            return null;
        }

        List<SpawnerData> spawnerDataList = SpawnersReader.getInstance().getSpawnerData();

        for (SpawnerData spawnerData : spawnerDataList) {
            Logger.info(spawnerData.toString());
            if (spawnedType.name().equalsIgnoreCase(spawnerData.getSpawner_type())) {
                return spawnerData;
            }
        }

        Logger.info("No spawners found for type " + spawnedType + " #getSpawnerDataFor()");
        return null;
    }

    public static SpawnerData getNextSpawnerData(CreatureSpawner currentCreatureSpawner) {
        SpawnerData currentSpawnerData = SpawnerUtils.getSpawnerDataFor(currentCreatureSpawner);
        if (currentSpawnerData == null) {
            return null;
        }

        List<SpawnerData> spawnerDataList = SpawnersReader.getInstance().getSpawnerData();
        int currentSpawnerDataIndex = spawnerDataList.indexOf(currentSpawnerData);

        if (currentSpawnerDataIndex >= spawnerDataList.size() - 1) {
            Logger.info("No next item in the spawnerDataList #getNextSpawnerData()");
            return null;
        }

        return spawnerDataList.get(++currentSpawnerDataIndex);
    }


    public static String getSpawnerName(EntityType spawnedType) {
        return spawnedType == null ? "Unknown" : CommonUtils.firstToUpperCase(spawnedType.name());
    }

    public static boolean upgradeSpawnerTo(Block spawnerBlock, EntityType entityType) {
        CreatureSpawner cs = (CreatureSpawner) spawnerBlock.getState();

        if (cs.getSpawnedType() == null) {
            Logger.info("cs.getSpawnedType() is null #upgradeSpawnerTo()");
            return false;
        }

        cs.setSpawnedType(entityType);

        // update if setSpawnPerMinute() method is no longer used.
//        cs.update(true);

        int spawnPerMinute = getSpawnerDataFor(cs).getSpawn_time();
        setSpawnTime(cs, spawnPerMinute);


        return true;
    }

    // spawn time in seconds,
    // if spawnTime is 30 this means 1 mob will spawn every 30 seconds.
    public static void setSpawnTime(CreatureSpawner cs, int spawnTime) {
        int delay = spawnTime * 20;

        if(delay > cs.getMinSpawnDelay()){
            cs.setMaxSpawnDelay(delay);
            cs.setMinSpawnDelay(delay);
        }else{
            cs.setMinSpawnDelay(delay);
            cs.setMaxSpawnDelay(delay);
        }

        cs.setSpawnCount(1);
        cs.update(true);
    }

}
