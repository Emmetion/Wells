package me.emmetion.wells.creature;

import de.tr7zw.nbtapi.NBTEntity;
import me.emmetion.wells.Wells;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.model.Well;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

import static me.emmetion.wells.util.Utilities.getColor;

public class Pixie extends WellCreature implements ParticleMob, Movable, WellBound {

    private Well well;

    private int id;

    // --- Position Information ---
    private int ticksPlayed = 0;
    private int max_pixies;
    private final double radius = 3.0f;

    private final double angle_from_id;
    private Location center;
    private float angle = 0.0f;

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

        this.angle_from_id = (Math.PI / 3) * (id + 1); // 1/3 * Math.PI, 2/3 * Math.PI, 1 * Math.PI
        this.id = well.getActiveBuffs().size();
        this.setLocation(location);

        // was 'calculatePixieType();'
        Random random = new Random();

        int i = random.nextInt(100) + 1;
        if (i > 20) { // Commons.
            this.pixieType = PixieType.COMMON;
        } else if (i > 5) { // Rare.
            this.pixieType = PixieType.RARE;
        } else if (i > 0) { // Legendary.
            this.pixieType = PixieType.LEGENDARY;
        } else { // edge cases are common;
            System.out.println("Pixie edge case: i=" + i);
            this.pixieType = PixieType.COMMON;
        }
        System.out.println("Pixie");
    }

    // This is essentially additional information you can apply to an entity after it's been spawned in.
    // I can think of lot sof use cases for this.
    @Override
    public void handleEntitySpawn(Entity entity) {
        if (!(entity instanceof ArmorStand)) // validates that the entity is of armorstand type.
            return;

        ArmorStand armorStand = (ArmorStand) entity;

        armorStand.setInvisible(true);
        if (pixieType == null)
            pixieType = PixieType.COMMON;
        armorStand.setCustomName(getColor(pixieType.getDisplayName())); //
        armorStand.setCustomNameVisible(true);
        NBTEntity nbtEntity = new NBTEntity(armorStand);
        nbtEntity.setString("creature_uuid", this.getUUID().toString());
        nbtEntity.setString("creature_type", "PIXIE");
        nbtEntity.setObject("pixie_type", pixieType);
    }

    /**
     * Because PlayerEvent is a generic event of all PlayerInteract events, I'm using PlayerEvent as a method of
     * handling specific events.
     *
     * @param event
     */
    @Override
    public void handleInteraction(PlayerEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Interaction Event!.");

        if (event instanceof PlayerInteractEntityEvent) {
            handle((PlayerInteractEntityEvent) event);
        }
    }

    // This method is called above in #handleInteraction.
    private void handle(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ArmorStand as = (ArmorStand) entity;
        NBTEntity nbtEntity = new NBTEntity(as);


        CreatureType creature_type = nbtEntity.getObject("creature_type", CreatureType.class);
        String uuidString = nbtEntity.getString("creature_uuid");
        UUID uuid = UUID.fromString(uuidString);

        if (creature_type == null) {
            return;
        }

        if (creature_type != CreatureType.PIXIE) {
            return;
        }

        PixieType type = PixieType.valueOf(
                nbtEntity.getString("pixie_type")
        );

        Vector vector = event.getRightClicked().getLocation().toVector();
        player.sendMessage("Pixie Punched: " + vector.toString() + " id: " + this.id + "pixie_type: " + type.toString().toUpperCase());

        CreatureManager cm = Wells.plugin.getCreatureManager();
        WellCreature wc = cm.getWellCreature(getUUID());
        if (wc != null) {
            event.setCancelled(true);
            player.sendMessage("Killing WellCreaure: " + wc.getCreatureType() + " frame: " + wc.getFrame());
            wc.kill();
        }
    }

    @Override
    public Class<? extends Entity> entityClassType() {
        return ArmorStand.class;
    }

    @Override
    public void updateCreature() {

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
        center = well.getHologramLocation().clone().subtract(0,1,0);

        World world = center.getWorld();

        double x1 = center.getX() + radius * Math.cos(angle + angle_from_id);
        double y1 = center.getY(); // (y_radius * Math.sin(angle));
        double z1 = center.getZ() + radius * Math.sin(angle + angle_from_id);
        for (int i = 0; i < 2; i++) {
            Location loc = new Location(world, x1, y1, z1).toLocation(world);
            world.spawnParticle(particle(), loc, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.PURPLE, 0.8f), false);
        }
        Location loc = new Location(world, x1, y1, z1).toLocation(world);

        angle += 0.1;
        setLocation(loc);
        this.teleportEntityTo(loc.clone().subtract(0,1.6,0));

    }

    @Override
    public Well getBoundWell() {
        return well;
    }

    enum PixieType {
        COMMON("&fCommon Pixie"),
        RARE("&bRare Pixie"),
        LEGENDARY("&6Legendary Pixie"),
        NONE("None.");

        private final String display_name;

        PixieType(String display_name) {
            this.display_name = display_name;
        }

        public String getDisplayName() {
            return this.display_name;
        }


    }

}



