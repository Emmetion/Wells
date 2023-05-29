package me.emmetion.wells.model;

import me.emmetion.wells.observer.Observer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Well {

    private List<Observer> observers = new ArrayList<>();

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

    public String getWellName() {
        return townName + "'s Well";
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public Location getLocation() {
        return position;
    }

    public void setLocation(Location position) {
        this.position = position;
    }

    public void incrementLevel() {
        notifyObservers();
        this.level += 1;
    }

    public void decrementLevel() {
        notifyObservers();
        this.level -= 1;
    }

    public int getLevel() {
        return this.level;
    }

    public void attachObserver(Observer attached) {
        observers.add(attached);
    }

    public void detachObserver(Observer detach) {
        observers.remove(detach);
    }

    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    private void performStateChange() {
        notifyObservers();
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
