package me.emmetion.wells.commands;

import me.emmetion.wells.commands.main.WellCommand;
import me.emmetion.wells.commands.main.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This is the base command for /wells.
 * It handles parsing of the first subcommand and passes it to the designated subcommand.
 */
public class MainCommand extends ECommand implements CommandExecutor {

    @Override
    public String command() {
        return "well";
    }

    @Override
    public List<ECommand> subcommands() {
        return List.of(
                new HelpCommand(),
                new ToggleParticleCommand(),
                new BuffCommand(),
                new SpawnCommand(),
                new WellCommand(),
                new GiveCommand()
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("Hello.");
        if (args.length == 0) {
            // No subcommand was given, show help.
            sender.sendMessage("You need to provide a subcommand. Type: /wells help");
            return false;
        }

        return parseNextSubcommand(sender, args);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return true;
    }
}
