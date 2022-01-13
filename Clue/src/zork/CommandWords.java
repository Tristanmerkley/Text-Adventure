package zork;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class CommandWords {
  // a constant array that holds all valid command words
  public static final HashSet<String> commands = new HashSet<String>(Arrays.asList("quit", "help", "eat", "drink", "take", "drop", "inspect", "inventory", "look", "bowl", "insert", "place", "open", "time", "put", "unlock", "dig", "read", "save", "load", "time"));
  public static final HashSet<String> directions = new HashSet<String>(Arrays.asList("north", "east", "south", "west", "n", "e", "s", "w", "northeast", "southwest", "southeast", "northwest", "nw", "ne", "sw", "se"));
  public static HashMap<String, String> dirConversions = new HashMap<String, String>();

  // just combined the Valid Commands and the directions with their respective HashSets

  /**
   * Constructor - initialise the command words. added nw ne sw se to be converted
   */
  public CommandWords() {
    dirConversions.put("n", "north");
    dirConversions.put("s", "south");
    dirConversions.put("e", "east");
    dirConversions.put("w", "west");
    dirConversions.put("nw", "northwest");
    dirConversions.put("ne", "northeast");
    dirConversions.put("sw", "southwest");
    dirConversions.put("se", "southeast");
  }

  /**
   * Check whether a given String is a valid command word. Return true if it is, false if it isn't.
   **/
  public boolean isCommand(String command) {
    return commands.contains(command) || directions.contains(command);
  }

  /*
   * Print all valid commands to System.out.
   */
  public void showAll() {
    for (String c : commands) {
      System.out.print(c + "  ");
    }
    System.out.println();
  }
}
