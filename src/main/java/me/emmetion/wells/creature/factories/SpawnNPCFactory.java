package me.emmetion.wells.creature.factories;

import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.creature.creatures.SpawnNPC;
import me.emmetion.wells.managers.CreatureManager;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class SpawnNPCFactory implements CreatureFactory {

    public CreatureManager manager;

    public SpawnNPCFactory(CreatureManager manager) {
        this.manager = manager;
    }

    @Override
    public WellCreature createCreature(@Nullable Well well, Location location) {

        return new SpawnNPC(location);
    }
}
