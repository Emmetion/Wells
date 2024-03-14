package me.emmetion.wells.commands.main;

import me.emmetion.wells.Wells;
import me.emmetion.wells.commands.ECommand;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.List;

public class GiveCommand extends ECommand {

    private WellManager wellManager = Wells.plugin.getWellManager();

    @Override
    public String command() {
        return "give";
    }

    @Override
    public List<ECommand> subcommands() {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Too few arguments! Syntax: /wells give <player> <item>.");
            // Send help message
            return true;
        }

        String playerName = args[0];
        String coinTypeID = args[1];

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Player '"+playerName+"' is offline.");
            return true;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack coinFromID = Utilities.createCoinFromID(coinTypeID);
        if (coinFromID == null) {
            sender.sendMessage("Invalid coin type.");
            return true;
        }

        inventory.addItem(coinFromID);
        return false;
    }
}
