package me.emmetion.wells.database;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Database {

    private Connection connection;

    /**
     * Returns the connection to the MySQL server.
     * Use this to send statements and manipulate information in the table.
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {

        if(connection != null){
            return connection;
        }

        Configuration config = Configuration.getInstance();

        String user = config.getSQLUsername();
        String password = config.getSQLPassword();
        String url = config.getSQLURL();


        // Connect to mysql server.
        // these credentials are only temporary.
        // will change it to config in
//        String url = "jdbc:mysql://u33061_WQm36YtTvQ:Ri1lKnBTcXFYVz6w0SBNYEfA@mario.bloom.host:3306/s33061_Wells";
//        String user = "u33061_WQm36YtTvQ";
//        String password = "Ri1lKnBTcXFYVz6w0SBNYEfA";

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;
        System.out.println("Connected to database.");

        return connection;
    }

    /**
     * Run this when you initialize a database connection.
     * @throws SQLException
     */
    public void initializeDatabase() throws SQLException {
        // Create wells table
        Statement wells = getConnection().createStatement();
        String wellsSQL = "CREATE TABLE IF NOT EXISTS wells (" +
                "townname varchar(36) primary key," +
                " well_level int, experience int," +
                " xcor int, ycor int, zcor int," +
                " xholocor double, yholocor double, zholocor double," +
                " worldname varchar(36)," +
                " buff1_id varchar(36), buff1_endtimestamp timestamp," +
                " buff2_id varchar(36), buff2_endtimestamp timestamp," +
                " buff3_id varchar(36), buff3_endtimestamp timestamp)";

        wells.execute(wellsSQL);
        wells.close();

        // Create well_players table
        Statement well_players = getConnection().createStatement();
        String wellsPlayerSQL = "CREATE TABLE IF NOT EXISTS well_players (" +
                "uuid varchar(36) primary key," +
                " bronzeCoins int, silverCoins int, goldCoins int," +
                " coinsDeposited int, experiencePoints int)";

        well_players.execute(wellsPlayerSQL);
        well_players.close();

        // Create well_creatures table
        Statement well_creatures = getConnection().createStatement();
        String wellcreaturesSQL = "CREATE TABLE IF NOT EXISTS well_creatures (" +
                "uuid varchar(36) primary key," +
                "type varchar(36))";

        well_creatures.execute(wellcreaturesSQL);
        well_creatures.close();

    }

    /**
     * Returns well from townname from MySQL database.
     * @param townname
     * @return
     * @throws SQLException
     */
    public Well findWellByTownName(String townname) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM wells WHERE townname = ?");
        statement.setString(1, townname);

        ResultSet set = statement.executeQuery();



        if(set.next()){

            Well well = new Well(
                    set.getString("townname"),
                    new Location(
                            Bukkit.getWorld(set.getString("worldname")),
                            set.getInt("xcor"),
                            set.getInt("ycor"),
                            set.getInt("zcor")),
                    new Location(
                            Bukkit.getWorld(set.getString("worldname")),
                            set.getDouble("xholocor"),
                            set.getDouble("yholocor"),
                            set.getDouble("zholocor")),
                    set.getInt("well_level"),
                    set.getInt("experience"),
                    set.getString("buff1_id"),
                    set.getTimestamp("buff3_endtimestamp"),
                    set.getString("buff2_id"),
                    set.getTimestamp("buff3_endtimestamp"),
                    set.getString("buff3_id"),
                    set.getTimestamp("buff3_endtimestamp"),
                    set.getBoolean("is_boosted"),
                    set.getTimestamp("boost_end")
            );

            statement.close();
            return well;
        }
        statement.close();
        return null;
    }

    /**
     * Create well at a given location, writing it into the MySQL table.
     * @param well
     * @throws SQLException
     */
    public void createWell(Well well) throws SQLException {
        announceWellPlacement(well);

        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO wells(townname, well_level, experience, xcor, ycor, zcor, xholocor, yholocor, zholocor, worldname, buff1_id, buff1_endtimestamp, buff2_id, buff2_endtimestamp, buff3_id, buff3_endtimestamp, is_boosted, boost_end) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, well.getTownName());
        statement.setInt(2, well.getWellLevel());
        statement.setInt(3, well.getExperience());
        statement.setInt(4, well.getLocation().getBlockX());
        statement.setInt(5, well.getLocation().getBlockY());
        statement.setInt(6, well.getLocation().getBlockZ());
        statement.setDouble(7, well.getHologramLocation().getX());
        statement.setDouble(8, well.getHologramLocation().getY());
        statement.setDouble(9, well.getHologramLocation().getZ());
        statement.setString(10, well.getLocation().getWorld().getName());
        statement.setString(11, well.getBuff1().getBuffID());
        statement.setTimestamp(12, well.getBuff1().getEndTimestamp());
        statement.setString(13, well.getBuff2().getBuffID());
        statement.setTimestamp(14, well.getBuff2().getEndTimestamp());
        statement.setString(15, well.getBuff3().getBuffID());
        statement.setTimestamp(16, well.getBuff3().getEndTimestamp());
        statement.setBoolean(16, well.isBoosted());
        statement.setTimestamp(16, well.getBoostEnd());

        try {
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate entry for well! Ignored.");
        }

        statement.close();
    }

    /**
     * Sends well placement announcement to all residents in the town.
     * @param well
     */
    private void announceWellPlacement(Well well) {
        Town town = TownyAPI.getInstance().getTown(well.getTownName());
        for (Resident r : town.getResidents()) {
            r.sendMessage(Component.text(ChatColor.GREEN + "---- Town Announcement ----"));
            r.sendMessage(Component.text(ChatColor.YELLOW + "A well has been created in your town!"));
            r.sendMessage(Component.text("Well '" + well.getTownName() + "' Level: " + ChatColor.YELLOW + well.getWellLevel()));
            r.sendMessage(Component.text(ChatColor.YELLOW + "x: " + well.getLocation().getBlockX() + " y: " + well.getLocation().getBlockY()+" z: " + well.getLocation().getBlockZ()));
        }
    }

    /**
     * Returns a hashmap of wells inside MySQL table at runtime.
     *
     * @return
     * @throws SQLException
     */
    public HashMap<String, Well> getWellsFromTable() throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM wells");

        List<Town> towns = TownyAPI.getInstance().getTowns();
        List<String> townList = towns.stream().map(town -> town.getName()).collect(Collectors.toList());

        ResultSet set = statement.executeQuery();

        HashMap<String, Well> wells = new HashMap<>();


        while (set.next()) {
            // add to hashmap.
            wells.put(
                    set.getString("townname"),
                    new Well(
                            set.getString("townname"),
                            new Location(
                                    Bukkit.getWorld(set.getString("worldname")),
                                    set.getInt("xcor"),
                                    set.getInt("ycor"),
                                    set.getInt("zcor")),
                            new Location(
                                    Bukkit.getWorld(set.getString("worldname")),
                                    set.getDouble("xholocor"),
                                    set.getDouble("yholocor"),
                                    set.getDouble("zholocor")),
                            set.getInt("well_level"),
                            set.getInt("experience"),
                            set.getString("buff1_id"),
                            set.getTimestamp("buff1_endtimestamp"),
                            set.getString("buff2_id"),
                            set.getTimestamp("buff2_endtimestamp"),
                            set.getString("buff3_id"),
                            set.getTimestamp("buff3_endtimestamp"),
                            set.getBoolean("is_boosted"),
                            set.getTimestamp("boost_end")
                    )
            );
        }

        return wells;
    }

    /**
     * Updates an individial well in the MySQL Server.
     * Use updateWells() to update a list of wells.
     *
     * @param well
     * @throws SQLException
     */
    public void updateWell(Well well) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("UPDATE wells SET townname = ?, well_level = ?, experience = ?, xcor = ?, ycor = ?, zcor = ?, xholocor = ?, yholocor = ?, zholocor = ?, worldname = ?, buff1_id = ?, buff1_endtimestamp = ?, buff2_id = ?, buff2_endtimestamp = ?, buff3_id = ?, buff3_endtimestamp = ? WHERE townname = ?");

        statement.setString(1, well.getTownName());
        statement.setInt(2, well.getWellLevel());
        statement.setInt(3, well.getExperience());
        statement.setInt(4, well.getLocation().getBlockX());
        statement.setInt(5, well.getLocation().getBlockY());
        statement.setInt(6, well.getLocation().getBlockZ());
        statement.setDouble(7, well.getHologramLocation().getX());
        statement.setDouble(8, well.getHologramLocation().getY());
        statement.setDouble(9, well.getHologramLocation().getZ());
        statement.setString(10, well.getLocation().getWorld().getName());
        statement.setString(11, well.getBuff1().getBuffID());
        statement.setTimestamp(12, well.getBuff1().getEndTimestamp());
        statement.setString(13, well.getBuff2().getBuffID());
        statement.setTimestamp(14, well.getBuff2().getEndTimestamp());
        statement.setString(15, well.getBuff3().getBuffID());
        statement.setTimestamp(16, well.getBuff3().getEndTimestamp());

        statement.setString(17, well.getTownName());

        statement.executeUpdate();
        statement.close();
    }

    /**
     * Iterate saving a list of wells into a MySQL Server.
     * Uses updateWell(Well well) on each
     * @param wells
     */
    public void updateWells(Collection<Well> wells) {
        for (Well well : wells) {
            try {
                this.updateWell(well);
            } catch (SQLException e) {
                System.out.println("Failed to save well " + well.getTownName() + ".");
                e.printStackTrace();
            }
        };
    }

    /**
     * Remove a well from a town in the MySQL Server.
     * @param well
     * @throws SQLException
     */
    public void deleteWell(Well well) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM wells WHERE townname = ?");
        statement.setString(1, well.getTownName());

        statement.executeUpdate();
        statement.close();
    }

    public HashMap<UUID, WellPlayer> getOnlineWellPlayersFromTable() throws SQLException {
        HashMap<UUID, WellPlayer> well_players = new HashMap<>();

        List<UUID> onlineUUIDS = Bukkit.getOnlinePlayers().stream()
                .map(player -> player.getUniqueId())
                .collect(Collectors.toList());

        for (UUID uuid : onlineUUIDS) {
            WellPlayer wp = getWellPlayerOrNew(uuid);
            well_players.put(uuid, wp);
        }

        return well_players;
    }

    public void createWellPlayer(WellPlayer wellPlayer) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO well_players(uuid, bronzeCoins, silverCoins, goldCoins, coinsDeposited, experiencePoints) VALUES (?, ?, ?, ?, ?, ?)");
        statement.setString(1, wellPlayer.getPlayerUUID().toString());
        statement.setInt(2, wellPlayer.getBronzeCoins());
        statement.setInt(3, wellPlayer.getSilverCoins());
        statement.setInt(4, wellPlayer.getGoldCoins());
        statement.setInt(5, wellPlayer.getCoinsDeposited());
        statement.setInt(6, wellPlayer.getExperiencePoints());

        try {
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate entry for well! Ignored.");
        }

        statement.close();
    }

    public void updateWellPlayers(Collection<WellPlayer> wellPlayers) {
        for (WellPlayer wellPlayer : wellPlayers) {
            try {
                updateWellPlayer(wellPlayer);
            } catch (SQLException e) {
                System.out.println("Failed to save '" + wellPlayer.getPlayerUUID() + "'.");
                e.printStackTrace();
            }
        }
    }

    public void updateWellPlayer(WellPlayer player) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("UPDATE well_players SET uuid = ?, bronzeCoins = ?, silverCoins = ?, goldCoins = ?, coinsDeposited = ?, experiencePoints = ? WHERE uuid = ?");

        statement.setString(1, player.getPlayerUUID().toString());
        statement.setInt(2, player.getBronzeCoins());
        statement.setInt(3, player.getSilverCoins());
        statement.setInt(4, player.getGoldCoins());
        statement.setInt(5, player.getCoinsDeposited());
        statement.setInt(6, player.getExperiencePoints());

        statement.setString(7, player.getPlayerUUID().toString());

        statement.executeUpdate();
        statement.close();
    }

    public void deleteWellPlayer(Player player) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM well_players WHERE uuid = ?");
        statement.setString(1, uuid);

        statement.executeUpdate();
        statement.close();
    }

    public WellPlayer getWellPlayerFromDatabase(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM well_players WHERE uuid = ?");
        statement.setString(1, uuid.toString());

        ResultSet set = statement.executeQuery();

        WellPlayer wellPlayer;

        if (set.next()) {
            wellPlayer = new WellPlayer(uuid,
                    set.getInt("bronzeCoins"),
                    set.getInt("silverCoins"),
                    set.getInt("goldCoins"),
                    set.getInt("coinsDeposited"),
                    set.getInt("experiencePoints")
            );

            statement.close();

            return wellPlayer;

        }

        statement.close();

        return null;
    }

    public WellPlayer getWellPlayerOrNew(UUID uuid) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM well_players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            WellPlayer wp = new WellPlayer(
                    uuid,
                    resultSet.getInt("bronzeCoins"),
                    resultSet.getInt("silverCoins"),
                    resultSet.getInt("goldCoins"),
                    resultSet.getInt("coinsDeposited"),
                    resultSet.getInt("experiencePoints")
            );

            statement.close();

            return wp;
        } else {
            WellPlayer wellPlayer = new WellPlayer(uuid);
            this.createWellPlayer(wellPlayer); // inserts it into new db.

            return wellPlayer;
        }
    }

}
