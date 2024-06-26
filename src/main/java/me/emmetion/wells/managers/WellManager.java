package me.emmetion.wells.managers;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.database.DatabaseType;
import me.emmetion.wells.database.EDatabase;
import me.emmetion.wells.database.SQLDatabase;
import me.emmetion.wells.database.YAMLDatabase;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.observer.LevelUpWellListener;
import me.emmetion.wells.observer.XPIncrementWellListener;
import me.emmetion.wells.util.Utilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

import static me.emmetion.wells.util.Utilities.getColor;

/**
 * WellManager.java, operates as a wrapper for EDatabase's and removes a layer of commands we otherwise would have to code.
 */
public final class WellManager {

    /**
     * MySQL database connection. This wrapper class uses methods inside database to achieve its goal.
     */
    private EDatabase database;

    /**
     * Well Hash Map, contains every well by their respective townname as a key.
     */
    @NotNull
    private HashMap<String, Well> wellHashMap = new HashMap<>();

    /**
     * A set of locations well's currently inhabit, use this to quickly find well blocks.
     */
    private final HashSet<Location> wellCache = new HashSet<>();

    // Observers
    private final List<LevelUpWellListener> levelupObservers = new ArrayList<>();
    private final List<XPIncrementWellListener> xpincrementObservers = new ArrayList<>();

    private HashMap<UUID, WellPlayer> wellPlayerHashMap = new HashMap<>();

    /**
     * Used in isConnected(), returns whether the SQL Server was successfully connected and parsed.
     */
    private boolean connected;

    /**
     * Used to send messages to plugin developer, toggled via WellsCommand.
     */
    private boolean debug;

    /**
     * Constructor for WellManager, calls WellManager#init() upon being called.
     */
    public WellManager() {
        init();
    }

    /**
     * This init function handles creating the new database connection, and getting wells from the database upon startup.
     */
    private void init() {
        Configuration configuration = Configuration.getInstance();

        if (configuration.getSQLEnabled()) {
            this.database = new SQLDatabase();
        } else {
            this.database = new YAMLDatabase();
        }

        try {
            this.database.initializeDatabase();
            this.wellHashMap = database.getWellsFromDatabase();
            // Now fill in cache
            for (Well well : wellHashMap.values()) {
                Location position = well.getLocation();
                this.wellCache.add(position);
                LevelUpWellListener observer = new LevelUpWellListener(well);
                levelupObservers.add(observer);
                XPIncrementWellListener obsv2 = new XPIncrementWellListener(well);
                xpincrementObservers.add(obsv2);
            }

            this.wellPlayerHashMap = database.getOnlineWellPlayers();

            this.connected = true;
        } catch (SQLException e) {
            this.connected = false;
            e.printStackTrace();
        }
    }

    // TODO: Implement pseudocode

    /**
     *
     * @param townMember - Player who placed the well block.
     * @param wellPosition - Block at which the well is being placed.
     * @return - Returns whether a well was created.
     */
    public boolean createWell(@NotNull Player townMember, Block wellPosition) {
        Town town = TownyAPI.getInstance().getTown(townMember.getLocation());
        // run various checks before writing it into database.
        if (town != null && town.hasResident(townMember)) {

            boolean can_place = PlayerCacheUtil.getCachePermission(townMember, wellPosition.getLocation(), wellPosition.getType(), TownyPermission.ActionType.BUILD);
            if (!can_place) {// if you cannot place, return.
                townMember.sendMessage(getColor("You cannot place a well in a town you are not part of!"));
                return false;
            }

            if (wellExistsByTownName(town.getName()))  {
                // if well exists, return.
                townMember.sendMessage(getColor("&cYour town already has a well!"));
                return false;
            }

            // Check for water blocks.
            if (!Utilities.blockRequirement(wellPosition, Material.WATER, 5)) // requires 5 waterblocks in the highlighted particle area
                return false;

            // Create a new well object, then it will be saved in to database.
            Well newWell = new Well(town.getName(), new Location(townMember.getWorld(), wellPosition.getX(), wellPosition.getY(), wellPosition.getZ()));

            this.wellCache.add(newWell.getLocation());
            this.wellHashMap.put(newWell.getTownName(), newWell);

            this.database.createWell(newWell); // puts it into mysql server.

            this.saveAllWells(); // saves all wells.

            townMember.sendMessage(getColor("&aSuccessfully created your town well!"));

            return true;
        }
        // print reason
        townMember.sendMessage(getColor("&cYou cannot place a well in a town you are not part of!"));

        return false;
    }


    /**
     * Delete a well from the known well manager.
     * Deleted wells will not be found in SQL upon being deleted.
     *
     * @param well
     */
    public void deleteWell(@NotNull Well well) {
        if (!wellExists(well))
            return;
        well.endAnimation();

        this.wellHashMap.remove(well.getTownName());
        this.wellCache.remove(well.getLocation());
        well.getNearWellAnimation().end();
        DHAPI.getHologram(well.getTownName()).delete();

        this.database.deleteWell(well);
    }


