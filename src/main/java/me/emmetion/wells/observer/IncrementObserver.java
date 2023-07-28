package me.emmetion.wells.observer;

import me.emmetion.wells.model.Well;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class IncrementObserver implements Observer {

    private final Well well;

    public IncrementObserver(Well well) {
        well.attachObserver(this);
        this.well = well;
    }

    @Override
    public void update() {
        Server server = Bukkit.getServer();

        well.updateHologram();

        System.out.println("HELLO WORLD!");
        server.sendMessage(Component.text("IncrementObserver called."));
    }


}
