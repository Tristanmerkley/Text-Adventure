package zork;

public class Command {
  private String commandWord;
  private String secondWord;
  private String thirdWord;

  /**
   * Create a command object. First and second word must be supplied, but either
   * one (or both) can be null. The command word should be null to indicate that
   * this was a command that is not recognised by this game.
   */
  public Command(String firstWord, String secondWord) {
    commandWord = firstWord;
    this.secondWord = secondWord;
  }

  public Command(String firstWord, String secondWord, String thirdWord) {
    commandWord = firstWord;
    this.secondWord = secondWord;
    this.thirdWord = thirdWord;
  }

  /**
   * Return the command word (the first word) of this command. If the command was
   * not understood, the result is null.
   */
  public String getCommandWord() {
    return commandWord;
  }

  /**
   * Return the second word of this command. Returns null if there was no second
   * word.
   */
  public String getSecondWord() {
    if (CommandWords.dirConversions.get(secondWord) != null)
      return CommandWords.dirConversions.get(secondWord);
    return secondWord;
  }

  /**
   * Return true if this command was not understood.
   */
  public boolean isUnknown() {
    return (commandWord == null);
  }

  /**
   * Return true if the command has a second word.
   */
  public boolean hasSecondWord() {
    return (secondWord != null);
  }

  public boolean isDirection(String commandWord) {
    return CommandWords.directions.contains(commandWord); // returns true or false
  }

  public boolean hasThirdWord() {
    return (thirdWord != null);
  }

  public String getThirdWord() {
    return thirdWord;
  }
}
