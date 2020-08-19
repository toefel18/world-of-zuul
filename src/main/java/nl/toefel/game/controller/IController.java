package nl.toefel.game.controller;

/**
 * Defines an inteface for controller objects
 * Each type of control
 */
public interface IController {
	/**
	 * Processes the input if it can, the code specifies what to do
	 * @param code tells what to process
	 */
	void processInput( int code );
}
