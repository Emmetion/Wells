package me.emmetion.wells.menu;


import me.emmetion.wells.Wells;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static me.emmetion.wells.util.Utilities.getColor;

public abstract class Menu implements InventoryHolder, Listener {

    protected ItemStack nextPage = Utilities.createItemStack(Material.ARROW, 1, Component.text("Next Page"), null);
    protected ItemStack prevPage = Utilities.createItemStack(Material.ARROW, 1, Component.text("Previous Page"), null);

    protected Inventory inventory;

    protected final PlayerMenuUtility playerMenuUtility;
    protected final Wells wells;

    private boolean isClosed = false;

    protected ItemStack FILLER_GLASS = Utilities.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, Component.text(""), null);

    public abstract int getSlots();

    public abstract String getTitle();

    public Menu(Wells wells, PlayerMenuUtility utility){
        this.wells = wells;
        this.playerMenuUtility = utility;
    }

    @EventHandler
    public abstract void handleClick(InventoryClickEvent e);

    @EventHandler
    public abstract void handleClose(InventoryCloseEvent e);

    public abstract boolean cancelAllClicks();

    public abstract void setMenuItems();

    public void open(){
        inventory = Bukkit.createInventory(this, getSlots(), Component.text(Utilities.getColor(getTitle())));
        Menu menu = this;
        this.setMenuItems();

        playerMenuUtility.getOwner().openInventory(inventory);

        // does this work? well find out.
        if (!(this instanceof AnimatedMenu)) {
            playerMenuUtility.getWellPlayer().getBukkitPlayer().sendMessage("Not an animated menu.");
            return;
        }


        // Handle AnimatedMenu's update methods.
        Bukkit.getScheduler().runTaskTimer(
                wells,
                bukkitTask -> {
                    AnimatedMenu animatedMenu = (AnimatedMenu) menu;
                    Player player = menu.playerMenuUtility.getOwner();
                    if (isClosed) {
                        player.sendMessage("Animated task has been cancelled.");
                        bukkitTask.cancel();
                        return;
                    }

                    player.sendMessage("Animation task called.");
                    animatedMenu.update();
                },
                20L,
                20L
        );
    }

    public void setClosed() {
        if (isClosed)
            throw new IllegalArgumentException("Cannot close a menu twice!");
        this.isClosed = true;
    }


    @Override
    public @NotNull Inventory getInventory(){ return inventory; }

}