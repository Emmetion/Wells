package me.emmetion.wells.config;

import me.emmetion.wells.Wells;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Configuration {

    private static Configuration configuration;

    public static final NamespacedKey creatureUUIDKey = new NamespacedKey(Wells.plugin, "creature-uuid");

    private final Wells wells;
    private final YamlConfiguration yamlConfig = new YamlConfiguration();
    private final File configFile;


    // SpawnNPC Data.
    private UUID spawnNPCUUID;
    /**
     * SQL LOGIN CREDENTIALS
     **/
    private boolean sqlEnabled = false;
    private String username = "";
    private String password = "";
    private String url = "";

    private List<String> wellLevelUp = null;

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

        // either null or proper UUID.
        String uuidString = yamlConfig.getString("wells.spawn-npc-uuid", "");
        if (uuidString.equals(""))
            spawnNPCUUID = null;
        else
            spawnNPCUUID = UUID.fromString(uuidString);

        System.out.println("uuidString = " + uuidString);

        wellLevelUp = yamlConfig.getStringList("messages.well.level-up-announcement");
    }

    public static Configuration getInstance() {
        if (configuration == null) {
            configuration = new Configuration(Wells.plugin); // hardcoded singleton.
        }
        return configuration;
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

    public boolean getSQLEnabled() {
        return sqlEnabled;
    }

    public List<String> getWellLevelUp() {
        return this.wellLevelUp;
    }

    public boolean hasSpawnNPCUUID() {
        if (spawnNPCUUID == null) {
            return false;
        }

        return true;
    }


    public UUID getSpawnNPCUUID() {
        return spawnNPCUUID;
    }

    public void setSpawnNPCUUID(@NotNull UUID uuid) {
        yamlConfig.setComments("wells.spawn-npc-uuid", Arrays.asList("Do not modify these values.", "Instead use the in-game commands via. /wells spawnnpc"));
        yamlConfig.set("wells.spawn-npc-uuid", uuid.toString());

        save();

        spawnNPCUUID = uuid;
    }

    public Location getSpawnNPCLocation() {
        // TODO: Implement getting location from configuration instead of hard-coded spawn location.
        Location loc = yamlConfig.getLocation("well.spawn-npc-location", new Location(Bukkit.getWorld("world"), 144, 68, -139));
        //returns the location in config, or a default one if it's null.
        return loc;
    }

    private void save() {
        try {
            yamlConfig.save(configFile);
        } catch (IOException e) {
            System.out.println("Failed to save configuration file.");
            e.printStackTrace();
        }

    }


}
