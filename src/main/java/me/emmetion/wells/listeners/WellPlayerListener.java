package me.emmetion.wells.listeners;

import me.emmetion.wells.managers.WellManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WellPlayerListener implements Listener {

    private final WellManager wellManager;

    public WellPlayerListener(WellManager wellManager) {
        this.wellManager = wellManager;
    }

    @EventHandler
    public void onWellPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.sendMessage("Loading well player...");
        wellManager.loadWellPlayer(player);
        player.sendMessage("Loaded!");

    }

    @EventHandler
    public void onWellPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("you quit."));

        wellManager.unloadPlayer(player);
    }

}
