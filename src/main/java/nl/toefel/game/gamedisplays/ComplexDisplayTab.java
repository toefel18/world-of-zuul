package nl.toefel.game.gamedisplays;
import java.util.ArrayList;
import java.util.HashMap;
import nl.toefel.game.Game;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.roomobject.Point;

/**
 * Contains all the items in this tab
 * @author Christophe
 *
 */
public class ComplexDisplayTab {
	
	/**
	 * Internal structure to hold item data
	 *
	 */
	public class Item{
		public String imagePath;
		public String name;
		public ArrayList<ICommand> commands;
	}
	
	protected String name = "menu";
	protected String text = "";
	protected ArrayList<Item> items = new ArrayList<Item>();
	protected int selected = 0;
	
	public ComplexDisplayTab(String name, String text){
		this.name = name;
		this.text = text;
	}
	
	/**
	 * Returns the name of the tab
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * Sets the name of the tab
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Returns the text message of the tab
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}


	/**
	 * Sets the textmessage of the tab
	 * @param text the text to set
	 */
	public void setText(String text){
		this.text = text;
	}

	/**
	 * Add an item to the display;
	 * 
	 * @param name name of the item
	 * @param imagePath the image path of the item (this image should be shrinked by the drawers)
	 * @param commands array with exectuable commands when this item is selected
	 */
	public void addItem(String name, String imagePath, ArrayList<ICommand> commands){
		Item item = new Item();
		
		item.name = name;
		item.imagePath = imagePath;
		item.commands = commands;
		
		items.add(item);
	}
	
	/**
	 * Adds a configured item to internal list of items, WARNING CHECK INITIALISATION
	 * @param item
	 */
	public void addItem(Item item){
		items.add(item);
	}
	
	/**
	 * Returns all the items in the internal structure
	 * @return
	 */
	public ArrayList<Item> getItems(){
		return items;
	}
	
	/**
	 * Returns an item containd by this tab
	 * @param index index of the item
	 * @return ItemData
	 */
	public Item getItem(int index){
		if(index >= 0 && index < items.size())
			return items.get(index);
		else
			return null;
	}
	
	/**
	 * Returns the number of items;
	 * 
	 * @return num items
	 */
	public int getNumItems(){
		return items.size();
	}
	
	/**
	 * Returns the selected item in this tab
	 * @return ItemData
	 */
	public Item getSelectedItem(){
		return getItem(selected);
	}
	
	/**
	 * Sets the selected command index
	 * @param index
	 */
	public void setSelected(int index){
		if (items != null && items.size() > 0)
			selected = index < 0 ? items.size() - 1 : index % items.size();
	}
	
	/**
	 * Selects the next command. If there are no more commands left, it selects the very first.
	 */
	public void incrementSelected(){
		setSelected(selected + 1);
	}
	
	/**
	 * Selects the previous command. If there are no previous commands, it selects the last command.
	 */
	public void decrementSelected(){
		setSelected(selected - 1);
	}
	
	/**
	 * Gets the selected index
	 * @return selected index
	 */
	public int getSelected(){
		return selected;
	}
	
	/**
	 * Pops up the menu with commands related to the selected item
	 */
	public void executeSelectedItem( Point location ){
		Item item = getSelectedItem();
		if (item==null) return;
		
		CommandDisplay commandDisplay = new CommandDisplay();
		
		commandDisplay.setLocation(location);
		commandDisplay.setTekst(item.name);
		commandDisplay.addCommandList(item.commands);
		
		Game.getInstance().setCommandDisplay(commandDisplay);
	}
		
}
