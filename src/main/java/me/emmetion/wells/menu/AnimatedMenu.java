package me.emmetion.wells.menu;

/**
 * Interface for WellMenu's that have an animated component.
 */
public interface AnimatedMenu {

    // Constants
    int DEFAULT_DELAY = 0; // No delay.
    int DEFAULT_PERIOD = 2; // Updating every 2 ticks.

    default int runnableDelay() {
        return DEFAULT_DELAY;
    }

    // The interval (in server-ticks) of how long each frame should be between updates.
    default int runnablePeriod() {
        return DEFAULT_PERIOD;
    }

    // Frames are counted
    int getCurrentFrame();

    // A void method to update the menu.
    void update();

}

