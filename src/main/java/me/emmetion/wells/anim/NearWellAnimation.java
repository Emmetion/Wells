package me.emmetion.wells.anim;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;

public class NearWellAnimation extends Animation {

    private final Particle particle = Particle.END_ROD;
    private final double radius = 3.0f;
    private Random random = new Random();
    private Queue<CoinType> particleQueue = new LinkedList<>();

    private Location center;
    private float angle = 0.0f;
    private Well well;

    public NearWellAnimation(Well well) {
        this.well = well;
    }


    @Override
    public void run() {
        center = well.getHologramLocation().clone().subtract(0, 1, 0); // this causes the particle animation to move when "/wells holo add/sub x y z" is used.

        World world = center.getWorld();

        double y_radius = 0.4;

        double x1 = center.getX() + radius * Math.cos(angle);
        double y1 = center.getY() + (y_radius * Math.sin(angle));
        double z1 = center.getZ() + radius * Math.sin(angle);
        Location location1 = new Location(world, x1, y1, z1);

        // Calculate the position for the second particle
        double x2 = center.getX() + radius * Math.cos(angle + Math.PI);
        double y2 = center.getY() + (random.nextFloat(1) * .3); // (y_radius * Math.sin(angle + Math.PI));
        double z2 = center.getZ() + radius * Math.sin(angle + Math.PI);
        Location location2 = new Location(world, x2, y2, z2);

        // Spawn the particles at the calculated positions
        for (WellPlayer wp : well.getNearbyPlayers()) {
            UUID playerUUID = wp.getPlayerUUID();
            Player player = Bukkit.getPlayer(playerUUID);

            WellPlayer wellPlayer = Wells.plugin.getWellManager().getWellPlayer(player);
            if (wellPlayer == null || !wellPlayer.canSeeParticles()) {
                continue;
            }
            int wellLevel = well.getWellLevel();

            if (wellLevel > 0) {
                player.spawnParticle(particle, location1, 1, 0.001, 0, 0.001, 0.01);
                // this is written twice, each with different locations. (2 spiraling trails).
                if (!particleQueue.isEmpty()) {
                    player.sendMessage("Polling Cointype...");
                    CoinType cointype = particleQueue.poll();
                    spawnTrailByCoinType(location1, cointype);
                }
            }

            if (wellLevel > 1) {
                player.spawnParticle(particle, location2, 1, 0.001, 0, 0.001, 0.01);
                if (!particleQueue.isEmpty()) {
                    player.sendMessage("Polling Cointype...");
                    CoinType cointype = particleQueue.poll();
                    spawnTrailByCoinType(location2, cointype);
                }
            }

            if (well.hasBuff1()) {
                if (well.getBuff1().hasWellParticle())
                    player.spawnParticle(well.getBuff1().getWellParticle(), location1, 1, 0.001, 0, 0.001, 0.01);
            }

            if (well.hasBuff2()) {
                if (well.getBuff2().hasWellParticle())
                    player.spawnParticle(well.getBuff2().getWellParticle(), location2, 1, 0.001, 0, 0.001, 0.01);
            }
        }

        angle += 0.1;
    }

    @Override
    public AnimationSettings getAnimationSettings() {
        return new AnimationSettings("NearWellAnimation", 4, 3);
    }

    /**
     * When a player deposits a coin, the CoinType is enqueued into the Particle Queue.
     * Every tick, two particles from the particle queue are polled and displayed.
     *
     * @param cointype
     */
    public void enqueueDepositedCoinType(CoinType cointype) {
        this.particleQueue.add(cointype);
    }

    /**
     * This creates the particle trail of a coin after being deposited.
     *
     * @param location
     * @param cointype
     */
    public void spawnTrailByCoinType(Location location, CoinType cointype) {
        // Create a Firework entity
        TextColor color = cointype.getColor();
        Location temp = location.clone();
        float size = 2f;
        for (int i = 0; i < 10; i++) {
            temp = temp.add(0, 0.3, 0);
            size -= 0.2f;
            location.getWorld().spawnParticle(Particle.REDSTONE, temp, 1, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(color.red(), color.green(), color.blue()), size));
        }
    }

}