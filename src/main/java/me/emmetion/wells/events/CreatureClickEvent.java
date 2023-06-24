package me.emmetion.wells.events;

import me.emmetion.wells.creature.WellCreature;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CreatureClickEvent extends CreatureEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final ClickType clickType;

    public CreatureClickEvent(Player player, WellCreature wellCreature, ClickType clickType) {
        super(wellCreature);
        this.player = player;
        this.clickType = clickType;
    }

    public Player getPlayer() {
        return player;
    }

    public ClickType getClickType() {
        return clickType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ClickType {
        LEFT_CLICK,
        RIGHT_CLICK;
    }
}

