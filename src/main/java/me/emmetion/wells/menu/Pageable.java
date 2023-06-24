package me.emmetion.wells.menu;

import org.bukkit.inventory.Inventory;

public interface Pageable {

    /**
     * Menus with a pageable interface can call the menu's nextpage method, and check whether the provided input was
     * correct
     */
    void nextPage();

    void previousPage();

    void addPage(int pageNum, Inventory inventory);

}
