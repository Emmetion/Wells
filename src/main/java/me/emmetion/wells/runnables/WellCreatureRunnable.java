package me.emmetion.wells.runnables;

import me.emmetion.wells.Wells;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.database.WellManager;
import org.bukkit.scheduler.BukkitRunnable;

public class WellCreatureRunnable extends BukkitRunnable {

    private Wells plugin;
    private WellManager wellManager;
    private CreatureManager creatureManager;

    public WellCreatureRunnable(Wells plugin, WellManager wellManager, CreatureManager creatureManager) {
        this.plugin = plugin;
        this.wellManager = wellManager;
        this.creatureManager = creatureManager;
    }

    // Every time this is executed, the well creatures will
    @Override
    public void run() {
        // wellCreature.updateCreature() is an abstract method that's a part of the WellCreature abstract class.
        creatureManager.handleFrameUpdate();
    }
}
