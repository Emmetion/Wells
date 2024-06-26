package me.emmetion.wells.events.buff;

import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * BuffEvents are meant to be a designated class for when a Buff gets activated.
 * BuffEvents store basic information regarding the player in the plugin. It will pass
 * a WellPlayer object, a Player, and BuffData regarding the kind of buff h
 * This will help with handling BuffEvents being called.
 */
public abstract class BuffEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public final Player player;
    public final WellPlayer wellPlayer;
    private boolean cancelled = false;

    public BuffEvent(WellPlayer wellPlayer) {
        Player player1 = Bukkit.getPlayer(wellPlayer.getPlayerUUID());
        if (player1 == null)
            setCancelled(true);
        this.player = player1;
        this.wellPlayer = wellPlayer;

        if (!validEvent()) // This will automatically stop the event from being called,
            // if the passed in variables are invalid.
            setCancelled(true);
    }

    private boolean validEvent() {
        if (this.player == null || this.wellPlayer == null) {
            return false;
        }
        return true;
    }

    public abstract BuffType getBuffType();

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
