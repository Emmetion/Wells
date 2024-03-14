package me.emmetion.wells.creature.factories;

import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.creature.creatures.Pixie;
import me.emmetion.wells.model.Well;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class PixieFactory implements CreatureFactory {

    @Override
    public WellCreature createCreature(Well well, @Nullable Location location) {

        Location clone = well.getLocation().clone();
        float radius = 3;
        double x0 = clone.getX();
        double y0 = clone.getY();
        double z0 = clone.getZ();

        y0 += 3;

        ThreadLocalRandom random = ThreadLocalRandom.current();

        double radians = Math.toRadians(random.nextInt(360));
        z0 += radius * Math.sin(radians);
        x0 += radius * Math.cos(radians);

        Location newLoc = new Location(clone.getWorld(), x0, y0, z0);

        return new Pixie(well, newLoc);

    }
}
