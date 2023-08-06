package me.emmetion.wells.listeners;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import me.emmetion.wells.Wells;
import me.emmetion.wells.anim.Animation;
import me.emmetion.wells.anim.AnimationSettings;
import me.emmetion.wells.creature.SpawnNPC;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.emmetion.wells.util.Utilities.getColor;

public class SpawnNPCListeners implements Listener {

    private ConcurrentHashMap<UUID, Animation> animationList = new ConcurrentHashMap<UUID, Animation>();
    private HashSet<UUID> confirmBuildUUID = new HashSet<>();

    @EventHandler
    public void onCraftWellItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();

        BlockFace blockFace = event.getBlockFace();

        // check if player is already in craft-animation.
        if (animationList.containsKey(player.getUniqueId())) {
            return;
        }

        player.sendMessage(blockFace + "");
        if (!blockFace.equals(BlockFace.UP) || !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (!clickedBlock.getLocation().equals(SpawnNPC.anvilLocation)) {
            return;
        }

        // Confirm the player wants to craft the selected item.

        HologramPage page = DHAPI.getHologram("SpawnNPCHologram").getPage(player);

        if (page.getIndex() == 0) {
            player.sendMessage("You must be viewing a schematic on the hologram to craft one.");
            return;
            // Return, as they have not selected a Schematic.
        }

        List<HologramLine> lines = page.getLines();

        if (!confirmBuildUUID.contains(player.getUniqueId())) {
            player.sendMessage("Doesn't contain confirmBuildUUID.");
            String craftedItem = lines.get(0).getText();

            player.sendMessage(Component.text(getColor("&eAre you sure you want to follow through with a " + craftedItem + "&e?")));
            player.sendMessage(Component.text(getColor("&7Punch again to confirm.")));

            confirmBuildUUID.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(Wells.plugin, task -> {
                if (confirmBuildUUID.contains(player.getUniqueId())) {
                    player.sendMessage(getColor("&eCancelled."));
                    confirmBuildUUID.remove(player.getUniqueId()); // Removes from confirmation set.
                }
                task.cancel();
            }, 30); // 1.5's to confirm.
            return;
        }

        confirmBuildUUID.remove(player.getUniqueId());


        // Run animation.
        World world = clickedBlock.getWorld();

        ArmorStand armorStand;

        armorStand = (ArmorStand) world.spawnEntity(clickedBlock.getLocation().clone().add(2,1,0), EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.setInvisible(true);
        armorStand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.CAULDRON));
//        armorStand.setRightArmPose(EulerAngle.ZERO);
//        armorStand.setRightArmRotations(Rotations.ZERO);

        Animation animation = new Animation() {

            private int ticks = 0;
            private Location anvilLoc = SpawnNPC.anvilLocation;

            @Override
            public AnimationSettings getAnimationSettings() {
                return new AnimationSettings("CraftItemAnimation", 0, 4);
            }

            @Override
            public void run() {
                // Ticks == 0, then spawn
                world.spawnParticle(Particle.BLOCK_CRACK, anvilLoc.clone().add(0.5, 0.9, 0.5), 20, 0.05, 0.05, 0.05, 0.01, clickedBlock.getBlockData());
                armorStand.teleport(armorStand.getLocation().clone().add(0,0.2,0));

                if (ticks == 4) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    animationList.remove(player.getUniqueId());
                    this.cancel();
                }

                ticks++;
            }
        };

        animationList.put(player.getUniqueId(), animation);
        animation.start();

    }

}
