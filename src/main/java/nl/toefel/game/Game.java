package nl.toefel.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.applet.AudioClip;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import nl.toefel.gameeditor.GameEditor;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import nl.toefel.game.command.*;
import nl.toefel.game.controller.IController;
import nl.toefel.game.controller.KeyController;
import nl.toefel.game.roomobject.*;
import nl.toefel.game.roomobject.movable.*;
import nl.toefel.game.sound.AePlayWave;
import nl.toefel.game.sound.AudioPlayer;
import nl.toefel.game.sound.MidiPlayer;
import nl.toefel.game.drawer.IDrawer;
import nl.toefel.game.drawer.SWTDrawer;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.gamedisplays.ComplexDisplay;
import nl.toefel.game.gamedisplays.ComplexDisplayTab;
import nl.toefel.game.ControlProcessor;

/**
 * Contains and controls all the parts in a game. Because there is
 * only one game, it is a singleton.
 */
public class Game {
	/** Regex for validating player name */
	public static final String PLAYER_NAME = "[a-zA-Z][a-zA-Z0-9]*"; //be aware that this is used below, and also will be used to check partial filename, don't allow illegal chars
	
	/** Regex for validating a whole string as a player name */
	public static final String VALID_PLAYER_NAME = "^" + PLAYER_NAME + "$"; 
	
	/** Extension for savegames */
	public static final String SAVE_GAME_EXTENSION = ".xml";
	
	/** Regex for extracting information out of the filename of a savegame*/
	public static final String SAVE_GAME_NAME = "^(" + PLAYER_NAME + ")_([0-9][0-9])u([0-9][0-9])m_([0-9][0-9])d([0-9][0-9])m([0-9][0-9][0-9][0-9])y" + SAVE_GAME_EXTENSION + "$";
	
	/** File that contains the configuration */
	public static final String SETTINGS_FILE = "settings.xml";
	
	/** Contains the current game version. */
	public static final String GAMEVERSION = "0.1"; 
	
	/** Default desired framerate, used when no framerate element is present in XML */ 
	public static final int DEFAULT_FRAMERATE = 40;
	
	/** Specifies the maximum message box width of an in-game message box*/
	public static final int MESSAGE_BOX_WIDTH = 250;
	
	/** Specifies the message box padding of the message text*/
	public static final int MESSAGE_BOX_PADDING = 10;
	
	/** Specifies the message box line spacing of the message text*/
	public static final int MESSAGE_BOX_LINE_SPACING = 8;	
	
	/** Specifies the message box line spacing of the message text*/
	public static final int MESSAGE_BOX_FONT_SIZE = 10;	
	
	/** Specifies the message box line spacing of the message text*/
	public static final String MESSAGE_BOX_FONT_NAME = "Verdana";	
	
	/** Specifies the width and height of the corner rounding */
	public static final int MESSAGE_BOX_CORNER_ROUNDING = 15;
	
	/** Specifies the padding used to draw the menu selector box around the text*/
	public static final int MESSAGE_BOX_MENU_PADDING = 5;
	
	/** Specifies the width and height of the selected menu item rounding box*/
	public static final int MESSAGE_BOX_MENU_ROUNDING = 10;	
	
	/** Specifies the margin between the border and screen image*/
	public static final int COMPLEX_DISPLAY_PADDING = 10;
	
	/** Specifies the width and height of the rounding factor of the selected inventory box*/
	public static final int COMPLEX_DISPLAY_ROUNDING = 15;	
	
	/** Specifieds the spacing between text lines!*/
	public static final int COMPLEX_DISPLAY_LINE_SPACING = 8;
	
	/** Specifies the spacing between the tabs */
	public static final int COMPLEX_DISPLAY_TAB_SPACING = 20;
	
	/** Specifies the message box line spacing of the message text*/
	public static final String COMPLEX_DISPLAY_FONT_NAME = "Verdana";	
	
	/** Specifies the padding of the text between the complex display tab*/
	public static final int COMPLEX_DISPLAY_TAB_PADDING = 5;
	
	/** Specifies the padding of the text between the complex display tab*/
	public static final int COMPLEX_DISPLAY_TAB_ROUNDING = 10;
	
	/** Specifies the font size for tab elements */
	public static final int COMPLEX_DISPLAY_TAB_FONT_SIZE = 12;	
	
	/** Specifies the font size for items */
	public static final int COMPLEX_DISPLAY_FONT_SIZE = 10;	
	
	/** Specifies the minimum width for items */
	public static final int COMPLEX_DISPLAY_MIN_ITEM_WIDTH = 150;
	
	/** Specifies the minimum height for items*/
	public static final int COMPLEX_DISPLAY_MIN_ITEM_HEIGHT = 60;
	
	/** Specifies the space between items*/
	public static final int COMPLEX_DISPLAY_ITEM_SPACING = 10;
	
	/** Specifies the rounding of the items*/
	public static final int COMPLEX_DISPLAY_ITEM_ROUNDING = 10;
	
	/** Specifies the rectangular item image size */
	public static final int COMPLEX_DISPLAY_ITEM_IMAGE_SIZE = 40;
	
