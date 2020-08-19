package nl.toefel.game.roomobject.movable;

import nl.toefel.game.Game;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.drawer.*;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Node;


public class Player extends Human{
	public Player(Node beginState)
	{
		// Let the begin state be processed (e.g. stored) by the parent's constructor.
		super(beginState);
		
		//Game.getInstance().addDrawer(new SWTDrawer(Display.getCurrent(), this));
		
		// Register the used images.
		registerImages();
		
		//Game.getInstance().registerPlayer(this);
	}
}
