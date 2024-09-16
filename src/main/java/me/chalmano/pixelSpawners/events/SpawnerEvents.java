package me.chalmano.pixelSpawners.events;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.inventory.SpawnerInventory;
import me.chalmano.pixelSpawners.models.Drop;
import me.chalmano.pixelSpawners.models.SpawnerData;
import me.chalmano.pixelSpawners.utils.HologramUtils;
import me.chalmano.pixelSpawners.utils.InventoryUtils;
import me.chalmano.pixelSpawners.utils.Logger;
import me.chalmano.pixelSpawners.utils.SpawnerUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SpawnerEvents implements Listener {

    private static final Map<Player, Block> clickedSpawnerMap = new HashMap<>();

    private final Set<Entity> spawnerEntities = new HashSet<>();

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        if (blockPlaced.getType() != Material.SPAWNER) {
            return;
        }

        CreatureSpawner cs = (CreatureSpawner) blockPlaced.getState();
        SpawnerData spawnerDataFor = SpawnerUtils.getSpawnerDataFor(cs);
        if(spawnerDataFor != null) {
            int spawnTime = spawnerDataFor.getSpawn_time();
            SpawnerUtils.setSpawnTime(cs, spawnTime);
        }

        // create hologram
        if(spawnerDataFor == null){
            if(PixelSpawners.getInstance().getConfig().getBoolean("all-spawner-holograms")){
                HologramUtils.createHologramForSpawner(blockPlaced);
            }
        }else{
            HologramUtils.createHologramForSpawner(blockPlaced);
        }

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

        if (!(e.getInventory().getHolder(false) instanceof SpawnerInventory)) {
            return;
        }
        e.setCancelled(true);

//        ItemStack item = e.getCurrentItem();
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


        if (!inventory.getItem(InventoryUtils.UPGRADE_ITEM_INDEX).equals(e.getCurrentItem())) {
            return;
        }
//        // check if the 'upgrade item' is clicked
//        if (!inventory.getItem(e.getSlot()).equals(item)) {
//            return;
//        }

        Logger.info("Place4");
        SpawnerData nextSpawnerData = SpawnerUtils.getNextSpawnerData(cs);
        if (nextSpawnerData == null) {
            return;
        }

        Logger.info("Place5");
        EntityType spawnedType = EntityType.fromName(nextSpawnerData.getSpawner_type());

        String hologramName = HologramUtils.getBlockHologramName(spawnerBlock);

        Economy economy = PixelSpawners.getEconomy();
        double playBalance = economy.getBalance(player);

        if (playBalance < nextSpawnerData.getPrice()) {
            player.sendMessage("§c(!) You don't have enough balance!");
            player.closeInventory();
            return;
        }

        if (!SpawnerUtils.upgradeSpawnerTo(spawnerBlock, spawnedType)) {
            return;
        }
        economy.withdrawPlayer(player, nextSpawnerData.getPrice());

        Logger.info("Place6");
        HologramUtils.removeHologram(hologramName);
        HologramUtils.createHologramForSpawner(spawnerBlock);

        player.closeInventory();
        player.sendMessage("§a(!) Spawner has been upgraded to " + SpawnerUtils.getSpawnerName(spawnedType));

        Location spawnerCenterLocation = spawnerBlock.getLocation().toCenterLocation();

        Sound sound = Sound.valueOf(PixelSpawners.getInstance().getConfig().getString("upgrade-sound"));
        Particle particle = Particle.valueOf(PixelSpawners.getInstance().getConfig().getString("upgrade-particle"));
        player.getWorld().playSound(spawnerCenterLocation, sound, 10F, 1F);
        player.getWorld().spawnParticle(particle, spawnerCenterLocation , 100);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {

        if(SpawnerUtils.getSpawnerDataFor(event.getEntityType()) == null){
            return;
        }

        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            return;
        }

        spawnerEntities.add(event.getEntity());
//            PixelSpawners.getInstance().getLogger().info("Killing: "+event.getEntityType());
        event.getEntity().setHealth(0);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {

        if (!spawnerEntities.remove(e.getEntity())) {
            return;
        }

        SpawnerData spawnerDataFor = SpawnerUtils.getSpawnerDataFor(e.getEntityType());

        if(spawnerDataFor == null) {
            PixelSpawners.getInstance().getLogger().warning("Spawner data for "+e.getEntityType().name()+" was not found, Entity won't drop custom drops.");
            return;
        }

        List<Drop> dropList = spawnerDataFor.getDrops();

        List<ItemStack> dropItems = new ArrayList<>();

        for (Drop drop : dropList) {
            if (Math.random() < drop.getChance()) {
                dropItems.add(new ItemStack(Material.valueOf(drop.getItem()), (drop.getAmount())));
            }
        }

        e.getDrops().clear();
        e.getDrops().addAll(dropItems);
    }

}
