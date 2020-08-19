package nl.toefel.game;

public interface IObservable {
	
	/**
	 * adds an observer to the internal list
	 * @param o observer object
	 */
	public void addObserver(IObserver observer);
	
	/**
	 * deletes an observer from the internal list
	 * @param o
	 */
	public void deleteObserver(IObserver observer);
	
	/**
	 * deletes all observers in the internal list
	 */
	public void deleteObservers();
	
	/**
	 * counts the observers in the internal list
	 * @return number of observers in the list
	 */
	public int countObservers();
	
	/**
	 * notifies all observers in the internal list
	 */
	public void notifyObservers();
}
