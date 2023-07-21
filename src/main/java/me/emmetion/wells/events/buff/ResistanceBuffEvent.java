package me.emmetion.wells.events.buff;

import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.event.entity.EntityDamageEvent;

public class ResistanceBuffEvent extends BuffEvent {

    private EntityDamageEvent.DamageCause cause;

    public ResistanceBuffEvent(WellPlayer wellPlayer, EntityDamageEvent.DamageCause cause) {
        super(wellPlayer);
        this.cause = cause;
    }

    @Override
    public BuffType getBuffType() {
        return BuffType.RESISTANCE;
    }

}
