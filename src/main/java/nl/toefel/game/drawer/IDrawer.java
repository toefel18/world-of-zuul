package nl.toefel.game.drawer;


//TODO CRITICAL!!   the whole IObserve pattern can be removed, because when something moves 
//the drawer NEEDS to update, the movable will update only attached observers, 
//others will not be notified. This SHOULD in fact always happen.
//	SOLUTION: just store a reference of the IRoomObject you want to have as center
//when any movable object moves, it notifies Game. and Game notifies ALL drawers
//the drawers update their presentation, and center on the IRoomObject

import nl.toefel.game.roomobject.IRoomObject;

public interface IDrawer {
	/**
	 * draws the current context of the associated object
	 */
	public void draw();
	
	/**
	 * preloads the image if it is not already preloaded
	 * @param path path to the image
	 */
	public void preloadImage(String fullImagePath, String imagePath);
	
	/**
	 * disposes the graphical window, this should automatically be
	 * forwarded to game, and the whole drawer will be removed from the game!
	 */
	public void dispose();
	
	/**
	 * Returns the focused IRoomObject.
	 * @return	IRoomObject	The focus.
	 */
	public IRoomObject getFocus();
}
