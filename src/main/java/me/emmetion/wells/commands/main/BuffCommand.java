package me.emmetion.wells.commands.main;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.commands.ECommand;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.Well;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class BuffCommand extends ECommand {

    private final WellManager wellManager = Wells.plugin.getWellManager();

    @Override
    public String command() {
        return "buff";
    }

    @Override
    public List<ECommand> subcommands() {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender player, String[] args) {
        if (!(player instanceof Player p)) {
            player.sendMessage("You must be a player to use this command.");
            return false;
        }

        TownyAPI towny = TownyAPI.getInstance();
        Town town = towny.getTown(p);
        if (town == null) {
            p.sendMessage("You must be in a town to use this command.");
            return false;
        }
        Well wellByTown = wellManager.getWellByTown(town);
        String subcmd = args[1];
        if (args.length == 0) {
            p.sendMessage("You must provide a subcommand.");
            return false;
        } else if (args[0].equalsIgnoreCase("list")) {
            // List active buffs the players town has.
            return listBuffs(p, wellByTown);
        } else if (args[0].equalsIgnoreCase("add")) {
            // Add a buff to the players town.
            return addBuff(p, wellByTown, args);
        } else if (args[0].equalsIgnoreCase("remove")) {
            // Remove a buff from the players town.
            return removeBuff(p, wellByTown, args);
        } else {
            p.sendMessage("Invalid subcommand.");
            return false;
        }
    }

    private boolean listBuffs(Player p, Well wellByTown) {
        for (ActiveBuff buff : wellByTown.getActiveBuffs()) {
            p.sendMessage(buff.toString());
        }
        return true;
    }

    private boolean addBuff(Player p, Well wellByTown, String[] args) {
        if (args.length < 3) {
            p.sendMessage("You must provide a town name and a buff type.");
            return false;
        }
        TownyAPI towny = TownyAPI.getInstance();
        Town town = towny.getTown(args[1]);
        if (town == null) {
            p.sendMessage("Invalid town name.");
            return false;
        }
        BuffType bt;
        try {
            bt = BuffType.valueOf(args[2]);
        } catch (IllegalArgumentException e) {
            p.sendMessage("Invalid buff type.");
            return false;
        }

        // get Slot num
        int slot = wellByTown.getAvailableSlot();
        wellByTown.setActiveBuff(new ActiveBuff(bt, LocalDateTime.now().plusMinutes(5)), slot);
        p.sendMessage("Buff added.");
        return true;
    }

    private boolean removeBuff(Player p, Well wellByTown, String[] args) {
        if (args.length < 1) {
            p.sendMessage("You must provide a town name and a buff type.");
            return false;
        }

        // Parse slot arg
        int slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            p.sendMessage("Invalid slot number.");
            return false;
        }

        wellByTown.setActiveBuff(ActiveBuff.defaultActiveBuff(), slot);
        // get Slot num
        p.sendMessage("Buff slot " + slot + " removed.");
        return true;
    }
}
