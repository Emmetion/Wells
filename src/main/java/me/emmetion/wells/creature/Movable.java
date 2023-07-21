package me.emmetion.wells.creature;

/**
 * This interface is used on WellCreature's that have a movement component.
 * Example: Pixie (name & hit-box flying up and down)
 *
 * Every frame, WellCreature's receive an update.
 * If the WellCreature implements the Moveable interface, it will call the move function defined inside the Creatures class.
 */
public interface Movable {

    void move();

}
