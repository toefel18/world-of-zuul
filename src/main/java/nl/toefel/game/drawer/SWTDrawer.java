package nl.toefel.game.drawer;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.awt.geom.RectangularShape;
import java.io.File;

import nl.toefel.game.Game;
import nl.toefel.game.TextFormatter;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.gamedisplays.ComplexDisplay;
import nl.toefel.game.gamedisplays.ComplexDisplayTab;
import nl.toefel.game.roomobject.IRoomObject;
import nl.toefel.gameeditor.GameEditor;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;


/**
 * Draws the context of a particular object into a SWT window
 * 
 */
public class SWTDrawer implements IDrawer, KeyListener, DisposeListener, ControlListener{

	/** Holds the preloaded imagepath<->image pairs */
	protected static HashMap<String, Image> images = new HashMap<String, Image>();
	protected static HashMap<Image, HashMap<java.awt.Rectangle, Image>> repeatedImages = new HashMap<Image, HashMap<java.awt.Rectangle, Image>>();
	protected IRoomObject focus;

	/** Graphical context handles */
	protected Display display;
	protected Shell shell;
	protected Image drawable;
	protected Canvas canvas;
	protected Rectangle canvasBounds;
	protected GC gc;
	protected Image buffer;
	protected GC bufferDrawer;
	protected nl.toefel.game.roomobject.Point focusLocation;
	protected Point canvasCornerCoordinates;

	/** The game this drawer belongs to */
	protected Game game;

	/**
	 * keyPressed, interface of Keylistener
	 * 
	 * @param e
	 *            KeyEvent context info
	 */
	public void keyPressed(KeyEvent e) {
		game.keyPressed(e.keyCode);
		e.doit = false;
	}

	/**
	 * keyReleased, interface of Keylistener
	 * 
	 * @param e
	 *            KeyEvent context info
	 */
	public void keyReleased(KeyEvent e) {
		game.keyReleased(e.keyCode);
		e.doit = false;
	}

	/**
	 * widgetDisposed, interface of DisposeListener
	 * 
	 * @param e
	 *            DisposeEvent context info
	 */
	public void widgetDisposed(DisposeEvent e) {
		game.removeDrawer((IDrawer) this);
	}

	/**
	 * controlMoved, interface of ControlListener
	 * 
	 * @param e
	 *            ControlEvent context info
	 */
	public void controlMoved(ControlEvent e) {
	}

	/**
	 * controlResized, interface of ControlListener
	 * 
	 * @param e
	 *            ContrlEvent context info
	 */
	public void controlResized(ControlEvent e) {
		synchronized (this) {
			shell.layout();
			canvasBounds = canvas.getBounds();
			buffer = new Image(this.display, canvasBounds);
			bufferDrawer = new GC(buffer);
			game.redraw();
		}
	}

	/**
	 * Creates a drawer object that focusses on a room object (uses current
	 * display and disables fullscreen)
	 * 
	 * @param focus
	 *            room object to have as center
	 */
	public SWTDrawer(IRoomObject focus) {
		// forward call to another constructor
		this(focus, false);
	}

	/**
	 * Creates a drawer on the current display and focusses on an object
	 * 
	 * @param focus
	 *            room object to focus on
	 * @param fullscreen
	 *            should it display in fullscreen
	 */
	public SWTDrawer(IRoomObject focus, boolean fullscreen) {
		// forward call to other constructor
		this(Display.getCurrent(), focus, fullscreen);
	}

	/**
	 * Creates a drawer object that focusses on a room object (fullscreen set to
	 * false)
	 * 
	 * @param display
	 *            the computer display to draw on
	 * @param focus
	 *            the roomobject to have as center
	 */
	public SWTDrawer(Display display, IRoomObject focus) {
		this(display, focus, false);
	}

