package me.emmetion.wells.menu;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.Arrays;

import static me.emmetion.wells.util.Utilities.getColor;

public class WellMenu extends Menu implements AnimatedMenu {

    private final Well well;

    private int currentFrame = 1;

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

        int rawSlot = e.getRawSlot();

        player.sendMessage("clicked_slot: " + rawSlot);

        if (rawSlot == 13) {
            player.sendMessage(getColor("You have clicked on the middle &bcauldron&r!"));
        } else if (rawSlot == 8) {
            WellPlayer wellPlayer = this.playerMenuUtility.getWellPlayer();
            boolean new_option = wellPlayer.toggleParticles();

            if (new_option) {
                player.sendMessage(getColor("You have set your particles to: &aON"));
            } else {
                player.sendMessage(getColor("You have set your particles to: &cOFF"));
            }

            setMenuItems();
        } else if (rawSlot == 11) {
            player.sendMessage(Component.text("Buff1: " + well.getBuff1().toString()));
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
        } else if (rawSlot == 15) {
            player.sendMessage(Component.text("Buff2: " + well.getBuff2().toString()));
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        Player owner = playerMenuUtility.getOwner();

        InventoryCloseEvent.Reason reason = e.getReason();

        if (reason.equals(InventoryCloseEvent.Reason.PLAYER)) {
            owner.sendMessage(Component.text("You have closed the well menu."));
        }

        setClosed();
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void setMenuItems() {
//        well.getBuffs().forEach(ActiveBuff::update);

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
                Component.text(ChatColor.DARK_GREEN + well.getWellName()),
                Arrays.asList(
                        Component.text(ChatColor.YELLOW + "Level: " + well.getWellLevel()),
                        Component.text(ChatColor.BLUE + "XP: " + well.getExperience()),
                        Component.text(ChatColor.GRAY + "NearbyPlayers: " + ChatColor.YELLOW + ChatColor.UNDERLINE + well.getNearbyPlayers().size()),
                        Component.text(ChatColor.GRAY + "Pretty Position: " + well.prettyPosition()),
                        Component.text(ChatColor.GRAY + "Hologram Vector: " + well.getHologramLocation().toVector().toString())
                ));

        this.inventory.setItem(13, cauldron);

        // set buff items
        this.inventory.setItem(11, createBuffItem(well.getBuff1(), 1));
        this.inventory.setItem(15, createBuffItem(well.getBuff2(), 2));
    }

    public ItemStack createBuffItem(ActiveBuff buff, int id) {
        String buffID = buff.getBuffID();
        BuffType buffType = buff.getBuffType();

        Material woolColor;
        Duration duration = buff.getRemainingDuration();
        String timeRemaining = buff.getEndDateAsString();
        String durLeft;

        if (duration.isZero() || duration.isNegative()) {
            woolColor = Material.RED_WOOL;
            durLeft = "&c" + timeRemaining;
        }
        else if (duration.isPositive()) {
            woolColor = Material.GREEN_WOOL;
            durLeft = "&a" + timeRemaining;
        }
        else {
            woolColor = Material.ORANGE_WOOL;
            durLeft = "&cNone.";
        }


        ItemStack item = new ItemStack(woolColor);
        ItemMeta itemMeta = item.getItemMeta();

        // finally fixed switch statement.
        switch (buffType) {
            case RESISTANCE, GREEN_THUMB -> {
                itemMeta.displayName(Component.text(getColor("&a" + StringUtils.capitalize(buffType.getBuffID().toLowerCase()) + " Buff. &e(&ccrop_type&e)")));
                itemMeta.lore(Arrays.asList(
                        Component.text(getColor("&7Duration Left: " + durLeft)),
                        Component.text(getColor("Level: ")))
                );
            }
            case NONE -> {
                itemMeta.displayName(Component.text("Empty Buff..."));
                itemMeta.lore(Arrays.asList(
                        Component.text(getColor("You have no buff in this slot!")),
                        Component.text(getColor("&7Deposit &6Gold Coins &7 for a chance to get a buff!")))
                );
            }
            default -> itemMeta.displayName(Component.text("Unknown buff... '" + buffID + "'"));
        }
        item.setItemMeta(itemMeta);

        return item;
    }

    @Override
    public int getCurrentFrame() {
        return this.currentFrame;
    }

    @Override
    public int updateInterval() {
        return 2;
    }

    @Override
    public void update() {

        // Every 5 ticks, we perform our update and then continue.
        if (currentFrame % 2 == 0) {
            inventory.setItem(11, createBuffItem(well.getBuff1(), 1));
            inventory.setItem(15, createBuffItem(well.getBuff2(), 2));
        }

        this.currentFrame += 1;

        playerMenuUtility.sendMessage(Component.text("Menu is open and animating.®"));
    }

}
