package nl.toefel.game;

/**
 * application specific observer interaces
 *
 */
public interface IObserver {
	/**
	 * Updates itself with the data from
	 * the attached observable(s)
	 * @param observer the observer who triggered the update
	 */
	public void update(IObservable observable);
}
