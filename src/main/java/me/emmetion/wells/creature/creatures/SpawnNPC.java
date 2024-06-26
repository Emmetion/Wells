package me.emmetion.wells.creature.creatures;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.actions.Action;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import me.emmetion.wells.Wells;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.creature.CreatureType;
import me.emmetion.wells.creature.WellCreature;
import me.emmetion.wells.events.creature.CreatureClickEvent;
import me.emmetion.wells.managers.WellManager;
import me.emmetion.wells.menu.PlayerMenuUtility;
import me.emmetion.wells.menu.menus.SpawnNPCMenu;
import me.emmetion.wells.model.WellPlayer;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static me.emmetion.wells.util.Utilities.getColor;
import static me.emmetion.wells.util.Utilities.getComponentColor;

public final class SpawnNPC extends WellCreature {

    public static final Location anvilLocation = new Location(Bukkit.getWorld("world"), 144, 68, -136);

    private final String npcName = "Wellbi";

    private final Collection<Player> playersChatting = new ArrayList<>();
    private final WellManager wellManager = Wells.plugin.getWellManager();

    private Marker marker; // Invisible entity that represents the location of the NPC.
    private NPC npc; // The Citizens NPC entity.
    private Hologram nearbyHologram; // The hologram that displays information about the NPC.
    private Hologram confirmationHologram; //


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
        Logger logger = Wells.plugin.getLogger();

        // We are passed information about the Marker entity that represents the position of the NPC.
        Marker marker = (Marker) entity;
        this.marker = marker;

        Location npcLocation = marker.getLocation();


        // Similar to CreatureManager#isSpawnNPCSpawned.
        npcLocation.getNearbyEntities(1, 1, 1).stream().filter(e -> e.hasMetadata("NPC")).map(e -> CitizensAPI.getNPCRegistry().getNPC(e)).filter(npc1 -> npc1.getName().equals(npcName)).forEach(spawnNPC -> {
            // Verify that it's actually the SpawnNPC.
            // Check for SpawnTrait.

            Bukkit.broadcast(Component.text("Deleted SpawnNPC, preparing to spawn a new one."));
            spawnNPC.destroy();
        });

        npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName, npcLocation);
        // Add traits.

        npc.addTrait(new LookClose());
        npc.spawn(npcLocation);
        Entity npcEntity = npc.getEntity();
        npcEntity.getPersistentDataContainer().set(Configuration.creatureUUIDKey, PersistentDataType.STRING, this.getUUID().toString());


        // Here we will also initialize the hologram information.
        // The hologram will help guide the NPC into knowing which building they want to build.


        Location loc = npc.getEntity().getLocation().clone().add(1.3, 3, 3);

        nearbyHologram = DHAPI.createHologram("SpawnNPCHologram", loc, false);

        HologramPage page1 = nearbyHologram.getPage(0);
        DHAPI.setHologramLines(nearbyHologram, Arrays.asList("&cSchematic Station", "", "&fHere you can build &cschematics &7to", "&7place inside your town claim!"));

        // page1.addAction(ClickType.RIGHT, new Action("PREV_PAGE"));
        page1.addAction(ClickType.LEFT, new Action("NEXT_PAGE"));

        HologramPage page2 = DHAPI.addHologramPage(nearbyHologram, Arrays.asList(getColor("&bWell Schematic"), "", getColor("Materials Required:"), getColor(" &7- &c1 Reinforced Cauldron"), getColor(" &7- &c124 Oak Logs")));

        page2.addAction(ClickType.LEFT, new Action("NEXT_PAGE"));
        page2.addAction(ClickType.RIGHT, new Action("PREV_PAGE"));


        HologramPage page3 = DHAPI.addHologramPage(nearbyHologram,
                Arrays.asList(
                        getColor("&fReinforced Cauldron"),
                        "",
                        getColor("&7Build Cost:"),
                        getColor(" &7- &f64 Iron Ingots")
                )
        );
        
        page3.addAction(ClickType.RIGHT, new Action("PREV_PAGE"));
