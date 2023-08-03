package me.emmetion.wells.database;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static me.emmetion.wells.util.Utilities.getComponentColor;

public abstract class EDatabase {

    abstract void initializeDatabase();

    // Well methods
    abstract void createWell(Well well);

    abstract Well getWell(String townName);

    abstract void updateWell(Well well);

    abstract void deleteWell(Well well);

    // WellPlayer methods
    abstract void createWellPlayer(WellPlayer wellPlayer);

    abstract WellPlayer getWellPlayer(Player player);

    abstract void updateWellPlayer(WellPlayer wellPlayer);

    abstract void deleteWellPlayer(WellPlayer wellPlayer);

    abstract DatabaseType databaseType();

    /**
     * Sends well placement announcement to all residents in the town.
     */
    protected void announceWellPlacement(@NotNull Well well) {
        Town town = TownyAPI.getInstance().getTown(well.getTownName());
        assert town != null;

        town.getResidents().forEach(resident -> {
            resident.sendMessage((Component) getComponentColor("---- Town Announcement ----"));
            resident.sendMessage((Component) getComponentColor("&eA well has been created in your town!"));
            resident.sendMessage((Component) getComponentColor("Well '" + well.getTownName() + "' Level: &e" + well.getWellLevel()));
            resident.sendMessage((Component) getComponentColor("&ex: " + well.getLocation().getBlockX() + " y: " + well.getLocation().getBlockY()+" z: " + well.getLocation().getBlockZ()));
        });
    }

    protected HashMap<UUID, WellPlayer> getOnlineWellPlayers() throws SQLException {
        HashMap<UUID, WellPlayer> wellPlayers = new HashMap<>();

        Bukkit.getOnlinePlayers().stream()
                .map(this::getWellPlayer)
                .toList()
                .forEach(wp -> {
                    UUID uuid = wp.getPlayerUUID();
                    wellPlayers.put(uuid, wp);
                });

        return wellPlayers;
    }

    protected HashMap<String, Well> getWellsFromDatabase() {
        HashMap<String, Well> wells = new HashMap<>();

        TownyAPI.getInstance().getTowns().stream()
                .map(Town::getName)
                .map(this::getWell)
                .forEach(well -> {
                    wells.put(well.getTownName(), well);
                });

        return wells;
    }


    /**
     * Iterate saving a list of wells into a MySQL Server.
     * Uses updateWell(Well well) on each
     * @param wells
     */
    protected void updateWells(@NotNull Collection<Well> wells) {
        wells.forEach(this::updateWell);
    }

    protected void updateWellPlayers(@NotNull Collection<WellPlayer> wellPlayers) {
        wellPlayers.forEach(this::updateWellPlayer);
    }

    enum DatabaseType {
        MYSQL,
        YAML
    }

}
