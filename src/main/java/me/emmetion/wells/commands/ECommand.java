package me.emmetion.wells.commands;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public abstract class ECommand {

    /*
      The unique command name. Used to identify the command when parsing.
     */
    public abstract String command();

    /*
     All related subcommands this base ECommand has.
     */
    public abstract List<ECommand> subcommands();

    /*
     The method each command uses to execute actions.

     */
    public abstract boolean execute(CommandSender player, String[] args);

    /**
     * Parses the next subcommand from list defined above.
     * @param player
     * @param args arguments passed by CommandExecutor.
     */
    public boolean parseNextSubcommand(CommandSender player, String[] args) {
        if (subcommands() == null || subcommands().size() == 0) {
            return false; // Nothing in subcommands.
        }


        for (ECommand cmd : subcommands()) {
            if (cmd.command().equals(args[0])) {
                System.out.println("Found subcommand: " + cmd.command());
                // Remove the first argument and pass the rest to the subcommand.
                return cmd.execute(player, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        player.sendMessage("You must provide a valid subcommand!");

        // send player available subcommands.
        for (ECommand cmd : subcommands()) {
            player.sendMessage("Subcommand: " + cmd.command());
        }
        return false;
    }

    public List<String> availableSubcommands() {
        return this.subcommands().stream().map(ECommand::command).toList();
    }
}
