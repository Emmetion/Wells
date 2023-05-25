package me.emmetion.wells.listeners;

import me.emmetion.wells.database.Database;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class WellListener implements Listener {

    public WellListener(Database database) {

    }

    @EventHandler
    public void onWellPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block blockPlaced = event.getBlockPlaced();
        Block blockAgainst = event.getBlockAgainst();
        if (blockPlaced.getType().equals(Material.BARREL)) {
            Location location = blockAgainst.getLocation();

        }
    }
}
