package me.emmetion.wells.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;

public class Utilities {

    public static Collection<Block> getBlocksAround(Location location, int radius) {
        ArrayList<Block> sphere = new ArrayList<>();
        for (int y = -radius; y < radius; y++) {
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if (Math.sqrt((x*x) + (y*y) + (z*z)) <= radius) {
                        final Block b = location.getWorld().getBlockAt(x + location.getBlockX(), y + location.getBlockY(), z + location.getBlockZ());
                        sphere.add(b);
                    }
                }
            }
        }


        System.out.println("Sphere block size " + sphere.size() + " from radius " + radius + ".");

        return sphere;
    }

}
