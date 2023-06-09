package me.emmetion.wells;

import com.palmergames.bukkit.towny.TownyAPI;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.emmetion.wells.commands.WellCommand;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.listeners.WellBuffListener;
import me.emmetion.wells.listeners.WellListener;
import me.emmetion.wells.listeners.WellPlayerListener;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Wells extends JavaPlugin {

    public static Wells plugin;

    private WellManager wellManager;

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


        // Console startup debug.
//        boolean b = this.wellManager.wellExistsByTownName("Emmet's Town");
//        System.out.println("FakeWell present: " + b);
//
//        boolean connected = this.wellManager.isConnected();
//        System.out.println("isConnected(): " + connected);

        initCommands();
        initListeners();
        initSchedules();
        initWellHolograms();
    }

    // leftover code snippets
    // Well fakeWell = new Well("Emmet's Town", new Location(Bukkit.getWorld("world"), 0,0,0), 0);


    @Override
    public void onDisable() {
        try {
            this.wellManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was an error closing the database connection!");
        }

        deleteWellHolograms();
    }


    public WellManager getWellManager() {
        return this.wellManager;
    }

    private void initCommands() {
        this.getCommand("wells").setExecutor(new WellCommand(wellManager));
        System.out.println("Commands have been successfully enabled.");
    }

    private void initListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new WellListener(wellManager), this);
        pluginManager.registerEvents(new WellBuffListener(wellManager), this);
        pluginManager.registerEvents(new WellPlayerListener(wellManager), this);
    }


    private HashMap<Player, String> playersNearWell = new HashMap<>();
    private HashMap<Player, Integer> nearWellMessageCooldown = new HashMap<>();

    private void initSchedules() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskTimer(this, () -> {

            List<Player> currentWellPlayers = new ArrayList<>();

            for (Well w : this.wellManager.getWells()) {
                Location location = w.getLocation();
                Collection<Player> nearbyPlayers = location.getNearbyPlayers(10);

                for (Player p : nearbyPlayers) {

                    WellPlayer wellPlayer = wellManager.getWellPlayer(p);

                    if (playersNearWell.containsKey(p)) { // if player was already near a well
                        if (!playersNearWell.get(p).equals(w.getWellName())) { // and the well the player is currently at is different. (maybe tp, other reasons idk)

                            w.removeNearbyPlayer(wellPlayer); // remove from well's collection.
                            this.playersNearWell.remove(p); // remove from the list
                        }
                    }

                    currentWellPlayers.add(p); // add the player to the list.
                    playersNearWell.put(p, w.getWellName());
                    w.addNearbyPlayer(wellPlayer);

                    // "Town's Well"
                    Hologram hologram = DHAPI.getHologram(w.getWellName()); // gets the hologram from hashmap.
                    if (hologram == null) {
                        hologram = createWellHologram(w);
                    }
                    hologram.setShowPlayer(p); // displays the well hologram to the player.

                    if (nearWellMessageCooldown.containsKey(p)) {
                        if (nearWellMessageCooldown.get(p) == -1) { // removes cooldown if time is at -1.
                            nearWellMessageCooldown.remove(p);
                        } else {
                            nearWellMessageCooldown.put(p, nearWellMessageCooldown.get(p) - 1); // count down every second.
                        }
                    } else {
                        if (wellManager.isDebug())
                            p.sendMessage("☢ You are near a well! [" + w.getWellName() + "]");
                        nearWellMessageCooldown.put(p, 5); // -1 on cooldown each second.
                    }
                }
            }


            List<Player> notNearWells = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !currentWellPlayers.contains(p))
                    .collect(Collectors.toList());

            for (Player p : notNearWells) {

                WellPlayer wp = wellManager.getWellPlayer(p);

                if (this.playersNearWell.containsKey(p)) {
                    String wellName = this.playersNearWell.get(p);
                    Hologram h = DHAPI.getHologram(wellName);
                    h.removeShowPlayer(p);

                    Well wellByWellName = wellManager.getWellByWellName(wellName);

                    wellByWellName.removeNearbyPlayer(wp);

                    h.hide(p);
                    if (wellManager.isDebug())
                        p.sendMessage("☢ You are no longer near a well. [" + wellName + "]");

                    this.playersNearWell.remove(p);
                }
            }



        }, 20, 20);

        scheduler.runTaskTimer(this, () -> {
            this.wellManager.updateDatabase();
        }, 1, 300);
    }



    public void initWellHolograms() {
        for (Well w : wellManager.getWells()) {
            w.updateHologram();
        }
    }



    private Hologram createWellHologram(Well well) {
        if (DHAPI.getHologram(well.getWellName()) != null) {
            DHAPI.removeHologram(well.getWellName());
        }
        // now we create it knowing it doesn't exist.

        String wellName = well.getWellName();
        Location location = well.getHologramLocation();
        boolean saveToFile = false;
        List<String> lines = Arrays.asList(well.getWellName(), well.prettyPosition(), ChatColor.YELLOW + "Level: " + well.getWellLevel());

        Hologram hologram = DHAPI.createHologram(wellName, location, saveToFile, lines);
        hologram.setDefaultVisibleState(false);

        return hologram;

    }

    private void deleteWellHolograms() {
        this.wellManager.getWells().stream()
                .forEach(well -> DHAPI.removeHologram(well.getWellName()));
    }

}
