package nl.toefel.game.roomobject;

/**
 * Is a point in space, based on values of the x and y axises.
 */
public class Point {
	/** Contains the value of the x-axis. */
	private int x;
	
	/** Contains the value of the y-axis. */
	private int y;
	
	/**
	 * Initializes the begin values of the axises.
	 * 
	 * @param int x The begin value of the x-axis.
	 * @param int y The begin value of the y-axis.
	 */
	public Point(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the value of the x-axis.
	 * 
	 * @return int The value of the x-axis.
	 */
	public int getX(){
		return x;
	}
	
	/**
	 * Returns the value of the y-axis.
	 * 
	 * @return int The value of the y-axis.
	 */
	public int getY(){
		return y;
	}
}
