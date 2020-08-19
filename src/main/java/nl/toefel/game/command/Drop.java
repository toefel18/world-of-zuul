package nl.toefel.game.command;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.ICarryable;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.w3c.dom.Node;

public class Drop extends ICommand {

	/** Object that initiates the action. */
	private IOrganism initiator;
	
	/** The object to execute the action on. */
	private ICarryable executer;
	
	/**
	 * Default constructor, this must be available to
	 * construct meaningless objects that can be
	 * duplicated via the createInstance interface
	 */
	public Drop(){
		this(null, null);
	}
	
	/**
	 * Moves the movable object a certain amount of pixels
	 * into the x and y direction
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 */
	public Drop(IOrganism initiator, ICarryable executer){
		this(initiator, executer, "Pick Up");
	}
	
	/**
	 * Makes the initiator drop the executer
	 * 
	 * @param movable the object to be moved
	 * @param movement the x and y coordinates relative from the objects current state.
	 * @param name the name of the command
	 */
	public Drop(IOrganism initiator, ICarryable executer, String name){
		this.initiator 	= initiator;
		this.executer 	= executer;
		this.name = name;
	}

	/**
	 * Make the associated initiator drop the item
	 */
	public boolean execute() {
		if(initiator != null && executer != null){
			initiator.dropItem(executer);
			return true;
		}else{
			System.err.println("DEBUG-INFO: Executing drop command failed because of missing initiator/executer.");
		}
		
		return false;
	}

	/**
	 * Creates a drop command from xml
	 * 
	 * @param xmlData
	 * @param initiatior the IOrganism that drops the item
	 * @param executer the object that is dropped 
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(initiator instanceof IOrganism) || !(executer instanceof ICarryable)){
			System.err.println("ERROR, drop command created with executer or initiator set to null, not possible");
			return null;
		}

		Drop command = new Drop((IOrganism) initiator, (ICarryable)executer, Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
		return command;
	}
	
}
