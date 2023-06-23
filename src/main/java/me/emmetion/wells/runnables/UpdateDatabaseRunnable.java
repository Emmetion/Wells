package me.emmetion.wells.runnables;

import me.emmetion.wells.database.WellManager;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateDatabaseRunnable extends BukkitRunnable {

    private final WellManager manager;

    public UpdateDatabaseRunnable(WellManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        this.manager.updateDatabase();
    }
}
