package nl.toefel.game.roomobject;

import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

/**
 * Compares IRoomObjects in an ArrayList based on their position on the Y-axis.
 */
public class RoomObjectComparator implements Comparator<IRoomObject>{
	
	/**
	 * Compares the Z-order, then the Y-axis of the given IRoomObjects of each other.
	 * 
	 * @param roomObject1 The object to compare.
     * @param roomObject2 The object to be compared to.
     * @return a negative integer, zero, or a positive integer as the Y-axis (and Z-Order) of the first IRoomOjbect is less than, equal to, or greater than the second. 
	 */
	public int compare(IRoomObject roomObject1, IRoomObject roomObject2)
	{
		try
		{
			//System.out.println(roomObject1.getIDString() + " vs " + roomObject2.getIDString());
		
			if(roomObject1.inRange(roomObject2) && roomObject1.getZOrder() == roomObject2.getZOrder())
			{
				int result = -1;
				
				//System.out.println("Special sorting algorithm..");

				for(RectangularShape boundary1: roomObject1.getBoundaries())
				{
					for(RectangularShape boundary2: roomObject2.getBoundaries())
					{
						Rectangle testBounds1 = new Rectangle(
																	boundary1.getBounds().x,
																	boundary1.getBounds().y,
																	boundary1.getBounds().width,
																	boundary1.getBounds().height
																);
						
						Rectangle testBounds2 = new Rectangle(
																	boundary2.getBounds().x,
																	boundary2.getBounds().y,
																	boundary2.getBounds().width,
																	boundary2.getBounds().height
																);
	
						if(!testBounds1.intersects(testBounds2))
							continue;

						//System.out.println("OK");	
						
						//System.out.println((boundary1.getBounds().y + boundary1.getBounds().height) + " > " + (boundary2.getBounds().y + boundary2.getBounds().height));
						
						if((boundary1.getBounds().y + boundary1.getBounds().height) > (boundary2.getBounds().y + boundary2.getBounds().height))
						{
							//System.out.println("222");
							result = 1;
						}
					}
				}
				
				return result;
			}
			else
			{
				if(roomObject1.getZOrder() < roomObject2.getZOrder())
					return -1;
			
				else if(roomObject1.getZOrder() > roomObject2.getZOrder())
					return 1;
				
				else
				{
					if(roomObject1.getLocation().getY() < roomObject2.getLocation().getY())
						return -1;
					
					else if(roomObject1.getLocation().getY() > roomObject2.getLocation().getY())
						return 1;
					
					else
						return 0;
				}
			}
		}
		catch(ConcurrentModificationException e)
		{
			System.out.println("ConcurrentModificationException: " + e.getStackTrace());
			
			// Recover by re-execute the method.
			return compare(roomObject1, roomObject2);
		}
	}
}
