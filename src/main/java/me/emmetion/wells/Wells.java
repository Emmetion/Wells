package me.emmetion.wells;

import com.palmergames.bukkit.towny.TownyAPI;
import eu.decentsoftware.holograms.api.DHAPI;
import me.emmetion.wells.commands.WellCommand;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.listeners.*;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.runnables.ActiveBuffRunnable;
import me.emmetion.wells.runnables.NearWellRunnable;
import me.emmetion.wells.runnables.UpdateDatabaseRunnable;
import me.emmetion.wells.runnables.WellCreatureRunnable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.emmetion.wells.util.Utilities.getColor;

public final class Wells extends JavaPlugin {

    public static Wells plugin;

    private ActiveBuffRunnable activeBuffRunnable;
    private WellCreatureRunnable wellCreatureRunnable;
    private NearWellRunnable nearWellRunnable;
    private UpdateDatabaseRunnable updateDatabaseRunnable;

    private WellManager wellManager;
    private CreatureManager creatureManager;

    @Override
    public void onEnable() {

        this.plugin = this;

        Logger logger = Logger.getLogger("Wells");
        logger.log(Level.INFO, "Plugin starting...");

        // Checks whether TownyAPI instance was found and if not, turns off plugin.
        TownyAPI instance = TownyAPI.getInstance();
        if (instance == null) {
            System.out.println("Disabling because townyapi was not found.");
            this.setEnabled(false);
            return;
        }
        System.out.println("DEBUG: townyapi was found... continuing...");

        // Checks whether MySQL connection was successful and if not, turns off plugin.
        this.wellManager = new WellManager();
        if (!wellManager.isConnected()) {
            System.out.println("Failed connection to the wellmanager.");
            this.setEnabled(false);
            return;
        }

        this.creatureManager = new CreatureManager();

        initCommands();
        initListeners();
        initSchedules();
        initWellHolograms();

        Bukkit.broadcastMessage(getColor("&c&lReload! &fWell's has been reloaded!"));
    }

    @Override
    public void onDisable() {
        try {
            this.wellManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an error closing the database connection!");
        }

        deleteWellHolograms();
        deleteCreatures();
    }


    public WellManager getWellManager() {
        return this.wellManager;
    }

    public CreatureManager getCreatureManager() {
        return this.creatureManager;
    }

    // Helper methods for startup/shutdown.

    private void initCommands() {
        this.getCommand("wells").setExecutor(new WellCommand(wellManager, creatureManager));
        System.out.println("Commands have been successfully enabled.");
    }

    private void initListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new WellListener(wellManager), this);
        pluginManager.registerEvents(new MenuListener(wellManager), this);
        pluginManager.registerEvents(new WellBuffListener(wellManager), this);
        pluginManager.registerEvents(new WellPlayerListener(wellManager), this);
        pluginManager.registerEvents(new WellCreatureListener(creatureManager), this);
    }

    private void initSchedules() {

        // Locates nearby well players.
        nearWellRunnable = new NearWellRunnable(wellManager);
        nearWellRunnable.runTaskTimer(Wells.plugin, 1, 1);

        // Updates the wells database every 5 minutes.
        updateDatabaseRunnable = new UpdateDatabaseRunnable(wellManager);
        updateDatabaseRunnable.runTaskTimer(Wells.plugin, 1, 3000);

        // Creates the ActiveBuffRunnable.
        activeBuffRunnable = new ActiveBuffRunnable(this);
        activeBuffRunnable.runTaskTimer(Wells.plugin, 1, 1);

        // Creatres the WellCreatureRunnable
        wellCreatureRunnable = new WellCreatureRunnable(this, wellManager, creatureManager);
        wellCreatureRunnable.runTaskTimer(Wells.plugin, 1, 1);
    }

    public void initWellHolograms() {
        for (Well w : wellManager.getWells()) {
            w.updateHologram();
        }
    }


    private void deleteWellHolograms() {
        this.wellManager.getWells().stream()
                .forEach(well -> DHAPI.removeHologram(well.getWellName()));
    }

    private void deleteCreatures() {
        this.creatureManager.saveCreatures();
    }

}
