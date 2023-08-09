package me.emmetion.wells.model;

import me.emmetion.wells.util.Utilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CraftableSchematic {

    private final String name;
    private final List<SMaterial> schematicMateiralList;

    public CraftableSchematic(String name, List<SMaterial> materialsRequired) {
        this.name = name;
        this.schematicMateiralList = materialsRequired;
    }

    public List<String> createHologramMaterialList() {
        List<String> itemsRequired = this.schematicMateiralList.stream()
                .map(smaterial -> {
                    ItemStack representativeItemStack = smaterial.getRepresentativeItemStack();
                    if (representativeItemStack == null) {
                        return "&b" + smaterial.getWellXPRequired() + " xp";
                    }
                    return "&b" + smaterial.getRepresentativeItemStack().getItemMeta().displayName();
                }).toList();
        return itemsRequired;
    }



}

class SMaterial {

    private Material vanillaMaterial;
    private String wellsItemId;
    private int amount;
    private int xp;

    private int wells_xp_required;

    private final MaterialType materialType;

    enum MaterialType {
        WellXP,
        VanillaItem,
        WellItem
    }

    // Vanilla Item Constructor
    public SMaterial(Material material, int amount) {
        this.vanillaMaterial = material;
        this.amount = amount;
        this.materialType = MaterialType.VanillaItem;

    }

    public SMaterial(String wells_item_id, int amount) {
        this.wellsItemId = wells_item_id;
        this.amount = amount;

        this.materialType = MaterialType.WellItem;
    }

    public SMaterial(int wells_xp) {
        this.wells_xp_required = wells_xp;
        this.materialType = MaterialType.WellXP;

    }


    public Material getVanillaMaterial() {
        return vanillaMaterial;
    }

    public String getWellsItemId() {
        return wellsItemId;
    }

    public int getAmount() {
        return amount;
    }

    public int getXp() {
        return xp;
    }

    public int getWellXPRequired() {
        return wells_xp_required;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    @Nullable
    public ItemStack getRepresentativeItemStack() {
        switch (materialType) {
            case WellItem -> {
                if (Arrays.stream(CoinType.values()).map(ct -> ct.name().toUpperCase()).toList().contains(wellsItemId)) {
                    ItemStack coin = Utilities.createCoinFromID(wellsItemId);
                    coin.setAmount(amount);
                    return coin;
                } else if (wellsItemId.equals("WELL_BLOCK"))
                return null;
            }
            case WellXP -> {
                return null;
            }
            case VanillaItem -> {
                ItemStack item = new ItemStack(vanillaMaterial);
                item.setAmount(amount);

                return item;
            }
        };

        return null;
    }





}
