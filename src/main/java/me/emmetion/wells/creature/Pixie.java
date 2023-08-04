package me.emmetion.wells.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.anim.PixiePunchAnimation;
import me.emmetion.wells.events.creature.CreatureClickEvent;
import me.emmetion.wells.model.Well;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

import static me.emmetion.wells.util.Utilities.getColor;

public final class Pixie extends WellCreature implements ParticleMob, Movable, WellBound {

    private final Random random = new Random();

    private Well well;

    // --- Position Information ---
    private boolean upwards = true;

    private PixieType pixieType = PixieType.NONE;


    public Pixie(Well well, Location location) {
        super(location);

        this.well = well;

        // Determines location of Pixie.
        int angle = random.nextInt(360) + 1;

        Location hl = getLocation();

        int radius = 3;

        double x = hl.getX() + radius * Math.cos(angle);
        double y = hl.getY();
        double z = hl.getZ() + radius * Math.sin(angle);

        Location newLoc = new Location(hl.getWorld(), x, y, z);
        setLocation(newLoc);
    }

    @Override
    public CreatureType getCreatureType() {
        return CreatureType.PIXIE;
    }

    @Override
    public Entity handleEntitySpawn(Entity entity) {
        ArmorStand armorStand = (ArmorStand) entity;

        armorStand.setInvisible(true);
        armorStand.setSmall(true);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName("..."); // Temporarily sets name to '...'. This then gets updated to the pixie's name on the next frame.
        armorStand.setCustomNameVisible(true);
        NamespacedKey nk = new NamespacedKey(Wells.plugin, "creature-uuid");
        armorStand.getPersistentDataContainer().set(nk, PersistentDataType.STRING, this.getUUID().toString());

        return armorStand;
    }

    @Override
    public void kill() {
        if (this.getEntity() == null || this.getEntity().isDead())
            return;
        // removes the entity in-game. This will change to be updated with NPC's, should maybe make it overridable.
        this.getEntity().remove();
        // Wells.plugin.getCreatureManager().removeCreature(this);
    }

    @Override
    public void handleLeftClick(CreatureClickEvent event) {
        Entity entity = event.getWellCreature().getEntity();
        Player player = event.getPlayer();

        handle(entity, player);
    }

    @Override
    public void handleRightClick(CreatureClickEvent event) {
        Entity entity = event.getWellCreature().getEntity();
        Player player = event.getPlayer();

        handle(entity, player);
    }

    // Helper method.
    private void handle(Entity entity, Player player) {
        ArmorStand as = (ArmorStand) entity;

        player.sendMessage(getColor("&a&lPixie Punched! &7(+" + this.pixieType.xpDropped + " well xp)"));

        well.depositXP(this.pixieType.xpDropped);
        well.updateHologram();
        PixiePunchAnimation anim = new PixiePunchAnimation(this, player);
        anim.start();
        kill();
    }

    @Override
    public EntityType creatureEntityType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void updateCreature() {
        // This if-statement is called only once.
        if (pixieType.equals(PixieType.NONE)) {
            calculateRarity();
            updateName();
        }

        if (this.getEntity() == null) {
            return;
        }

        // Disables fire effect is present.
        if (this.getEntity().isVisualFire())
            this.getEntity().setVisualFire(false);
    }

    @Override
    public Particle particle() {
        return Particle.REDSTONE;
    }

    @Override
    public void move() {
        World world = getLocation().getWorld();
        // Spawn a particle at the Pixie's location.
        world.spawnParticle(particle(), getLocation().clone().add(0, 0.3, 0), 2, 0.1, 0.1, 0.1, pixieType.getDustOptions());

        // If the pixie has been moving upwards/downwards for 20ticks, it will swap directions.
        if (getFrame() % 20 == 0) { // Flip between up and down movements.
            this.upwards = !this.upwards;
        }

        if (getFrame() % 5 == 0) // only moves every 5 ticks.
            return;

        // Here we handle movement for upwards/downwards cases.
        if (upwards) {
            setLocation(getLocation().clone().add(0, 0.1, 0));
        } else {
            setLocation(getLocation().clone().subtract(0, 0.1, 0));
        }
    }

    @Override
    public Well getBoundWell() {
        return well;
    }

    public PixieType getPixieType() {
        return this.pixieType;
    }

    private void calculateRarity() {
        final Random random = new Random();
        // Determines rarity of Pixie.
        int i = random.nextInt(100) + 1;
        if (i > 20) { // Commons.
            this.pixieType = PixieType.COMMON;
        } else if (i > 5) { // Rare.
            this.pixieType = PixieType.RARE;
        } else if (i >= 0) { // Legendary.
            this.pixieType = PixieType.LEGENDARY;
        } else {
            this.pixieType = PixieType.NONE;
        }
    }

    private void updateName() {
        getEntity().setCustomName(getColor(this.pixieType.getDisplayName()));
    }

    public enum PixieType {
        COMMON("&7Pixie", new Particle.DustOptions(Color.GRAY, 1), 1), RARE("&bPixie", new Particle.DustOptions(Color.BLUE, 1), 3), LEGENDARY("&6Pixie", new Particle.DustOptions(Color.ORANGE, 1), 10), NONE("&fNONE", new Particle.DustOptions(Color.WHITE, 1), 0);

        private final String displayName;
        private final Particle.DustOptions dustOptions;
        private final int xpDropped;

        PixieType(String displayName, Particle.DustOptions dustOptions, int xpDropped) {
            this.displayName = displayName;
            this.dustOptions = dustOptions;
            this.xpDropped = xpDropped;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Particle.DustOptions getDustOptions() {
            return dustOptions;
        }

        public int getXP() {
            return this.xpDropped;
        }
    }

}



