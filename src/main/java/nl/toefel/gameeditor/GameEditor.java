package nl.toefel.gameeditor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import nl.toefel.game.Game;
import nl.toefel.game.drawer.SWTDrawer;
import nl.toefel.game.roomobject.IRoomObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import org.eclipse.swt.*; 
import org.eclipse.swt.widgets.*; 
import org.eclipse.swt.graphics.*; 
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*; 

public class GameEditor
{
	private Game game;
	
	private Display display;
	private Shell 	shell;
	private Canvas 	gameCanvas;
	private StyledText xmlEditor;
	
	private Document gameState;
	
	private IRoomObject player;
	
	/**
	 * Contains all the selected IRoomObject's.
	 */
	private ArrayList<IRoomObject> selectedRoomObjects = new ArrayList<IRoomObject>();
	
	private Point dragPreviousMousePoint;
	
	private static GameEditor instance;
	
	public static GameEditor getInstance()
	{
		return instance;
	}
	
	public GameEditor()
	{
		instance = this;
		
		game = Game.getInstance();
		
		initLayout();
		
		initGame();
		
		gameState = game.getGameState();
		
		//handleNewGameState();
		
		// Message loop.
		while(!shell.isDisposed())
			if(!display.readAndDispatch()) 
				display.sleep();
		
		display.dispose();
	}
	
