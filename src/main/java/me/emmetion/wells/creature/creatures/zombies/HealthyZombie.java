package me.emmetion.wells.creature.creatures.zombies;

import me.emmetion.wells.creature.CreatureType;
import me.emmetion.wells.events.creature.CreatureClickEvent;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * A healthy zombie that is bound to a well.
 * Killing this zombie will reward the player with XP / BUFFS.
 */
public class HealthyZombie extends WellZombie{

    /**
     * Constructor for the WellZombie
     * @param well     The well the zombie is bound to
     * @param location Location to spawn the zombie at.
     */
    public HealthyZombie(@NotNull Well well, @NotNull Location location) {
        super(well, location);
    }

    @Override
    public CreatureType getCreatureType() {
        return null;
    }

    @Override
    public Entity handleEntitySpawn(Entity entity) {
        return null;
    }

    @Override
    public void handleLeftClick(CreatureClickEvent event) {
        // Subtract healthpoints from the zombie.

    }

    @Override
    public void handleRightClick(CreatureClickEvent event) {

    }

    @Override
    public EntityType creatureEntityType() {
        return null;
    }

    @Override
    public void updateCreature() {

    }
}