    /**
     * Returns a well object if a well at the given town exists.
     *
     * @return returns null if no well is found.
     */
    public Well getWellByTownName(@NotNull String townName) {
        return wellHashMap.get(townName);
    }

    public Well getWellByTown(@NotNull Town town) {
        return wellHashMap.get(town.getName());
    }


    public Well getWellByWellName(@NotNull String wellName) {
        if (wellName.length() <= 7) // there must be a minimum of 7 characters in the well's name for it to be valid.
            // "'s Well"
            return null;
        // Bukkit.broadcast(Component.text("well_name" + well_name.substring(0, well_name.length() - 7)));
        return wellHashMap.get(wellName.substring(0, wellName.length() - 7));
    }

    /**
     * Returns whether a well exists by a well object.
     * It checks the wellname of the database and the object passed in.
     *
     * @param well
     * @return
     */
    public boolean wellExists(Well well) {
        String townName = well.getTownName();

        return wellHashMap.containsKey(townName);
    }

    public boolean wellExists(@NotNull String townName) {
        return wellHashMap.containsKey(townName);
    }

    public boolean wellExistsForPlayer(@NotNull Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) { // not part of a town.
            if (debug)
                player.sendMessage("Not part of town.");
            return false;
        }

        String townname = town.getName();

        return wellExistsByTownName(townname);
    }

    /**
     * Returns whether a well exists by a townname.
     * Similar to WellManager#wellExists(Well well) but taking townname as param this time.
     *
     * @param townName
     */
    public boolean wellExistsByTownName(@NotNull String townName) {
        return wellHashMap.containsKey(townName);
    }

    /**
     * This method checks for the passed location in the
     * well cache.
     * <p>
     * The well cached it developed whenever a well object is created,
     * or modified when changed.
     *
     * @param location
     */
    public boolean isWell(@NotNull Location location) {
        Utilities.sendDebugMessage("Checking for well at location: " + location.toString());
        if (wellCache.size() == 0)
            return false;
        else
            return wellCache.contains(location);
    }

    /**
     * Returns the Well from a given location,
     * otherwise returning null.
     *
     */
    public Well getWellFromLocation(@NotNull Location location) {
        for (Well w : this.wellHashMap.values()) {
            if (w.getLocation().equals(location))
                return w;
        }
        return null;
    }

    /**
     * Saves all wells to the database.
     * This should be executed every 5 minutes or when a well is deleted.
     */
    public void saveAllWells() {
        Collection<Well> wells = this.wellHashMap.values();
        this.database.updateWells(wells);
    }

    /**
     * Saves all WellPlayers to the database.
     * This should also be executed every 5 minutes or when a WellPlayer is deleted.
     */
    public void saveAllWellPlayers() {
        Collection<WellPlayer> wellPlayers = this.wellPlayerHashMap.values();

        if (debug)
            System.out.println("☢ WellPlayersSize: " + wellPlayers.size());

        this.database.updateWellPlayers(wellPlayers);
    }

    public WellPlayer getWellPlayer(@NotNull Player player) {
        UUID uniqueId = player.getUniqueId();

        return this.wellPlayerHashMap.get(uniqueId);
    }

    /**
     * Gets a current list of wells around the world.
     *
     * @return
     */
    public Collection<Well> getWells() {
        return wellHashMap.values();
    }

    public Collection<Location> getCache() {
        return this.wellCache;
    }

    public Collection<WellPlayer> getWellPlayers() {
        return this.wellPlayerHashMap.values();
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void loadWellPlayer(@NotNull Player player) {
        WellPlayer wellPlayer = this.database.getWellPlayer(player);
        if (wellPlayer == null) {
            System.out.println("Created a new WellPlayer for player " + player.getName());
            WellPlayer newPlayer = new WellPlayer(player.getUniqueId(), 0, 0, 0, 0, 0);
            this.database.createWellPlayer(newPlayer);
        }

        this.wellPlayerHashMap.put(wellPlayer.getPlayerUUID(), wellPlayer);
        if (debug)
            player.sendMessage("☢ Got well player for load.");
    }

    public void unloadPlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (this.wellPlayerHashMap.containsKey(uuid)) {

            WellPlayer wellPlayer = this.wellPlayerHashMap.get(uuid);

            this.database.updateWellPlayer(wellPlayer);
            this.wellPlayerHashMap.remove(uuid);
        }
    }

    /**
     * Returns whether the init() function was successful or not.
     * This is used at the start of the plugin launch, stopping the plugin if no connection was made.
     *
     * @return
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Call this every 5 minutes. Updates the MySQL database with the latest information.
     */
    public void updateDatabase() {
        saveAllWellPlayers();
        saveAllWells();
    }

    /**
     * Closes the connection on the database object.
     * Once closed, the database will not save until a new Database object has been created.
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
        updateDatabase();
    }

    public DatabaseType getDatabaseType() {
        if (database == null)
            return null;

        return database.databaseType();
    }
}
