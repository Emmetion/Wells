package me.emmetion.wells.runnables;

import me.emmetion.wells.Wells;
import me.emmetion.wells.managers.CreatureManager;
import me.emmetion.wells.managers.WellManager;
import org.bukkit.scheduler.BukkitRunnable;

public final class WellCreatureRunnable extends BukkitRunnable {

    private final Wells plugin;
    private final WellManager wellManager;
    private final CreatureManager creatureManager;

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
