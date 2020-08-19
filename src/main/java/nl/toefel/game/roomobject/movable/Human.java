package nl.toefel.game.roomobject.movable;

import nl.toefel.game.Game;
import nl.toefel.game.command.*;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.roomobject.IRoomObject;

import java.util.ArrayList;

import org.w3c.dom.Node;

/**
 * 
 * @author Jeffrey
 *
 */
public class Human extends IOrganism {

	
	public Human(Node state) {
		super(state);
		registerImages();
		imageIndex = 1;
	}
	
}
