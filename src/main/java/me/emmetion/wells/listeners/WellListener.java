package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.papermc.paper.event.entity.WaterBottleSplashEvent;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.runnables.DroppedCoinRunnable;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;
import java.util.HashMap;

public class WellListener implements Listener {

    private WellManager manager;

    public WellListener(WellManager manager) {
        this.manager = manager;
    }

    /**
     * WellPlace event. Listens for a BlockPlaceEvent, then checks for item in hand.
     * @param event
     */
    @EventHandler
    public void onWellPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block blockAgainst = event.getBlockAgainst();

        if (!Utilities.isWellBlockItem(event.getItemInHand())) {
            return;
        }

        if (manager.wellExistsForPlayer(player)) {
            player.sendMessage(ChatColor.RED + "You have a well, you cannot place another.");
            event.setCancelled(true);
            return;
        }
        Town town = TownyAPI.getInstance().getTown(event.getPlayer());
        manager.wellExists(town.getName());

        if (!blockAgainst.getType().equals(Material.CAULDRON)) {
            player.sendMessage(ChatColor.RED + "Not a valid well position! Read item description.");
            event.setCancelled(true);
            return;
        }

        Collection<Block> blocksUnderneathLocation = Utilities.getBlocksUnderneathLocation(blockAgainst.getLocation());
        long waterCount = blocksUnderneathLocation.stream().filter(block -> block.getType().equals(Material.WATER)).count();
        if (waterCount < 5) // Need at least 5 water-blocks from well position.
            return;

        player.sendMessage(ChatColor.GREEN + "Well Criteria Met!...");
        // all criteria met, now we create it in database
        manager.createWell(player, blockAgainst);

    }

    /**
     * BlockBreak event. Checks a block in a BlockBreakEvent for being a Well Block. Cancelled if true.
     * @param event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location block_location = block.getLocation();

        if (manager.isWell(block_location)) {
            event.setCancelled(true); // prevents the well block from being modified
            player.sendMessage("You cannot break a well like this!");
        }

    }


    private HashMap<String, Integer> coinTossCooldown = new HashMap<>();

    @EventHandler
    public void onCoinToss(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItemDrop();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (Utilities.isCoin(itemDrop.getItemStack())) {
            if (coinTossCooldown.containsKey(player.getName())) {
                player.sendMessage(
                        Component.text("You are on cooldown! (" + coinTossCooldown.get(player.getName()))
                        .color(TextColor.color(255,0,0))
                );
                return;
            }
            player.sendMessage("Is coin!");
            DroppedCoinRunnable droppedCoinRunnable = new DroppedCoinRunnable(Wells.plugin, itemDrop, player, manager);
            droppedCoinRunnable.runTaskTimer(Wells.plugin, 1, 1);
        }

//        //if (Utilities.isCoin(itemInHand)) {
//        if (coinTossCooldown.containsKey(player.getName())) {
//            player.sendMessage(ChatColor.BLUE + "You are on cooldown. [WELLS]");
//            event.setCancelled(true);
//            return;
//        }
//

//        // schedule 1 second cooldown with bukkit scheduler
//        // {}
    }

//    @EventHandler
//    public void onCoinEnterWater(Event e) {
//        e.
//    }

}
