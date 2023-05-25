package me.emmetion.wells.model;

import org.bukkit.Location;

public class Well {

    public String townName;
    public Location position;
    private int level;

    public Well(String townName, Location position, int level) {
        this.townName = townName;
        this.position = position;
        this.level = level;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public void incrementLevel() {
        this.level += 1;
    }

    public void decrementLevel() {
        this.level -= 1;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public String toString() {
        return "Well{" +
                "townName='" + townName + '\'' +
                ", position=" + position +
                ", level=" + level +
                '}';
    }
}
