package nl.toefel.game.command;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.IRoomObject;

import org.w3c.dom.Node;

public class Save extends ICommand {
	/**
	 * Creates the save command
	 * 
	 * @param xmlData, contains the name
	 * @param initiator not used
	 * @param executer not used
	 * @return save commmand
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		ICommand command = new Save();
		command.setName( Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), "Save") );
		return command;
	}
	
	/**
	 * Tells game to save it's state
	 */
	public boolean execute() {
		Game.getInstance().saveGame();
		return true;
	}

}