	/** Holds the one and only Game instance. */
	private static Game instance = null;
	
	/** Identifies room music */
	private static final int MUSIC = 1;
	
	/** Identifies sound effects*/
	private static final int SOUND_EFFECT = 2;

	/** Contains all the rooms. */
	private HashMap<String, Room> rooms = new HashMap<String, Room>();

	/** Contains all the named commands for access by events */
	private HashMap<String, ICommand> commands = new HashMap<String, ICommand>();
	
	/** Contains the XML handler of the current saveGame. */
	private Document gameState;
	
	/** Contains the XML handler of the current configuration. */
	private Document settings;
	
	/** Contains all drawers that draw something in the game */
	private ArrayList< IDrawer > drawers = new ArrayList< IDrawer >();
	
	/** Contains all the filenames of all images used in the game */
	private HashMap< String, String > imagePaths = new HashMap< String, String > ();

	//TODO i changed this to hashmap, maybe better use ArrayList?
	/** Contains all the players in the game*/
	private HashMap< String, Player> players = new HashMap<String, Player>();
	
	/** Contains all the keys pressed*/
	private Vector<Integer> keysPressed = new Vector<Integer>();
	
	/** Contains all the controllers that can process input*/
	private ArrayList<IController> controllers = new ArrayList<IController>();
	
	/** Contains the animated objects */
	private Vector<IRoomObject> animatedObjects = new Vector<IRoomObject>();
	
	/** Holds the control processor instance*/
	private ControlProcessor controlProcessor = new ControlProcessor();
	
	/** Holds the drawing processor instance*/
	private DrawingProcessor drawingProcessor = new DrawingProcessor();
	
	/** Contains the event processor instance. */
	private EventProcessor eventProcessor = new EventProcessor();
	
	/** Holds the animation processor instance*/
	private AnimationProcessor animationProcessor = new AnimationProcessor();
	
	/** Draw in-game debug info */
	private boolean showDebug = false; 
	
	/** Holds the room music thread instance */
	private AudioClip music = null;
	
	/** Contains the path to the music file that is currently playing as the world musci*/
	private String musicPath = "";
	
	/** Holds the language */
	private String language = "en";
	
	/** Holds the instance to the display window, or null if none*/
	private CommandDisplay commandDisplay = null;
	
	/** Holds the instance to the complex window, or null*/
	private ComplexDisplay complexDisplay = null;
	
	/** If an inventory is shown from a certain organism, this holds the instance*/
	private IOrganism inventoryFrom = null;
	
	/**
	 * Returns the one and only instance of Game. May instantiated one if none exists.
	 * 
	 * @note Singleton pattern implemented.
	 * @return Game The one and only instance.
	 */
	public static Game getInstance(){
		if(instance == null)
			instance = new Game();
		
		return instance;
	}

	/**
	 * Constructor.
	 * 
	 * @note Private due implemented Singleton pattern.
	 */
	private Game(){
		loadCommands();
		
		//TODO: REMOVE THIS:
		
		/*
		commandDisplay = new CommandDisplay();
		commandDisplay.setTekst("Dit is een een testberichtje voor het venster dat in-game weergegeven gaat worden. En geformat door het crappy algoritme!");
		commandDisplay.addCommand("Enter door", null);
		commandDisplay.addCommand("Do nothing", null);
		commandDisplay.setSelected(0);
		*/
	}
	
	/**
	 * adds a controller to the array of controllers
	 * if it is already registered, nothing will happen
	 * 
	 * @param controller instance derived of the IController interface
	 */
	public void addController(IController controller){
		for(IController ctrl: controllers){
			if(ctrl.equals(controller))
				return;
		}	
		
		controllers.add(controller);
	}
	
	/**
	 * adds a drawer class to the game, and give it all the paths from the images the game uses.
	 * 
	 * @param drawer a class capable of drawing the game context
	 */
	public void addDrawer( IDrawer drawer ){
		if(drawer == null)
			return;
		
		drawers.add( drawer );
		
		//register all images to the new drawer (if the drawer type is from a from a framework that already has a drawer, all images are already loaded)
		Iterator<String> imageIterator = imagePaths.keySet().iterator();
		
		while(imageIterator.hasNext())
		{
			String key = imageIterator.next();
		
			drawer.preloadImage(key, imagePaths.get(key));
		}
	}
		
	public IDrawer getDrawerByRoomObject(IRoomObject roomObject)
	{
		for(IDrawer drawer : drawers)
		{
			if(drawer.getFocus() == roomObject)
				return drawer;
		}
		
		return null;
	}
	
