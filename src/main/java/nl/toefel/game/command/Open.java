package nl.toefel.game.command;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.Door;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Open extends ICommand {
	//TODO IMPORTANT! SHOULDN'T THIS BE RETYPED TO IMOVABLES!
	/** Object that initiates the action. */
	private IOrganism initiator;
	
	/** The object to execute the action on. */
	private IRoomObject executer;
	
	/**
	 * Default constructor, this must be available to
	 * construct meaningless objects that can be
	 * duplicated via the createInstance interface
	 */
	public Open(){
		this(null, null);
	}
	
	/**
	 * Opens a door to another room 
	 * 
	 * @param organism the object to be moved
	 * @param door
	 */
	public Open(IOrganism initiator, Door executer){
		this(initiator, executer, "Open");
	}

	/**
	 * Opens the door
	 * 
	 * @param organism the object to be moved
	 * @param door
	 * @param name name of the command
	 */
	public Open(IOrganism initiator, Door executer, String name){
		this.initiator = initiator;
		this.executer = executer;
		this.name = name;
	}
	
	/**
	 * Executes the action.
	 */
	public boolean execute() {
		System.out.println("Open::execute()");
		
		if(initiator == null || executer == null)
		{
			System.out.println("DEBUG-INFO: Executing open command failed because of missing initiator/executer.");
			
			return false;
		}
		//TODO: just check on NULL, do not use instanceof in this kind of situations!
		
		//MUST BE IOrganism to check inventory for keys
		if(executer instanceof Door && initiator instanceof IOrganism)
			((Door) executer).open((IOrganism) initiator);
		
		else
		{
			System.out.println("DEBUG-INFO: Executing open command failed because of unknown type.");
			
			return false;
		}
		
		return true;
	}

	/**
	 * Creates an open command
	 * 
	 * @param xmlData
	 * @param initiatior the IMovable that opens the door
	 * @param executer an IMovable object that should be moved, REQUIRED!
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(initiator instanceof IOrganism) || !(executer instanceof Door)){
			System.err.println("ERROR, open command created with executer or initiator set to null or invalid type, not possible");
			return null;
		}
		
		Open command = new Open((IOrganism) initiator, (Door)executer, Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
		return command;
	}
}
