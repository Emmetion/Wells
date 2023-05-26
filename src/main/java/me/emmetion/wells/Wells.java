package me.emmetion.wells;

import com.palmergames.bukkit.towny.TownyAPI;
import me.emmetion.wells.commands.WellCommand;
import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.Well;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Wells extends JavaPlugin {

    private WellManager wellManager;

    @Override
    public void onEnable() {

        Logger logger = Logger.getLogger("Wells");
        logger.log(Level.INFO, "Plugin starting...");


        TownyAPI instance = TownyAPI.getInstance();
        if (instance == null) {
            System.out.println("Disabling because townyapi was not found.");
            this.setEnabled(false);
        }
        System.out.println(ChatColor.RED + "DEBUG: townyapi was found... continuing...");

        this.wellManager = new WellManager();
        if (!wellManager.isConnected()) {
            System.out.println("Failed connection to the wellmanager.");
            this.setEnabled(false);
        }

        Well fakeWell = new Well("Emmet's Town", new Location(Bukkit.getWorld("world"), 0,0,0), 0);

        boolean b = this.wellManager.wellExistsByTownName("Emmet's Town");
        System.out.println("FakeWell present: " + b);

        boolean connected = this.wellManager.isConnected();
        System.out.println("isConnected(): " + connected);

        initCommands();

    }

    @Override
    public void onDisable() {

        this.wellManager.saveAllWells();
    }


    private void initCommands() {
        this.getCommand("wells").setExecutor(new WellCommand(wellManager));
        System.out.println("Commands have been successfully enabled.");
    }

}
