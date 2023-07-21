package me.emmetion.wells.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.anim.CropFarmAnimation;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.events.buff.GreenThumbEvent;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import me.emmetion.wells.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class WellBuffListener implements Listener {

    private final WellManager wellManager;

    public WellBuffListener(WellManager wellManager) {
        this.wellManager = wellManager;
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (!wellManager.wellExistsForPlayer(player)) {
            player.sendMessage(Component.text("Well does not exist for player."));
            return;
        }

        // Filters non-ageable blocks. (non-crops)
        if (!(block.getBlockData() instanceof Ageable)) {
            return;
        }

        // Player must be in a town to be part of a well
        Town town = TownyAPI.getInstance().getTown(player);

        if (town == null) // A player must be in a town to receive WellBuffs.
            return;

        // CropBuff only activates when farming inside your own town.
        // Maybe add a buff/token to make it temporarily global?
        String town_name = Utilities.getTownFromBlock(block);
        if (town_name == null || !town.getName().equals(town_name)) {
            player.sendMessage("Block broken was not in town.");
            return;
        }

        player.sendMessage("Player is in their own town. well is placed.");

        // Check for well buffs.
        Well well = wellManager.getWellByTownName(town_name);

        well.incrementLevel();

        WellPlayer wellPlayer = wellManager.getWellPlayer(player);

        // Handle randomness farming crops.

        // Call event for API.
        GreenThumbEvent greenThumbEvent = new GreenThumbEvent(wellPlayer);
        greenThumbEvent.callEvent();

        CropFarmAnimation animation = new CropFarmAnimation(player, block);
        animation.start();
    }

}
