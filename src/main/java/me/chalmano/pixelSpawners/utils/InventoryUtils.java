package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.data.SpawnersReader;
import me.chalmano.pixelSpawners.inventory.SpawnerInventory;
import me.chalmano.pixelSpawners.models.Drop;
import me.chalmano.pixelSpawners.models.SpawnerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static final int MID_CHEST_INV_SLOT = 13;

    public static Inventory createUpgradeInventory(Block spawnerBlock) {
        CreatureSpawner currentCreatureSpawner = (CreatureSpawner) spawnerBlock.getState();
        SpawnerData nextSpawnerData = SpawnerUtils.nextSpawnerData(currentCreatureSpawner);
        SpawnerData previousSpawnerData = SpawnerUtils.previousSpawnerData(currentCreatureSpawner);

        Logger.info("Creating new inventory");
        Inventory inventory = Bukkit.createInventory(new SpawnerInventory(), InventoryType.CHEST, Component.text("Upgrade Spawner", TextColor.color(110, 49, 33)));

        ItemStack upgradeItem = null;

        if (nextSpawnerData == null) {
            SpawnerData currentSpawnerData = SpawnerUtils.getSpawnerDataFor(currentCreatureSpawner);

            if (currentSpawnerData == null) {
                return null;
            }

            upgradeItem = createLastStageItem(currentSpawnerData);
            fillInventory(inventory, Material.ORANGE_STAINED_GLASS_PANE);
        } else {
            fillInventory(inventory, Material.GRAY_STAINED_GLASS_PANE);
            upgradeItem = createUpgradeItem(spawnerBlock);
        }

        // todo if upgrade is max still open the inventory with current mob display item saying it's maxed.
//        ItemStack downgradeItem = createDowngradeItem(previousSpawnerData);
        ItemStack downgradeItem = null;

        int upgradeItemSlot = MID_CHEST_INV_SLOT;

        if (downgradeItem != null) {
            inventory.setItem(upgradeItemSlot - 1, downgradeItem);
            upgradeItemSlot++;
        }

        inventory.setItem(upgradeItemSlot, upgradeItem);
        inventory.setItem(inventory.getSize() - 1, createMenuItem());
        return inventory;
    }

    public static Inventory createSpawnerMenuInventory() {
        List<SpawnerData> spawnerDataList = SpawnersReader.getInstance().getSpawnerData();
        if (spawnerDataList == null) {
            return null;
        }

        int invSize = (int) (2 * 9 + Math.ceil(spawnerDataList.size() / 9.0) * 9);

        Inventory inventory = Bukkit.createInventory(new SpawnerInventory(), invSize, Component.text("Spawner Menu", TextColor.color(110, 49, 33)));
        fillInventory(inventory, Material.MAGENTA_STAINED_GLASS_PANE);

        int startPosition = 9;

        for (SpawnerData spawnerData : spawnerDataList) {
            inventory.setItem(startPosition++, createItemFor(spawnerData));
        }

        return inventory;
    }

    public static Inventory createDowngradeInventory(CreatureSpawner currentCreatureSpawner) {
        SpawnerData previousSpawnerData = SpawnerUtils.previousSpawnerData(currentCreatureSpawner);

        if (previousSpawnerData == null) {
            return null;
        }


        Inventory inventory = Bukkit.createInventory(new SpawnerInventory(), InventoryType.CHEST, Component.text("Spawner Menu", TextColor.color(110, 49, 33)));
        fillInventory(inventory, Material.ORANGE_STAINED_GLASS_PANE);


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

    public static ItemStack createUpgradeItem(Block spawnerBlock) {
        CreatureSpawner currentCreatureSpawner = (CreatureSpawner) spawnerBlock.getState();
        SpawnerData nextSpawnerData = SpawnerUtils.nextSpawnerData(currentCreatureSpawner);
        if (nextSpawnerData == null) {
            return null;
        }

        ItemStack itemStack = new ItemStack(Material.valueOf(nextSpawnerData.getDisplay_item()));

        ItemMeta itemMeta = itemStack.getItemMeta();

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300Upgrade to " + CommonUtils.firstToUpperCase(nextSpawnerData.getSpawner_type()) + " Spawner").decoration(TextDecoration.ITALIC, false);
        itemMeta.displayName(itemName);

        List<Component> loreList = new ArrayList<>();

        String loreColor = "&#f5f520";

        loreList.add(Component.empty());

        String pricePrefix = loreColor + "Price: &f";
        String priceStr;

        if (SpawnerUtils.spawnerBlockPersistentDataContainsSpawnType(spawnerBlock, nextSpawnerData)) {
            priceStr = pricePrefix + "FREE";
        } else {
            priceStr = pricePrefix + "$" + nextSpawnerData.getPrice();
        }

        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(priceStr).decoration(TextDecoration.ITALIC, false));
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(loreColor + "Spawn time: &f" + nextSpawnerData.getSpawn_time() + "s").decoration(TextDecoration.ITALIC, false));

        itemMeta.lore(loreList);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }


    public static ItemStack createDowngradeItem(Block spawnerBlock) {
        CreatureSpawner currentCreatureSpawner = (CreatureSpawner) spawnerBlock.getState();
        return createDowngradeItem(SpawnerUtils.previousSpawnerData(currentCreatureSpawner));
    }

    public static ItemStack createDowngradeItem(SpawnerData previousSpawnerData) {

        if (previousSpawnerData == null) {
            return null;
        }

        ItemStack itemStack = new ItemStack(Material.valueOf(previousSpawnerData.getDisplay_item()));

        ItemMeta itemMeta = itemStack.getItemMeta();

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300Downgrade to " + CommonUtils.firstToUpperCase(previousSpawnerData.getSpawner_type()) + " Spawner").decoration(TextDecoration.ITALIC, false);
        itemMeta.displayName(itemName);

        List<Component> loreList = new ArrayList<>();

        String loreColor = "&#f5f520";

        loreList.add(Component.empty());
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize("&aAfter downgrading, you can still").decoration(TextDecoration.ITALIC, false));
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize("&aupgrade to current spawner for free!").decoration(TextDecoration.ITALIC, false));
        loreList.add(Component.empty());
        loreList.add(LegacyComponentSerializer.legacy('&').deserialize(loreColor + "Spawn time: &f" + previousSpawnerData.getSpawn_time() + "s").decoration(TextDecoration.ITALIC, false));

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


    public static ItemStack createItemFor(SpawnerData spawnerData) {
        ItemStack itemStack = new ItemStack(Material.valueOf(spawnerData.getDisplay_item()));
        ItemMeta itemMeta = itemStack.getItemMeta();

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300" + normalizeName(spawnerData.getSpawner_type()) + " Spawner").decoration(TextDecoration.ITALIC, false);
        itemMeta.displayName(itemName);

        List<Component> loreList = new ArrayList<>();

        String loreColor = "&a";
        loreList.add(Component.empty());

        loreList.add(CommonUtils.toComponent(loreColor + "Spawn time: " + spawnerData.getSpawn_time() + "s"));
        loreList.add(CommonUtils.toComponent(loreColor + "Drops: "));

        for (Drop drop : spawnerData.getDrops()) {
            loreList.add(CommonUtils.toComponent(loreColor + " - " + normalizeName(drop.getItem()) + " x" + drop.getAmount()));
        }

        loreList.add(Component.empty());

        loreList.add(CommonUtils.toComponent(loreColor + "Upgrade Requirements: "));
        loreList.add(CommonUtils.toComponent(loreColor + " - $" + spawnerData.getPrice()).decoration(TextDecoration.ITALIC, false));
        SpawnerData previousSpawnerData = SpawnerUtils.previousSpawnerData(spawnerData);
        if (previousSpawnerData != null) {
            loreList.add(CommonUtils.toComponent(loreColor + " - " + previousSpawnerData.getSpawner_type() + " Spawner"));
        }

        itemMeta.lore(loreList);
        itemMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
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

    public static ItemStack createMenuItem() {
        ItemStack itemStack = new ItemStack(Material.NETHER_STAR);

        ItemMeta itemMeta = itemStack.getItemMeta();

        TextComponent itemName = LegacyComponentSerializer.legacy('&').deserialize("&#FFD300Open Menu").decoration(TextDecoration.ITALIC, false);
        itemMeta.displayName(itemName);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
