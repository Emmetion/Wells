package me.emmetion.wells.listeners;

import me.emmetion.wells.Wells;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.events.creature.CreatureClickEvent;
import me.emmetion.wells.events.creature.CreatureSpawnEvent;
import me.emmetion.wells.managers.CreatureManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class WellCreatureListener implements Listener {

    private final CreatureManager creatureManager;

    public WellCreatureListener(CreatureManager manager) {
        this.creatureManager = manager;
    }

    // Uses Spigot events to call custom events. Custom events are then handled by individual
    @EventHandler
    public void leftClickListener(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();

        WellCreature wc = creatureManager.getWellCreatureFromEntity(entity);

        if (wc == null)
            return;

        CreatureClickEvent creatureClickEvent = new CreatureClickEvent(player, wc, CreatureClickEvent.ClickType.LEFT_CLICK);

        Bukkit.getPluginManager().callEvent(creatureClickEvent);
        wc.handleLeftClick(creatureClickEvent);
    }

    @EventHandler
    public void rightClickListener(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        Player player = e.getPlayer();

        // This event fires twice because of the hands that it is interacting with.
        // In our case, we only want one event to fire, so we will check for EqipmentSlot.HAND, thatway it only files this event if the
        // hand is equal to the Main hand slot.

        // player.sendMessage(e.getHand().toString());

        // Prevents duplicate events from firing.
        if (!e.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        WellCreature wc = creatureManager.getWellCreatureFromEntity(entity);
        if (wc == null) {
            return;
        }

        CreatureClickEvent creatureClickEvent = new CreatureClickEvent(player, wc, CreatureClickEvent.ClickType.RIGHT_CLICK);

        // This lets the other developers access CreatureClickEvents, and I can modify values that occur inside the event.
        Bukkit.getPluginManager().callEvent(creatureClickEvent);
        wc.handleRightClick(creatureClickEvent);
    }

    // NPCLeftClickEvent, NPCRightClickEvent are also CitizenAPI events that can be called.

    @EventHandler
    public void handleCreatureSpawn(CreatureSpawnEvent event) {
        WellCreature creature = event.getWellCreature();
        EntityType entityType = creature.creatureEntityType();
        Logger logger = Wells.plugin.getLogger();

        Bukkit.broadcast(Component.text("Spawned WellCreature: " + wellCreatureSimplified(creature)));
    }


    @NotNull
    private String wellCreatureSimplified(@NotNull WellCreature creature) {
        return creature.getCreatureType() + "{loc= " + creature.getLocation().toVector().toString() + "}";
    }

}
