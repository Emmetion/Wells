package me.emmetion.wells.listeners;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HologramListener implements Listener {

    private Hologram spawnNPCHologram;

    @EventHandler
    public void handleHologramAction(HologramClickEvent event) {
//        // Must be a normal left/right click. No shift.
//        if (!event.getClick().equals(ClickType.RIGHT) && !event.getClick().equals(ClickType.LEFT)) {
//            return;
//        }
//
//        Player player = event.getPlayer();
//
//        if (spawnNPCHologram == null) {
//            spawnNPCHologram = DHAPI.getHologram("SpawnNPCHologram");
//            player.sendMessage("Assigned SpawnNPCHologram.");
//        }
//
//        Hologram clickedHologram = event.getHologram();
//        if (!clickedHologram.getName().equals(spawnNPCHologram.getName())) {
//            return;
//        }
//
//        HologramPage page = clickedHologram.getPage(player);
//        int pageIndex = page.getIndex();
//
//        int hologramPageCount = clickedHologram.getPages().size();
//
//        // Cycle through hologram next to SpawnNPC.
//        int newPageIndex;
//        if (pageIndex == hologramPageCount - 1) {
//            newPageIndex = 0;
//        } else {
//            newPageIndex = pageIndex + 1;
//        }
//
//        clickedHologram.show(player, newPageIndex);
//
//        player.sendMessage(getColor("Clicked SpawnNPCHologram!"));

    }

}
