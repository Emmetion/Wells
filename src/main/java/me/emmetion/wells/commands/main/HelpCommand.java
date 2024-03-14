package me.emmetion.wells.commands.main;

import me.emmetion.wells.Wells;
import me.emmetion.wells.commands.ECommand;
import me.emmetion.wells.config.Configuration;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class HelpCommand extends ECommand {

    private Configuration configuration;

    public HelpCommand() {

        // Assumes configuration has already been initialized.
        this.configuration = Wells.plugin.getConfiguration();
    }

    @Override
    public String command() {
        return "help";
    }

    @Override
    public List<ECommand> subcommands() {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender player, String[] args) {
        List<String> helpMessage = configuration.getHelpMessage();

        for (String message : helpMessage) {
            player.sendMessage(message);
        }

        return true;
    }
}
