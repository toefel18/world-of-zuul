package nl.toefel.game.roomobject.movable;

import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.toefel.game.Game;
import nl.toefel.game.InvokedEvent;
import nl.toefel.game.Translator;
import nl.toefel.game.command.ClearCommandDisplay;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.game.roomobject.ICarryable;
import nl.toefel.game.roomobject.Point;
import nl.toefel.game.roomobject.movable.Inventory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jeffrey
 *
 */
public abstract class IOrganism extends IMovable{
	
	/** holds different kind of inventories */
	protected ArrayList<Inventory> inventorys = new ArrayList<Inventory>();
	
//	protected ArrayList<ICarryable> inventory = new ArrayList<ICarryable>();
	
	public IOrganism(Node state) {
		super(state);
		initializeInventories();
	}
	
	/**
	 * Reads the inventories from XML and creates them in this game
	 */
	protected void initializeInventories(){
		try {
			NodeList inventoryNodes = (NodeList) XPathFactory.newInstance().newXPath().compile("inventorys/*").evaluate(objectState, XPathConstants.NODESET);
			
			for(int i = 0, max = inventoryNodes.getLength(); i < max; i++)
			{	
				Node inventoryNode = inventoryNodes.item(i);
				if(inventoryNode == null){
					System.err.println("ERROR: inventoryNode is null, skipping!");
					continue;
				}
				
				NamedNodeMap inventoryAttributes = inventoryNode.getAttributes();
				if(inventoryAttributes == null){
					System.err.println("ERROR: inventoryNode has no attributes, skipping!");
					continue;
				}
				
				Node maxWeightNode = inventoryAttributes.getNamedItem("maxcarryableweight");
				Node storeTypeNode = inventoryAttributes.getNamedItem("storetype");
				
				if(maxWeightNode == null || storeTypeNode == null){
					System.err.println("ERROR: initializing player inventories, maxcarryableweight or storetype is not correctly given, skipping!");
					continue;
				}
				
				String maxWeightString = maxWeightNode.getNodeValue();
				if(!isNumeric(maxWeightString)){
					System.err.println("ERROR: maxcarryableweight is not a number!, skipping");
					continue;
				}
				
				int maxWeight = Integer.valueOf(maxWeightString);
				String storeType = storeTypeNode.getNodeValue();
				String name = Translator.getTranslation(inventoryNode, Game.getInstance().getLanguage(), "DEFAULTNAME");
				
				Inventory storage = new Inventory(name, storeType, maxWeight);
				this.inventorys.add(storage);
				
				//TODO extract names from XML;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determines if the object can be carried, type and weight will be checked
	 * @return can or cannot
	 */
	public boolean canCarryItem(ICarryable item){
		if(item == null) return false; 
		
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);
			
			if(	storage != null && storage.canAddItem(item) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if the object type can be carried
	 * @param type
	 * @return can or cannot
	 */
	public boolean canCarryType(String type){
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);
			
			if(storage != null && storage.getType().equals(type) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the inventory with the given type can carry the extra weight
	 * @return can store extra weight
	 */
	public boolean canCarryExtraWeight(String type, int weight){
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);
			
			if(	storage != null && 
				storage.getType().equals(type) && 
				(storage.getCurrentWeight() + weight) <= storage.getMaxWeight() )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Adds an item to the inventory, if it can be added!
	 * @param item
	 * @return
	 */
	public void carryItem(ICarryable item){
		String type = item.getType();
		
		if(!canCarryType(type)){
			
			CommandDisplay commandDisplay = new CommandDisplay();
			commandDisplay.setTekst("You can't carry this type of items!");
			commandDisplay.addCommand("Back", new ClearCommandDisplay());
			commandDisplay.setLocation(location);
			Game.getInstance().setCommandDisplay(commandDisplay);
			return;
			
		}else if(!canCarryExtraWeight(item.getType(), item.getWeight())){
			
			CommandDisplay commandDisplay = new CommandDisplay();
			commandDisplay.setTekst("You can't carry this item, it's too heavy!");
			commandDisplay.addCommand("Back", new ClearCommandDisplay());
			commandDisplay.setLocation(location);
			Game.getInstance().setCommandDisplay(commandDisplay);
			return;
			
		}
		
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);

			//add item to the inventory, if possible
			if(	storage != null && storage.addItem(item) ){
				
				item.getRoom().unregisterObject(item);
				
				//insert the unique id of this organism as the carrier into the XML
				item.setCarrier( getIDString() );
				
				Game.getInstance().playSound("pickup.wav");
				
				//add it to only one inventory, if here means one is added, so break
				break;
			}
		}
	}
	
	/**
	 * Returns the current id of the textline
	 * 
	 * @return string id of the textline
	 */
	public String getTextlineId(){
		Node textline = objectState.getAttributes().getNamedItem("currenttextline");
		if(textline == null){
			System.out.println("WARNING: textline attribute not found, creating textline and setting to 0" );
			textline = objectState.getOwnerDocument().createAttribute("currenttextline");
			textline.setNodeValue("");
			objectState.getAttributes().setNamedItem(textline);
		}
		
		return textline.getNodeValue();
	}
	
	/**
	 * Gets the inventory at a certain index
	 * 
	 * @param index 
	 * @return Inventory || null
	 */
	public Inventory getInventory(int index){
		if(index >= 0 && index < inventorys.size() ){
			return inventorys.get(index);
		}
		return null;
	}
	
	/**
	 * Returns all the inventory's this organism holds
	 * 
	 * @return all inventorys this organism holds
	 */
	public ArrayList<Inventory> getInventorys(){
		return inventorys;
	}
	
	/**
	 * Returns the inventory of the given type, if this organism holds it. otherwise null
	 * 
	 * @param type of inventory
	 * @return Inventory of type or null
	 */
	public Inventory getInventoryOfType(String type){
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);

			if(	storage != null && storage.getType().equals(type)){
				return storage;
			}
		}
		return null;
	}
	
	/**
	 * Returns true if the item is in one of the users inventories
	 * @param item
	 * @return
	 */
	public boolean hasInInventory(ICarryable item){
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);
			if(storage.contains(item))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Removes the item from the inventory
	 * @return
	 */
	public void dropItem(ICarryable item){
		for(int i = 0; i < inventorys.size(); i++){
			Inventory storage = inventorys.get(i);
			
			if(storage.contains(item)){
				storage.removeItem(item);
				
				//drop the item on this spot!
				item.setLocation(new Point(location.getX()+item.getSize().width, location.getY()+item.getSize().y ));
				
				//register the item back into the room
				item.setRoom(getRoom());
				
				//update the xml to signal that the object is no longer carried!
				item.clearCarrier();
				
				//update the inventory, if present!
				Game.getInstance().updateCurrentInventoryWindow();
				
				return;
			}
		}
	}
	
	/**
	 * Makes the object talk
	 */
	public void talk(){
		System.out.println("Talk Triggered");
		String textline = Translator.getTextline(objectState, Game.getInstance().getLanguage(), getTextlineId());
		
		CommandDisplay commandDisplay = new CommandDisplay();
		commandDisplay.setTekst(textline);
		commandDisplay.addCommand("...", new ClearCommandDisplay());
		commandDisplay.setLocation(location);
		
		Game.getInstance().setCommandDisplay(commandDisplay);
		Game.getInstance().redraw();
		
		new InvokedEvent(this, this, "talk");
	}
	
	/**
	 * Sets the textline id of this object
	 * 
	 * @param id id of the textline in the xml
	 */
	public void setTextline(String id){
		Node textline = objectState.getAttributes().getNamedItem("currenttextline");
		
		if(textline == null){
			System.out.println("WARNING: textline attribute not found, creating textline and setting to 0" );
			textline = objectState.getOwnerDocument().createAttribute("currenttextline");
			objectState.getAttributes().setNamedItem(textline);
		}
		
		textline.setNodeValue(id);
	}

}
