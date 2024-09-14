package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.models.SpawnerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.HSVLike;
import net.kyori.adventure.util.RGBLike;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryUtils {

    public static Inventory createInventory(CreatureSpawner currentCreatureSpawner) {

        SpawnerData nextSpawnerData = SpawnerUtils.getNextSpawnerData(currentCreatureSpawner);

        if (nextSpawnerData == null) {
            return null;
        }

        Logger.info("Creating new inventory");
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, Component.text("Upgrade Spawner", TextColor.color(93, 255, 0)));
        fillInventory(inventory, Material.GRAY_STAINED_GLASS_PANE);

        // todo if upgrade is max still open the inventory with current mob display item saying it's maxed.
        inventory.setItem(13, createUpgradeItem(nextSpawnerData));

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

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300Upgrade to " + CommonUtils.firstToUpperCase(nextSpawnerData.getSpawner_type()) + " spawner").decoration(TextDecoration.ITALIC,false);
        itemMeta.displayName(itemName);

        List<Component> loreList = new ArrayList<>();

        String loreColor = "&#f5f520";

        loreList.add(Component.empty());
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(loreColor + "Price: &f$" + nextSpawnerData.getPrice()).decoration(TextDecoration.ITALIC,false));
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(loreColor + "Spawn per minute: &f" + nextSpawnerData.getSpawn_per_minute()).decoration(TextDecoration.ITALIC,false));

        itemMeta.lore(loreList);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
