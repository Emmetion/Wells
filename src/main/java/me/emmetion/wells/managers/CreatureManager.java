package me.emmetion.wells.managers;

import de.tr7zw.nbtapi.NBTEntity;
import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.CreatureType;
import me.emmetion.wells.creature.Movable;
import me.emmetion.wells.creature.WellBound;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.creature.factories.CreatureFactory;
import me.emmetion.wells.events.creature.CreatureKillEvent;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.util.Utilities;
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

import static me.emmetion.wells.util.Utilities.getColor;

public final class CreatureManager {

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
            e.sendActionBar(Component.text(getColor("Creatures: &c" + wellCreatureMap.size())));

        }

        List<WellCreature> creatures = new ArrayList<>(); // store dead creatures, handle afterward.

        for (WellCreature wc : wellCreatureMap.values()) {
            if (wc.isKilled()) {
                Utilities.sendDebugMessage("Creature is killed.");
                creatures.add(wc); // Will be deleted after other entities are updated.
                continue;
            }
            if (wc instanceof Movable movable) { // if creature can move, it executes the movement method on it.
                movable.move();
            }
            wc.updateCreature();
            wc.incrementFrame();
            Utilities.sendDebugMessage("Updating Creature: " + wc.getEntity().getType());

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
        return wellsWithCreatures.get(well).stream()
                .map(wellCreatureMap::get)
                .toList();

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

    /**
     * Removes a well create from maps storing information about them/
     * Called after a creature is killed.
     * @param uuid UUID of the creature to remove.
     */
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

    /**
     * Spawns a creature in the world.
     * @param creatureFactory Factory class to create the creature.
     * @param well The well to bind the creature to.
     * @return
     */
    public WellCreature spawnCreature(CreatureFactory creatureFactory, @Nullable Well well, @Nullable Location location) {
        WellCreature creature = creatureFactory.createCreature(well, location);

        addToMaps(creature, well); // add to manager maps.


        return creature;
    }

    /**
     * Get WellCreature from an entity in the world.
     * @param entity Entity to get the WellCreature from.
     * @return WellCreature
     */
    public WellCreature getWellCreatureFromEntity(@NotNull Entity entity) {
        NamespacedKey namespacedKey = new NamespacedKey(Wells.plugin, "creature-uuid");

        String creatureUUID = entity.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if (debug) {
            // Prints all NamespacedKey's at the entity.
            for (NamespacedKey key : entity.getPersistentDataContainer().getKeys()) {
                Player e = Bukkit.getPlayer("Emmetion");
                e.sendMessage(key.asString());
            }
        }

        if (creatureUUID == null) {
            // creature does not exist.
            return null;
        }


        UUID uuid = UUID.fromString(creatureUUID);
        if (debug) {
            // TODO: REMOVE-DEBUG
            // send debug message
            Player e = Bukkit.getPlayer("Emmetion");
            e.sendMessage(uuid.toString());
        }
        return wellCreatureMap.get(uuid);
    }

    public CreatureType getCreatureTypeFromEntity(Entity e) {
        if (getWellCreatureFromEntity(e) == null)
            return null;

        return this.getWellCreatureFromEntity(e).getCreatureType();
    }

    private void removeCreature(@NotNull UUID uuid) {
        WellCreature wellCreature = wellCreatureMap.get(uuid);
        if (wellCreature == null)
            return;

        CreatureKillEvent killEvent = new CreatureKillEvent(wellCreature);
        killEvent.callEvent();

        removeFromMaps(uuid);
    }


    public void removeCreature(@NotNull WellCreature wellCreature) {
        removeCreature(wellCreature.getUUID());
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
            if (uuid == null) {
                continue;
            }
            this.wellCreatureMap.remove(uuid);
        }
    }



}
