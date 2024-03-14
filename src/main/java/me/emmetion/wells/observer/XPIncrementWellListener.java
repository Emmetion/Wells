package me.emmetion.wells.observer;

import com.palmergames.bukkit.towny.TownyAPI;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;

public class XPIncrementWellListener implements WellListener {

    private final TownyAPI townyAPI = TownyAPI.getInstance();
    private final Well well;

    public XPIncrementWellListener(Well well) {
        this.well = well;
    }

    @Override
    public void update() {
        // we don't handle for XPIncrement yet.
        well.depositCoin(CoinType.BRONZE_COIN);
    }
}
