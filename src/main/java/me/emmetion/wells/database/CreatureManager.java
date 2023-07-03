package me.emmetion.wells.database;

import de.tr7zw.nbtapi.NBTEntity;
import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.*;
import me.emmetion.wells.model.Well;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static me.emmetion.wells.util.Utilities.getColor;

public class CreatureManager {

    private final HashMap<UUID, WellCreature> wellCreatureMap;
    private final HashMap<Well, Set<UUID>> wellsWithCreatures;

    private boolean debug = false;

    public CreatureManager() {
        wellCreatureMap = new HashMap<>();
        wellsWithCreatures = new HashMap<>();
    }

    /**
     * Every tick this is called, iterates through the known WellCreature map and finds instances of movable creatures.
     * It then executes the movement code that's defined in the class.
     * ex. Pixie#move();
     */
    public void handleFrameUpdate() {
        Player e = Bukkit.getPlayer("Emmetion");
        if (e != null) {
            e.sendActionBar(getColor("Creatures: " + wellCreatureMap.size()));
        }

        List<WellCreature> creatures = new ArrayList<>(); // store dead creatures, handle afterward.

        for (WellCreature wc : wellCreatureMap.values()) {
            if (wc.isKilled()) {
                continue;
            }
            if (wc == null)
                continue;
            if (wc instanceof Movable) { // if creature can move, it executes the movement method on it.
                Movable movable = (Movable) wc;
                movable.move();
            }
            wc.updateCreature();
            wc.incrementFrame();
        }

        // remove creatures that were newly marked killed.
        for (WellCreature wc : creatures) {
            this.removeCreature(wc);
        }
    }

    public WellCreature getWellCreature(UUID uuid) {
        if (wellCreatureMap.containsKey(uuid)) {
            return wellCreatureMap.get(uuid);
        }
        return null;
    }

    public List<WellCreature> getCreaturesAtWell(Well well) {
        if (!wellsWithCreatures.containsKey(well)) {
            return null;
        }

        // maps UUID's to WellCreature, then collects them as a List.
        List<WellCreature> creatures = this.wellsWithCreatures.get(well).stream()
                .map(uuid -> this.wellCreatureMap.get(uuid))
                .collect(Collectors.toList());

        return creatures;
    }

    public Collection<Well> getWellsWithCreatures() {
        return this.wellsWithCreatures.keySet();
    }

    public Collection<WellCreature> getAllWellCreatures() {
        return this.wellCreatureMap.values();
    }

    private void addToMaps(WellCreature creature, Well well) {
        if (creature.getUUID() == null) {
            System.out.println("Attempted to spawn creature with null UUID.");
            return;
        } else if (well == null) {
            System.out.println("Attempted to spawn creature with null Well.");
        }

        this.wellCreatureMap.put(creature.getUUID(), creature);

        Set<UUID> uuids = this.wellsWithCreatures.get(well);
        if (uuids == null)
            uuids = new HashSet<>();

        uuids.add(creature.getUUID());

        this.wellsWithCreatures.put(well, uuids);
    }

    private void removeFromMaps(UUID uuid) {

        WellCreature wellCreature = this.wellCreatureMap.get(uuid);
        if (wellCreature == null) {
            return;
        }

        this.wellCreatureMap.remove(uuid);

        if (wellCreature instanceof WellBound) {
            WellBound wb = (WellBound) wellCreature;
            Well boundWell = wb.getBoundWell();
            Set<UUID> uuids = this.wellsWithCreatures.get(boundWell);
            uuids.remove(wellCreature.getUUID());
            this.wellsWithCreatures.put(boundWell, uuids); // update well with removed wellcreature uuid.
        }
    }

    public void spawnCreature(Class<? extends WellCreature> creature, @Nullable Well well) {
        CreatureType type = CreatureType.getFromClazz(creature);
        WellCreature wellCreature = null;


        switch (type) {

            case PIXIE:
                if (well == null) {
                    Bukkit.broadcast(Component.text("Well cannot be null when spawning a creature in!"));
                    Bukkit.broadcast(Component.text("A creature must be bounded by a well, otherwise we have no " +
                            "anchor for it's spawning location."));
                    break;
                }

                wellCreature = new Pixie(well, well.getHologramLocation());
                break;

            case SPAWN_NPC:
                wellCreature = new SpawnNPC(new Location(Bukkit.getWorld("world"), 143, 67, -141));
                break;

            default:
                break;
        }

        if (wellCreature == null) {
            Bukkit.broadcast(Component.text("No proper CreatureType was specified."));
            return; // no proper CreatureType was specified.
        }

        // add to manager maps.
        addToMaps(wellCreature, well);
    }

    public WellCreature getWellCreatureFromEntity(Entity entity) {
        Player e = Bukkit.getPlayer("Emmetion");
        if (entity == null) {
            return null;
        }
        NamespacedKey nk = new NamespacedKey(Wells.plugin, "creature-uuid");
        String creature_uuid = entity.getPersistentDataContainer().get(nk, PersistentDataType.STRING);
//        for (NamespacedKey key : entity.getPersistentDataContainer().getKeys()) {
//            e.sendMessage(key.asString());
//        }

        UUID uuid = UUID.fromString(creature_uuid);
        if (debug) {
            e.sendMessage(uuid.toString());
        }
        return wellCreatureMap.get(uuid);
    }

    public CreatureType getCreatureTypeFromEntity(Entity e) {
        if (this.getWellCreatureFromEntity(e) == null)
            return null;
        return this.getWellCreatureFromEntity(e).getCreatureType();
    }

    public void removeCreature(UUID uuid) {
        if (uuid == null)
            return;

        WellCreature wellCreature = this.wellCreatureMap.get(uuid);
        if (wellCreature == null)
            return;

        removeFromMaps(uuid);
    }



    public void removeCreature(WellCreature wellCreature) {
        if (wellCreature == null)
            return;
        removeCreature(wellCreature.getUUID());
    }

    public static UUID getUUIDFromEntity(Entity e) {
        if (e == null)
            return null;

        NBTEntity entity = new NBTEntity(e);
        if (!entity.hasKey("creature_uuid"))
            return null;

        return entity.getUUID("creature_uuid");
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void saveCreatures() {
        this.wellCreatureMap.keySet().stream()
                .forEach(uuid -> wellCreatureMap.get(uuid).kill());

    }

}
