package me.emmetion.wells.model;

import me.athlaeos.valhallammo.ValhallaMMO;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import scala.Enumeration;

import java.util.UUID;

public class WellPlayer {


    private UUID playerUUID;

    private int bronzeCoins;
    private int silverCoins;
    private int goldCoins;

    private int coinsDeposited;

    private int experiencePoints;

    private boolean hideParticles = false;
    private boolean hideBossBar = false;

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

    public void depositCoin(CoinType coinType, Well well) {
        if (well == null) { // don't know what would cause this, but if it ever happens, we prevent it here.
            System.out.println("Well was null on deposited coin.");
            return;
        }

        if (coinType == null)
            return;

        this.experiencePoints += coinType.getExperience();
        this.coinsDeposited += 1;

        well.depositCoin(coinType);
        // add well experiences.
        switch (coinType) {
            case GOLD_COIN:
                this.goldCoins += 1;
                break;
            case SILVER_COIN:
                this.silverCoins += 1;
                break;
            case BRONZE_COIN:
                this.bronzeCoins += 1;
                break;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage(ChatColor.WHITE + "You have deposited a " + coinType.getWellsId() + "! (" + coinType.getExperience() + "xp)");
        }
    }

    public void setHideParticles(boolean hide) {
        this.hideParticles = hide;
    }

    public boolean canSeeParticles() {
        return !this.hideParticles;
    }

    public void sendMessage(String text) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null)
            player.sendMessage(Component.text(text));
    }

    /**
     * Toggles hideParticles between true and false.
     * @return canSeeParticles.
     */
    public boolean toggleParticles() {
        this.hideParticles = !this.hideParticles;
        return canSeeParticles();
    }

    public void displayBossBar() {
        if (this.hideBossBar || !isOnline())
            return;

        Player player = getBukkitPlayer();

        Well well;

        // TODO: Create bossbar detailing time remaining on current well-buff.

    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(this.playerUUID);
    }

    public boolean isOnline() {
        Player player = Bukkit.getPlayer(this.playerUUID);
        if (player == null)
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WellPlayer) {
            WellPlayer other = (WellPlayer) obj;
            return this.playerUUID.equals(other.playerUUID);
        }
        return false;
    }


}
