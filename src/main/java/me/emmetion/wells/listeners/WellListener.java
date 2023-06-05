package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.runnables.DroppedCoinRunnable;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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

import java.util.ArrayList;
import java.util.Collection;

public class WellListener implements Listener {

    private WellManager manager;

    private Collection<Player> playersOnCooldown = new ArrayList<>();

    public WellListener(WellManager manager) {
        this.manager = manager;
    }

    /**
     * WellPlace event. Listens for a BlockPlaceEvent, then checks for item in hand.
     *
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
     *
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

    @EventHandler
    public void onCoinToss(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItemDrop();

        if (Utilities.isCoin(itemDrop.getItemStack())) {
            if (playersOnCooldown.contains(player)) {
                player.sendMessage(Component.text("You are on cooldown!").color(TextColor.color(255, 0, 0)));
                event.setCancelled(true);
                return;
            }
            player.sendActionBar(Component.text("You have thrown a coin!"));
            DroppedCoinRunnable droppedCoinRunnable = new DroppedCoinRunnable(Wells.plugin, itemDrop, player, manager, playersOnCooldown);
            droppedCoinRunnable.runTaskTimer(Wells.plugin, 1, 1);

            if (!playersOnCooldown.contains(player)) playersOnCooldown.add(player);
            Bukkit.getScheduler().runTaskLater(Wells.plugin, () -> {
                this.playersOnCooldown.remove(player);
            }, 3 * 20);
        }
    }
}
