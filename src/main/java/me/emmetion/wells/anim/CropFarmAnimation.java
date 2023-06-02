package me.emmetion.wells.anim;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class CropFarmAnimation extends BukkitRunnable {

    private Player farmer;
    private Block cropBlock;
    private int frame;

    private Material cropType;


    private Collection<Entity> bats = new ArrayList<>();

    public CropFarmAnimation(Player farmer, Block crop) {
        this.farmer = farmer;
        this.cropBlock = crop;
        this.frame = 0;

        this.cropType = cropBlock.getType();

        Random random = new Random();
        for (int i = 0; i < 2; i++) {
            Location location = crop.getLocation().clone();

            double x = random.nextDouble();
            double z = random.nextDouble();
            location.add(x, 0, z);



            Bat bat = location.getWorld().spawn(location, Bat.class);

            bat.setVisibleByDefault(false);
            bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 2, true, false, true));

            bats.add(bat);
        }
    }


    @Override
    public void run() {
        if (this.frame == 25) {
            killBats();
            this.cancel();
            return;
        }

        for (Entity bat : bats) {
            Location location = bat.getLocation();
            World world = location.getWorld();
//            world.spawnParticle(Particle.CLOUD, );
        }

    }

    private void createBats() {

    }

    private void killBats() {
        for (Entity bat : bats) {
            bat.remove();
        }
    }

//    @Override
//    public void run() {
//
//        if (frame == 30) {
//            pop();
//            return;
//        }
//
//        if (frame % 5 == 0) {
//            farmer.sendMessage(ChatColor.YELLOW + "Frame: " + frame);
//        }
//
//        for (Location particleLoc : particleLocations) {
//            particleLoc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc.clone().add(0, 0.1 * frame, 0), 1, new Particle.DustOptions(Color.fromRGB(0, Math.abs(255 - (frame * 15)) % 255, (0 + (frame * 15)) % 255), (float) (1)));
//        }
//
//        this.frame++;
//    }
//
//    /**
//     * Pop is the end of a particles life.
//     */
//    private void pop() {
//        farmer.sendMessage("Popped!");
//        for (Location loc : particleLocations) {
//            Location add = loc.clone().add(0, 0.1 * frame, 0);
//            World world = add.getWorld();
//
//            Random random = new Random();
//
//
//            ItemStack itemStack = new ItemStack(cropType);
//            ItemMeta meta = itemStack.getItemMeta();
//            spawnYellowFirework(add);
//            meta.displayName(Component.text("wells_crop_" + random.nextInt(999999)));
//            world.dropItem(add, itemStack);
//
//            farmer.playSound(loc, Sound.ENTITY_CHICKEN_EGG, 3, 1);
//        }
//        this.cancel();
//
//    }
//
//    public void spawnYellowFirework(Location location) {
//        Firework firework = location.getWorld().spawn(location, Firework.class);
//        FireworkMeta fireworkMeta = firework.getFireworkMeta();
//
//        // Create a yellow firework effect
//        FireworkEffect effect = FireworkEffect.builder()
//                .with(FireworkEffect.Type.BURST)
//                .withColor(Color.YELLOW)
//                .build();
//
//        // Apply the firework effect to the firework meta
//        fireworkMeta.addEffect(effect);
//        firework.setFireworkMeta(fireworkMeta);
//
//        // Optional: Make the firework explode instantly upon spawning
//        firework.detonate();
//    }
}
