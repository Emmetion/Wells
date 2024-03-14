package me.emmetion.wells.creature.creatures.zombies;

import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.CreatureType;
import me.emmetion.wells.events.creature.CreatureClickEvent;
import me.emmetion.wells.managers.CreatureManager;
import me.emmetion.wells.model.Well;
import me.emmetion.wells.util.Utilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ExcessWaterZombie extends WellZombie {

    private final CreatureManager manager = Wells.plugin.getCreatureManager();

    private int healthPoints = 5;

    public ExcessWaterZombie(Well well, Location location) {
        super(well, location);
    }

    @Override
    public CreatureType getCreatureType() {
        return CreatureType.EXCESS_WATER_ZOMBIE;
    }

    @Override
    public Entity handleEntitySpawn(Entity entity) {
        return entity;
    }

    @Override
    public void handleLeftClick(CreatureClickEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("You clicked the excess water zombie.");
        healthPoints -= 1;
        if (healthPoints <= 0) {
            // Remove the entity
            manager.removeCreature(this);
            player.sendMessage("&fYou have killed the &bWATER ZOMBIE&f!");
        }
    }

    @Override
    public void updateCreature() {
        // This is called every frame

        Location location = super.getEntity().getLocation();
        boolean dead = super.getEntity().isDead();
        if (dead) {
            manager.removeCreature(this);
            return;
        }
        Utilities.sendDebugMessage("Updating ExcessWaterZombie: " + super.getEntity().getType());
    }

    @Override
    public void handleRightClick(CreatureClickEvent event) {

    }

    @Override
    public EntityType creatureEntityType() {
        return EntityType.ZOMBIE;
    }
}
