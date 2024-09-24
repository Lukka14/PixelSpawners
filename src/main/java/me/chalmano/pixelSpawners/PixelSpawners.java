package me.chalmano.pixelSpawners;

import com.jeff_media.customblockdata.CustomBlockData;
import lombok.Getter;
import me.chalmano.pixelSpawners.commands.PixelSpawnerCommand;
import me.chalmano.pixelSpawners.commands.CommandTabCompleter;
import me.chalmano.pixelSpawners.events.SpawnerEvents;
import me.chalmano.pixelSpawners.utils.SpawnerUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class PixelSpawners extends JavaPlugin {

    @Getter
    private static PixelSpawners instance;

    private static Economy econ = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        this.getCommand("pixelspawners").setExecutor(new PixelSpawnerCommand());
        this.getCommand("pixelspawners").setTabCompleter(new CommandTabCompleter());

        this.getServer().getPluginManager().registerEvents(new SpawnerEvents(), this);

        //todo TEST, behavior is unknown
        CustomBlockData.registerListener(this);
        this.getLogger().info("PixelSpawners plugin enabled!");
    }

    public PixelSpawners() {
        instance = this;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

}
