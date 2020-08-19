package nl.toefel.game.command;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.movable.Player;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateCommandDisplay extends ICommand {

	IRoomObject initiator = null;
	String message = "";
	ArrayList<ICommand> commands = null;
	
	public CreateCommandDisplay(){
		this(null, null);
	}
	
	public CreateCommandDisplay(IRoomObject initiator, ArrayList<ICommand> commands){
		this(initiator, "", commands);
	}
	
	public CreateCommandDisplay(IRoomObject initiator, String message, ArrayList<ICommand> commands){
		this(initiator, message, commands, "OK" );
	}
	
	public CreateCommandDisplay(IRoomObject initiator, String message, ArrayList<ICommand> commands, String name ){
		this.name = name;
		this.initiator = initiator;
		this.message = message;
		this.commands = commands;
	}
	
	
	public ICommand createInstance(Node xmlData, IRoomObject initiator,	IRoomObject executer) {
		if(initiator == null){
			System.out.println("WARNING, create command created with initiator set to null, cannot determine location! using the position of first player!");
			HashMap<String, Player> players = Game.getInstance().getPlayers();
			
			if(players.size() <= 0){
				System.err.println("ERROR, no players found to use as the location object for CreateCommandDisplay!");
				return null;
			}
			
			Player player = players.values().iterator().next();
			System.out.println("DEBUG-INFO, createcommanddisplay is using position of " + player.getIDString());
			
			//extract a player from the hashmap
			this.initiator = player;
		}
		
		String message = "";
		ArrayList<ICommand> commands = new ArrayList<ICommand>();
		NodeList children = xmlData.getChildNodes();
		
		//iterate all children
		for(int i = 0; i < children.getLength(); i++){
			Node item = children.item(i);
			
			if(item.getNodeName().equals("message")){
				message = Translator.getTranslation(item, Game.getInstance().getLanguage(), "");
			}else if(item.getNodeName().equals("options")){
				commands = extractCommands(item);
			}
		}
		
		CreateCommandDisplay command = new CreateCommandDisplay((IRoomObject) initiator, message, commands, Translator.getTranslation(xmlData, Game.getInstance().getLanguage(), "OK"));
		return command;
	}

	/**
	 * Creates a command display with the initialized values
	 */
	public boolean execute() {
		if(commands == null){
			//TODO, no commands creates a window that can be escaped with the esc keypress. but this isn't usually a wanted situation
			System.err.println("ERROR: create command display command executed with no commands!");
			return false;
		}
		
		CommandDisplay commandDisplay = new CommandDisplay();
		commandDisplay.setTekst(message);
		commandDisplay.addCommandList(commands);
		commandDisplay.setLocation(initiator.getLocation());
		Game.getInstance().setCommandDisplay(commandDisplay);
		
		return true;
	}

	/**
	 * Extracts the commands from this the containing node
	 * 
	 * @param containingNode node that contains commands
	 * @return all commands contained in this node
	 */
	protected ArrayList<ICommand> extractCommands(Node containingNode){
		ArrayList<ICommand> extractedCommands = new ArrayList<ICommand>();
		
		Game game = Game.getInstance();
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList commands;
		
		try {
			// Get the right XML nodes.
			commands = (NodeList) xPath.compile("command").evaluate(containingNode, XPathConstants.NODESET);
		
			// extensive error checking because crashes here render the game unusable
			for(int i = 0, max = commands.getLength(); i < max; i++){
				Node commandNode = commands.item(i);
				if(commandNode == null) continue;
				
				NamedNodeMap attributes = commandNode.getAttributes();
				if(attributes == null) continue;
				
				Node commandNameNode = attributes.getNamedItem("commandname");
				if(commandNameNode == null) continue;
				
				//empty command cannot be instantiated
				String commandName = commandNameNode.getNodeValue();
				if(commandName.equals("")) continue;
				
				IRoomObject initiator = null;
				IRoomObject executer = null;
				
				Node initiatorNode = attributes.getNamedItem("initiator");
				if(initiatorNode != null){
					String initiatorId = initiatorNode.getNodeValue();
					initiator = game.getRoomObjectById(initiatorId);
				}
				
				Node executerNode = attributes.getNamedItem("executer");
				if(executerNode != null){
					String executerId = executerNode.getNodeValue();
					executer = game.getRoomObjectById(executerId);
				}
								
				ICommand command = game.getCommand(commandName, commandNode, initiator, executer);
				
				/*//NOT NEEDED IN THIS SITUATION I GUESS!
				Node directlyExecuteNode = attributes.getNamedItem("directlyexecute"); 
				if(directlyExecuteNode != null){
					if(directlyExecuteNode.getNodeValue().equals("true")){
						command.execute();
						//TODO returns null if command is directly executed, should this be so?
						return null;
					}
				}
				*/
				
				if(command != null)
					extractedCommands.add(command);
			}
		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return extractedCommands;
	}

}
