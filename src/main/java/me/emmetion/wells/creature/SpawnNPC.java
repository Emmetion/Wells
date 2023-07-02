package me.emmetion.wells.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.database.WellManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.*;
import net.citizensnpcs.trait.text.Text;
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
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static me.emmetion.wells.util.Utilities.getColor;

public class SpawnNPC extends WellCreature {

    private NPC npc;
    private WellManager wellManager = Wells.plugin.getWellManager();

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
        entity.getPersistentDataContainer().set(new NamespacedKey(Wells.plugin, "creature-uuid"), PersistentDataType.STRING, getUUID().toString());
        NPC tora = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Tora");
        tora.setProtected(true);
        tora.addTrait(new SpawnTrait("Spawn Trait", getUUID()));
        tora.addTrait(new LookClose());
        tora.addTrait(new ScriptTrait().);
        SkinTrait st = new SkinTrait();
        st.setTexture("", "");
        tora.addTrait(st);
        tora.spawn(getLocation());

        return tora.getEntity();
    }

    @Override
    public void kill() {
        // despawn the NPC.
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(this.getEntity());
        npc.destroy();
    }

    private Collection<Player> playersChatting = new ArrayList<>();

    private int chatCounter = 0;

    @Override
    public void handleLeftClick(EntityDamageByEntityEvent event) {
        Player player = (Player) event.getDamager();

        npc.setAlwaysUseNameHologram(false);

        if (wellManager.wellExistsForPlayer(player)) {
            player.sendMessage(getColor("&eYour town already has a well! You don't need to build one"));
            return;
        }

        if (playersChatting.contains(player)) {
            return;
        }

        executeChat(player);

        chatCounter++;
    }

    @Override
    public void handleRightClick(PlayerInteractAtEntityEvent event) {
        // Use Citizens handling instead.
    }


    private void executeChat(Player player) {
        this.playersChatting.add(player);

        Wells pl = Wells.plugin;
        BukkitScheduler scheduler = Bukkit.getScheduler();

        player.sendMessage(getColor("&e[NPC] Wellbi: &fHi &e" + player.getName() + "&f! I notice your interested in the well that " + "I have next to me this is a hard wrap" + "the well I have next to me..."));

        scheduler.runTaskLater(pl, () -> {
            player.sendMessage(getColor("&e[NPC] Wellbi: &fWells are small structures craftable inside your town that " + "can help improve the quality of life in your town!"));
        }, 60l);

        scheduler.runTaskLater(pl, () -> {
            player.sendMessage(getColor("&e[NPC] Wellbi: &fTo begin, you first need to build a well in your town. " + "Luckily, I can provide you with &cschematics &fto help the building process!"));
        }, 120l);

        scheduler.runTaskLater(pl, () -> {
            player.sendMessage(getColor("&e[NPC] Wellbi: &fCome back to be with the proper supplies and I can craft you a &cSchematic&f!"));
            this.playersChatting.remove(player);
        }, 180l);


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


    @Persist("chats")
    int chatCounter = 0;

    @Persist("creature-uuid")
    String creature_uuid;

    protected SpawnTrait(String name, UUID creature_uuid) {
        super(name);
        this.creature_uuid = creature_uuid.toString();
    }

    // Inserts creature-uuid onto NPC.
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

    @EventHandler
    public void handleNPCDelete(NPCRemoveEvent event) {
        Entity entity = event.getNPC().getEntity();
        CreatureManager cm = Wells.plugin.getCreatureManager();
        WellCreature wc = cm.getWellCreatureFromEntity(entity);

        if (wc == null)
            return;
        else if (wc instanceof SpawnNPC) {
            Bukkit.broadcast(Component.text("NPCRemoveEvent called upon SpawnNPC."));
            cm.removeCreature(wc);
        }
    }



}