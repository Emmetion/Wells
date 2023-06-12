package me.emmetion.wells.listeners;

import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.menu.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    private WellManager manager;

    public MenuListener(WellManager wellManager) {
        this.manager = wellManager;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            Menu menu = (Menu) holder;
            menu.handleClick(e);
        }
    }


    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            Menu menu = (Menu) holder;
            menu.handleClose(e);
        }
    }

}
