package me.emmetion.wells.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.database.Database;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.Well;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WellCommand implements CommandExecutor {

    private WellManager manager;

    public WellCommand(WellManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a Player to use this command!");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("Too few arguments! Syntax: /wells <option>");
            return true;
        }

        Player player = (Player) sender;
        Town town = TownyAPI.getInstance().getTown(player);

        String arg1 = args[0];
        if (arg1.equals("delete")) {
            if (town == null) {
                player.sendMessage(ChatColor.RED + "You are not part of a town!");
                return true;
            }
            Well well = this.manager.getWellByTownName(town.getName());
            if (well == null) {
                player.sendMessage("No well was found in your town!");
                return true;
            }
            this.manager.deleteWell(well);
            player.sendMessage(ChatColor.GREEN + "Deleted well!");
        } else if (arg1.equals("create")) {
            this.manager.createWell(player, player.getLocation().getBlock());
        } else if (arg1.equals("print")) {
            player.sendMessage("--- Wells ---");
            for (Well well : manager.getWells()) {
                player.sendMessage(well.toString());
            }
        } else if (arg1.equals("save")) {
            this.manager.saveAllWells();
            player.sendMessage("Saved wells.");
        } else if (arg1.equals("increment")) {
            if (args.length < 2) {
                player.sendMessage("Too few arguments!");
                return true;
            }
            String townname = args[1];
            Well wellByTownName = manager.getWellByTownName(townname);
            wellByTownName.incrementLevel();
            player.sendMessage("Incremented level of " + townname + " to " + wellByTownName.getLevel());
        }


        return true;
    }
}
