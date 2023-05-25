package me.emmetion.wells;

import me.emmetion.wells.database.Database;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Wells extends JavaPlugin {

    private Database database;

    @Override
    public void onEnable() {

        Logger logger = Logger.getLogger("Wells");
        logger.log(Level.INFO, "Plugin starting...");


        this.database = new Database();

        try {
            database.initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize database...");
        }

    }

}
