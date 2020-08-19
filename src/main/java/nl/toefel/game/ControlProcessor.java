package nl.toefel.game;

class ControlProcessor implements Runnable {

	private Game game;
	private int ms = 10;
	private boolean keepGoing = true;
	
	/**
	 * Starts the control processor
	 */
	public void run() {
		
		game = Game.getInstance();
		
		while(keepGoing){
			game.processKeys();
			
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("DEBUG-WARNING: Thread sleep exception");
				e.printStackTrace();
			}
		}
		
		System.out.println("DEBUG-INFO: Controls Processsing runmethod stopped (THREAD STOP)");
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
