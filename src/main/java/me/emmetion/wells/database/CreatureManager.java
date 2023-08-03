package me.emmetion.wells.database;

import de.tr7zw.nbtapi.NBTEntity;
import me.emmetion.wells.Wells;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.creature.*;
import me.emmetion.wells.model.Well;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.emmetion.wells.util.Utilities.getColor;
import static me.emmetion.wells.util.Utilities.getComponentColor;

public class CreatureManager {

    private final Logger logger = Wells.plugin.getLogger();

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
        creatures.forEach(this::removeCreature);
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
        List<WellCreature> creatures = this.wellsWithCreatures.get(well).stream().map(uuid -> this.wellCreatureMap.get(uuid)).collect(Collectors.toList());

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

    public WellCreature spawnCreature(Class<? extends WellCreature> creature, @Nullable Well well) {
        CreatureType type = CreatureType.getFromClazz(creature);
        WellCreature wellCreature = null;

        Objects.requireNonNull(type);

        switch (type) {
            case PIXIE -> {
                if (well == null) {
                    Bukkit.broadcast(Component.text("Well cannot be null when spawning a creature in!"));
                    Bukkit.broadcast(Component.text("A creature must be bounded by a well, otherwise we have no " + "anchor for it's spawning location."));
                    break;
                }
                wellCreature = new Pixie(well, well.getHologramLocation());
            }
            case SPAWN_NPC -> {
                Location spawnNPCLocation = Configuration.getInstance().getSpawnNPCLocation();
                Configuration config = Configuration.getInstance();
                UUID uuid = config.getSpawnNPCUUID();
                if (isSpawnNPCSpawned()) {
                    Bukkit.broadcast(getComponentColor("&cNPC tried to get spawned but one already existed."));
                    return null;
                }
                wellCreature = new SpawnNPC(spawnNPCLocation);
            }
            default -> {

            }
        }

        if (wellCreature == null) {
            Bukkit.broadcast(Component.text("No proper CreatureType was specified."));
            return null; // no proper CreatureType was specified.
        }

        // add to manager maps.
        addToMaps(wellCreature, well);

        return wellCreature;
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

        if (creature_uuid == null) {
            // creature does not exist.
            return null;
        }


        UUID uuid = UUID.fromString(creature_uuid);
        if (debug) {
            // TODO: REMOVE-DEBUG
            // send debug message
            e.sendMessage(uuid.toString());
        }
        return wellCreatureMap.get(uuid);
    }

    public CreatureType getCreatureTypeFromEntity(Entity e) {
        if (this.getWellCreatureFromEntity(e) == null)
            return null;

        return this.getWellCreatureFromEntity(e).getCreatureType();
    }

    private void removeCreature(@NotNull UUID uuid) {
        WellCreature wellCreature = this.wellCreatureMap.get(uuid);
        if (wellCreature == null)
            return;

        removeFromMaps(uuid);
    }


    public void removeCreature(@NotNull WellCreature wellCreature) {
        removeCreature(wellCreature.getUUID());
    }

    public boolean isSpawnNPCSpawned() {

        // TODO: Get entity UUID from config file.

        return getSpawnNPC() == null;
    }

    public NPC getSpawnNPC() {

        Configuration config = Configuration.getInstance();
        Location spawnNPCLocation = config.getSpawnNPCLocation();

        return spawnNPCLocation.getNearbyEntities(1, 1, 1).stream()
                .filter(entity -> entity.hasMetadata("NPC"))
                .map(CitizensAPI.getNPCRegistry()::getNPC)
                .filter(npc1 -> npc1.hasTrait(SpawnNPC.SpawnTrait.class))
                .findFirst()
                .orElse(null);

    }

    public static UUID getUUIDFromEntity(Entity e) {
        NBTEntity entity = new NBTEntity(e);
        if (!entity.hasKey("creature_uuid"))
            return null;

        return entity.getUUID("creature_uuid");
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void saveCreatures() {
        Set<UUID> creatureUUIDs = wellCreatureMap.keySet();
        this.wellCreatureMap.keySet().forEach(uuid -> {
            WellCreature wc = wellCreatureMap.get(uuid);
            // TODO: Implement saving of WellCreatures.
            // wc.save();
            wc.kill();
        });

        for (UUID uuid : creatureUUIDs) {
            this.wellCreatureMap.remove(uuid);
        }
    }



}
