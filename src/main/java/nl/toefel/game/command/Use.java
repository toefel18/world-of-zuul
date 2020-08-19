package nl.toefel.game.command;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.Doorkey;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.IMovable;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Use extends ICommand {
	
	/** Object that initiates the action. */
	private IMovable initiator;
	
	/** The object to execute the action on. */
	private IRoomObject executer;
	
	/**
	 * Default constructor, this must be available to
	 * construct meaningless objects that can be
	 * duplicated via the createInstance interface
	 */
	public Use(){
		this(null, null);
	}
	
	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 */
	public Use(IMovable initiator, IRoomObject executer){
		this(initiator, executer, "Use");
	}
	
	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 * @param name the name of the command
	 */
	public Use(IMovable initiator, IRoomObject executer, String name){
		this.initiator 	= initiator;
		this.executer 	= executer;
		this.name = name;
	}
	
	/**
	 * Executes the action.
	 */
	public boolean execute() {
		System.out.println("Use::execute()");
		
		if(initiator == null || executer == null)
		{
			System.out.println("DEBUG-INFO: Executing use command failed because of missing initiator/executer.");
			
			return false;
		}
		
		//TODO revisit this code, using this kind of mechanisms is not wanted, create IUsable interface for this!
		//this should be abstract
		if(executer instanceof Doorkey && initiator instanceof IOrganism)
			((Doorkey) executer).use((IOrganism) initiator);
		
		else
		{
			System.out.println("Use::execute(): Executing use command failed because of unknown type(s).");
			
			return false;
		}
		
		return true;
	}

	/**
	 * Creates a use command from xml
	 * 
	 * @param xmlData
	 * @param initiatior the IOrganism that uses the item
	 * @param executer the object that is used
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(initiator instanceof IMovable)){
			System.err.println("ERROR, use command created with executer set to null, not possible");
			return null;
		}
		
		//TODO URGENT UPDATE, CREATE INTERFACE interface IUSABLE, which creats a method that indicates if objects can be used!!
		Use command = new Use((IMovable) executer, executer, Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
		return command;
	}
}
