package me.emmetion.wells.config;

import me.emmetion.wells.Wells;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static me.emmetion.wells.util.Utilities.getColor;

public class Configuration {

    private static Configuration configuration;

    private final Wells wells;

    private final YamlConfiguration yamlConfig = new YamlConfiguration();
    private final File configFile;

    private static UUID spawnNPCUUID;

    /**
     * SQL LOGIN CREDENTIALS
     **/
    private static boolean sqlEnabled = false;
    private static String username = "";
    private static String password = "";
    private static String url = "";

    public static Configuration getInstance() {
        if (configuration == null) {
            configuration = new Configuration(Wells.plugin); // hardcoded singleton.
        } return configuration;
    }

    private Configuration(Wells plugin) {
        this.wells = plugin;

        if (!wells.getDataFolder().exists()) {
            wells.getDataFolder().mkdirs();
        }

        File configFile = new File(wells.getDataFolder(), "config.yml"); if (!configFile.exists()) {
            wells.saveResource("config.yml", false); wells.getLogger().info("Default config created.");
        }

        this.configFile = configFile; try {
            yamlConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // set static variables

        sqlEnabled = yamlConfig.getBoolean("SQL.enabled"); username = yamlConfig.getString("SQL.username"); password = yamlConfig.getString("SQL.password");
        url = yamlConfig.getString("SQL.url");

        // TODO: Remove debug.

        System.out.println("SQL Debugging."); System.out.println("sqlEnabled = " + sqlEnabled); System.out.println("username = " + username);
        System.out.println("password = " + password); System.out.println("url = " + url);

    }

    public void saveConfigFile() {
        File configFile = new File(wells.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            wells.saveResource("config.yml", false); wells.getLogger().info("Default config created.");
        }
    }

    public YamlConfiguration getYaml() {
        return this.yamlConfig;
    }

    public String getSQLUsername() {
        return username;
    }

    public String getSQLPassword() {
        return password;
    }

    public String getSQLURL() {
        return url;
    }

    public boolean hasSpawnNPCUUID() {
        if (spawnNPCUUID == null) {
            return false;
        }

        return true;
    }



    public UUID getSpawnNPCUUID() {
        String s = yamlConfig.getString("wells.spawn-npc-uuid");
        yamlConfig.set("wells.spawn-npc-uuid", false);

        UUID uuid = null;

        if (hasSpawnNPCUUID()) {
            return spawnNPCUUID;
        }

        return null;
    }

    public void setSpawnNPCUUID(UUID uuid) {
        this.yamlConfig.setComments("wells.spawn-npc-uuid", Arrays.asList("SpawnNPC's are ", "", ""));
        this.yamlConfig.set("wells.spawn-npc-uuid", uuid.toString());

        spawnNPCUUID = uuid;
    }

    public Location getSpawnNPCLocation() {
        // TODO: Implement getting location from configuration instead of hard-coded spawn location.
        Location loc = yamlConfig.getLocation("well.spawn-npc-location");

        return new Location(Bukkit.getWorld("world"), 144, 68, -139);
    }
}
