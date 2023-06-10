package me.emmetion.wells.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.util.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        if (arg1.equals("help")) {
            player.sendMessage(ChatColor.BLUE + "-- Well Commands! --");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells delete' deletes your current town.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells create' creates a well at your location.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells print' prints every placed well in chat.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells save' save all changed data to database.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells level <increase/decrease/reset>' increases/decreases your well level by 1, or resets to 0.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells give <wells_id>' give yourself a custom wells item.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells well_req' displays the block requirements of a well underneath you.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells well_reset' resets all well holograms and animations.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells players' prints how many players are apart of wellplayers.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells holo <add/sub> <x> <y> <z>' changes the position of a well hologram, max dist 5.");
            player.sendMessage(ChatColor.GRAY + " - "+ChatColor.BLUE + "'/wells debug <true/false>' toggles debug messages.");
        } else if (arg1.equals("delete")) {
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
                player.sendMessage(well.createHoverableTextComponent());
            }
        } else if (arg1.equals("save")) {
            this.manager.saveAllWells();
            player.sendMessage("Saved wells.");
        } else if (arg1.equals("level")) {
            if (args.length < 3) {
                player.sendMessage("Too few arguments!");
                return true;
            }
            String townname = args[1];
            Well well = manager.getWellByTownName(townname);
            if (well == null) {
                player.sendMessage(ChatColor.RED + "No town was found from your input!");
                return true;
            }
            String option = args[2];
            if (option.equals("increase")) {
                well.incrementLevel();
                player.sendMessage("Incremented level of " + townname + " to " + well.getWellLevel());
                well.notifyObservers();
            } else if (option.equals("decrease")) {
                well.decrementLevel();
                player.sendMessage("Incremented level of " + townname + " to " + well.getWellLevel());
                well.notifyObservers();
            } else if (option.equals("reset")) {
              well.resetLevel();
            } else {
                player.sendMessage("Syntax: /wells increment <townname> <increase/decrease>");
            }
        } else if (arg1.equals("give")) {
            if (args.length < 2) {
                player.sendMessage("Too few arguments!");
                return true;
            }
            String block_id = args[1];
            if (block_id.equals("WELL_BLOCK")) {
                player.getInventory().addItem(Utilities.createWellBlockItem(1));
                player.sendMessage("Gave you a WELL_BLOCK");
            } else if (block_id.endsWith("_COIN")) { // handles things ending with _COIN, ex. BRONZE_COIN, SILVER_COIN, GOLD_COIN
                player.getInventory().addItem(Utilities.createCoinFromID(block_id));
                player.sendMessage(ChatColor.GOLD + "Gave you a " + block_id);
            } else {
                player.sendMessage("Your item '" + block_id + "' wasn't found. " + ChatColor.RED + ":(");
            }
        } else if (arg1.equals("well_req")) {
            Block block = player.getLocation().getBlock();
            Utilities.blockRequirement(block, Material.WATER, 5);
            player.sendMessage(ChatColor.BLUE + "Look under you.");
        } else if (arg1.equals("well_reset")) {
            Wells.plugin.initWellHolograms();
        } else if (arg1.equals("players")) {
            player.sendMessage("Players");
            int size = this.manager.getWellPlayers().size();
            player.sendMessage("Size " + size);
        } else if (arg1.equals("holo")) {
            Well well = manager.getWellByTownName(town.getName());
            if (well == null) {
                player.sendMessage("You aren't in a town! You cannot use this command!");
                return true;
            }
            try {
                String addsub = String.valueOf(args[1]);
                if (addsub.equals("add")) {
                    float x = Float.parseFloat(args[2]);
                    float y = Float.parseFloat(args[3]);
                    float z = Float.parseFloat(args[4]);

                    boolean success = well.addHologramLocation(x, y, z);
                    if (!success)
                        player.sendMessage("Well hologram out of bounds! (dist: >5)");
                } else if (addsub.equals("sub")) {
                    float x = Float.parseFloat(args[2]);
                    float y = Float.parseFloat(args[3]);
                    float z = Float.parseFloat(args[4]);

                    boolean success = well.subtractHologramLocation(x, y, z);
                    if (!success)
                        player.sendMessage("Well hologram out of bounds! (dist: >5)");
                }
            } catch (IndexOutOfBoundsException e) {
                player.sendMessage("Syntax: /wells golo <add/sub> <x> <y> <z>");
            }
        } else if (arg1.equals("debug")) {
            try {
                boolean mode = Boolean.valueOf(args[1]);
                this.manager.setDebug(mode);
                player.sendMessage("Toggled debug to " + mode + ".");
            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage("Syntax: /wells debug <true/false>");
            }
        } else if (arg1.equals("particle")) {
            try {
                Boolean aBoolean = Boolean.valueOf(args[1]);
                this.manager.getWellPlayer(player).setHideParticles(aBoolean);
                player.sendMessage("Toggled particles to " + aBoolean + ".");
            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage("Syntax: /wells particle <true/false>");
            }
        }

        return true;
    }
}
