package me.emmetion.wells.events;

import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class HarvestCropEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private Block cropBlock;
    private Material material;
    private WellPlayer player;

    private boolean cancelled;

    public HarvestCropEvent(Block block, Material blockMaterial, WellPlayer player) {
        this.cropBlock = block;
        this.material = blockMaterial;
        this.player = player;
    }

    public Block getCropBlock() {
        return cropBlock;
    }

    public WellPlayer getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
