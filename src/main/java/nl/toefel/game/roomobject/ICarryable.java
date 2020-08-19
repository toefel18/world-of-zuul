package nl.toefel.game.roomobject;

import java.util.HashMap;

import nl.toefel.game.Game;
import nl.toefel.game.Translator;
import nl.toefel.game.roomobject.IRoomObject;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Interface for carryable objects!
 */
public abstract class ICarryable extends IRoomObject {
	
	public ICarryable(Node state) {
		super(state);
		registerImages();
		
		//extract the name to be displayed in game, from the xml. if not found use unique id!
		
	}
	
	/**
	 * Gets the carrier, as identified by the XML
	 * 
	 * @return the id of the carrier
	 */
	public String getCarrier(){
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		
		Node carrier = attributes.getNamedItem("carrier");
		
		//no carrier attribute present yet, add!
		if(carrier == null){
			carrier = objectState.getOwnerDocument().createAttribute( "carrier" );
			carrier.setNodeValue("");
			//TODO, URGENT check: does this add attributes to the xml?
			attributes.setNamedItem(carrier);
		}
		
		return carrier.getNodeValue();
	}
	
	/**
	 * Returns the weight of the carryable object
	 */
	public int getWeight()
	{
		return Integer.valueOf(objectState.getAttributes().getNamedItem("weight").getNodeValue()); 
	}
	
	/**
	 * if the object was carried by an organism, this will remove it's presense from xml
	 */
	public void clearCarrier(){
		setCarrier("");
	}
	
	/**
	 * Sets the unique id of the person who is carrying this item to the carrier attribute
	 * @param id
	 */
	public void setCarrier(String id){
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		
		Node carrier = attributes.getNamedItem("carrier");
		
		//no carrier attribute present yet, add!
		if(carrier == null){
			carrier = objectState.getOwnerDocument().createAttribute( "carrier" );
			//TODO, URGENT check: does this add attributes to the xml?
			attributes.setNamedItem(carrier);
		}
		
		carrier.setNodeValue(id);
	}
	
	/**
	 * Sets the location of the carryable object (used when dropping items)
	 * 
	 * @param newLocation the new location
	 */
	public void setLocation(Point newLocation)
	{
		location = newLocation;
		
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		
		Node xPos = attributes.getNamedItem("x");
		Node yPos = attributes.getNamedItem("y");
		
		xPos.setNodeValue("" + location.getX());
		yPos.setNodeValue("" + location.getY());
	}
	
	/**
	 * Returns the type of the carryable object, this is needed for determining where to store it in the inventory
	 * 
	 * TODO read from XML!
	 * 
	 * @return item type
	 */
	public String getType(){		
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		if(attributes == null) return "";
		
		Node type = attributes.getNamedItem("type");
		
		if(type == null) return "";
				
		return type.getNodeValue();
	}

}