	/**
	 * Creates a drawer object that focusses on a room object (fullscreen is
	 * pickable)
	 * 
	 * @param display
	 *            the computer display to draw on
	 * @param focus
	 *            the roomobject to have as center
	 * @param fullscreen
	 *            show the window in fullscreen modus
	 */
	public SWTDrawer(Display display, IRoomObject focus, boolean fullscreen) {
		this.display = display;
		this.focus = focus;
		int shellStyle = fullscreen ? SWT.NONE : SWT.BORDER | SWT.CLOSE
				| SWT.RESIZE;

		game = Game.getInstance();

		shell = GameEditor.getInstance() != null ? GameEditor.getInstance().getShell() : new Shell(this.display, shellStyle);// , /*SWT.APPLICATION_MODAL
													// |*/ SWT.CLOSE |
													// SWT.BORDER );
		shell.setLayout(new FillLayout());

		shell.addKeyListener(this);

		// Impossible to change style bits of shell window after creation
		canvas = GameEditor.getInstance() != null ? GameEditor.getInstance().getCanvas() : new Canvas(shell, SWT.NONE);
		canvas.addKeyListener(this);
		canvasBounds = canvas.getBounds();

		// notifies game if window is closed
		shell.addDisposeListener(this);
		
		if(GameEditor.getInstance() == null)
			shell.setSize(800, 600);

		if (fullscreen)
			shell.setMaximized(true);

		// updates the bounds if window is resized
		shell.addControlListener(this);
		shell.open();

		gc = new GC(canvas);
		canvasBounds = canvas.getBounds();
	}

	/**
	 * Draws the display.
	 */
	public void draw() {
		// Empty buffer if present.
		if (buffer != null)
			buffer.dispose();

		if (bufferDrawer != null)
			bufferDrawer.dispose();

		buffer = new Image(this.display, canvasBounds.width,
				canvasBounds.height);
		bufferDrawer = new GC(buffer);

		// Obtain the location of the focused IRoomObject.
		focusLocation = focus.getLocation();

		focus.getRoom().sortRoomObjects();

		drawRoom();
		drawRoomObjects();

		if (game.getShowDebug())
			drawDebug();

		// drawDebug();

		drawComplexDisplay();
		
		// a display which shows up when somebody talks or a menu pops up
		drawCommandDisplay();
		

		// (Debug) Draw crosshair.
		// bufferDrawer.drawOval(canvasBounds.width / 2 - (250 / 2),
		// canvasBounds.height / 2 - (250 / 2), 250, 250);

		// draw backbuffer on canvas
		gc.drawImage(buffer, 0, 0);
	}

	/**
	 * Draws all the debug contexts
	 */
	private void drawDebug() {
		debugDrawBoundaries();
		debugDrawRanges();
		debugDrawInfo();
		debugDrawCollisions();
		debugDrawLegend();
		debugDrawCommandRanges();
	}

	/**
	 * Draws all command ranges of all roomobjects.
	 */
	private void debugDrawCommandRanges()
	{
		try
		{
			for (IRoomObject iRoomObject : focus.getRoom().getRoomObjects())
			{
				nl.toefel.game.roomobject.Point	location 	= iRoomObject.getLocation();
				java.awt.Rectangle 		size		= iRoomObject.getSize();
				
				int commandRange = iRoomObject.getCommandRange();
				
				bufferDrawer.setForeground(new Color(null, 255, 255, 0));
				bufferDrawer.drawRectangle(
												-2 + location.getX() - commandRange - canvasCornerCoordinates.x - (size.width / 2),
												-2 + location.getY() - commandRange - canvasCornerCoordinates.y -  size.height,
												 4 + size.width 	+ (commandRange * 2),
												 4 + size.height + (commandRange * 2)
											);
			}
		} 
		catch (ConcurrentModificationException e)
		{
			System.out.println("ConcurrentModificationException: " + e.getStackTrace());

			// Recover by re-execute the method.
			debugDrawCommandRanges();
		}
	}
	
	private void debugDrawLegend()
	{
		Color color1 = new Color(display, 255, 255, 255);
		Color color2 = new Color(display, 200, 200, 200);

		bufferDrawer.setForeground(color1);
		bufferDrawer.setBackground(color2);

		bufferDrawer.fillGradientRectangle(10, canvasBounds.height - 110, 100, 100, true);

		bufferDrawer.setBackground(new Color(null, 255, 0, 0));
		bufferDrawer.drawText("Boundaries", 15, canvasBounds.height - 110 + 10, false);

		bufferDrawer.setForeground(new Color(null, 0, 0, 0));
		bufferDrawer.setBackground(new Color(null, 0, 255, 0));
		bufferDrawer.drawText("Ranges", 15, canvasBounds.height - 90 + 10, false);

		bufferDrawer.setForeground(new Color(null, 255, 255, 255));
		bufferDrawer.setBackground(new Color(null, 0, 0, 255));
		bufferDrawer.drawText("Collisions", 15, canvasBounds.height - 70 + 10, false);
		
		bufferDrawer.setForeground(new Color(null, 0, 0, 0));
		bufferDrawer.setBackground(new Color(null, 255, 255, 0));
		bufferDrawer.drawText("CommandRange", 15, canvasBounds.height - 50 + 10, false);
	}

