package me.emmetion.wells.events;

import me.emmetion.wells.creature.WellCreature;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class CreatureEvent extends Event {

    protected static final HandlerList handlers = new HandlerList();

    private final WellCreature wellCreature;

    public CreatureEvent(WellCreature wellCreature) {
        this.wellCreature = wellCreature;
    }

    public WellCreature getWellCreature() {
        return wellCreature;
    }

}
