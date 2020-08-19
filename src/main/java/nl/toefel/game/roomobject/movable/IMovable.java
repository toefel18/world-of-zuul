package nl.toefel.game.roomobject.movable;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

//import java.util.ArrayList;
import nl.toefel.game.Game;
import nl.toefel.game.InvokedEvent;
import nl.toefel.game.command.*;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.roomobject.Door;
import nl.toefel.game.roomobject.Point;
import nl.toefel.game.roomobject.IRoomObject;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public abstract class IMovable extends IRoomObject {

	/** contains four walking direction images, with 2 states per direction*/
	protected String[][] imagePaths = new String[4][2];
	
	/** the current index in the imagePaths array*/
	protected int imageIndex = 0;
	
	/** the speed in which the object will move in one direction*/
	protected int velocity = 1;
	
	/** Holds the velocity node */
	protected Node velocityNode = null;
	
	/** change the image after x times walk in same direction*/
	protected int changeFrequency = 1;
	
	/** steps in same direction counter*/
	protected int stepCounter = 0;
	
	/** used for creating the walking effect*/
	protected boolean imageSwitcher = false;
		
	/** Movement directions */
	public static final int NORTH = 1;
	public static final int SOUTH = 2;
	public static final int EAST = 3;
	public static final int WEST = 4;
	
	/** Last move direction*/
	protected int lastMoveDirection = SOUTH;
	
	public IMovable(Node state) {
		super(state);
		
		//check for optional changefrequency value
		Node changeFrequencyNode = objectState.getAttributes().getNamedItem("walkimagefrequency");
		if(changeFrequencyNode != null){
			String changeFrequencyString = changeFrequencyNode.getNodeValue();
			
			if(isNumeric(changeFrequencyString))
				changeFrequency = Integer.valueOf(changeFrequencyString);
		}
			
		//check for the velocity, if set!
		Node velocityNode = objectState.getAttributes().getNamedItem("velocity");
		if(velocityNode != null){
			this.velocityNode = velocityNode;
			String velocityString = velocityNode.getNodeValue();
			
			if(isNumeric(velocityString)){
				velocity = Integer.valueOf(velocityString);
			}else{
				velocityNode.setNodeValue("1");
			}
		}else{
			//create the velocity node
			velocityNode = objectState.getOwnerDocument().createAttribute("velocity");
			velocityNode.setNodeValue("1");
			objectState.getAttributes().setNamedItem(velocityNode);
			this.velocityNode = velocityNode;
		}
	}
	
	/**
	 * Checks if any collisions will occured when this IMoveable will be placed on the given point.
	 * 
	 * @param newLocation The point where the IMoveable will be 'placed' to test for collision.
	 * @return
	 */
	private boolean hasCollision(Point newLocation)
	{
		Point oldLocation = location;
		
		location = newLocation;
		
		determineCollision();
		
		boolean result = false;
		
		// Check collision with Room.
		Area testRoomCollision = (Area) collision.clone();
		
		for(Rectangle collision : getRoom().getCollisions())
		{
			if(testRoomCollision.intersects(collision))
			{
				location = oldLocation;
				
				return true;
			}
		}
		
		if(getCollidingObjects().size() > 0)
			result = true;
		
		location = oldLocation;
		
		return result;
	}
	
	/**
	 * Returns all IRoomObject's which are colliding with this IRoomObject.
	 * @return
	 */
	public ArrayList<IRoomObject> getCollidingObjects()
	{
		ArrayList<IRoomObject> collidingObjects = new ArrayList<IRoomObject>();
		
		for(IRoomObject roomObject : this.getRoom().getRoomObjects())
		{
			if(this == roomObject)
				continue;
			
			Area testCollision = (Area) collision.clone();
			
			testCollision.intersect(roomObject.getCollision());
			
			if(!testCollision.isEmpty())
				collidingObjects.add(roomObject);
		}
		
		return collidingObjects;
	}
	
	/**
	 * Returns the speed at which this object should currently be moving
	 * 
	 * @return moving speed
	 */
	public int getVelocity(){
		return velocity;
	}
	
	/**
	 * moves the object in the x and y direction this call should
	 * only be done from within a Move command. Do not call this 
	 * method directly!
	 * 
	 * @param disposition the values to move from the current point
	 */
	public void move(Point disposition){	
		
		// Determine new location.
		Point newLocation = new Point(location.getX() + disposition.getX(), location.getY() + disposition.getY());
		
		if(hasCollision(newLocation))
		{
			for(IRoomObject roomObject : getCollidingObjects())
				new InvokedEvent(this, roomObject, "collision");
			
			updateImage(disposition);
			return;
		}
		
		location = newLocation;
		
		// @note Is unessecary?
		//determineCollision();
		
		/*
		//TODO REMOVE REMOVE, DEBUG INFO FOR TESTING, THIS WILL RAISE EXCEPTIONS (nullpointer) WHEN USED WHILE NORMAL OPERATIONS
		if(location.getX() > 250)	
			Game.getInstance().getCommandDisplay().setSelected(0);
		else
			Game.getInstance().getCommandDisplay().setSelected(1);
		*/
		
	//	checkCommands();
		
		determineBoundaries();
		
		//update the imagepath
		updateImage(disposition);
		
		//Game.getInstance().notifyDrawers(); //causes this thread to actually start redrawing
		Game.getInstance().redraw(); // invokes the drawing processors drawing mechanism
	
		//TODO this should not be written every move!! this should only be done on a save!!
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		
		Node xPos = attributes.getNamedItem("x");
		Node yPos = attributes.getNamedItem("y");
		
		xPos.setNodeValue("" + location.getX());
		yPos.setNodeValue("" + location.getY());
	}
	
	/**
	 * Collects and displays all Command's which this IMovable could execute on
	 * other IRoomObject's which are in the command range of those.
	 */
	public void showCommandInteractions()
	{
		HashMap<IRoomObject, ArrayList<ICommand>> usableCommands = new HashMap<IRoomObject, ArrayList<ICommand>>();
				
		// Collect all usable commands.
		for(IRoomObject roomObject : getRoom().getRoomObjects())
		{
			if(roomObject.inCommandRange(this))
			{
				System.out.println("Incommand range");
				
				ArrayList<ICommand> objectCommands = roomObject.getUsableCommands(this);
				
				if(objectCommands == null) 
					continue;
				
				for(ICommand command : objectCommands)
				{
					if(!usableCommands.containsKey(roomObject))
						usableCommands.put(roomObject, new ArrayList<ICommand>());
					
					usableCommands.get(roomObject).add(command);
				}
			}
		}
		
		if(usableCommands.size() == 0)
		{
			System.out.println("No commands");
			
			//Game.getInstance().clearCommandDisplay(); //this is erroneous
			Game.getInstance().redraw();
			return;
		}
		
		CommandDisplay commandDisplay = new CommandDisplay();
		commandDisplay.setTekst("Choose a command:");
		
		Iterator<IRoomObject> commandIterator = usableCommands.keySet().iterator();
		
		// add all usable commands to the command display, and show the ID of the command (currently for debug use)
		while(commandIterator.hasNext())
		{
			IRoomObject 		roomObject 	= commandIterator.next();
			ArrayList<ICommand> commands 	= usableCommands.get(roomObject);
			
			for(ICommand command : commands){
				if(command != null)
					commandDisplay.addCommand("(" + roomObject.getIDString() + ") " + command.getName(), command);
			}
		}
		
		commandDisplay.setSelected(0);
		commandDisplay.setLocation(this.getLocation());
		Game.getInstance().setCommandDisplay(commandDisplay);
		Game.getInstance().redraw();
	}
	
	/**
	 * Sets the correct image for the moving object
	 * 
	 * @param disposition the disposition in which the movable should be moving, this is used to determine the correct sprite
	 */
	public void updateImage(Point disposition){
	
		if(disposition.getX() > 0){			//movement east
			if(lastMoveDirection == EAST)
				checkImageSwitch();
			else
				setFaceDirection( EAST );
			
		}else if(disposition.getX() < 0){	//movement west
			if(lastMoveDirection == WEST)
				checkImageSwitch();
			else
				setFaceDirection(WEST);
			
		}else if(disposition.getY() > 0){	//movement south
			if(lastMoveDirection == SOUTH)
				checkImageSwitch();
			else
				setFaceDirection(SOUTH);
			
		}else if(disposition.getY() < 0){	//movement north
			if(lastMoveDirection == NORTH)
				checkImageSwitch();
			else
				setFaceDirection(NORTH);
			
		}		
	}
	
	/**
	 * Checks if an image switch should be performed, if so, it does
	 */
	protected void checkImageSwitch(){
		if(stepCounter++ % changeFrequency == 0){
			imageSwitcher = !imageSwitcher;
			stepCounter = 1;
		}		
	}
	
	/**
	 * Returns the filename of the image.
	 * 
	 * @return String The filename of the image.
	 */
	public String getImage()
	{
		if(imageSwitcher)
			return imagePaths[imageIndex][0];
		else
			return imagePaths[imageIndex][1]; 
	}
	
	/**
	 * 
	 * Obtain all used images of the player and register them to Game.
	 * Images should be in the following format:
	 * since there are 4 directions, each direction has 2 different images: walk
	 * and normal. These are used to create the walking effect it should be in this format
	 * inside the direction:   	00.png  01.png for the right direction
	 * 							10.png  11.png for the bottom direction, etc
	 * 
	 * TODO MOVE THIS CODE TO PLAYER?? NOW EVERY IMOVABLE HAS TO HAVE ALL THOSE IMAGES!
	 */
	protected void registerImages()
	{
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		Node imageNode = attributes.getNamedItem("image");
		String imageDir = imageNode.getNodeValue();
		
		if(imageDir.endsWith("/")){
			Game game = Game.getInstance();
			for(int i = 0; i < 4; i++){
				//this could be another loop, but for optimalisation: loop unrolling!
				imagePaths[i][0] = imageDir + i + "0.png";
				imagePaths[i][1] = imageDir + i + "1.png";
				
				game.registerImage(imagePaths[i][0]);
				game.registerImage(imagePaths[i][1]);
			}
		}else{
			System.out.println("DEBUG-INFO: player image attributed should be a directory, and end with a slash");
		}
	}
	
	/**
	 * Sets the face of the movable
	 * 
	 * @param  IMovable:: NORTH | SOUTH | EAST | WEST
	 */
	public void setFaceDirection(int direction){
		switch(direction){
		case NORTH:
			lastMoveDirection = NORTH;
			imageIndex = 3;
			imageSwitcher = false;
			break;
		case SOUTH:
			lastMoveDirection = SOUTH;
			imageIndex = 1;
			imageSwitcher = false;
			break;
		case EAST:
			lastMoveDirection = EAST;
			imageIndex = 0;
			imageSwitcher = false;
			break;
		case WEST:
			lastMoveDirection = WEST;
			imageIndex = 2;
			imageSwitcher = false;
			break;
		}
	}
	 
	/**
	 * Sets the location of the movable object
	 * 
	 * @param newLocation the new location
	 */
	public void setLocation(Point newLocation)
	{
		location = newLocation;
		
		// Execute move to adjust to the new envorniment.
		move(new Point(0, 0));
	}
	
	/**
	 * Sets the new location, and and updates 
	 * 
	 * @param newLocation the new location
	 * @param direction the direction in whi
	 */
	public void setLocation(Point newLocation, int direction)
	{
		setLocation(newLocation);
		setFaceDirection(direction);
	}
	
	/**
	 * Sets the velocity of the player (how fast it will move in one direction)
	 * 
	 * @param velocity of this movable
	 */
	public void setVelocity(int velocity){
		this.velocity = velocity;
	}
}
