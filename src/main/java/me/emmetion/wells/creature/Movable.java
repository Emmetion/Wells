package me.emmetion.wells.creature;

/**
 * This interface is used on WellCreature's that have a movement component.
 * Example: Pixie (name flying up and down)
 *
 * Every frame, WellCreature's get an update frame.
 * The base method is WellCreature#updateCreature(), which is called every frame.
 */
public interface Movable {

    void move();

}
