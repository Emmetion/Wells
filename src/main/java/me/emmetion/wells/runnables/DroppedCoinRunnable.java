package me.emmetion.wells.runnables;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class DroppedCoinRunnable extends BukkitRunnable {

    private Wells wells;
    private Item item;
    private CoinType coinType;
    private Player dropper;
    private WellManager wellManager;

    public DroppedCoinRunnable(Wells wells, Item item, Player dropper, WellManager wellManager) {
        this.wells = wells;
        this.item = item;
        NBTItem nbt = new NBTItem(item.getItemStack());
        String wells_id = nbt.getString("wells_id");
        this.coinType = CoinType.getCoinTypeFromWellsID(wells_id);
        this.dropper = dropper;
        this.wellManager = wellManager;
    }

    @Override
    public void run() {

        if (!dropper.isOnline()) {
            this.cancel();
            return;
        }

        if (item == null || item.isDead()) {
            this.cancel();
            return;
        }

        if (item.isOnGround()) {
            ItemStack itemstack = item.getItemStack().asOne();
            dropper.getInventory().addItem(itemstack);

            Component text = Component.text("Your coin has been returned to you.")
                    .color(TextColor.color(52, 217, 241));

            dropper.sendMessage(text);
            item.remove();
            this.cancel();
        }

        if (item.isInWater()) {
            List<Well> collect = wells.getWellManager().getWells().stream().filter(w -> w.getLocation().distance(w.getLocation()) < 5).collect(Collectors.toList());
            if (collect.size() >= 1) {

                Component component = Component.text("Exchanged Coin: ")
                        .color(TextColor.color(251, 255, 246))
                        .append(
                                Component.text(coinType.getWellsId())
                                        .color(coinType.getColor())
                        );

                dropper.sendMessage(component);

                WellPlayer wellPlayer = wellManager.getWellPlayer(dropper);
                Well well = collect.get(0);
                wellPlayer.depositCoin(coinType, well);
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
