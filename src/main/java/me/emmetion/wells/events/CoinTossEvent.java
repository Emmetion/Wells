package me.emmetion.wells.events;

import me.emmetion.wells.model.WellPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class CoinTossEvent extends Event implements Cancellable {

    public static final HandlerList handlers = new HandlerList();

    private Item droppedItem;
    private WellPlayer player;

    private boolean cancelled = false;

    public CoinTossEvent(WellPlayer player, Item droppedItem) {
        this.player = player;
        this.droppedItem = droppedItem;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public Item getDroppedItem() {
        return droppedItem;
    }

    public WellPlayer getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
