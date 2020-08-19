package nl.toefel.game.command;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.Point;
import nl.toefel.game.roomobject.movable.IMovable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Move extends ICommand {
		
	/** Object that can be moved*/
	private IMovable movable;
	
	/** The disposition to move the object*/
	private Point disposition;

	
	
	/**
	 * Default constructor, this must be available to
	 * construct meaningless objects that can be
	 * duplicated via the createInstance interface
	 */
	public Move(){
		this(null, null);
	}
	
	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 */
	public Move(IMovable movable, Point disposition){
		this(movable, disposition, "Move");
	}

	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 * @param name the name of the command
	 */
	public Move(IMovable movable, Point disposition, String name){
		this.movable = movable;
		this.disposition = disposition;
		this.name = name;
	}
	
	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param x pixels to be moved in x direction
	 * @param y pixels to be moved in y direction
	 */
	public Move(IMovable movable, int x, int y){
		this(movable, new Point(x, y));
	}

	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param x pixels to be moved in x direction
	 * @param y pixels to be moved in y direction
	 * @param name the name of the command
	 */
	public Move(IMovable movable, int x, int y, String name){
		this(movable, new Point(x, y), name);
	}
	
	/**
	 * moves the movable object
	 */
	public boolean execute() {
		if(movable == null){
			System.out.println("DEBUG-INFO: Move command executed on NULL object");
			return false;
		}
		
		movable.move(disposition);
		return true;
	}

	/**
	 * Creates a move command, the executer will be moved according to the specified coordinates
	 * retrieved from xml data
	 * 
	 * @param xmlData
	 * @param initiatior (not used,can be null)
	 * @param executer an IMovable object that should be moved, REQUIRED!
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(executer instanceof IMovable)){
			System.err.println("Error, Move command created via createInstance with executer set to NULL, not possible");
			return null;
		}
		
		this.movable = (IMovable) executer;
		//TODO IMPLEMENT MOVE XML READING!
		//return (IMovable) new Move();
		return null;
	}
}
