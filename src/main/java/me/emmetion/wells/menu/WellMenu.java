package me.emmetion.wells.menu;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

import static me.emmetion.wells.util.Utilities.getColor;

public class WellMenu extends Menu {

    private Well well;

    public WellMenu(Wells wells, Well well, PlayerMenuUtility utility) {
        super(wells, utility);
        this.well = well;
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public String getTitle() {
        return well.getWellName();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        Player player = this.playerMenuUtility.getOwner();

        e.setCancelled(true);

        int rawslot = e.getRawSlot();

        player.sendMessage("clicked slot: "+rawslot);

        if (rawslot == 13) {
            player.sendMessage("You have clicked on the middle cauldron!");
        } else if (rawslot == 8) {
            WellPlayer wellPlayer = this.playerMenuUtility.getWellPlayer();
            boolean new_option = wellPlayer.toggleParticles();

            if (new_option) {
                player.sendMessage("You have set your particles to: " + ChatColor.GREEN + "ON");
            } else {
                player.sendMessage("You have set your particles to: " + ChatColor.RED + "OFF");
            }

            setMenuItems();
        } else if (rawslot == 11) {
            player.sendMessage(Component.text("Buff1: " + well.getBuff1().toString()));
        } else if (rawslot == 15) {
            player.sendMessage(Component.text("Buff2: " + well.getBuff2().toString()));
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        Player owner = playerMenuUtility.getOwner();
        if (e.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
            owner.sendMessage(Component.text("You have closed the well menu."));
        }
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < 27; i++) {
            this.inventory.setItem(i, FILLER_GLASS);
        }

        boolean see = this.playerMenuUtility.getWellPlayer().canSeeParticles();

        ItemStack item;
        if (see) {
            item = Utilities.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1,
                    Component.text(getColor("&aCan see Particles.")), null);

        } else {
            item = Utilities.createItemStack(Material.RED_STAINED_GLASS_PANE, 1,
                    Component.text(getColor("&cCannot see Particles.")), null);
        }
        this.inventory.setItem(8, item);



        ItemStack cauldron = Utilities.createItemStack(Material.CAULDRON,
                Component.text(ChatColor.GOLD + well.getWellName()),
                Arrays.asList(
                        Component.text(ChatColor.YELLOW + "Level: " + well.getWellLevel()),
                        Component.text(ChatColor.BLUE + "XP: " + well.getExperience()),
                        Component.text(ChatColor.GRAY + "NearbyPlayers: " + ChatColor.YELLOW + ChatColor.UNDERLINE + well.getNearbyPlayers().size()),
                        Component.text(ChatColor.GRAY + "Pretty Position: " + well.prettyPosition()),
                        Component.text(ChatColor.GRAY + "Hologram Vector: " + well.getHologramLocation().toVector().toString())
                ));

        this.inventory.setItem(13, cauldron);

        this.inventory.setItem(11, createBuffItem(well.getBuff1(), 1));
        this.inventory.setItem(15, createBuffItem(well.getBuff2(), 2));
    }

    public ItemStack createBuffItem(ActiveBuff buff, int id) {
        String buffID = buff.getBuffID();
        ActiveBuff.BuffType b = ActiveBuff.BuffType.valueOf(buffID);

        ItemStack item = new ItemStack(Material.WHITE_WOOL);
        ItemMeta itemMeta = item.getItemMeta();

        switch (b) {
            case FARM_BOOST:
                itemMeta.displayName(Component.text(ChatColor.GREEN + "Farm Boost! (id: " + id + ")"));
                itemMeta.lore(Arrays.asList(
                        Component.text(ChatColor.GRAY + "Has Ended: " + ChatColor.RED +  buff.hasEnded()),
                        Component.text(ChatColor.GRAY + "End Time: " + ChatColor.YELLOW + buff.getEndDateAsString())
                ));
                break;
            case NONE:
                itemMeta.displayName(Component.text(ChatColor.GRAY + "No Buff..."));
                itemMeta.lore(Arrays.asList(
                        Component.text(ChatColor.GRAY + "You have no buff in this slot!"),
                        Component.text(ChatColor.GRAY + "Deposit " + ChatColor.GOLD + "Gold Coins" + ChatColor.GRAY + " for a chance to get a buff!")
                ));
                break;
            default:
                itemMeta.displayName(Component.text("Unknown buff... '" + buffID + "'"));
                break;
        }
        item.setItemMeta(itemMeta);

        return item;
    }


    @Override
    public int getCurrentPage() {
        return 1;
    }


}
