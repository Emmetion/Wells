package me.emmetion.wells.anim;

import me.emmetion.wells.Wells;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class Animation extends BukkitRunnable {

    protected BukkitTask animationTask;

    private boolean hasBegun = false;
    private boolean hasEnded = false;


    /**
     * Each animation stores an animation settings.
     * @return
     */
    public abstract AnimationSettings getAnimationSettings();

    /**
     * This method will start an animation with #runTaskTimer().
     */
    public void start() {
        if (getAnimationSettings() == null) { // If AnimationSettings are undefined, we don't start the animation.
            Wells.plugin.getLogger().info("Attempted to start Animation with invalid Animation Settings. Check if " +
                                                "AnimationSettings are null in class definition.");
            return;
        }

        JavaPlugin plugin = Wells.plugin;
        animationTask = runTaskTimer(plugin, getAnimationSettings().getDelay(), getAnimationSettings().getPeriod());
        this.hasBegun = true;
    }

    public boolean hasBegun() {
        return this.hasBegun;
    }

    private boolean hasEnded() {
        return this.hasEnded;
    }


    public BukkitTask getAnimationTask() {
        return this.animationTask;
    }

    /**
     * This will end the animation if it has not already ended.
     */
    public void end() {
        if (!animationTask.isCancelled()) {
            animationTask.cancel();
            this.hasEnded = true;
        }
    }



}
