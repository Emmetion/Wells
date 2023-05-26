package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.util.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Collection;

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
        Location location = blockAgainst.getLocation();

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
}
