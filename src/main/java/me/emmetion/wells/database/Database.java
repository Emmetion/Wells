package me.emmetion.wells.database;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.Well;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;

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

    public void deleteWell(Well well) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM wells WHERE townname = ?");
        statement.setString(1, well.getTownName());

        statement.executeUpdate();

        statement.close();

    }

}
