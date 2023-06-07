package me.emmetion.wells.database;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.observer.IncrementObserver;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

/**
 * WellManager.java, operates as a wrapper for the database class, removing a layer of SQL statements and creating handling operations more efficiently.
 */
public class WellManager {

    /**
     * MySQL database connection. This wrapper class uses methods inside database to achieve its goal.
     */
    private Database database;

    /**
     * Well Hash Map, contains every well by their respective townname as a key.
     */
    private HashMap<String, Well> wellHashMap = new HashMap<>();

    /**
     * A set of locations well's currently inhabit, use this to quickly find well blocks.
     */
    private HashSet<Location> wellCache = new HashSet<>();

    private List<IncrementObserver> levelupObserver = new ArrayList<>();

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
        this.database = new Database();
        try {
            this.database.initializeDatabase();
            this.wellHashMap = database.getWellsFromTable();
            // Now fill in cache
            for (Well well : wellHashMap.values()) {
                Location position = well.getLocation();
                this.wellCache.add(position);
                IncrementObserver observer = new IncrementObserver(well);
                levelupObserver.add(observer);
            }

            this.wellPlayerHashMap = database.getOnlineWellPlayersFromTable();
            Bukkit.getServer().sendMessage(Component.text("wphm: "+wellPlayerHashMap.size()));

            this.connected = true;
        } catch (SQLException e) {
            this.connected = false;
            e.printStackTrace();
        }
    }

    // TODO: Implement pseudocode
    public boolean createWell(Player townMember, Block wellPosition) {
        Town town = TownyAPI.getInstance().getTown(townMember.getLocation());
        // run various checks before writing it into database.
        if (town != null && town.hasResident(townMember)) {
            Player player = townMember;

            boolean can_place = PlayerCacheUtil.getCachePermission(townMember, wellPosition.getLocation(), wellPosition.getType(), TownyPermission.ActionType.BUILD);
            if (!can_place) {// if you cannot place, return.
                player.sendMessage(ChatColor.RED + "You cannot place a well in a town you are not part of!");
                return false;
            }
            if (wellExistsByTownName(town.getName())) // if well exists, return.
                return false;
            // Check for water blocks.
            if (!Utilities.blockRequirement(wellPosition, Material.WATER, 5)) // requires 5 waterblocks in the highlighted particle area
                return false;

            // Create a new well object, then it will be saved in to database.
            Well newWell = new Well(
                    town.getName(),
                    new Location(
                            townMember.getWorld(),
                            wellPosition.getX(),
                            wellPosition.getY(),
                            wellPosition.getZ()
                    )
            );
            try {
                this.wellCache.add(newWell.getLocation());
                this.wellHashMap.put(newWell.getTownName(), newWell);

                this.database.createWell(newWell); // puts it into mysql server.
                this.saveAllWells(); // saves all wells.

                townMember.sendMessage(ChatColor.GREEN + "Successfully created your town well!");
            } catch (SQLException e) {
                townMember.sendMessage("Failed to create new well because of SQLException, please check console.");
                e.printStackTrace();
            }

            return true;

        }
        // pseudocode
        // First check whether the player is in their own town.
        // Then check if a well is placed in that players town
        // Check the blocks around where a well block was placed for water.
        // Create well if all checks pass.

        //if (!wellExists())

        return false;
    }


    /**
     * Delete a well from the known well manager.
     * Deleted wells will not be found in SQL upon being deleted.
     *
     * @param well
     */
    public boolean deleteWell(Well well) {
        if (!wellExists(well))
            return false;

        try {
            this.wellHashMap.remove(well.getTownName());
            this.wellCache.remove(well.getLocation());
            this.database.deleteWell(well);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Returns a well object if a well at the given town exists.
     *
     * @param townname
     * @return returns null if no well is found.
     */
    public Well getWellByTownName(String townname) {
        if (wellHashMap == null)
            return null;
        return wellHashMap.get(townname);
    }

    public Well getWellByWellName(String well_name) {
        if (wellHashMap == null || well_name == null || well_name.length() <= 7)
            return null; // 's Well
        Bukkit.broadcast(Component.text("well_name" + well_name.substring(0, well_name.length() - 7)));
        return wellHashMap.get(well_name.substring(0, well_name.length() - 7));
    }

    /**
     * Returns whether a well exists by a well object.
     * It checks the wellname of the database and the object passed in.
     * @param well
     * @return
     */
    public boolean wellExists(Well well) {
        String townName = well.getTownName();
        if (wellHashMap == null)
            return false;
        return wellHashMap.containsKey(townName);
    }

    public boolean wellExists(String townname) {
        if (wellHashMap == null) {
            return false;
        }
        return wellHashMap.containsKey(townname);
    }

    public boolean wellExistsForPlayer(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null) { // not part of a town.
            if (debug)
                player.sendMessage("Not part of town.");
            return false;
        }

        int size = wellHashMap.size();
        player.sendMessage("wellHashMap#size(): " + size);
        for (String s : wellHashMap.keySet()) {
            player.sendMessage("key: " + s);
        }

        String townname = town.getName();

        player.sendMessage("Town of player: " + townname);

        return wellExistsByTownName(townname);
    }

    /**
     * Returns whether a well exists by a townname.
     * Similar to WellManager#wellExists(Well well) but taking townname as param this time.
     * @param townName
     * @return
     */
    public boolean wellExistsByTownName(String townName) {
        if (wellHashMap == null)
            return false;
        boolean b = wellHashMap.containsKey(townName);
        System.out.println("townname: " + b);
        return b;
    }

    public boolean isWell(Location location) {
        if (wellCache.size() == 0)
            return false;
        else
            return wellCache.contains(location);
    }

    /**
     * Saves all wells to the database.
     * This should be executed every 5 minutes or when a well is deleted.
     */
    public void saveAllWells() {
        System.out.println("☢ Saving wells...");
        Player emmetion = Bukkit.getPlayer("Emmetion");
        Collection<Well> wells = this.wellHashMap.values();
        int count = wells.size();
        if (debug)
            emmetion.sendMessage(Component.text("☢ Wells Saved: " + count));

        this.database.updateWells(wells);
        System.out.println("Saved!");
    }

    /**
     * Saves all WellPlayers to the database.
     * This should also be executed every 5 minutes or when a WellPlayer is deleted.
     */
    public void saveAllWellPlayers() {
        System.out.println("☢ Saving well players...");
        Player em = Bukkit.getPlayer("Emmetion");

        Collection<WellPlayer> wellPlayers = this.wellPlayerHashMap.values();
        int count = wellPlayers.size();
        if (debug)
            em.sendMessage(Component.text("☢ WellPlayersSize: " + count));
        this.database.updateWellPlayers(wellPlayers);

    }

    public WellPlayer getWellPlayer(Player player) {
        UUID uniqueId = player.getUniqueId();
        return this.wellPlayerHashMap.get(uniqueId);
    }

    /**
     * Gets a current list of wells around the world.
     * @return
     */
    public Collection<Well> getWells() {
        if (wellHashMap == null) {
            return null;
        }

        return wellHashMap.values();
    }

    public Collection<Location> getCache() {
        return this.wellCache;
    }

    public Collection<WellPlayer> getWellPlayers() {
        return this.wellPlayerHashMap.values();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void loadWellPlayer(Player player) {

        if (player == null) {
            Player emmetion = Bukkit.getPlayer("Emmetion");
            if (debug)
                emmetion.sendMessage("☢ player is null in loadWellPlayer");
            return;

        }
        try {
            WellPlayer wellPlayer = this.database.getWellPlayerOrNew(player.getUniqueId());
            this.wellPlayerHashMap.put(wellPlayer.getPlayerUUID(), wellPlayer);
            if (debug)
                player.sendMessage("☢ Got well player for load.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unloadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.wellPlayerHashMap.containsKey(uuid)) {

            WellPlayer wellPlayer = this.wellPlayerHashMap.get(uuid);
            try {
                this.database.updateWellPlayer(wellPlayer);
                this.wellPlayerHashMap.remove(uuid);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns whether the init() function was successful or not.
     * This is used at the start of the plugin launch, stopping the plugin if no connection was made.
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
     * @throws SQLException
     */
    public void close() throws SQLException {
        updateDatabase();
        if (this.database.getConnection().isClosed()) {
            this.database.getConnection().close();
        }
    }
}
