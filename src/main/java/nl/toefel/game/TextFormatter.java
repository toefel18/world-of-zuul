package nl.toefel.game;

import java.awt.Label;
import java.awt.Font;
import java.util.ArrayList;

/**
 * Formats the text using the java label!, this is not acurate!
 * @author Christophe
 *
 */
public class TextFormatter {
	protected String text = "";
	protected int maxWidth = 1;
	protected int fontSize = 10;
	protected String fontName = "Arial";
	protected int fontStyle = Font.PLAIN;
	
	protected ArrayList<String> lines = new ArrayList<String>();
	
	/**
	 * Configures textformatter with a text to break, font that will be used and a width to break on
	 * @param text
	 * @param fontName
	 * @param fontStyle string like "BOLD", "ITALIC", "", "PLAIN", or a combination like  "BOLD|ITALIC" 
	 * @param fontSize
	 * @param maxWidth
	 */
	public TextFormatter(String text, String fontName, String fontStyle, int fontSize, int maxWidth){
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.maxWidth = maxWidth;
		
		if (fontStyle != null && !fontStyle.equals("") && !fontStyle.equalsIgnoreCase("PLAIN")){
			if(fontStyle.equalsIgnoreCase("BOLD")){
				this.fontStyle = Font.BOLD;
			}else if(fontStyle.equalsIgnoreCase("ITALIC")){
				this.fontStyle = Font.ITALIC;
			}else if(fontStyle.equalsIgnoreCase("BOLD|ITALIC")){
				this.fontStyle = Font.ITALIC | Font.BOLD;
			}
		}
		
		formatPreciseWords();
	}

	
	/**
	 * Breaks a string text on letters on the width this object is created with.
	 * @deprecated use precise Words!
	 */
	protected void formatPreciseLetters(){
		if(this.text == null || this.text.length()<= 0 ) return;
		
		Label lbj = new Label();
		int i;
		int starter = 0;
		
		for(i = 0; i < text.length(); i++){
			int strWidth = lbj.getFontMetrics(new Font(fontName, fontStyle, fontSize)).stringWidth(text.substring(starter, i));
			
			if( strWidth > maxWidth){
				lines.add(text.substring(starter, --i).trim());
				starter = i;
			}
		}
		
		if(starter < i)
			lines.add(text.substring(starter));
	}

	/**
	 * Breaks text on words with the configured width 
	 */
	protected void formatPreciseWords(){
		if(this.text == null || this.text.length()<= 0 ) return;
		Label lbj = new Label();
		int i;
		int startIndex = 0;
		String []words = text.split(" ");
		
		if (words.length <= 0)
			return;
		
		for(i = 1; i <= words.length; i++){
			String line = pasteString(startIndex, i, words);
			int strWidth = lbj.getFontMetrics(new Font(fontName, fontStyle, fontSize)).stringWidth(line);
			
			if( strWidth > maxWidth){
				--i;
				line = pasteString(startIndex, i, words).trim();
				lines.add(line);
				startIndex = i;
			}
		}
		
		if(i > words.length)
			i = words.length;
				
		if(startIndex < i)
			lines.add(pasteString(startIndex, i, words).trim());
	}
	
	/**
	 * Determines the width of a string, formatted with the given parameters
	 * 
	 * @param text
	 * @param fontName
	 * @param fontStyle string like "BOLD", "ITALIC", "", "PLAIN", or a combination like  "BOLD|ITALIC"
	 * @param fontSize
	 */
	public static int getStringWidth(String text, String fontName, String fontStyle, int fontSize){
		int fontStyleInt = Font.PLAIN;
		
		if (fontStyle != null && !fontStyle.equals("") && !fontStyle.equalsIgnoreCase("PLAIN")){
			if(fontStyle.equalsIgnoreCase("BOLD")){
				fontStyleInt = Font.BOLD;
			}else if(fontStyle.equalsIgnoreCase("ITALIC")){
				fontStyleInt = Font.ITALIC;
			}else if(fontStyle.equalsIgnoreCase("BOLD|ITALIC")){
				fontStyleInt = Font.ITALIC | Font.BOLD;
			}
		}
		
		Label lbj = new Label();
		return lbj.getFontMetrics(new Font(fontName, fontStyleInt, fontSize)).stringWidth(text);
	}
	
	/**
	 * Concatenates words from an array from the start index to the end index
	 * @param startIndex start index in array
 	 * @param endIndex everything before this index will be concatenated
	 * @param words array with words
	 * @param glue concatenate sequence
	 * @return string
	 */
	private String pasteString(int startIndex, int endIndex, String[] words, String glue){
		String temp = "";
		
		for(int i = startIndex; i < endIndex; i++)
			temp = temp + glue + words[i];
		
		return temp;
	}

	/**
	 * Concatenates words from an array with spaces from the start index to the end index
	 * @param startIndex start index in array
 	 * @param endIndex everything before this index will be concatenated
	 * @param words array with words
	 * @return string
	 */
	private String pasteString(int startIndex, int endIndex, String[] words){
		return pasteString(startIndex, endIndex, words, " ");
	}
	
	/**
	 * Returns the number of lines
	 * @return number of lines
	 */
	public int getLineCount(){
		return lines.size();
	}
	
	/**
	 * Returns the complete configured text
	 * @return text
	 */
	public String getText(){
		return text;
	}
	
	/**
	 * Returns an array with broken lines
	 * @return array with lines
	 */
	public ArrayList<String> getLines(){
		return lines;
	}
}
