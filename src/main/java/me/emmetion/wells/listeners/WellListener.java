package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.events.CoinTossEvent;
import me.emmetion.wells.menu.PlayerMenuUtility;
import me.emmetion.wells.menu.WellMenu;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.slf4j.helpers.Util;

import java.util.ArrayList;
import java.util.Collection;

public class WellListener implements Listener {

    /**
     * Dependency Injection of WellManager.
     */
    private WellManager manager;


    //TODO
    // Change this to a HashMap<Player, Boolean>. Only send the cooldown message once,
    // then check if the HashMap is true when sending further messages.
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
        Well well = manager.getWellByTownName(town.getName());
        well.addHologramLocation(0.5f, 2, 0.5f);
        event.setCancelled(true);
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
    public void onItemThrow(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItemDrop();

        if (!Utilities.isCoin(itemDrop.getItemStack()))
            return;

        if (playersOnCooldown.contains(player)) {
            player.sendMessage(Component.text("You are on cooldown!").color(TextColor.color(255, 0, 0)));
            event.setCancelled(true);
            return;
        }
        player.sendActionBar(Component.text("You have thrown a coin!"));
        // Prepare to call custom event.

        WellPlayer wellPlayer = manager.getWellPlayer(player);

        Bukkit.getPluginManager().callEvent(new CoinTossEvent(wellPlayer, itemDrop));

        if (!playersOnCooldown.contains(player))
            playersOnCooldown.add(player);

        Bukkit.getScheduler().runTaskLater(Wells.plugin, () -> {
            this.playersOnCooldown.remove(player);
        }, 3 * 20); // Removes a players cooldown after 60 ticks (3seconds).

    }

    @EventHandler
    public void onCoinTossEvent(CoinTossEvent event) {
        // Other plugins can now listen in on this event, and determine whether they want to cancel it for themselves
        // or not.
        DroppedCoinRunnable runnable = new DroppedCoinRunnable(Wells.plugin,
                event.getDroppedItem(),
                event.getPlayer(),
                this.manager,
                playersOnCooldown);
        runnable.runTaskTimer(Wells.plugin, 1, 1);

    }

    @EventHandler
    public void onWellClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if (!manager.isWell(clickedBlock.getLocation()))
            return;

        Well well = manager.getWellFromLocation(clickedBlock.getLocation());
        if (well == null)
            return;

        e.setCancelled(true);

        WellMenu wellMenu = new WellMenu(Wells.plugin, well,
                new PlayerMenuUtility(player, manager.getWellPlayer(player)));
        wellMenu.open();
    }
}
