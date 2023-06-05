package me.emmetion.wells.model;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.emmetion.wells.observer.Observer;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Date;
import java.util.List;

public class Well {

    private List<Observer> observers = new ArrayList<>();

    public String townName;
    public Location position;
    private Location hologramPosition;

    private int well_level;
    private int experience;

    private ActiveBuff buff1;
    private ActiveBuff buff2;
    private ActiveBuff buff3;

    public Well(String townName, Location position, Location hologramPosition,
                int well_level, int experience,
                String buff1_id, Date buff1_end,
                String buff2_id, Date buff2_end,
                String buff3_id, Date buff3_end) {
        this.townName = townName;
        this.position = position;
        this.hologramPosition = hologramPosition;
        this.well_level = well_level;
        this.experience = experience;
        if (buff1_id.equals("none")) {
            this.buff1 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff1 = new ActiveBuff(
                    buff1_id, buff1_end
            );
        }
        if (buff2_id.equals("none")) {
            this.buff2 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff2 = new ActiveBuff(
                    buff2_id, buff2_end
            );
        }
        if (buff3_id.equals("none")) {
            this.buff3 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff3 = new ActiveBuff(
                    buff3_id, buff3_end
            );
        }
    }

    public Well(String townName, Location location) {
        this(townName, location, location, 0, 0, "none", null, "none", null, "none", null);
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

    public int getExperience() {
        return experience;
    }

    public ActiveBuff getBuff1() {
        return buff1;
    }

    public ActiveBuff getBuff2() {
        return buff2;
    }

    public ActiveBuff getBuff3() {
        return buff3;
    }

    public Location getHologramLocation() {
        return this.hologramPosition;
    }

    public void setLocation(Location position) {
        this.position = position;
    }

    public Hologram recreateHologram() {
        if (DHAPI.getHologram(this.getWellName()) != null)
            DHAPI.removeHologram(this.getWellName());

        String wellName = this.getWellName();
        Location location = this.getHologramLocation();
        boolean saveToFile = false;
        List<String> lines = Arrays.asList(this.getWellName(), this.prettyPosition(), ChatColor.YELLOW + "Level: " + this.getWellLevel());

        Hologram hologram = DHAPI.createHologram(wellName, location, saveToFile, lines);
        hologram.setDefaultVisibleState(false);

        return hologram;

    }

    public boolean addHologramLocation(float x, float y, float z) {
        if (this.hologramPosition == null)
            return false;
        Location clone = this.hologramPosition.clone();
        Location subtract = clone.add(x, y, z);
        if (subtract.distance(this.position) > 5) {
            return false;
        } else {
            this.hologramPosition = subtract;
            return true;
        }
    }

    public boolean subtractHologramLocation(float x, float y, float z) {
        if (this.hologramPosition == null)
            return false;
        Location clone = this.hologramPosition.clone();
        Location subtract = clone.subtract(x, y, z);
        if (subtract.distance(this.position) > 5) {
            return false;
        } else {
            this.hologramPosition = subtract;
            return true;
        }
    }

    public void incrementLevel() {
        notifyObservers();
        this.well_level += 1;
    }

    public void decrementLevel() {
        notifyObservers();
        this.well_level -= 1;
    }

    public void depositCoin(WellPlayer wellPlayer, CoinType coinType) {

    }

    public int getWellLevel() {
        return this.well_level;
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

    public List<ActiveBuff> getActiveBuffs() {
        List<ActiveBuff> activeBuffs = Arrays.asList(buff1, buff2, buff3);
        ArrayList<ActiveBuff> realBuff = new ArrayList<>();
        for (ActiveBuff ab : activeBuffs) {
            if (ab.getBuffID().equals("none"))
                realBuff.add(ab);
        }

        return realBuff;
    }

    private void performStateChange() {
        notifyObservers();
    }

    public String prettyPosition() {
        return this.getLocation().toVector().toString();
    }

    @Override
    public String toString() {
        return "Well{" +
                "observers=" + observers +
                ", townName='" + townName + '\'' +
                ", position=" + position +
                ", hologramPosition=" + hologramPosition +
                ", well_level=" + well_level +
                ", experience=" + experience +
                ", buff1=" + buff1 +
                ", buff2=" + buff2 +
                ", buff3=" + buff3 +
                '}';
    }
}
