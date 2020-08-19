package nl.toefel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.FocusListener;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

/**
 * Creates a window that presents the user a configuration option
 * @author Christophe
 *
 */
public class ConfigureControls {
	/** Internal constants*/
	private static final String ASK_KEY = "Enter Key";
	private static final int SPACE_BAR = 32;
	private static final int RETURN = 13;
	private static final int ALT_LEFT = 65536;
	private static final int ALT_RIGHT = 262144;
	private static final int BACKSPACE = 8;
	
	/** Identifiers to distinguish keys*/
	public static final int MOVE_NORTH = 1;
	public static final int MOVE_SOUTH = 2;
	public static final int MOVE_EAST = 3;
	public static final int MOVE_WEST = 4;
	public static final int USE = 5;
	public static final int INVENTORY = 6;
	public static final int DEBUG = 7;
	
	/** controls */
	private Shell ctrlWindow;
	private Text moveUpText;
	private Text moveDownText;
	private Text moveLeftText;
	private Text moveRightText;
	private Text showInventoryText;
	private Text useText;
	private Text toggleDebugText;
	
	private Node controlSet;
	private HashMap<Text, Integer> controls = new HashMap<Text, Integer>();
	
	protected class CtrlConfigure implements KeyListener{
		private Text ref;
		public CtrlConfigure(Text keyOf){
			ref = keyOf;
		}
		public void keyPressed(KeyEvent e) {
			e.doit = false;	//disable the event from happening, restore the key when focus is lost
			ctrlWindow.forceFocus(); //set the focus to the window, so that the control loses focus
			//27 == ESC
			System.out.println("DEBUG-INFO: keycode: " + e.keyCode);
			if(e.character != SWT.ESC){
				controls.put(ref, e.keyCode);
				ref.setText( ConfigureControls.renameCode(e.keyCode));
				
				/*
				//range of printable chars
				if(e.character > 32 && e.character < 127)
					ref.setText( "" + e.character );
				else
					ref.setText( ConfigureControls.renameCode(e.keyCode) );
				*/
			}
		}
		public void keyReleased(KeyEvent e) {}
	}
		
	protected class TextFocusListener implements FocusListener{
		private Text ref;
		private String backup;
		public TextFocusListener(Text focusOf){
			ref = focusOf;	
		}
		public void focusGained(FocusEvent e) {
			backup = ref.getText();
			ref.setText(ASK_KEY);
		}
		public void focusLost(FocusEvent e){
			//moveUpText.setText( "KeyCode: " + lastKeyCode);
			if(ref.getText().equals(ASK_KEY))
				ref.setText(backup);
		}
		
	}
	
