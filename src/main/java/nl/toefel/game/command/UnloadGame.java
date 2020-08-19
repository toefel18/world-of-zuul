package nl.toefel.game.command;
import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.IRoomObject;
import org.w3c.dom.Node;

public class UnloadGame extends ICommand {

	public UnloadGame(){
		this("Stop game unsaved?");
	}
	
	public UnloadGame(String name){
		this.name = name;
	}
	
	public boolean execute() {
		Game.getInstance().unload();
		return false;
	}
	
	/**
	 * Creates an unload command
	 * 
	 * @param xmlData
	 * @param initiatior not used, can be null
	 * @param executer not used, can be null
	 */
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		UnloadGame command = new UnloadGame(Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), name));
		return command;
	}
}
