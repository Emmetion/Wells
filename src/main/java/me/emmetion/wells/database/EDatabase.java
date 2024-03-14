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
import java.util.Objects;
import java.util.UUID;

import static me.emmetion.wells.util.Utilities.getComponentColor;

public abstract class EDatabase {


    public abstract void initializeDatabase();

    // Well methods
    public abstract void createWell(Well well);

    public abstract Well getWell(String townName);

    public abstract void updateWell(Well well);

    public abstract void deleteWell(Well well);

    // WellPlayer methods
    public abstract void createWellPlayer(WellPlayer wellPlayer);

    public abstract WellPlayer getWellPlayer(Player player);

    public abstract void updateWellPlayer(WellPlayer wellPlayer);

    public abstract void deleteWellPlayer(WellPlayer wellPlayer);

    public abstract DatabaseType databaseType();

    /**
     * Sends well placement announcement to all residents in the town.
     */
    public void announceWellPlacement(@NotNull Well well) {
        Town town = TownyAPI.getInstance().getTown(well.getTownName());
        assert town != null;

        town.getResidents().forEach(resident -> {
            resident.sendMessage((Component) getComponentColor("---- Town Announcement ----"));
            resident.sendMessage((Component) getComponentColor("&eA well has been created in your town!"));
            resident.sendMessage((Component) getComponentColor("Well '" + well.getTownName() + "' Level: &e" + well.getWellLevel()));
            resident.sendMessage((Component) getComponentColor("&ex: " + well.getLocation().getBlockX() + " y: " + well.getLocation().getBlockY()+" z: " + well.getLocation().getBlockZ()));
        });
    }

    public HashMap<UUID, WellPlayer> getOnlineWellPlayers() throws SQLException {
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

    public HashMap<String, Well> getWellsFromDatabase() {
        HashMap<String, Well> wells = new HashMap<>();

        TownyAPI.getInstance().getTowns().stream()
                .map(Town::getName)
                .map(this::getWell)
                .filter(Objects::nonNull) // Remove null wells from list.
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
    public void updateWells(@NotNull Collection<Well> wells) {
        wells.forEach(this::updateWell);
    }

    public void updateWellPlayers(@NotNull Collection<WellPlayer> wellPlayers) {
        wellPlayers.forEach(this::updateWellPlayer);
    }



}

