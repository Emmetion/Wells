package me.emmetion.wells.database;

import me.emmetion.wells.Wells;

import java.util.logging.Level;

public class Error {
    public static void execute(Wells plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(Wells plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
