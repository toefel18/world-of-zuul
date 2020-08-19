package nl.toefel.game.roomobject;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RectangularShape;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.*;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import nl.toefel.game.Game;
import nl.toefel.game.Room;
import nl.toefel.game.Translator;
import nl.toefel.game.command.*;
import nl.toefel.game.roomobject.movable.IMovable;

/**
 * Is an room object.
 * 
 * TODO To implement.
 */
public abstract class IRoomObject {
	
	/** Contains the state. */
	protected Node objectState;
	
	/** Contains the object location*/
	protected Point location;
	
	/** Contains the object size*/
	protected Rectangle size;
	
	/** Contains the z-order. */
	protected int zOrder = 0;
	
	/** Contains all apart collisions. */
	protected ArrayList<RectangularShape> collisions = new ArrayList<RectangularShape>();
	
	/** Contains all apart collisions absolute to top left of the room. */
	protected ArrayList<RectangularShape> absoluteCollisions = new ArrayList<RectangularShape>();	
	
	/** Contains all boundaries. */
	protected ArrayList<RectangularShape> originalBoundaries = new ArrayList<RectangularShape>();
	
	/** Contains all apart boundaries. */
	protected ArrayList<RectangularShape> boundaries = new ArrayList<RectangularShape>();
	
	/** Contains the sum of all collisions. */
	protected Area collision = new Area();
	
	/** Contains the command radius*/
	protected int commandRange;
	
	/** Contains the object 'unique' id*/
	protected String id;
	
	/** Contains the image path*/
	protected String image;
	
	/** Contains an array of animation images if this object is animated */
	protected ArrayList<String> animationImages = new ArrayList<String>();
	
	/** Contains the current animation image index if this object is animated*/
	protected int currentAnimationImage = 0;
	
	/** Contains the interval for the image to switch*/
	protected int animationInterval = 0;
	
	/** Contains the current ms since last change*/
	protected int currentAnimationInterval = 0;
	
	/** Holds true if this object is animated, false otherwise*/
	protected boolean isAnimated = false;
	
	/** Contains if the object is visible or not. **/
	protected boolean visible = true;
	
	/** Contains the name of the object in-game, if specified*/
	protected String inGameName = "";
	
	/** Contains if this should be visually be highligted or not. */
	private boolean highlight = false;
	
	/**
	 * Contains the Room wherein this instance resides.
	 * 
	 * @note Deprecated?
	 * TODO To discuss.
	 * TODO Add to UML. 
	 **/
	protected Room room;
	
	/**
	 * Constructor which sets the begin state.	 
	 * 
	 * TODO Discuss about the possible need of a validator.
	 * 
	 * @param Element state The begin state of the Room.
	 */
	public IRoomObject(Node state)
	{
		this.objectState = state;
		
		update();
		
		loadBoundaries();
		loadCollisions();
		
		determineBoundaries();
		determineCollision();
		
		inGameName = Translator.getTranslation(objectState, Game.getInstance().getLanguage(), id);
	}
	
	/**
	 * Reads the object values from the XML node. Do not use this function
	 * repeated often, if you need to update one particular property, write
	 * an updateProperty function, that will be faster and less error prone.
	 * 
	 */
	public void update(){
		if(objectState == null) return;
		NamedNodeMap attributes = (NamedNodeMap) objectState.getAttributes();
		Node zorderNode = attributes.getNamedItem("zorder");
		
		this.location 	= new Point(Integer.valueOf(attributes.getNamedItem("x").getNodeValue()), Integer.valueOf(attributes.getNamedItem("y").getNodeValue()));
		this.size 		= new Rectangle(0, 0, Integer.valueOf(attributes.getNamedItem("w").getNodeValue()), Integer.valueOf(attributes.getNamedItem("h").getNodeValue()));
		this.id 		= attributes.getNamedItem("id").getNodeValue();
		

		Node attribute = objectState.getAttributes().getNamedItem("commandrange");
		
		if(attribute == null)
			commandRange = 0;
		else
			commandRange = Integer.valueOf(attribute.getNodeValue());

		this.image 		= attributes.getNamedItem("image") != null ? attributes.getNamedItem("image").getNodeValue() : null;
		
		this.zOrder 	= zorderNode == null ? 1 : Integer.valueOf(zorderNode.getNodeValue());
		loadAnimatedImages();
		
	}
	
