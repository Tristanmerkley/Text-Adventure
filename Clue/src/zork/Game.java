package zork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game {

  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();
  public static HashMap<String, Item> itemMap = new HashMap<String, Item>();

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
      currentRoom = roomMap.get("GrandEntry"); // ! spawn room
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
      long weight = ((JSONObject) itemObj).get("weight") != null ? (Long) ((JSONObject) itemObj).get("weight")
          : Long.MAX_VALUE;
      long holdingWeight = ((JSONObject) itemObj).get("holdingWeight") != null
          ? (Long) ((JSONObject) itemObj).get("holdingWeight")
          : 0; // ! how much an item can hold in its inventory
      boolean isLocked = ((JSONObject) itemObj).get("isLocked") != null
          ? (Boolean) ((JSONObject) itemObj).get("isLocked")
          : false;
      boolean isOpenable = ((JSONObject) itemObj).get("isOpenable") != null
          ? (Boolean) ((JSONObject) itemObj).get("isOpenable")
          : false;

      String itemDescription = (String) ((JSONObject) itemObj).get("description");
      String startingRoom = ((JSONObject) itemObj).get("startingroom") != null
          ? (String) ((JSONObject) itemObj).get("startingroom")
          : null;
      String startingItem = ((JSONObject) itemObj).get("startingitem") != null
          ? (String) ((JSONObject) itemObj).get("startingitem")
          : null;

      item.setDescription(itemDescription);
      item.setName(itemName);
      item.setLocked(isLocked);
      item.setOpenable(isOpenable);
      item.setWeight((int) weight);
      item.createInventory((int) holdingWeight);

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
    System.out.println();
    System.out.println("Welcome to _____.");
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

    String commandWord = command.getCommandWord();
    if (commandWord.equals("help"))
      printHelp();
    else if (commandWord.equals("go"))
      goRoom(command);
    else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        return true; // signal that we want to quit
    } else if (commandWord.equals("eat")) {
      System.out.println("Do you really think you should be eating at a time like this?");
    } else if (commandWord.equals("inventory")) {
      printInventory();
    }
    return false;
  }

  // implementations of user commands:

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
    System.out.println();
    System.out.println("Your command words are:");
    parser.showCommands();
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */
  private void goRoom(Command command) {
    if (!command.hasSecondWord()) {
      // if there is no second word, we don't know where to go...
      System.out.println("Go where?");
      return;
    }

    String direction = command.getSecondWord();

    // Try to leave current room.
    Room nextRoom = currentRoom.nextRoom(direction);

    if (nextRoom == null)
      System.out.println("There is no door!");
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
    }
  }
}
