package me.emmetion.wells.listeners;

import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.menu.Menu;
import me.emmetion.wells.menu.WellMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    private final WellManager manager;

    public MenuListener(WellManager wellManager) {
        this.manager = wellManager;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();

        if (holder instanceof Menu menu) {
            menu.handleClick(e);

            if (menu instanceof WellMenu wellMenu) {
                // .
            }
        }
    }


    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();

        if (holder instanceof Menu menu) {
            menu.handleClose(e);
        }
    }



}
