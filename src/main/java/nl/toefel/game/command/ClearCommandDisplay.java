package nl.toefel.game.command;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.IRoomObject;
import org.w3c.dom.Node;

public class ClearCommandDisplay extends ICommand {
	/**name of the command*/

	/**Intialised standard command with standard name*/
	public ClearCommandDisplay(){
		this.name = "Back";
	}
	
	/**
	 * Initialises clearing display command with the given name
	 * @param name the name of the command to be shown
	 */
	public ClearCommandDisplay(String name){
		this.name = name;
	}
	
	/**
	 * Creates a usable clear display command with language depended name found in the xml
	 * 
	 * @param xmldata
	 * @param initiator ( unusued, can be null )
	 * @param executer (unused, can be null)
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		return new ClearCommandDisplay(Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
	}

	public boolean execute() {
		Game.getInstance().clearCommandDisplay();
		Game.getInstance().redraw();
		return true;
	}

}
