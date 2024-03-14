package me.emmetion.wells.creature.factories;

import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.creature.creatures.zombies.ExcessWaterZombie;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ExcessWaterZombieFactory implements CreatureFactory {

    @Override
    public WellCreature createCreature(Well well, @Nullable Location location) {
        Location location1 = well.getLocation().clone();
        Location subtract = location1.subtract(0, 1, 0);


        ArrayList<Block> sphere = new ArrayList<>();
        World world = subtract.getWorld();

        // #_0   means original.
        int x_o = subtract.getBlockX();
        int y_o = subtract.getBlockY();
        int z_o = subtract.getBlockZ();

        for (int x = x_o - 2; x <= x_o + 2; x++) {
            for (int y = y_o - 1; y >= y_o + 1; y--) {
                for (int z = z_o - 2; z <= z_o + 2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, block.getLocation().add(.5, .5, .5), 3, 0, 0, 0,0, null, true);
                    if (block.getType() != Material.AIR && !block.equals(location.getBlock())) {
                        // Use the block below the cauldron as needed
                        sphere.add(block);
                    }
                }
            }
        }
        long count = sphere.stream().filter(block -> block.getType() == Material.WATER).count();
        System.out.println("Water blocks: " + count);

        return new ExcessWaterZombie(well, location1);
    }
}
