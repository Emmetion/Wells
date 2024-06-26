package me.emmetion.wells.anim;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class CropFarmAnimation extends Animation {


    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Player farmer;
    private final Block cropBlock;
    private int frame;

    private final Material cropType;

    private final Collection<Marker> markers = new ArrayList<>();

    public CropFarmAnimation(Player farmer, Block crop) {
        this.farmer = farmer;
        this.cropBlock = crop;
        this.frame = 0;

        this.cropType = cropBlock.getType();

        createMarkets();
    }


    @Override
    public void run() {
        for (Marker marker : markers) {
            Location location = marker.getLocation();
            World world = location.getWorld();
            world.spawnParticle(Particle.VILLAGER_HAPPY, location, 1, .5, 0, .5, 0);
        }
        if (this.frame == 25) {
            killBats();
            this.cancel();
            return;
        }
        frame++;
    }

    private void createMarkets() {

        for (int i = 0; i < 2; i++) {
            World world = this.cropBlock.getLocation().getWorld();
            Location location = this.cropBlock.getLocation();
            Marker marker  = world.spawn(location, Marker.class);

            markers.add(marker);
        }
    }

    private void killBats() {
        for (Entity bat : markers) {
            Location location = bat.getLocation();
            World world = location.getWorld();
            world.dropItemNaturally(location, new ItemStack(cropType));
            bat.remove();
        }
    }

    @Override
    public AnimationSettings getAnimationSettings() {
        return new AnimationSettings("CropFarmAnimation", 1, 1);
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