//        page2.addAction(ClickType.LEFT, new Action("NEXT_PAGE"));
//         The nearby hologram should be able to alternate between pages.

        nearbyHologram.realignLines();
        nearbyHologram.setLocation(loc);
        nearbyHologram.showAll();
        nearbyHologram.updateAll();


        // Confirmation hologram setup./

        Location confirmationLoc = anvilLocation.clone().add(0.5, 1.2, 0.5);

        confirmationHologram = DHAPI.createHologram("SpawnNPCConfirmation", confirmationLoc, false, List.of(getColor(
                "&eBuild?")));
        confirmationHologram.addPage().setLine(0, "&eClick again to confirm.");
        confirmationHologram.setDefaultVisibleState(true);


        return npc.getEntity();
    }


//        HELPFUL DEBUG CODE.
//        Map<ClickType, List<Action>> actions = page1.getActions();
//        for (Map.Entry<ClickType, List<Action>> entry : actions.entrySet()) {
//
//            player.sendMessage("ClickType: " + entry.getKey());
//            for (Action action : entry.getValue()) {
//                player.sendMessage("ActionType: " + action.getType().getName() + " ActionData: " + action.getData());
//            }
//        }
//        boolean b = page1.hasActions();
//        if (b)
//            Bukkit.broadcastMessage(getColor("page 1 has actions!"));


    private String hasEnoughItemsString(@NotNull Player player, @NotNull ItemStack itemStack, int amount) {
        PlayerInventory inventory = player.getInventory();
        itemStack.setAmount(amount);

        if (inventory.contains(itemStack)) {
            return "&a&l✔";
        } else {
            return "&c&l✖";
        }
    }

    @Override
    public void kill() {
        if (marker != null && !marker.isDead())
            marker.remove();
        npc.destroy();

        nearbyHologram.destroy();
        confirmationHologram.destroy();
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
            player.sendActionBar(getComponentColor("&cYou are already chatting with this npc."));
            return;
        }

        executeChat(player);
    }

    @Override
    public void handleRightClick(CreatureClickEvent event) {

        npc.setAlwaysUseNameHologram(false);

        Player clicker = event.getPlayer();
//        if (wellManager.wellExistsForPlayer(clicker)) {
//            clicker.sendMessage(getColor("&eYour town already has a well, try building something else."));
//            return;
//        }

        if (playersChatting.contains(clicker)) {
            clicker.sendActionBar(getComponentColor("&cYou are already chatting with this npc."));
            return;
        }


        WellPlayer wellPlayer = wellManager.getWellPlayer(clicker);
        if (wellPlayer == null) {
            Logger logger = Wells.plugin.getLogger();
            logger.info("WellPlayer should not be null here. (SpawnNPC:215)");
        }
        SpawnNPCMenu spawnNPCMenu = new SpawnNPCMenu(Wells.plugin, new PlayerMenuUtility(clicker, wellPlayer), this);
        spawnNPCMenu.open();
//        executeChat(clicker);
    }

    private void executeChat(Player player) {
        this.playersChatting.add(player);

        Wells pl = Wells.plugin;
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduleMessage(player, getColor("&e[NPC] Wellbi: &fHi &e" + player.getName() + "&f! I notice your interested in the well that the well I have next to me..."), 0L);

        scheduleMessage(player, getColor("&e[NPC] Wellbi: &fWells are small structures craftable inside your town that can help improve the quality of life in your town!"), 60L);

        scheduleMessage(player, getColor("&e[NPC] Wellbi: &fTo begin, you first need to build a well in your town. " + "Luckily, I can provide you with some help in the building process"), 120L);

        scheduleMessage(player, getColor("&e[NPC] Wellbi: &fCome back to be with the proper supplies and I can lend you one of my &cSchematic&f's!"), 180L);


        scheduler.runTaskLater(pl, task -> {
            player.sendMessage(Component.text().appendNewline());
            this.playersChatting.remove(player);
            task.cancel();
        }, 180L);
    }

    private void scheduleMessage(@NotNull Player player, @NotNull String message, long delay) {
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

}

