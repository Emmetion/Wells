package me.emmetion.wells.observer;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import me.emmetion.wells.Wells;
import me.emmetion.wells.config.Configuration;
import me.emmetion.wells.model.Well;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.emmetion.wells.util.Utilities.getColor;

public class LevelUpObserver implements Observer {

    private final Well well;

    public LevelUpObserver(Well well) {
        well.attachObserver(this);
        //
        this.well = well;
    }

    @Override
    public void update() {
        Server server = Bukkit.getServer();

        well.updateHologram();


        String townname = well.getTownName();
        List<String> levelUpMessages = Configuration.getInstance().getWellLevelUp();

        Town town = TownyAPI.getInstance().getTown(townname);
        town.getResidents().stream()
                .filter(Resident::isOnline)
                .forEach(resident -> {
                    for (String levelUpMessage : levelUpMessages) {
                        resident.sendMessage(Component.text(getColor(levelUpMessage))); // Need to use townyapi component version.
                    }
                });
    }

    public void announceLevelUp() {
        Configuration config = Configuration.getInstance();
        List<String> wellLevelUp = config.getWellLevelUp();

        if (wellLevelUp == null) {
            Logger logger = Wells.plugin.getLogger();
            logger.info("Failed to send messages.well.level-up-announcement");

            return;
        }


        int well_level = well.getWellLevel();
        int old_well_level = well.getWellLevel() - 1;

        List<String> messages = wellLevelUp.stream()
                .map(s -> s.replace("{well_level}", String.valueOf(well_level)))
                .map(s -> s.replace("{old_well_level", String.valueOf(old_well_level)))
                .toList();

        final List<String> coloredMessages = getColor(messages); // apply color codes.



        TownyAPI townyapi = TownyAPI.getInstance();
        String townName = well.getTownName();

        // Now find the audience to send these messages to.
        List<Player> recipients = townyapi.getOnlinePlayersInTown(townyapi.getTown(townName));
        // sending messages.
        recipients.forEach(r -> coloredMessages.forEach(r::sendMessage));
    }


}
