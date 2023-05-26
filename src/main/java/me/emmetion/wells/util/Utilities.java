package me.emmetion.wells.util;

import de.tr7zw.nbtapi.NBTBlock;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Utilities {

    /**
     * Returns a collection of blocks (with their locations) from a given position.
     * It counts in a radius 1x3x1 underneath itself.
     * This means 27 water blocks could be placed in a well location.
     * Right now, it's implemented to check for 5 water blocks.
     *
     * @param location
     * @return
     */
    public static Collection<Block> getBlocksUnderneathLocation(Location location) {
        ArrayList<Block> sphere = new ArrayList<>();
        World world = location.getWorld();

        // #_0 means original.
        int x_o = location.getBlockX();
        int y_o = location.getBlockY();
        int z_o = location.getBlockZ();

        for (int x = x_o - 1; x <= x_o + 1; x++) {
            for (int y = y_o - 1; y >= y_o - 3; y--) {
                for (int z = z_o - 1; z <= z_o + 1; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, block.getLocation().add(.5, .5, .5), 3, 0, 0, 0,0, null, true);
                    if (block.getType() != Material.AIR && !block.equals(location.getBlock())) {
                        // Use the block below the cauldron as needed
                        sphere.add(block);
                        System.out.println("Block below cauldron: " + block.getType());
                    }
                }
            }
        }

        // DEBUG
        Player emmetion = Bukkit.getPlayer("Emmetion");
        if (emmetion != null) {
            emmetion.sendMessage(Component.text("Sphere block size " + sphere.size() + " from radius 1x3x1."));
        }

        return sphere;
    }

    /**
     *  Set model data number on an itemstack
     *
     * @param itemStack
     * @param modelID
     * @return replacement itemstack.
     */
    public static ItemStack setModelData(ItemStack itemStack, int modelID) {
        ItemMeta itemmeta = itemStack.getItemMeta();
        itemmeta.setCustomModelData(modelID);
        itemStack.setItemMeta(itemmeta);

        return itemStack;
    }


    /**
     * Helper method to determine whether an item in hand is a well block.
     *
     * @param wellBlock
     * @return
     */
    public static boolean isWellBlockItem(ItemStack wellBlock) {
        if (wellBlock == null) {
            return false;
        }

        NBTItem item = new NBTItem(wellBlock);
        String wellsId = item.getString("wells_id");

        if (wellsId == null) {
            return false;
        }

        if (wellsId.equals("WELL_BLOCK")) {
            return true;
        }

        return false;
    }

    /**
     * Helper method to determine whether a block placed in the world is a well-block.
     *
     * @param block
     * @return
     */
    public static boolean isWellBlock(Block block) {
        if (block == null) {
            return false;
        }

        NBTBlock nbt_block = new NBTBlock(block);
        NBTCompound data = nbt_block.getData();
        if (data.hasKey("wells_id"))
            return false;

        String wells_id = data.getString("wells_id");
        if (wells_id.equals("WELL_BLOCK")) {
            return true;
        }

        return false;
    }

    /**
     * Helper method to create WELL_BLOCK items.
     *
     * @param count - stack size
     * @return
     */

    public static ItemStack createWellBlockItem(int count) {
        if (count < 1 && count > 64) {
            System.out.println("Attempted to create Well Block with invalid stack count.");
            return null;
        }
        ItemStack well = new ItemStack(Material.CAULDRON);
        well.setAmount(count);
        ItemMeta itemmeta = well.getItemMeta();
        itemmeta.displayName(Component.text(ChatColor.DARK_GREEN + "Well Block"));
        itemmeta.lore(Arrays.asList(
                Component.text(ChatColor.GRAY + "Place this well block above" + ChatColor.YELLOW + " 5 " + ChatColor.AQUA + "water blocks" + ChatColor.GRAY + ".")
        ));

        well.setItemMeta(itemmeta);

        NBTItem nbtItem = new NBTItem(well);
        nbtItem.setString("wells_id", "WELL_BLOCK");
        ItemStack item = nbtItem.getItem();

        return item;
    }

}
