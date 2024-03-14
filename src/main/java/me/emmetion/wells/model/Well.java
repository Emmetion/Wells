package me.emmetion.wells.model;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.actions.Action;
import eu.decentsoftware.holograms.api.actions.ActionType;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import me.emmetion.wells.anim.Animation;
import me.emmetion.wells.anim.AnimationSettings;
import me.emmetion.wells.anim.NearWellAnimation;
import me.emmetion.wells.observer.WellListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.*;

import static me.emmetion.wells.util.Utilities.getColor;

public class Well {

    private final List<WellListener> wellListeners = new ArrayList<>();

    private final List<WellPlayer> nearbyPlayers = new ArrayList<>();

    // Will use 'contributions' to create a total donations board.
    // Storable in the MySQL server.
    private final HashMap<UUID, Integer> contributions = new HashMap<>();

    private String townName;
    private Location position;
    private Location hologramLocation;

    private int well_level;
    private int experience;
    private int experienceRequired; // this is calculated upon initialization, and then later used when we deposit coins.

    private ActiveBuff buff1;
    private ActiveBuff buff2;
    private ActiveBuff buff3;

    private boolean isBoosted;
    private Timestamp boost_end;

    // Local animation for well.
    private NearWellAnimation animation;

    public Well(String townName, Location position, Location hologramLocation, int well_level, int experience, String buff1_id, Timestamp buff1_end, String buff2_id, Timestamp buff2_end, String buff3_id, Timestamp buff3_end, boolean isBoosted, Timestamp boost_end) {
        this.townName = townName;
        this.position = position;
        this.hologramLocation = hologramLocation;
        this.well_level = well_level;
        this.experience = experience;
        // Converts saved database BUFF_ID string into real ActiveBuff class.
        if (buff1_id == null || buff1_id.equalsIgnoreCase("NONE")) {
            this.buff1 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff1 = new ActiveBuff(BuffType.valueOf(buff1_id), buff1_end);
        }
        if (buff2_id == null || buff2_id.equalsIgnoreCase("NONE")) {
            this.buff2 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff2 = new ActiveBuff(BuffType.valueOf(buff1_id), buff2_end);
        }
        if (buff3_id == null || buff3_id.equalsIgnoreCase("NONE")) {
            this.buff3 = ActiveBuff.defaultActiveBuff();
        } else {
            this.buff3 = new ActiveBuff(BuffType.valueOf(buff1_id), buff3_end);
        }

        this.isBoosted = isBoosted;
        this.boost_end = boost_end;

        // calculate experience needed for next level.
        this.experienceRequired = 100 + (well_level * 5); // increments experience needed by 5 every level.

        this.hologramLocation = position.clone().add(.5, 2, .5);
        this.updateHologram();

        this.animation = new NearWellAnimation(this);
        this.animation.start();
    }

    public Well(String townName, Location location) {
        this(townName, location, location, 0, 0, "NONE", null, "NONE", null, "NONE", null, false, null);

        this.hologramLocation = location.clone().add(.5, 2, .5);
        this.updateHologram();

        this.animation = new NearWellAnimation(this);
        this.animation.start();


    }

