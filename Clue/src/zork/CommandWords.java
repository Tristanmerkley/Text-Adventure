package zork;

public class CommandWords {
  // a constant array that holds all valid command words
  private static final String validCommands[] = { "quit", "help", "eat", "drink", "take", "drop", "give", "inspect",
      "inventory", "look", "bowl", "insert", "place", "open", "time", "put", "east", "south", "north", "west", "unlock",
      "dig", "southeast", "southwest", "northeast", "northwest" }; // time tells the player how much time they have left

  /**
   * Constructor - initialise the command words.
   */
  public CommandWords() {
    // nothing to do at the moment...
  }

  /**
   * Check whether a given String is a valid command word. Return true if it is,
   * false if it isn't.
   **/
  public boolean isCommand(String aString) {
    for (String c : validCommands) {
      if (c.equals(aString))
        return true;
    }
    // if we get here, the string was not found in the commands
    return false;
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