	/**
	 * Draws the command display, if present
	 */
	private void drawCommandDisplay() {

		Font oldFont = bufferDrawer.getFont();
		CommandDisplay commandDisplay = game.getCommandDisplay();

		if (commandDisplay == null)
			return;

		// set the location with the canvas corner calculated into the
		// coordinates(can be smaller than window if room is smaller than
		// display)
		nl.toefel.game.roomobject.Point location = commandDisplay.getLocation();
		location = new nl.toefel.game.roomobject.Point((canvasCornerCoordinates.x * -1)
				+ location.getX(), (canvasCornerCoordinates.y * -1)
				+ location.getY());

		int numCommands = commandDisplay.countCommands();

		// TODO update with drawer specific sollution!
		// Textformatter breaks the test message in multiple lines that fit in
		// the window, and tells how many lines should be used
		TextFormatter text = new TextFormatter(commandDisplay.getText(),
				Game.MESSAGE_BOX_FONT_NAME, "", Game.MESSAGE_BOX_FONT_SIZE,
				(int) Math.floor(Game.MESSAGE_BOX_WIDTH * 0.88)); // account for
																	// a little
																	// bit
																	// correction
																	// by taking
																	// 90%!

		ArrayList<ICommand> commands = commandDisplay.getCommands();
		// HashMap<String, ICommand> commands = commandDisplay.getCommandMap();

		// calculate height, according to num lines, and num commands
		int height = (int) ((2 * Game.MESSAGE_BOX_PADDING)
				+ ((Game.MESSAGE_BOX_FONT_SIZE) * text.getLineCount())
				+ (Game.MESSAGE_BOX_LINE_SPACING * text.getLineCount())
				+ (Game.MESSAGE_BOX_LINE_SPACING * (numCommands))
				+ (Game.MESSAGE_BOX_FONT_SIZE * (numCommands + 1)) + // +1 for
																		// whitespace
		(Game.MESSAGE_BOX_MENU_PADDING * (numCommands + 1)));

		int width = Game.MESSAGE_BOX_WIDTH + (2 * Game.MESSAGE_BOX_PADDING);

		bufferDrawer.setFont(new Font(this.display, Game.MESSAGE_BOX_FONT_NAME,
				Game.MESSAGE_BOX_FONT_SIZE, SWT.NONE));
		bufferDrawer.setAlpha(230);
		bufferDrawer.setBackground(new Color(this.display, 50, 50, 50));
		bufferDrawer.fillRoundRectangle(location.getX(), location.getY(),
				width, height, Game.MESSAGE_BOX_CORNER_ROUNDING,
				Game.MESSAGE_BOX_CORNER_ROUNDING);
		bufferDrawer.setForeground(new Color(this.display, 255, 255, 255));
		bufferDrawer.setAlpha(255);

		ArrayList<String> lines = text.getLines();
		int y = Game.MESSAGE_BOX_PADDING;

		for (String line : lines) {
			bufferDrawer.drawText(line, location.getX()
					+ Game.MESSAGE_BOX_PADDING, location.getY() + y, true);
			y = y + Game.MESSAGE_BOX_FONT_SIZE + Game.MESSAGE_BOX_LINE_SPACING;
		}

		// whitespace between text message and commands
		y = y + Game.MESSAGE_BOX_FONT_SIZE + Game.MESSAGE_BOX_LINE_SPACING;

		int selectedCommand = commandDisplay.getSelected();
		int counter = 0;
		for (ICommand command : commands) {
			bufferDrawer.drawText(command.getName(), location.getX()
					+ Game.MESSAGE_BOX_PADDING, location.getY() + y, true);
			if (counter++ == selectedCommand) {
				bufferDrawer.drawRoundRectangle(location.getX()
						+ Game.MESSAGE_BOX_PADDING
						- Game.MESSAGE_BOX_MENU_PADDING, location.getY() + y
						- Game.MESSAGE_BOX_MENU_PADDING, Game.MESSAGE_BOX_WIDTH
						+ (2 * Game.MESSAGE_BOX_MENU_PADDING),
						Game.MESSAGE_BOX_FONT_SIZE
								+ (3 * Game.MESSAGE_BOX_MENU_PADDING),
						Game.MESSAGE_BOX_MENU_ROUNDING,
						Game.MESSAGE_BOX_MENU_ROUNDING);
			}

			y = y + Game.MESSAGE_BOX_FONT_SIZE + Game.MESSAGE_BOX_LINE_SPACING
					+ Game.MESSAGE_BOX_MENU_PADDING; // include for rectangle!
		}

		bufferDrawer.setFont(oldFont);
	}

