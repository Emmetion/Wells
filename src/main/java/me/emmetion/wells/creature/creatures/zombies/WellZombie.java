package me.emmetion.wells.creature.creatures.zombies;

import com.palmergames.bukkit.towny.object.Town;
import me.emmetion.wells.creature.WellBound;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/*
 This represents a Zombie attached to a well in the world.
 The zombie is "WellBound", meaning it will not deviate away from the well.
 */
public abstract class WellZombie extends WellCreature implements WellBound {

    private Well boundWell;
    private Town town;
    private Entity zombieEntity;

    /**
     * Constructor for the WellZombie
     * @param well The well the zombie is bound to
     */
    public WellZombie(@NotNull Well well, @NotNull Location location) {
        super(location);
        this.boundWell = well;
    }

    @Override
    public Well getBoundWell() {
        return boundWell;
    }

    @Override
    public void kill() {
        if (super.getEntity() != null) {
            super.getEntity().remove();
        }
    }
}
