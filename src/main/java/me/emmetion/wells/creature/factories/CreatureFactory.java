package me.emmetion.wells.creature.factories;

import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public interface CreatureFactory {

    /**
     * Create a creature at the provided location.
     * This will also decide the spawn location depending on the creature.
     * @return WellCreature
     */
    WellCreature createCreature(Well well, @Nullable Location location);

}
