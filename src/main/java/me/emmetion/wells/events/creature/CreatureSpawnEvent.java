package me.emmetion.wells.events.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.WellCreature;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CreatureSpawnEvent extends CreatureEvent {

    public CreatureSpawnEvent(WellCreature wellCreature, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        super(wellCreature);

        handle();
    }

    private void handle() {
        if (getWellCreature() == null) {
            System.out.println("Spawned WelLCreature is Null in CreatureSpawnEvent.");
            return;
        }
        Logger logger = Wells.plugin.getLogger();
        System.out.println("Spawned WellCreature : " + getWellCreature().getCreatureType() + " [" + getWellCreature().getUUID() + "]");
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
