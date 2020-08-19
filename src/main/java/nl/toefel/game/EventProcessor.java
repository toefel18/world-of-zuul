package nl.toefel.game;

import nl.toefel.game.Game;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;

import java.applet.AudioClip;
import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import nl.toefel.game.command.*;
import nl.toefel.game.controller.IController;
import nl.toefel.game.controller.KeyController;
import nl.toefel.game.roomobject.*;
import nl.toefel.game.roomobject.movable.*;
import nl.toefel.game.sound.AePlayWave;
import nl.toefel.game.sound.AudioPlayer;
import nl.toefel.game.sound.MidiPlayer;
import nl.toefel.game.drawer.IDrawer;
import nl.toefel.game.drawer.SWTDrawer;
import nl.toefel.game.gamedisplays.CommandDisplay;
import nl.toefel.game.gamedisplays.ComplexDisplay;
import nl.toefel.game.ControlProcessor;

public class EventProcessor implements Runnable
{
	/** Contains all event instances. */
	private HashMap<String, Event> events = new HashMap<String, Event>();
	
	/** Contains the events -node of Game's XML. */
	private Node eventsNode = null;
	
	/** Contains the Game instance. */
	private Game game;
	
	//TODO CONSIDER CHANGEING ARRAYLIST TO VECTOR, THIS CLASS IS SYNCHRONISED 
	/** Contains all invoked events which are currently processed. */
	private ArrayList<InvokedEvent> processingInvokedEvents = new ArrayList<InvokedEvent>();
	
	/** Contains all invoked events which will be processed. */
	private ArrayList<InvokedEvent> waitingInvokedEvents = new ArrayList<InvokedEvent>();
	
	/** Contains the fact if the processor should run (again) or not. */
	private boolean process = true;
	
	/** Contains the amount of milliseconds between every iteration in the processor. */
	private int recheckDelay = 1000 / 40;
	
	/**
	 * Returns the invoked events.
	 */
	public ArrayList<InvokedEvent> getProcessingInvokedEvents()
	{
		return processingInvokedEvents;
	}
	
	/**
	 * Initiates all events.
	 */
	private void init()
	{
		try
		{
			eventsNode = game.getEventsXML();
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			// Initiate the Event instances by the event nodes in the XML.
			NodeList events = (NodeList) xPath.compile("event").evaluate(eventsNode, XPathConstants.NODESET);
		    
			// Construct event instances and give them their begin state.
			for(int i = 0, max = events.getLength(); i < max; i++)
			{
				Event event = new Event(this, events.item(i));
				System.out.println("registering event id: " + event.getId());
				this.events.put(event.getId(), event);
			}
				
			// @note Debug.
			System.out.println("Event Processor initialized.");
		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Registers the given invoked event, which will be processed.
	 * 
	 * @param invokedEvent
	 */
	public void registerInvokedEvent(InvokedEvent invokedEvent)
	{
		waitingInvokedEvents.add(invokedEvent);
	}
	
	/**
	 * Initiates and runs the processor.
	 */
	public void run()
	{
		game = Game.getInstance();
		
		init();
		
		while(process)
		{
			synchronized (this)
			{	
				// Switch the events which are waiting, to be processed.
				processingInvokedEvents.addAll(waitingInvokedEvents);
				waitingInvokedEvents.clear();
				
				// Process all events.
				for(Event event : events.values())
					event.process();
				
				// Delete all invoked events.
				processingInvokedEvents.clear();
			}
			
			try
			{
				Thread.sleep(recheckDelay);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("DEBUG-INFO: Event Processsing runmethod stopped (THREAD STOP)");
	}
	
	/**
	 * Stops the event processor, including the thread.
	 */
	public void setStopFlag()
	{
		process = false;
	}
}