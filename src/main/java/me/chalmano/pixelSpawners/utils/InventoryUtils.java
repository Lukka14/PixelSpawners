package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.inventory.SpawnerInventory;
import me.chalmano.pixelSpawners.models.SpawnerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static final int UPGRADE_ITEM_INDEX = 13;

    public static Inventory createInventory(CreatureSpawner currentCreatureSpawner) {

        SpawnerData nextSpawnerData = SpawnerUtils.getNextSpawnerData(currentCreatureSpawner);

        Logger.info("Creating new inventory");
        Inventory inventory = Bukkit.createInventory(new SpawnerInventory(), InventoryType.CHEST, Component.text("Upgrade Spawner", TextColor.color(110, 49, 33)));

        ItemStack upgradeItem = null;

        if (nextSpawnerData == null) {
            SpawnerData currentSpawnerData = SpawnerUtils.getSpawnerDataFor(currentCreatureSpawner);

            if(currentSpawnerData == null ){
                return null;
            }

            upgradeItem = createLastStageItem(currentSpawnerData);
            fillInventory(inventory, Material.ORANGE_STAINED_GLASS_PANE);
        } else {
            fillInventory(inventory, Material.GRAY_STAINED_GLASS_PANE);
            upgradeItem = createUpgradeItem(nextSpawnerData);
        }

        // todo if upgrade is max still open the inventory with current mob display item saying it's maxed.
        inventory.setItem(UPGRADE_ITEM_INDEX, upgradeItem);

        return inventory;
    }

    private static void fillInventory(Inventory inventory, Material material) {
        ItemStack[] itemStacks = new ItemStack[inventory.getSize()];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = new ItemStack(material);
            itemStacks[i].getItemMeta().displayName(Component.text(""));
        }
        inventory.setContents(itemStacks);
    }

    public static ItemStack createUpgradeItem(SpawnerData nextSpawnerData) {
        ItemStack itemStack = new ItemStack(Material.valueOf(nextSpawnerData.getDisplay_item()));

        ItemMeta itemMeta = itemStack.getItemMeta();

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300Upgrade to " + CommonUtils.firstToUpperCase(nextSpawnerData.getSpawner_type()) + " Spawner").decoration(TextDecoration.ITALIC, false);
        itemMeta.displayName(itemName);

        List<Component> loreList = new ArrayList<>();

        String loreColor = "&#f5f520";

        loreList.add(Component.empty());
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(loreColor + "Price: &f$" + nextSpawnerData.getPrice()).decoration(TextDecoration.ITALIC, false));
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(loreColor + "Spawn time: &f" + nextSpawnerData.getSpawn_time()+"s").decoration(TextDecoration.ITALIC, false));

        itemMeta.lore(loreList);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static ItemStack createLastStageItem(SpawnerData currentSpawnerData) {
        ItemStack itemStack = new ItemStack(Material.valueOf(currentSpawnerData.getDisplay_item()));

        ItemMeta itemMeta = itemStack.getItemMeta();

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300Max Stage reached").decoration(TextDecoration.ITALIC, false);
        itemMeta.displayName(itemName);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
