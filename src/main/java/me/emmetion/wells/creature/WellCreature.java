package me.emmetion.wells.creature;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.UUID;

public abstract class WellCreature {

    // Every well creature has a custom UUID. These uuids are mapped inside CreatureManager.java.
    private final UUID uuid;
    // stores the current location, the creature is an entity, particle, or just a hit-box.
    private Location currentLocation;

    private Location spawnLocation;

    private final Class<? extends Entity> defaultEntityClass = ArmorStand.class;
    // Entity of hitbox/monster/trader
    private Entity entity;

    private int frame = 0;

    private boolean isKilled = false;

    public WellCreature(Location location) {
        currentLocation = location;
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
        if (entityClassType() == null) {
            // if the entity class was null, then we will assume it's an armor-stand and
            // handle entity spawn the same way.
            entity = world.spawn(currentLocation, defaultEntityClass);
        } else {
            entity = world.spawn(currentLocation, entityClassType());
        }

        handleEntitySpawn(entity);

        this.entity = entity;
        this.spawnLocation = currentLocation;

        return entity;
    }

    public abstract void handleEntitySpawn(Entity entity);

    public abstract void kill();

    public void setLocation(Location location) {
        if (this.currentLocation.distance(location) >= 1) {
            System.out.println("Cannot move creature one blocks distance from it's current location");
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


    public abstract void handleLeftClick(EntityDamageByEntityEvent event);

    public abstract void handleRightClick(PlayerInteractAtEntityEvent event);

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
