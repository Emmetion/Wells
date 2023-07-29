package me.emmetion.wells;

import com.palmergames.bukkit.towny.TownyAPI;
import eu.decentsoftware.holograms.api.DHAPI;
import me.emmetion.wells.commands.WellCommand;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.creature.SpawnNPC;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.listeners.*;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.runnables.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.emmetion.wells.util.Utilities.getColor;

public final class Wells extends JavaPlugin {

    public static Wells plugin;

    private Configuration configuration;

    private ActiveBuffRunnable activeBuffRunnable;
    private WellCreatureRunnable wellCreatureRunnable;
    private NearWellRunnable nearWellRunnable;
    private UpdateDatabaseRunnable updateDatabaseRunnable;

    private WellManager wellManager;
    private CreatureManager creatureManager;

    @Override
    public void onEnable() {
        plugin = this;

        configuration = Configuration.getInstance();

        // loads config from disc
        reloadConfig();


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

        // Prints into chat.
        Bukkit.broadcastMessage(getColor("&c&lReload! &fWell's has been reloaded!"));

        handleSpawnNPCSpawning();
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

        configuration.saveConfigFile();
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

    // Runnables are different than animations in the fact they are always running while the server is online.
    // Animations are endable, while here we run these and never stop them until plugin shutdown.
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

    // W
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
        if (this.creatureManager == null)
            return;
        this.creatureManager.saveCreatures();
    }

    private void handleSpawnNPCSpawning() {
        Logger logger = getLogger();
        Player p = Bukkit.getPlayer("Emmetion");
        if (this.creatureManager.isSpawnNPCSpawned()) {
            UUID spawnNPCUUID = configuration.getSpawnNPCUUID();
            p.sendMessage(getColor("&c[NPC:<SpawnNPC>] &fwas not spawned. Spawning..."));
            if (spawnNPCUUID == null) {
                logger.info("UUID not found in config.");
                Bukkit.broadcast(Component.text(getColor("Creature is already spawned, but SpawnNPCUUID is null from configuration.")));
                return;
            }


        } else {
            // spawn the npc.
            WellCreature wellCreature = creatureManager.spawnCreature(SpawnNPC.class, null);
            if (wellCreature == null) {
                // in the case that something went wrong while spawning it, we ignore it.
                return;
            }

            UUID uuid = wellCreature.getUUID(); // get uuid from creature, should already be assigned in the WellCreature.
            configuration.setSpawnNPCUUID(uuid);
        }
    }





}
