package me.emmetion.wells.menu;

import me.emmetion.wells.Wells;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static me.emmetion.wells.util.Utilities.getColor;

public abstract class PaginatedMenu extends Menu {
    
    protected ItemStack nextPage = Utilities.createItemStack(Material.ARROW, Component.text(getColor("&eNext Page"))); 
    protected ItemStack prevPage = Utilities.createItemStack(Material.ARROW, Component.text(getColor("&ePrevious Page"))); 
    
    protected int pageIndex = 0;

    public PaginatedMenu(Wells wells, PlayerMenuUtility utility) {
        super(wells, utility);
    }

    public int getPageIndex() {
        return this.pageIndex;
    }





}
