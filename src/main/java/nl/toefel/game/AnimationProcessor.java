package nl.toefel.game;
import java.util.Vector;
import nl.toefel.game.roomobject.IRoomObject;


/**
 * Animates objects inside the game
 * 
 * @author Christophe
 *
 */
class AnimationProcessor implements Runnable {

	private Game game;
	private int ms = 50;
	private boolean keepGoing = true;
	
	/**
	 * Starts the control processor
	 */
	public void run() {
		
		game = Game.getInstance();
		
		while(keepGoing){
			Vector<IRoomObject> animatedObjects = game.getAnimatedObjects();
			
			for(int i = 0; i < animatedObjects.size(); i++){
				IRoomObject obj = animatedObjects.get(i);
				obj.updateAnimationInterval(ms);
			}
			
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("DEBUG-WARNING: Thread sleep exception");
				e.printStackTrace();
			}
		}
		
		System.out.println("DEBUG-INFO: Animation Processsing runmethod stopped (THREAD STOP)");
	}
	
	/**
	 * causes the executing control loop to stop and the thread to end
	 */
	public void setStopFlag(){
		keepGoing = false;
	}
	
	/**
	 * sets the interval in which commands are processed
	 * @param ms
	 */
	public void setIntervalDelay(int ms){
		this.ms = ms;
	}
}
