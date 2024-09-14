package me.chalmano.pixelSpawners;

import lombok.Getter;
import me.chalmano.pixelSpawners.commands.ReloadCommand;
import me.chalmano.pixelSpawners.commands.ReloadTabCompleter;
import me.chalmano.pixelSpawners.events.SpawnerEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class PixelSpawners extends JavaPlugin {

    @Getter
    private static PixelSpawners instance;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();

        this.getLogger().info("PixelSpawners plugin enabled!");

        this.getCommand("pixelspawners").setExecutor(new ReloadCommand());
        this.getCommand("pixelspawners").setTabCompleter(new ReloadTabCompleter());

        this.getServer().getPluginManager().registerEvents(new SpawnerEvents(),this);
    }

    public PixelSpawners(){
        instance = this;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
