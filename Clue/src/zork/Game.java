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
  public static final Double MAX_ALLOWED_TIME = 1440.0; // amount of time before losing in seconds
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
      playerInventory = new Inventory(50); // ! player max inventory weight
    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
  }

  /**
   * converts item's and their properties from a json file type, to an item class
   *
   * @param fileName
   * @throws Exception
   */
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

  /**
   * converts room's and their properties from a json file type to a rooms class
   *
   * @param fileName
   * @throws Exception
   */
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
      String roomHint = (String) ((JSONObject) roomObj).get("hint") != null ? (String) ((JSONObject) roomObj).get("hint") : "I shouldn't waste any time here, I should check the rest of the house.";
      room.setDescription(roomDescription);
      room.setRoomName(roomName);
      room.setRoomHint(roomHint);

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
    printWelcome(); // Stopped for testing
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
          finished = true;
          System.out.println("You hear a loud thumping sound as the house goes completely dark. You can't see anything and the sound gets louder, you hear a door open up and it sounds like someone else is in the room with you.");
          System.out.println("You hear someone say \"Time's up\" and you feel something hard knock you on the head.");
          System.out.println("");
          System.out.println("You have failed to complete the game. Play again to have another chance at winning.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (currentRoom.getRoomName().equalsIgnoreCase("the end")) {
        finished = true;
        System.out.println("\u001B[31m" + "Congratulations! You have successfully escaped the house!");
      }
      if (currentRoom.getRoomName().equalsIgnoreCase("bunker")) {
        finished = true;
        System.out.println("\u001B[31m" + "The door closes behind you and locks automatically. You are stuck in the bunker with no food and water and eventually die of dehydration.");
        System.out.println("\u001B[31m" + "YOU LOSE!!!");
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
    welcome.title();
    welcome.slowtext("Do you wanna play a game? \nThe game is simple,\n", 80);
    welcome.slowtext("You have 24 hours", 60);
    welcome.slowtext(" (24 real-time minutes) ", 5);
    welcome.slowtext("to escape the house and pass the gate, or else you will be killed.\n", 50);
    welcome.slowtext("Pay attention to detail, as everything is there for a reason.\n", 50);
    welcome.slowtext("Don't stray from the path, or else you may not make it back. \nFollow the clues to escape in time, make sure not to waste your time.\n", 60);
    welcome.slowtext("Your time has already started, better get MOVING!\n", 50);
    welcome.slowtext("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n\n", 10);
    System.out.println("Type 'help' if you require assistance.");
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
      if (answer.equals("yes") || answer.equals("y"))
        return true;
      System.out.println("Quit aborted");
      return false;
    } else if (commandWord.equalsIgnoreCase("eat") || commandWord.equalsIgnoreCase("drink")) {
      consumeItem(command);
    } else if (commandWord.equalsIgnoreCase("inventory")) {
      printInventory();
    } else if (commandWord.equalsIgnoreCase("take")) {
      takeItem(command);
    } else if (commandWord.equalsIgnoreCase("drop")) {
      dropItem(command);
    } else if (commandWord.equalsIgnoreCase("place")) {
      placeItem(command);
    } else if (commandWord.equalsIgnoreCase("look")) {
      lookAround();
    } else if (commandWord.equalsIgnoreCase("bowl")) {
      bowling();
    } else if (commandWord.equalsIgnoreCase("open") || commandWord.equalsIgnoreCase("dig")) {
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
    } else if (commandWord.equalsIgnoreCase("hint")) {
      printHint();
    }
    return false;
  }

  // implementations of user commands:

  private void printHint() {
    String hint = currentRoom.getRoomHint();
    System.out.println(hint);
  }


  /**
   * Prints out the total run time of the current game
   */
  private void printTime() {
    endTime = new Date().getTime();
    System.out.printf("%5.2f%n", timeElapsed + (endTime - startTime) / 1000.0);
  }

  /**
   * The load function is required to load a previously saved game file
   */

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
      timeElapsed = save.getTimeElapsed();
    }
  }

  /**
   * This method is required for reading the book on locking picking
   *
   * @param command - the command parameter is the command that the user types after being processed
   *        by the parser
   */
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
      if (playerInventory.contains("Book") != null) {
        isUseable = true;
        System.out.println("You've read the book. You can now unlock doors with basic locks using a knife.");
        playerInventory.removeItem("book");
      } else
        System.out.println("You don't have a book to read.");
    }
  }

  /**
   * unlocks a door using the direction that is inputed. Special cases include, safe, desk, closet.
   *
   * @param command
   */
  private void unlockDoor(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Which direction is the door you want to unlock?");
      return;
    }
    if (command.getSecondWord().equalsIgnoreCase("safe")) {
      if (currentRoom.contains("safe") != null) { // special case for pin code items
        if (currentRoom.contains("safe").isLocked())
          unlockSafe(command);
        else
          System.out.println("The safe is already unlocked!");
      } else
        System.out.println("There is no safe in the current room!");
      return;
    }
    if (command.getSecondWord().equalsIgnoreCase("desk") && currentRoom.contains("desk").isLocked()) { // special case for unlocking a desk
      if (currentRoom.contains("desk") != null) {
        if (currentRoom.contains("desk").isLocked())
          unlockDesk(command);
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
        if (i.getAdjacentRoom().equals("Library") || i.getAdjacentRoom().equals("Maze1")) { // special case for pickable doors
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
        if (i.getAdjacentRoom().equals("Closet3")) { // special case for password entry door(s)
          System.out.println("What is my favourite colour?");
          if (in == null)
            in = new Scanner(System.in);
          System.out.print("> ");
          String code = in.nextLine();
          if (code.equalsIgnoreCase("purple")) { // checks if it is the correct password
            i.setLocked(false);
            System.out.println("The closet is now unlocked");
          } else
            System.out.println("That is not the right code");
          return;
        }
        for (Item j : playerInventory.getInventory()) {
          if (i.getKeyId().equals(j.getKeyId())) { // checks if it is the correct key
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

  /**
   * unlocks desk if piggy bank has peculiar coin in its inventory
   *
   * @param command
   */
  private void unlockDesk(Command command) {
    if (currentRoom.contains("Piggy bank").contains("Peculiar coin") == null) {
      System.out.println("Is there a coin in the piggy bank yet?");
    } else {
      currentRoom.contains("Desk").setLocked(false);
      System.out.println("The desk has been unlocked");
    }
  }

  /**
   * unlocks safe if the correct code is input
   *
   * @param command
   */
  private void unlockSafe(Command command) {
    System.out.println("What is the 4-digit code?");
    if (in == null)
      in = new Scanner(System.in);
    System.out.print("> ");
    String code = in.nextLine();
    if (code.equals("6351")) { // checks if the pin is correct
      currentRoom.contains("safe").setLocked(false);
      System.out.println("The safe is now unlocked");
    } else
      System.out.println("That is not the right code");
  }

  /**
   * The openObject funtion is required to be able to open up an object so that you can take them with
   * the take command
   *
   * @param command
   */
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

    if (item.equals("couch")) {
      System.out.println("A key fell out of the couch and onto the ground.");
      Item BackyardKey = new Key("BackyardKey", "Key from Couch", 1);
      BackyardKey.setDescription("Another key with an engraving of a flower on it, does it open more doors?");
      currentRoom.addItem(BackyardKey);
      return;
    }

    if (item.equals("safe") && !currentRoom.contains("safe").isLocked()) {
      System.out.println("A key fell out of the safe and onto the ground.");
      Item BalconyKey = new Key("BalconyKey", "Key from safe", 1);
      BalconyKey.setDescription("A key with a cloud on it.");
      currentRoom.addItem(BalconyKey);
      return;
    }

    if (object.isOpenable()) { // checks if you can open the object
      if (object.isLocked()) { // checks if the item is locked
        System.out.println("You must first unlock: " + object.getName());
        return;
      }
      if (item.equalsIgnoreCase("Main floor map") || item.equalsIgnoreCase("Upstairs left map") || item.equalsIgnoreCase("Upstairs right map")) { // special case for opening maps
        printMap(item);
        return;
      }
      if (item.equalsIgnoreCase("Hole")) { // special case for opening a hole
        if (playerInventory.contains("Shovel") != null) {
          nonNull(item).setOpen(true);
          System.out.println("Opened " + object.getName() + "\n\nContains:");
          object.displayInventory();
          Item shedKey = new Key("shedKey", "Key from hole", 1);
          playerInventory.addItem(shedKey);
          shedKey.setDescription("I should keep exploring to figure out where this leads.");
          System.out.println("A key has been added to your inventory");
          return;
        } else {
          System.out.println("You need to use the shovel to open the hole.");
          return;
        }
      }
      if (item.equalsIgnoreCase("floorboard")) { // special case for the floorboard
        // currentRoom.addItem(currentRoom.contains("Floorboard").contains("Key from attic"));
        // currentRoom.getInventory().remove(currentRoom.contains("Floorboard"));
        System.out.println("You pushed the floor board aside, revealing a key.");
        Item frontDoorKey = new Key("FrontDoorKey", "Key from floorboard", 1);
        frontDoorKey.setDescription("A shiny key that has a design of an ornate door on it.");
        currentRoom.addItem(frontDoorKey);
        return;
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

  /**
   * figures out which map is supose to be shown then prints out the corresponding map
   *
   * @param map
   */

  private void printMap(String map) {
    if (map.equalsIgnoreCase("Main floor map")) {
      try {
        Scanner in = new Scanner(new File("src/zork/floor0.map"));
        while (in.hasNextLine()) {
          System.out.println(in.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else if (map.equalsIgnoreCase("Upstairs left map")) {
      try {
        Scanner in = new Scanner(new File("src/zork/floor1secondhalf.map"));
        while (in.hasNextLine()) {
          System.out.println(in.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      try {
        Scanner in = new Scanner(new File("src/zork/floor1firsthalf.map"));
        while (in.hasNextLine()) {
          System.out.println(in.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * displays description and inventory of current room
   */
  private void lookAround() {
    System.out.println(currentRoom.longDescription());
    currentRoom.displayInventory();
  }

  /**
   * takes a specified item from the current room, or an item in the current room
   *
   * @param command
   */
  private void takeItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Take what?");
      return;
    }
    if (currentRoom.getTotalInventorySize() - currentRoom.numItemsCannotMove() <= 0) {
      System.out.println("There are no items to take.");
      return;
    }


    String word = command.getSecondWord();
    if (currentRoom.contains("Shoe") != null && (word.equals("all") || word.equals("shoe"))) { // special case for the shoe item
      System.out.println("A peculiar coin fell out of the shoe and it has been added to your inventory");
      Item coin = new Item(1, "Peculiar Coin", false, 0);
      coin.setDescription("This is a weird looking coin, I should find somewhere to keep this safe.");
      coin.setAlternateName("coin");
      playerInventory.addItem(coin);
      return;
    }

    if (command.getSecondWord().equals("all")) { // implements use of the take "all" command
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

      if (!command.getSecondWord().equals("all") && playerInventory.getCurrentWeight() + currentRoom.contains(command.getSecondWord()).getWeight() > playerInventory.getMaxWeight()) {
        System.out.println("You do not have enough space in your inventory to take this.");
        return;
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

  /**
   * drops item into the current room. has a special case for when item is dropped into the same room
   * the mouse is in.
   *
   * @param command
   */
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
    if (command.getSecondWord().equalsIgnoreCase("Cheese")) { // special case for pantry room with mice
      playerInventory.addItem(currentRoom.contains("Mouse").contains("Note from Mouse"));
      System.out.println("The mouse took the cheese and retreated, leaving behind a note which you pick up.");
      System.out.println("The letter reads as follows -- 'You can never go wrong with alphabetical order'");
      currentRoom.removeItem("Cheese");
    }
  }

  /**
   * if player inventory contains bowling ball, the bowling ball will be dropped and there is a 50%
   * chance of getting a strike. if a strike occurs, the player will get a key that unlocks basement
   * door that leads to grand entry room.
   */
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
    if ((int) (Math.random() * 2) + 1 == 2) { // 50% chance of strike
      System.out.println("Strike!!");
      System.out.println("You hear a lock click upstairs. ");
      Item strikeKey = new Key("strikeKey", "Key", 1);
      strikeKey.setDescription("Maybe this will help me go upstairs...");
      playerInventory.addItem(strikeKey);
      currentRoom.removeItem("bowling pins");
    } else {
      System.out.println("Not a strike.");
      System.out.println("Take the bowling ball to try again.");
    }
  }

  /**
   * puts an item from the player inventory into another item in the current room
   *
   * @param command
   */
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

  /**
   * will either eat or drink an item depending on its attributes. special case for rotten milk, a key
   * will be added to player inventory once milk has been drunk.
   *
   * @param command
   */
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
      PantryKey.setDescription("I wonder what other doors are locked on this floor.");
      playerInventory.addItem(PantryKey);
      System.out.println("A key has been added to your inventory");
    }
  }

  /**
   * prints the player inventory
   */
  private void printInventory() {
    System.out.println("Player Inventory (" + (int) (((double) playerInventory.getCurrentWeight() / playerInventory.getMaxWeight()) * 100) + "%) :");
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
      if (!currentRoom.getRoomName().equals("The End")) {
        System.out.println(currentRoom.longDescription());
        currentRoom.displayInventory();
      }
    }
  }

  /**
   * saves the current game state to come back to later on.
   */
  private void save() {
    Save save = new Save(roomMap, itemMap, currentRoom, playerInventory, timeElapsed);
    try {
      FileOutputStream fileOut = new FileOutputStream(GAME_SAVE_LOCATION); // specifies the save location
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
