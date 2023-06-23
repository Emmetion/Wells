package me.emmetion.wells.runnables;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.Well;
import org.bukkit.scheduler.BukkitRunnable;

public class ActiveBuffRunnable extends BukkitRunnable {

    private Wells plugin;

    public ActiveBuffRunnable(Wells wells) {
        this.plugin = wells;
    }


    // This operation will become
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
