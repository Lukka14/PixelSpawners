package me.chalmano.pixelSpawners;

import lombok.Getter;
import me.chalmano.pixelSpawners.commands.ReloadCommand;
import me.chalmano.pixelSpawners.commands.ReloadTabCompleter;
import me.chalmano.pixelSpawners.events.SpawnerEvents;
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


        this.getCommand("pixelspawners").setExecutor(new ReloadCommand());
        this.getCommand("pixelspawners").setTabCompleter(new ReloadTabCompleter());

        this.getServer().getPluginManager().registerEvents(new SpawnerEvents(), this);

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