	/**
	 * Draws the complex display, if present!
	 */
	private void drawComplexDisplay()
	{	
		Font oldFont = bufferDrawer.getFont();
		ComplexDisplay complexDisplay = game.getComplexDisplay();
		 
		if(complexDisplay == null)
			return;
		
		//try to fill the screen! 
		nl.toefel.game.roomobject.Point location = new nl.toefel.game.roomobject.Point(10, 10);

		bufferDrawer.setFont(new Font(this.display, Game.COMPLEX_DISPLAY_FONT_NAME, Game.COMPLEX_DISPLAY_TAB_FONT_SIZE, SWT.BOLD));
		bufferDrawer.setAlpha(230);
		bufferDrawer.setBackground(new Color(this.display, 50, 50, 50));
		bufferDrawer.fillRoundRectangle(Game.COMPLEX_DISPLAY_PADDING, 
										Game.COMPLEX_DISPLAY_PADDING, 
										canvasBounds.width - (Game.COMPLEX_DISPLAY_PADDING * 2), 
										canvasBounds.height - (Game.COMPLEX_DISPLAY_PADDING * 2), 
										Game.COMPLEX_DISPLAY_ROUNDING, 
										Game.COMPLEX_DISPLAY_ROUNDING); 
		
		bufferDrawer.setForeground(new Color(this.display, 255, 255, 255));
		bufferDrawer.setAlpha(255);
				
		ArrayList<ComplexDisplayTab> tabs = complexDisplay.getTabs();
		if(tabs == null) return;
		
		final int DEFAULT_DISPOSITION = Game.COMPLEX_DISPLAY_ROUNDING;
		int x = Game.COMPLEX_DISPLAY_PADDING + DEFAULT_DISPOSITION;
		int y = Game.COMPLEX_DISPLAY_PADDING + DEFAULT_DISPOSITION;
		
		ComplexDisplayTab selectedTab = complexDisplay.getSelectedTab();
		
		for(int i = 0; i < tabs.size(); i++)
		{
			ComplexDisplayTab tab = tabs.get(i);
			Point stringSpace = bufferDrawer.stringExtent(tab.getName());
			
			if( tab == selectedTab )
			{
				bufferDrawer.setBackground(new Color(this.display, 25, 25, 25));
				
				//if selected, draw light border!
				Color backupForeground = bufferDrawer.getForeground();
				bufferDrawer.setForeground(new Color(this.display, 120, 120, 120));
				
				bufferDrawer.drawRoundRectangle(x - Game.COMPLEX_DISPLAY_TAB_PADDING -1, 
						y - Game.COMPLEX_DISPLAY_TAB_PADDING -1, 
						stringSpace.x + (2*Game.COMPLEX_DISPLAY_TAB_PADDING) + 1,
						stringSpace.y + (2*Game.COMPLEX_DISPLAY_TAB_PADDING) + 1, 
						Game.COMPLEX_DISPLAY_TAB_ROUNDING, 
						Game.COMPLEX_DISPLAY_TAB_ROUNDING);
				
				bufferDrawer.setForeground(backupForeground);
			}
			else
			{
				bufferDrawer.setBackground(new Color(this.display, 75, 75, 75));
			}
			
			bufferDrawer.fillRoundRectangle(x - Game.COMPLEX_DISPLAY_TAB_PADDING, 
											y - Game.COMPLEX_DISPLAY_TAB_PADDING, 
											stringSpace.x + (2*Game.COMPLEX_DISPLAY_TAB_PADDING),
											stringSpace.y + (2*Game.COMPLEX_DISPLAY_TAB_PADDING), 
											Game.COMPLEX_DISPLAY_TAB_ROUNDING, 
											Game.COMPLEX_DISPLAY_TAB_ROUNDING);
			
			bufferDrawer.drawText(tab.getName(), x, y, true);
			
			x += stringSpace.x + Game.COMPLEX_DISPLAY_TAB_SPACING;
		}
		
		if(selectedTab != null)
		{
			//increase Y
			x =  Game.COMPLEX_DISPLAY_PADDING + DEFAULT_DISPOSITION;
			y += bufferDrawer.stringExtent(selectedTab.getName()).y + (Game.COMPLEX_DISPLAY_LINE_SPACING * 2) + Game.COMPLEX_DISPLAY_TAB_PADDING;
			bufferDrawer.setFont(new Font(this.display, Game.COMPLEX_DISPLAY_FONT_NAME, Game.COMPLEX_DISPLAY_FONT_SIZE, SWT.NONE));
			
			//format this text!
			bufferDrawer.drawText(selectedTab.getText(), x, y, true);
			
			y += bufferDrawer.stringExtent(selectedTab.getText()).y + (Game.COMPLEX_DISPLAY_LINE_SPACING * 3);
			
			//get the items
			ComplexDisplayTab.Item selectedItem = selectedTab.getSelectedItem();
			ArrayList<ComplexDisplayTab.Item> items = selectedTab.getItems();
						
			//the width and height available for placing items!
			int innerWidth = canvasBounds.width - (Game.COMPLEX_DISPLAY_PADDING * 2) - (DEFAULT_DISPOSITION * 2) ; 
			int innerHeight = canvasBounds.height - (Game.COMPLEX_DISPLAY_PADDING * 2) - y; 
			
			//calculate how many items fit in the display, and stretch their width to fully cover the display 
			int columns = (innerWidth / (Game.COMPLEX_DISPLAY_MIN_ITEM_WIDTH + ((items.size() - 1) * Game.COMPLEX_DISPLAY_ITEM_SPACING)));
			columns = (items.size() < columns ?  items.size() : columns);
			if(columns <= 0) columns = 1;
			
			int itemWidth =  (innerWidth - ((items.size() - 1) * Game.COMPLEX_DISPLAY_ITEM_SPACING)) / columns;
			int itemHeight = Game.COMPLEX_DISPLAY_MIN_ITEM_HEIGHT;
			int currentColumn = 0;

			//draw all the items
			for(int i = 0; i < items.size(); i++)
			{
				ComplexDisplayTab.Item item = items.get(i);
				
				Color oldBackground = bufferDrawer.getBackground();
				Color oldForeground = bufferDrawer.getForeground();
	
				final int DEFAULT_SPACE = 10;
				
				int currentX = x + (currentColumn * itemWidth) + (currentColumn * Game.COMPLEX_DISPLAY_ITEM_SPACING);
			
				if( item == selectedItem )
				{
					bufferDrawer.setForeground(new Color(this.display, 80, 80, 80));
					bufferDrawer.setBackground(new Color(this.display, 45, 45, 45));
					
					//The window will be draw relative to the upper left room coordinates, cancel out this effect!
					nl.toefel.game.roomobject.Point commandWindowLocation = new nl.toefel.game.roomobject.Point((canvasCornerCoordinates.x)
							+ currentX, (canvasCornerCoordinates.y)
							+ y);
					
					//update the command display location
					complexDisplay.setCommandDisplayLocation(commandWindowLocation);
				}
				else
				{						
					bufferDrawer.setForeground(new Color(this.display, 95, 95, 95));
					bufferDrawer.setBackground(new Color(this.display, 65, 65, 65));
				}
				
				bufferDrawer.fillGradientRectangle(	currentX, 
													y, 
													itemWidth, 
													itemHeight, 
													true);
				
				bufferDrawer.setForeground(oldForeground);
				bufferDrawer.setBackground(oldBackground);
				
				
				
				Image img = images.get(item.imagePath);
				
				if(img != null){
					bufferDrawer.drawImage(	img, 
											0, 
											0,
											img.getBounds().width, 
											img.getBounds().height, 
											currentX + DEFAULT_SPACE, 
											y + ((itemHeight / 2) - (Game.COMPLEX_DISPLAY_ITEM_IMAGE_SIZE /2)), 
											Game.COMPLEX_DISPLAY_ITEM_IMAGE_SIZE, 
											Game.COMPLEX_DISPLAY_ITEM_IMAGE_SIZE);
				}
								
				currentX += (DEFAULT_SPACE * 2 ) + Game.COMPLEX_DISPLAY_ITEM_IMAGE_SIZE; 
				
				bufferDrawer.drawText(item.name, currentX, y + ((itemHeight / 2) - (img.getBounds().height /2)), true);
								
				if(++currentColumn >= columns)
				{
					currentColumn = 0;
					y += Game.COMPLEX_DISPLAY_MIN_ITEM_HEIGHT + Game.COMPLEX_DISPLAY_ITEM_SPACING ; 
				}
			}		
		}
		 
		bufferDrawer.setFont(oldFont);
	}

