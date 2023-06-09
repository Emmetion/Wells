package me.emmetion.wells.model;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public enum CoinType {
    BRONZE_COIN("BRONZE_COIN", 1, TextColor.color(205, 127, 50)),
    SILVER_COIN("SILVER_COIN", 3, TextColor.color(192, 192, 192)),
    GOLD_COIN("GOLD_COIN", 10, TextColor.color(255, 215, 0)),
    LEVEL_UP_COIN("LEVEL_UP_COIN", -1, TextColor.color(1, 255, 0));

    private String wells_id;
    private int experience;
    private TextColor color;

    CoinType(String wells_id, int experience, @NotNull TextColor color) {
        this.wells_id = wells_id;
        this.experience = experience;
        this.color = color;
    }

    public String getWellsId() {
        return this.wells_id;
    }

    public int getExperience() {
        return this.experience;
    }

    public TextColor getColor() {
        return this.color;
    }

    public static CoinType getCoinTypeFromWellsID(String wells_id) {
        for (CoinType ct : CoinType.values()) {
            if (ct.wells_id.equals(wells_id)) {
                return ct;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("CoinType{type=%s, experience=%d}", wells_id, experience);
    }

}
