package me.emmetion.wells.events;

import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.event.Listener;

public class FarmBoostEvent extends BuffEvent {

    public FarmBoostEvent(WellPlayer wellPlayer) {
        super(wellPlayer);
        super.buffData = ActiveBuff.BuffData.FARM_BOOST;
    }


}
