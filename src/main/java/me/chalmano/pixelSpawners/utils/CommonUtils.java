package me.chalmano.pixelSpawners.utils;

import com.jeff_media.customblockdata.CustomBlockData;
import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.enums.Const;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

}
