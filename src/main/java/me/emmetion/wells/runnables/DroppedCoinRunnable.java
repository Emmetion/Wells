package me.emmetion.wells.runnables;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.Well;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class DroppedCoinRunnable extends BukkitRunnable {

    private Wells wells;
    private Item item;
    private Player dropper;

    public DroppedCoinRunnable(Wells wells, Item item, Player dropper) {
        this.wells = wells;
        this.item = item;
        this.dropper = dropper;
    }

    @Override
    public void run() {

        if (!dropper.isOnline()) {
            this.cancel();
        }

        if (item.isInWater()) {
            List<Well> collect = wells.getWellManager().getWells().stream().filter(w -> w.getLocation().distance(w.getLocation()) < 5).collect(Collectors.toList());
            if (collect.size() >= 1) {
                dropper.sendMessage(ChatColor.GOLD + "Your gold coin was collected!");
                this.cancel();
                return;
            }
        }

        if (item == null || item.isOnGround() || item.isInWater()) {
            this.cancel();
        }
        Location location = item.getLocation();
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 1, new Particle.DustOptions(Color.fromRGB(255,128,0), 1));
    }
}
