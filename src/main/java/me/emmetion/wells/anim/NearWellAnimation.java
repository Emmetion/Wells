package me.emmetion.wells.anim;

import me.emmetion.wells.Wells;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class NearWellAnimation extends BukkitRunnable implements Animation {

    private Random random = new Random();
    private Queue<CoinType> particleQueue = new LinkedList<>();

    private Location center;
    private final Particle particle = Particle.END_ROD;
    private float angle = 0.0f;
    private final double radius = 3.0f;

    private Well well;

    public NearWellAnimation(Well well) {
        this.well = well;
    }


    @Override
    public void run() {
        center = well.getHologramLocation().clone().subtract(0,1,0); // this causes the particle animation to move when "/wells holo add/sub x y z" is used.

        World world = center.getWorld();

        double x1 = center.getX() + radius * Math.cos(angle);
        double y1 = center.getY() + (random.nextFloat(1) * .3); // (y_radius * Math.sin(angle));
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
            if (player == null || !wellPlayer.canSeeParticles()) {
                continue;
            }
            player.spawnParticle(particle, location1, 1, 0.001, 0, 0.001, 0.01);
            player.spawnParticle(particle, location2, 1, 0.001, 0, 0.001, 0.01);

            player.spawnParticle(Particle.DRAGON_BREATH, location1, 1, 0.001, 0, 0.001, 0.01);
            player.spawnParticle(Particle.SCRAPE, location2, 1, 0.001, 0, 0.001, 0.01);

            // this is written twice, each with different locations. (2 spiraling trails).
            if (!particleQueue.isEmpty()) {
                CoinType cointype = particleQueue.poll();
                spawnTrailByCoinType(location1, cointype);
            }

            if (!particleQueue.isEmpty()) {
                CoinType cointype = particleQueue.poll();
                spawnTrailByCoinType(location2, cointype);
            }
        }

        angle += 0.1;
    }

    @Override
    public void start() {
        runTaskTimer(Wells.plugin, 4, 3);
    }

    public void enqueueDepositedCoinType(CoinType cointype) {
        this.particleQueue.add(cointype);
    }

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