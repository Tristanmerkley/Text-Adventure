package zork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game {

  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();
  public static HashMap<String, Item> itemMap = new HashMap<String, Item>();

  private Scanner input;
  private Parser parser;
  private Room currentRoom;
  private Inventory playerInventory;

  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");
      currentRoom = roomMap.get("BowlingAlley"); // ! spawn room
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
      String itemId = (String) ((JSONObject) itemObj).get("id");
      long weight = ((JSONObject) itemObj).get("weight") != null ? (Long) ((JSONObject) itemObj).get("weight") : Integer.MAX_VALUE;
      long holdingWeight; // ! how much an item can hold in its inventory
      boolean isLocked = ((JSONObject) itemObj).get("isLocked") != null ? (Boolean) ((JSONObject) itemObj).get("isLocked") : false;
      boolean isOpenable = ((JSONObject) itemObj).get("isOpenable") != null ? (Boolean) ((JSONObject) itemObj).get("isOpenable") : false;
      if (!isOpenable)
        holdingWeight = 0;
      else
        holdingWeight = ((JSONObject) itemObj).get("holdingWeight") != null ? (Long) ((JSONObject) itemObj).get("holdingWeight") : Long.MAX_VALUE;

      String itemDescription = (String) ((JSONObject) itemObj).get("description");
      String startingRoom = ((JSONObject) itemObj).get("startingroom") != null ? (String) ((JSONObject) itemObj).get("startingroom") : null;
      String startingItem = ((JSONObject) itemObj).get("startingitem") != null ? (String) ((JSONObject) itemObj).get("startingitem") : null;

      item.setDescription(itemDescription);
      item.setName(itemName);
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
        command = parser.getCommand();
        finished = processCommand(command);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    System.out.println("Thank you for playing.  Good bye.");
  }

  /**
   * Print out the opening message for the player.
   */
  private void printWelcome() {
    //!welcome.title(); //disabled for testing
    System.out.println();
    System.out.println("Welcome to _____."); // TODO need to pick game name
    System.out.println("Type 'help' if you need help.");
    System.out.println("You have 24 hours to escape the house and pass the gate, or else you will be killed.");
    System.out.println("Every ten decisions made, an hour will pass.");
    System.out.println("Pay attention to detail, everything is there for a reason. ");
    System.out.println("Don't stray from the path, follow the clues to escape in time");
    System.out.println("Your time starts now… What are you waiting for?");
    System.out.println();
    System.out.println(currentRoom.longDescription());
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends
   * the game, true is returned, otherwise false is returned.
   */
  private boolean processCommand(Command command) {
    if (command.isUnknown()) {
      System.out.println("I don't know what you mean...");
      return false;
    }

    String commandWord = command.getCommandWord().toLowerCase();
    if (commandWord.equals("help"))
      printHelp();
    else if (commandWord.equals("south") || commandWord.equals("north") || commandWord.equals("east") || commandWord.equals("west"))
      goRoom(command);
    else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        return true; // signal that we want to quit
    } else if (commandWord.equals("eat") || commandWord.equals("drink") || commandWord.equals("consume")) {
      consumeItem();
    } else if (commandWord.equals("inventory")) {
      printInventory();
    } else if (commandWord.equals("take")) {
      takeItem(command);
    } else if (commandWord.equals("drop") || commandWord.equals("throw")) {
      dropItem(command);
    } else if (commandWord.equals("give")) { // give cheese to mouse
      System.out.println(""); // say something about note mouse dropped
      placeItem(command);
    } else if (commandWord.equals("look")) {
      lookAround();
      // gives room definition and items in the room
      //printInventory();
      /*} else if (commandWord.equals("put")) { // put item into another items inventory
      placeItem();
      */ }
    return false;
  }

  // implementations of user commands:

  private void lookAround() {
    System.out.println(currentRoom.longDescription());
    currentRoom.displayInventory();
  }

  private void takeItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Take what?");
      return;
    }
    if (command.getSecondWord().equals("all")) {
      ArrayList<Item> inventory = currentRoom.getInventory();
      String taken = "";
      while (inventory.size() > 0) {
        Item remove = currentRoom.removeItem(inventory.get(0).getName());
        playerInventory.addItem(remove);
        taken += remove.getName() + ", ";
      }
      System.out.println("You took: " + taken.substring(0, taken.length() - 2));
    } else if (currentRoom.contains(command.getSecondWord()) != null) {
      playerInventory.addItem(currentRoom.removeItem(command.getSecondWord()));
      System.out.println("You took: " + command.getSecondWord());
    } else
      System.out.println(command.getSecondWord() + " is not a vaild item");
    return;
  }

  private void dropItem(Command command) {
    if (playerInventory.contains(command.getSecondWord()) == null) {
      System.out.println("You do not have a " + command.getSecondWord());
    }
    if (!command.hasSecondWord() && command.getCommandWord().equals("drop")) {
      System.out.println("Drop what?");
      return;
    } else if (!command.hasSecondWord()) {
      System.out.println("Throw what?");
      return;
    }
    if (currentRoom.contains("Cheese") != null) {
      playerInventory.addItem(currentRoom.contains("PantryMouse").contains("MouseNote"));
    }
    Item item = playerInventory.removeItem(command.getSecondWord());
    currentRoom.addItem(item);
    System.out.println("You dropped " + item.getName());
    if (item.getName().equalsIgnoreCase("bowling ball"))
      bowling(); // TODO finish bowling
  }

  private void bowling() {
    if (currentRoom.contains("bowling pins") != null) {
      System.out.println("I need bowling pins to bowl.");
      return;
    }
    if ((int) (Math.random()) == 0) {
      System.out.println("Strike!!");
      Item strikeKey = new Key("strikeKey", "Key", 1);
      playerInventory.addItem(strikeKey);
      currentRoom.removeItem("bowling pins");
    } else {
      System.out.println("Try again");
    }
  }

  private void placeItem(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Place what?");
      return;
    }
    String item = command.getSecondWord();
    System.out.println("Where do you want to put " + item + "?");
    String area = input.nextLine();
    while (currentRoom.contains(area) == null) {
      System.out.println(area + " is not a valid object");
      System.out.println("Where do you want to put " + item + "?");
      area = input.nextLine();
    }
    currentRoom.contains(area).addItem(playerInventory.removeItem(item));
  }

  private void consumeItem() {

  }

  private void printInventory() {
    System.out.println("Player Inventory :");
    playerInventory.displayInventory();
  }

  /**
   * Print out some help information. Here we print some stupid, cryptic message
   * and a list of the command words.
   */
  private void printHelp() {
    System.out.println("You are lost. You are alone. You wander");
    System.out.println("around at Monash Uni, Peninsula Campus.");
    System.out.println("\nYour command words are:");
    parser.showCommands();
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */
  private void goRoom(Command command) {
    String direction = command.getCommandWord();

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
}
