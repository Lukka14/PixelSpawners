package me.chalmano.pixelSpawners.events;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.inventory.SpawnerInventory;
import me.chalmano.pixelSpawners.models.Drop;
import me.chalmano.pixelSpawners.models.SpawnerData;
import me.chalmano.pixelSpawners.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.awt.*;
import java.util.*;
import java.util.List;

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
        if (spawnerDataFor != null) {
            int spawnTime = spawnerDataFor.getSpawn_time();
            SpawnerUtils.setSpawnTime(cs, spawnTime);
        }

        // create hologram
        if (spawnerDataFor == null) {
            if (PixelSpawners.getInstance().getConfig().getBoolean("all-spawner-holograms")) {
                HologramUtils.createHologramForSpawner(blockPlaced);
            }
        } else {
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

//        CreatureSpawner creatureSpawner = (CreatureSpawner) clickedBlock.getState();

        Player player = e.getPlayer();

        // check if player has permission to right-click the item
        Optional<Island> islandOptional = BentoBox.getInstance().getIslands().getIslandAt(clickedBlock.getLocation());
        if (!SkyBlockUtils.isAllowed(player, islandOptional, Flags.BREAK_BLOCKS)) {
            return;
        }

        Inventory inventory = InventoryUtils.createUpgradeInventory(clickedBlock);

        if (inventory == null) {
            return;
        }

        player.openInventory(inventory);
        clickedSpawnerMap.put(player, clickedBlock);

        e.setCancelled(true);
    }


    @EventHandler
    public void onItemClick(InventoryClickEvent e) {

        Inventory currentInventory = e.getInventory();
        if (!(currentInventory.getHolder(false) instanceof SpawnerInventory)) {
            return;
        }
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        Block spawnerBlock = clickedSpawnerMap.get(player);

        if (spawnerBlock == null) {
            return;
        }

        Inventory inventory = InventoryUtils.createUpgradeInventory(spawnerBlock);

        if (inventory == null) {
            return;
        }


        ItemStack clickedItem = e.getCurrentItem();

        boolean upgradeItemClicked = itemWasClicked(InventoryUtils.createUpgradeItem(spawnerBlock), clickedItem);
        boolean downgradeItemClicked = itemWasClicked(InventoryUtils.createDowngradeItem(spawnerBlock), clickedItem);
        boolean menuItemClicked = itemWasClicked(InventoryUtils.createMenuItem(), clickedItem);

        int currentPageNumber = InventoryUtils.getCurrentPageNumber(currentInventory);

        boolean nextPageItemClicked = itemWasClicked(InventoryUtils.createNextPageItem(currentPageNumber), clickedItem);
        boolean prevPageItemClicked = itemWasClicked(InventoryUtils.createPrevPageItem(currentPageNumber), clickedItem);

        // -- if one of the items are clicked then we continue
        if (!upgradeItemClicked && !downgradeItemClicked && !menuItemClicked && !nextPageItemClicked
        && !prevPageItemClicked) {
            return;
        }

        boolean downgraded = false;
        boolean upgraded = false;

        if (upgradeItemClicked) {
            upgraded = upgradeItemClicked(player, spawnerBlock);
        }

        if (downgradeItemClicked) {
            downgraded = downgradeItemClicked(player, spawnerBlock);
        }

        if (menuItemClicked){
            Integer indexForSpawnerData = SpawnerUtils.getIndexFor(spawnerBlock);
            int menuPageIndex = indexForSpawnerData == null ? 0 : indexForSpawnerData;

            Inventory spawnerMenuInventory = InventoryUtils.createSpawnerMenuInventory(menuPageIndex);

            if (spawnerMenuInventory == null) {
                PixelSpawners.getInstance().getLogger().warning("Could not create Spawner Menu Inventory.");
                player.closeInventory();
                return;
            }
            player.openInventory(spawnerMenuInventory);
            return;
        }

        if (nextPageItemClicked) {
            Inventory spawnerMenuInventory = InventoryUtils.createSpawnerMenuInventory(currentPageNumber);
            if (spawnerMenuInventory != null) {
                player.openInventory(spawnerMenuInventory);
            }
            return;
        }

        if (prevPageItemClicked) {
            Inventory spawnerMenuInventory = InventoryUtils.createSpawnerMenuInventory(currentPageNumber-2);
            if (spawnerMenuInventory != null) {
                player.openInventory(spawnerMenuInventory);
            }
            return;
        }

        // no upgraded, no downgraded, skip sound and particels
        if(!upgraded && !downgraded){
            return;
        }

        Location spawnerCenterLocation = spawnerBlock.getLocation().toCenterLocation();

        Sound sound = Sound.valueOf(PixelSpawners.getInstance().getConfig().getString("upgrade-sound"));
        Particle particle = Particle.valueOf(PixelSpawners.getInstance().getConfig().getString("upgrade-particle"));
        player.getWorld().playSound(spawnerCenterLocation, sound, 10F, 1F);
        player.getWorld().spawnParticle(particle, spawnerCenterLocation, 100);
    }

    boolean itemWasClicked(ItemStack itemStack, ItemStack clickedItem) {
        if (itemStack == null) {
            return false;
        }

        return itemStack.equals(clickedItem);
    }

    public boolean upgradeItemClicked(Player player, Block spawnerBlock) {
        SpawnerData nextSpawnerData = SpawnerUtils.nextSpawnerData((CreatureSpawner) spawnerBlock.getState());
        if (nextSpawnerData == null) {
            return false;
        }

        EntityType spawnedType = EntityType.fromName(nextSpawnerData.getSpawner_type());
        String hologramName = HologramUtils.getBlockHologramName(spawnerBlock);

        if (!executeBuying(player, spawnerBlock, nextSpawnerData, spawnedType)){
            return false;
        }

        HologramUtils.removeHologram(hologramName);
        HologramUtils.createHologramForSpawner(spawnerBlock);

        player.closeInventory();
        player.sendMessage("§a(!) Spawner has been upgraded to " + SpawnerUtils.getSpawnerName(spawnedType));
        return true;
    }

    // true - successful
    // false - unsuccessful
    private static boolean executeBuying(Player player, Block spawnerBlock, SpawnerData nextSpawnerData, EntityType spawnedType) {

        if(SpawnerUtils.spawnerBlockPersistentDataContainsSpawnType(spawnerBlock,nextSpawnerData)){
            return SpawnerUtils.changeSpawnerTo(spawnerBlock, spawnedType);
        }

        Economy economy = PixelSpawners.getEconomy();
        double playBalance = economy.getBalance(player);

        if (playBalance < nextSpawnerData.getPrice()) {
            player.sendMessage("§c(!) You don't have enough balance!");
            player.closeInventory();
            return false;
        }

        if (!SpawnerUtils.changeSpawnerTo(spawnerBlock, spawnedType)) {
            return false;
        }
        economy.withdrawPlayer(player, nextSpawnerData.getPrice());
        return true;
    }

    public boolean downgradeItemClicked(Player player, Block spawnerBlock) {
        SpawnerData previousSpawnerData = SpawnerUtils.previousSpawnerData((CreatureSpawner) spawnerBlock.getState());
        if (previousSpawnerData == null) {
            return false;
        }

        EntityType spawnedType = EntityType.fromName(previousSpawnerData.getSpawner_type());
        String hologramName = HologramUtils.getBlockHologramName(spawnerBlock);

        if (!SpawnerUtils.changeSpawnerTo(spawnerBlock, spawnedType)) {
            return false;
        }

        HologramUtils.removeHologram(hologramName);
        HologramUtils.createHologramForSpawner(spawnerBlock);

        player.closeInventory();
        player.sendMessage("§e(!) Spawner has been downgraded to " + SpawnerUtils.getSpawnerName(spawnedType));
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {

        if (SpawnerUtils.getSpawnerDataFor(event.getEntityType()) == null) {
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

        if (spawnerDataFor == null) {
            PixelSpawners.getInstance().getLogger().warning("Spawner data for " + e.getEntityType().name() + " was not found, Entity won't drop custom drops.");
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

        e.setDroppedExp(spawnerDataFor.getXp_drop());
    }

}
