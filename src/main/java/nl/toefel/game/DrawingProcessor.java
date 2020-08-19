package nl.toefel.game;
import nl.toefel.game.Game;

public class DrawingProcessor implements Runnable {
	
	private Game game;
	private boolean keepGoing = true;
	private boolean redrawNeeded = false;
	private int redrawDelay = 1000/40;	// = ~24 framechecks per sec
	
	/**
	 * Starts the drawing processor
	 */
	public void run() {
		game = Game.getInstance();
		
		while(keepGoing){
			if(redrawNeeded){
				game.notifyDrawers();
				redrawNeeded = false;
			}
			
			try {
				Thread.sleep(redrawDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("DEBUG-INFO: Drawing Processsing runmethod stopped (THREAD STOP)");
	}
	
	/**
	 * causes the executing drawing loop to stop and the thread to end
	 */
	public void setStopFlag(){
		keepGoing = false;
	}
	
	/**
	 * Sets the interval in which commands are processed
	 * @param ms calulcate 1000/FRAMESPERSECOND as value
	 */
	public void setIntervalDelay(int ms){
		this.redrawDelay = ms;
	}
	
	/**
	 * Sets the desired framerate, this can also be set via setIntervalDelay
	 */
	public void setDesiredFramerate(int fps){
		this.redrawDelay = 1000 / fps;
	}

	/**
	 * 
	 */
	public void setRedraw(){
		this.redrawNeeded = true;
	}
}


