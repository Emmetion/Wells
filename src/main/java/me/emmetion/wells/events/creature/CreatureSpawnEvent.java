package me.emmetion.wells.events.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.WellCreature;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class CreatureSpawnEvent extends CreatureEvent {

    private final org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason;

    public CreatureSpawnEvent(WellCreature wellCreature, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        super(wellCreature);

        this.spawnReason = spawnReason;

        handle();
    }

    private void handle() {
        Logger logger = Wells.plugin.getLogger();
        if (getWellCreature() == null) {
            logger.info(() -> "Spawned WellCreature was null in CreatureSpawnEvent.");
            // System.out.println("Spawned WelLCreature is Null in CreatureSpawnEvent.");
            return;
        }
        // System.out.println("Spawned WellCreature : " + getWellCreature().getCreatureType() + " [" + getWellCreature().getUUID() + "]");
        logger.info("Spawned WellCreature : " + getWellCreature().getCreatureType() + " [" + getWellCreature().getUUID() + "]");
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
