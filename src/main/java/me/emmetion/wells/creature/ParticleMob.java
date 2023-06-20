package me.emmetion.wells.creature;

import org.bukkit.Particle;

public interface ParticleMob {

    /**
     * Particle mobs are mobs that use particles around them.
     * Every tick they display particles.
     * @return Particle trail meant for mob.
     */
    Particle particle();

}
