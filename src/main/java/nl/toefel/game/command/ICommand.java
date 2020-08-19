package nl.toefel.game.command;

import nl.toefel.game.roomobject.IRoomObject;
import org.w3c.dom.Node;


/**
 * Interface for all commands
 * @note MODIFIED execute(), removed sender/receiver. Handle this in constructor of subclass!
 * @note Could be abstract class with constructor setting the given XML. and getname/setname functionality
 */
public abstract class ICommand {
	protected String name = "";
	
	/**
	 * executes the command ( if sender/receiver associates are needed, this must be done in constructors of subclasses)
	 * 
	 * @return executed result success/failure
	 */
	public abstract boolean execute();
	
	/**
	 * Returns the command itself, initialized with data
	 * from the xmlDdata node, this function is used for
	 * creating commands from within the XML file. the 2
	 * Parameters will be upcasted to their required type,
	 * Depending on what the command needs! If not able to
	 * upcast, this function should return null.
	 * 
	 * TODO consider upgrading initiator to IMovable, this should almos always be the case!
	 * 
	 * @param xmlData xml node with containing information to construct the object
	 * @param initator the object that initiates the command 
	 * @param executer the object that will be triggered to execute something
	 * 
	 * @return ready to execute command!
	 */
	public abstract ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer);
	
	/**
	 * Returns the name of the command.
	 * TODO this fucntion should return language specific name, therefore setName should be called first
	 * @return the name of the command.
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Sets the name of the commmand for a particular situation
	 * 
	 * @param name name of the command
	 */
	public void setName(String name){
		this.name = name;
	}
	
}
