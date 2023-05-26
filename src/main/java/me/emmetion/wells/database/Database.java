package me.emmetion.wells.database;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.model.Well;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Database {

    private Connection connection;

    public Connection getConnection() throws SQLException {

        if(connection != null){
            return connection;
        }

        //Try to connect to my MySQL database running locally
        String url = "jdbc:mysql://u33061_WQm36YtTvQ:Ri1lKnBTcXFYVz6w0SBNYEfA@mario.bloom.host:3306/s33061_Wells";
        String user = "u33061_WQm36YtTvQ";
        String password = "Ri1lKnBTcXFYVz6w0SBNYEfA";

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;

        System.out.println("Connected to database.");

        return connection;
    }

    public void initializeDatabase() throws SQLException {

        Statement statement = getConnection().createStatement();

        //Create the player_stats table
        String sql = "CREATE TABLE IF NOT EXISTS wells (townname varchar(36) primary key, level int, xcor int, ycor int, zcor int, worldname varchar(36))";

        statement.execute(sql);
        statement.close();
    }

    public Well findWellByTownName(String townname) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM wells WHERE townname = ?");
        statement.setString(1, townname);

        ResultSet resultSet = statement.executeQuery();



        if(resultSet.next()){

            Well well = new Well(
                    resultSet.getString("townname"),
                    new Location(
                            Bukkit.getWorld("world"),
                            resultSet.getInt("xcor"),
                            resultSet.getInt("ycor"),
                            resultSet.getInt("zcor")
                    ),
                    resultSet.getInt("level")
            );

            statement.close();
            return well;
        }
        statement.close();
        return null;
    }

    public void createWell(Well well) throws SQLException {
        Town town = TownyAPI.getInstance().getTown(well.getTownName());
        for (Resident r : town.getResidents()) {
            r.sendMessage(Component.text("A well has been created in your town!"));
            r.sendMessage(Component.text("Well '"+well.getTownName() + "' Level: " + well.getLevel()));
            r.sendMessage(Component.text("x: " + well.getPosition().getBlockX() + " y: " + well.getPosition().getBlockY()+" z: " + well.getPosition().getBlockZ()));
        }

        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO wells(townname, level, xcor, ycor, zcor, worldname) VALUES (?, ?, ?, ?, ?, ?)");
        statement.setString(1, well.getTownName());
        statement.setInt(2, well.getLevel());
        statement.setInt(3, well.getPosition().getBlockX());
        statement.setInt(4, well.getPosition().getBlockY());
        statement.setInt(5, well.getPosition().getBlockZ());
        statement.setString(6, well.getPosition().getWorld().getName());

        try {
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate entry for well! Ignored.");
        }

        statement.close();

    }

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
                                    set.getInt("zcor")
                        ),
                        set.getInt("level"))
            );
        }

        return wells;
    }

    /**
     * Updates an individial well in the MySQL server.
     * Use updateWells() to update a list of wells.
     *
     * @param well
     * @throws SQLException
     */
    public void updateWell(Well well) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("UPDATE wells SET townname = ?, level = ?, xcor = ?, ycor = ?, zcor = ?, worldname = ? WHERE townname = ?");

        statement.setString(1, well.getTownName());
        statement.setInt(2, well.getLevel());
        statement.setInt(3, well.getPosition().getBlockX());
        statement.setInt(4, well.getPosition().getBlockY());
        statement.setInt(5, well.getPosition().getBlockZ());
        statement.setString(6, well.getPosition().getWorld().getName());

        statement.executeUpdate();
        statement.close();
    }

    /**
     * Iterate saving a list of wells into a MySQL Server.
     * Uses updateWell(Well well) on each
     * @param wells
     */
    public void updateWells(Collection<Well> wells) {
        wells.iterator().forEachRemaining(well -> {
            try {
                this.updateWell(well);
            } catch (SQLException e) {
                System.out.println("Failed to save well " + well.getTownName() + ".");
                e.printStackTrace();
            }
        });
    }

    public void deleteWell(Well well) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM wells WHERE townname = ?");
        statement.setString(1, well.getTownName());
        statement.executeUpdate();
        statement.close();
    }



}
