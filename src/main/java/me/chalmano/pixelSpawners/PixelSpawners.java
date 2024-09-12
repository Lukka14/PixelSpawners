package me.chalmano.pixelSpawners;

import me.chalmano.pixelSpawners.events.RightClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PixelSpawners extends JavaPlugin {

    private static PixelSpawners instance;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        // Plugin startup logic


        this.getLogger().info("§aPixelSpawners plugin enabled!");

        this.getServer().getPluginManager().registerEvents(new RightClickEvent(),this);
    }

    public PixelSpawners(){
        instance = this;
    }

    public static PixelSpawners getInstance() {
        return instance;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
