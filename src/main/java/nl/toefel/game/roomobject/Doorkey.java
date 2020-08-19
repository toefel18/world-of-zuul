package nl.toefel.game.roomobject;

import java.util.ArrayList;

import org.w3c.dom.Node;

import nl.toefel.game.Game;
import nl.toefel.game.Room;
import nl.toefel.game.command.*;

import nl.toefel.game.roomobject.*;
import nl.toefel.game.roomobject.movable.IMovable;
import nl.toefel.game.roomobject.movable.IOrganism;
import nl.toefel.game.roomobject.movable.Player;

/**
 * The most primitive of IRoomObject.
 * 
 * TODO Discuss.
 * TODO Add to UML.
 */
public class Doorkey extends ICarryable {

//	private ArrayList<ICommand> usableCommands = new ArrayList<ICommand>();
	
	public Doorkey(Node state) {
		super(state);
		//TODO, COMMANDS SHOULD BE READ FOM XML, NOT LIKE THIS I GUESS

		
	}
	
	public int getCommandRange(){
		return 5;
	}
	
	/*
	public ArrayList<ICommand> getUsableCommands(IMovable initiator){
		ArrayList<ICommand> usableCommands = new ArrayList<ICommand>();
		usableCommands.add(new PickUp(initiator, this));
		usableCommands.add(new ClearCommandDisplay());
		
		return usableCommands;
	}
	*/
	
	/**
	 * TODO this method should not try to carry the item, this method should be forced by IUsable interface
	 * @param initiator
	 */
	public void use(IOrganism initiator){
		//initiator.carryItem(this);
		//Game.getInstance().redraw();
	}
}
