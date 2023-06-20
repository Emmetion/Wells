package me.emmetion.wells.runnables;

import de.tr7zw.nbtapi.NBTItem;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.CoinType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DroppedCoinRunnable extends BukkitRunnable {

    private Wells wells;
    private Item item;
    private CoinType coinType;
    private WellPlayer wplayer;
    private WellManager wellManager;

    private Map<Player, Boolean> playersOnCooldown;

    private boolean isComplete;


    public DroppedCoinRunnable(Wells wells, Item item, WellPlayer wplayer, WellManager wellManager,
                               Map<Player, Boolean> playersOnCooldown) {
        this.wells = wells;
        this.item = item;
        NBTItem nbt = new NBTItem(item.getItemStack());
        String wells_id = nbt.getString("wells_id");
        this.coinType = CoinType.getCoinTypeFromWellsID(wells_id);
        this.wplayer = wplayer;
        this.wellManager = wellManager;
        this.playersOnCooldown = playersOnCooldown;

        this.isComplete = false;
        item.setCanPlayerPickup(false);
    }

    public void setComplete(boolean b) {
        this.isComplete = b;
    }

    @Override
    public void run() { //
        if (isComplete()) {
            this.cancel();
            return;
        }

        if (!wplayer.isOnline()) {
            this.cancel();
            return;
        }

        Player player = Bukkit.getPlayer(wplayer.getPlayerUUID());

        if (item == null || item.isDead()) {
            setComplete(true);
            return;
        }

        if (item.isOnGround()) {
            ItemStack itemstack = item.getItemStack().asOne();
            player.getInventory().addItem(itemstack);

            Component text = Component.text("You missed your coin toss!")
                    .color(TextColor.color(52, 217, 241));

            player.sendMessage(text);
            item.remove();
            this.cancel();
        }

        if (item.isInWater()) {
            List<Well> collect = wells.getWellManager().getWells().stream().filter(w -> w.getLocation().distance(w.getLocation()) < 2).collect(Collectors.toList());
            if (collect.size() >= 1) {
                WellPlayer wellPlayer = wellManager.getWellPlayer(player);
                Well well = collect.get(0);

                wellPlayer.depositCoin(coinType, well);
                item.remove();
                if (playersOnCooldown.containsKey(player)) // removes cooldown located in WellListener object.
                    playersOnCooldown.remove(player);
                setComplete(true);
                return;
            } else {
                Component text = Component.text("You coin wasn't close to a well!")
                        .color(TextColor.color(52, 217, 241))
                        .append(
                                Component.text()
                        );
                player.sendActionBar(text);
                setComplete(true);
                return;
            }

        }

        if (item == null || item.isOnGround() || item.isInWater()) {
            this.cancel();
        }

        Location location = item.getLocation();
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 1, new Particle.DustOptions(
                Color.fromRGB(255,128,0)
                , 1));
    }

    public boolean isComplete() {
        return this.isComplete;
    }

}
