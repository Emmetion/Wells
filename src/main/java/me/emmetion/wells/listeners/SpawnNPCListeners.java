package me.emmetion.wells.listeners;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.event.HologramClickEvent;
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

    // This listener is built only for SpawnNPC actions, like crafting items at the schematic table or cycling through build options.

    private final ConcurrentHashMap<UUID, Animation> animationList = new ConcurrentHashMap<UUID, Animation>();
    private final HashSet<UUID> confirmBuildUUID = new HashSet<>();
    private final HashSet<UUID> craftingCooldown = new HashSet<>();

    @EventHandler
    public void handleItemDisplay(HologramClickEvent event) {
        Player player = event.getPlayer();
        HologramPage page = event.getHologram().getPage(player);
        page.getLine(0);
    }

    @EventHandler
    public void onCraftWellItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();

        BlockFace blockFace = event.getBlockFace();

        // check if player is already in craft-animation.
        if (animationList.containsKey(player.getUniqueId())) {
            return;
        }

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

        Hologram spawnNPCHologram = DHAPI.getHologram("SpawnNPCHologram");
        HologramPage page = spawnNPCHologram.getPage(player);

        Hologram confirmationHologram = DHAPI.getHologram("SpawnNPCConfirmation");

        if (page == null) {
            System.out.println("Currently on null page. That's not good.");
            return;
        }

        if (page.getIndex() == 0) {
            player.sendMessage("You must be viewing a schematic on the hologram to craft one.");
            return;
            // Return, as they have not selected a Schematic.
        }

        List<HologramLine> lines = page.getLines();

        if (!confirmBuildUUID.contains(player.getUniqueId())) {
            String craftedItem = lines.get(0).getText();

            player.sendMessage(Component.text(getColor("&eYou're sure you want to craft a " + craftedItem + "&e?")));
            player.sendMessage(Component.text(getColor("&7Punch again to confirm.")));

            confirmBuildUUID.add(player.getUniqueId());
            confirmationHologram.show(player, 1);

            // Remove UUID from cooldown list.
            Bukkit.getScheduler().runTaskLater(Wells.plugin, task -> {
                if (confirmBuildUUID.contains(player.getUniqueId())) {
                    confirmationHologram.show(player, 0);
                    player.sendMessage(getColor("&cCancelled."));
                    confirmBuildUUID.remove(player.getUniqueId()); // Removes from confirmation set.
                }
                task.cancel();
            }, 60); // 1.5's to confirm.
            return;
        }

        confirmBuildUUID.remove(player.getUniqueId());


        // Run animation.
        World world = clickedBlock.getWorld();

        ArmorStand armorStand;

        armorStand = (ArmorStand) world.spawnEntity(clickedBlock.getLocation().clone().add(0.5,0.1,0.5), EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.setInvisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setSmall(true);
        armorStand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.CRAFTING_TABLE));
//        armorStand.setRightArmPose(EulerAngle.ZERO);
//        armorStand.setRightArmRotations(Rotations.ZERO);

        Animation animation = new Animation() {

            private int ticks = 0;
            private final Location anvilLoc = SpawnNPC.anvilLocation;

            @Override
            public AnimationSettings getAnimationSettings() {
                return new AnimationSettings("CraftItemAnimation", 0, 0);
            }

            @Override
            public void run() {
                // Ticks == 0, then spawn
                world.spawnParticle(Particle.BLOCK_CRACK, anvilLoc.clone().add(0.5, 0.9, 0.5), 5, 0.05, 0.05, 0.05, 0.01, clickedBlock.getBlockData());

                armorStand.teleport(armorStand.getLocation().clone().add(0,0.05,0));
                armorStand.setArrowsInBody(ticks);
                float raw = (float) (ticks > 4 ? ticks: .3 * ticks) * 30;
                player.sendMessage("raw: " + raw);
                armorStand.setRotation(raw, 0);

                // Sounds every tick.
                if (ticks < 16) {
                    float pitch = (ticks * (0.07f) + .5f);
                    player.sendMessage(Component.text("pitch: " + pitch));
                    player.playSound(armorStand.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, pitch);
                }

                if (ticks == 16) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    if (!armorStand.isDead())
                        armorStand.remove();
                    animationList.remove(player.getUniqueId());

                    craftingCooldown.add(player.getUniqueId());

                    Bukkit.getScheduler().runTaskLater(Wells.plugin, (task) -> {
                        craftingCooldown.remove(player.getUniqueId());

                        player.sendMessage(getColor("&bYou can craft again!"));
                    }, 60); // 3second cooldown between crafting items.


                    this.cancel();


                }

                ticks++;
            }
        };

        animationList.put(player.getUniqueId(), animation);
        animation.start();

    }

}
