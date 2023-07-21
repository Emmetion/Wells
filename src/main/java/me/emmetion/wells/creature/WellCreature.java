package me.emmetion.wells.creature;

import me.emmetion.wells.events.creature.CreatureSpawnEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.UUID;

public abstract class WellCreature {

    // Every well creature has a custom UUID. These uuids are mapped inside CreatureManager.java.
    private final UUID uuid;
    // stores the current location, the creature is an entity, particle, or just a hit-box.
    private Location currentLocation;

    private final Location originalLocation;

    private final Class<? extends Entity> defaultEntityClass = ArmorStand.class;

    // Entity of hitbox/monster/trader
    private Entity entity;

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

    public UUID getUUID() {
        return this.uuid;
    }

    public Entity spawn() {
        World world = this.currentLocation.getWorld();

        Entity entity;
        // TODO: Super-spaghetti code.
        // Update this code to remove entityClassType() == null, and also
        // simplify the actions we are taking here.
        if (entityClassType() == null) {
            // if the entity class was null, then we will assume it's an armor-stand and
            // handle entity spawn the same way.
            entity = world.spawn(currentLocation, defaultEntityClass);
            entity = handleEntitySpawn(entity);

        } else if (entityClassType().equals(EntityType.PLAYER)) { // NPC's handle with CITIZENS. (inside individual
            // creaturetypes)
            entity = world.spawn(currentLocation, EntityType.MARKER.getEntityClass());
            entity = handleEntitySpawn(entity);
        } else {
            entity = world.spawn(currentLocation, entityClassType());
            entity = handleEntitySpawn(entity);
        }

        this.entity = entity;

        CreatureSpawnEvent spawnEvent = new CreatureSpawnEvent(this, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);
        spawnEvent.callEvent();

        return entity;
    }

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
    public abstract void handleLeftClick(EntityDamageByEntityEvent event);

    // This method is used whenever a player right-clicks a WellCreature.
    // After this method is called, a CreatureClickEvent.java is called.
    public abstract void handleRightClick(PlayerInteractAtEntityEvent event);

    // This stores the entities class type. This is used when spawning in entities in the bukkit world.
    // If the entityClassType is set to a Player, it will instead spawn an NPC at the location.
    public abstract Class<? extends Entity> entityClassType();

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
