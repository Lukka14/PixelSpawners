package me.chalmano.pixelSpawners.events;

import eu.decentsoftware.holograms.api.DHAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RightClickEvent implements Listener {

    private static Map<Player, Block> clickedSpawnerMap = new HashMap<>();

    private Inventory inventory;

    public ItemStack getItem(CreatureSpawner cs) {
        EntityType spawnedType = cs.getSpawnedType() == null ? EntityType.MINECART_MOB_SPAWNER : cs.getSpawnedType();
        String newSpawnerType = getSpawnerName(EntityType.fromId(spawnedType.getTypeId() + 1));

        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.displayName(Component.text("Upgrade Spawner to " + newSpawnerType).color(TextColor.color(0xFFB80C)));
        item.setItemMeta(itemMeta);
        return item;
    }

    // BLOCK MUST BE A SPAWNER
    public String getBlockHologramName(Block block) {
        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        EntityType spawnedType = creatureSpawner.getSpawnedType();

        String locationStr = block.getLocation().toString().
                replace('{', '_')
                .replace('}', '_')
                .replace('=', '_')
                .replace(',', '_')
                .replace('.', '_')
                .replace(' ', '_');

        return getSpawnerName(spawnedType) + "_SPAWNER_" + locationStr;
    }


    public String firstToUpperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public String getSpawnerName(EntityType spawnedType) {
        return spawnedType == null ? "Unknown" : firstToUpperCase(spawnedType.name());
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        if (blockPlaced.getType() != Material.SPAWNER) {
            return;
        }

        createHologramForSpawner(blockPlaced);
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        removeHologramForSpawner(block);
    }

    public void removeHologramForSpawner(Block spawnerBlock) {
        DHAPI.removeHologram(getBlockHologramName(spawnerBlock));
    }

    public void createHologramForSpawner(Block spawnerBlock) {
        CreatureSpawner creatureSpawner = (CreatureSpawner) spawnerBlock.getState();
        EntityType spawnedType = creatureSpawner.getSpawnedType();
        String hologramText = "§a" + getSpawnerName(spawnedType) + " Spawner";
        String name = getBlockHologramName(spawnerBlock);

        Location loc = spawnerBlock.getLocation().toCenterLocation().add(0, 1, 0);
        DHAPI.createHologram(name, loc, true, List.of(hologramText));
    }

    @EventHandler
    public void onSpawnerRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = e.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getType() != Material.SPAWNER) {
            return;
        }

        CreatureSpawner creatureSpawner = (CreatureSpawner) clickedBlock.getState();

        Player player = e.getPlayer();
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, Component.text("Upgrade Spawner", TextColor.color(93, 255, 0)));

        // filling inventory
        ItemStack[] itemStacks = new ItemStack[inventory.getSize()];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
            itemStacks[i].getItemMeta().displayName(Component.text(""));
        }
        inventory.setContents(itemStacks);


        inventory.setItem(13, getItem(creatureSpawner));

        player.openInventory(inventory);
        this.inventory = inventory;
        clickedSpawnerMap.put(player, clickedBlock);

        e.setCancelled(true);
    }


    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }

        CreatureSpawner cs = (CreatureSpawner) clickedSpawnerMap.get(player).getState();

        if (!getItem(cs).equals(item)) {
            return;
        }

        upgradeSpawner(player);
    }

    public void upgradeSpawner(Player player) {
        //todo BUG: #removeHologramForSpawner() method won't remove the hologram as spawnerBlock object state is updated.
        Block spawnerBlock = clickedSpawnerMap.get(player);

        CreatureSpawner cs = (CreatureSpawner) spawnerBlock.getState();

        EntityType spawnedType = cs.getSpawnedType() == null ? EntityType.MINECART_MOB_SPAWNER : cs.getSpawnedType();
        cs.setSpawnedType(EntityType.fromId(spawnedType.getTypeId() + 1));
        cs.update();
        cs.setDelay(100);

        spawnedType = cs.getSpawnedType();

        player.closeInventory();
        player.sendMessage("§a(!) Spawner has been upgraded to " + getSpawnerName(spawnedType));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 10F);
        player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 10, 1);

        removeHologramForSpawner(spawnerBlock);
        createHologramForSpawner(spawnerBlock);
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            event.getEntity().setHealth(0);
        }
    }

}
