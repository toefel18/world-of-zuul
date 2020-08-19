package nl.toefel.game.controller;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.util.HashMap;

import nl.toefel.game.Game;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.command.Move;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.gamedisplays.ComplexDisplay;
import nl.toefel.game.roomobject.movable.IMovable;
import nl.toefel.game.roomobject.movable.IOrganism;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KeyController implements IController {
	private static final int MOVE_NORTH = 1;
	private static final int MOVE_SOUTH = 2;
	private static final int MOVE_EAST = 3;
	private static final int MOVE_WEST = 4;
	private static final int USE = 5;
	private static final int INVENTORY = 6;
	private static final int SHOWDEBUG = 7;
	private static final int ESCAPE = 8;
	
	//private static final int STEPS_PER_MOVE = 1;
	
	/** XML Node with the controls to respond to*/
	protected Node controlInfo;
	
	/** Movable object to alter when processing input*/
	protected IMovable movable;
	
	/** Contains a quick mapping from keycode to action*/
	private HashMap<Integer, Integer> commandMappings;
	
	/**
	 * Initializes the keyboard controller with data
	 * TODO review if IMovable should be changed to Player
	 * @param controlInfo
	 */
	public KeyController(IMovable movable, Node controlInfo){
		this.movable = movable;
		this.controlInfo = controlInfo;
		commandMappings = new HashMap<Integer, Integer>();
		updateControlInfo();
	}
	
	/**
	 * Sets a new control set, and reloads it
	 * @param controlInfo the new control set node
	 */
	public void updateControlSet(Node controlInfo){
		if(controlInfo != null)
			this.controlInfo = controlInfo;
		
		updateControlInfo();
	}
	
	/**
	 * Reads the XML node and creates in internal faster
	 * representation of the commands in the XML node
	 * 
	 */
	public void updateControlInfo(){
		NodeList commands = controlInfo.getChildNodes();
		
		//automatically delets old one, if it was already present
		commandMappings.clear();
		for(int i = 0; i<commands.getLength(); i++){
			Node node = commands.item(i);
			
			//check if the node is a command element!
			if(node.getNodeType() != Node.ELEMENT_NODE || node.getNodeName() != "command" )
				continue;
			
			//get the attributes
			NamedNodeMap commandAttribs = (NamedNodeMap) node.getAttributes();
			
			//should not happen
			if(commandAttribs == null){
				System.out.println("Element with no attributes");
				continue;
			}
			
			Node commandName = commandAttribs.getNamedItem("name");
			Node commandKeyCode = commandAttribs.getNamedItem("keycode");
			
			if(commandName == null || commandKeyCode == null){
				System.out.println("DEBUG-INFO: KeyController no name or keycode in controlset definition (settings.xml), skipping control");
				continue;
			}
			
			String name = commandName.getNodeValue();
			String keyCodeS = commandKeyCode.getNodeValue();
			int keyCode = Integer.valueOf(keyCodeS);
			
			
			System.out.println("DEBUG-INFO: Registering command '" + name + "' to keycode '" + keyCode + "'" );
			
			if(name.equals("move-north"))
				commandMappings.put(keyCode, MOVE_NORTH);
			else if(name.equals("move-south"))
				commandMappings.put(keyCode, MOVE_SOUTH);
			else if(name.equals("move-west"))
				commandMappings.put(keyCode, MOVE_WEST);
			else if(name.equals("move-east"))
				commandMappings.put(keyCode, MOVE_EAST);
			else if(name.equals("use"))
				commandMappings.put(keyCode, USE);
			else if(name.equals("inventory"))
				commandMappings.put(keyCode, INVENTORY);
			else if(name.equals("showdebug"))
				commandMappings.put(keyCode, SHOWDEBUG);
			else{
				System.out.println("DEBUG-INFO: Unknown command '" + name + "' found, skipping");
				continue;	//command not found
			}
		}
		
		//always map escape to escape!
		commandMappings.put((int)SWT.ESC, ESCAPE);
	}
	
	
	/**
	 * Processes keyboard input if it can
	 * @param code the keyCode of the button that is pressed
	 */
	public void processInput(int keyCode) {
		//System.out.println("DEBUG-INFO: KeyController got processInput request for code: " + keycode);
		ICommand command = null;
		Game game = Game.getInstance();
		Integer mappedTo = commandMappings.get(keyCode);
		if(mappedTo == null)
			return;
		
		CommandDisplay commandDisplay = game.getCommandDisplay();
		ComplexDisplay complexDisplay = game.getComplexDisplay();
		
		/*
		 //THIS DOESN'T WORK, DON'T KNOW WHY. METHOD BELOW DOES WORK!!
		if(commandDisplay != null || complexDisplay != null)
			game.keyReleased(keyCode)
		*/
		switch( mappedTo ){
		case MOVE_NORTH:
			if(commandDisplay != null){
				game.keyReleased(keyCode);
				commandDisplay.decrementSelected();
				game.redraw();
			}else if (complexDisplay != null){
				game.keyReleased(keyCode);
				complexDisplay.decrementTabSelectedItem();	//moves cursor of selected item in current tab one up!
				game.redraw();
			}else{
				command = new Move(this.movable, 0, -movable.getVelocity());
			}
			
			break;
		case MOVE_SOUTH:
			if(commandDisplay != null){
				game.keyReleased(keyCode);
				commandDisplay.incrementSelected();
				game.redraw();
			}else if (complexDisplay != null){
				game.keyReleased(keyCode);
				complexDisplay.incrementTabSelectedItem();
				game.redraw();
			}else{
				command = new Move(this.movable, 0, movable.getVelocity());
			}
		
			break;
		case MOVE_EAST:
			if(commandDisplay != null){
				//nothing to do, not possible in command displays
			}else if (complexDisplay != null){
				game.keyReleased(keyCode);
				complexDisplay.incrementSelected();
				game.redraw();
			}else{
				command = new Move(this.movable, movable.getVelocity(), 0);
			}
			
			break;
		case MOVE_WEST:
			if(commandDisplay != null){
				
			}else if (complexDisplay != null){
				game.keyReleased(keyCode);
				complexDisplay.decrementSelected();
				game.redraw();
			}else{
				command = new Move(this.movable, -movable.getVelocity(), 0);
			}
			
			break;
		case USE:	
			game.keyReleased(keyCode);
			System.out.println("executing enter");
			try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(commandDisplay != null){
				commandDisplay.executeSelectedCommand();
			}else if (complexDisplay != null){
				complexDisplay.executeSelectedTab();
			}else{
				movable.showCommandInteractions();
			}
			
			return;
		case INVENTORY:
			game.keyReleased(keyCode);
						
			//if this movable is an organism, show inventory, otherwise ignore!
			if((this.movable instanceof IOrganism) && game.getCommandDisplay() == null){
				game.setInventoryFrom((IOrganism) movable);
				game.redraw();
			}
			
			return;
		case SHOWDEBUG:
			//toggle debug
			game.keyReleased(keyCode);
			game.setShowDebug(!game.getShowDebug());
			game.redraw();
			return;
		case ESCAPE:
			game.keyReleased(keyCode);
		
			if(game.getCommandDisplay() != null)
				game.clearCommandDisplay();
			else if(game.getComplexDisplay() != null)
				game.clearComplexDisplay();
			else
				game.setShutdownDisplay(movable.getLocation());
	
			return;
		default:
			//no processor for keycode found, exit function!
			return;
		}
		
		if(command != null)
			command.execute();
	}

}
