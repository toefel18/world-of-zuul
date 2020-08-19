package nl.toefel.game.roomobject;

import java.util.ArrayList;
import java.lang.Exception;
import org.w3c.dom.Node;

import nl.toefel.game.Game;
import nl.toefel.game.InvokedEvent;
import nl.toefel.game.Room;
import nl.toefel.game.command.ClearCommandDisplay;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.command.Move;
import nl.toefel.game.command.Open;
import nl.toefel.game.gamedisplays.CommandDisplay;
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
public class Door extends IRoomObject {

	//private ArrayList<ICommand> usableCommands = new ArrayList<ICommand>();
	
	public Door(Node state) {
		super(state);
		
		visible = false;
		
	//	usableCommands.add(new Open());
	}
	
	public int getCommandRange()
	{
		return 10;
	}
	/*
	public ArrayList<ICommand> getUsableCommands(IMovable movable)
	{
		return usableCommands;
	}
	*/
	
	public Door getLinkedDoor()
	{
		return (Door) Game.getInstance().getRoomObjectById(objectState.getAttributes().getNamedItem("linkeddoor").getNodeValue());
	}
	
	private Point getNewPoint()
	{
		return new Point(
							Integer.valueOf(objectState.getAttributes().getNamedItem("newx").getNodeValue()),
							Integer.valueOf(objectState.getAttributes().getNamedItem("newy").getNodeValue())
						);
	}
	
	private String getDirection()
	{
		return objectState.getAttributes().getNamedItem("direction").getNodeValue();
	}
	
	protected Doorkey getUnlockDoorkey()
	{
		Node unlockKeyNode = objectState.getAttributes().getNamedItem("unlockdoorkey");
		
		if(unlockKeyNode == null)
			return null;

		return (Doorkey) Game.getInstance().getRoomObjectById(unlockKeyNode.getNodeValue());
	}
	
	public void open(IOrganism initiator)
	{
		Game game = Game.getInstance();
		
		Doorkey unlockDoorkey = getUnlockDoorkey();
		
		if(unlockDoorkey != null && !initiator.hasInInventory(unlockDoorkey))
		{
			System.out.println("Doorkey::use(): IOrganism '" + initiator.getIDString() + "' does not has the Unlockkey in his inventory. Door wil not open.");
			CommandDisplay commandDisplay = new CommandDisplay();
			commandDisplay.setTekst("You do not have the right key to open the door. This text is largened for testing purposes, for looking to the line breaking algorithm heheh, i believe this should break on words!!");
			commandDisplay.setLocation(initiator.getLocation());
			commandDisplay.addCommand("I'll search", new ClearCommandDisplay());
			game.setCommandDisplay(commandDisplay);
			//TODO add sounds from xml
			game.playSound("doorlocked1.wav");
			game.redraw();
			new InvokedEvent(initiator, this, "lockeddoor");
			return;
		}
		
		Door buddyDoor = getLinkedDoor();
		Room room = buddyDoor.getRoom();
		game.playMusic(room.getSoundFile());
		initiator.setRoom(room);
		
		//TODO add sounds from xml!
		buddyDoor.placeRoomObjectInFront(initiator);
		game.playSound("opendoor1.wav");
		game.redraw();
	}
	
	/**
	 * 
	 * TODO	Implement collisions checks which are based on collisions instead of the whole size of the Moveable.
	 * @param moveable
	 */
	public void placeRoomObjectInFront(IMovable moveable)
	{
		int newDirection;
		
		Point 	newPoint 	= getNewPoint();
		String 	direction 	= getDirection();
		
		if(direction.equals("north"))
		{
			newDirection = 1;
		
			newPoint = new Point(
									newPoint.getX(),
									newPoint.getY() + moveable.getSize().height
								);
		}
		else if(direction.equals("south"))
		{
			newDirection = 2;
		
			newPoint = new Point(
									newPoint.getX(),
									newPoint.getY() + moveable.getSize().height 
								);
		}
		else if(direction.equals("east"))
		{
			newDirection = 3;
			
			newPoint = new Point(
									newPoint.getX() + moveable.getSize().width ,
									newPoint.getY()
								);
		}		
		else if(direction.equals("west"))
		{
			newDirection = 4;
		
			newPoint = new Point(
									newPoint.getX() - moveable.getSize().width ,
									newPoint.getY()
								);
		}
		else // Use north direction.
		{
			newDirection = 1;
			
			newPoint = new Point(
									newPoint.getX(),
									newPoint.getY() + moveable.getSize().height
								);
			
			System.out.println("Door::placeRoomObjectInFront: Invalid direction given, 'north' used.");
		}
		
		moveable.setLocation(newPoint, newDirection);
	}

	/**
	 * Gets the usable commands associated to a movable object
	 * 
	 * TODO IF ONLY ONE COMMAND IS READY TO EXECUTE, MAYBE EXECUTE IT DIRECT? (watch out for infinite loop)
	 * 
	 * @param initiator IMovable object (altough IROomObject is required, it will be upcast to IMovable!)
	 */
	/*
	public ArrayList<ICommand> getUsableCommands(IMovable initiator) {
		//TODO extract from xml, make sure XML is updated!
		ArrayList<ICommand> usableCommands = new ArrayList<ICommand>();				
		usableCommands.add(new Open((IOrganism) initiator, this));
		return usableCommands;
	}
	*/
}
