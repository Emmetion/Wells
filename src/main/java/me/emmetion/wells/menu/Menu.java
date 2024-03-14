package me.emmetion.wells.menu;


import me.emmetion.wells.Wells;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Menu implements InventoryHolder, Listener {

    protected final PlayerMenuUtility playerMenuUtility;
    protected final Wells wells;
    protected ItemStack nextPage = Utilities.createItemStack(Material.ARROW, 1, Component.text("Next Page"), null);
    protected ItemStack prevPage = Utilities.createItemStack(Material.ARROW, 1, Component.text("Previous Page"), null);
    protected Inventory inventory;
    protected ItemStack FILLER_GLASS = Utilities.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, Component.text(""), null);
    private boolean isClosed = false;

    public Menu(Wells wells, PlayerMenuUtility utility) {
        this.wells = wells;
        this.playerMenuUtility = utility;
    }

    public abstract int getSlots();

    /**
     * This title is automatically converts color codes upon instantiation.
     * @return Title of the menu.
     */
    public abstract String getTitle();

    /**
     * Handles any inventory click for the given menu.
     * @param e The InventoryClickEvent called from Bukkit.
     */
    @EventHandler
    public abstract void handleClick(InventoryClickEvent e);

    /**
     * Handles any inventory close for the given menu.
     * @param e The InventoryCloseEvent called from Bukkit.
     */
    @EventHandler
    public abstract void handleClose(InventoryCloseEvent e);

    public abstract boolean cancelAllClicks();

    public abstract void setMenuItems();

    public void open() {
        this.isClosed = false;

        inventory = Bukkit.createInventory(this, getSlots(), Component.text(Utilities.getColor(getTitle())));
        Menu menu = this;
        menu.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);

        // Handle animated menu's
        // Essentially starting a bukkit task, and ending it when Menu#setClosed() is called
        if (!(this instanceof AnimatedMenu)) {
            return;
        }
        AnimatedMenu animatedMenu = (AnimatedMenu) menu;

        // Handle AnimatedMenu's update methods.
        Bukkit.getScheduler().runTaskTimer(wells, bukkitTask -> {
            Player player = menu.playerMenuUtility.getOwner();
            WellManager manager = Wells.plugin.getWellManager();
            if (isClosed) {
                player.sendMessage("[DEBUG]: ANIMATION TASK CANCELLED");
                bukkitTask.cancel();
                return;
            }

            animatedMenu.update();
        }, animatedMenu.runnableDelay(), animatedMenu.runnablePeriod());
    }

    public void setClosed() {
        if (isClosed)
            throw new IllegalArgumentException("Cannot close a menu twice!");

        this.isClosed = true;
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public List<HumanEntity> getViewers() {
        return this.inventory.getViewers();
    }

}