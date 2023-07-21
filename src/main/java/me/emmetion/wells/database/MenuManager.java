package me.emmetion.wells.database;

import me.emmetion.wells.Wells;
import me.emmetion.wells.menu.AnimatedMenu;
import me.emmetion.wells.menu.Menu;
import me.emmetion.wells.menu.WellMenu;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class MenuManager {

    private static MenuManager instance;

    private final Wells wells;

    private HashMap<UUID, Menu> currentlyOpenMenus = new HashMap<>();

    private MenuManager(Wells wells) {
        this.wells = wells;
    }

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager(Wells.plugin);
        }
        return instance;
    }

    public void openMenu(Menu menu, Player player) {
        openMenu(menu, player.getUniqueId());
    }

    public void openMenu(Menu menu, UUID uuid) {
        if (menu == null || uuid == null) {

        }
    }

    public void handleTick() {
        Collection<Menu> menus = currentlyOpenMenus.values();
        menus.forEach(menu -> {
                    if (menu instanceof AnimatedMenu animatedMenu) { // ???????????? apparently this is a thing.
                        animatedMenu.update();
                    }
                });
    }

    public boolean isInWellMenu(Player player) {
        if (player == null)
            return false;

        UUID uuid = player.getUniqueId();
        return isInWellMenu(uuid);
    }

    public boolean isInWellMenu(UUID uuid) {
        if (uuid == null)
            return false;

        return this.currentlyOpenMenus.containsKey(uuid);
    }




}
