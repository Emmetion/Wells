package me.emmetion.wells.config;

import me.emmetion.wells.Wells;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static me.emmetion.wells.util.Utilities.getColor;

public class Configuration {

    private static Configuration configuration;

    private final Wells wells;

    private final YamlConfiguration yamlConfig = new YamlConfiguration();
    private final File configFile;

    /**  SQL LOGIN CREDENTIALS **/
    private static boolean sqlEnabled = false;
    private static String username = "";
    private static String password = "";
    private static String url = "";

    public static Configuration getInstance() {
        if (configuration == null) {
            configuration = new Configuration(Wells.plugin); // hardcoded singleton.
        }
        return configuration;
    }

    private Configuration(Wells plugin) {
        this.wells = plugin;

        if (!wells.getDataFolder().exists()) {
            wells.getDataFolder().mkdirs();
        }

        File configFile = new File(wells.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            wells.saveResource("config.yml", false);
            wells.getLogger().info("Default config created.");
        }

        this.configFile = configFile;
        try {
            yamlConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // set static variables

        sqlEnabled = yamlConfig.getBoolean("SQL.enabled");
        username = yamlConfig.getString("SQL.username");
        password = yamlConfig.getString("SQL.password");
        url = yamlConfig.getString("SQL.url");
    }

    public void saveConfigFile() {
        File configFile = new File(wells.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            wells.saveResource("config.yml", false);
            wells.getLogger().info("Default config created.");
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


    public UUID getOrCreateNewSpawnNPCUUID() {
        String s = yamlConfig.getString("wells.spawn-npc-uuid");
        if (s == null || s.equals("")) {
            Bukkit.broadcast(Component.text(getColor("&cgetOrCreateNewSpawnNPCUUID creating a new uuid.")));

            return UUID.randomUUID();
        }
        UUID uuid = UUID.fromString(s);

        return uuid;
    }

    public Location getSpawnNPCLocation() {
        // TODO: Implement getting location from configuration instead of hard-coded.
        return new Location(Bukkit.getWorld("world"), 144, 68, -139);
    }
}