	/**
	 * find the player objects in the game XML and create an instance 
	 * of KeyController attached to the player id (defined in game.xml) 
	 * and attach the corresponding controlset(defined in settings.xml)
	 * 
	 * TODO Discuss. this reads the XML DOM tree for players, why not read the Game.players HashMap? add function to IMovable 
	 */
	public void attachPlayerControllers(){
		try {
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList playerlist = (NodeList) xPath.compile("/game/roomobjects/player").evaluate(gameState, XPathConstants.NODESET);
		
			for(int i = 0; i < playerlist.getLength(); i++){
				NamedNodeMap playerAttributes = playerlist.item(i).getAttributes();
				Node playerId = playerAttributes.getNamedItem("id");
				if(playerId == null){
					System.out.println("Warning: no 'id' attribute found with a player, skipping player!");
					continue;	
				}
				//get the playerid in string format
				String player = playerId.getNodeValue();
				
				Node controlsetId = playerAttributes.getNamedItem("usecontrolset");
				if(controlsetId == null){
					System.out.println("Warning: no 'usecontrolset' attribute found with player, skipping player!");
					continue;
				}
				
				String controlSet = controlsetId.getNodeValue();
				
				if( !players.containsKey(player) ){
					System.out.println("Warning: no player registerd with the id " + player + ", skipping player!");
					continue;
				}
				
				Node controlSetNode = (Node) xPath.compile("/settings/controls/controlset[@id=" + controlSet + "]").evaluate(settings, XPathConstants.NODE);
				if(controlSetNode == null){
					System.out.println("Warning: no controlset defined with an 'id' attribute that corresponds to '" + controlSet + "', skipping player!");
					continue;
				}
				
				
				{//DUMP KEYCONTROLLER NODE DEBUG INFO
					NodeList contents = (NodeList) xPath.compile("//move").evaluate(controlSetNode, XPathConstants.NODESET);
					int len = contents.getLength();
					for(int x = 0; x < len; x++){
						Node content = contents.item(x);
						System.out.println("DEBUG-INFO: ---NODE---");
						System.out.println("DEBUG-INFO: NODENAME: " + content.getNodeName());
						System.out.println("DEBUG-INFO: DIRECTION: " + content.getAttributes().getNamedItem("direction").getNodeValue());
						System.out.println("DEBUG-INFO: KEYCODE: " + content.getAttributes().getNamedItem("keycode").getNodeValue());
						System.out.println("DEBUG-INFO: ---END---");
					}
				}
				
				IController controller = new KeyController(players.get(player), controlSetNode);
				addController(controller);
			}
		} catch (XPathExpressionException e) {
			System.out.println("ERROR: XPATH error");
			e.printStackTrace();
		}

	}

	/*
	public boolean drawMoveable(int uniqueid, Device dc) {
		return false;
	}


	*/
	
	/**
	 * Clears the command display
	 */
	public void clearCommandDisplay(){
		setCommandDisplay(null);
		redraw();
	}
	
	/**
	 * Clears the complex display (usually inventory)
	 */
	public void clearComplexDisplay(){
		setComplexDisplay(null);
		redraw();
	}

	/**
	 * Creates a new game. 
	 * 
	 * @param worldname 	The name of the world.
	 * @param playerName The name of the player.
	 * @param language	The name of the language
	 * @throws Exception
	 */
	public void createNewGame(String worldName, String playerName, String language) throws Exception
	{
		loadGame("nl/toefel/game/games/" + worldName + "/game.xml");
		setLanguage(language);
		
		NamedNodeMap gameAttributes = gameState.getFirstChild().getAttributes();
		Node playerNameNode = gameState.createAttribute("playername");
		playerNameNode.setNodeValue(playerName);
		gameAttributes.setNamedItem(playerNameNode);
		
		Node tempNode;
		
		tempNode = gameAttributes.getNamedItem("world");
		if( tempNode != null )
			tempNode.setNodeValue( worldName );
		//TODO else create worldName attribute?
		
		//TODO game version in XML should only be read, not set!
		tempNode = gameAttributes.getNamedItem("version");
		if( tempNode != null)
			tempNode.setNodeValue( GAMEVERSION );	
		
		// Replace the name of the player in all the texts.
		
		// Create a new Xpath instance.
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		// Construct the Xpath query.
		String query = "/game/texts/text/line";
		
		// Compile and evaluate the query on the Xpath object, to obtain the String which is the requested line.
		NodeList textValues = (NodeList) xpath.compile(query).evaluate(gameState, XPathConstants.NODESET);
			
		for(int i = 0, max = textValues.getLength(); i < max; i++)
		{	
			// Get text value.
			String textValue = textValues.item(i).getFirstChild().getNodeValue();
		
			// Replace tokens.
			textValue = textValue.replace("%PLAYERNAME%", playerName);
			
			// Store the new text value.
			textValues.item(i).getFirstChild().setNodeValue(textValue);
		}
		
		
	}
	
	public void debug_redraw()
	{
		for(IDrawer iDrawer : drawers)
			iDrawer.draw();
	}
	
	/**
	 * Returns all the registered animated objects in a synchronized vector list!
	 * 
	 * @return vector(array) of animated objects
	 */
	public Vector<IRoomObject> getAnimatedObjects(){
		return animatedObjects;
	}
	
