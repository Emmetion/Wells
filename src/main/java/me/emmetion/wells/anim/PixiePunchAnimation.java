package me.emmetion.wells.anim;

import me.emmetion.wells.creature.creatures.Pixie;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class PixiePunchAnimation extends Animation {

    private final Pixie pixie;
    private final Player player;

    private final ArmorStand armorStand;

    private int ticks = 0;

    public PixiePunchAnimation(Pixie pixie, Player player) {
        this.pixie = pixie;
        this.player = player;

        // Create hologram displaying gained xp.
        Location loc = pixie.getLocation().clone();
        loc = loc.offset(0.5,0.1,0.5).toLocation(loc.getWorld());
        armorStand = loc.getWorld().spawn(loc, ArmorStand.class);
        armorStand.setInvisible(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);

        pixie.getPixieType();

        int xp = pixie.getPixieType().getXP();
        armorStand.setCustomNameVisible(true);
        armorStand.customName(Component.text(ChatColor.GOLD + "+" + xp + "xp"));
    }

    @Override
    public void run() {
        if (ticks >= 30) { // delete xp hologram, end task
            if (!armorStand.isDead())
                armorStand.remove();
            armorStand.getLocation().getWorld().spawnParticle(Particle.FIREWORKS_SPARK,
                    armorStand.getLocation().clone(), 2, 0.01, 0, 0.01, 0.5, null);
            this.cancel();
            return;
        }

        ticks++;
    }

    @Override
    public AnimationSettings getAnimationSettings() {
        return new AnimationSettings("Pixie Punch Animation", 0, 0);
    }
}
