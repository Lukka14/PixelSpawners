package me.chalmano.pixelSpawners.utils;

import com.jeff_media.customblockdata.CustomBlockData;
import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.enums.Const;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CommonUtils {

    public static String firstToUpperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static Component toComponent(String str) {
        return LegacyComponentSerializer.legacy('&').deserialize(str.trim()).decoration(TextDecoration.ITALIC, false);
    }

    public static NamespacedKey getPersistentKey(){
        return new NamespacedKey(PixelSpawners.getInstance(), Const.PERSISTENT_KEY);
    }

    public static PersistentDataContainer getPersistentDataContainerFor(Block spawnerBlock){
        return new CustomBlockData(spawnerBlock, PixelSpawners.getInstance());
    }

    public static String getPersistentDataFor(Block spawnerBlock){
        PersistentDataContainer customBlockData = CommonUtils.getPersistentDataContainerFor(spawnerBlock);
        NamespacedKey persistentKey = CommonUtils.getPersistentKey();

        if (customBlockData.has(persistentKey)) {
            return customBlockData.get(persistentKey, PersistentDataType.STRING);
        }
        return null;
    }

    public static String getPDCData(Block spawnerBlock) {
        NamespacedKey key = CommonUtils.getPersistentKey();
        PersistentDataContainer container = CommonUtils.getPersistentDataContainerFor(spawnerBlock);
        return getPDCData(container, key);
    }

    private static String getPDCData(PersistentDataContainer container, NamespacedKey key) {
        if (container.has(key, PersistentDataType.STRING)) {
            String value = container.get(key, PersistentDataType.STRING);
//            Bukkit.broadcast(Component.text("Spawner data: " + value));
            return value;
        }
//        Bukkit.broadcast(Component.text("No spawner data found."));
        return null;
    }

    public static String getPDCData(ItemStack item) {
        NamespacedKey key = CommonUtils.getPersistentKey();
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        return getPDCData(container, key);
    }

    public static String normalizeName(String itemName) {
        StringBuilder res = new StringBuilder();
        String[] split = itemName.split("_");

        for (int i = 0; i < split.length; i++) {
            String str = split[i];

            if (i != 0) {
                res.append(" ");
            }

            res.append(CommonUtils.firstToUpperCase(str));
        }

        return res.toString();
    }

    public static String getSpawnTime(int timeInSeconds){
        if(timeInSeconds < 60){
            return timeInSeconds + "s";
        }

        double timeInMinutes = timeInSeconds / 60.0;
        return Math.round(Math.round(timeInMinutes*100)/100.0) + "m";
    }

}
