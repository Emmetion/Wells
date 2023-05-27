package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
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

        boolean water_count = Utilities.blockRequirement(blockAgainst, Material.WATER, 3);
        player.sendMessage(ChatColor.BLUE + "WaterCount: " + water_count);
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

        player.sendMessage("BlockBreakEvent");
        player.sendMessage("Cache Size: " + manager.getCache().size());

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
        //if (Utilities.isCoin(itemInHand)) {
        if (coinTossCooldown.containsKey(player.getName())) {
            player.sendMessage(ChatColor.BLUE + "You are on cooldown. [WELLS]");
            event.setCancelled(true);
            return;
        }

        coinTossCooldown.put(player.getName(), 1);
        Bukkit.getScheduler().runTaskLater(Wells.plugin, () -> coinTossCooldown.remove(player.getName()), 3 * 20);
        // schedule 1 second cooldown with bukkit scheduler
        // }
    }


}
