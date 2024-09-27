package me.chalmano.pixelSpawners.commands;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.data.SpawnersReader;
import me.chalmano.pixelSpawners.utils.InventoryUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PixelSpawnerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return reload(commandSender);
        }

        if (args[0].equalsIgnoreCase("menu")) {
            return menu(commandSender, args);
        }

        return false;
    }

    private static boolean menu(@NotNull CommandSender commandSender, String[] args) {
        String specifyPlayerMessage = "§e(!) You must specify a player.";
        String menuCouldNotBeCreatedMsg = "§6(!) Something went wrong, menu could not be created.";
        String playerNotFoundMsg = "§e(!) Player not found.";

        Player targetPlayer = null;

        if (!(commandSender instanceof Player player)) {
            if (args.length < 2) {
                commandSender.sendMessage(specifyPlayerMessage);
                return true;
            }
        } else {
            if (args.length < 2) {
                targetPlayer = player;
            } else {

                // e.g /ps menu Chalmano
                // if player has no permission, it will open menu for himself
                if (!player.hasPermission("pixelspawners.admin")) {
                    targetPlayer = player;
                }else{
                    targetPlayer = Bukkit.getPlayerExact(args[1]);
                }

            }
        }

        if (targetPlayer == null) {
            commandSender.sendMessage(playerNotFoundMsg);
            return true;
        }

        Inventory spawnerMenuInventory = InventoryUtils.createSpawnerMenuInventory();

        if (spawnerMenuInventory == null) {
            commandSender.sendMessage(menuCouldNotBeCreatedMsg);
            return true;
        }

        targetPlayer.openInventory(spawnerMenuInventory);

        return true;
    }

    private static boolean reload(@NotNull CommandSender commandSender) {
        TextComponent successMessage = Component.text("(!) Configurations have been successfully reloaded.").color(TextColor.color(60, 229, 40));
        if (!(commandSender instanceof Player player)) {
            PixelSpawners.getInstance().reloadConfig();
            SpawnersReader.getInstance().updateSpawnerDataListMap();
            commandSender.sendMessage(successMessage);
            return true;
        }

        TextComponent noPermMessage = Component.text("(!) Permission denied.").color(TextColor.color(255, 0, 40));

        if (!player.hasPermission("pixelspawners.reload")) {
            commandSender.sendMessage(noPermMessage);
            return true;
        }

        PixelSpawners.getInstance().reloadConfig();
        SpawnersReader.getInstance().reloadSpawnerData();
        player.sendMessage(successMessage);
        return true;
    }


}
