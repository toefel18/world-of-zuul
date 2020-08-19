package nl.toefel;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nl.toefel.game.Game;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WorldOfZuulPortal {

	private static final String WINDOW_TITLE = "World of zuul";
//	private static final Point DEFAULT_GAME_WINDOW_SIZE = new Point(800, 600);
	private static final Point DEFAULT_PORTAL_WINDOW_SIZE = new Point(425, 300);
	private static final Point DEFAULT_CONFIGURE_CONTROL_WINDOW_SIZE = new Point(280, 350);
	private static final String PLAYER_NAME_ASK = "<enter player name>";
	private static final String LOGO_PATH = "logo.png";
	private static final String ABOUT_TEXT = "World of zuul \n\n Role playing game\n\nAuthors:\nC. Hesters\nJ. Krist\n\n(C)2008";
//	private static final String ABOUT_WEBLINK = "<A HREF=\"http://trac.woz.toefel.nl\">Visit the trac environment</A>";
	
	/*ATTENTION: ORDER OF CONTROLS DEFINED BELOW IS IMPORTANT FOR LAYOUTS!!*/
	
	//create standard display and create tab control
	private Display display = new Display();
	private Shell shell = new Shell( display, SWT.PRIMARY_MODAL | SWT.CLOSE | SWT.BORDER ); // for fixed window: SWT.PRIMARY_MODAL | SWT.CLOSE | SWT.BORDER
	private Label label = new Label(shell,SWT.NONE);
	private FillLayout fillLayout = new FillLayout();
	private TabFolder tabFolder = new TabFolder( shell, SWT.TOP );

	//create tabs, and for each tab a composite panel
	private TabItem newGame = new TabItem( tabFolder, SWT.NONE );
	private Composite newGamePanel = new Composite( tabFolder, SWT.NONE );
	private TabItem loadGame = new TabItem( tabFolder, SWT.NONE );
	private Composite loadGamePanel = new Composite( tabFolder, SWT.NONE );
	private TabItem settings = new TabItem( tabFolder, SWT.NONE );
	private Composite settingsPanel = new Composite( tabFolder, SWT.NONE );
	private TabItem about = new TabItem( tabFolder, SWT.NONE );
	private Composite aboutPanel = new Composite( tabFolder, SWT.NONE );
	
	//define controls for the newGamePanel
	private Label newGamesLabel = new Label( newGamePanel, SWT.NONE );
	private Combo newGames = new Combo( newGamePanel, SWT.READ_ONLY );
	private Label newPlayerLabel = new Label( newGamePanel, SWT.NONE );
	private Text newPlayer = new Text( newGamePanel, SWT.SINGLE | SWT.BORDER);
	private Button newGameButton = new Button( newGamePanel, SWT.PUSH );
	
	//define controls for the loadGamePanel
	private Label availableGamesLabel = new Label( loadGamePanel, SWT.NONE );
	private Combo availableGames = new Combo( loadGamePanel, SWT.READ_ONLY );
	private Label availableNamesLabel = new Label( loadGamePanel, SWT.NONE );
	private Combo availableNames = new Combo( loadGamePanel, SWT.READ_ONLY );
	private Label availableSavesLabel = new Label( loadGamePanel, SWT.NONE );
	private Combo availableSaves = new Combo( loadGamePanel, SWT.READ_ONLY );
	private Button loadGameButton = new Button( loadGamePanel, SWT.PUSH );
	
	//define controls for the settings Panel
	private Label languageLabel = new Label(settingsPanel, SWT.NONE);
	private Combo availableLanguages = new Combo(settingsPanel, SWT.READ_ONLY);
	private Label configureControlsLabel = new Label(settingsPanel, SWT.NONE);
	private Button configureControlsButton = new Button(settingsPanel, SWT.PUSH);
	private Label fullscreenLabel = new Label(settingsPanel, SWT.NONE);
	private Button fullscreenButton = new Button(settingsPanel, SWT.CHECK); 
	private Label framerateLabel = new Label(settingsPanel, SWT.NONE);
	private Text framerate = new Text(settingsPanel, SWT.SINGLE | SWT.BORDER);
	private Button saveSettingsButton = new Button( settingsPanel, SWT.PUSH );
	
	//define controls for the about panel
	private Label aboutLabel = new Label(aboutPanel, SWT.CENTER);
//	private Link webLink = new Link(aboutPanel, SWT.NONE);

	private Document settingsDocument = null;
	private Node fpsNode = null;
	private Node fullscreenNode = null;
	private Node language = null;
	private Image logo = null;

	/**
	 * Main entrypoint of the program
	 * @param args
	 */
	public static void main(String[] args) {
		new WorldOfZuulPortal();
	}
	/**
	 * Create world of zuul portal, creates controls
	 * and fills them with appropriate data. Provide
	 * functionality like creating a new game, loading
	 * saved games and configure settings like language
	 * and user controls
	 */
	public WorldOfZuulPortal(){
		//decorate the shell
		/*Image logo;
		
		if(new File(LOGO_PATH).exists()){
			logo = new Image(display, LOGO_PATH);
		    label.setImage(logo);
		}else{
			logo = new Image(display, 1, 1);//do not forget to initialize!!
			label.dispose();
		}
		*/
		newGames.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				//unused stub
			}

			public void widgetSelected(SelectionEvent e) {
				loadGameImage(newGames.getText());
			}
			
		});
		
	    fillLayout.type = SWT.VERTICAL;
	    
		//set layout algorithm data		
		GridLayout panelLayout = new GridLayout( 2, false );
		panelLayout.horizontalSpacing = 15;
		panelLayout.verticalSpacing = 10;
		panelLayout.marginTop = 10;
		newGamePanel.setLayout( panelLayout ); 
		loadGamePanel.setLayout( panelLayout );
		settingsPanel.setLayout( panelLayout );
		aboutPanel.setLayout( getXYFillLayout() );
			
		//setup new game tab
		newGamesLabel.setText( "Available world(s):" );	
		newPlayerLabel.setText( "Pick player name:" );
		newPlayer.setText( PLAYER_NAME_ASK );
		
		//add fancy textbox message, that automatically leaves when it receives focus
		newPlayer.addListener( SWT.FocusIn , new Listener(){
				public void handleEvent( Event event ) {
					if( newPlayer.getText().equals( PLAYER_NAME_ASK) ) newPlayer.setText( "" );
				}
			});
		newPlayer.addListener( SWT.FocusOut , new Listener(){
				public void handleEvent(Event event) {
					if( newPlayer.getText().equals("") ){
						newPlayer.setText(PLAYER_NAME_ASK);
						newPlayer.setBackground(new Color(display, 255,255,255));
					}
				}
			});	
		newPlayer.addListener( SWT.KeyUp, new Listener(){
				public void handleEvent( Event event ) {
					if( !validPlayerName( newPlayer.getText()) )
						newPlayer.setBackground( new Color(display, 255,200,200) );
					else
						newPlayer.setBackground( new Color(display, 200,255,200) );
				}
			});
				
		newGameButton.setText( "Start new game" );
		newGameButton.addListener( SWT.Selection, new Listener(){
				public void handleEvent(Event event) {
					newGame();
				}
			});
		
		//set extra layout information
		newGames.setLayoutData( getFillLayout() );
		newPlayer.setLayoutData( getFillLayout());
		newGameButton.setLayoutData( getButtonCenterLayout() );
		
