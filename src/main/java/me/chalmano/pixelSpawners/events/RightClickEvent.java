package me.chalmano.pixelSpawners.events;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.ItemHologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class RightClickEvent implements Listener {

    private static ItemStack item = null;

    private static Map<Player, CreatureSpawner> clickedSpawnerMap = new HashMap<>();

    public static ItemStack getItem() {

        if (item != null) {
            return item;
        }

        ItemStack item = new ItemStack(Material.SQUID_SPAWN_EGG);
        item.getItemMeta().displayName(Component.text("Upgrade Spawner").color(TextColor.color(0x40F70C)));

        return RightClickEvent.item = item;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e){

        Block blockPlaced = e.getBlockPlaced();
        if (blockPlaced.getType() != Material.SPAWNER) {
            return;
        }

        Plugin plugin = PixelSpawners.getInstance(); // Your plugin's instance
        Location where = blockPlaced.getLocation(); // The location where the hologram will be placed
        HolographicDisplaysAPI api = HolographicDisplaysAPI.get(plugin); // The API instance for your plugin
        Hologram hologram = api.createHologram(where);

        CreatureSpawner creatureSpawner = (CreatureSpawner) blockPlaced.getState();

        TextHologramLine textLine = hologram.getLines().appendText("§a"+creatureSpawner.getSpawnedType().name()+" Spawner");

//        TextHologramLine textLine1 = hologram.getLines().appendText("...");
//        TextHologramLine textLine2 = hologram.getLines().insertText(0, "...");

        ItemHologramLine itemLine1 = hologram.getLines().appendItem(new ItemStack(Material.STONE));
//        ItemHologramLine itemLine2 = hologram.getLines().insertItem(0, new ItemStack(Material.STONE));

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
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, Component.text("Upgrade Spawner"));

        inventory.setItem(13, getItem());

        player.openInventory(inventory);
        clickedSpawnerMap.put(player, creatureSpawner);
    }




    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();

        if (!getItem().equals(item)) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        CreatureSpawner cs = clickedSpawnerMap.get(player);
        cs.setSpawnedType(EntityType.fromId(cs.getSpawnedType().getTypeId() + 1));
        cs.update();
        cs.setDelay(100);

        e.setCancelled(true);
        player.closeInventory();
        player.sendMessage("§a(!) Spawner has been upgraded to "+cs.getSpawnedType().name());
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            event.getEntity().setHealth(0);
        }
    }


}
