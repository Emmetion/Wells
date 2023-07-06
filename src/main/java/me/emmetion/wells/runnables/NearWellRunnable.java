package me.emmetion.wells.runnables;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.emmetion.wells.anim.NearWellAnimation;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class NearWellRunnable extends BukkitRunnable {
    public WellManager wellManager;

    private final HashMap<Player, String> playersNearWell = new HashMap<>();
    private final HashMap<Player, Integer> nearWellMessageCooldown = new HashMap<>();

    public NearWellRunnable(WellManager manager) {
        this.wellManager = manager;
    }


    @Override
    public void run() {
        // Temporarily stores current nearby well players.
        // Later iterated over when removing non-nearby players.
        List<Player> currentWellPlayers = new ArrayList<>();

        for (Well w : this.wellManager.getWells()) {
            Location location = w.getLocation();
            Collection<Player> nearbyPlayers = location.getNearbyPlayers(10);

            for (Player p : nearbyPlayers) {
                if (p == null || !p.isOnline())
                    continue;

                WellPlayer wellPlayer = wellManager.getWellPlayer(p);

                if (playersNearWell.containsKey(p)) { // if player was already near a well
                    if (!playersNearWell.get(p).equals(w.getTownName())) { // and the well the player is currently at is different. (maybe tp, other reasons idk)

                        w.removeNearbyPlayer(wellPlayer); // remove from well's collection.
                        this.playersNearWell.remove(p); // remove from the list
                    }
                }

                currentWellPlayers.add(p); // add the player to the list.
                playersNearWell.put(p, w.getTownName()); // put the player into our HashMap
                w.addNearbyPlayer(wellPlayer); // add nearby player

                Hologram hologram = DHAPI.getHologram(w.getTownName()); // grabs hologram via. well name.
                if (hologram == null) {
                    hologram = createWellHologram(w);
                }
                hologram.setShowPlayer(p); // displays the well hologram to the player.

                if (nearWellMessageCooldown.containsKey(p)) {
                    if (nearWellMessageCooldown.get(p) == -1) { // removes cooldown if time is at -1.
                        nearWellMessageCooldown.remove(p);
                    } else {
                        nearWellMessageCooldown.put(p, nearWellMessageCooldown.get(p) - 1); // count down every second.
                    }
                } else {
                    if (wellManager.isDebug())
                        p.sendMessage("☢ You are near a well! [" + w.getTownName() + "]");
                    nearWellMessageCooldown.put(p, 5); // -1 on cooldown each second.
                }
            }
        }


        List<Player> notNearWells = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !currentWellPlayers.contains(p))
                .collect(Collectors.toList());

        for (Player p : notNearWells) {

            WellPlayer wp = wellManager.getWellPlayer(p);

            if (this.playersNearWell.containsKey(p)) {
                String wellName = this.playersNearWell.get(p);
                Hologram h = DHAPI.getHologram(wellName);
                h.removeShowPlayer(p);

                Well wellByWellName = wellManager.getWellByWellName(wellName);

                wellByWellName.removeNearbyPlayer(wp);

                h.hide(p);
                if (wellManager.isDebug())
                    p.sendMessage("☢ You are no longer near a well. [" + wellName + "]");

                this.playersNearWell.remove(p);
            }
        }
    }

    /**
     * This method creates a well hologram provided at the given well's
     * location.
     *
     * This will not create a new well hologram if one already exists with that name.
     *
     * @param well - Well where you want the hologram loaded.
     * @return
     */
    private Hologram createWellHologram(Well well) {
        if (DHAPI.getHologram(well.getTownName()) != null) {
            DHAPI.removeHologram(well.getTownName());
        }
        // now we create it knowing it doesn't exist.

        String townName = well.getTownName();
        Location location = well.getHologramLocation();
        boolean saveToFile = false;
        String levelbar = well.createLevelBar();

        List<String> lines = Arrays.asList(well.getWellName(), well.prettyPosition(), levelbar);

        Hologram hologram = DHAPI.createHologram(townName, location, saveToFile, lines);
        hologram.setDefaultVisibleState(false);

        return hologram;

    }

}
