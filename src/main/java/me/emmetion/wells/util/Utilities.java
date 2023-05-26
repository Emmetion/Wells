package me.emmetion.wells.util;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Utilities {

    public static Collection<Block> getBlocksAround(Location location, int radius) {
        ArrayList<Block> sphere = new ArrayList<>();
        for (int y = -radius; y < radius; y++) {
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if (Math.sqrt((x*x) + (y*y) + (z*z)) <= radius) {
                        final Block b = location.getWorld().getBlockAt(x + location.getBlockX(), y + location.getBlockY(), z + location.getBlockZ());
                        sphere.add(b);
                    }
                }
            }
        }


        System.out.println("Sphere block size " + sphere.size() + " from radius " + radius + ".");

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


    public static boolean isWellBlock(ItemStack wellBlock) {
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

    public static ItemStack createWellBlockItem(int count) {
        if (count < 1 && count > 64) {
            System.out.println("Attempted to create Well Block with invalid stack count.");
            return null;
        }
        ItemStack well = new ItemStack(Material.BARREL);
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
