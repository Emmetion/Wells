package me.emmetion.wells.database;

import de.tr7zw.nbtapi.NBTEntity;
import me.emmetion.wells.creature.*;
import me.emmetion.wells.model.Well;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreatureManager {

    private Map<UUID, WellCreature> wellCreatureMap;
    private Map<Well, List<UUID>> wellsWithCreatures;


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
        for (WellCreature wc : wellCreatureMap.values()) {
            if (wc instanceof Movable) { // if creature can move, it executes the movement method on it.
                Movable movable = (Movable) wc;
                movable.move();
            }
            wc.updateCreature();
            wc.incrementFrame();
        }
    }

    public WellCreature getWellCreature(UUID uuid) {
        if (wellCreatureMap.containsKey(uuid)) {
            return wellCreatureMap.get(uuid);
        }
        return null;
    }

    public List<WellCreature> getCreaturesAtWell(Well well) {
        if (!wellCreatureMap.containsKey(well)) {
            return null;
        }

        // maps UUID's to WellCreature, then collects them as a List.
        List<WellCreature> creatures = this.wellsWithCreatures.get(well).stream()
                .map(uuid -> this.wellCreatureMap.get(uuid))
                .collect(Collectors.toList());

        return creatures;
    }

    private void addToMaps(WellCreature creature, Well well) {
        if (creature.getUUID() == null) {
            System.out.println("Attempted to spawn creature with null UUID.");
            return;
        } else if (well == null) {
            System.out.println("Attempted to spawn creature with null Well.");
        }

        this.wellCreatureMap.put(creature.getUUID(), creature);

        List<UUID> uuids = this.wellsWithCreatures.get(well);
        uuids.add(creature.getUUID());

        this.wellsWithCreatures.put(well, uuids);
    }

    private void removeFromMaps(UUID uuid) {
        WellCreature wellCreature = this.wellCreatureMap.get(uuid);
        if (wellCreature == null)
            return;

        this.wellCreatureMap.remove(wellCreature);
        if (wellCreature instanceof WellBound) {
            WellBound wb = (WellBound) wellCreature;
            Well boundWell = wb.getBoundWell();
            List<UUID> uuids = this.wellsWithCreatures.get(boundWell);
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
                    System.out.println("Well cannot be null when spawning a creature in!");
                    System.out.println("A creature must be bounded by a well, otherwise we have no anchor for it's spawning location.");
                }

                wellCreature = new Pixie(well, well.getHologramLocation());
                break;
            default:
                break;
        }

        if (wellCreature == null) {
            return;
        }

        // add to manager maps.
        addToMaps(wellCreature, well);
    }

    public WellCreature getWellCreatureFromEntity(Entity entity) {
        if (entity == null)
            return null;

        NBTEntity nbtEntity = new NBTEntity(entity);
        String creature_uuid = nbtEntity.getString("creature_uuid");
        if (creature_uuid == null) {
            return null;
        }
        return wellCreatureMap.get(creature_uuid);
    }

    public CreatureType getCreatureTypeFromEntity(Entity e) {
        if (this.getWellCreatureFromEntity(e) == null)
            return null;
        return this.getWellCreatureFromEntity(e).getCreatureType();
    }

    public void killCreature(UUID uuid) {
        if (uuid == null)
            return;

        WellCreature wellCreature = this.wellCreatureMap.get(uuid);
        if (wellCreature == null)
            return;

        wellCreature.kill();
        removeFromMaps(uuid);
    }



    public void killCreature(WellCreature wellCreature) {
        if (wellCreature == null)
            return;
        killCreature(wellCreature.getUUID());
    }

}