	private void handleNewGameState()
	{
		try
		{
			// Select the XMLNode containing all the room objects. Those changed states may be saved. Events are likely processed.
			Node oldRoomObjects = (Node) XPathFactory.newInstance().newXPath().compile("/game/roomobjects[1]").evaluate(gameState, 				XPathConstants.NODE);
			Node newRoomObjects = (Node) XPathFactory.newInstance().newXPath().compile("/game/roomobjects[1]").evaluate(game.getGameState(), 	XPathConstants.NODE);

			oldRoomObjects.getOwnerDocument().importNode(newRoomObjects, true);
			
			oldRoomObjects.getParentNode().replaceChild(newRoomObjects, oldRoomObjects);
			
			DOMSource gameSource = new DOMSource(gameState);
			StreamResult result  = new StreamResult(new StringWriter());
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			{
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(gameSource, result);
			}
			
			xmlEditor.setText(result.getWriter().toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void initGame()
	{
		try
		{
			Game.getInstance().createNewGame("pokemon", "Jeffrey", "en");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Canvas getCanvas()
	{
		return gameCanvas;
	}
	
	public Shell getShell()
	{
		return shell;
	}
	
	private void initLayout()
	{
		display 	= new Display();
		shell 		= new Shell(display);

		final CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		{
			CTabItem gameTab = new CTabItem(tabFolder, SWT.NULL);
			{
				gameTab.setText("Game");
				
				Composite composite = new Composite(tabFolder, SWT.NONE);
				{
					composite.setLayout(new FillLayout());
				}
				
				gameTab.setControl(composite);
				
				gameCanvas = new Canvas(composite, SWT.BORDER);
			
				gameCanvas.addListener(SWT.MouseDown, 	new Listener()
														{
															public void handleEvent(Event e)
															{
																Point mousePoint = new Point(e.x, e.y);
																
																selectRoomObjectsByPoint(mousePoint);
																
																startDragSelection(mousePoint);
															}
														});
			}
			
			final CTabItem xmlTab = new CTabItem(tabFolder, SWT.NULL);
			{
				xmlTab.setText("XML");
				
				Composite composite = new Composite(tabFolder, SWT.NONE);
				{
					
					//FillLayout layout = new FillLayout();
					//layout.type = SWT.VERTICAL;
					
					GridLayout layout = new GridLayout(1, false);
					composite.setLayout(layout);
					
					GridData fillControl = new GridData();
					fillControl.horizontalAlignment = GridData.FILL;
					fillControl.verticalAlignment = GridData.FILL;
					fillControl.grabExcessVerticalSpace = true;
					fillControl.grabExcessHorizontalSpace = true;
					
					xmlEditor = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
					xmlEditor.setLayoutData(fillControl);
					
					Button saveButton = new Button(composite, SWT.PUSH);
					{
						
						saveButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
						
						saveButton.setText("Save to XML");
						
						saveButton.addListener(SWT.Selection, 	new Listener()
																{
																	public void handleEvent(Event event)
																	{
																		saveXML();
																	}
																});
					}
				}
				xmlTab.setControl(composite);
			}
			
			tabFolder.addListener(SWT.Selection, new Listener(){
				
				public void handleEvent(Event e)
				{
					if(tabFolder.getSelection() == xmlTab)
						handleNewGameState();
					
					else // Game tab
					{
						if(xmlEditor.getText().length() == 0)
							return;
						
						try
						{
							DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							DocumentBuilder db = dbf.newDocumentBuilder();
							Document doc = db.parse(new ByteArrayInputStream(xmlEditor.getText().getBytes()));

							gameState = doc;
						}
						catch (Exception e2)
						{
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						reloadGame();
					}
				}
				
			});
		}
		
		tabFolder.setSize(800, 600);
		tabFolder.setSimple(false);
		
		shell.setSize(800, 650);
		shell.pack();
		shell.open();
		
		Game.getInstance().redraw();
	}
	
	private void saveXML()
	{
		try
		{
			String filepath = "nl/toefel/game/games/pokemon/game.xml";
			
			// Select the root node of the current gamestate.
			XPath xPath =  XPathFactory.newInstance().newXPath();
			Node node = (Node) xPath.compile("/*[1]").evaluate(gameState, XPathConstants.NODE);
			
			File file = new File(filepath);
			
			if(!file.exists())
				file.createNewFile();
			
			// Open the FileWriter with the given filepath.
			FileWriter fileWriter = new FileWriter(filepath);

			// Transform XML to String and write it to the given StreamResult.
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(node), new StreamResult(fileWriter)); 
			
			// Close the file handle.
			fileWriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void unselectRoomObjects()
	{
		for(IRoomObject roomObject : selectedRoomObjects)
			roomObject.setHighlight(false);
	}
	
	private void reloadGame()
	{
		if(xmlEditor.getText().length() == 0)
			return;

		Game.getInstance().unload();
		
		game = null;
			
		try
		{
			game = Game.getInstance();
			game.loadGame(gameState);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		game.redraw();
	}
	
	private void selectRoomObjectsByPoint(Point mousePoint)
	{
		// Unselect previous room objects.
		unselectRoomObjects();
		
		// Get the player (assumed that there is only 1 player).
		player = game.getPlayers().values().iterator().next();
		
		// Get the drawer.
		SWTDrawer drawer = (SWTDrawer) game.getDrawerByRoomObject(player);
		
		// Convert the point of the mouse to the absolute point in the room.
		Point point = drawer.relativePointToAbsolute(new Point(mousePoint.x, mousePoint.y));
		
		// Get the hitted IRoomObjects.
		ArrayList<IRoomObject> hittedRoomObjects = player.getRoom().getRoomObjectsAtPoint(new nl.toefel.game.roomobject.Point(point.x, point.y));
		
		selectedRoomObjects.clear();
		
		if(hittedRoomObjects.size() == 0)
			return;
		
		// Select one by random.
		selectedRoomObjects.add(hittedRoomObjects.get((int) (Math.random() * hittedRoomObjects.size())));
		
		for(IRoomObject roomObject : selectedRoomObjects)
			roomObject.setHighlight(true);
	}
	
	private void updateSelectionAfterDrag(Point mousePoint)
	{
		Point mutation = new Point(
										mousePoint.x - dragPreviousMousePoint.x,
										mousePoint.y - dragPreviousMousePoint.y
									);
		
		for(IRoomObject roomObject : selectedRoomObjects)
		{
			nl.toefel.game.roomobject.Point oldLocation = roomObject.getLocation();

			nl.toefel.game.roomobject.Point newLocation = new nl.toefel.game.roomobject.Point(
																			oldLocation.getX() + mutation.x,
																			oldLocation.getY() + mutation.y
																		);
			
			roomObject.setLocation(newLocation);
		}
		
		dragPreviousMousePoint = mousePoint;
		
		game.redraw();
	}
	
	/**
	 * Starts dragging the selected room objects.
	 */
	private void startDragSelection(Point mousePoint)
	{
		if(selectedRoomObjects.size() == 0)
			return;
		
		dragPreviousMousePoint = mousePoint;
		
		final Listener dragListener = new Listener()
		{
			public void handleEvent(Event e)
			{
				updateSelectionAfterDrag(new Point(e.x, e.y));
			}
		};
		
		gameCanvas.addListener(SWT.MouseMove, 	dragListener);
		
		gameCanvas.addListener(SWT.MouseUp, 	new Listener()
												{
													public void handleEvent(Event e)
													{
														gameCanvas.removeListener(SWT.MouseMove, dragListener);
													}
												});
	}
}
