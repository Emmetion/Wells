package me.emmetion.wells.anim;

import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class NearWellAnimation extends Animation {

    private int frame = 0;

    private final Particle particle = Particle.END_ROD;
    private final double radius = 3.0f;

    private Iterator<Color> redToGreen = new ColorGradient(Color.RED, Color.GREEN, 20).iterator();

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

        double y_radius = 0.7;

        double x1 = center.getX() + radius * Math.cos(angle);
        double y1 = center.getY() + (y_radius * Math.sin(angle));
        double z1 = center.getZ() + radius * Math.sin(angle);

        Location location1 = new Location(world, x1, y1, z1);

        // Calculate the position for the second particle
        double x2 = center.getX() + radius * Math.cos(angle + Math.PI);
        double y2 = center.getY() + (y_radius * Math.sin(angle + Math.PI)); // (y_radius * Math.sin(angle + Math.PI));
        double z2 = center.getZ() + radius * Math.sin(angle + Math.PI);

        Location location2 = new Location(world, x2, y2, z2);

        // Spawn the particles at the calculated positions


        well.getNearbyPlayers().stream()
                .filter(WellPlayer::canSeeParticles)
                .forEach(wellPlayer -> {
                    Player player = wellPlayer.getBukkitPlayer();
                    List<WellPlayer> nearbyWellPlayers = well.getNearbyPlayers();

                    int wellLevel = well.getWellLevel();

                    if (wellLevel >= 1) {
                        player.spawnParticle(particle, location1, 1, 0.001, 0, 0.001, 0.01);
                        // this is written twice, each with different locations. (2 spiraling trails).
                        if (!particleQueue.isEmpty()) {
                            CoinType cointype = particleQueue.poll();
                            spawnTrailByCoinType(location1, cointype);
                        }
                    }

                    if (wellLevel >= 2) {
                        player.spawnParticle(particle, location2, 1, 0.001, 0, 0.001, 0.01);
                        if (!particleQueue.isEmpty()) {
                            CoinType cointype = particleQueue.poll();
                            spawnTrailByCoinType(location2, cointype);
                        }
                    }

                    handleActiveBuffParticleSpawn(well.getBuff1(), location1, nearbyWellPlayers);
                    handleActiveBuffParticleSpawn(well.getBuff2(), location2, nearbyWellPlayers);
                });


        angle += 0.1;
    }

    private void handleActiveBuffParticleSpawn(@NotNull ActiveBuff buff, @NotNull Location particleLoc, Collection<WellPlayer> receivers) {
        if (buff.isNone()) {
            return;
        }

        Particle p = buff.getWellParticle();

        if (p == null) {
            return;
        }

        switch (p) {
            // Theoretically this will create a color gradient alternating through red to green in 20 steps.
            case NOTE -> {
                Color c;
                if (!redToGreen.hasNext()) {
                    ColorGradient rtg = (ColorGradient) redToGreen;
                    // alternate between colors.
                    // could probably make a rainbow here.
                    Color endColor = rtg.getEndColor();
                    if (endColor.equals(Color.RED)) {
                        redToGreen = new ColorGradient(Color.RED, Color.GREEN).iterator();
                    } else if (endColor.equals(Color.GREEN))  {
                        redToGreen = new ColorGradient(Color.GREEN, Color.RED).iterator();
                    } else {
                        // this should not happen rn.
                        redToGreen = new ColorGradient(Color.BLACK, Color.BLACK).iterator();
                    }
                }
                c = redToGreen.next();
                Particle.REDSTONE.builder()
                        .location(particleLoc)
                        .color(c.getRed(), c.getGreen(), c.getBlue())
                        .offset(0.001, 0.001, 0.001)
                        .receivers(receivers.stream()
                                        .map(WellPlayer::getBukkitPlayer)
                                        .toList())
                        .spawn();
            }
            case VILLAGER_HAPPY -> {
                // Using AnimationAPI we create a new anonymous object, name it with AnimationSettings then start.
                World world = particleLoc.getWorld();
                Animation a = new Animation() {

                    Bat bat;
                    int frame = 0;

                    @Override
                    public void run() {

                        if (frame == 0) {
                            bat = world.spawn(particleLoc, Bat.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            bat.setInvulnerable(true);
                            bat.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10, 1, false, true, false));
                            bat.setTargetLocation(particleLoc.add(0, 100, 0));
                        } else if (frame == 5) {
                            bat.remove();
                            this.cancel();
                        }

                        frame++;
                    }

                    @Override
                    public AnimationSettings getAnimationSettings() {
                        return new AnimationSettings("Happy Villager", 1, 1);
                    }
                };
                a.start();
            }
        }


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
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(color.red(), color.green(), color.blue()), size));
        }
    }

}

/** ChatGPT'd **/
class ColorGradient implements Iterable<Color> {

    private final Color startColor;
    private final Color endColor;
    private final int steps;

    public ColorGradient(Color startColor, Color endColor, int steps) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.steps = steps;
    }

    public Color getStartColor() {
        return startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    public int getSteps() {
        return steps;
    }

    // Default steps = 20;
    public ColorGradient(Color startColor, Color endColor) {
        this(startColor, endColor, 20);
    }

    @Override
    public Iterator<Color> iterator() {
        return new ColorGradientIterator();
    }

    private class ColorGradientIterator implements Iterator<Color> {

        private int currentStep;

        @Override
        public boolean hasNext() {
            return currentStep < steps;
        }

        @Override
        public Color next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            float ratio = (float) currentStep / (steps - 1);
            int red = (int) (startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed()));
            int green = (int) (startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen()));
            int blue = (int) (startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue()));
            int alpha = (int) (startColor.getAlpha() + ratio * (endColor.getAlpha() - startColor.getAlpha()));

            currentStep++;

            return new Color(red, green, blue, alpha);
        }
    }

}