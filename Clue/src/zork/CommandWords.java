package zork;

public class CommandWords {
  // a constant array that holds all valid command words
  private static final String validCommands[] =
      {"go", "quit", "help", "eat", "drink", "take", "drop", "north", "east", "west", "south", "up", "down", "give", "inspect", "n", "s", "e", "w", "u", "d", "inventory", "i", "look", "l"}; //key words for lock picking and other unique action

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
