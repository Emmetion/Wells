package me.emmetion.wells.runnables;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.Well;
import org.bukkit.scheduler.BukkitRunnable;

public final class ActiveBuffRunnable extends BukkitRunnable {

    private final Wells plugin;

    public ActiveBuffRunnable(Wells wells) {
        this.plugin = wells;
    }


    /**
     * This loop is called every tick. It updates all the active buffs in the server.
     */
    @Override
    public void run() {
        for (Well well : this.plugin.getWellManager().getWells()) {
            for (ActiveBuff activeBuff : well.getBuffs()) {
                if (activeBuff == null) {
                    continue;
                }

                activeBuff.update();
            }
        }
    }
}
