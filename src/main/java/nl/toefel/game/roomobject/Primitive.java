package nl.toefel.game.roomobject;

import java.util.ArrayList;

import org.w3c.dom.Node;

import nl.toefel.game.command.ICommand;
import nl.toefel.game.roomobject.*;
import nl.toefel.game.roomobject.movable.IMovable;

/**
 * The most primitive of IRoomObject.
 * 
 * TODO Discuss.
 * TODO Add to UML.
 */
public class Primitive extends IRoomObject {

	public Primitive(Node state) {
		super(state);
		
		// Register the used images.
		registerImages();
	}

	/**
	 * Gets empty command list for primitive type, use extended classes if you need commands
	 */

	
}
