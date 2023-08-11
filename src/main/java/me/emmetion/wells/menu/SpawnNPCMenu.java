package me.emmetion.wells.menu;


import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.SpawnNPC;
import me.emmetion.wells.model.CraftableSchematic;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static me.emmetion.wells.util.Utilities.getColor;

public class SpawnNPCMenu extends Menu implements Pageable, AnimatedMenu {

    // The current page of the player viewing.
    private int currentPage = 0;

    private int currentFrame = 0;

    private final List<CraftableSchematic> schematics = new ArrayList<>();
    private final List<Inventory> pages = new ArrayList<>();
    private final SpawnNPC spawnNPC;

    public SpawnNPCMenu(Wells wells, PlayerMenuUtility utility, SpawnNPC spawnNPC) {
        super(wells, utility);

        this.spawnNPC = spawnNPC;
        this.currentPage = 0;
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public String getTitle() {
        return "&bWellbi";
    }

    private final Material[] colorList = {
        Material.BLACK_STAINED_GLASS_PANE,
        Material.GRAY_STAINED_GLASS_PANE,
        Material.GREEN_STAINED_GLASS_PANE,
        Material.BLUE_STAINED_GLASS_PANE,
        Material.ORANGE_STAINED_GLASS_PANE,
        Material.CYAN_STAINED_GLASS_PANE,
        Material.BROWN_STAINED_GLASS_PANE,
        Material.MAGENTA_STAINED_GLASS_PANE,
        Material.RED_STAINED_GLASS_PANE,
        Material.BEACON,
    };

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public void handleClick(InventoryClickEvent e) {
        int rawSlot = e.getRawSlot();

        // This handles slots clicked outside the Menu's Inventory box.
        if (rawSlot > getSlots() || e.getClick().isKeyboardClick()) {
            e.setCancelled(true);
            return;
        }

        ItemStack cursor = e.getCursor();
        // inventory actions could be a really cool interaction manager.
        InventoryAction action = e.getAction();

        if (cursor != null) {
            cursor.setType(colorList[random.nextInt(colorList.length)]);
        }

        // Handle each slowtype of
        switch (rawSlot) {
            case 11 -> {
                previousPage();
            }
            case 15 -> {
                nextPage();
            }
            default -> {
                // e.setCancelled(true);
                return;
            }
        }

    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        HumanEntity player = e.getPlayer();

        player.sendMessage(Component.text(getColor("&bClosed SpawnNPC Menu.")));

        setClosed(); // This is required to stop the animation task.
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void setMenuItems() {
        Inventory inventory = this.getInventory();
        for (int i = 0; i < this.getSlots(); i++) {
            inventory.setItem(i, FILLER_GLASS);
        }

        // set arrows.
        inventory.setItem(11, prevPage);
        inventory.setItem(15, nextPage);
    }

    @Override
    public void nextPage() {
        if (currentPage == this.pages.size()) {
            Player player = playerMenuUtility.getWellPlayer().getBukkitPlayer();
            player.sendMessage(Component.text(getColor("No next page.")));
            return;
        }

        this.currentPage += 1;

        openPage(currentPage);
    }

    @Override
    public void previousPage() {
        if (currentPage == 0) {
            Player player = playerMenuUtility.getWellPlayer().getBukkitPlayer();
            player.sendMessage(Component.text(getColor("Cannot go back a page. ")));
        }
        this.currentPage -= 1;
    }

    @Override
    public void addPage(int pageNum, Inventory inventory) {
        pages.add(inventory);
    }


    private void openPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex > pages.size()) {
            throw new IllegalArgumentException("out of bounds page index provided.");
        }

        this.open();

    }


    @Override
    public int getCurrentFrame() {
        return this.currentFrame;
    }

    //
    @Override
    public int updateInterval() {
        return 0;
    }

    @Override
    public void update() {
        if (this.playerMenuUtility.getOwner() == null)
            return;

        InventoryView openInventory = this.playerMenuUtility.getOwner().getOpenInventory();
        ItemStack cursor = openInventory.getCursor();
        if (cursor == null)
            return;

        cursor.setType(colorList[random.nextInt(colorList.length)]);
    }
}
