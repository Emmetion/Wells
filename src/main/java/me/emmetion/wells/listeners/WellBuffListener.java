package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import me.emmetion.wells.Wells;
import me.emmetion.wells.anim.CropFarmAnimation;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class WellBuffListener implements Listener {

    private WellManager wellManager;

    public WellBuffListener(WellManager wellManager) {
        this.wellManager = wellManager;
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (!(block.getBlockData() instanceof Ageable)) {
            player.sendMessage(Component.text("Block isn't Ageable."));
            return;
        }

        if (!wellManager.wellExistsForPlayer(player)) {
            player.sendMessage(Component.text("Well does not exist for player."));
            return;
        }

        Town town = TownyAPI.getInstance().getTown(player);

        if (town == null) {
            return;
        }

        String town_name = Utilities.getTownFromBlock(block);

        if (town_name == null) {
            player.sendMessage("Block broken was not in town.");
            return;
        }


        if (town.getName().equals(town_name)) {
            player.sendMessage("Player is in their own tonw. well is placed.");
            // Check for well buffs.
            Well well = wellManager.getWellByTownName(town_name);

            well.incrementLevel();

            CropFarmAnimation animation = new CropFarmAnimation(player, block);
            animation.runTaskTimer(Wells.plugin, 1, 1);


        } else {
            player.sendMessage("Not in the correct town.");
        }
    }

}
