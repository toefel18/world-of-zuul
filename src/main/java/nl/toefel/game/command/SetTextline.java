package nl.toefel.game.command;


import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.w3c.dom.Node;

public class SetTextline extends ICommand {

	IOrganism organism = null;
	String textlineId = "";
	
	public SetTextline(){
		this(null, null);
	}
	
	public SetTextline(IOrganism organism, String textlineId){
		this(organism, textlineId, "Set Textline");
	}
	
	public SetTextline(IOrganism organism, String textlineId, String name){
		this.organism = organism;
		this.textlineId = textlineId;
		this.name = name;
	}
	
	public ICommand createInstance(Node xmlData, IRoomObject initiator, IRoomObject executer) {
		if(!(initiator instanceof IOrganism) || xmlData == null){
			System.err.println("ERROR, trying to update the textline while no organism specified");
			return null;
		}
		
		
		
		Node newid = xmlData.getAttributes().getNamedItem("newid");
		if(newid == null){
			System.err.println("ERROR, trying to update the textline while on id specified");
			return null;
		}
		
		return new SetTextline((IOrganism) initiator, newid.getNodeValue(), Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), "Update textline"));
	}

	/**
	 * Updates the textline of the configured organism
	 */
	public boolean execute() {
		if(organism != null){
			organism.setTextline(textlineId);
			return true;
		}
		
		return false;
	}

}
