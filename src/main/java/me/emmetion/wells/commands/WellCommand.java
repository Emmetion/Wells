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
        if (args.length < 2) {
            sender.sendMessage("Too few arguments! Syntax: /wells <option>");
            return true;
        }

        Player player = (Player) sender;
        Town town = TownyAPI.getInstance().getTown(player);

        String arg1 = args[1];
        if (arg1.equals("delete")) {
            if (town == null) {
                player.sendMessage(ChatColor.RED + "You are not part of a town!");
                return true;
            }
            Well well = this.manager.getWellByTownName(town.getName());
            this.manager.deleteWell(well);
        } else if (arg1.equals("create")) {
            this.manager.createWell(player, player.getLocation().getBlock());
        } else if (arg1.equals("print")) {
            player.sendMessage("--- Wells ---");
            for (Well well : manager.getWells()) {
                player.sendMessage(well.toString());
            }
        } else if (arg1.equals("save")) {
            this.manager.saveAllWells();
        }


        return true;
    }
}
