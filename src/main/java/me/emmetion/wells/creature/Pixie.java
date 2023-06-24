package me.emmetion.wells.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.anim.PixiePunchAnimation;
import me.emmetion.wells.model.Well;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Random;

import static me.emmetion.wells.util.Utilities.getColor;

public class Pixie extends WellCreature implements ParticleMob, Movable, WellBound {

    private Well well;

    private int id;

    // --- Position Information ---
    private int max_pixies;
    private boolean upwards = true;

    private PixieType pixieType = PixieType.NONE;


    public Pixie(Well well, Location location) {
        super(location);

        this.well = well;
        // max_pixies is calculated through the total amount of buffs from a well.
        this.max_pixies = well.getBuffs().size();
        // then we calculate the id of the current pixie, will be used in the message.
        if (this.max_pixies <= well.getActiveBuffs().size()) {
            // the first id is 0, then 1, then 2.
            this.id = well.getBuffs().size();
        }

        this.id = well.getActiveBuffs().size();
        this.setLocation(location);

        //
        Random random = new Random();

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
        if (!(entity instanceof ArmorStand)) // validates that the entity is of armorstand type.
            return null;

        ArmorStand armorStand = (ArmorStand) entity;

        armorStand.setInvisible(true);
        armorStand.setSmall(true);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName("..."); //
        armorStand.setCustomNameVisible(true);
        NamespacedKey nk = new NamespacedKey(Wells.plugin, "creature-uuid");
        armorStand.getPersistentDataContainer().set(nk,
                PersistentDataType.STRING, this.getUUID().toString());

        return armorStand;
    }

    @Override
    public void kill() {
        if (this.getEntity() == null || this.getEntity().isDead())
            return;
        // removes the entity in-game. This will change to be updated with NPC's, should maybe make it overridable.
        this.getEntity().remove();
        Wells.plugin.getCreatureManager().removeCreature(getUUID());
    }

    @Override
    public void handleLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Entity entity = event.getEntity();
        Player player = (Player) event.getDamager();

        handle(entity, player);
        event.setCancelled(true);
    }

    @Override
    public void handleRightClick(PlayerInteractAtEntityEvent event) {

        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        handle(entity, player);
    }

    // This method is used in both right and left click.
    // helper method.
    private void handle(Entity entity, Player player) {
        ArmorStand as = (ArmorStand) entity;

        player.sendMessage(getColor("&a&lPixie Punched! &7(+" + this.pixieType.xp + " well xp)"));

        well.depositXP(this.pixieType.xp);
        well.updateHologram();
        PixiePunchAnimation anim = new PixiePunchAnimation(this, player);
        anim.start();
        kill();
    }

    @Override
    public Class<? extends Entity> entityClassType() {
        return ArmorStand.class;
    }

    @Override
    public void updateCreature() {
        if (pixieType.equals(PixieType.NONE)) {
            calculateRarity();
            updateName();
        }

        if (this.getEntity() == null) {
            return;
        }
        if (this.getEntity().isVisualFire())
            this.getEntity().setVisualFire(false);
    }

    @Override
    public Particle particle() {
        return Particle.REDSTONE;
    }

    // This WellBound particle has the added benefit of using the position of the well to handle where the entity is
    // currently located.
    // Move is called every frame as it is from the Moveable interface, which when called every frame updates the
    // displays the new location and sets the location of the entity.
    @Override
    public void move() {
        World world = getLocation().getWorld();
        world.spawnParticle(particle(), getLocation().clone().add(0,0.3,0), 2, 0.1, 0.1, 0.1,
                pixieType.getDustOptions());

        if (getFrame() % 20 == 0) { // Flip between up and down movements.
            this.upwards = !this.upwards;
        }

        if (getFrame() % 5 == 0) // only moves every 4 frames.
            return;

        if (upwards) {
            setLocation(getLocation().clone().add(0,0.1,0));
        } else {
            setLocation(getLocation().clone().subtract(0,0.1,0));
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
        Random random = new Random();
        // Determines rarity of Pixie.
        int i = random.nextInt(100) + 1;
        if (i > 20) { // Commons.
            this.pixieType = PixieType.COMMON;
        } else if (i > 5) { // Rare.
            this.pixieType = PixieType.RARE;
        } else if (i >= 0) { // Legendary.
            this.pixieType = PixieType.LEGENDARY;
        }
    }

    private void updateName() {
        getEntity().setCustomName(getColor(this.pixieType.getDisplayName()));
    }

    public enum PixieType {
        COMMON("&7Pixie", new Particle.DustOptions(Color.GRAY, 1), 1),
        RARE("&bPixie", new Particle.DustOptions(Color.BLUE, 1), 3),
        LEGENDARY("&6Pixie", new Particle.DustOptions(Color.ORANGE, 1), 10),
        NONE("&f...", new Particle.DustOptions(Color.WHITE, 1), 0);

        private final String display_name;
        private final Particle.DustOptions dust_options;
        private final int xp;


        PixieType(String display_name, Particle.DustOptions dust_option, int xp) {
            this.display_name = display_name;
            this.dust_options = dust_option;
            this.xp = xp;
        }

        public String getDisplayName() {
            return this.display_name;
        }

        public Particle.DustOptions getDustOptions() {
            return dust_options;
        }

        public int getXP() {
            return this.xp;
        }
    }

}



