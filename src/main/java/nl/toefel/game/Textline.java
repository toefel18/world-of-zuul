package nl.toefel.game;

import javax.xml.xpath.*;
import org.w3c.dom.Node;

/**
 * Is a textline.
 */
public class Textline {

	/** Contains the XML node. */
	private Node state;
	
	/**
	 * Stores the given Node.
	 * 
	 * @param Node state The XML node of the text.
	 */
	public Textline(Node state)
	{
		this.state = state;
	}
	
	/**
	 * Returns the text of the textline, in the correct language.
	 * TODO remove texception throw, return "" on error and send message to system.out!
	 * @return The text.
	 * @throws Exception
	 */
	public String getText() throws Exception
	{
		// Create a new Xpath instance.
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		// Construct the Xpath query.
		String query = "line[@lang='" + Game.getInstance().getLanguage() + "'][1]";
		
		// Compile and evaluate the query on the Xpath object, to obtain the String which is the requested line.
		Node textLine = (Node) xpath.compile(query).evaluate(state, XPathConstants.NODE);
			
		if(textLine == null)
			throw new Exception("The Textline does not support the language '" + Game.getInstance().getLanguage() + "'.");
			
		return textLine.getFirstChild().getNodeValue();
	}
	
	/**
	 * Returns the emotion of the textline.
	 * 
	 * @return Emotion, e.g. 'normal', 'happy', 'angry'. Returns 'normal' by default.
	 */
	public String getEmotion()
	{
		Node emotion = state.getAttributes().getNamedItem("emotion");
		
		return emotion != null ? emotion.getNodeValue() : "normal";
	}
}
