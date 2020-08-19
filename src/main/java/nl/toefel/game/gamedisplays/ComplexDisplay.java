package nl.toefel.game.gamedisplays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import nl.toefel.game.roomobject.Point;


/**
 * Handles in-game complex windows like inventories
 * @author Christophe
 *
 */
public class ComplexDisplay {

	protected ArrayList<ComplexDisplayTab> tabs;
	protected int selected = 0;
	protected Point commandDisplayLocation = new Point(10, 10);
	
	/**
	 * Creates an empty complex window
	 */
	public ComplexDisplay(){
		tabs = new ArrayList<ComplexDisplayTab>();
	}

	/**
	 * Adds a tab to the complex window
	 * @param tab (fully configured)
	 */
	public void addTab(ComplexDisplayTab tab){
		tabs.add(tab);
	}
	
	public Point getLocation(){
		return commandDisplayLocation;
	}
	
	/**
	 * Gets all the tabs in this window
	 * @return
	 */
	public ArrayList<ComplexDisplayTab> getTabs(){
		return tabs;
	}
	
	/**
	 * Get a certain tab in the window
	 * @param index
	 * @return the selected tab
	 */
	public ComplexDisplayTab getTab(int index){
		if(index >= 0 && index < tabs.size())
			return tabs.get(index);
		else
			return null;
	}

	/**
	 * Returns the selected tab
	 * @return selected tab or null if none
	 */
	public ComplexDisplayTab getSelectedTab(){
		return getTab(selected);
	}
	
	/**
	 * gets the selected tab
	 * @return
	 */
	public int getSelected(){
		return selected;
	}
	
	/**
	 * Sets the selected index
	 * @param index
	 */
	public void setSelected(int index){
		selected = index < 0 ? tabs.size() - 1 : index % tabs.size();
	}
	
	/**
	 * Sets the location of the command display for items that are executed
	 */
	public void setCommandDisplayLocation(Point location){
		this.commandDisplayLocation = location;
	}
	
	/**
	 * Selects the next tab. If there are no more tabs left, it selects the very first.
	 */
	public void incrementSelected(){
		setSelected(selected + 1);
	}
	
	/**
	 * Selects the previous tab. If there are no previous tabs, it selects the last tab.
	 */
	public void decrementSelected(){
		setSelected(selected - 1);
	}
		
	/**
	 * Increments the item selection in the selected tab
	 */
	public void incrementTabSelectedItem(){
		ComplexDisplayTab tab = getSelectedTab();
		if(tab != null)
			tab.incrementSelected();
	}
	
	/**
	 * Decrements the item selection in the selected tab
	 */
	public void decrementTabSelectedItem(){
		ComplexDisplayTab tab = getSelectedTab();
		if(tab != null)
			tab.decrementSelected();
	}
	
	/**
	 * Executes the selected item in the selected tab of this display.
	 * Basically this means popping up a command window!
	 */
	public void executeSelectedTab(){
		ComplexDisplayTab tab = getSelectedTab();
		if(tab != null)
			tab.executeSelectedItem( commandDisplayLocation );
	}
}