	protected void addBoundary(RectangularShape boundary)
	{
		originalBoundaries.add(boundary);
	}
	
	public ArrayList<RectangularShape> getBoundaries()
	{
		return boundaries;
	}
	
	/*
	 * Loads the animated images into the game, prepares this object for image switching and registers it to the animationprocessor!
	 */
	protected void loadAnimatedImages(){
		if(objectState == null) return;
		NamedNodeMap attributes = objectState.getAttributes();
		if(attributes == null) return;
		
		Node isAnimatedNode = attributes.getNamedItem("animated");
		Node animationFrequencyNode = attributes.getNamedItem("animationfrequency");
		Node animationImagesNode = attributes.getNamedItem("animationimages");
		
		if(isAnimatedNode != null && animationFrequencyNode != null && animationImagesNode != null)
		{
			if(isAnimatedNode.getNodeValue().equals("true"))
			{
				//read the animation interval, if not found, return and this object is not animated!!
				String animationFrequency = animationFrequencyNode.getNodeValue();
				
				if(!isNumeric(animationFrequency))
				{
					System.err.println("ERROR: Animation frequency not found while object is marked animated, object id: " + getIDString());
					return;
				}
				
				this.animationInterval = Integer.valueOf(animationFrequency);
				
				String animatedImagesPath = animationImagesNode.getNodeValue();
				
				if(!animatedImagesPath.endsWith("/"))
				{
					System.err.println("ERROR: animatedimages path doesn't end with / while object is marked animated, object id: " + getIDString());
					return;
				}
				
				Game game = Game.getInstance();
				//animatedImagesPath = "game/games/" + game.getWorldName() + "/images/" + animatedImagesPath;
				File imagesPath = new File( Game.gameDataDir() + "/games/" + game.getWorldName() + "/images/" + animatedImagesPath );
				
				if( !imagesPath.exists() )
				{
					System.err.println("ERROR: animatedimages path doesn't exist in game specific folder(required!), object id: " + getIDString() + ", path: " + animatedImagesPath);
					return;
				}
				
				//filter to accept only valid images
			    FilenameFilter filter = new FilenameFilter() {
			        public boolean accept(File dir, String name) {
			    		Pattern validImageFile = Pattern.compile("^[0-9]+\\.(png|jpg|jpeg|gif)$");
			    		Matcher check = validImageFile.matcher(name);
			    		return check.find();
			        }
			    };
			    
			    File[] files = imagesPath.listFiles(filter);	
				
				if (files.length < 0){
					System.err.println("ERROR: no images found to animate the object with while object is marked animated, objectid: " + getIDString());
					return;
				}
				
				int counter = 0;
				
				while(true){
					Pattern neededImage = Pattern.compile("^" + counter + "\\.(png|jpg|jpeg|gif)$");
					int compare = counter;
					for(int i = 0; i < files.length; i++){
						String filename = files[i].getName();
						Matcher match = neededImage.matcher(filename);
						if(match.find()){
							animationImages.add(animatedImagesPath + filename);
							counter++;
							break;
						}
					}
					
					if(compare == counter) break;
				}
				//no images found
			    if(animationImages.size() <= 0){
			    	System.err.println("ERROR: no images found to animate the object with while object is marked animated, objectid: " + getIDString());
			    	return;
			    }
			    
			    //load the images in game
			    for(String img: animationImages){
			    	game.registerImage(img);
			    }
			    
			    image = animationImages.get(0);
			    
				//this should only be done if the rest is OK!
				this.isAnimated = true;
				Game.getInstance().registerAnimatedObject(this);
			}
		}
	}
	
