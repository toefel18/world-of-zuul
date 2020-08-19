package nl.toefel.game.gamedisplays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import nl.toefel.game.Game;
import nl.toefel.game.command.ClearCommandDisplay;
import nl.toefel.game.command.ICommand;
import nl.toefel.game.roomobject.Point;


/**
 * Class that handles the in-game command windows
 * @author Christophe
 *
 */
public class CommandDisplay {
	
	protected String message;
	//protected HashMap<String, ICommand> commandLines = new HashMap<String, ICommand>();
	protected ArrayList<ICommand> commands = new ArrayList<ICommand>();
	
	/**The selected index in the array */
	protected int selected = 0; 
	
	/**The location to show the window*/
	protected Point location = new Point(10, 10);
	
	/**
	 * Creates an empty command display instance
	 */
	public CommandDisplay(){
	}
	
	/**
	 * Creates a command display pointed to a specific location
	 * @param location relative coordinates relative to the left-top corner of the room
	 */
	public CommandDisplay(Point location){
		this.location = location;
	}
	
	/**
	 * Adds a command to the display
	 * 
	 * @param instantiated command (name will be extracted with command.getName())
	 */
	public void addCommand(ICommand command){
		if(command != null)
			commands.add(command);
	}
	
	/**
	 * Adds a command to the display and sets the commandname
	 * 
	 * @param commandName name of the command
	 * @param command command that is ready to execute
	 */
	public void addCommand(String commandName, ICommand command){
		command.setName(commandName);
		commands.add(command);
	}
	
	/**
	 * Adds a HashMap of commands to the internal representation
	 * NOTICE:every command it's name will be set the name of the key!
	 * 
	 * @param commands HashMap of name<->command pairs
	 */
	public void addCommandMap(HashMap<String, ICommand> commands){
		for(String name: commands.keySet()){
			ICommand command = commands.get(name);
			command.setName(name);
			this.commands.add(command);
		}
	}
	
	/**
	 * Adds an ArrayList of commands to the internal representation
	 * @param commands
	 */
	public void addCommandList(ArrayList<ICommand> commands){
		this.commands.addAll(commands);
	}
	
	/**
	 * Sets the display message
	 * @param message text message
	 */
	public void setTekst(String message){
		this.message = message;
	}
		
	/**
	 * Counts the commands currently registered
	 * @return number of commands
	 */
	public int countCommands(){
		return commands.size();
	}
	
	/**
	 * Returns the current text message
	 * @return message
	 */
	public String getText(){
		return message;
	}
	
	/**
	 * Gets the registered commands
	 * @return name<->command pairs
	 */
	public ArrayList<ICommand> getCommands(){
		//if no commands present, make sure there always is a way to get rid of the display!
		if(commands.size() <= 0)
			commands.add(new ClearCommandDisplay("Back"));
		
		return commands;
	}
	
	/**
	 * Sets the selected command index
	 * @param index
	 */
	public void setSelected(int index){
		if (commands != null && commands.size() > 0)
			selected = index < 0 ? commands.size() - 1 : index % commands.size();
	}
	
	/**
	 * Selects the next command. If there are no more commands left, it selects the very first.
	 */
	public void incrementSelected(){
		setSelected(selected + 1);
	}
	
	/**
	 * Selects the previous command. If there are no previous commands, it selects the last command.
	 */
	public void decrementSelected(){
		setSelected(selected - 1);
	}
	
	/**
	 * Gets the selected index
	 * @return
	 */
	public int getSelected(){
		return selected;
	}
	
	/**
	 * Sets the location, relative to the rooms top-left coordinates
	 * @param location 
	 */
	public void setLocation(Point location){
		this.location = location;
	}
	
	/**
	 * Gets the location, relative to the rooms top-left coordinates
	 * @return
	 */
	public Point getLocation(){
		return location;
	}
	
	/**
	 * Executes the selected command, if no command is selected, the first command will be executed
	 */
	public void executeSelectedCommand()
	{
		ICommand command = commands.get(selected);
		command.execute();
		
		/*
		if(commandLines == null || commandLines.size() <= 0) return;
		
		int num = 0;
		Iterator<ICommand> commandIterator = commandLines.values().iterator();
		
		while(commandIterator.hasNext())
		{
			ICommand currentCommand = commandIterator.next();
			
			if(num == selected)
			{
				currentCommand.execute();

				break;
			}
		    
		    num++;
		}
		*/
		if( Game.getInstance().getCommandDisplay() == this)
			Game.getInstance().clearCommandDisplay();
	}
}
