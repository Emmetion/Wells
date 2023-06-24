package me.emmetion.wells.events;

import me.emmetion.wells.creature.WellCreature;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CreatureKillEvent extends CreatureEvent {

    public CreatureKillEvent(WellCreature wellCreature) {
        super(wellCreature);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private static HandlerList getHandlerList() {
        return handlers;
    }
}
