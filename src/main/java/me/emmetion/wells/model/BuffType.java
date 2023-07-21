package me.emmetion.wells.model;

import org.bukkit.Particle;

public enum BuffType {
    NONE("NONE", null),
    GREEN_THUMB("GREEN_THUMB", Particle.VILLAGER_HAPPY),
    RESISTANCE("RESISTANCE", Particle.NOTE);

    private final String buff_id;
    private final Particle anim_particle;

    BuffType(String buff_id, Particle particle) {
        this.buff_id = buff_id;
        this.anim_particle = particle;
    }

    public String getBuffID() {
        return buff_id;
    }

    public Particle getParticle() {
        return anim_particle;
    }

    @Override
    public String toString() {
        return "BuffType{" + "buff_id='" + buff_id + '\'' + ", anim_particle=" + anim_particle + '}';
    }
}