//setup load game tab
		
		availableGamesLabel.setText( "Available world(s):" );
		availableNamesLabel.setText( "Savegames from:" );
		availableSavesLabel.setText( "Available saves:" );
		loadGameButton.setText( "Load selected game" );
		loadGameButton.addListener( SWT.Selection, new Listener(){
				public void handleEvent(Event event) {
					loadGame();
				}
			});
		
		availableGames.setLayoutData( getFillLayout() );
		availableNames.setLayoutData( getFillLayout() );
		availableSaves.setLayoutData( getFillLayout() );
		loadGameButton.setLayoutData( getButtonCenterLayout() );
		
		availableGames.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event event) {
					addNamesSaved(availableGames.getText());
					loadGameImage(availableGames.getText());
				}			
			});
		availableNames.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event event) {
					addAvailableSaves( availableGames.getText(),availableNames.getText() );
				}			
			});
		availableNames.setSize(200, availableNames.getSize().y);
		
//setup settings panel
		languageLabel.setText( "Preferred Language:" );
		availableLanguages.setLayoutData( getFillLayout() );		
		configureControlsLabel.setText( "Configure Controls:" );
		configureControlsButton.setLayoutData( getCenterLayout() );
		configureControlsButton.setText( "Configure..." );
		configureControlsButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				//upgrade interface to support configuration of more control sets!
				configureControls(0);
			}
		});
		
		fullscreenLabel.setText("Fullscreen:");
		fullscreenButton.setLayoutData( getCenterLayout() );
		framerateLabel.setText("Desired framerate");
		framerate.setText("40 ");
		framerate.setTextLimit(3);
		framerate.setLayoutData( getCenterLayout() );
		
		framerate.addListener( SWT.KeyUp, new Listener(){
			public void handleEvent( Event event ) {
				if( !isNumeric(framerate.getText()) )
					framerate.setBackground( new Color(display, 255,200,200) );
				else
					framerate.setBackground( new Color(display, 200,255,200) );
			}
		});
		
		saveSettingsButton.setText("Save Settings");
		saveSettingsButton.setLayoutData( getButtonCenterLayout() );
		saveSettingsButton.addListener( SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				saveSettings();
			}
		});
		