	public ConfigureControls( Display display, Point size, Node controlSet){		
		ctrlWindow = new Shell( display, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.CLOSE );	
		ctrlWindow.setSize( size );
		ctrlWindow.setText( "Configure Controls" );
	
		GridLayout gl = new GridLayout( 2, false );
		gl.marginTop = 10;
		gl.marginLeft = 10;
		gl.marginRight = 10;
		ctrlWindow.setLayout(gl);
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		
		GridData infolabelgd = new GridData();
		infolabelgd.grabExcessHorizontalSpace = true;
		infolabelgd.horizontalAlignment = SWT.FILL;
		infolabelgd.horizontalSpan = 2; 

		Label tmpLabel = new Label( ctrlWindow, SWT.WRAP);
		tmpLabel.setText( "Put your cursor into one of the textboxes. The textbox will capture the key you enter, and configure it for the corresponding task." );
		tmpLabel.setLayoutData(infolabelgd);
		
		//spacer -- temporary ugly solution
		tmpLabel = new Label( ctrlWindow, SWT.NONE);
		tmpLabel.setSize(1, 8);
		tmpLabel = new Label( ctrlWindow, SWT.NONE);
		tmpLabel.setSize(1, 8);
		
		tmpLabel = new Label( ctrlWindow, SWT.NONE );
		tmpLabel.setText( "Move up:" );
		
		moveUpText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		moveUpText.setText( "Arrow UP" );
		moveUpText.setLayoutData( gd );
		moveUpText.addKeyListener( new CtrlConfigure(moveUpText) );
		moveUpText.addFocusListener(new TextFocusListener(moveUpText));
		
		tmpLabel = new Label( ctrlWindow, SWT.NONE );
		tmpLabel.setText( "Move down:" );
		moveDownText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		moveDownText.setText( "Arrow down" );
		moveDownText.setLayoutData( gd );
		moveDownText.addKeyListener( new CtrlConfigure(moveDownText) );
		moveDownText.addFocusListener(new TextFocusListener(moveDownText));
		
		tmpLabel = new Label( ctrlWindow, SWT.NONE );
		tmpLabel.setText( "Move left:" );
		moveLeftText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		moveLeftText.setText( "Arrow left" );
		moveLeftText.setLayoutData( gd );
		moveLeftText.addKeyListener( new CtrlConfigure(moveLeftText) );
		moveLeftText.addFocusListener(new TextFocusListener(moveLeftText));
				
		tmpLabel = new Label( ctrlWindow, SWT.NONE );
		tmpLabel.setText( "Move right:" );
		moveRightText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		moveRightText.setText( "Arrow right" );
		moveRightText.setLayoutData( gd );		
		moveRightText.addKeyListener( new CtrlConfigure(moveRightText) );
		moveRightText.addFocusListener(new TextFocusListener(moveRightText));
		
		tmpLabel = new Label( ctrlWindow, SWT.NONE );
		tmpLabel.setText( "Show inventory:" );
		showInventoryText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		showInventoryText.setText( "i" );
		showInventoryText.setLayoutData( gd );	
		showInventoryText.addKeyListener( new CtrlConfigure(showInventoryText) );
		showInventoryText.addFocusListener(new TextFocusListener(showInventoryText));
		
		tmpLabel = new Label( ctrlWindow, SWT.NONE );
		tmpLabel.setText( "Use Item / pick up:" );
		useText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		useText.setText( "Enter" );
		useText.setLayoutData( gd );
		useText.addKeyListener( new CtrlConfigure(useText) );
		useText.addFocusListener(new TextFocusListener(useText));
		
		tmpLabel = new Label( ctrlWindow, SWT.NONE);
		tmpLabel.setText( "Toggle debug info" );
		
		toggleDebugText = new Text( ctrlWindow, SWT.SINGLE | SWT.BORDER | SWT.CENTER );
		toggleDebugText.setText( "d" );
		toggleDebugText.setLayoutData( gd );
		toggleDebugText.addKeyListener( new CtrlConfigure(toggleDebugText) );
		toggleDebugText.addFocusListener(new TextFocusListener(toggleDebugText));
		
		GridData gdButton = new GridData();
		gdButton.horizontalAlignment = SWT.CENTER;
		gdButton.verticalIndent = 12;
		gdButton.grabExcessHorizontalSpace = true;
		
		Button cancel = new Button( ctrlWindow, SWT.PUSH );
		cancel.setText( "      Cancel     " ); 
		cancel.setLayoutData( gdButton );
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				ctrlWindow.close();
			}			
		});
		
		Button save = new Button( ctrlWindow, SWT.PUSH );
		save.setText( "        OK        " ); 
		save.setLayoutData( gdButton );
		save.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				writeControlSet();
				ctrlWindow.close();
			}			
		});
		
		setControls(controlSet);
	}
	
	public void open(){
		//ctrlWindow.pack();
		ctrlWindow.open();
	}
	
	public static String renameCode(int keyCode){
		
		if(keyCode > 32 && keyCode < 127)
			return( "" + (char)keyCode );
		
		switch(keyCode){
			case SWT.ARROW_DOWN:	return "Arrow Down";
			case SWT.ARROW_UP:		return "Arrow Up";
			case SWT.ARROW_LEFT:	return "Arrow Left";
			case SWT.ARROW_RIGHT:	return "Arrow Right";
			case SWT.SHIFT:			return "Shift";
			case SPACE_BAR:			return "Space";
			case RETURN:			return "Enter";
			case ALT_LEFT:			return "Alt left";
			case ALT_RIGHT:			return "Alt right";
			case BACKSPACE: 		return "Backspace";
		}
		
		return "Code: " + keyCode;
	}
	/*
	public HashMap<Integer, Integer> getControls(){
		HashMap<Integer, Integer> controlSet = new HashMap<Integer, Integer>();
		
		controlSet.put(MOVE_NORTH, controls.get(moveUpText));
		controlSet.put(MOVE_SOUTH, controls.get(moveDownText));
		controlSet.put(MOVE_EAST, controls.get(moveRightText));
		controlSet.put(MOVE_WEST, controls.get(moveLeftText));
		controlSet.put(USE, controls.get(useText));
		controlSet.put(INVENTORY, controls.get(showInventoryText));
		controlSet.put(DEBUG, controls.get(toggleDebugText));
		
		return controlSet;
	}
	
	*/
	
	/**
	 * Loads the function<->keycode pairs from a map
	 * TODO this should not be possible, because everythign eneds to be written to xml
	 * @param keyPairs
	 */
	/*
	public void setControls(HashMap<Integer, Integer> keyPairs){
		controls.put(moveDownText, keyPairs.get(MOVE_SOUTH));
		controls.put(moveUpText, keyPairs.get(MOVE_NORTH));
		controls.put(moveRightText, keyPairs.get(MOVE_EAST));
		controls.put(moveLeftText, keyPairs.get(MOVE_WEST));
		controls.put(useText, keyPairs.get(USE));
		controls.put(showInventoryText, keyPairs.get(INVENTORY));
		controls.put(toggleDebugText, keyPairs.get(DEBUG));
		updateTextboxes();
	}
	*/
	
	/**
	 * Loads the function<->keycode pairs from an XML node
	 * 
	 * @param controlSet XML node
	 */
	public void setControls(Node controlSet){
		this.controlSet = controlSet;
		
		NodeList children = (NodeList) controlSet.getChildNodes();
		if(children == null) return;
		
		for(int i = 0; i < children.getLength(); i++){
			Node item = children.item(i);
			if(item != null){
				NamedNodeMap attributes = (NamedNodeMap) item.getAttributes();
				
				if(attributes == null) continue;
				
				Node nameNode = attributes.getNamedItem("name");
				Node valueNode = attributes.getNamedItem("keycode");

				if(nameNode != null && valueNode != null){
					String name = nameNode.getNodeValue();
					String value = valueNode.getNodeValue();
					int keyCode = Integer.valueOf(value);
					
					if(name.equals("move-north")){
						controls.put(moveUpText, keyCode);
					}else if(name.equals("move-south")){
						controls.put(moveDownText, keyCode);
					}else if(name.equals("move-east")){
						controls.put(moveRightText, keyCode);
					}else if(name.equals("move-west")){
						controls.put(moveLeftText, keyCode);
					}else if(name.equals("inventory")){
						controls.put(showInventoryText, keyCode);
					}else if(name.equals("use")){
						controls.put(useText, keyCode);
					}else if(name.equals("showdebug")){
						controls.put(toggleDebugText, keyCode);
					}	
				}
			}
		}
		updateTextboxes();
	}
	
	public void writeControlSet(){
		if(controlSet == null) return;
		
		NodeList children = (NodeList) controlSet.getChildNodes();
		if(children == null) return;
		
		for(int i = 0; i < children.getLength(); i++){
			Node item = children.item(i);
			if(item != null){
				NamedNodeMap attributes = (NamedNodeMap) item.getAttributes();
				
				if(attributes == null) continue;
				
				Node nameNode = attributes.getNamedItem("name");
				Node valueNode = attributes.getNamedItem("keycode");
				
				if(nameNode != null && valueNode != null){
					String name = nameNode.getNodeValue();

					if(name.equals("move-north")){
						valueNode.setNodeValue( "" + (int)controls.get(moveUpText) );
					}else if(name.equals("move-south")){
						valueNode.setNodeValue( "" + (int)controls.get(moveDownText) );
					}else if(name.equals("move-east")){
						valueNode.setNodeValue( "" + (int)controls.get(moveRightText) );
					}else if(name.equals("move-west")){
						valueNode.setNodeValue( "" + (int)controls.get(moveLeftText) );
					}else if(name.equals("inventory")){
						valueNode.setNodeValue( "" + (int)controls.get(showInventoryText) );
					}else if(name.equals("use")){
						valueNode.setNodeValue( "" + (int)controls.get(useText) );
					}else if(name.equals("showdebug")){
						valueNode.setNodeValue( "" + (int)controls.get(toggleDebugText) );
					}	
				}
			}
		}
	}
	
	protected void updateTextboxes(){
		moveUpText.setText( renameCode(controls.get(moveUpText)) );
		moveDownText.setText( renameCode(controls.get(moveDownText)) );
		moveRightText.setText( renameCode(controls.get(moveRightText)) );
		moveLeftText.setText( renameCode(controls.get(moveLeftText)) );
		useText.setText( renameCode(controls.get(useText)) );
		showInventoryText.setText( renameCode(controls.get(showInventoryText)) );
		toggleDebugText.setText( renameCode(controls.get(toggleDebugText)) );
	}
}
