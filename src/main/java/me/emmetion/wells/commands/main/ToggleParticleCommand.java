package me.emmetion.wells.commands.main;

import me.emmetion.wells.Wells;
import me.emmetion.wells.commands.ECommand;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static me.emmetion.wells.util.Utilities.getColor;

public class ToggleParticleCommand extends ECommand {

        WellManager manager = Wells.plugin.getWellManager();

        @Override
        public String command() {
            return "particles";
        }

        @Override
        public List<ECommand> subcommands() {
            return Collections.emptyList();
        }

        @Override
        public boolean execute(CommandSender player, String[] args) {
            player.sendMessage("Running particles command.");
            if (!(player instanceof Player p)) {
                player.sendMessage("You must be a player to use this command.");
                return false;
            }
            WellPlayer wellPlayer = manager.getWellPlayer(p);
            boolean state = wellPlayer.toggleParticles();
            player.sendMessage(getColor("&fParticles are now " + (state ? "&aenabled" : "&cdisabled") + "&f."));
            return true;
        }
}
