package me.emmetion.wells.observer;

import com.palmergames.bukkit.towny.TownyAPI;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;

public class XPIncrementObserver implements Observer {

    private final TownyAPI townyAPI = TownyAPI.getInstance();
    private final Well well;

    public XPIncrementObserver(Well well) {
        this.well = well;
    }

    @Override
    public void update() {
        // we don't handle for XPIncrement yet.
        well.depositCoin(CoinType.BRONZE_COIN);
    }
}
