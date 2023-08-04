package me.emmetion.wells.events;

import me.emmetion.wells.model.WellPlayer;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class CoinTossEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Item droppedItem;
    private final WellPlayer player;

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

    public static @NotNull HandlerList getHandlerList() { return handlers; }
}
