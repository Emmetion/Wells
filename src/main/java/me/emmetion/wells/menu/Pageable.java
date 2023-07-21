package me.emmetion.wells.menu;

import org.bukkit.inventory.Inventory;

public interface Pageable {

    void nextPage();

    void previousPage();

    void addPage(int pageNum, Inventory inventory);

}
