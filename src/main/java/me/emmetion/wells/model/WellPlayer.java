package me.emmetion.wells.model;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WellPlayer {


    private UUID playerUUID;

    private int bronzeCoins;
    private int silverCoins;
    private int goldCoins;

    private int coinsDeposited;

    private int experiencePoints;

    public WellPlayer(UUID uuid) {
        this(uuid, 0, 0, 0, 0, 0);
    }

    public WellPlayer(UUID playerUUID, int bronzeCoins, int silverCoins, int goldCoins, int coinsDeposited, int experiencePoints) {
        this.playerUUID = playerUUID;
        this.bronzeCoins = bronzeCoins;
        this.silverCoins = silverCoins;
        this.goldCoins = goldCoins;
        this.coinsDeposited = coinsDeposited;
        this.experiencePoints = experiencePoints;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getBronzeCoins() {
        return bronzeCoins;
    }

    public int getSilverCoins() {
        return silverCoins;
    }

    public int getGoldCoins() {
        return goldCoins;
    }

    public int getCoinsDeposited() {
        return coinsDeposited;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void depositCoin(CoinType coinType) {
        if (coinType == null)
            return;

        this.experiencePoints += coinType.getExperience();
        this.coinsDeposited += 1;
        switch (coinType) {
            case GOLD:
                this.goldCoins += 1;
                break;
            case SILVER:
                this.silverCoins += 1;
                break;
            case BRONZE:
                this.bronzeCoins += 1;
                break;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage(ChatColor.GRAY + "You have deposited a " + coinType.getWellsId() + ChatColor.GRAY + "!");
        }
    }

}
