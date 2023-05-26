package me.emmetion.wells.observer;

import me.emmetion.wells.model.Well;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class IncrementObserver implements Observer {

    private Well well;

    public IncrementObserver(Well well) {
        this.well = well;
        well.attachObserver(this);
    }

    @Override
    public void update(Well well) {
        Server server = Bukkit.getServer();
        server.sendMessage(Component.text("IncrementObserver called."));
    }
}
