package me.chalmano.pixelSpawners.commands;

import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.data.SpawnersReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(args.length != 1) {
            return false;
        }

        if(!args[0].equalsIgnoreCase("reload")) {
            return false;
        }

        TextComponent successMessage = Component.text("(!) Configurations have been successfully reloaded.").color(TextColor.color(60, 229, 40));
        if (!(commandSender instanceof Player player)) {
            PixelSpawners.getInstance().reloadConfig();
            SpawnersReader.getInstance().updateSpawnerData();
            commandSender.sendMessage(successMessage);
            return true;
        }

        TextComponent noPermMessage = Component.text("(!) Permission denied.").color(TextColor.color(255, 0, 40));

        if (!player.hasPermission("pixelspawners.reload")) {
            commandSender.sendMessage(noPermMessage);
            return true;
        }

        PixelSpawners.getInstance().reloadConfig();
        SpawnersReader.getInstance().updateSpawnerData();
        player.sendMessage(successMessage);
        return true;
    }


}
