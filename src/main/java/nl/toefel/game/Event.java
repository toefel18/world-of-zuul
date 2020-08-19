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

public class Event
{
	/** Contains the XML node which represents the beginstate of the Event. */
	private Node beginState;
	
	/** Contains the amount of times this event is executed. */
	private int timesExecuted = 0;
	
	/** Contains the amount of times this event can be executed. Zero is unlimited. */
	private int timesExecutable = 0;
	
	/** Contains the required initiator. */
	private IRoomObject initiator;
	
	/** Contains the required executor. */
	private IRoomObject interactor;
	
	/** Contains the event type this event should responds to*/
	private String eventType = "";
	
	/** Contains the instance of the event processor. */
	private EventProcessor eventProcessor;
	
	public Event(EventProcessor eventProcessor, Node beginState)
	{
		this.eventProcessor = eventProcessor;
		this.beginState 	= beginState;
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		// Initiate the Event instances by the event nodes in the XML. Implemented due performance?
		try
		{
			// <timesexecuted>
			Node timesExecutedNode = (Node) xPath.compile("timesexecuted").evaluate(beginState, XPathConstants.NODE);
			
			if(timesExecutedNode != null)
				timesExecuted = Integer.valueOf(timesExecutedNode.getFirstChild().getNodeValue());
			
			// <timesexecutable>
			Node timesExecutableNode = (Node) xPath.compile("timesexecutable").evaluate(beginState, XPathConstants.NODE);

			if(timesExecutableNode != null)
				timesExecutable = Integer.valueOf(timesExecutableNode.getFirstChild().getNodeValue());
			
			// <initiator>
			Node initiatorNode = (Node) xPath.compile("conditions/initiator").evaluate(beginState, XPathConstants.NODE);

			if(initiatorNode != null)
				initiator = Game.getInstance().getRoomObjectById(initiatorNode.getFirstChild().getNodeValue());
			
			// <executor>
			Node interactorNode = (Node) xPath.compile("conditions/interactor").evaluate(beginState, XPathConstants.NODE);

			if(interactorNode != null)
				interactor = Game.getInstance().getRoomObjectById(interactorNode.getFirstChild().getNodeValue());
			
			Node eventTypeNode = (Node) xPath.compile("conditions/eventtype").evaluate(beginState, XPathConstants.NODE);

			if(eventTypeNode != null)
				eventType = eventTypeNode.getFirstChild().getNodeValue();

		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
		//this.timesExecuted = 
	}
	
	/**
	 * Returns the identifier.
	 *
	 * @return The identifier.
	 */
	public String getId()
	{
		return beginState.getAttributes().getNamedItem("id").getNodeValue();
	}
	
	/**
	 * Checks if the conditions meets.
	 * 
	 * @todo	Implement more requirements.
	 * 
	 * @return
	 */
	private boolean meetsConditions(InvokedEvent invokedEvent)
	{
		if(interactor != null && interactor != invokedEvent.getInteractor())
			return false;
		
		if(initiator != null && initiator != invokedEvent.getInitiator())
			return false;
		
		if(!eventType.equals(invokedEvent.getEventType()))
			return false;
		
		
		try {
			//Check if all checkproperty elements meet their conditions
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList checks = (NodeList)xPath.compile("conditions/checkproperty").evaluate(beginState, XPathConstants.NODESET);
			
			if(checks != null){ 
				for(int i = 0; i < checks.getLength(); i++){
					Node check = checks.item(i);
					if(check == null) continue;
					
					if(!checkProperty(check)) return false;
				}
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;
	}
	
	/**
	 * Checks if the property evaluates to true
	 * @param xmlnode the containing node
	 * @return
	 */
	private boolean checkProperty(Node xmlNode){
		NamedNodeMap attributes = xmlNode.getAttributes();
		Node checkType = attributes.getNamedItem("checktype");
		
		if(checkType == null){
			System.err.println("Check property on event " + getId() + " doesn't containt checktype! cannot evaluate!");
			return false;
		}
		
		String type = checkType.getNodeValue();
		
		if(type.equals("hasitem")){
			Node propertyOf = attributes.getNamedItem("propertyof");
			Node itemId = attributes.getNamedItem("itemid");
			
			if(propertyOf == null || itemId == null) {
				System.err.println("Check hasitem property on event " + getId() + " doesn't containt propertyof or itemid! cannot evaluate!");
				return false;
			}
			
			IRoomObject object = Game.getInstance().getRoomObjectById(propertyOf.getNodeValue());
			
			if(!(object instanceof IOrganism)){
				System.err.println("Check hasitem property on event " + getId() + " checks on an object that cannot carry items! cannot evaluate!");
				return false;
			}
						
			IOrganism organism = (IOrganism) object;
			
			IRoomObject item = Game.getInstance().getRoomObjectById(itemId.getNodeValue());
			if(!(item instanceof ICarryable)){
				System.err.println("Check hasitem property on event " + getId() + " checks on an object that is not carryable! cannot evaluate!");
				return false;	
			}
			
			if(!organism.hasInInventory((ICarryable)item))
				return false;
			
		}
				
		return true;
	}
	
	/**
	 * Returns true when the amount of times executed is smaller then the limit, or when no limit is set.
	 * 
	 * TODO
	 * 
	 * @return True if fireable, false if not.
	 */
	private boolean fireable()
	{
	//	System.out.println(timesExecuted + " < " + timesExecutable);
		
		if(timesExecutable == 0)
			return true;
		
		else if(timesExecuted < timesExecutable)
			return true;
		
		else
			return false;
	}
	
	/**
	 * Processes the given InvokedEvent (if possible).
	 */
	private void processInvokedEvent(InvokedEvent invokedEvent)
	{
		// Test if the conditions are met.
		if(!meetsConditions(invokedEvent))
			return;
	
		try
		{
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			// Process the nodes of the execute element
			//TODO optimalize pre-read commands and store in simple array, xpath extractions can be expensive
			NodeList executeNodes = (NodeList) xPath.compile("execute/command").evaluate(beginState, XPathConstants.NODESET);
		    
			for(int i = 0, max = executeNodes.getLength(); i < max; i++)
			{
				Node executeNode = executeNodes.item(i);
				
				if(executeNode.getNodeName() == "command")
				{
					NamedNodeMap attributes = executeNode.getAttributes();
					
					Game game = Game.getInstance();
	
					Node commandNameNode = attributes.getNamedItem("commandname");
					Node initiatorNode = attributes.getNamedItem("initiator");
					Node executerNode = attributes.getNamedItem("executer");
					
					String commandName = "";
					IRoomObject initiator =  null;//
					IRoomObject executer = null;//attributes.getNamedItem("executer").getNodeValue();
					
					if(commandNameNode != null){
						commandName = commandNameNode.getNodeValue();
					}
					
					if(initiatorNode != null){
						initiator = game.getRoomObjectById(initiatorNode.getNodeValue());
					}
					
					if(executerNode != null){
						executer = game.getRoomObjectById(executerNode.getNodeValue());
					}				
					
					ICommand command = game.getCommand(
															commandName,
															executeNode,
															initiator,
															executer	
														);
					if(command == null){
						System.err.println("ERROR, event resulted in command that cannot be executed, event id: " + getId());
						return;
					}
					
					command.execute();
				}
			}
		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		increaseTimesExecuted();
	}
	
	/**
	 * Increases the amount of times the event has been executed, by one.
	 * TODO optimalize, do not use xpath, but store in local node!
	 */
	private void increaseTimesExecuted()
	{
		timesExecuted++;
		
		try
		{
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			Node timesExecutedNode;timesExecutedNode = (Node) xPath.compile("timesexecuted").evaluate(beginState, XPathConstants.NODE);
		
			if(timesExecutedNode != null)
				timesExecutedNode.getFirstChild().setNodeValue(String.valueOf(timesExecuted));
		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Processes the event.
	 */
	public void process()
	{
		if(fireable() == false)
			return;
		
		// @todo Process events based on time related events.
		
		// Process all invoked objects.
		for(InvokedEvent invokedEvent : eventProcessor.getProcessingInvokedEvents())
			processInvokedEvent(invokedEvent);
	}
}
