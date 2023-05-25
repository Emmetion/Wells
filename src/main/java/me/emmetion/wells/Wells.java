package me.emmetion.wells;

import me.emmetion.wells.database.Database;
import me.emmetion.wells.model.Well;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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


        Well fakeWell = new Well("Emmet's Town", new Location(Bukkit.getWorld("world"), 0,0,0), 0);

        try {
            this.database.createWell(fakeWell);
            Well wellByTownName = this.database.findWellByTownName("Emmet's Town");
            System.out.println("wellByTownName = " + wellByTownName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
