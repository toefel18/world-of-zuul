package nl.toefel.game.command;


import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.w3c.dom.Node;

public class Talk extends ICommand {

	IOrganism organism = null;
	
	/**
	 * Initializes default talk command, cannot really execute
	 */
	public Talk(){
		this(null);
	}
	
	/**
	 * Initializes the talk command, and triggers the organism to talk when executed
	 * @param organism the organism which starts talking
	 */
	public Talk(IOrganism organism){
		this(organism, "Talk");
	}
	
	/**
	 * Initializes the talk command with a name, and triggers organism to talk when executed
	 * @param organism the organism which starts talking
	 * @param name the name of the command
	 */
	public Talk(IOrganism organism, String name){
		this.organism = organism;
		this.name = name;
	}
	
	/**
	 * Creates a talk command, reads the xml data for information
	 * 
	 * @param xmlData
	 * @param initiatior the IOrganism that initiates the conversation
	 * @param executer the IOrganism that should start talking
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(executer instanceof IOrganism)){
			System.err.println("ERROR, talk command created with executer set to null, not possible");
			return null;
		}
		
		Talk command = new Talk((IOrganism) executer, Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
		return command;
	}

	/**
	 * Triggers the attached organism to talk
	 */
	public boolean execute() {
		if(organism == null)
			return false;
		
		organism.talk();
		return true;
	}
}
