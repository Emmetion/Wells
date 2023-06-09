package me.emmetion.wells.anim;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.Well;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class NearWellAnimation extends BukkitRunnable {

    private Random random = new Random();

    private final Location center;
    private final Particle particle = Particle.END_ROD;
    private float angle = 0.0f;
    private float height = 0.0f;
    private final double radius = 3.0f;
    private final double y_radius = 0f;

    private double rad_flip = -0.05;
//    private float size = 2f;

    private Well well;

    public NearWellAnimation(Well well) {
        this.well = well;
        center = well.getHologramLocation().clone().subtract(0,1,0);
    }


    @Override
    public void run() {

        World world = center.getWorld();

        double x1 = center.getX() + radius * Math.cos(angle);
        double y1 = center.getY() + random.nextFloat(1); // (y_radius * Math.sin(angle));
        double z1 = center.getZ() + radius * Math.sin(angle);
        Location location1 = new Location(world, x1, y1, z1);

        // Calculate the position for the second particle
        double x2 = center.getX() + radius * Math.cos(angle + Math.PI);
        double y2 = center.getY() + random.nextFloat(1); // (y_radius * Math.sin(angle + Math.PI));
        double z2 = center.getZ() + radius * Math.sin(angle + Math.PI);
        Location location2 = new Location(world, x2, y2, z2);

        // Spawn the particles at the calculated positions
        world.spawnParticle(particle, location1, 1, 0, 0, 0, 0);
        world.spawnParticle(particle, location2, 1, 0, 0, 0, 0);

        angle += 0.1;
        height += 0.1;


    }

    public void start() {
        runTaskTimer(Wells.plugin, 2, 2);
    }
}



//        size -= 0.05;
//        double increment = (2 * Math.PI) / 50; // Number of particles in the circle
//
//        for (double angle = 0; angle < 2 * Math.PI; angle += increment) {
//            double x = center.getX() + radius * Math.cos(angle);
//            double z = center.getZ() + radius * Math.sin(angle);
//            Location particleLocation = new Location(center.getWorld(), x, center.getY() + angle, z);
//
//            center.getWorld().spawnParticle(particle, particleLocation, 1, new Particle.DustOptions(Color.GREEN, 3));
//        }