	/**
	 * Draws debug boundaries
	 */
	private void debugDrawBoundaries() {
		try {
			for (IRoomObject iRoomObject : focus.getRoom().getRoomObjects()) {
				for (RectangularShape boundary : iRoomObject.getBoundaries()) {
					bufferDrawer.setForeground(new Color(null, 255, 0, 0));
					bufferDrawer.drawRectangle(boundary.getBounds().x
							- canvasCornerCoordinates.x, boundary.getBounds().y
							- canvasCornerCoordinates.y,
							boundary.getBounds().width,
							boundary.getBounds().height);

				}
			}
		} catch (ConcurrentModificationException e) {
			System.out.println("ConcurrentModificationException: "
					+ e.getStackTrace());

			// Recover by re-execute the method.
			debugDrawBoundaries();
		}
	}

	private void debugDrawRanges() {
		for (IRoomObject iRoomObject : focus.getRoom().getRoomObjects()) {
			bufferDrawer.setForeground(new Color(null, 0, 255, 0));
			bufferDrawer.drawRectangle(-1 + iRoomObject.getLocation().getX()
					- (iRoomObject.getSize().width / 2)
					- canvasCornerCoordinates.x, -1
					+ iRoomObject.getLocation().getY()
					- iRoomObject.getSize().height - canvasCornerCoordinates.y,
					2 + iRoomObject.getSize().width,
					2 + iRoomObject.getSize().height);
		}
	}

