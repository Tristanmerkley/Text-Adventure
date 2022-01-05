package zork;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class CommandWords {
  // a constant array that holds all valid command words
  private static final String validCommands[] = {"quit", "help", "eat", "drink", "consume", "take", "drop", "give", "inspect", "inventory", "look", "bowl", "insert", "place", "open", "time", "put", "east", "south", "north", "west", "unlock", "dig", "southeast", "southwest", "northeast", "northwest", "read"}; // time tells the player how much time they have left
  public static String[] dirs = {"north", "east", "south", "west", "n", "e", "s", "w", "northeast", "southwest", "southeast", "northwest"};
  public static final HashSet<String> commands = new HashSet<String>(Arrays.asList(validCommands));
  public static final HashSet<String> directions = new HashSet<String>(Arrays.asList(dirs));
  public static HashMap<String, String> dirConversions = new HashMap<String, String>();


  /**
   * Constructor - initialise the command words.
   */
  public CommandWords() {
    dirConversions.put("n", "north");
    dirConversions.put("s", "south");
    dirConversions.put("e", "east");
    dirConversions.put("w", "west");
  }

  /**
   * Check whether a given String is a valid command word. Return true if it is, false if it isn't.
   **/
  public boolean isCommand(String aString) {
    return commands.contains(aString);
  }

  /*
   * Print all valid commands to System.out.
   */
  public void showAll() {
    for (String c : validCommands) {
      System.out.print(c + "  ");
    }
    System.out.println();
  }
}
