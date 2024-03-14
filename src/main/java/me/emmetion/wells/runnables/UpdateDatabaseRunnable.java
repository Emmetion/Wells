package me.emmetion.wells.runnables;

import me.emmetion.wells.managers.WellManager;
import org.bukkit.scheduler.BukkitRunnable;

public final class UpdateDatabaseRunnable extends BukkitRunnable {

    private final WellManager manager;

    /**
     * Constructor for the UpdateDatabaseRunnable
     * @param manager the WellManager
     */
    public UpdateDatabaseRunnable(WellManager manager) {
        this.manager = manager;
    }

    /**
     * Updates the database.
     * Called every 5 minutes by default. Can configure in the config.yml
     */
    @Override
    public void run() {
        this.manager.updateDatabase();
    }
}