	private void debugDrawInfo() {
		bufferDrawer.setBackground(new Color(null, 255, 255, 255));
		bufferDrawer.setForeground(new Color(null, 0, 0, 0));

		int num = 0;

		for (IRoomObject iRoomObject : focus.getRoom().getRoomObjects()) {
			bufferDrawer.drawText(num + " (" + iRoomObject.getIDString() + ")",
					iRoomObject.getLocation().getX()
							- (iRoomObject.getSize().width / 2)
							- canvasCornerCoordinates.x, iRoomObject
							.getLocation().getY()
							- canvasCornerCoordinates.y);
			num++;
		}
	}

	private void debugDrawCollisions() {
		try {
			for (RectangularShape collision : focus.getRoom().getCollisions()) {
				bufferDrawer.setForeground(new Color(null, 0, 0, 255));

				bufferDrawer.drawRectangle(1 + (int) collision.getX()
						- canvasCornerCoordinates.x, 1 + (int) collision.getY()
						- canvasCornerCoordinates.y, -2
						+ (int) collision.getWidth(), -2
						+ (int) collision.getHeight());
			}

			for (IRoomObject iRoomObject : focus.getRoom().getRoomObjects()) {
				// iRoomObject.getCollision().

				for (RectangularShape collision : iRoomObject
						.getAbsoluteCollisions()) {
					bufferDrawer.setForeground(new Color(null, 0, 0, 255));

					bufferDrawer.drawRectangle(
					/* TODO NULPOINTER EXECEPTION */1 + (int) collision.getX()
							- canvasCornerCoordinates.x, 1
							+ (int) collision.getY()
							- canvasCornerCoordinates.y, -2
							+ (int) collision.getWidth(), -2
							+ (int) collision.getHeight());
				}
			}
		} catch (ConcurrentModificationException e) {
			System.out.println("ConcurrentModificationException: "
					+ e.getStackTrace());

			// Recover by re-execute the method.
			debugDrawCollisions();
		}
	}

