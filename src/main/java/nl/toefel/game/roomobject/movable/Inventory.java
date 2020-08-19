package nl.toefel.game.roomobject.movable;

import nl.toefel.game.roomobject.ICarryable;

import java.util.ArrayList;

/**
 * Holds items in an inventory
 * @author Christophe
 *
 */
public class Inventory {
	/** name of the inventory, can change per language*/
	protected String name;
	
	/** the types the inventory can store*/
	protected String type;
	
	/** the maximum number of weight this inventory can store*/
	protected int maxWeight;
	
	/** the items carried in this inventory*/
	protected ArrayList<ICarryable> items = new ArrayList<ICarryable>();
	
	/**
	 * Creates the inventory with a name, a certain type that can be stored in it and till what kind of weight
	 * @param name;
	 * @param type
	 * @param maxWeight
	 */
	public Inventory(String name, String type, int maxWeight){
		this.name = name;
		this.type = type;
		this.maxWeight = maxWeight;
	}
	
	/**
	 * Returns true if this inventory contains the item
	 * @param item 
	 * @return true if contains, false otherwise
	 */
	public boolean contains(ICarryable item){
		return items.contains(item);
	}
	
	/**
	 * Returns the name of the inventory
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the inventory
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the type of items the inventory can store
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the number of items in the inventory
	 * @return int 
	 */
	public int getNumItems(){
		return items.size();
	}

	/**
	 * Returns the maximum weigth of all items in total this inventory can store
	 * 
	 * @return the maxWeight
	 */
	public int getMaxWeight() {
		return maxWeight;
	}

	/**
	 * sets the maximun weight
	 * 
	 * @param maxWeight the maxWeight to set
	 */
	public void setMaxWeight(int maxWeight) {
		this.maxWeight = maxWeight;
	}

	/**
	 * Returns all items stored in this inventory
	 * 
	 * @return the items
	 */
	public ArrayList<ICarryable> getItems() {
		return items;
	}
	
	/**
	 * Returns a particular item in the inventory
	 * @param index of the item
	 * @return item
	 */
	public ICarryable getItem(int index){
		if(index >= 0 && index < items.size())
			return items.get(index);
	
		return null;
 	}
	
	/**
	 * Adds an item to the array
	 * 
	 * @param item
	 * @return
	 */
	public boolean addItem(ICarryable item){
		if(canAddItem(item))
			return items.add(item);
				
		return false;
	}
	
	/**
	 * Determines if the item can be added to this inventory
	 * @param item
	 * @return
	 */
	public boolean canAddItem(ICarryable item){
		if (item == null) 
			return false;
		else if( (getCurrentWeight() + item.getWeight()) > maxWeight )
			return false;
		else if( !getType().equals(item.getType()) )
			return false;
		
		return true;
	}
	
	/**
	 * Computes the weight of all items in this inventory
	 * @return the weight of all items together
	 */
	public int getCurrentWeight(){
		int weight = 0;
		
		for(int i = 0; i < items.size(); i++){
			ICarryable obj = items.get(i); 
			
			if( obj != null ){
				weight += obj.getWeight();
			}
		}
		
		return weight;
	}
	
	/**
	 * Removes the item that is linked to the index from the array
	 * @param index the index of the item to remove
	 */
	public void removeItem(int index){
		if(index >= 0 && index < items.size())
			items.remove(index);
	}
	
	/**
	 * Removes the item from the array
	 * @param the item to remove
	 */
	public void removeItem(ICarryable item){
		items.remove(item);
	}	
	
}
