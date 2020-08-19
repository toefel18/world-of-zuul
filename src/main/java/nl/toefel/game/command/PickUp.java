package nl.toefel.game.command;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.ICarryable;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PickUp extends ICommand {
	
	/** Object that initiates the action. */
	private IOrganism initiator;
	
	/** The object to execute the action on. */
	private ICarryable executer;
	
	/**
	 * Default constructor, this must be available to
	 * construct meaningless objects that can be
	 * duplicated via the createInstance interface
	 */
	public PickUp(){
		this(null, null);
	}
	
	/**
	 * Makes the initiator pickup the executer
	 *
	 * @param organism the object to be moved
	 * @param carryable item
	 */
	public PickUp(IOrganism initiator, ICarryable executer){
		this(initiator, executer, "Pick Up");
	}
	
	/**
	 * Makes the initiator drop the executer
	 * 
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 * @param name the name of the command
	 */
	public PickUp(IOrganism initiator, ICarryable executer, String name){
		this.initiator 	= initiator;
		this.executer 	= executer;
		this.name = name;
	}
	
	/**
	 * Executes the action.
	 */
	public boolean execute() {
		System.out.println("Pick up::execute()");
		
		if(executer instanceof ICarryable && initiator instanceof IOrganism){
			((IOrganism)initiator).carryItem((ICarryable) executer);
		}else{
			System.out.println("DEBUG-INFO: Executing pickup command failed because of missing initiator/executer.");
			return false;
		}
		
		return true;
	}

	/**
	 * Creates a pickup command from xml
	 * 
	 * @param xmlData
	 * @param initiatior the IOrganism that picks up the item
	 * @param executer the object that is picked up
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(initiator instanceof IOrganism) || !(executer instanceof ICarryable)){
			System.err.println("ERROR, pick up command created with executer or initiator set to null, not possible");
			return null;
		}
		
		PickUp command = new PickUp((IOrganism) initiator, (ICarryable)executer, Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
		return command;
	}
}
