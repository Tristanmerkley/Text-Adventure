package zork;

import java.util.Date;
import java.util.Scanner;

public class Parser {
  private CommandWords commands; // holds all valid command words
  private Scanner in;

  public Parser() {
    commands = new CommandWords();
    in = new Scanner(System.in);
  }

  /**
   * takes a command from the player and tells them how much time is left.
   * @return
   * @throws java.io.IOException
   */
  public Command getCommand() throws java.io.IOException {
    String inputLine = "";
    String[] words;
    double timeLeft = (Game.MAX_ALLOWED_TIME - (Game.timeElapsed + (new Date().getTime() - Game.startTime) / 1000.0));
    System.out.print("\033[1;91m" + "Time Remaining: " + (int) timeLeft / 60); // prints minutes left using red font
    if (timeLeft % 60 != 0)
      System.out.printf(":" + "%02.0f", timeLeft % 60); // prints out seconds remaing when more than 0 seconds
    System.out.print("\n\u001B[0m> "); // print prompt

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
