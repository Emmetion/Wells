package me.emmetion.wells.menu;

import me.emmetion.wells.model.WellPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerMenuUtility {

    // This could change to not be final, if we are going to pass the inventory to another player lets say.
    private final Player owner;
    // The WellPlayer object representing stats relating to well players.
    private WellPlayer wellPlayer;

    // Constructor for PlayerMenuUtility
    public PlayerMenuUtility(Player owner, WellPlayer playerData){
        this.owner = owner;
        this.wellPlayer = playerData;
    }

    public Player getOwner() {
        return owner;
    }


    /**
     * Helper method for sending TextComponent's to Inventory Owner.
     * @param component - the compnent you want to send to the inventory viewer.
     */
    public void sendMessage(@NotNull Component component) {
        if (this.getOwner() != null) {
            owner.sendMessage(component);
        }
    }

    /**
     * Helper method for sending String/TextComponent's to Inventory Owner.
     * @param text The message you want to send to the inventory viewer.
     */
    public void sendMessage(@NotNull String text) {
        sendMessage(Component.text(text));
    }

    public WellPlayer getWellPlayer() {
        return wellPlayer;
    }

    public void playSound(Location location, Sound sound){
        owner.playSound(location, sound, 1, 1);
    }

}