    public void handleAllBuffUpdate() {
        this.getActiveBuffs().stream()
                .filter(ActiveBuff::isNone)
                .forEach(ActiveBuff::update);
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

    /**
     * Returns a copy of the location of the well.
     * This is to prevent the case where we accidentally set the location of the well to a different block.
     * @return Location of Well.
     */
    public Location getLocation() {
        return position.clone();
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
        return hologramLocation;
    }

    public NearWellAnimation getNearWellAnimation() {
        return animation;
    }

//    public void setLocation(Location position) {
//        this.position = position;
//    }

    public boolean hasBuff1() {
        return !buff1.isNone();
    }

    public boolean hasBuff2() {
        return !buff2.isNone();
    }

    public boolean hasBuff3() {
        return !buff3.isNone();
    }

    public boolean isBoosted() {
        return isBoosted;
    }

    public Timestamp getBoostEnd() {
        return boost_end;
    }

    public void setActiveBuff(ActiveBuff activeBuff, int id) {
        if (id < 1 || id > 3) {
            return;
        }
        switch (id) {
            case 1 -> this.buff1 = activeBuff;
            case 2 -> this.buff2 = activeBuff;
            case 3 -> this.buff3 = activeBuff;
        }
        String s = activeBuff.toString();
        Bukkit.broadcast(Component.text(s));
    }

    public Hologram updateHologram() {
        List<String> lines = Arrays.asList(this.getWellName(), ChatColor.YELLOW + "Level: " + this.getWellLevel(), ChatColor.BLUE + "XP: " + this.experience + "/" + this.experienceRequired);

        Hologram hologram;
        if (DHAPI.getHologram(this.getTownName()) == null)
            hologram = DHAPI.createHologram(this.getTownName(), this.getHologramLocation(), false, lines);
        else
            hologram = DHAPI.getHologram(this.getTownName());

        assert hologram != null;

        hologram.setLocation(hologramLocation);
        hologram.realignLines();

        DHAPI.setHologramLine(hologram, 1, ChatColor.YELLOW + "Level: " + this.getWellLevel());
        DHAPI.setHologramLine(hologram, 2, ChatColor.BLUE + "XP: " + this.experience + "/" + this.experienceRequired);


        HologramPage page1 = hologram.getPage(0);
        page1.addAction(ClickType.RIGHT, new Action(ActionType.COMMAND, "/wells well " + this.getTownName() + " info"));



        hologram.setDefaultVisibleState(false);

        return hologram;

    }

    public boolean addHologramLocation(float x, float y, float z) {
        if (this.hologramLocation == null)
            return false;

        Location clone = this.hologramLocation.clone();
        Location subtract = clone.add(x, y, z);

        if (subtract.distance(this.position) > 5) {
            return false;
        } else {
            this.hologramLocation = subtract;
            updateHologram();
            return true;
        }
    }

    public boolean subtractHologramLocation(float x, float y, float z) {
        if (this.hologramLocation == null)
            return false;
        Location clone = this.hologramLocation.clone();
        Location subtract = clone.subtract(x, y, z);
        if (subtract.distance(this.position) > 5) {
            return false;
        } else {
            this.hologramLocation = subtract;
            updateHologram();
            return true;
        }

    }

    public void spawnTemporaryHologramNearby(String text, int ticksAlive) {
        World world = getHologramLocation().getWorld();

        // calculate random location around well.
        Location loc;

        loc = this.getHologramLocation().clone();
        loc = loc.offset(1.5, 0.5, 1.5).toLocation(world);

        ArmorStand armorstand = world.spawn(loc, ArmorStand.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
        armorstand.setInvulnerable(true);
        armorstand.setMarker(true);
        armorstand.setInvisible(true);
        armorstand.setGravity(false);
        armorstand.setCanMove(false);

        // apply invulnerability, invisibility, etc.

        Animation anim = new Animation() {

            private int ticksAlive = 0;

            @Override
            public void run() {


                if (ticksAlive == 40) {
                    armorstand.remove();
                }

                this.ticksAlive++;
            }

            @Override
            public AnimationSettings getAnimationSettings() {
                return new AnimationSettings("TempWellHologram", 0, 1);
            }
        };

        anim.start();
    }

    public void incrementLevel() {
        notifyObservers();
        this.well_level += 1;
    }

    public void decrementLevel() {
        notifyObservers();
        this.well_level -= 1;
    }

    public void depositXP(int xp) {
        if (experience + xp >= this.experienceRequired) {

            incrementLevel();
            this.announceLevelUp();
//            wellPlayer.sendMessage("this.experience = " + ((experience + coinType.getExperience()) % this.experienceRequired));
            this.experience = ((experience + xp) % this.experienceRequired);

            this.experienceRequired = 100 + (well_level * 5);
            // coin will increase level
        } else {
            experience = experience + xp;
            // coin won't increase level.
        }
//        notifyObservers();
    }

    public void depositCoin(@NotNull CoinType coinType) {
        switch (coinType) {
            case LEVEL_UP_COIN -> {
                incrementLevel();
                this.experience = 0;
                this.experienceRequired = 100 + (well_level * 5);
                updateHologram();
            }
            case TIME_BONUS -> getBuffs().forEach(ActiveBuff::addTwentySeconds);
            case GOLD_COIN, SILVER_COIN, BRONZE_COIN -> {
                this.animation.enqueueDepositedCoinType(coinType); // adds coindrop information to nearwellanimation.
                depositXP(coinType.getExperience());
                updateHologram();
            }
        }
    }

    /**
     * Returns -1 if no slot is available.
     * @return
     */
    public int getAvailableSlot() {
        if (this.buff1.isNone()) {
            return 1;
        } else if (this.buff2.isNone()) {
            return 2;
        } else if (this.buff3.isNone()) {
            return 3;
        } else {
            return -1;
        }
    }

    public int getWellLevel() {
        return this.well_level;
    }

    public void attachObserver(WellListener attached) {
        wellListeners.add(attached);
    }

    public void detachObserver(WellListener detach) {
        wellListeners.remove(detach);
    }

    public void notifyObservers() {
        for (WellListener wellListener : wellListeners) {
            wellListener.update();
        }
    }

    /**
     * Returns a list of ActiveBuffs that are not None.
     *
     * @return
     */
    public List<ActiveBuff> getActiveBuffs() {
        List<ActiveBuff> activeBuffs = Arrays.asList(buff1, buff2, buff3);
        ArrayList<ActiveBuff> realBuff = new ArrayList<>();
        for (ActiveBuff ab : activeBuffs) {
            if (!ab.getBuffID().equalsIgnoreCase("none") && !ab.hasEnded())
                realBuff.add(ab);
        }

        return realBuff;
    }

    /**
     * Get buffs regardless of being none or ended duration.
     *
     * @return
     */
    public List<ActiveBuff> getBuffs() {
        return Arrays.asList(buff1, buff2, buff3);
    }

    private void performStateChange() {
        notifyObservers();
    }

    public String prettyPosition() {
        return this.getLocation().toVector().toString();
    }

    @NotNull
    public List<WellPlayer> getNearbyPlayers() {
        return this.nearbyPlayers;
    }

    public void addNearbyPlayer(@NotNull WellPlayer wellPlayer) {
        if (!this.nearbyPlayers.contains(wellPlayer))
            this.nearbyPlayers.add(wellPlayer);
    }

    public void removeNearbyPlayer(@NotNull WellPlayer wellPlayer) {
        this.nearbyPlayers.remove(wellPlayer);
    }

    public boolean containsNearbyPlayer(@NotNull Player player) {
        return this.nearbyPlayers.stream()
                .map(WellPlayer::getPlayerUUID)
                .toList()
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
        return Component.text(this.getWellName(), TextColor.color(255, 170, 0)).hoverEvent(Component.text("Town: " + this.getTownName()).appendNewline().append(Component.text("Experience: " + this.experience + "/" + this.experienceRequired).appendNewline().append(Component.text("Buff1: " + this.buff1)).appendNewline().append(Component.text("Buff2: " + this.buff2))));
    }


    /**
     * Creates a level bar for a well.
     * This bar represents the amount of 'energy' a well has.
     * 'Energy' is used as a strength multiplier for ActiveBuffs.
     *
     * @return
     */
    public String createLevelBar() {
        StringBuilder builder = new StringBuilder();

        // Level
        int amountOfBars = 10;

        builder.append(getColor("&f["));
        for (int i = 0; i < amountOfBars; i++) {
            if (i < well_level)
                builder.append(getColor("&b|"));
            else
                builder.append(getColor("&7|"));
        }
        builder.append(getColor("&f]"));

        return builder.toString();
    }

    private void announceLevelUp() {
        Town town = TownyAPI.getInstance().getTown(townName);
        for (Resident r : town.getResidents()) {
            if (r.isOnline())
                r.sendMessage(com.palmergames.adventure.text.Component.text(getColor("&aYour well has leveled up! ")).append(com.palmergames.adventure.text.Component.text(getColor("New Level: " + this.well_level))));
        }

    }

    // IntelliJ default equals() and hashCode().
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Well well = (Well) o;

        if (!townName.equals(well.townName))
            return false;
        return position.equals(well.position);
    }

    @Override
    public int hashCode() {
        int result = townName.hashCode();
        result = 31 * result + position.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Well{" + "observers=" + wellListeners + ", townName='" + townName + '\'' + ", position=" + position + ", hologramPosition=" + hologramLocation + ", well_level=" + well_level + ", experience=" + experience + ", buff1=" + buff1 + ", buff2=" + buff2 + ", buff3=" + buff3 + '}';
    }
}
