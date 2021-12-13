package zork;

import java.util.Scanner;

public class Parser {
  private CommandWords commands; // holds all valid command words
  private Scanner in;

  public Parser() {
    commands = new CommandWords();
    in = new Scanner(System.in);
  }

  public Command getCommand() throws java.io.IOException {
    String inputLine = "";
    String[] words;

    System.out.print("> "); // print prompt

    inputLine = in.nextLine();

    words = inputLine.split(" ", 2);
    String word1 = words[0];
    words = words.length > 1 ? words[1].split(" in ") : null;
    String word2 = words != null ? words[0] : null;
    String word3 = words != null && words.length > 1 ? words[1] : null;

    if (word3 != null && commands.isCommand(word1))
      return new Command(word1, word2, word3);
    if (commands.isCommand(word1))
      return new Command(word1, word2);
    else
      return new Command(null, word2);

  }

  /**
   * Print out a list of valid command words.
   */
  public void showCommands() {
    commands.showAll();
  }
}
