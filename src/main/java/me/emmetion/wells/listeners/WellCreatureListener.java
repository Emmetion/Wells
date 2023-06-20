package me.emmetion.wells.listeners;

import de.tr7zw.nbtapi.NBTEntity;
import me.emmetion.wells.commands.WellCommand;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.database.CreatureManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

public class WellCreatureListener implements Listener {

    private CreatureManager creatureManager;

    public WellCreatureListener(CreatureManager manager) {
        this.creatureManager = manager;
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("PlayerInteractEntityEvent.");

        Entity rightClicked = event.getRightClicked();
        NBTEntity entity = new NBTEntity(rightClicked);

        String creature_uuid = entity.getString("creature_uuid");
        if (creature_uuid == null) {
            return;
        }
        UUID uuid = UUID.fromString(creature_uuid);
        WellCreature wellCreature = creatureManager.getWellCreature(uuid);
        if (wellCreature == null) {
            return;
        }

        player.sendMessage("wellCreature.handleInteraction(event)");
        wellCreature.handleInteraction(event);

    }

}
