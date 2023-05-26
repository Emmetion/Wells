package me.emmetion.wells.database;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

/**
 * WellManager.java, operates as a wrapper for the database class, removing the use of SQL strings.
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

    /**
     * Contains the trur
     */
    private boolean connected;

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
                Location position = well.getPosition();
                this.wellCache.add(position);
            }
            this.connected = true;
        } catch (SQLException e) {
            this.connected = false;
            e.printStackTrace();
        }
    }

    // TODO: Implement pseudocode
    public void createWell(Player wellOwner, Block wellPosition) {
        // pseudocode
        // First check whether the player is in their own town.
        // Then check if a well is placed in that players town
        // Check the blocks around where a well block was placed for water.
        // Create well if all checks pass.

        //if (!wellExists())


    }

    /**
     * Delete a well from the known well manager.
     * Deleted wells will not be found in SQL upon being deleted.
     *
     * @param well
     */
    public void deleteWell(Well well) {
        if (!wellExists(well))
            return;

        this.wellHashMap.remove(well);
        try {
            this.database.deleteWell(well);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            return false;
        }
        String townname = town.getName();
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
        return wellHashMap.containsKey(townName);
    }

    /**
     * Saves all wells to the database.
     * This should be executed every 5 minutes or when a well is deleted.
     */
    public void saveAllWells() {
        System.out.println("Saving wells...");
        this.database.updateWells(this.wellHashMap.values());
        System.out.println("Saved!");
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

    /**
     * Returns whether the init() function was successful or not.
     * This is used at the start of the plugin launch, stopping the plugin if no connection was made.
     * @return
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Closes the connection on the database object.
     * Once closed, the database will not save until a new Database object has been created.
     * @throws SQLException
     */
    public void close() throws SQLException {
        if (this.database.getConnection().isClosed()) {
            this.database.getConnection().close();
        }
    }
}
