package me.emmetion.wells;

import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.SQLite;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Wells extends JavaPlugin {


    private Database database;

    @Override
    public void onEnable() {
        this.database = new SQLite(this);
        this.database.load();
        try {

            FileConfiguration config = this.getConfig();
            this.getConfig().addDefault("SQLite.Filename", "Wells");
            this.saveDefaultConfig();
            String db_name = config.getString("SQLite.Filename");

            System.out.println("db_name = " + db_name);

            this.getConfig().load("plugin.yml");

        } catch (IOException | InvalidConfigurationException e) {
            // catch
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Database getDatabase() {
        return this.database;
    }

}
