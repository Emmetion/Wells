package me.emmetion.wells.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.model.Well;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public abstract class WellCreature {

    // Every well creature has a customm UUID. These uuids are mapped inside of CreatureManager.java.
    private UUID uuid;
    // stores the current location, the the creature is an entity, particle, or just a hitbox.
    private Location currentLocation;

    private Class<? extends Entity> defaultEntityClass = ArmorStand.class;
    // Entity of hitbox/monster/trader
    private Entity entity;

    private int frame = 0;

    private CreatureType creatureType;

    public WellCreature(Location location) {
        currentLocation = location;
        this.uuid = UUID.randomUUID();

        this.entity = spawn();
    }


    public CreatureType getCreatureType() {
        return creatureType;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Entity spawn() {
        World world = this.currentLocation.getWorld();

        Entity entity;
        if (entityClassType() == null) {
            // if the entity class was null, then we will assume it's an armorstand and
            // handle entity spawn the same way.
            entity = world.spawn(currentLocation, defaultEntityClass);
        } else {
            entity = world.spawn(currentLocation, entityClassType());
        }

        handleEntitySpawn(entity);

        this.entity = entity;
        return entity;
    }

    public abstract void handleEntitySpawn(Entity entity);

    public void kill() {
        if (this.entity == null || this.entity.isDead())
            return;
        // removes the entity in-game. This will change to be updated with NPC's, should maybe make it overwritable.
        this.entity.remove();
    }

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

        if (this.currentLocation.distance(location) >= 1) {
            System.out.println("Cannot teleport over a distance >= 1.");
        }
        this.entity.teleport(location);
    }

    public Location getLocation() {
        return currentLocation;
    }

    public abstract void handleInteraction(PlayerEvent event);

    public abstract Class<? extends Entity> entityClassType();

    // this can be used to update the creature on a frame-by-frame basis.
    // could check it's potiooon
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
}