//setup about panel
		aboutLabel.setText(ABOUT_TEXT);
		//webLink.setText(ABOUT_WEBLINK);
		
//finalize and start
		//add composites to TabFolder items
		newGame.setControl( newGamePanel );
		loadGame.setControl( loadGamePanel );
		settings.setControl( settingsPanel );
		about.setControl( aboutPanel);
		
		tabFolder.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				// unused stub
			}

			public void widgetSelected(SelectionEvent e) {
				if(e.item == loadGame){
					availableGames.select(newGames.getSelectionIndex());
					addNamesSaved(availableGames.getText());
					loadGameImage(availableGames.getText());
				}else if(e.item == newGame){
					newGames.select(availableGames.getSelectionIndex());
					loadGameImage(newGames.getText());
				}
			}
		});
		
		//set the shell properties
		shell.setLayout( fillLayout );
		shell.setText( WINDOW_TITLE );
		shell.setSize( DEFAULT_PORTAL_WINDOW_SIZE );
		
		//set the tekst for the tabs
		newGame.setText("New game");
		loadGame.setText("Load game");
		settings.setText("Settings");
		about.setText("About");
		
		shell.setDefaultButton(newGameButton);
		
		//fill combo boxes in new and load game panel
		addGameInfo();
	
		//debug shortcuts:
		newGames.select(2);
		newPlayer.setText("Player1");
		
		loadSettings();
		
		loadGameImage(newGames.getText());
		
		//run layout algorithms and open shell
		tabFolder.pack();
		shell.setSize( DEFAULT_PORTAL_WINDOW_SIZE.x + logo.getBounds().x , DEFAULT_PORTAL_WINDOW_SIZE.y + logo.getBounds().y);
		shell.pack();
		shell.open();
		
		//message loop
		while( !shell.isDisposed() ) {
			if( !display.readAndDispatch() )
				display.sleep();
		}
		
		Game.getInstance().unload();

		display.dispose();
	}
	
	/**
	 * generates layout data object that horizontally centers the control
	 * without grabbing the excess horizontal space
	 * 
	 * @return layout object
	 */
	private GridData getCenterLayout(){
		GridData centerControl= new GridData();
		centerControl.horizontalAlignment = SWT.CENTER;
		return centerControl;
	}
	
	/**
	 * generates layout data object that stretches the control over the 
	 * space that is horizontally left over
	 * 
	 * @return layout object
	 */
	private GridData getFillLayout(){
		GridData fillControl = new GridData();
		fillControl.horizontalAlignment = SWT.FILL;
		fillControl.grabExcessHorizontalSpace = true;
		return fillControl;
	}
	
	/**
	 * generates layout data object that centers the control over the space
	 * that is horizontally and vertically left over
	 * 
	 * @return layout object
	 */
	private GridData getButtonCenterLayout(){
		GridData newGameButtonData = new GridData();
		newGameButtonData.horizontalAlignment = SWT.CENTER;
		newGameButtonData.verticalAlignment = SWT.CENTER;
		newGameButtonData.grabExcessVerticalSpace = true;
		newGameButtonData.grabExcessHorizontalSpace = true;
		newGameButtonData.horizontalSpan = 2;
		return newGameButtonData;
	}

	/**
	 * generates layout data object that stretches the control 
	 * horizontally as wel vertically
	 * 
	 * @return layout object
	 */
	private FillLayout getXYFillLayout(){
		FillLayout fillXY = new FillLayout();
		fillXY.marginHeight = 10;
		fillXY.marginWidth = 10;
		fillXY.type = SWT.VERTICAL;
		return fillXY;
	}

	/**
	 * Creates a message box and returns the the button clicked
	 * 
	 * @param title the title of the message box window
	 * @param message the message inside the message box window
	 * @param style the style and buttons, example: SWT.OK | SWT.CANCEL | SWT.ICON_WARNING;
	 * @return the button clicked in the message box
	 */
	public int msgBox(String title, String message, int style){
		MessageBox msg = new MessageBox(shell, style);
		msg.setText(title);
		msg.setMessage(message);
		return msg.open();
	}	
	
	/**
	 * adds the available languages to the settings panel's combo box
	 */
	public void addAvailableLanguages(){
		availableLanguages.add( "en - English" );
		availableLanguages.add( "nl - Nederlands" );
		availableLanguages.select( 0 );		
	}
	
	/**
	 * Checks if the playername is valid to
	 * be used inside the game and save filenames
	 * 
	 * @param name the playername to be used
	 * @return playername == valid
	 */
	public boolean validPlayerName(String name){
		Pattern validname = Pattern.compile("^" + Game.VALID_PLAYER_NAME + "$");
		Matcher check = validname.matcher(name);
		return check.find();
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

	/**
	 * gets files in directory 
	 * 
	 * @param directory path to traverse
	 * @param endsWith the filename must end with
	 * @return File[] array
	 */
	public File[] getSavegames(String worldName){
		File root = new File( Game.gameDataDir() + "/games/" + worldName + "/savegames");
		if( !root.exists() )
			return null;
		
		//exclude hidden folders starting with dot
	    FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.endsWith(Game.SAVE_GAME_EXTENSION);
	        }
	    };
	    
	    return root.listFiles(filter);	
	}
	
	/**
	 * fills all the combo boxes in the new game, load game
	 * and settings panel, and fills the portal with data
	 */
	public void addGameInfo(){
		newPlayer.setEnabled( false );
		newGameButton.setEnabled( false );
		loadGameButton.setEnabled( false );
		
		addAvailableLanguages();
		
		File gameroot = new File( Game.gameDataDir() + "/games" );
	    if( !gameroot.exists() )
	    	return;
	    
		//exclude hidden folders starting with dot
	    FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return !name.startsWith(".");
	        }
	    };
	    
	    File[] games = gameroot.listFiles( filter );
	    
		for(int i=0; i < games.length ;i++){
			newGames.add(games[i].getName());
			availableGames.add(games[i].getName());
		}
		
		//assume availableGames is also filled by the loop above!
		if(newGames.getItemCount() >= 1){
			newGames.select(0);
			availableGames.select(0);
	
			newPlayer.setEnabled( true );
			newGameButton.setEnabled( true );
			addNamesSaved( availableGames.getText() );			
		}
	}
	
	/**
	 * adds persons who saved in the given world
	 * into the combo box in the load game panel
	 * 
	 * @param worldName of the world to lookup
	 */
	public void addNamesSaved(String worldName){
		availableSaves.removeAll();
		availableNames.removeAll();
		loadGameButton.setEnabled( false );
		
	    File[] saves = getSavegames(worldName);
	    
	    if( saves != null ){
		    Pattern savename = Pattern.compile(Game.SAVE_GAME_NAME);
		    ArrayList<String> list = new ArrayList<String>();
		    
			for(int i=0; i < saves.length ;i++){
				Matcher parts = savename.matcher(saves[i].getName());
	
				//add only unique names
				if( parts.find() && !list.contains(parts.group(1)) )
					list.add(parts.group(1));
			}
			
			for(String name : list){
				availableNames.add(name);
			}
			
			if(availableNames.getItemCount() >= 1){
				String [] names = availableNames.getItems();
				Arrays.sort(names);
				availableNames.setItems(names);
				
				availableNames.select(0);
				addAvailableSaves(worldName, availableNames.getText());
			}
	    }
	}
	
	/**
	 * adds the available saves to load 
	 * into the combo box load game panel
	 * 
	 * @param worldName the name of the selected world
	 * @param personName the name of the selected person
	 */
	public void addAvailableSaves(String worldName, String personName){
		availableSaves.removeAll();
		loadGameButton.setEnabled( false );
	    File[] saves = getSavegames(worldName) ;
	    
	    if( saves != null ){	    
		    Pattern savename = Pattern.compile(Game.SAVE_GAME_NAME);
		    
			for(int i=0; i < saves.length ;i++){
				String filename = saves[i].getName();
				Matcher parts = savename.matcher(filename);
					
				if( parts.find() && parts.group(1).equals(personName))
					availableSaves.add(parts.group(4) + "-" + parts.group(5) + "-" + parts.group(6) + " " + parts.group(2) + ":" +  parts.group(3));				
			}
			
			if(availableSaves.getItemCount() >= 1){
				String [] savenames = availableSaves.getItems();
				Arrays.sort(savenames);
				availableSaves.setItems(savenames);
				availableSaves.select(0);		
				
				loadGameButton.setEnabled( true );
			}
	    }
	}

	/**
	 * initializes the game object with a new  
	 * profile, with the parameters filled in the UI 
	 */
	public void newGame(){
		if( !validPlayerName(newPlayer.getText()) ){
			//if statement needed for return; to work, else: unreachable code
			if( msgBox("Invalid Player Name", "The playername you entered is invalid, please rename!", SWT.OK | SWT.ICON_WARNING) == SWT.OK )
				return;
		}else if(newGames.getItemCount() <= 0){
			if( msgBox("No Avaialable Games", "There are no available games found. You should probably reinstall the game.", SWT.OK | SWT.ICON_ERROR) == SWT.OK )
				return;			
		}
		
		if ( msgBox("Create New Game", "Are you sure you want to create a new game with the player name '" + newPlayer.getText() + "'?", SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION) == SWT.OK){
			try {
				//unload current instance;
				Game.getInstance().unload();
				Game.getInstance().createNewGame(newGames.getText(), newPlayer.getText(), "en");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * initializes the game object with a previously  
	 * saved profile. The profile itself is determined
	 * in the UI 
	 */	
	public void loadGame(){
		if(availableGames.getItemCount() <= 0){
			if( msgBox("No Available Games", "There are no available games found. You should probably reinstall the game.", SWT.OK | SWT.ICON_ERROR) == SWT.OK )
				return;			
		}else if(availableNames.getItemCount() <= 0){
			if( msgBox("No Saved Games", "There are no players found which could have saved, please start a new game.", SWT.OK | SWT.ICON_ERROR) == SWT.OK )
				return;			
		}
		//this can't happen, because all saves and names are resolved from the same filename, and names are checked above
		//else if(availableSaves.getItemCount() <= 0)
		
		if ( msgBox("Load Game", "Are you sure you want to load the game of'" + availableNames.getText() + "', saved on '" + availableSaves.getText() + "'?", SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION) == SWT.OK){
			try {
				Pattern datetime = Pattern.compile("([0-9][0-9])-([0-9][0-9])-([0-9][0-9][0-9][0-9]) ([0-9][0-9]):([0-9][0-9])");
				Matcher re = datetime.matcher(availableSaves.getText());
				if(re.find()){
					//TODO constructing filenames this way can be error prone
					//construct filename, and check if it exists
					File filename = new File( Game.gameDataDir() +
									"/games/" + 
									availableGames.getText() + 
									"/savegames/" + 
									availableNames.getText() + 
									"_" + re.group(4) + "u" + 
									re.group(5) + "m_" + 
									re.group(1) + "d" + 
									re.group(2) + "m" + 
									re.group(3) + "y" + 
									Game.SAVE_GAME_EXTENSION );
					
					if( filename.exists() ){
						//unload any current configuration
						Game.getInstance().unload();
						Game.getInstance().loadGame( filename.getPath() );
					}else{
						msgBox("Failed Opening Savegame", "Canont load save game, filename could not be resolved! This is probably a program error, please report this bug!", SWT.OK | SWT.ICON_INFORMATION);
					}
				}else{
					msgBox("Failed Opening Savegame", "Canont load save game, filename could not be resolved! This is probably a program error, please report this bug!", SWT.OK | SWT.ICON_INFORMATION);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads the image that matches the selected game!
	 * @param gameName
	 */
	public void loadGameImage(String gameName){
		//update to match more image types!
		String path = Game.gameDataDir() + "/games/" + gameName + "/logo.png";
		
		if(new File(path).exists()){
			logo = new Image(display, path);
		    label.setImage(logo);
		}else{
			if(new File(Game.gameDataDir() + "/" + LOGO_PATH).exists()){
				logo = new Image(display, LOGO_PATH);
				label.setImage(logo);
			}else{
				logo = new Image(display, 1, 1);
				label.setImage(logo);
			}
		}
	}

	/**
	 * loads the settings XML document
	 */
	public void loadSettings(){
		try {
			String settingsLocation = Game.gameDataDir() + "/" + Game.SETTINGS_FILE;
			settingsDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(settingsLocation);
			loadFramerate();
			loadFullscreen();
			loadLanguage();
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads the framerate from the internal settings document file and updates the user interface
	 */
	protected void loadFramerate(){
		if(settingsDocument != null){ 
			NodeList framerateNodes = settingsDocument.getElementsByTagName("framerate");
			if(framerateNodes.getLength() > 0){
				Node framerateNode = framerateNodes.item(0);	//select first node
				
				if(framerateNode != null){
					NamedNodeMap attributes = framerateNode.getAttributes();
					fpsNode = attributes.getNamedItem("fps");
					if(fpsNode != null){
						if(isNumeric(fpsNode.getNodeValue())){
							framerate.setText(fpsNode.getNodeValue());
							return;
						}
					}
				}
			}
		}
		
		framerate.setText("" + Game.DEFAULT_FRAMERATE);
	}
	
	/**
	 * Reads the language from the internal settings document file and updates the user interface
	 */
	protected void loadLanguage(){
		if(settingsDocument != null){ 
			NodeList languageNodes = settingsDocument.getElementsByTagName("language");
			if(languageNodes.getLength() > 0){
				Node languageNode = languageNodes.item(0);	//select first node
				
				if( languageNode != null){
					NamedNodeMap attributes = languageNode.getAttributes();
					language = attributes.getNamedItem("lang");
					if(language != null){
						String lang = language.getNodeValue();
						for(int i = 0 ; i < availableLanguages.getItemCount(); i++){
							if(availableLanguages.getItem(i).startsWith(lang)){
								availableLanguages.select(i);
								break;
							}
						}
						
					}
				}
			}
		}
	}
	
	/**
	 * Reads the fullscreen property from the xml file and updates the user interface
	 */
	protected void loadFullscreen(){
		if(settingsDocument != null){ 
			NodeList fullscreenNodes = settingsDocument.getElementsByTagName("fullscreen");
			if(fullscreenNodes.getLength() > 0){
				fullscreenNode = fullscreenNodes.item(0);	//select first node
				
				if(fullscreenNode != null){
					NamedNodeMap attributes = fullscreenNode.getAttributes();
					fullscreenNode = attributes.getNamedItem("on");
					if(fullscreenNode != null){
						if(fullscreenNode.getNodeValue().equals("true")){
							fullscreenButton.setSelection(true);
							return;
						}
					}
				}
			}
		}
		
		fullscreenButton.setSelection(false);	
	}
	
	/**
	 * Updates the memory xml file with the framerate from the user interface
	 */
	protected void writeFramerate(){
		if(fpsNode == null || !isNumeric(framerate.getText())) return;
		fpsNode.setNodeValue(framerate.getText());
	}
	
	/**
	 * Updates the memory xml file with the fullscreen true|false depending on the checkbox in the user interface
	 */
	protected void writeFullscreen(){
		if(fullscreenNode == null) return;
		
		if(fullscreenButton.getSelection())
			fullscreenNode.setNodeValue("true");
		else
			fullscreenNode.setNodeValue("false");
	}

	/**
	 * Updates the memory xml file with the currently selected language
	 */
	protected void writeLanguage(){
		if(language != null && availableLanguages.getText().length() >= 2){
			language.setNodeValue(availableLanguages.getText().substring(0, 2));
		}
	}
	
	/**
	 * Saves the settings XML document
	 */
	public void saveSettings(){
		XMLSerializer serializer = new XMLSerializer();
	    try {
	    	//sync user interface and XML
	    	writeFramerate();
	    	writeFullscreen();
	    	writeLanguage();
	    	
	    	//serialize and write xml file
			serializer.setOutputCharStream(new java.io.FileWriter(Game.SETTINGS_FILE));
			serializer.serialize(settingsDocument);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Reads current controls from config file (if exists)
	 * or loads default controls and shows the user a dialog
	 * in which he can change them, this function block execution
	 * until dialog is closed
	 * 
	 * @param controlSetId (NOT USED YET!) the controlset to configure
	 */
	public void configureControls(int controlSetId){
		
		NodeList controls = (NodeList)settingsDocument.getElementsByTagName("controlset");
		Node controlSet;
		
		if(controls.getLength() > 0){
			controlSet = controls.item(0);
		}else{
			System.out.println("ERROR: No controlset found in XML!!");
			return;
		}
		
		ConfigureControls ctrlConfig = new ConfigureControls(display, DEFAULT_CONFIGURE_CONTROL_WINDOW_SIZE, controlSet);
		ctrlConfig.open();
	}

}
