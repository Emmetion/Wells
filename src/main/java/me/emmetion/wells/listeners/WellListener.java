package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.events.CoinTossEvent;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.menu.PlayerMenuUtility;
import me.emmetion.wells.menu.menus.WellMenu;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static me.emmetion.wells.util.Utilities.getColor;

public class WellListener implements Listener {

    private final WellManager manager;
    private final Map<Player, Boolean> playersOnCooldown = new HashMap<>();

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

        PlayerInventory inventory = player.getInventory();
        inventory.removeItemAnySlot();

        if (manager.wellExistsForPlayer(player)) {
            player.sendMessage(getColor("&cYou have a well, you cannot place another."));
            event.setCancelled(true);
            return;
        }

        Town town = TownyAPI.getInstance().getTown(event.getPlayer());
        if (town == null) {
            player.sendMessage(getColor("&cYou cannot make a well if you're not part of a town!"));
            event.setCancelled(true);
            // player is not part of a town
            return;
        }
        String townName = town.getName();

        if (!blockAgainst.getType().equals(Material.CAULDRON) && !blockAgainst.getType().equals(Material.WATER_CAULDRON)) {
            Material type = blockAgainst.getType();

            player.sendMessage(getColor("&cNot a valid well position! Read item description."));
            player.sendMessage("Reached here");
            player.sendMessage("Block type: " + type);
            event.setCancelled(true);
            return;
        }

        Collection<Block> blocksUnderneathLocation = Utilities.getBlocksUnderneathLocation(blockAgainst.getLocation());
        long waterCount = blocksUnderneathLocation.stream().filter(b -> b.getType().equals(Material.WATER)).count();
        if (waterCount < 5) // Need at least 5 water-blocks from well position.
            return;

        player.sendMessage(getColor("&aWell Criteria Met!..."));
        // all criteria met, now we create it in database
        boolean successful = manager.createWell(player, blockAgainst);

        if (successful) {
            Well well = manager.getWellByTownName(townName);
            well.addHologramLocation(0.5f, 2, 0.5f);

        } else {
            player.sendMessage(getColor("&cFailed to create well from wellmanager."));

        }

        event.setCancelled(true);
    }

    /**
     * BlockBreak event. Checks a block in a BlockBreakEvent for being a Well Block. Cancelled if true.
     *
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

        if (playersOnCooldown.containsKey(player)) {
            if (!playersOnCooldown.get(player)) {
                player.sendMessage(Component.text("You are on cooldown!").color(TextColor.color(255, 0, 0)));
                playersOnCooldown.put(player, true);
            }
            event.setCancelled(true);
            return;
        }
        player.sendActionBar(Component.text("You have thrown a coin!"));
        // Prepare to call custom event.

        WellPlayer wellPlayer = manager.getWellPlayer(player);

        Bukkit.getPluginManager().callEvent(new CoinTossEvent(wellPlayer, itemDrop));

        if (!playersOnCooldown.containsKey(player))
            playersOnCooldown.put(player, false);

        Bukkit.getScheduler().runTaskLater(Wells.plugin, () -> {
            this.playersOnCooldown.remove(player);
        }, 3 * 20); // Removes a players cooldown after 60 ticks (3seconds).

    }

    @EventHandler
    public void onCoinTossEvent(CoinTossEvent event) {
        // Other plugins can now listen in on this event, and determine whether they want to cancel it for themselves
        // or not.

        DroppedCoinRunnable runnable = new DroppedCoinRunnable(Wells.plugin, event.getDroppedItem(), event.getPlayer(), manager, playersOnCooldown);
        runnable.runTaskTimer(Wells.plugin, 1, 1);
        event.getPlayer().sendMessage("CoinTossEvent!");
    }

    @EventHandler
    public void onWellClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        Action action = e.getAction();

        if (clickedBlock == null || !action.equals(Action.RIGHT_CLICK_BLOCK))
            return;

        Location blockLocation = clickedBlock.getLocation();

        if (!manager.isWell(blockLocation))
            return;

        Location interactionPoint = e.getInteractionPoint();
        player.sendMessage("You have clicked on a well block at " + interactionPoint.toString());
        World world = interactionPoint.getWorld();
        world.spawnParticle(Particle.CRIT, interactionPoint, 3);

        Well well = manager.getWellFromLocation(blockLocation);
        if (well == null)
            return;

        e.setCancelled(true);

        // Open a new WellMenu, as the player has clicked on a wellblock.
        WellMenu wellMenu = new WellMenu(Wells.plugin, well, new PlayerMenuUtility(player, manager.getWellPlayer(player)));
        wellMenu.open();
    }
}
