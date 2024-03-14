package me.emmetion.wells.events.buff;

import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.block.Block;

public final class GreenThumbEvent extends BuffEvent {

    private final Block farmedBlock;

    public GreenThumbEvent(WellPlayer wellPlayer, Block farmedBlock) {
        super(wellPlayer);
        this.farmedBlock = farmedBlock;
    }

    @Override
    public BuffType getBuffType() {
        return BuffType.GREEN_THUMB;
    }


    public Block getFarmedBlock() {
        return farmedBlock;
    }

}
