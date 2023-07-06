package me.emmetion.wells.events;

import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.event.entity.EntityDamageEvent;

public class ResistanceBuffEvent extends BuffEvent {

    private EntityDamageEvent.DamageCause cause;

    public ResistanceBuffEvent(WellPlayer wellPlayer, EntityDamageEvent.DamageCause cause) {
        super(wellPlayer);
        this.cause = cause;
    }

    @Override
    public ActiveBuff.BuffType getBuffType() {
        return ActiveBuff.BuffType.RESISTANCE;
    }

}
