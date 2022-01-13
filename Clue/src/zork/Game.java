package zork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game {

  private static final String GAME_SAVE_LOCATION = "src/zork/data/game.ser";
  public static final Double MAX_ALLOWED_TIME = 1200.0; // amount of time before losing in minutes
  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();
  public static HashMap<String, Item> itemMap = new HashMap<String, Item>();

  public static Double timeElapsed = 0.0;
  public static Long startTime, endTime;
  private Parser parser;
  private Room currentRoom;
  private Inventory playerInventory;
  private boolean isUseable = false;
  private Scanner in;

  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("src/zork/data/rooms.json");
      initItems("src/zork/data/items.json");
      currentRoom = roomMap.get("Theatre"); // ! spawn room
      playerInventory = new Inventory(300); // ! player max inventory weight
    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
  }

  private void initItems(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);
    JSONArray jsonItems = (JSONArray) json.get("items");
    HashMap<String, String> roomPlacement = new HashMap<String, String>();
    HashMap<String, String> itemPlacement = new HashMap<String, String>();
    for (Object itemObj : jsonItems) {
      Item item = new Item();
      String itemName = (String) ((JSONObject) itemObj).get("name");
      String itemAlternateName = (String) ((JSONObject) itemObj).get("alternatename");
      String itemId = (String) ((JSONObject) itemObj).get("id");
      long weight = ((JSONObject) itemObj).get("weight") != null ? (Long) ((JSONObject) itemObj).get("weight") : Integer.MAX_VALUE;
      long holdingWeight; // ! how much an item can hold in its inventory
      boolean isLocked = ((JSONObject) itemObj).get("isLocked") != null ? (Boolean) ((JSONObject) itemObj).get("isLocked") : false;
      boolean isOpenable = ((JSONObject) itemObj).get("isOpenable") != null ? (Boolean) ((JSONObject) itemObj).get("isOpenable") : false;
      boolean isEdible = ((JSONObject) itemObj).get("isEdible") != null ? (Boolean) ((JSONObject) itemObj).get("isEdible") : false;
      boolean isDrinkable = ((JSONObject) itemObj).get("isDrinkable") != null ? (Boolean) ((JSONObject) itemObj).get("isDrinkable") : false;

      if (!isOpenable)
        holdingWeight = 0;
      else
        holdingWeight = ((JSONObject) itemObj).get("holdingWeight") != null ? (Long) ((JSONObject) itemObj).get("holdingWeight") : Long.MAX_VALUE;
      String itemDescription = (String) ((JSONObject) itemObj).get("description");
      String startingRoom = ((JSONObject) itemObj).get("startingroom") != null ? (String) ((JSONObject) itemObj).get("startingroom") : null;
      String startingItem = ((JSONObject) itemObj).get("startingitem") != null ? (String) ((JSONObject) itemObj).get("startingitem") : null;

      item.setEdible(isEdible);
      item.setDrinkable(isDrinkable);
      item.setDescription(itemDescription);
      item.setName(itemName);
      item.setAlternateName(itemAlternateName);
      item.setLocked(isLocked);
      item.setOpenable(isOpenable);
      item.setWeight((int) weight);
      item.createInventory(holdingWeight);

      if (startingRoom != null)
        roomPlacement.put(itemId, startingRoom);

      if (startingItem != null)
        itemPlacement.put(itemId, startingItem);

      itemMap.put(itemId, item);
    }

    for (String itemId : roomPlacement.keySet()) {
      Item item = itemMap.get(itemId);
      Room room = roomMap.get(roomPlacement.get(itemId));
      room.addItem(item);
    }

    for (String itemId : itemPlacement.keySet()) {
      Item item = itemMap.get(itemId);
      Item openableItem = itemMap.get(itemPlacement.get(itemId));
      openableItem.addItem(item);
    }
  }

  private void initRooms(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);

    JSONArray jsonRooms = (JSONArray) json.get("rooms");

    for (Object roomObj : jsonRooms) {
      Room room = new Room();
      String roomName = (String) ((JSONObject) roomObj).get("name");
      String roomId = (String) ((JSONObject) roomObj).get("id");
      String roomDescription = (String) ((JSONObject) roomObj).get("description");
      room.setDescription(roomDescription);
      room.setRoomName(roomName);

      JSONArray jsonExits = (JSONArray) ((JSONObject) roomObj).get("exits");
      ArrayList<Exit> exits = new ArrayList<Exit>();
      for (Object exitObj : jsonExits) {
        String direction = (String) ((JSONObject) exitObj).get("direction");
        String adjacentRoom = (String) ((JSONObject) exitObj).get("adjacentRoom");
        String keyId = (String) ((JSONObject) exitObj).get("keyId");
        Boolean isLocked = (Boolean) ((JSONObject) exitObj).get("isLocked");
        Boolean isOpen = (Boolean) ((JSONObject) exitObj).get("isOpen");
        Exit exit = new Exit(direction, adjacentRoom, isLocked, keyId, isOpen);
        exits.add(exit);
      }
      room.setExits(exits);
      roomMap.put(roomId, room);
    }
  }

  /**
   * Main play routine. Loops until end of play.
   */
  public void play() {
    printWelcome();
    boolean finished = false;
    while (!finished) {
      Command command;
      try {
        startTime = new Date().getTime();
        command = parser.getCommand();
        finished = processCommand(command);
        endTime = new Date().getTime();
        timeElapsed += (endTime - startTime) / 1000.0;
        if (timeElapsed > MAX_ALLOWED_TIME) {
          // lost
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (currentRoom.getRoomName().equalsIgnoreCase("the end")) {
        finished = true;
        System.out.println("\u001B[31m" + "Congratulations! You have successfully escaped the house!");
      }
    }
    System.out.println("Thank you for playing.  Good bye.");
    if (in != null)
      in.close();
  }

  /**
   * Print out the opening message for the player.
   */
  private void printWelcome() {
    // !welcome.title(); //disabled for testing
    System.out.println();
    System.out.println("Welcome to _____."); // TODO need to pick game name
    System.out.println("Type 'help' if you need help.");
    System.out.println("You have 24 hours to escape the house and pass the gate, or else you will be killed.");
    System.out.println("Every ten decisions made, an hour will pass.");
    System.out.println("Pay attention to detail, everything is there for a reason. ");
    System.out.println("Don't stray from the path, follow the clues to escape in time");
    System.out.println("Your time starts now… What are you waiting for? \n");
    System.out.println(currentRoom.longDescription());
    currentRoom.displayInventory();
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends the game, true is
   * returned, otherwise false is returned.
   */
  private boolean processCommand(Command command) { // returning true ends game
    if (command.isUnknown()) {
      System.out.println("I don't know what you mean...");
      return false;
    }
    String commandWord = command.getCommandWord();
    if (commandWord.equals("help"))
      printHelp();
    else if (command.isDirection(commandWord))
      goRoom(command);
    else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        System.out.println("\033[33;5m Are you sure you want to quit? You can also save your game if you want. \u001B[0m");
      if (in == null)
        in = new Scanner(System.in);
      System.out.print("> ");
      String answer = in.nextLine();
      if (answer.equals("yes") || answer.equals("y")) {
        return true;
      } else
        return false;
    } else if (commandWord.equalsIgnoreCase("eat") || commandWord.equalsIgnoreCase("drink")) {
      consumeItem(command);
    } else if (commandWord.equalsIgnoreCase("inventory")) {
      printInventory();
    } else if (commandWord.equalsIgnoreCase("take")) {
      takeItem(command);
    } else if (commandWord.equalsIgnoreCase("drop")) {
      dropItem(command);
    } else if (commandWord.equalsIgnoreCase("place")) { // give cheese to mouse
      placeItem(command);
    } else if (commandWord.equalsIgnoreCase("look")) {
      lookAround();
    } else if (commandWord.equalsIgnoreCase("bowl")) {
      bowling();
    } else if (commandWord.equalsIgnoreCase("open")) {
      openObject(command);
    } else if (commandWord.equalsIgnoreCase("unlock")) {
      unlockDoor(command);
    } else if (commandWord.equalsIgnoreCase("read")) {
      read(command);
    } else if (commandWord.equalsIgnoreCase("save")) {
      save();
    } else if (commandWord.equalsIgnoreCase("load")) {
      load();
    } else if (commandWord.equalsIgnoreCase("time")) {
      printTime();
    }
    return false;
  }

  // implementations of user commands:

  /**
   * Prints out the total run time of the current game
   */
  private void printTime() {
    endTime = new Date().getTime();
    System.out.printf("%5.2f%n", timeElapsed + (endTime - startTime) / 1000.0);
  }

  private void load() {
    Save save = null;
    try {
      FileInputStream fileIn = new FileInputStream(GAME_SAVE_LOCATION);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      save = (Save) in.readObject();
      in.close();
      fileIn.close();
    } catch (InvalidClassException i) {
      System.out.println("File is currupt");
    } catch (ClassNotFoundException j) {
      System.out.println("File is incorrect");
    } catch (FileNotFoundException ex) {
      System.out.println("No saved games!!");
    } catch (IOException e) {
      System.out.println("Can't load game");
    }

    if (save != null) {
      roomMap = save.getRoomMap();
      itemMap = save.getItemMap();
      currentRoom = save.getCurrentRoom();
      playerInventory = save.getPlayerInventory();
    }
  }

  private void read(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("What do you want to read?");
      return;
    }
    String item = command.getSecondWord();
    Item object = nonNull(item);
    if (object == null) {
      System.out.println(item + " is not a vaild object.");
      return;
    }
    if (!item.equalsIgnoreCase("book")) {
      System.out.println("You can't read that.");
      return;
    } else {
      isUseable = true;
      System.out.println("You've read the book. You can now unlock doors with basic locks using a knife.");
      playerInventory.removeItem("book");
    }
  }

  private void unlockDoor(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Which direction is the door you want to unlock?");
      return;
    }
    if (command.getSecondWord().equalsIgnoreCase("safe")) {
      if (currentRoom.contains("safe") != null) {
        if (currentRoom.contains("safe").isLocked())
          unlockSafe(command);
        else
          System.out.println("The safe is already unlocked!");
      } else
        System.out.println("There is no safe in the current room!");
      return;
    }
    if (command.getSecondWord().equalsIgnoreCase("desk") && currentRoom.contains("desk").isLocked()) {
      if (currentRoom.contains("desk") != null) {
        if (currentRoom.contains("desk").isLocked())
          unlockDesk(command); // TODO
        else
          System.out.println("The desk is already unlocked!");
      } else
        System.out.println("There is no desk in the current room!");
      return;
    }
    if (!command.isDirection(command.getSecondWord())) {
      System.out.println(command.getSecondWord() + " is not a vaild direction");
      return;
    }
    for (Exit i : currentRoom.getExits()) {
      String direction = command.getSecondWord();
      String temp = CommandWords.dirConversions.get(command.getSecondWord());
      if (temp != null)
        direction = temp;
      if (i.getDirection().equalsIgnoreCase(direction) || i.getDirection().substring(0, 1).equalsIgnoreCase(direction)) {
        if (!i.isLocked()) {
          System.out.println(Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " is already unlocked.");
          return;
        }
        if (i.getAdjacentRoom().equals("Library") || i.getAdjacentRoom().equals("Maze1")) {
          if ((playerInventory.contains("Knife") != null) && isUseable) {
            i.setLocked(false);
            System.out.println("You picked the " + Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " door.");
          } else if (!isUseable) {
            System.out.println("Read a special book to be able to pick basic locks.");
          } else if (playerInventory.contains("Knife") == null) {
            System.out.println("Find a knife to be able to pick basic locks.");
          } else {
            System.out.println("You need to have read a special book and find a knife before you can unlock this door.");
          }
          return;
        }
        if (i.getAdjacentRoom().equals("Closet3")) {
          System.out.println("What is my favourite colour?");
          if (in == null)
            in = new Scanner(System.in);
          System.out.print("> ");
          String code = in.nextLine();
          if (code.equalsIgnoreCase("purple")) {
            i.setLocked(false);
            System.out.println("The closet is now unlocked");
          } else
            System.out.println("That is not the right code");
          return;
        }
        for (Item j : playerInventory.getInventory()) {
          if (i.getKeyId().equals(j.getKeyId())) {
            i.setLocked(false);
            System.out.println("Unlocked the " + Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " door.");
            return;
          }
        }
        System.out.println("You do not have the correct key for the " + Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " door.");
        return;
      }
    }
    System.out.println("There is no door there!");
  }

  private void unlockDesk(Command command) {
    if (currentRoom.contains("Piggy bank").contains("Peculiar coin") == null) {
      System.out.println("Is there a coin in the piggy bank yet?");
    } else {
      currentRoom.contains("Desk").setLocked(false);
      System.out.println("The desk has been unlocked");
    }
  }

  private void unlockSafe(Command command) {
    System.out.println("What is the 4-digit code?");
    if (in == null)
      in = new Scanner(System.in);
    System.out.print("> ");
    String code = in.nextLine();
    if (code.equals("6351")) {
      currentRoom.contains("safe").setLocked(false);
      System.out.println("The safe is now unlocked");
    } else
      System.out.println("That is not the right code");
  }

  private void openObject(Command command) {
    String item = command.getSecondWord();
    if (!command.hasSecondWord()) {
      System.out.println("Open what?");
      return;
    }
    Item object = nonNull(item);
    if (object == null) {
      System.out.println(item + " is not a vaild object");
      return;
    }
    if (object.isOpen()) {
      System.out.println(object.getName() + " is already open!");
      return;
    }
    if (object.isOpenable()) {
      if (object.isLocked()) {
        System.out.println("You must first unlock: " + object.getName());
        return;
      }
      if (item.equalsIgnoreCase("Main floor map") || item.equalsIgnoreCase("Upstairs left map") || item.equalsIgnoreCase("Upstairs right map")) {
        printMap(item);
        return;
      }
      if (item.equalsIgnoreCase("Hole")) {
        if (playerInventory.contains("Shovel") != null) {
          nonNull(item).setOpen(true);
          System.out.println("Opened " + object.getName() + "\n\nContains:");
          object.displayInventory();
          Item shedKey = new Key("shedKey", "Key from hole", 1);
          playerInventory.addItem(shedKey);
          System.out.println("A key has been added to your inventory");
          return;
        } else {
          System.out.println("You need to use the shovel to open the hole.");
          return;
        }
      }
      nonNull(item).setOpen(true);
      System.out.println("You opened the " + object.getName() + "\n\nContains:");
      object.displayInventory();
    } else
      System.out.println("You cannot open the " + object.getName());
  }

  /**
   * checks the current room, player inventory and items in the current room for a specified item.
   *
   * @param item to search for
   * @return the specified item if found, otherwise null
   */
  private Item nonNull(String item) {
    if (currentRoom.contains(item) != null)
      return currentRoom.contains(item);
    if (playerInventory.contains(item) != null)
      return playerInventory.contains(item);
    for (Item i : currentRoom.getInventory()) {
      if (i.contains(item) != null) {
        return i;
      }
    }
    return null;
  }

  private void printMap(String map) {
    if (map.equalsIgnoreCase("Main floor map")) {
      try {
        Scanner in = new Scanner(new File("src\\zork\\floor0.map"));
        while (in.hasNextLine()) {
          System.out.println(in.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else if (map.equalsIgnoreCase("Upstairs left map")) {
      try {
        Scanner in = new Scanner(new File("src\\zork\\floor1secondhalf.map"));
        while (in.hasNextLine()) {
          System.out.println(in.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      try {
        Scanner in = new Scanner(new File("src\\zork\\floor1firsthalf.map"));
        while (in.hasNextLine()) {
          System.out.println(in.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

  }

  private void lookAround() {
    System.out.println(currentRoom.longDescription());
    currentRoom.displayInventory();
  }

  private void takeItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Take what?");
      return;
    }
    if (currentRoom.getTotalInventorySize() - currentRoom.numItemsCannotMove() <= 0) {
      System.out.println("There are no items to take.");
      return;
    }

    String shoe = command.getSecondWord();
    if (shoe.equals("shoe")) {
      System.out.println("A peculiar coin fell out of the shoe and it has been added to your inventory");
      Item coin = new Item(1, "Peculiar Coin", false, 0);
      coin.setDescription("This is a weird looking coin, I should find somewhere to keep this safe.");
      playerInventory.addItem(coin);
    }

    if (command.getSecondWord().equals("all")) {
      ArrayList<Item> inventory = currentRoom.getInventory();
      int i = 0;
      String taken = "";
      while (currentRoom.getTotalInventorySize() - currentRoom.numItemsCannotMove() > 0) {
        if (inventory.get(i).isOpen()) {
          ArrayList<Item> items = inventory.get(i).getInventory();
          while (inventory.get(i).getInventory().size() > 0) {
            Item remove = items.remove(0);
            playerInventory.addItem(remove);
            taken += ", " + remove.getName();
          }
          inventory.get(i).setInventory(items);
        }
        Item remove = currentRoom.removeItem(inventory.get(i).getName());
        if (remove != null) {
          playerInventory.addItem(remove);
          taken += ", " + remove.getName();
        } else
          i++;
      }
      System.out.println(taken.length() == 0 ? "There are no items to take." : "You took: " + taken.replaceFirst(", ", ""));
    } else if (nonNull(command.getSecondWord()) == null) {
      System.out.println(command.getSecondWord() + " is not a vaild item");
    } else {
      if (currentRoom.contains(command.getSecondWord()) == null)
        playerInventory.addItem(nonNull(command.getSecondWord()).removeItem(command.getSecondWord()));
      else
        playerInventory.addItem(currentRoom.removeItem(command.getSecondWord()));
      System.out.println("You took the " + playerInventory.contains(command.getSecondWord()));
    }
  }

  private void dropItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Drop what?");
      return;
    }
    if (playerInventory.contains(command.getSecondWord()) == null) {
      System.out.println("You do not have a " + command.getSecondWord());
      return;
    }

    Item item = playerInventory.removeItem(command.getSecondWord());
    currentRoom.addItem(item);
    System.out.println("You dropped the " + item.getName());
    if (command.getSecondWord().equalsIgnoreCase("Cheese")) {
      playerInventory.addItem(currentRoom.contains("Mouse").contains("Note from Mouse"));
      System.out.println("The mice take the cheese and retreat, leaving behind a note which you pick up.");
      System.out.println("The letter reads as follows -- 'Sorting things by alphabetical order makes organizing easy'");
      currentRoom.removeItem("Cheese");
    }
  }

  private void bowling() {
    if (playerInventory.contains("bowling ball") == null) {
      System.out.println("You need a bowling ball, try to take one.");
      return;
    }
    if (currentRoom.contains("bowling pins") == null) {
      System.out.println("You need bowling pins to bowl.");
      return;
    }
    currentRoom.addItem(playerInventory.removeItem("bowling ball"));
    if ((int) (Math.random() * 2) + 1 == 2) { // ! change chance for testing (int) (Math.random() * 2) + 1;
      System.out.println("Strike!!");
      System.out.println("You hear a lock click upstairs. ");
      Item strikeKey = new Key("strikeKey", "Key", 1);
      playerInventory.addItem(strikeKey);
      currentRoom.removeItem("bowling pins");
    } else {
      System.out.println("Not a strike.");
      System.out.println("Take the bowling ball to try again.");
    }
  }

  private void placeItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Place what?");
      return;
    }

    String item = command.getSecondWord();
    if (nonNull(item) == null) {
      System.out.println(item + " is not a valid object.");
      return;
    }


    if (playerInventory.contains(command.getSecondWord()) == null) {
      System.out.println("You do not have a " + command.getSecondWord());
      return;
    }

    if (!command.hasThirdWord()) {
      System.out.println("You must specify where to put the " + item + "");
      return;
    }
    String area = command.getThirdWord();
    if (nonNull(area) != null) {
      if (nonNull(area).isOpen()) {
        nonNull(area).addItem(playerInventory.removeItem(item));
        System.out.println("The " + command.getSecondWord() + " has been added to the " + area + ".");
      } else
        System.out.println("You must open the " + nonNull(area).getName().toLowerCase() + " first.");
    } else
      System.out.println(area + " is not a valid placement");
  }

  private void consumeItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println(command.getCommandWord().equals("eat") ? "Eat what?" : "Drink what?");
      return;
    }
    if (playerInventory.contains(command.getSecondWord()) == null) {
      System.out.println("You do not have a " + command.getSecondWord());
      return;
    }
    String item = command.getSecondWord();
    if (nonNull(item) == null) {
      System.out.println(item + " is not a valid object.");
      return;
    }

    if (!playerInventory.contains(command.getSecondWord()).isEdible() && command.getCommandWord().equals("eat")) {
      System.out.println("You cannot eat the " + playerInventory.contains(command.getSecondWord()));
      return;
    }

    if (!playerInventory.contains(command.getSecondWord()).isDrinkable() && command.getCommandWord().equals("drink")) {
      System.out.println("You cannot drink the " + playerInventory.contains(command.getSecondWord()));
      return;
    }
    item = playerInventory.removeItem(command.getSecondWord()).getName();
    System.out.println(command.getCommandWord().equals("eat") ? "You ate the " + item : "You drank the " + item);
    if (item.equalsIgnoreCase("Rotten milk")) {
      Item PantryKey = new Key("PantryKey", "Key from rotten milk", 1);
      playerInventory.addItem(PantryKey);
      System.out.println("A key has been added to your inventory");
    }
  }

  private void printInventory() {
    System.out.println("Player Inventory :");
    playerInventory.displayInventory();
  }

  /**
   * Print out some help information. Here we print some stupid, cryptic message and a list of the
   * command words.
   */
  private void printHelp() {
    System.out.println("These are all the valid commands you can use.");
    System.out.println("\nYour command words are:");
    parser.showCommands();
    System.out.println("To unlock a room, enter [unlock (and the direction of the room you are unlocking)]");
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room, otherwise print an error
   * message.
   */
  private void goRoom(Command command) {
    String direction = command.getCommandWord();
    // convert n, s, w, e here.
    String temp = CommandWords.dirConversions.get(command.getCommandWord());
    if (temp != null)
      direction = temp;

    // Try to leave current room.
    Room nextRoom = currentRoom.nextRoom(direction, currentRoom);

    if (nextRoom == null)
      System.out.println("There is no door!");
    else if (nextRoom == currentRoom)
      System.out.println("You cannot go there, it is locked.");
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
      currentRoom.displayInventory();
    }
  }

  private void save() {
    Save save = new Save(roomMap, itemMap, currentRoom, playerInventory, timeElapsed);
    try {
      FileOutputStream fileOut = new FileOutputStream(GAME_SAVE_LOCATION);
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(save);
      out.close();
      fileOut.close();
      welcome.slowtext("Saving current game", 9);
      welcome.slowtext("....................", 9);
      welcome.slowtext(".....", 500);
      welcome.slowtext("..", 750);
      System.out.println("\n\nGame saved!");
    } catch (NotSerializableException ex) {
      System.out.println("NotSerializableException - A class that needs to be saved does not implement Serializable!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
