package me.emmetion.wells.anim;

public class AnimationSettings {

    private final String name;
    private final int delay;
    private final int period;

    public AnimationSettings(String name, int timer_delay, int timer_period) {
        this.name = name;
        this.delay = timer_delay;
        this.period = timer_period;
    }

    public String getName() {
        return this.name;
    }

    public int getDelay() {
        return delay;
    }

    public int getPeriod() {
        return period;
    }



}
