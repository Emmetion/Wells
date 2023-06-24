package me.emmetion.wells.creature;

public enum CreatureType {
    SPAWN_NPC(SpawnNPC.class),
    PIXIE(Pixie.class),
    ;

    // for some reason, Class<? extends WellCreature does not work in this case.
    // I tried to implement something similar to bukkits EntityType class.

    private Class<? extends WellCreature> clazz;

    CreatureType(Class<? extends WellCreature> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends WellCreature> getCreatureClazz() {
        return clazz;
    }

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
