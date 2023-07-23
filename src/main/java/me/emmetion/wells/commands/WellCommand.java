package me.emmetion.wells.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Profile;
import me.athlaeos.valhallammo.managers.ProfileManager;
import me.athlaeos.valhallammo.skills.account.AccountProfile;
import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.CreatureType;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.ActiveBuff;
import me.emmetion.wells.model.BuffType;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.util.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static me.emmetion.wells.model.BuffType.GREEN_THUMB;
import static me.emmetion.wells.util.Utilities.getColor;

//TODO Migrate this command to use Utilities.getColor().
public class WellCommand implements CommandExecutor {

    private final ValhallaMMO mmo = ValhallaMMO.getPlugin();

    private final WellManager wellManager;
    private final CreatureManager creatureManager;

    public WellCommand(WellManager wellManager, CreatureManager creatureManager) {
        this.wellManager = wellManager;
        this.creatureManager = creatureManager;
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
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells delete' deletes your current town.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells create' creates a well at your location.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells print' prints every placed well in chat.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells save' save all changed data to database.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells level <increase/decrease/reset>' increases/decreases your well level by 1, or resets to 0.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells give <wells_id>' give yourself a custom wells item.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells well_req' displays the block requirements of a well underneath you.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells well_reset' resets all well holograms and animations.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells players' prints how many players are apart of wellplayers.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells holo <add/sub> <x> <y> <z>' changes the position of a well hologram, max dist 5.");
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.BLUE + "'/wells debug <true/false>' toggles debug messages.");
        } else if (arg1.equals("delete")) {
            if (town == null) {
                player.sendMessage(ChatColor.RED + "You are not part of a town!");
                return true;
            }
            Well well = this.wellManager.getWellByTownName(town.getName());
            if (well == null) {
                player.sendMessage("No well was found in your town!");
                return true;
            }
            this.wellManager.deleteWell(well);
            player.sendMessage(ChatColor.GREEN + "Deleted well!");
        } else if (arg1.equals("create")) {
            this.wellManager.createWell(player, player.getLocation().getBlock());
        } else if (arg1.equals("print")) {
            player.sendMessage("--- Wells ---");
            for (Well well : wellManager.getWells()) {
                player.sendMessage(well.createHoverableTextComponent());
            }
        } else if (arg1.equals("save")) {
            this.wellManager.updateDatabase();
            player.sendMessage("Saved wells.");
        } else if (arg1.equals("level")) {
            if (args.length < 3) {
                player.sendMessage("Too few arguments!");
                return true;
            }
            String townname = args[1];
            Well well = wellManager.getWellByTownName(townname);
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
                player.getInventory().addItem(Objects.requireNonNull(Utilities.createWellBlockItem(1)));
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
            int size = this.wellManager.getWellPlayers().size();
            player.sendMessage("Size " + size);
        } else if (arg1.equals("holo")) {
            Well well = wellManager.getWellByTownName(town.getName());
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
                this.wellManager.setDebug(mode);
                this.creatureManager.setDebug(mode);
                player.sendMessage("Toggled debug to " + mode + ".");
            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage("Syntax: /wells debug <true/false>");
            }
        } else if (arg1.equals("particle")) {
            try {
                Boolean aBoolean = Boolean.valueOf(args[1]);
                this.wellManager.getWellPlayer(player).setHideParticles(aBoolean);
                player.sendMessage("Toggled particles to " + aBoolean + ".");
            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage("Syntax: /wells particle <true/false>");
            }
        } else if (arg1.equals("buff_data")) {
            try {
                if (town == null) {
                    player.sendMessage(getColor("You must be in a town to use this command!"));
                    return true;
                }
                int i = 1;
                for (ActiveBuff buff : wellManager.getWellByTownName(town.getName()).getBuffs()) {
                    if (buff.isNone()) {
                        player.sendMessage("Buff (" + i + getColor("): &cNone"));
                    } else {
                        player.sendMessage("Buff (" + i + getColor("): ID: " + buff.getBuffID()));
                        player.sendMessage("           EndDate: " + getColor(buff.getEndDateAsString()));
                        player.sendMessage("           Particle: " + buff.getWellParticle());
                    }
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage("");
            }
        } else if (arg1.equals("buff")) {
            try {
                String townname = String.valueOf(args[1]);
                String buff_id = String.valueOf(args[2]).toUpperCase();
                int slot = Integer.parseInt(args[3]);
                BuffType buffType = BuffType.valueOf(buff_id);
                Well well = wellManager.getWellByTownName(townname);

                Timestamp plus_five_min = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));

                if (well == null) {
                    throw new IllegalArgumentException();
                }

                switch (buffType) {
                    // Here, every normal type of buff we handle the same. Except for NONE types. We also have a null type in-case buff-type did not get sorted out.
                    case GREEN_THUMB:
                    case RESISTANCE:
                        ActiveBuff activebuff = new ActiveBuff(buffType, plus_five_min);
                        well.setActiveBuff(activebuff, slot);
                        player.sendMessage("New time: " + plus_five_min.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        break;
                    case NONE:
                        player.sendMessage("Cannot put buff_id 'NONE' onto well!");
                        break;
                    default:
                        player.sendMessage("Unknown?");
                        break;
                }
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                player.sendMessage("Syntax: /wells buff <townname> <buff_id> <id>");
            }
        } else if (arg1.equals("buff_details")) {

        } else if (arg1.equals("spawn")) {
            try {
                CreatureType type = CreatureType.valueOf(args[1]);
                if (type == null) {
                    player.sendMessage("Your creature type was invalid!");
                    return true;
                }
                switch (type) {
                    // All
                    case PIXIE:
                        String townname = args[2];
                        Well well = wellManager.getWellByTownName(townname);
                        if (well == null) {
                            player.sendMessage("Your town name was invalid!");
                            return true;
                        }

                        creatureManager.spawnCreature(type.getCreatureClazz(), well);
                        break;
                    case SPAWN_NPC:
                        creatureManager.spawnCreature(type.getCreatureClazz(), null);
                        break;
                    default:
                        break;

                }

            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage("Syntax: /wells spawn <creature_type> [town_name]");
            }
        } else if (arg1.equals("maps")) {
            player.sendMessage(getColor("&c -- Creature Maps --"));
            for (Well well : this.creatureManager.getWellsWithCreatures()) {
                List<WellCreature> creatures = this.creatureManager.getCreaturesAtWell(well);
                if (creatures == null)
                    continue;
                player.sendMessage(well.getWellName());
                int i = 1;
                for (WellCreature wc : creatures) {
                    CreatureType ct = wc.getCreatureType();
                    player.sendMessage(getColor(" " + i + ":  Type: "  + ct + " UUID: " + wc.getUUID().toString().substring(0, 5)));
                    i++;
                }
            }

        } else if (arg1.equalsIgnoreCase("val")) {
            player.sendMessage("ValhallaMMO commands called...");
            Profile profile = ProfileManager.getManager().getProfile(player, "ACCOUNT");
            if (profile != null) {
                if (profile instanceof AccountProfile) {
                    AccountProfile ap = (AccountProfile) profile;
                    double lifetimeEXP = ap.getLifetimeEXP();
                    player.sendMessage("lifetimeEXP=" + lifetimeEXP);
                    player.sendMessage("armor-multiplier-bonus: " +  ap.getArmorMultiplierBonus());
                    ap.setArmorMultiplierBonus(ap.getArmorMultiplierBonus() + 0.5f);
                    player.sendMessage("armor-multiplier-bonus (+0.5f): " +  ap.getArmorMultiplierBonus());

                }
            }

        }

        return true;
    }
}
