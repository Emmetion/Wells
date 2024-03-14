package me.emmetion.wells.database;

import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.UUID;

public final class SQLDatabase extends EDatabase {

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

        // retrieves SQL login credentials from configuration file.
        String user = config.getSQLUsername();
        String password = config.getSQLPassword();
        String url = config.getSQLURL();

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;

        return connection;
    }


    /**
     * Run this when you initialize a database connection.
     */
    @Override
    public void initializeDatabase() {
        // Create wells table
        try {
            Statement wells = getConnection().createStatement();
            String wellsSQL = "CREATE TABLE IF NOT EXISTS wells (" +
                    "townname varchar(36) primary key," +
                    " well_level int, experience int," +
                    " xcor int, ycor int, zcor int," +
                    " xholocor double, yholocor double, zholocor double," +
                    " worldname varchar(36)," +
                    " buff1_id varchar(36), buff1_endtimestamp timestamp," +
                    " buff2_id varchar(36), buff2_endtimestamp timestamp," +
                    " buff3_id varchar(36), buff3_endtimestamp timestamp," +
                    " is_boosted boolean, boost_end timestamp)";

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
            String wellCreaturesSQL = "CREATE TABLE IF NOT EXISTS well_creatures (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "TYPE VARCHAR(36))";

            well_creatures.execute(wellCreaturesSQL);
            well_creatures.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    /**
     * Create well at a given location, writing it into the MySQL table.
     */
    @Override
    public void createWell(@NotNull Well well) {
        try {

            PreparedStatement statement = getConnection()
                    .prepareStatement("INSERT INTO wells(townname, well_level, experience, xcor, ycor, zcor, xholocor, yholocor, zholocor, worldname, buff1_id, buff1_endtimestamp, buff2_id, buff2_endtimestamp, buff3_id, buff3_endtimestamp, is_boosted, boost_end) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            prepareWellStatement(well, statement);
            statement.setBoolean(17, well.isBoosted());
            statement.setTimestamp(18, well.getBoostEnd());

            try {
                statement.executeUpdate();
                announceWellPlacement(well); // Send messages to town members.
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Duplicate entry for well! Ignored.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns well from townname from MySQL database.
     */
    @Override
    public Well getWell(@NotNull String townName) {

        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM wells WHERE townname = ?");
            statement.setString(1, townName);

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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates an individual well in the MySQL Server.
     * Use updateWells() to update a list of wells.
     */
    @Override
    public void updateWell(@NotNull Well well) {
        try {
            PreparedStatement statement = getConnection().prepareStatement("UPDATE wells SET townname = ?, well_level = ?, experience = ?, xcor = ?, ycor = ?, zcor = ?, xholocor = ?, yholocor = ?, zholocor = ?, worldname = ?, buff1_id = ?, buff1_endtimestamp = ?, buff2_id = ?, buff2_endtimestamp = ?, buff3_id = ?, buff3_endtimestamp = ? WHERE townname = ?");

            prepareWellStatement(well, statement);

            statement.setString(17, well.getTownName());

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Remove a well from a town in the MySQL Server.
     * @param well
     * @throws SQLException
     */
    @Override
    public void deleteWell(@NotNull Well well) {
        try {
            PreparedStatement statement = getConnection().prepareStatement("DELETE FROM wells WHERE townname = ?");
            statement.setString(1, well.getTownName());

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

//    OLD getWellsFromTable method.
//
//    /**
//     * Returns a hashmap of wells inside MySQL table at runtime.
//     *
//     * @return
//     * @throws SQLException
//     */
//    @Override
//    public HashMap<String, Well> getWellsFromTable() {
//        try {
//            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM wells");
//
//            List<Town> towns = TownyAPI.getInstance().getTowns();
//            List<String> townNameList = towns.stream()
//                    .map(Town::getName)
//                    .toList();
//
//            ResultSet set = statement.executeQuery();
//
//            HashMap<String, Well> wells = new HashMap<>();
//
//
//            while (set.next()) {
//                // add to hashmap.
//                wells.put(
//                        set.getString("townname"),
//                        new Well(
//                                set.getString("townname"),
//                                new Location(
//                                        Bukkit.getWorld(set.getString("worldname")),
//                                        set.getInt("xcor"),
//                                        set.getInt("ycor"),
//                                        set.getInt("zcor")),
//                                new Location(
//                                        Bukkit.getWorld(set.getString("worldname")),
//                                        set.getDouble("xholocor"),
//                                        set.getDouble("yholocor"),
//                                        set.getDouble("zholocor")),
//                                set.getInt("well_level"),
//                                set.getInt("experience"),
//                                set.getString("buff1_id"),
//                                set.getTimestamp("buff1_endtimestamp"),
//                                set.getString("buff2_id"),
//                                set.getTimestamp("buff2_endtimestamp"),
//                                set.getString("buff3_id"),
//                                set.getTimestamp("buff3_endtimestamp"),
//                                set.getBoolean("is_boosted"),
//                                set.getTimestamp("boost_end")
//                        )
//                );
//            }
//            return wells;
//        } catch (SQLException e) {
//            // TODO: Log error in console.
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    @Override
    public void createWellPlayer(@NotNull WellPlayer wellPlayer) {
        try {
            PreparedStatement statement = getConnection()
                    .prepareStatement("INSERT INTO well_players(uuid, bronzeCoins, silverCoins, goldCoins, coinsDeposited, experiencePoints) VALUES (?, ?, ?, ?, ?, ?)");
            prepareWellPlayerStatement(wellPlayer, statement);

            try {
                statement.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Duplicate entry for well! Ignored.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WellPlayer getWellPlayer(@NotNull Player player) {
        try {

            UUID uuid = player.getUniqueId();
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM well_players WHERE uuid = ?");
            statement.setString(1, uuid.toString());

            ResultSet set = statement.executeQuery();


            WellPlayer wellPlayer;

            if (set.next()) {
                wellPlayer = new WellPlayer(uuid, set.getInt("bronzeCoins"), set.getInt("silverCoins"), set.getInt("goldCoins"), set.getInt("coinsDeposited"), set.getInt("experiencePoints"));

                statement.close();

                return wellPlayer;

            } else {
                statement.close();
                // The player does not have an account.
                // To comply with java streams, I will create the account here to ensure this returns a WellPlayer everytime.
                WellPlayer newPlayer = new WellPlayer(uuid, 0, 0, 0, 0, 0);
                this.createWellPlayer(newPlayer);
                return newPlayer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateWellPlayer(@NotNull WellPlayer player) {
        try {
            PreparedStatement statement = getConnection().prepareStatement("UPDATE well_players SET uuid = ?, bronzeCoins = ?, silverCoins = ?, goldCoins = ?, coinsDeposited = ?, experiencePoints = ? WHERE uuid = ?");

            prepareWellPlayerStatement(player, statement);

            statement.setString(7, player.getPlayerUUID().toString());

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteWellPlayer(@NotNull WellPlayer wellPlayer) {
        try {
            String uuid = wellPlayer.getPlayerUUID().toString();
            PreparedStatement statement = getConnection().prepareStatement("DELETE FROM well_players WHERE uuid = ?");
            statement.setString(1, uuid);

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseType databaseType() {
        return DatabaseType.YAML;
    }

    private void prepareWellStatement(@NotNull Well well, PreparedStatement statement) throws SQLException {
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
    }

    private void prepareWellPlayerStatement(@NotNull WellPlayer player, PreparedStatement statement) throws SQLException {
        statement.setString(1, player.getPlayerUUID().toString());
        statement.setInt(2, player.getBronzeCoins());
        statement.setInt(3, player.getSilverCoins());
        statement.setInt(4, player.getGoldCoins());
        statement.setInt(5, player.getCoinsDeposited());
        statement.setInt(6, player.getExperiencePoints());
    }

}
