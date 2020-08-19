package nl.toefel.game;

import nl.toefel.game.roomobject.IRoomObject;

/**
 * When an event occurs between two IRoomObjects, an InvokedEven
 * will be instantiated which the EventProcessor will handle.
 * 
 * @author Jeffrey
 */
public class InvokedEvent
{
	/** Contains the executer, which fired the event. */
	private IRoomObject initiator;
	
	/** Contains the interactor of the event. */
	private IRoomObject interactor;
	
	/** Contains the name of the event. */
	private String eventType;
	
	/**
	 * @param executer 
	 * @param initiator
	 */
	public InvokedEvent(IRoomObject initiator, IRoomObject interactor, String eventType)
	{
		this.interactor = interactor;
		this.initiator	= initiator;
		this.eventType	= eventType;
		
		System.out.println("DEBUG-INFO: new invoked event of type" + eventType);
		
		Game.getInstance().getEventProcessor().registerInvokedEvent(this);
	}

	/**
	 * Returns the initiator.
	 * 
	 * @return 	IRoomObject
	 */
	public IRoomObject getInitiator()
	{
		return initiator;
	}

	/**
	 * Returns the initiator.
	 * 
	 * @return IRoomObject
	 */
	public IRoomObject getInteractor()
	{
		return interactor;
	}
	
	/**
	 * Returns the name of the event.
	 * 
	 * @return String
	 */
	public String getEventType()
	{
		if(eventType == null)
		{
			System.err.println("ERROR, Event type null");
		}
		return eventType;
	}
}
