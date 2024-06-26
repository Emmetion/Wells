package me.emmetion.wells.runnables;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.emmetion.wells.anim.NearWellAnimation;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.model.WellPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public final class NearWellRunnable extends BukkitRunnable {
    public final WellManager wellManager;

    private final HashMap<Player, String> playersNearWell = new HashMap<>();
    private final HashMap<Player, Integer> nearWellMessageCooldown = new HashMap<>();

    public NearWellRunnable(WellManager wellManager) {
        this.wellManager = wellManager;
    }


    @Override
    public void run() {
        // Temporarily stores current nearby well players.
        // Later iterated over when removing non-nearby players.
        List<Player> currentWellPlayers = new ArrayList<>();

        double startTime = System.nanoTime();

        for (Well w : wellManager.getWells()) {
            Location location = w.getLocation();
            Collection<Player> nearbyPlayers = location.getNearbyPlayers(10);

            for (Player p : nearbyPlayers) {
                if (p == null || !p.isOnline())
                    continue;

                WellPlayer wellPlayer = wellManager.getWellPlayer(p);

                if (playersNearWell.containsKey(p)) { // if player was already near a well
                    if (!playersNearWell.get(p).equals(w.getTownName())) { // and the well the player is currently at is different. (maybe tp, other reasons idk)

                        w.removeNearbyPlayer(wellPlayer); // remove from well's collection.
                        playersNearWell.remove(p); // remove from the list
                    }
                }

                currentWellPlayers.add(p); // add the player to the list.
                playersNearWell.put(p, w.getWellName()); // put the player into our HashMap ( THIS CANNOT BE w.getTownName() because it will break the .substring() call down the line in NearWellRunnable.java line 94)
                w.addNearbyPlayer(wellPlayer); // add nearby player


                // Player is now marked as near well.
                // Now we can set all entities we want to show to true.

                Hologram hologram = DHAPI.getHologram(w.getTownName()); // grabs hologram via. well name.
                if (hologram == null) {
                    hologram = createWellHologram(w);
                }
                hologram.setShowPlayer(p); // displays the well hologram to the player.

                NearWellAnimation nearWellAnimation = w.getNearWellAnimation();
                //

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

            if (playersNearWell.containsKey(p)) {
                String wellName = playersNearWell.get(p);
                Well w = wellManager.getWellByWellName(wellName);
                NearWellAnimation anim = w.getNearWellAnimation();

                Hologram h = DHAPI.getHologram(w.getTownName());
                if (h == null) {
                    h = createWellHologram(w);
                }

                h.removeShowPlayer(p); // Hides the hologram from the player.

                Well wellByWellName = wellManager.getWellByWellName(wellName);
                wellByWellName.removeNearbyPlayer(wp);

                h.hide(p);

                if (wellManager.isDebug())
                    p.sendMessage("☢ You are no longer near a well. [" + wellName + "]");

                playersNearWell.remove(p);
            }
        }

        double endTime = System.nanoTime();
        double msBetween = endTime - startTime;
        // Currently, this algorithm runs at about ~10,000ns every tick.
        // This operation could be further
//        Player emmetion = Bukkit.getPlayer("Emmetion");
//        if (emmetion != null) {
//            emmetion.sendMessage("msBetween: " + msBetween + "ns");
//        }
    }

    /**
     * This method creates a well hologram provided at the given well's
     * location.
     * <p> <link href="https://google.com">
     * This will not create a new well hologram if one already exists with that name.
     *
     * @param well Well where you want the hologram loaded.
     * @return The new Well Hologram
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
