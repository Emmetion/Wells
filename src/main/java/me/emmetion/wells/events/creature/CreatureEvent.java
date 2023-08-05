package me.emmetion.wells.events.creature;

import me.emmetion.wells.creature.WellCreature;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class CreatureEvent extends Event {

    protected static final HandlerList handlers = new HandlerList();

    @NotNull
    private final WellCreature wellCreature;

    public CreatureEvent(@NotNull WellCreature wellCreature) {
        this.wellCreature = wellCreature;
    }

    @NotNull
    public WellCreature getWellCreature() {
        return this.wellCreature;
    }

}
