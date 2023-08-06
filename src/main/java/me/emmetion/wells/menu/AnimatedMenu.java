package me.emmetion.wells.menu;

/**
 * Interface for WellMenu's that have an animated component.
 */
public interface AnimatedMenu {

    // Frames are counted
    int getCurrentFrame();

    // The interval (in server-ticks) of how long each frame should be between updates.
    int updateInterval();

    // A void method to update the menu.
    void update();

}

