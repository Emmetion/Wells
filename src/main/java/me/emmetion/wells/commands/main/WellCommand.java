package me.emmetion.wells.commands.main;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.commands.ECommand;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class WellCommand extends ECommand {

    private final WellManager wellManager = Wells.plugin.getWellManager();

    @Override
    public String command() {
        return "well";
    }

    @Override
    public List<ECommand> subcommands() {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Too few arguments! Syntax: /wells well <town> <create/delete>.");
            // Send help message
            return true;
        }

        String townname = args[0];
        String subcmd = args[1];

        Town town = TownyAPI.getInstance().getTown(townname);
        if (town == null) {
            sender.sendMessage("Town not found.");
            return true;
        }

        if (subcmd.equalsIgnoreCase("delete")) {
            // Delete the well
            deleteWell(town);

            return true;
        }  else if (subcmd.equalsIgnoreCase("create")) {
            // Create a new well
            Player player = (Player) sender;
            Block targetBlockExact = player.getTargetBlockExact(5);

            if (targetBlockExact == null) {
                sender.sendMessage("You must be looking at a block to create a well.");
                return true;
            }

            createWell(player, town, targetBlockExact.getLocation());
            return true;
        }

        return false;
    }

    private void createWell(@NotNull Player player, @NotNull Town town, @NotNull Location location) {
        if (wellManager.wellExists(town.getName())) {
            player.sendMessage("This town already has a well.");
            return;
        }
        // Create the well.
        this.wellManager.createWell(player, location.getBlock());
    }

    private void deleteWell(@NotNull Town town) {
        if (!wellManager.wellExists(town.getName())) return;

        Well well = wellManager.getWellByTown(town);
        wellManager.deleteWell(well);
    }


}
