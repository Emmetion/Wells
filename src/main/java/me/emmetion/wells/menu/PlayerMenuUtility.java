package me.emmetion.wells.menu;

import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class PlayerMenuUtility {


    private Player owner;
    private WellPlayer wellPlayer;

    public PlayerMenuUtility(Player owner, WellPlayer playerData){
        this.owner = owner;
        this.wellPlayer = playerData;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public WellPlayer getWellPlayer() {
        return wellPlayer;
    }

    public void setWellPlayer(WellPlayer playerData) {
        this.wellPlayer = playerData;
    }

    public void playSound(Location location, Sound sound){
        owner.playSound(location, sound, 1, 1);
    }

}