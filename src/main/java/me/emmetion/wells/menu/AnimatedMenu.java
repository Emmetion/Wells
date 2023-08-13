package me.emmetion.wells.menu;

/**
 * Interface for WellMenu's that have an animated component.
 */
public interface AnimatedMenu {

    // Frames are counted
    int getCurrentFrame();

    int runnableDelay();

    // The interval (in server-ticks) of how long each frame should be between updates.
    int runnablePeriod();

    // A void method to update the menu.
    void update();

}

