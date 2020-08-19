package nl.toefel.game;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides standard translation methods
 * @author Christophe
 *
 */
public class Translator {
	
	/**
	 * Reads the children node for names in the given language
	 * 
	 * @param containingElement the element which contains children like:<names><name lang="en">englishname</name></names>
	 * @param xPathPrefix, the possible prefix for xpath (example: inventory needs prefix: inventorys/inventory/)
	 * @param lang the language
	 * @param default text to return when not found
	 * @return language depended name of object
	 */
	public static String getTranslation(Node containingElement, String xPathPrefix, String lang, String defaultText){
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList languages = (NodeList) xPath.compile(xPathPrefix + "names/name[@lang='" + lang + "']").evaluate(containingElement, XPathConstants.NODESET);
			
			if(languages.getLength() > 0){
				Node language = languages.item(0);
				
				if(language != null){
					String result = language.getTextContent();
					return result;
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return defaultText;
	}
	
	/**
	 * Reads the children node for names in the given language, if not found it returns "", uses no xpath prefix!
	 * 
	 * @param containingElement the element which contains children like:<names><name lang="en">englishname</name></names>
	 * @param lang the language
	 * @return language depended name of object
	 */	
	public static String getTranslation(Node containingElement, String lang){
		return getTranslation(containingElement, "", lang, "");
	}
	
	
	/**
	 * Reads the children node for names in the given language, if not found it returns defaultName, uses no xpath prefix
	 * 
	 * @param containingElement the element which contains children like:<names><name lang="en">englishname</name></names>
	 * @param lang the language
	 * @param defaultName the default if not found
	 * @return language depended name of object
	 */	
	public static String getTranslation(Node containingElement, String lang, String defaultName){
		return getTranslation(containingElement, "", lang, defaultName);
	}
	
	/**
	 * Searchers for the requested textline in the xml, containing element should be the ROOT of the document!
	 * @param containingElement, should be te root of the xml document!
	 * @param lang language
	 * @param textlineId id of the textline
	 * @return textline
	 */
	public static String getTextline(Node element, String lang, String textlineId){
		try {
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/game/texts/text[@id='" + textlineId + "']/line[@lang='" + lang + "']";
			Node textline = (Node) xPath.compile(expression).evaluate(element, XPathConstants.NODE);
			
			if(textline != null){
				return textline.getFirstChild().getNodeValue();
			}
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return "no text found";
	}
}