	/**
	 * Returns a repeated or cropped texture based on the given texture and
	 * size.
	 * 
	 * @param texture
	 *            The texture to repeat or cropped.
	 * @param size
	 *            The width and height the repeated or cropped texture would be.
	 * @return
	 */
	private Image repeatTexture(Image texture, java.awt.Rectangle size) {
		// Ask cache.
		if (repeatedImages.containsKey(texture)
				&& repeatedImages.get(texture).containsKey(size))
			return repeatedImages.get(texture).get(size);

		Rectangle textureBounds = texture.getBounds();

		Image repeatedTexture = new Image(this.display, size.width, size.height);
		GC repeatedTextureBuffer = new GC(repeatedTexture);

		for (int x = 0; x <= Math.ceil(size.width / textureBounds.width); x++) {
			int restWidth = size.width - (x * textureBounds.width);
			int width = restWidth > textureBounds.width ? textureBounds.width
					: restWidth;

			for (int y = 0; y <= Math.ceil(size.height / textureBounds.height); y++) {
				int restHeight = size.height - (y * textureBounds.height);
				int height = restHeight > textureBounds.height ? textureBounds.height
						: restHeight;

				repeatedTextureBuffer.drawImage(texture, 0, 0, width, height,
						textureBounds.width * x, textureBounds.height * y,
						width, height);
			}
		}

		// Add to cache.
		if (!repeatedImages.containsKey(texture))
			repeatedImages.put(texture,
					new HashMap<java.awt.Rectangle, Image>());

		repeatedImages.get(texture).put(size, repeatedTexture);

		return repeatedTexture;
	}

	/**
	 * Draws the room wherein the focused IRoomObject is.
	 */
	private void drawRoom() {
		// Obtain the boundaries of Room's image.
		java.awt.Rectangle roomBounds = focus.getRoom().getSize();

		Image roomImage = images.get(focus.getRoom().getImage());

		// Obtain the boundaries of Room's image.
		Rectangle roomImageBounds = roomImage.getBounds();

		// Repeat (or shrink) the texture if needed.
		if (roomImageBounds.width != roomBounds.width
				|| roomImageBounds.height != roomBounds.height)
			roomImage = repeatTexture(images.get(focus.getRoom().getImage()),
					roomBounds);

		// Determine the coordinates of room's image.
		int roomImageX = (focusLocation.getX() * -1) + (canvasBounds.width / 2);
		int roomImageY = (focusLocation.getY() * -1)
				+ (canvasBounds.height / 2);

		// Calculate the corrections. When an room image does not fit on the
		// display,
		// the centered point of the screen will be the centered point of the
		// level.

		// Calculate corrections of the X-axis.
		if (roomBounds.width < canvasBounds.width)
			roomImageX = canvasBounds.width - (roomBounds.width / 2);

		if (roomImageX > 0)
			roomImageX = 0;

		if (roomImageX * -1 > roomBounds.width - canvasBounds.width) {
			if (roomBounds.width < canvasBounds.width)
				roomImageX = (canvasBounds.width - roomBounds.width) / 2;

			else
				roomImageX = (roomBounds.width - canvasBounds.width) * -1;
		}

		if (roomBounds.height < canvasBounds.height)
			roomImageY = canvasBounds.height - (roomBounds.height / 2);

		// Calculate corrections of the Y-axis.
		if (roomImageY > 0)
			roomImageY = 0;

		if (roomImageY * -1 > roomBounds.height - canvasBounds.height) {
			if (roomBounds.height < canvasBounds.height)
				roomImageY = (canvasBounds.height - roomBounds.height) / 2;

			else
				roomImageY = (roomBounds.height - canvasBounds.height) * -1;
		}

		// Set the coordinates in the level, which is on the left corner of the
		// canvas.
		canvasCornerCoordinates = new Point(roomImageX * -1, roomImageY * -1);

		// Draw Room.
		bufferDrawer.drawImage(roomImage, 0, 0, roomBounds.width,
				roomBounds.height, roomImageX, roomImageY, roomBounds.width,
				roomBounds.height);
	}

