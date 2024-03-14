package me.emmetion.wells.database;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

public final class YAMLDatabase extends EDatabase {

    private final String wellsPath;
    private final String wellPlayersPath;

    private final Wells wells;

    public YAMLDatabase() {
        super();

        wells = Wells.plugin;

        File dataFolder = wells.getDataFolder();
        String absolutePath = dataFolder.getAbsolutePath();

        wellPlayersPath = absolutePath + "/db/well_players/";
        wellsPath = absolutePath + "/db/wells/";

    }

    @Override
    public void initializeDatabase() {
        // Creates directories to each database folder.

        File wells = new File(wellsPath);
        wells.mkdirs();

        File wellPlayers = new File(wellPlayersPath);
        wellPlayers.mkdirs();
    }

    @Override
    public void createWell(@NotNull Well well) {
        String townName = well.getTownName();
        // Well file location is:
        // Wells/db/

        File file = new File(wellsPath + townName + ".yml");
        if (file.exists()) {
            // Well already exists, no need to create a new one.
            return;
        }

        try {
            file.createNewFile();

            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

            yml.set("well.townname", well.getTownName());
            yml.set("well.position", well.getLocation());
            yml.set("well.hologram", well.getHologramLocation());
            yml.set("well.level", well.getWellLevel());
            yml.set("well.experience", well.getExperience());
            // Buffs
            yml.set("well.buff1.id", well.getBuff1().getBuffID());
            yml.set("well.buff1.endtimestamp", well.getBuff1().getEndTimestamp());
            yml.set("well.buff2.id", well.getBuff2().getBuffID());
            yml.set("well.buff2.endtimestamp", well.getBuff2().getEndTimestamp());
            yml.set("well.buff3.id", well.getBuff3().getBuffID());
            yml.set("well.buff3.endtimestamp", well.getBuff3().getEndTimestamp());
            yml.set("well.isboosted", well.isBoosted());
            yml.set("well.boost_end", well.getBoostEnd());

            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Well getWell(@NotNull String townName) {

        File file = new File(wellsPath + townName + ".yml");

        if (!file.exists())
            return null;

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        Well well = new Well(
                yml.getString("well.townname"),
                yml.getLocation("well.position"),
                yml.getLocation("well.hologram"),
                yml.getInt("well.level"),
                yml.getInt("well.experience"),
                // Buffs
                yml.getString("well.buff1.id"),
                yml.getObject("well.buff1.endtimestamp", Timestamp.class),
                yml.getString("well.buff2.id"),
                yml.getObject("well.buff2.endtimestamp", Timestamp.class),
                yml.getString("well.buff3.id"),
                yml.getObject("well.buff3.endtimestamp", Timestamp.class),
                yml.getBoolean("well.isboosted"),
                null
//                yml.getObject("well.boost_end", Timestamp.class)
        );


        // Checks if the townname is invalid, otherwise we assume that it's a valid well.
        if (well.getTownName().equals(""))
            return null;


        return well;
    }


    @Override
    public void updateWell(@NotNull Well well) {
        String townName = well.getTownName();
        // Well file location is:
        // Wells/db/

        File file = new File(wellsPath + townName + ".yml");
        if (!file.exists()) {
            // Well doesn't exist, therefor we cannot create one.
            return;
        }


        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

            yml.set("well.townname", well.getTownName());
            yml.set("well.position", well.getLocation());
            yml.set("well.hologram", well.getHologramLocation());
            yml.set("well.level", well.getWellLevel());
            yml.set("well.experience", well.getExperience());

            // Buffs
            yml.set("well.buff1.id", well.getBuff1().getBuffID());
            yml.set("well.buff1.endtimestamp", well.getBuff1().getEndTimestamp());
            yml.set("well.buff2.id", well.getBuff2().getBuffID());
            yml.set("well.buff2.endtimestamp", well.getBuff2().getEndTimestamp());
            yml.set("well.buff3.id", well.getBuff3().getBuffID());
            yml.set("well.buff3.endtimestamp", well.getBuff3().getEndTimestamp());
            yml.set("well.isboosted", well.isBoosted());
            yml.set("well.boost_end", well.getBoostEnd());

            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteWell(@NotNull Well well) {
        File file = new File(wellsPath + well.getTownName() + ".yml");

        if (!file.exists())
            return;

        file.delete();
    }

    @Override
    public void createWellPlayer(@NotNull WellPlayer wellPlayer) {
        UUID uuid = wellPlayer.getPlayerUUID();

        File file = new File(wellPlayersPath + uuid.toString() + ".yml");

        if (file.exists())
            return;

        assignWellPlayerValues(wellPlayer, uuid, file);
    }

    @Override
    public WellPlayer getWellPlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        File file = new File(wellPlayersPath + uuid.toString() + ".yml");

        if (!file.exists()) {
            // If it doesn't exist, we will make a new account for that user
            WellPlayer newPlayer = new WellPlayer(uuid, 0, 0, 0, 0, 0);
            this.createWellPlayer(newPlayer);
            return newPlayer;
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);


        String uuidString = yml.getString("wellplayer.uuid");
        if (uuidString == null) {
            return null;
        }

        return new WellPlayer(
                UUID.fromString(uuidString),
                yml.getInt("wellplayer.bronzeCoins"),
                yml.getInt("wellplayer.silverCoins"),
                yml.getInt("wellplayer.goldCoins"),
                yml.getInt("wellplayer.coinsDeposited"),
                yml.getInt("wellplayer.experiencePoints")
        );
    }

    @Override
    public void updateWellPlayer(@NotNull WellPlayer wellPlayer) {
        UUID uuid = wellPlayer.getPlayerUUID();

        File file = new File(wellPlayersPath + uuid.toString() + ".yml");

        if (!file.exists())
            throw new RuntimeException("Cannot update this well player, as they have no file.");

        assignWellPlayerValues(wellPlayer, uuid, file);
    }

    private void assignWellPlayerValues(@NotNull WellPlayer wellPlayer, UUID uuid, File file) {
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

            yml.set("wellplayer.uuid", uuid.toString());
            yml.set("wellplayer.bronzeCoins", wellPlayer.getBronzeCoins());
            yml.set("wellplayer.silverCoins", wellPlayer.getSilverCoins());
            yml.set("wellplayer.goldCoins", wellPlayer.getGoldCoins());
            yml.set("wellplayer.coinsDeposited", wellPlayer.getCoinsDeposited());
            yml.set("wellplayer.experiencePoints", wellPlayer.getExperiencePoints());

            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteWellPlayer(@NotNull WellPlayer wellPlayer) {
        UUID uuid = wellPlayer.getPlayerUUID();

        File file = new File(wellPlayersPath + uuid.toString() + ".yml");

        if (!file.exists())
            return;

        file.delete();
    }

    @Override
    public DatabaseType databaseType() {
        return DatabaseType.YAML;
    }
}
