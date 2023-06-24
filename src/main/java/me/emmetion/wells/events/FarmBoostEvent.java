package me.emmetion.wells.events;

import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.WellPlayer;

public class FarmBoostEvent extends BuffEvent {

    public FarmBoostEvent(WellPlayer wellPlayer) {
        super(wellPlayer);
    }

    @Override
    public ActiveBuff.BuffType getBuffType() {
        return ActiveBuff.BuffType.FARM_BOOST;
    }


}
