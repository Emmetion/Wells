package me.emmetion.wells.creature;

import me.emmetion.wells.Wells;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.database.CreatureManager;
import me.emmetion.wells.database.WellManager;
import me.emmetion.wells.events.creature.CreatureClickEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.LookClose;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import static me.emmetion.wells.util.Utilities.getColor;

public class SpawnNPC extends WellCreature {

    private final String npcName = "Wellbi";

    private Marker marker;
    private NPC npc;

    private final Collection<Player> playersChatting = new ArrayList<>();

    private final WellManager wellManager = Wells.plugin.getWellManager();


    public SpawnNPC(Location location) {
        super(location);
    }

    @Override
    public CreatureType getCreatureType() {
        return CreatureType.SPAWN_NPC;
    }
    // When SpawnNPC is called, the handle methohd does not pass in an entity. This is because here we use logic from
    // other languages

    // Called upon entitySpawn.
    // Handles outside events like NPC's getting spawned.
    @Override
    public Entity handleEntitySpawn(Entity entity) {

        // We are passed information about the Marker entity that represents the position of the NPC.

        Marker marker = (Marker) entity;

        Location npcLocation = marker.getLocation();


        Logger logger = Wells.plugin.getLogger();

        // Similar to CreatureManager#isSpawnNPCSpawned.
        npcLocation.getNearbyEntities(1, 1, 1).stream()
                .filter(e -> e.hasMetadata("NPC"))
                .map(e -> CitizensAPI.getNPCRegistry().getNPC(e))
                .filter(npc1 -> npc1.getName().equals(npcName))
                .findFirst()
                .ifPresentOrElse(spawnNPC -> {
                            // Verify that it's actually the SpawnNPC.
                            // Check for SpawnTrait.

                            SpawnTrait trait = spawnNPC.getTraitNullable(SpawnTrait.class);
                            if (trait == null) {
                                // This is not a spawnNPC.

                                logger.info("Found NPC near SpawnNPC location, but it didn't contain a spawnTrait.");
                                return;
                            } else {
                                // SpawnNPC Exists!

                                npc = spawnNPC;
                            }

                        },
                        () -> {
                            // SpawnNPC doesn't exist.
                            logger.info("Failed to find spawnNPC, now we can create our own.");

                            npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName, npcLocation);
                            // Add traits.

                            npc.addTrait(new SpawnTrait());
                            npc.addTrait(new LookClose());

                            Entity npcEntity = npc.getEntity();
                            npcEntity.getPersistentDataContainer().set(Configuration.creatureUUIDKey, PersistentDataType.STRING, this.getUUID().toString());

                        });

        
        return this.marker;
    }

    @Override
    public void kill() {
        if (marker != null && !marker.isDead())
            marker.remove();

        // Effectively removes the marker at the spawnNPC's location, then repeats this process over again when the plugin launches.
    }

    @Override
    public void handleLeftClick(CreatureClickEvent event) {
        Player player = event.getPlayer();

        npc.setAlwaysUseNameHologram(false);

        if (wellManager.wellExistsForPlayer(player)) {
            player.sendMessage(getColor("&eYour town already has a well, try building something else."));
            return;
        }

        if (playersChatting.contains(player)) {
            return;
        }

        executeChat(player);
    }

    @Override
    public void handleRightClick(CreatureClickEvent event) {

        npc.setAlwaysUseNameHologram(false);

        Player clicker = event.getPlayer();
        if (wellManager.wellExistsForPlayer(clicker)) {
            clicker.sendMessage(getColor("&eYour town already has a well, try building something else."));
            return;
        }

        if (playersChatting.contains(clicker)) {
            return;
        }

        executeChat(clicker);
    }


    private void executeChat(Player player) {
        this.playersChatting.add(player);

        Wells pl = Wells.plugin;
        BukkitScheduler scheduler = Bukkit.getScheduler();

        SpawnTrait spawnTrait = npc.getTraitNullable(SpawnTrait.class);
        if (spawnTrait == null)
            return;

        spawnTrait.incrementChatCounter();
        player.sendMessage(getColor("Chats: &c" + spawnTrait.getChatCount()));




        scheduleMessage(player,
                getColor("&e[NPC] Wellbi: &fHi &e" + player.getName() + "&f! I notice your interested in the well that the well I have next to me..."),
                0L);

        scheduleMessage(player,
                getColor("&e[NPC] Wellbi: &fWells are small structures craftable inside your town that can help improve the quality of life in your town!"),
                60L);

        scheduleMessage(player, getColor("&e[NPC] Wellbi: &fTo begin, you first need to build a well in your town. " +
                "Luckily, I can provide you with some help in the building process"),
                120L);


        scheduleMessage(player,
                getColor("&e[NPC] Wellbi: &fCome back to be with the proper supplies and I can lend you one of my &cSchematic&f's!"),
                180L);


        scheduler.runTaskLater(pl, task -> {
            player.sendMessage(Component.text().appendNewline());
            this.playersChatting.remove(player);
            task.cancel();
        }, 180L);
    }

    private void scheduleMessage(@NotNull Player player, @NotNull String message, @NotNull long delay) {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskLater(Wells.plugin, () -> {
            player.sendMessage(Component.text(message).appendNewline().appendNewline());
        }, delay);
    }

    @Override
    public EntityType creatureEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public void updateCreature() {

    }

    public static class SpawnTrait extends Trait {

        @Persist(value = "chats")
        private int chatCounter = 0;

        @Persist(value = "wellsCrafted")
        private int wellsCrafted = 0;

        protected SpawnTrait() {
            super("SpawnTrait");
        }



        public int getChatCount() {
            return chatCounter;
        }

        public void incrementChatCounter() {
            chatCounter++;
        }

        public int getWellsCrafted() {
            return wellsCrafted;
        }

        public void craftWellBlock() {
            wellsCrafted++;
        }

        @EventHandler
        public void onNPCDelete(NPCRemoveEvent event) {
            Entity entity = event.getNPC().getEntity();
            CreatureManager cm = Wells.plugin.getCreatureManager();
            WellCreature wc = cm.getWellCreatureFromEntity(entity);

            if (wc == null)
                return;
            else if (wc instanceof SpawnNPC) {
                Bukkit.broadcast(Component.text("Avoid deleting the spawnNPC, as it can cause problems with the database."));
                cm.removeCreature(wc);
            }
        }

    }

}

