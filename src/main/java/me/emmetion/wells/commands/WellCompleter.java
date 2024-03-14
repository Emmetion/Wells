package me.emmetion.wells.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WellCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        MainCommand cmd = new MainCommand();
        // print all the args
        sender.sendMessage("Args length: " + args.length);

        if (args.length == 1) {
            return cmd.subcommands().stream().map(ECommand::command).toList();
        }

        return null;
    }
}