	protected void loadBoundaries()
	{
		boundaries.clear();
		//originalBoundaries.clear();
		
		try {
			NodeList boundaryNodes = (NodeList) XPathFactory.newInstance().newXPath().compile("boundaries/*").evaluate(objectState, XPathConstants.NODESET);
			
			for(int i = 0, max = boundaryNodes.getLength(); i < max; i++)
			{	
				Node boundaryNode = boundaryNodes.item(i);
				NamedNodeMap boundaryAttributes = boundaryNodes.item(i).getAttributes();
				
				RectangularShape boundaryShape = null;
				
				//System.out.println(Integer.valueOf(boundaryAttributes.getNamedItem("y").getNodeValue()) + " : " + size.height);
				
				int test = 0 - size.height + Integer.valueOf(boundaryAttributes.getNamedItem("h").getNodeValue()) + Integer.valueOf(boundaryAttributes.getNamedItem("y").getNodeValue());
				
				//System.out.println(test);
				
				if(boundaryNode.getNodeName() == "rectangle")
					boundaryShape = new Rectangle(
														Integer.valueOf(boundaryAttributes.getNamedItem("x").getNodeValue()),
														test,
														Integer.valueOf(boundaryAttributes.getNamedItem("w").getNodeValue()),
														Integer.valueOf(boundaryAttributes.getNamedItem("h").getNodeValue())
													);
				
					
				else
					System.out.println("Error: Unkown boundary shape in IRoomObject #" + id);
				
				if(boundaryShape != null)
					originalBoundaries.add(boundaryShape);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void loadCollisions()
	{
		collisions.clear();
		
		try {
			NodeList collisionNodes = (NodeList) XPathFactory.newInstance().newXPath().compile("collisions/*").evaluate(objectState, XPathConstants.NODESET);
			
			for(int i = 0, max = collisionNodes.getLength(); i < max; i++)
			{	
				Node collisionNode = collisionNodes.item(i);
				NamedNodeMap collisionAttributes = collisionNodes.item(i).getAttributes();
				
				RectangularShape collisionShape = null;
				
				//System.out.println(Integer.valueOf(collisionAttributes.getNamedItem("y").getNodeValue()) + " : " + size.height);
				
				int test = 0 - size.height + Integer.valueOf(collisionAttributes.getNamedItem("h").getNodeValue()) + Integer.valueOf(collisionAttributes.getNamedItem("y").getNodeValue());
				
				//System.out.println(test);
				
				if(collisionNode.getNodeName() == "rectangle")
					collisionShape = new Rectangle(
														Integer.valueOf(collisionAttributes.getNamedItem("x").getNodeValue()),
														test,
														Integer.valueOf(collisionAttributes.getNamedItem("w").getNodeValue()),
														Integer.valueOf(collisionAttributes.getNamedItem("h").getNodeValue())
													);
				
					
				else
					System.out.println("Error: Unkown collision shape in IRoomObject #" + id);
				
				if(collisionShape != null)
					collisions.add(collisionShape);
				
				Node isBoundary = collisionAttributes.getNamedItem("isboundary");
				
				if(isBoundary != null && isBoundary.getNodeValue().equals("true"))
					addBoundary(collisionShape);
			}

			if(collisionNodes.getLength() == 0 && (objectState.getAttributes().getNamedItem("collision") == null || !objectState.getAttributes().getNamedItem("collision").getNodeValue().equals("false")))
			{
				Rectangle collision = new Rectangle(
														0,
														0,
														size.width,
														size.height
													);
				
				collisions.add(collision);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		determineCollision();
	}
	
	protected void determineCollision()
	{
		collision.reset();
		absoluteCollisions.clear();
		
		for(RectangularShape collision : collisions)
		{
			RectangularShape updatedCollision = (RectangularShape) collision.clone();
			
			updatedCollision.setFrame(
										location.getX() - (size.width / 2) + collision.getX(),
										location.getY() - size.height - collision.getY(),
										updatedCollision.getWidth(),
										updatedCollision.getHeight()
									);

			absoluteCollisions.add(updatedCollision);
			this.collision.add(new Area(updatedCollision));
		}
	}
	
	/**
	 * Checks if the text is pure numeric (integer)
	 * @param text to test on numeric value
	 * @return text == numeric
	 */
	public boolean isNumeric(String text){
		Pattern numeric = Pattern.compile("^[0-9]+$");
		Matcher check = numeric.matcher(text);
		boolean result = check.find();
		return result;
	}
	
	protected void determineBoundaries()
	{
		boundaries.clear();
		
		if(originalBoundaries.size() == 0)
		{
			Rectangle boundary = new Rectangle(
													0,
													0,
													size.width,
													size.height
												);
			
			originalBoundaries.add(boundary);
		}
		
		for(RectangularShape boundary : originalBoundaries)
		{
			RectangularShape updatedBoundary = (RectangularShape) boundary.clone();
			
			updatedBoundary.setFrame(
										location.getX() - (size.width / 2) + boundary.getX(),
										location.getY() - size.height - boundary.getY(),
										boundary.getWidth(),
										boundary.getHeight()
									);

			boundaries.add(updatedBoundary);
		}
	}

	/**
	 * 
	 * @todo	Implement
	 * @param 	roomObject
	 * @return
	 */
	public boolean inRange(IRoomObject roomObject)
	{
		Rectangle boundariesThis = new Rectangle(
													this.location.getX() - (this.size.width / 2) ,
													this.location.getY() -  this.size.height,
													this.size.width,
													this.size.height
												);
		
		Rectangle boundariesThat = new Rectangle(
													roomObject.getLocation().getX() - (roomObject.getSize().width / 2),
													roomObject.getLocation().getY() -  roomObject.getSize().height,
													roomObject.getSize().width,
													roomObject.getSize().height
												);
		
		return boundariesThis.intersects(boundariesThat) ? true : false;  
	}

	public boolean inCommandRange(IRoomObject initiator)
	{
		if(initiator == this)
			return false;
		
		int commandRange = getCommandRange() + 2;
		
		// Fast and global check (optimization).
		Rectangle boundariesThis = new Rectangle(
														this.location.getX() - commandRange - (this.size.width / 2),
														this.location.getY() - commandRange -  this.size.height,
														this.size.width 	+ (commandRange * 2),
														this.size.height 	+ (commandRange * 2)
													);

		Point initiatorLocation 		= initiator.getLocation();
		Rectangle initiatorSize 	= initiator.getSize();
		int initiatorCommandRange 	= initiator.getCommandRange();
		
		Rectangle boundariesThat = new Rectangle(
													initiatorLocation.getX() - initiatorCommandRange - (initiatorSize.width / 2),
													initiatorLocation.getY() - initiatorCommandRange -  initiatorSize.height,
													initiatorSize.width  + (initiatorCommandRange * 2),
													initiatorSize.height + (initiatorCommandRange * 2)
												);
		
		if(boundariesThis.intersects(boundariesThat) == false)
			return false;
		
		return true;
	}
	
	/**
	 * Returns if this object is an animated object, and is succesfully loaded
	 * 
	 * @return true if animated, false otherwise
	 */
	public boolean getAnimated(){
		return isAnimated;
	}
	
	/**
	 * Returns an array with all the images the animation of this object uses
	 * 
	 * @return array with animated image paths
	 */
	public ArrayList<String> getAnimatedImages(){
		return animationImages;
	}
	
	/**
	 * Returns the range wherein IOrganisms can execute commands on this instance.
	 * 
	 * @return int The command range, zero on default.
	 */
	public int getCommandRange()
	{
		return commandRange;
	}
	
	/**
	 * Returns all the collisions.
	 */
	public Area getCollision()
	{
		return collision;
	}
	
	public ArrayList<RectangularShape> getAbsoluteCollisions()
	{
		return absoluteCollisions;
	}
	
	public ArrayList<RectangularShape> getCollisions()
	{
		return collisions;
	}

	/**
	 * Returns the unique identifier as integer.
	 * @deprecated this should not be used, id's can be strings!
	 * TODO review this deprecated function and verify that unique id's are really strings!!!
	 * @return int The unique identifier.
	 */
	public int getID()
	{
		return Integer.getInteger(objectState.getAttributes().getNamedItem("id").getNodeValue()); 
	}
	
	/**
	 * Returns the unique identifier as string
	 * @return the unique identifying string of this object
	 */
	public String getIDString(){
		//return objectState.getAttributes().getNamedItem("id").getNodeValue();
		return id;
	}
	
	/**
	 * Returns the language depended name of the object to be used in game texts.
	 * returns uniqueid if not found! WARNING: to get the unique id use getIDString()
	 * 
	 * @return the name
	 */
	public String getName(){
		return this.inGameName; 
	}
	
	/**
	 * Returns if the object is visible or not.
	 */
	public boolean getVisible()
	{
		return visible;
	}
	
	/**
	 * Returns the filename of the image.
	 * 
	 * @return String The filename of the image.
	 */
	public String getImage()
	{
		//return objectState.getAttributes().getNamedItem("image").getNodeValue();
		return image;
	}
	
	/**
	 * Returns the current location.
	 * 
	 * @return Point The current location.
	 */
	public Point getLocation()
	{
		//return new Point(Integer.valueOf(objectState.getAttributes().getNamedItem("x").getNodeValue()).intValue(), Integer.valueOf(objectState.getAttributes().getNamedItem("y").getNodeValue()).intValue()); 
		return location;
	}
	
	/**
	 * Returns the z-order.
	 * 
	 * @return int The z-order.
	 */
	public int getZOrder()
	{
		return zOrder;
	}
	/**
	 * Returns the room wherein this IRoomObject resides.
	 * 
	 * @return Room The room wherein this IRoomObject resides.
	 */
	
	public Room getRoom()
	{
		return Game.getInstance().getRoom(objectState.getAttributes().getNamedItem("room").getNodeValue()); 
	}
	
	
	/**
	 * Returns the size.
	 * 
	 * @return Point The size.
	 */
	public Rectangle getSize()
	{
		//return new Point(Integer.getInteger(objectState.getAttributes().getNamedItem("w").getNodeValue()), Integer.getInteger(objectState.getAttributes().getNamedItem("h").getNodeValue())); 
		return size;
	}
	
	/**
	 * Returns the usable command on this IRoomObject.
	 * 
	 * @param obj movable object
	 * @return ArrayList<Class <? extends ICommand>> The usable commands on this IRoomObject.
	 */
	public ArrayList<ICommand> getUsableCommands(IMovable obj){
		ArrayList<ICommand> usableCommands = new ArrayList<ICommand>();
		
		Game game = Game.getInstance();
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList commands;
		
		try {
			// Get the right XML nodes.
			commands = (NodeList) xPath.compile("useablecommands/command").evaluate(objectState, XPathConstants.NODESET);
		
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
				
				//create command with all parameters 
				ICommand command = game.getCommand(commandName, commandNode, obj, this);
				
				Node directlyExecuteNode = attributes.getNamedItem("directlyexecute"); 
				if(directlyExecuteNode != null){
					if(directlyExecuteNode.getNodeValue().equals("true")){
						command.execute();
						//TODO returns null if command is directly executed, should this be so?
						return null;
					}
				}
				
				if(command != null)
					usableCommands.add(command);
			}
		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return usableCommands;
	}
	/*{
		ArrayList<ICommand> usableCommands = new ArrayList<ICommand>();
		
		Game game = Game.getInstance();
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList commands;
		
		try {
			// Get the right XML nodes.
			commands = (NodeList) xPath.compile("useablecommands/command").evaluate(objectState, XPathConstants.NODESET);
		
			// TODO extract commandname from XML > Done, but not tested.
			// Fill the result with Command instances.
			for(int i = 0, max = commands.getLength(); i < max; i++)
				usableCommands.add(game.getCommand(commands.item(i).getFirstChild().getNodeValue(), commands.item(i)));
		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return usableCommands;
		
	}*/
	
	/**
	 * Returns the weight.
	 * 
	 * @return int The weight.
	 */
	public int getWeight()
	{
		return Integer.getInteger(objectState.getAttributes().getNamedItem("weight").getNodeValue()); 
	}
	
	/**
	 * Obtain all used images and register them to Game.
	 * 
	 * TODO To discuss.
	 * TODO Add to UML.
	 */
	protected void registerImages()
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		Game game = Game.getInstance();
		
		try {
			// Select image attributes.
			NodeList images = (NodeList) xPath.compile("descendant-or-self::*/@image").evaluate(objectState, XPathConstants.NODESET);

			// Register all images to Game.
			for(int i = 0, max = images.getLength(); i < max; i++)
				game.registerImage(images.item(i).getNodeValue());
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the current room wherein this instances resides.
	 * 
	 * TODO Discuss this moved method from Movable.
	 * TODO Add to UML.
	 * @param room Room wherein the instance resides.
	 */
	public void setRoom(Room newRoom)
	{
		// Store new room.
		objectState.getAttributes().getNamedItem("room").setNodeValue(newRoom.getID());
		
		// Switch room.
		getRoom().unregisterObject(this);
		newRoom.registerObject(this);
		
		loadBoundaries();
		loadCollisions();
	}
	
	/**
	 * Adds ms to the internal counter, if it is above the image switch 
	 * 
	 * @param ms
	 */
	public void updateAnimationInterval(int ms){
		if(isAnimated){
			currentAnimationInterval += ms;
			
			if(currentAnimationInterval >= animationInterval){
				//set the currentAnimationInterval back to 0 or add the remainder if some left!
				currentAnimationInterval -= animationInterval; 
				
				if(animationImages != null && animationImages.size() > 0){
					
					if(currentAnimationImage > (animationImages.size() - 1))
						currentAnimationImage = 0;
					
					image = animationImages.get(currentAnimationImage++);
					Game.getInstance().redraw();
				}
			}
		}
	}
	
	/**
	 * Tests if the given Point hits the IRoomObject.
	 * 
	 * @param point
	 * @return True if the given Point hits, false if not.
	 */
	public boolean hitTest(Point point)
	{
		Rectangle rectangle = new Rectangle(
												location.getX() - (size.width / 2),
												location.getY() -  size.height,
												size.width,
												size.height
											);
		
		return rectangle.contains(point.getX(), point.getY()) ? true : false;
	}
	
	/**
	 * Sets if the IRoomObject should be visually highlighted or not.
	 * 
	 * @param value	True if to highlight, false if not.
	 */
	public void setHighlight(boolean value)
	{
		highlight = value;
	}
	
	/**
	 * Returns if the IRoomObject should be visually highlighted or not.
	 * 
	 * @return	True if to highlight, false if not.
	 */
	public boolean getHighlight()
	{
		return highlight;
	}
	
	/**
	 * Sets the location to the given Point.
	 * @param newLocation
	 */
	public void setLocation(Point newLocation)
	{
		location = newLocation;
		
		// Update XMLNode.
		objectState.getAttributes().getNamedItem("x").setNodeValue(String.valueOf(newLocation.getX()));
		objectState.getAttributes().getNamedItem("y").setNodeValue(String.valueOf(newLocation.getY()));
	
		determineBoundaries();
		determineCollision();
	}
}
