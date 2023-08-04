package me.emmetion.wells.events.buff;

import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.WellPlayer;

public final class GreenThumbEvent extends BuffEvent {

    public GreenThumbEvent(WellPlayer wellPlayer) {
        super(wellPlayer);
    }

    @Override
    public BuffType getBuffType() {
        return BuffType.GREEN_THUMB;
    }


}
