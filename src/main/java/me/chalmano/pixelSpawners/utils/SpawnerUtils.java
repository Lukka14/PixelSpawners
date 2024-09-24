package me.chalmano.pixelSpawners.utils;

import com.jeff_media.customblockdata.CustomBlockData;
import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.data.SpawnersReader;
import me.chalmano.pixelSpawners.enums.Const;
import me.chalmano.pixelSpawners.models.SpawnerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

        if (spawnedType == null) {
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

    public static SpawnerData nextSpawnerData(CreatureSpawner currentCreatureSpawner) {
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

    public static SpawnerData previousSpawnerData(SpawnerData currentSpawnerData) {
        if (currentSpawnerData == null) {
            return null;
        }

        List<SpawnerData> spawnerDataList = SpawnersReader.getInstance().getSpawnerData();
        int currentSpawnerDataIndex = spawnerDataList.indexOf(currentSpawnerData);

        if (currentSpawnerDataIndex < 1) {
            return null;
        }

        return spawnerDataList.get(--currentSpawnerDataIndex);
    }

    public static SpawnerData previousSpawnerData(CreatureSpawner currentCreatureSpawner) {
        return previousSpawnerData(SpawnerUtils.getSpawnerDataFor(currentCreatureSpawner));
    }


    public static String getSpawnerName(EntityType spawnedType) {
        return spawnedType == null ? "Unknown" : CommonUtils.firstToUpperCase(spawnedType.name());
    }

    public static boolean changeSpawnerTo(Block spawnerBlock, EntityType entityType) {
        CreatureSpawner cs = (CreatureSpawner) spawnerBlock.getState();

        if (cs.getSpawnedType() == null) {
            Logger.info("cs.getSpawnedType() is null #changeSpawnerTo()");
            return false;
        }

        cs.setSpawnedType(entityType);

        // update if setSpawnPerMinute() method is no longer used.
//        cs.update(true);

        int spawnPerMinute = getSpawnerDataFor(cs).getSpawn_time();
        setSpawnTime(cs, spawnPerMinute);
        addMobTypeToSpawnerPersistentData(spawnerBlock, entityType);

        return true;
    }

    // spawn time in seconds,
    // if spawnTime is 30 this means 1 mob will spawn every 30 seconds.
    public static void setSpawnTime(CreatureSpawner cs, int spawnTime) {
        int delay = spawnTime * 20;

        if (delay > cs.getMinSpawnDelay()) {
            cs.setMaxSpawnDelay(delay);
            cs.setMinSpawnDelay(delay);
        } else {
            cs.setMinSpawnDelay(delay);
            cs.setMaxSpawnDelay(delay);
        }

        cs.setSpawnCount(1);
        cs.update(true);
    }

    public static void addMobTypeToSpawnerPersistentData(Block spawnerBlock, EntityType entityType) {
        PersistentDataContainer customBlockData = CommonUtils.getPersistentDataContainerFor(spawnerBlock);
        NamespacedKey persistentKey = CommonUtils.getPersistentKey();

        String entityName = entityType.name();
        String value = "";

        if (customBlockData.has(persistentKey)) {
            String persistentDataValue = customBlockData.get(persistentKey, PersistentDataType.STRING);

            //already contains entity data
            if (persistentDataValue != null && persistentDataValue.contains(entityName)) {
                return;
            }

            value += addSep(persistentDataValue);
        }

        value += addSep(entityName);
        customBlockData.set(persistentKey, PersistentDataType.STRING, value);
    }

    // add data separator, currently - ';'
    public static String addSep(String str) {
        return str + Const.SPAWNER_INFO_SEPARATOR;
    }

    public static boolean spawnerBlockPersistentDataContainsSpawnType(Block spawnerBlock, SpawnerData spawnerData){
        NamespacedKey persistentKey = CommonUtils.getPersistentKey();
        PersistentDataContainer persistentDataContainerFor = CommonUtils.getPersistentDataContainerFor(spawnerBlock);
        if(!persistentDataContainerFor.has(persistentKey)){
            return false;
        }
        String persistentDataStr = persistentDataContainerFor.get(persistentKey, PersistentDataType.STRING);

        if(persistentDataStr == null){
            return false;
        }

        String[] entities = persistentDataStr.split(Const.SPAWNER_INFO_SEPARATOR);

        for (String entity : entities) {
            if(entity.equalsIgnoreCase(spawnerData.getSpawner_type())){
                return true;
            }
        }

        return false;
    }

}
