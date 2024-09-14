package me.chalmano.pixelSpawners.events;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.models.SpawnerData;
import me.chalmano.pixelSpawners.utils.HologramUtils;
import me.chalmano.pixelSpawners.utils.InventoryUtils;
import me.chalmano.pixelSpawners.utils.Logger;
import me.chalmano.pixelSpawners.utils.SpawnerUtils;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SpawnerEvents implements Listener {

    private static final Map<Player, Block> clickedSpawnerMap = new HashMap<>();

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        if (blockPlaced.getType() != Material.SPAWNER) {
            return;
        }

        HologramUtils.createHologramForSpawner(blockPlaced);
    }


    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        HologramUtils.removeHologramForSpawner(block);
    }

    @EventHandler
    public void onSpawnerRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = e.getClickedBlock();

        if (clickedBlock == null) {
            Logger.info("clickedBlock is null #onSpawnerRightClick()");
            return;
        }

        if (clickedBlock.getType() != Material.SPAWNER) {
            return;
        }
        Logger.info("Material type is SPAWNER #onSpawnerRightClick()");

        Logger.info("Place-1");
        CreatureSpawner creatureSpawner = (CreatureSpawner) clickedBlock.getState();

        Player player = e.getPlayer();


        Inventory inventory = InventoryUtils.createInventory(creatureSpawner);

        if (inventory == null) {
            return;
        }
        Logger.info("Place-2");
        player.openInventory(inventory);
        clickedSpawnerMap.put(player, clickedBlock);

        e.setCancelled(true);
    }


    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        Block spawnerBlock = clickedSpawnerMap.get(player);

        if (spawnerBlock == null) {
            return;
        }

        Logger.info("Place1");

        CreatureSpawner cs = (CreatureSpawner) spawnerBlock.getState();

        Inventory inventory = InventoryUtils.createInventory(cs);

        if (inventory == null) {
            return;
        }

        Logger.info("Place2");


        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }

        Logger.info("Place3");

        if (!inventory.getItem(e.getSlot()).equals(item)) {
            return;
        }

        Logger.info("Place4");
        SpawnerData nextSpawnerData = SpawnerUtils.getNextSpawnerData(cs);
        if (nextSpawnerData == null) {
            return;
        }

        Logger.info("Place5");
        EntityType spawnedType = EntityType.fromName(nextSpawnerData.getSpawner_type());

        String hologramName = HologramUtils.getBlockHologramName(spawnerBlock);

        // todo check player balance

        if (!SpawnerUtils.upgradeSpawnerTo(spawnerBlock, spawnedType)) {
            return;
        }

        Logger.info("Place6");
        HologramUtils.removeHologram(hologramName);
        HologramUtils.createHologramForSpawner(spawnerBlock);


        player.closeInventory();
        player.sendMessage("§a(!) Spawner has been upgraded to " + SpawnerUtils.getSpawnerName(spawnedType));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 10F);
        player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 10, 1);
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            event.getEntity().setHealth(0);
        }
    }

}
