package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.data.SpawnersReader;
import me.chalmano.pixelSpawners.enums.Const;
import me.chalmano.pixelSpawners.models.SpawnerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        List<SpawnerData> prevCurrNextSpawnerData = getPrevCurrNextSpawnerData(spawnedType);
        return prevCurrNextSpawnerData == null ? null : prevCurrNextSpawnerData.get(1);
    }


    public static List<SpawnerData> getPrevCurrNextSpawnerData(EntityType spawnedType) {
        return spawnedType == null ? null : getPrevCurrNextSpawnerData(spawnedType.name());
    }

    /**
     * @param spawnedType spawnedType
     * @return null if not found
     */
    public static List<SpawnerData> getPrevCurrNextSpawnerData(String spawnedType) {

        if (spawnedType == null) {
            return null;
        }

        List<SpawnerData> prevCurrNextSpawnerData = new ArrayList<>();
        List<List<SpawnerData>> spawnerDataListList = SpawnersReader.getInstance().getSpawnerDataListList();

        for (List<SpawnerData> spawnerDataList : spawnerDataListList) {
            for (int i = 0; i < spawnerDataList.size(); i++) {
                if (!spawnedType.equalsIgnoreCase(spawnerDataList.get(i).getSpawner_type())) {
                    continue;
                }

                try {
                    prevCurrNextSpawnerData.add(0, spawnerDataList.get(i - 1));
                } catch (IndexOutOfBoundsException e) {
                    prevCurrNextSpawnerData.add(0, null);
                }

                prevCurrNextSpawnerData.add(1, spawnerDataList.get(i));

                try {
                    prevCurrNextSpawnerData.add(2, spawnerDataList.get(i + 1));
                } catch (IndexOutOfBoundsException e) {
                    prevCurrNextSpawnerData.add(null);
                }

                return prevCurrNextSpawnerData;
            }
        }

        Logger.info("No spawners found for type " + spawnedType);
        return null;
    }

    public static SpawnerData nextSpawnerData(CreatureSpawner currentCreatureSpawner) {
        List<SpawnerData> prevCurrNextSpawnerData = getPrevCurrNextSpawnerData(currentCreatureSpawner.getSpawnedType());
        return prevCurrNextSpawnerData == null ? null : prevCurrNextSpawnerData.get(2);
    }

    public static SpawnerData previousSpawnerData(SpawnerData currentSpawnerData) {
        if (currentSpawnerData == null) {
            return null;
        }
        List<SpawnerData> prevCurrNextSpawnerData = getPrevCurrNextSpawnerData(currentSpawnerData.getSpawner_type());
        return prevCurrNextSpawnerData == null ? null : prevCurrNextSpawnerData.get(0);
    }

    public static SpawnerData previousSpawnerData(CreatureSpawner currentCreatureSpawner) {
        return previousSpawnerData(SpawnerUtils.getSpawnerDataFor(currentCreatureSpawner));
    }


    public static String getSpawnerName(EntityType spawnedType) {
        return spawnedType == null ? "Unknown" : CommonUtils.firstToUpperCase(spawnedType.name());
    }

    public static boolean setSpawnerTo(Block spawnerBlock, EntityType entityType) {
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

        cs.resetTimer();
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

        CommonUtils.getPDCData(spawnerBlock);
    }

    // add data separator, currently - ';'
    public static String addSep(String str) {
        return str + Const.SPAWNER_INFO_SEPARATOR;
    }

    public static boolean spawnerBlockPersistentDataContainsSpawnType(Block spawnerBlock, SpawnerData spawnerData) {
        NamespacedKey persistentKey = CommonUtils.getPersistentKey();
        PersistentDataContainer persistentDataContainerFor = CommonUtils.getPersistentDataContainerFor(spawnerBlock);
        if (!persistentDataContainerFor.has(persistentKey)) {
            return false;
        }
        String persistentDataStr = persistentDataContainerFor.get(persistentKey, PersistentDataType.STRING);

        if (persistentDataStr == null) {
            return false;
        }

        String[] entities = persistentDataStr.split(Const.SPAWNER_INFO_SEPARATOR);

        for (String entity : entities) {
            if (entity.equalsIgnoreCase(spawnerData.getSpawner_type())) {
                return true;
            }
        }

        return false;
    }

    // null if not found
    public static Integer getIndexFor(Block spawerBlock) {
        SpawnerData spawnerData = getSpawnerDataFor(spawerBlock);

        if (spawnerData == null) {
            return null;
        }

        List<List<SpawnerData>> spawnerDataListList = SpawnersReader.getInstance().getSpawnerDataListList();
        for (int i = 0; i < spawnerDataListList.size(); i++) {
            if (spawnerDataListList.get(i).contains(spawnerData)) {
                return i;
            }
        }

        return null;
    }

    public static ItemStack makeSpawnerItem(Block spawnerBlock) {
        CreatureSpawner cs = (CreatureSpawner) spawnerBlock.getState();
        EntityType entityType = cs.getSpawnedType();

        final ItemStack itemStack = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = itemStack.getItemMeta();

        String persistentDataFor = CommonUtils.getPersistentDataFor(spawnerBlock);

        meta.getPersistentDataContainer().set(new NamespacedKey(PixelSpawners.getInstance(), "SPAWNER_ENTITY_TYPE"), PersistentDataType.STRING, entityType.name());
        meta.displayName(CommonUtils.toComponent("§a" + CommonUtils.normalizeName(entityType.name()) + " Spawner"));
        itemStack.setItemMeta(meta);

//        Bukkit.broadcastMessage("Transferring data from Block to Item: " + persistentDataFor);
        if (persistentDataFor != null) {
            addPDataToItem(itemStack, persistentDataFor);
            CommonUtils.getPDCData(itemStack);
        }


        return itemStack;
    }

    public static void addPDataToItem(ItemStack item, String value) {
        NamespacedKey key = CommonUtils.getPersistentKey();

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

}
