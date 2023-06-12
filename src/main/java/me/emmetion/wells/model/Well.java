package me.emmetion.wells.model;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.emmetion.wells.anim.NearWellAnimation;
import me.emmetion.wells.observer.Observer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.sql.Date;
import java.util.stream.Collectors;

public class Well {

    private final List<Observer> observers = new ArrayList<>();

    private final List<WellPlayer> nearbyPlayers = new ArrayList<>();

    // Will use 'contributions' to create a total donations board.
    // Storable in the MySQL server.
    private final HashMap<UUID, Integer> contributions = new HashMap<>();

    public String townName;
    public Location position;
    private Location hologramPosition;

    private int well_level;
    private int experience;

    private int experienceRequired; // this is calculated upon initialization, and then later used when we deposit coins.

    private ActiveBuff buff1;
    private ActiveBuff buff2;
    private ActiveBuff buff3;

    private NearWellAnimation animation;

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
        if (buff1_id.equals("NONE")) {
            this.buff1 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff1 = new ActiveBuff(
                    buff1_id, buff1_end
            );
        }
        if (buff2_id.equals("NONE")) {
            this.buff2 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff2 = new ActiveBuff(
                    buff2_id, buff2_end
            );
        }
        if (buff3_id.equals("NONE")) {
            this.buff3 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff3 = new ActiveBuff(
                    buff3_id, buff3_end
            );
        }

        // calculate experience needed for next level.
        this.experienceRequired = 100 + (well_level * 5); // increments experience needed by 5 every level.

        this.animation = new NearWellAnimation(this);
        this.animation.start();
    }

    public Well(String townName, Location location) {
        this(townName, location, location, 0, 0, "NONE", null, "NONE", null, "NONE", null);

        this.animation = new NearWellAnimation(this);
        this.animation.start();
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

    public boolean hasBuff1() {
        return !buff1.isNone();
    }

    public boolean hasBuff2() {
        return !buff2.isNone();
    }

    public boolean hasBuff3() {
        return !buff3.isNone();
    }



    public void setActiveBuff(ActiveBuff activeBuff) {
        if (!hasBuff1()) {
            buff1 = activeBuff;
        } else if (!hasBuff2()) {
            buff2 = activeBuff;
        } else if (!hasBuff3()) {
            buff3 = activeBuff;
        } else {

        }
    }

    public Hologram updateHologram() {
        Bukkit.broadcast(Component.text(""));

        List<String> lines = Arrays.asList(this.getWellName(), ChatColor.YELLOW + "Level: " + this.getWellLevel(), ChatColor.BLUE + "XP: " + this.experience + "/" + this.experienceRequired);

        Hologram hologram;
        if (DHAPI.getHologram(this.getWellName()) == null)
            hologram = DHAPI.createHologram(this.getWellName(), this.getHologramLocation(), false, lines);
        else
            hologram = DHAPI.getHologram(this.getWellName());

        hologram.setLocation(hologramPosition);
        hologram.realignLines();

        DHAPI.setHologramLine(hologram, 1, ChatColor.YELLOW + "Level: " + this.getWellLevel());
        DHAPI.setHologramLine(hologram, 2, ChatColor.BLUE + "XP: " + this.experience + "/" + this.experienceRequired);

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
            updateHologram();
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
            updateHologram();
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
        if (coinType == null)
            return;

        if (coinType.equals(CoinType.LEVEL_UP_COIN)) {
            incrementLevel();
            this.experience = 0;
            this.experienceRequired = 100 + (well_level * 5);
            updateHologram();
            return;
        }

        this.animation.enqueueDepositedCoinType(coinType);

        if (experience + coinType.getExperience() >= this.experienceRequired) {

            wellPlayer.sendMessage("Your well has leveled up!");
            incrementLevel();
//            wellPlayer.sendMessage("this.experience = " + ((experience + coinType.getExperience()) % this.experienceRequired));
            this.experience = ((experience + coinType.getExperience()) % this.experienceRequired);

            this.experienceRequired = 100 + (well_level * 5);
            // coin will increase level
        } else {
            experience = experience + coinType.getExperience();
            // coin won't increase level.
        }

        updateHologram(); // updates hologram.
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

    public List<WellPlayer> getNearbyPlayers() {
        return this.nearbyPlayers;
    }

    public void addNearbyPlayer(WellPlayer wellPlayer) {
        if (!this.nearbyPlayers.contains(wellPlayer))
            this.nearbyPlayers.add(wellPlayer);
    }

    public void removeNearbyPlayer(WellPlayer wellPlayer) {
        if (this.nearbyPlayers.contains(wellPlayer))
            this.nearbyPlayers.remove(wellPlayer);
    }

    public boolean containsNearbyPlayer(Player player) {
        if (player == null || player.getUniqueId() == null)
            return false;

        return this.nearbyPlayers.stream()
                .map(wellPlayer -> wellPlayer.getPlayerUUID())
                .collect(Collectors.toList())
                .contains(player.getUniqueId());
    }

    public void resetLevel() {
        this.experience = 0;
        this.experienceRequired = 100;
        this.well_level = 0;

        updateHologram();
    }

    public void endAnimation() {
        if (!this.animation.isCancelled())
            this.animation.cancel();
    }

    public TextComponent createHoverableTextComponent() {
        return Component.text(this.getWellName(), TextColor.color(255,170,0))
                .hoverEvent(
                        Component.text("Town: " + this.getTownName()).appendNewline().append(
                        Component.text("Experience: " + this.experience + "/" + this.experienceRequired).appendNewline().append(
                        Component.text("Buff1: " + this.buff1)).appendNewline().append(
                        Component.text("Buff2: " + this.buff2)
                                )));
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
