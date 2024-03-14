package me.emmetion.wells.creature;

import me.emmetion.wells.creature.creatures.Pixie;
import me.emmetion.wells.creature.creatures.SpawnNPC;
import me.emmetion.wells.creature.creatures.zombies.WellZombie;

public enum CreatureType {
    SPAWN_NPC(SpawnNPC.class),
    PIXIE(Pixie.class),
    EXCESS_WATER_ZOMBIE(WellZombie.class);

    // for some reason, Class<? extends WellCreature does not work in this case.
    // I tried to implement something similar to bukkits EntityType class.

    /**
     * The class of the creature.
     */
    private Class<? extends WellCreature> clazz;

    CreatureType(Class<? extends WellCreature> clazz) {
        this.clazz = clazz;
    }

    /**
     * Returns the class of the creature.
     * @return
     */
    public Class<? extends WellCreature> getCreatureClazz() {
        return clazz;
    }

    /**
     * Returns the CreatureType from the given class.
     * @param clazz
     * @return
     */
    public static CreatureType getFromClazz(Class<? extends WellCreature> clazz){
        for (CreatureType type : CreatureType.values()) {
            if (type.getCreatureClazz().equals(clazz))
                return type;
        }
        return null;
    }

    @Override
    public String toString() {
        return "CreatureType{" +
                "clazz=" + clazz +
                '}';
    }
}
