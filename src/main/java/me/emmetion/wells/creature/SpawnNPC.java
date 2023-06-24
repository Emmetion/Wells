package me.emmetion.wells.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.database.CreatureManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.trait.versioned.GoatTrait;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class SpawnNPC extends WellCreature {

    private NPC npc;

    public SpawnNPC(UUID uuid, Location location) {
        super(uuid, location);
    }

    public SpawnNPC(Location location) {
        super(location);
    }

    @Override
    public CreatureType getCreatureType() {
        return CreatureType.SPAWN_NPC;
    }

    // When SpawnNPC is called, the handle methohd does not pass in an entity. This is because here we use logic from
    // other languages
    @Override
    public Entity handleEntitySpawn(Entity entity) {
        // Initialized marker.
        entity.getPersistentDataContainer().set(new NamespacedKey(Wells.plugin, "creature-uuid"),
                PersistentDataType.STRING, getUUID().toString());
        NPC tora = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Tora");
        tora.setProtected(true);
        tora.addTrait(new SpawnTrait("Spawn Trait", getUUID()));
        tora.spawn(getLocation());

        return tora.getEntity();
    }

    @Override
    public void kill() {
        // despawn the NPC.
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(this.getEntity());
        npc.destroy();
    }

    @Override
    public void handleLeftClick(EntityDamageByEntityEvent event) {
        // Use Citizens handling instead.
    }

    @Override
    public void handleRightClick(PlayerInteractAtEntityEvent event) {
        // Use Citizens handling instead.
    }

    @Override
    public Class<? extends Entity> entityClassType() {
        return Marker.class;
    }

    @Override
    public void updateCreature() {

    }
}

class SpawnTrait extends Trait {

    private final CreatureManager creatureManager;

    protected SpawnTrait(String name, UUID creature_uuid) {
        super(name);
        this.creature_uuid = creature_uuid.toString();
        creatureManager = Wells.plugin.getCreatureManager();
    }

    @Override
    public void onSpawn() {
        // Attaches creature-uuid on NPC entity

        if (!getNPC().getEntity().getPersistentDataContainer().has(new NamespacedKey(Wells.plugin, "creature-uuid"))) {
            // Bukkit.broadcast(Component.text("doesnt have uuid."));
            getNPC().getEntity().getPersistentDataContainer().set(new NamespacedKey(Wells.plugin, "creature-uuid"), PersistentDataType.STRING, creature_uuid);
            // Bukkit.broadcast(Component.text("set uuid."));

        }
        // Bukkit.broadcast(Component.text("had uuid on attach."));
    }

    @Persist("chats")
    int chatCounter = 0;

    @Persist("creature-uuid")
    String creature_uuid;

    @EventHandler
    public void rightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();

        chatCounter++;

        player.sendMessage("Chats: " + chatCounter);
        // player.sendMessage("UUID: " + creature_uuid);

    }

    @EventHandler
    public void leftClick(NPCLeftClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();

        chatCounter--;

        player.sendMessage("Chats: " + chatCounter);
        // player.sendMessage("UUID: " + creature_uuid);
    }


}