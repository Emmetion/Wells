package me.emmetion.wells;

import com.palmergames.bukkit.towny.TownyAPI;
import eu.decentsoftware.holograms.api.DHAPI;
import me.emmetion.wells.commands.WellCommand;
import me.emmetion.wells.commands.WellCompleter;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.creature.factories.SpawnNPCFactory;
import me.emmetion.wells.listeners.*;
import me.emmetion.wells.managers.CreatureManager;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.runnables.ActiveBuffRunnable;
import me.emmetion.wells.runnables.NearWellRunnable;
import me.emmetion.wells.runnables.UpdateDatabaseRunnable;
import me.emmetion.wells.runnables.WellCreatureRunnable;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.emmetion.wells.util.Utilities.getColor;

public final class Wells extends JavaPlugin {

    public static Wells plugin;

    private final Logger logger = Logger.getLogger("Wells");

    private Configuration configuration;

    private WellCreatureRunnable wellCreatureRunnable;
    private NearWellRunnable nearWellRunnable;
    private UpdateDatabaseRunnable updateDatabaseRunnable;
    private ActiveBuffRunnable activeBuffRunnable;

    private WellManager wellManager;

    private CreatureManager creatureManager;

    @Override
    public void onEnable() {
        plugin = this;

        configuration = Configuration.getInstance();

        // loads config from disc
        reloadConfig();

        logger.log(Level.INFO, "Plugin starting...");


        creatureManager = new CreatureManager();

        // Checks whether TownyAPI instance was found and if not, turns off plugin.
        TownyAPI instance = TownyAPI.getInstance();
        if (instance == null) {
            logger.info("Failed to find TownyAPI, disabling Wells.");
            this.setEnabled(false);
            return;
        }

        // Checks whether MySQL connection was successful and if not, turns off plugin.
        wellManager = new WellManager();
        if (!wellManager.isConnected()) {
            logger.info("Failed to connect to " + wellManager.getDatabaseType() + ", disabling Wells.");
            this.setEnabled(false);
            return;
        }

        initCommands();
        initListeners();
        initSchedules();
        initWellHolograms();

        // Prints into chat.
        Bukkit.broadcast(Component.text(getColor("&c&lReload! &fWell's has been reloaded!")));
//        Bukkit.getScheduler().runTaskLater(this, this::handleSpawnNPCSpawning, 40);
    }

    @Override
    public void onDisable() {

        deleteWellHolograms();
        deleteCreatures();

        try {
            if (this.wellManager != null)
                this.wellManager.close();
            // We assign the wellManager to null, preventing any other edits of the WellManager until the plugin is loaded again.
            this.wellManager = null;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an error closing the database connection!");
        }


        configuration.saveConfigFile();
    }

    @NotNull
    public WellManager getWellManager() {
        return this.wellManager;
    }

    @NotNull
    public CreatureManager getCreatureManager() {
        return this.creatureManager;
    }

    // Helper methods for startup/shutdown.

    private void initCommands() {
        WellCommand cmd = new WellCommand(wellManager, creatureManager);
        this.getCommand("wells").setExecutor(cmd);
        this.getCommand("wells").setTabCompleter(new WellCompleter());
    }

    private void initListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new MenuListener(), this);
        pluginManager.registerEvents(new WellListener(wellManager), this);
        pluginManager.registerEvents(new WellBuffListener(wellManager), this);
        pluginManager.registerEvents(new WellPlayerListener(wellManager), this);
        pluginManager.registerEvents(new WellCreatureListener(creatureManager), this);
        pluginManager.registerEvents(new SpawnNPCListeners(), this);
    }

    // Runnable are different from animations in the fact they are always running while the server is online.
    // Animations are endable, while here we run these and never stop them until plugin shutdown.
    private void initSchedules() {

        // Locates nearby well players.
        nearWellRunnable = new NearWellRunnable(wellManager);
        nearWellRunnable.runTaskTimer(Wells.plugin, 0, 5);

        // Updates the wells database every 5 minutes.
        updateDatabaseRunnable = new UpdateDatabaseRunnable(wellManager);
        updateDatabaseRunnable.runTaskTimer(Wells.plugin, 0, 3000);

        // Creates the ActiveBuffRunnable.
        activeBuffRunnable = new ActiveBuffRunnable(this);
        activeBuffRunnable.runTaskTimer(Wells.plugin, 0, 5);

        // Creates the WellCreatureRunnable
        wellCreatureRunnable = new WellCreatureRunnable(this, wellManager, creatureManager);
        wellCreatureRunnable.runTaskTimer(Wells.plugin, 0, 1);
    }

    // W
    public void initWellHolograms() {
        for (Well w : wellManager.getWells()) {
            w.updateHologram();
        }
    }


    private void deleteWellHolograms() {
        if (this.wellManager == null)
            return;

        this.wellManager.getWells().forEach(well -> DHAPI.removeHologram(well.getWellName()));
    }

    private void deleteCreatures() {
        this.creatureManager.saveCreatures();
    }

    private void handleSpawnNPCSpawning() {
        WellCreature wellCreature = creatureManager.spawnCreature(new SpawnNPCFactory(creatureManager), null, Configuration.getInstance().getSpawnNPCLocation());
        if (wellCreature == null) {
            Utilities.sendDebugMessage("Failed to spawn SpawnNPC.");
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }


}
