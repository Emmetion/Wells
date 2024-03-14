package me.emmetion.wells.creature;

import me.emmetion.wells.events.creature.CreatureClickEvent;
import me.emmetion.wells.events.creature.CreatureSpawnEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;

public abstract class WellCreature {

    // Every well creature has a custom UUID. These uuids are mapped inside CreatureManager.java.
    private final UUID uuid;
    // stores the current location, the creature is an entity, particle, or just a hit-box.
    private Location currentLocation;

    private final Location originalLocation;

    private final Class<? extends Entity> defaultEntityClass = ArmorStand.class;

    // Entity of hitbox/monster/trader
    private final Entity entity;

    private int frame = 0;

    private boolean isKilled = false;

    // Constructor for creating
    public WellCreature(UUID uuid, Location location) {
        this.currentLocation = location;
        this.originalLocation = location;
        this.uuid = uuid;

        this.entity = spawn();
    }

    // Constructor for creating a new WellCreature at the provided location.
    public WellCreature(Location location) {
        this.currentLocation = location;
        this.originalLocation = location;
        this.uuid = UUID.randomUUID();

        this.entity = spawn();
    }


    public abstract CreatureType getCreatureType();

    @NotNull
    public UUID getUUID() {
        return this.uuid;
    }

    private Entity spawn() {
        World world = this.currentLocation.getWorld();

        Entity entity;

        if (creatureEntityType().getEntityClass() == null || creatureEntityType().equals(EntityType.PLAYER)) {
            entity = world.spawn(currentLocation, EntityType.MARKER.getEntityClass());
        } else {
            entity = world.spawn(currentLocation, creatureEntityType().getEntityClass());
        }

        // This entity will transform into an NPC entity if the creature type was PLAYER.
        entity = handleEntitySpawn(entity);

        CreatureSpawnEvent spawnEvent = new CreatureSpawnEvent(this, CUSTOM);
        spawnEvent.callEvent();

        return entity;
    }


    /**
     * This method is different for other
     * @param entity The entity that was spawned.
     * @return The entity to apply creature-uuid to.
     */
    public abstract Entity handleEntitySpawn(Entity entity);

    public abstract void kill();

    public void setLocation(Location location) {
        if (this.currentLocation.distance(location) >= 3) {
            System.out.println("Cannot move creature three blocks distance from it's current location");
        }

        this.currentLocation = location;
        entity.teleport(location);
    }

    public void teleportEntityTo(Location location) {
        if (this.entity == null)
            return;

        this.entity.teleport(location);
    }

    public Location getLocation() {
        return currentLocation;
    }


    // This method is used whenever a player left-clicks a WellCreature.
    // After this method is called, a CreatureClickEvent.java is called.
    @EventHandler
    public abstract void handleLeftClick(CreatureClickEvent event);

    // This method is used whenever a player right-clicks a WellCreature.
    // After this method is called, a CreatureClickEvent.java is called.
    @EventHandler
    public abstract void handleRightClick(CreatureClickEvent event);

    // This stores the entities class type. This is used when spawning in entities in the bukkit world.
    @EventHandler
    // If the entityClassType is set to a Player, it will instead spawn an NPC at the location.
    public abstract EntityType creatureEntityType();

    // this can be used to update the creature on a frame-by-frame basis.
    // could check its potion
    public abstract void updateCreature();

    public boolean isSpawnFrame() {
        return this.frame == 0;
    }

    public void incrementFrame() {
        this.frame++;
    }

    public int getFrame() {
        return frame;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isKilled() {
        return this.isKilled;
    }

}