	/**
	 * Retrieves a command that can be executed. This command MUST be registered 
	 * to game before this function will return the command.
	 * 
	 * @param commandName the command name which is used in game!
	 * @param xmlData the XML data structure to initialize the command
	 * @return executable ICommand or null if not available
	 */
	public ICommand getCommand(String commandName, Node xmlData, IRoomObject initiator, IRoomObject executer){
		if(commands.containsKey(commandName)){
			ICommand command = commands.get(commandName);
			if(command != null)
				return (ICommand) command.createInstance(xmlData, initiator, executer);
		}
		return null;
	}
	
	/**
	 * Returns the in-game command display instance, null if none
	 * 
	 * @return commandDisplay if display is presend, else null
	 */
	public CommandDisplay getCommandDisplay(){
		return commandDisplay;		
	}
	
	/**
	 * Returns the in-game complex display instance(null if none), null if none
	 * @return
	 */
	public ComplexDisplay getComplexDisplay(){
		return complexDisplay;
	}
	
	/**
	 * Returns true if the drawers should draw in fullscreen modus, false otherwise
	 * 
	 * @return fullscreen true|false
	 */
	public boolean getFullscreen(){
		if(settings != null){ 
			NodeList fullscreenNodes = settings.getElementsByTagName("fullscreen");
			
			if(fullscreenNodes.getLength() > 0){
				Node fullscreenNode = fullscreenNodes.item(0);	//select first node
				
				if(fullscreenNode != null){
					NamedNodeMap attributes = fullscreenNode.getAttributes();
					fullscreenNode = attributes.getNamedItem("on");
					
					if(fullscreenNode != null){
						if(fullscreenNode.getNodeValue().equals("true")){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the language, if not set: English.
	 * TODO update this to read the language from the settings file!!
	 * @return The language.
	 */
	public String getLanguage()
	{
		return language;
	}
	
	/**
	 * Returns the name of the player.
	 * 
	 * @return The name of the player.
	 */
	public String getPlayerName()
	{
		String playername = gameState.getFirstChild().getAttributes().getNamedItem("playername").getNodeValue();
		return playername;
	}


	
	/**
	 * Returns the requested Room.
	 * 
	 * @param id Id of the Room.
	 * @return Requested Room.
	 */
	public Room getRoom(String id){
		return rooms.get(id);
	}
	
	/**
	 * Returns the instance of the Room Object with the specified id
	 * TODO modify to player search, and search in items the player caries.
	 * @param id unique id of roomobject
	 * @return roomobject or null if not found
	 */
	public IRoomObject getRoomObjectById(String id)
	{
		for(Room room : rooms.values())
		{
			for(IRoomObject roomObject : room.getRoomObjects())
			{
				if(roomObject.getIDString().equals(id))
					return roomObject;
			}
		}
		
		for(Player player: players.values()){
			for(Inventory inventory : player.getInventorys()){
				for(ICarryable carryable : inventory.getItems()){
					if(carryable.getIDString().equals(id))
						return carryable;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Determine the correct path for a given sound type
	 * 
	 * @param soundfile filename of the sound file
	 * @param soundType MUSIC | SOUND_EFFECT
	 * @return full file path if found
	 */
	protected String getSoundPath(String soundFile, int soundType){
		String type = soundType == MUSIC ? "music" : "effects";
		String path = "nl/toefel/game/games/" + getWorldName() + "/sounds/" + type + "/" + soundFile;
		
		File imageFile = new File(path);
		    
		if(!imageFile.exists()){
			path = "nl/toefel/game/sounds/" + type + "/" + soundFile;
			imageFile = new File(path);
				
			if(!imageFile.exists()){
				System.err.println("ERROR: " + soundFile + " not found (type: " + (soundType == MUSIC ? "music" : "sound effect") +  ")");
				return "";			
			}
		}

		return path;
	}
	
	/**
	 * Returns the requested Textline.
	 * 
	 * @param  id	The id of the desired text.
	 * @return Textline	The requested Textline.
	 * @throws Exception 
	 */
	public Textline getTextline(int id) throws Exception
	{
		// Create a new Xpath instance.
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		// Construct the Xpath query.
		String query = "/game/texts/text[@id='" + id + "'][1]"; ///line[@lang='" + language + "'][1]";
		
		// Compile and evaluate the query on the Xpath object, to obtain the String which is the requested line.
		Node textLine = (Node) xpath.compile(query).evaluate(gameState, XPathConstants.NODE);
			
		if(textLine == null)
			throw new Exception("Requested text #" + id + " does not exists.");

		return new Textline(textLine);
	}
	
	/**
	 * Returns the name of the world.
	 * 
	 * TODO Add to UML.
	 * 
	 * @return The name of the world.
	 */
	public String getWorldName()
	{
		return gameState.getFirstChild().getAttributes().getNamedItem("world").getNodeValue();
	}
	
	/**
	 * Adds a keycode that is treated as input
	 * @param keyCode the code of the key that is pressed
	 * TODO WHY NOT PROCESS pressed keys directly??
	 */
	public void keyPressed(int keyCode){
			if(!keysPressed.contains(keyCode))
				keysPressed.add(keyCode);
		
	}
	
	public void keyReleased(int keyCode){
	//	synchronized(this){
			keysPressed.remove((Object)keyCode);
	//	}
	}
	
	/**
	 * Adds the image to the array of imagePaths and registers 
	 * it in drawers (if registered later, it will be loaded afterwards)
	 * 
	 * @param path path of the image to load into the game
	 */
	/*
	public void preloadImage( String path ){
		if( !imagePaths.contains( path ) ){
			imagePaths.add( path );
			//if a path exists in imagePaths, it should already be registered in the drawers
			for(IDrawer drw : drawers)
				drw.preloadImage(path);
		}
	}
	*/
	
	/**
	 * Fills the local hashmap with name => commando pairs
	 */
	private void loadCommands(){
		commands.put("move", new Move());
		commands.put("open", new Open());
		commands.put("use", new Use());
		commands.put("pickup", new PickUp());
		commands.put("createdisplay", new CreateCommandDisplay());
		commands.put("cleardisplay", new ClearCommandDisplay());
		commands.put("talk", new Talk());
		commands.put("unloadgame", new UnloadGame());
		commands.put("drop", new Drop());
		commands.put("save", new Save());
		commands.put("settextline", new SetTextline());
	}
	
	/**
	 * Loads the given savegame -file AND the settings.xml configuration file.
	 * 
	 * @param filename The path to the savegame.
	 * @throws Exception 
	 */
	public void loadGame(String filename) throws Exception
	{
		loadGame(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename));
	}
	
	
	/**
	 * Loads the given savegame (XML) AND the settings.xml configuration file.
	 * 
	 * @param filename The path to the savegame.
	 * @throws Exception 
	 */
	public void loadGame(Document gameXML) throws Exception
	{		
		//TODO implement check to validate the XML that will be loaded contains valid basic elements and attributes

		// Load the settings.xml file
		settings = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(SETTINGS_FILE);
		
		// Load the savegame.
		gameState = gameXML;
		
		// Read the default language for the objects out of the XML file
		//readLanguageFromGamefile();
		readLanguageFromSettingsfile();
		
		// Obtain xpath instance.
		XPath xPath =  XPathFactory.newInstance().newXPath();
    
		// Select all rooms.
		NodeList rooms = (NodeList) xPath.compile("/game/rooms/room").evaluate(gameState, XPathConstants.NODESET);
        
		// Construct rooms and give them their begin state.
		for(int i = 0, max = rooms.getLength(); i < max; i++)
        	   this.rooms.put((String) xPath.compile("@id").evaluate(rooms.item(i), XPathConstants.STRING), new Room(rooms.item(i)));
   
		// Select all room objects.
		NodeList roomobjects = (NodeList) xPath.compile("/game/roomobjects/*").evaluate(gameState, XPathConstants.NODESET);
	    
		ArrayList<ICarryable> carryableObjects = new ArrayList<ICarryable>();
		
		// Construct room objects and give them their begin state.
		for(int i = 0, max = roomobjects.getLength(); i < max; i++)
		{
			// Instantiate the correct IRoomObject.
			IRoomObject roomObject = null;
			String nodeName = roomobjects.item(i).getNodeName();
			
			// Instantiate the correct IRoomObject.
			if(nodeName == "player")
			{
				//to be more typesafe and write clearer code, use a Player instance instead casting roomObject up to (Player)
				Player player = new Player(roomobjects.item(i));
				//register the player
				registerPlayer(player);
				
				roomObject = (IRoomObject) player;
				
				addDrawer(new SWTDrawer(player, getFullscreen()));
			}
			else if(nodeName == "animal")
				roomObject = new Animal(roomobjects.item(i));
			
			else if(nodeName == "human")
				roomObject = new Human(roomobjects.item(i));
			
			else if(nodeName == "primitive")
				roomObject = new Primitive(roomobjects.item(i));
			
			else if(nodeName == "door")
				roomObject = new Door(roomobjects.item(i));
			
			else if(nodeName == "doorkey")
				roomObject = new Doorkey(roomobjects.item(i));
				
			else
				throw new Exception("IRoomObject of type '" + nodeName + "' does not exists.");
        
			// Add the IRoomObject to the correct Room.
			String roomid = (String) xPath.compile("@room").evaluate(roomobjects.item(i), XPathConstants.STRING);
			Room room = this.rooms.get(roomid);
			
			if(room == null)
			{
				// Select the room id.
				String roomObjectId = (String) xPath.compile("@id").evaluate(roomobjects.item(i), XPathConstants.STRING);
	
				// Throw an exception.
				throw new Exception("Room #" + roomid + ", used by RoomObject #" + roomObjectId + ", does not exists.");
			}
			
			//if a certain object can be carried, check if it's carried, 
			//it should then not be registered. Because player may not have been loaded
			//wait until all roomobjects are loaded!
			
			if(roomObject instanceof ICarryable){
				carryableObjects.add((ICarryable)roomObject);
			}
			
			room.registerObject(roomObject);
		}
			
		for(int i = 0; i < carryableObjects.size(); i++){
			ICarryable obj = carryableObjects.get(i);
			Player player = players.get(obj.getCarrier());
			
			if(player != null){
				player.carryItem(obj);
			}
		}
		
		// Create a drawer per Player.
		//for(Player player : players)
		//	addDrawer(new SWTDrawer(Display.getCurrent(), player));
		
		attachPlayerControllers();
		
		notifyDrawers();
		
		readFramerateFromSettings();
		
		//start the drawing thread
		new Thread(drawingProcessor).start();
		
		//start the controller thread
		new Thread(controlProcessor).start();
		
		// Start the event processor.
		new Thread(eventProcessor).start();
		
		//Start the animation processor
		new Thread(animationProcessor).start();
		
		//start the sound of the room where the first player resides!
		if( players.size() > 0){
			Iterator<String> itr = players.keySet().iterator();
			Player plr = players.get(itr.next());	//should always work because there is at least one 
			playMusic(plr.getRoom().getSoundFile());
		}
	}
	
	
	/**
	 * Notifies all drawers of a visual game change, drawers will redraw
	 * Do not use this function to redraw the game. use redraw instead!!
	 */
	public void notifyDrawers(){
		for(int i = 0; i < drawers.size(); i++){
			drawers.get(i).draw();
		}
		/*
		for(IDrawer drw : drawers)
			drw.draw();
		*/
	}
	
	/**
	 * Starts playing (and looping) the music, if the file exists and can be played
	 * if some music was already playing, it will be stopped and the new music will be played
	 * 
	 * @param filename location of the music file
	 */
	public void playMusic(String filename){
		//this music is already playing!
		if(musicPath.equals(filename)) return;
		musicPath = filename;
		
		if(music != null){
			//find a better solution
			music.stop();
			music = null;
		}
		
		if(filename.endsWith(".wav"))
			music = new AudioPlayer(getSoundPath(filename, MUSIC));
		
		else if(filename.endsWith(".mid"))
			music = new MidiPlayer(getSoundPath(filename, MUSIC));
			
		music.loop();
	}
	
	/**
	 * Plays a particular sound only once, use this for little sound effects
	 * 
	 * @param filename location of the sound file
	 */
	public void playSound(String filename){
		//TODO add midi sounds too!
		new AePlayWave(getSoundPath(filename, SOUND_EFFECT)).start();
	}
	
	/**
	 * processes all keys in the queue, till it is empty
	 */
	public void processKeys(){
		//synchronized(this){
			for(int i = 0; i < keysPressed.size(); i++){
				//key can be removed by controllers, for example the USE key. 
				//to tackle concurrent problems. store in local var
				int keyCode = keysPressed.get(i);
				for(IController ctrl : controllers)
					ctrl.processInput( keyCode );
			}
			/*
			for(int key : keysPressed  ){
				for(IController ctrl : controllers)
					ctrl.processInput( key );
			}
			*/
	//	}
	}
	
	/**
	 * Reads the desired framerate form the XML file
	 */
	public void readFramerateFromSettings(){
		//Read the desired framerate from the XML, default to 40
		if(settings == null){
			drawingProcessor.setDesiredFramerate(DEFAULT_FRAMERATE);
		}
		NodeList framerates = (NodeList)settings.getElementsByTagName("framerate");
		Node framerate = framerates.getLength() > 0 ?  framerates.item(0) : null;
		
		if(framerate != null){
			NamedNodeMap map = framerate.getAttributes();
			Node fps = map.getNamedItem("fps");
			if (fps != null){
				try {
					int desiredFramerate = Integer.valueOf(fps.getNodeValue());
					if(desiredFramerate > 5 && desiredFramerate < 1000){
						drawingProcessor.setDesiredFramerate(desiredFramerate);
					}else{
						System.out.println("DEBUG-INFO: ERROR, framerate should be higher then 5 and less then 1000");
					}
				} catch (NumberFormatException e) {
					System.out.println("DEBUG-INFO: ERROR, settings.xml contains invalid framerate value '" + fps.getNodeValue() + "'");
					e.printStackTrace();
				} 
			}else{
				drawingProcessor.setDesiredFramerate(DEFAULT_FRAMERATE);
			}
		}else{
			drawingProcessor.setDesiredFramerate(DEFAULT_FRAMERATE);
		}
	}
	
	/**
	 * Reads the language from the game file
	 * @deprecated language should be read from settings file
	 */
	public void readLanguageFromGamefile(){
		NamedNodeMap gameAttributes = gameState.getFirstChild().getAttributes();
		Node languageAttribute = gameAttributes.getNamedItem( "language" );
		assert( languageAttribute != null );
			
		if( languageAttribute != null ){
			String language = languageAttribute.getNodeValue();
			//return languageAttribute.getNodeValue();
			if (language.equals("en"))
				System.err.println("LANG EN");
			else
				System.err.println("LANG NL");
			
			this.language = language;
		}else{
			System.err.println("LANG EN DEFAULT");
			this.language = "en";
		}
	}
	
	/**
	 * Reads the language from the settings file
	 */
	public void readLanguageFromSettingsfile(){
		try {
			XPath xPath =  XPathFactory.newInstance().newXPath();
			String lang = (String) xPath.compile("/settings/language/attribute::lang").evaluate(settings, XPathConstants.STRING);
			if(lang.length() == 2){
				language = lang;
			}else{
				System.out.println("WARNING: no or invalid language available in settings file, using english as default");
				language = "en";
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Signals the drawing processors to redraw the game.
	 */
	public void redraw(){
		drawingProcessor.setRedraw();
	}
	
	/**
	 * Registers an object as an animated object. The animation processor will update 
	 * this object every x miliseconds. The object can decide if it changes or not
	 * 
	 * @param animatedObject the object that should be animated
	 */
	public void registerAnimatedObject(IRoomObject animatedObject){
		if(!animatedObjects.contains(animatedObject))
			animatedObjects.add(animatedObject);
	}
	
	/**
	 * Registers the given image and may preload it.
	 * 
	 * TODO Use e.g. imageloader for a visual indicator?
	 * @param image
	 */
	public void registerImage(String imagefile)
	{
		try
		{
			// Return when the file is already preloaded.}
			if(imagePaths.containsKey(imagefile))
				return;
			
			// Determine file path.
			File imageFile = new File("nl/toefel/game/games/" + getWorldName() + "/images/" + imagefile);
		    
			if(!imageFile.exists())
			{
				imageFile = new File("nl/toefel/game/images/" + imagefile);
				
				if(!imageFile.exists())
					throw new Exception("Image '" + imagefile + "' not found.");
			}
			
			imagePaths.put(imageFile.getPath(), imagefile);
			
			for(IDrawer drw : drawers)
				drw.preloadImage(imageFile.getPath(), imagefile);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	/**
	 * For testing purpose, or not?
	 * 
	 * TODO Discuss.
	 * @param player
	 */
	public void registerPlayer(Player player)
	{
		players.put(player.getIDString(), player);
	}
	
	/**
	 * Removes the drawer from the game, if all drawers are
	 * removed, the game is automatically unloaded.
	 * TODO should the game automatically unload when all drawers removed?
	 * @param drawer
	 */
	public void removeDrawer(IDrawer drawer){
		drawers.remove(drawer);
		Runtime.getRuntime().gc();	//collect memory of preloaded images
		
		if(drawers.size() <= 0)
			unload();
	}
	
	/**
	 * Saves the game in the standard format!
	 */
	public void saveGame(){
		Calendar cal = Calendar.getInstance();
		
		String hour = "" +cal.get(Calendar.HOUR_OF_DAY);
		hour = hour.length() == 1 ? "0" + hour : hour;
		
		String minute = "" + cal.get(Calendar.MINUTE);
		minute = minute.length() == 1 ? "0" + minute : minute;
		
		String day = "" + cal.get(Calendar.DAY_OF_MONTH);
		day = day.length() == 1 ? "0" + day : day;
		
		String month = "" + (cal.get(Calendar.MONTH)+1);
		month = month.length() == 1 ? "0" + month : month;
		
		String year = "" + cal.get(Calendar.YEAR);
		year = hour.length() == 1 ? "0" + year : year;
		
		String filename = "nl/toefel/game/games/" + getWorldName() + "/savegames/";
		filename += getPlayerName() + "_" + 
					hour + "u" +
					minute + "m" + "_" +
					day + "d" + 
					month + "m" +
					year + "y" + ".xml";
		
		saveGame(filename);
	}

	/**
	 * Saves the current game state to the given filepath.
	 * TODO remove throw!!!
	 * @param filepath The filepath where the savegame needs to be stored (including '.xml').
	 * @throws Exception
	 **/
	public void saveGame(String filepath)
	{
		// Select the root node of the current gamestate.
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Node node;
		try {
			node = (Node) xPath.compile("/*[1]").evaluate(gameState, XPathConstants.NODE);
		
			File file = new File(filepath);
			
			if(!file.exists())
				file.createNewFile();
			
			// Open the FileWriter with the given filepath.
			FileWriter fileWriter = new FileWriter(filepath);
	
			// Transform XML to String and write it to the given StreamResult.
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(node), new StreamResult(fileWriter)); 
			
			// Close the file handle.
			fileWriter.close();
		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the command display window instance
	 * 
	 * @param commandDisplay the instance to the display, null if it must be erased
	 */
	public void setCommandDisplay(CommandDisplay commandDisplay){
		this.commandDisplay = commandDisplay;
		redraw();
	}
	
	/**
	 * Sets the complex display (usually inventory) instance
	 * 
	 * @param complexDisplay the complex display structure that should be initialized
	 */
	public void setComplexDisplay(ComplexDisplay complexDisplay){
		if(complexDisplay == null) inventoryFrom = null;
		this.complexDisplay = complexDisplay;
		redraw();
	}
	
	/**
	 * Builds the inventory of the specified organism and sets this into the game
	 * 
	 * @param organism
	 */
	public void setInventoryFrom(IOrganism organism){
		inventoryFrom = organism;
		if(organism == null) return;
		
		ComplexDisplay complexDisplay = new ComplexDisplay();
		ArrayList<Inventory> inventorys = organism.getInventorys();
		
		//use for loop, for avoiding synchronization problems 
		for(int i = 0; i < inventorys.size(); i++){
			Inventory inventory = inventorys.get(i);
			if(inventory == null) continue;
						
			ComplexDisplayTab tab = new ComplexDisplayTab( inventory.getName(), "Now carrying " + inventory.getCurrentWeight() + "kg, maximum is set to " + inventory.getMaxWeight() );
			
			for(int j = 0; j < inventory.getNumItems(); j++){
				ICarryable carryableItem = inventory.getItem(j);
				
				//TODO provide common translations in translator class for this
				//TODO check if object is usable, then provide extra use command!
				ArrayList<ICommand> itemCommands = new ArrayList<ICommand>();
				itemCommands.add(new ClearCommandDisplay("Nothing"));
				itemCommands.add(new Drop(organism, carryableItem, "Drop item"));
				
				tab.addItem(carryableItem.getName(), carryableItem.getImage(), itemCommands);
			}
			
			complexDisplay.addTab(tab);
		}
		
		this.setComplexDisplay(complexDisplay);
	}
	
	/**
	 * Sets the flag that causes the drawers to show debug information
	 * 
	 * @param showDebug
	 */
	public void setShowDebug(boolean showDebug){
		this.showDebug = showDebug;
	}
	
	/**
	 * Returns true if debug will be drawn, or false if it won't be drawn;
	 * 
	 * @return debug drawing on|off
	 */
	public boolean getShowDebug(){
		return this.showDebug;
	}

	/**
	 * Creates a shutdown display, in which the user can quit the game (useful for fullscreen modus!)
	 * 
	 *  @param location, the location of the display in the canvas (usually the location of the caller!)
	 */
	public void setShutdownDisplay(Point location){
		CommandDisplay commandDisplay = new CommandDisplay();
		commandDisplay.setTekst("Stop the game?");
		
		commandDisplay.addCommand("No, continue gaming!", new ClearCommandDisplay());
		commandDisplay.addCommand("Shutdown Game(unsaved)", new UnloadGame("Shutdown Game"));
		
		commandDisplay.setLocation(location);
		
		this.commandDisplay = commandDisplay;
		redraw();
	}
	
	/**
	 * Sets the language to the given language.
	 * 
	 * @param langauge The language to be used.
	 */
	public void setLanguage(String language)
	{
		NamedNodeMap gameAttributes = gameState.getFirstChild().getAttributes();
		Node languageAttribute = gameAttributes.getNamedItem( "language" );
		assert( languageAttribute != null );
		
		//when null? create attribute
		if( languageAttribute != null )
			languageAttribute.setNodeValue(language);
	}
	
	/**
	 * Updates the inventory window if an inventory is currently shown 
	 */
	public void updateCurrentInventoryWindow(){
		int selectedTab = complexDisplay.getSelected();
		this.setInventoryFrom(inventoryFrom);
		complexDisplay.setSelected(selectedTab);
		redraw();
	}
	
	/**
	 * Cleans up the game instance, stops the threads and music. closes drawer screens
	 * Removes internal singleton instance.
	 * TODO revisit, needs updates, some music threads stay alive!
	 */
	public void unload(){
		//cause thread control loops to end!
		controlProcessor.setStopFlag();
		drawingProcessor.setStopFlag();
		eventProcessor.setStopFlag();
		animationProcessor.setStopFlag();
		
		//stop the game music
		if( music != null){
			music.stop();
			music = null;
		}

		//wait for other threads to stop
		/*
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		//close the drawers
		if(drawers.size() > 0 && GameEditor.getInstance() == null){
			//disposing a drawer will close the canvas, and remove itself from the drawers array
			//automatically. So this will cause problems if disposing drawer objects in an array. 
			//Solution: convert to an ordinary array, and dispose all those items.
			Object[] drawerArray = drawers.toArray();
			
			for(Object drw: drawerArray){
				synchronized(this){
					((IDrawer)drw).dispose();
				}
			}
		} 
		
		//remove the instance
		instance = null;
			
		//because instance is set to 0, the whole game hierarchy should be 
		//unreachable. The garbageCollector will cleanup
		//Runtime.getRuntime().gc();
	}
	
	
	public void unregisterObject(IRoomObject animatedObject){
		synchronized(this){
			animatedObjects.remove(animatedObject);
		}
	}
	
	/**
	 * Returns the events -node of the XML.
	 * 
	 * @return Node	The event -node.
	 * @throws XPathExpressionException
	 */
	public Node getEventsXML() throws XPathExpressionException
	{
		XPath xPath =  XPathFactory.newInstance().newXPath();
	    
		return (Node) xPath.compile("/game/events").evaluate(gameState, XPathConstants.NODE);
	}
	
	/**
	 * Returns the event processor.
	 * 
	 * @return	The event processor.
	 */
	public EventProcessor getEventProcessor()
	{
		return eventProcessor;
	}
	
	public Document getGameState()
	{
		return gameState;
	}
	
	public void setGameState(Document gameState)
	{
		this.gameState = gameState;
	}
	
	public HashMap<String, Player> getPlayers()
	{
		return players;
	}
}