	/**
	 * Draws all the IRoomObjects in the Room wherein the focused IRoomObject
	 * is.
	 */

	private void drawRoomObjects() {
		// Draw IRoomObjects of the Room wherein focus resides.
		ArrayList<IRoomObject> roomObjects = focus.getRoom().getRoomObjects();
		
		//I think ordinary for loops are safer against concurrent modification, because the iterator foreach uses gets invalid after modification.
		//TODO merg arraylist to vectors?
		//for (IRoomObject roomObject : focus.getRoom().getRoomObjects())
		for(int i = 0 ; i < roomObjects.size(); i++)
		{
			IRoomObject roomObject = roomObjects.get(i);
			
			if (!roomObject.getVisible())
				continue;

			// Obtain roomObject's image.
			Image roomObjectImage = images.get(roomObject.getImage());

			if (roomObjectImage == null) {
				System.err.println("ERROR: " + roomObject.getImage()
						+ " not found");
				return;
			}
			// Obtain the room and image size.
			Rectangle imageBounds = roomObjectImage.getBounds();

			java.awt.Rectangle roomObjectBounds = roomObject.getSize();

			// Repeat (or shrink) the texture if needed.
			if (imageBounds.width != roomObjectBounds.width
					|| imageBounds.height != roomObjectBounds.height)
				roomObjectImage = repeatTexture(roomObjectImage,
						roomObjectBounds);

			// Obtain the IRoomObject's location.
			nl.toefel.game.roomobject.Point roomObjectLocation = roomObject.getLocation();

			// Determine the x and y coordinates absolute to the canvas.
			int x = (canvasCornerCoordinates.x * -1)
					+ roomObjectLocation.getX() - (roomObjectBounds.width / 2);
			int y = (canvasCornerCoordinates.y * -1)
					+ roomObjectLocation.getY() - roomObjectBounds.height;

			// Draw the IRoomObject.
			bufferDrawer.drawImage(roomObjectImage, 0, 0,
					roomObjectBounds.width, roomObjectBounds.height, x, y,
					roomObjectBounds.width, roomObjectBounds.height);
		}
		
		// Draw highlights
		// Checks if the IRoomObject needs to be visually highlighted.
		for (IRoomObject roomObject : focus.getRoom().getRoomObjects())
		{
			if(roomObject.getHighlight() == true)
			{
				// Visually highlight.
				bufferDrawer.setForeground(new Color(null, 255, 0, 0));
				bufferDrawer.drawRectangle(
												-1 + roomObject.getLocation().getX() - (roomObject.getSize().width / 2) - canvasCornerCoordinates.x,
												-1 + roomObject.getLocation().getY() - roomObject.getSize().height 		- canvasCornerCoordinates.y,
												 2 + roomObject.getSize().width,
												 2 + roomObject.getSize().height
											);
			}
		}
	}

	/**
	 * loads an image into the class array in a format SWT understands
	 * 
	 * @param path
	 *            filename of the image
	 */
	public void preloadImage(String fullImagePath, String imagePath) {
		if (!images.containsKey(imagePath)) {
			File file = new File(fullImagePath);
			if (file.exists())
				images.put(imagePath, new Image(this.display, fullImagePath));
		}
	}

	/**
	 * unloads the whole drawer object and eventually notifies Game of the
	 * disposed state
	 */
	public void dispose() {
		// ILLIGAL, shells can only be disposed by UI thread
		// shell.dispose();

		SWTUnloadDrawer disposer = new SWTUnloadDrawer(shell);
		// makes the UI thread run the disposer instead of another thread
		display.asyncExec(disposer);
	}
	
	/**
	 * Returns the focused IRoomObject.
	 * @return	IRoomObject	The focus.
	 */
	public IRoomObject getFocus()
	{
		return focus;
	}
	
	public Point relativePointToAbsolute(Point relative)
	{
		return new Point(
							canvasCornerCoordinates.x + relative.x,
							canvasCornerCoordinates.y + relative.y
						);
	}
}
