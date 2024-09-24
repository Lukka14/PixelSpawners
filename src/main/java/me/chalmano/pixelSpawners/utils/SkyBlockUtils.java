package me.chalmano.pixelSpawners.utils;

import org.bukkit.entity.Player;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class SkyBlockUtils {

    // Warning, only returns true if island is found and flag is allowed for the player.
    public static boolean isAllowed(Player player, Optional<Island> islandOptional, Flag flag) {

        if (player.hasPermission("pixelspawners.bypass")) {
            return true;
        }

        return islandOptional.filter(island -> !island.isAllowed(User.getInstance(player), flag)).isPresent();
    }

}
