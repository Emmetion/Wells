package me.emmetion.wells.listeners;

import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.WellManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class WellListener implements Listener {

    private WellManager manager;

    public WellListener(WellManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onWellPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block blockPlaced = event.getBlockPlaced();
        Block blockAgainst = event.getBlockAgainst();
        if (blockPlaced.getType().equals(Material.BARREL)) {
            player.sendMessage(ChatColor.RED + "Well place event");
            Location location = blockAgainst.getLocation();

        }
    }
}
