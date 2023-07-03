package me.emmetion.wells.events;

import me.emmetion.wells.creature.WellCreature;
import net.citizensnpcs.api.event.SpawnReason;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CreatureSpawnEvent extends CreatureEvent {

    public CreatureSpawnEvent(WellCreature wellCreature, ExperienceOrb.SpawnReason spawnReason) {
        super(wellCreature);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
