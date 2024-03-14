package me.emmetion.wells.commands.main;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.commands.ECommand;
import me.emmetion.wells.creature.CreatureType;
import me.emmetion.wells.creature.factories.ExcessWaterZombieFactory;
import me.emmetion.wells.creature.factories.PixieFactory;
import me.emmetion.wells.managers.CreatureManager;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.model.Well;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SpawnCommand extends ECommand {

    private WellManager wellManager = Wells.plugin.getWellManager();
    private CreatureManager creatureManager = Wells.plugin.getCreatureManager();

    @Override
    public String command() {
        return "spawn";
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

        //Syntax : /well spawn <town> <monster>
        if (args.length < 2) {
            p.sendMessage("Invalid syntax. /well spawn <town> <monster>");
            return false;
        }

        String townname = args[0];
        String monster = args[1];

        CreatureType creatureType;
        Town town;
        try {
            town = TownyAPI.getInstance().getTown(townname);
            creatureType = CreatureType.valueOf(monster.toUpperCase());
        } catch (Exception e) {
            p.sendMessage("Invalid syntax. /well spawn <town> <monster>");
            return false;
        }

        // get the players well

        if (town == null) {
            p.sendMessage("Invalid town.");
            return false;
        }

        Well wellByTown = wellManager.getWellByTown(town);
        if (wellByTown == null) {
            p.sendMessage("This town does not have a well. Please create provide a town with well.");
            return false;
        }

        switch (creatureType) {
            case EXCESS_WATER_ZOMBIE -> {
                // spawn the excess water zombie
                creatureManager.spawnCreature(new ExcessWaterZombieFactory(), wellByTown, null);
            }
            case PIXIE -> {
                creatureManager.spawnCreature(new PixieFactory(), wellByTown, null);
            }
        }

        return false;
    }
}
