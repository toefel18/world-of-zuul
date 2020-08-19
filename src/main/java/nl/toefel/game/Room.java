package nl.toefel.game;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import nl.toefel.game.roomobject.*;

/**
 * Is a Room.
 */
public class Room {
	/** Contains all the IRoomObjects. */
	private ArrayList<IRoomObject> roomObjects = new ArrayList<IRoomObject>();
	
	/** Contains the state. */
	private Node objectState;
	
	/** Contains the size. */
	private Rectangle size;
	
	/** Contains the collisions. */
	private ArrayList<Rectangle> collisions = new ArrayList<Rectangle>();
	
	/**
	 * Constructor which sets the begin state.	 
	 * 
	 * TODO Discuss about the possible need of a validator.
	 * 
	 * @param state The begin state of the Room.
	 */
	public Room(Node state)
	{
		this.objectState = state;
			
		this.size		=  new Rectangle(
											0,
											0,
											Integer.valueOf(state.getAttributes().getNamedItem("w").getNodeValue()),
											Integer.valueOf(state.getAttributes().getNamedItem("h").getNodeValue())
										); 
		
		registerImages();
		
		determineCollisions();
	}
	
	/**
	 * Returns the unique identifier of this Room.
	 * 
	 * @return The unique identifier of this Room.
	 */
	public String getID()
	{
		return objectState.getAttributes().getNamedItem("id").getNodeValue();
	}
	
	/**
	 * Obtain all used images and register them to Game.
	 * 
	 * TODO To discuss.
	 * TODO Add to UML.
	 */
	private void registerImages()
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		Game game = Game.getInstance();
		
		try {
			// Select image attributes.
			NodeList images = (NodeList) xPath.compile("@image").evaluate(objectState, XPathConstants.NODESET);
			
			// Register all images to Game.
			for(int i = 0, max = images.getLength(); i < max; i++)
				game.registerImage(images.item(i).getNodeValue());
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Returns the filename of the image.
	 * 
	 * @return String The filename of the image.
	 */
	public String getImage()
	{
		return objectState.getAttributes().getNamedItem("image").getNodeValue(); 
	}
	
	public Rectangle getSize()
	{
		return size;
	}
	
	/**
	 * Returns all the RoomObjects in this room.
	 * 
	 * @return All the RoomObjects in this room.
	 */
	public ArrayList<IRoomObject> getRoomObjects(){
		return roomObjects;
	}
	
	/**
	 * Returns all the RoomObjects in this room which hit the given point.
	 * 
	 * @return RoomObjects.
	 */
	public ArrayList<IRoomObject> getRoomObjectsAtPoint(Point point)
	{
		ArrayList<IRoomObject> roomObjects = new ArrayList<IRoomObject>();
		
		for(IRoomObject roomObject : this.roomObjects)
		{
			if(roomObject.hitTest(point))
				roomObjects.add(roomObject);
		}
		
		return roomObjects;
	}
	
	private void determineCollisions()
	{
		collisions.clear();
		
		collisions.add(new Rectangle(-10,		 	0, 				10, 			size.height		));
		collisions.add(new Rectangle(0, 			-10, 			size.width, 	10				));
		collisions.add(new Rectangle(size.width, 	0, 				10, 			size.height		));
		collisions.add(new Rectangle(0, 			size.height,	size.width, 	10				));
	}
	
	public ArrayList<Rectangle> getCollisions()
	{
		return collisions;
	}

	/**
	 * Sorts the RoomObjects based on their position on the Y-axis.
	 */
	public void sortRoomObjects()
	{
		synchronized(this)
		{
			Collections.sort(roomObjects, new RoomObjectComparator());
		}
	}
	
	/**
	 * Registers the given IRoomObject.
	 * 
	 * @param roomObject The object to be registered.
	 */
	public void registerObject(IRoomObject roomObject) {
		roomObjects.add(roomObject);
		
		sortRoomObjects();
	}
	
	public String getSoundFile(){
		NamedNodeMap attributes = (NamedNodeMap)objectState.getAttributes();
		Node music = attributes.getNamedItem("music");
		
		if(music != null)
			return music.getNodeValue();
		
		return "";
	}
	
	/**
	 * Unregisters the given IRoomObject.
	 * 
	 * @param roomObject The object to be unregistered.
	 */
	public void unregisterObject(IRoomObject roomObject) {
		roomObjects.remove(roomObject);
	}
}
