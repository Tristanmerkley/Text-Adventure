package zork;

import java.io.File;
import java.io.FileNotFoundException;
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

  private Parser parser;
  private Room currentRoom;
  private Inventory playerInventory;
  private boolean isUseable = false;

  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("src/zork/data/rooms.json");
      initItems("src/zork/data/items.json");
      currentRoom = roomMap.get("Kitchen"); // ! spawn room
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
      boolean isConsumable = ((JSONObject) itemObj).get("isConsumable") != null ? (Boolean) ((JSONObject) itemObj).get("isConsumable") : false;
      if (!isOpenable)
        holdingWeight = 0;
      else
        holdingWeight = ((JSONObject) itemObj).get("holdingWeight") != null ? (Long) ((JSONObject) itemObj).get("holdingWeight") : Long.MAX_VALUE;
      String itemDescription = (String) ((JSONObject) itemObj).get("description");
      String startingRoom = ((JSONObject) itemObj).get("startingroom") != null ? (String) ((JSONObject) itemObj).get("startingroom") : null;
      String startingItem = ((JSONObject) itemObj).get("startingitem") != null ? (String) ((JSONObject) itemObj).get("startingitem") : null;

      item.setConsumable(isConsumable);
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
      if (currentRoom.getRoomName().equalsIgnoreCase("the end")){
        finished = true;
      }
    }
    System.out.println("Congratulations! You have successfully escaped the house!");
    System.out.println("Thank you for playing.  Good bye.");
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
        return true; // signal that we want to quit
    } else if (commandWord.equalsIgnoreCase("consume")) {
      consumeItem(command);
    } else if (commandWord.equalsIgnoreCase("inventory")) {
      printInventory();
    } else if (commandWord.equalsIgnoreCase("take")) {
      takeItem(command);
    } else if (commandWord.equalsIgnoreCase("drop")) {
      dropItem(command);
    } else if (commandWord.equalsIgnoreCase("give") || commandWord.equalsIgnoreCase("place")) { // give cheese to mouse
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
    }
    return false;
  }

  // implementations of user commands:

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
    }
  }

  private void unlockDoor(Command command) {
    if (!command.hasSecondWord()) {
      System.out.println("Which direction is the door you want to unlock?");
      return;
    }
    if (command.getSecondWord().equalsIgnoreCase("safe")) {
      unlockSafe(command);
      return;
    }
    if (command.getSecondWord().equalsIgnoreCase("desk")) {
      unlockDesk(command); // TODO
      return;
    }
    if (!command.isDirection(command.getSecondWord())) {
      System.out.println(command.getSecondWord() + " is not a vaild direction");
      return;
    }
    for (Exit i : currentRoom.getExits()) {
      if (i.getDirection().equalsIgnoreCase(command.getSecondWord())) {
        if (!i.isLocked()) {
          System.out.println(Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " is already unlocked.");
          return;
        }
        if (i.getAdjacentRoom().equals("Library") || i.getAdjacentRoom().equals("Maze1")) {
          if ((playerInventory.contains("Knife") != null) && isUseable) {
            i.setLocked(false);
            System.out.println("Unlocked the " + Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " door.");
          } else if (!isUseable) {
            System.out.println("Read a special book to be able to pick basic locks.");
          } else if (playerInventory.contains("Knife") == null) {
            System.out.println("Find a knife to be able to pick basic locks.");
          } else {
            System.out.println("You need to have read a special book and find a knife before you can unlock this door.");
          }
          return;
        }
        for (Item j : playerInventory.getInventory()) {
          if (j.getKeyId().equals(i.getKeyId())) {
            i.setLocked(false);
            System.out.println("Unlocked the " + Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " door.");
            return;
          } else {
            System.out.println("You do not have the correct key for the " + Game.roomMap.get(i.getAdjacentRoom()).getRoomName() + " door.");
            return;
          }
        }
      }
    }
    System.out.println("There is no door there!");
  }

  private void unlockDesk(Command command) {
    if (currentRoom.contains("Piggy Bank").contains("peculiar coin").getName().equals("peculair coin")) {
      System.out.println("Is there a coin in the piggy bank yet?");
    } else {
      currentRoom.contains("Desk").setLocked(false);
      System.out.println("The desk has been unlocked");
    }
  }


  private void unlockSafe(Command command) {
    Scanner in = new Scanner(System.in);
    System.out.println("What is the 4-digit code?");
    String code = in.nextLine();
    if (code.equals("6531")) {
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
    if (object.isOpenable()) {
      if (object.isLocked()) {
        System.out.println("You must first unlock: " + object.getName());
        return;
      }
      if (item.equalsIgnoreCase("Main floor map") || item.equalsIgnoreCase("Upstairs left map") || item.equalsIgnoreCase("Upstairs right map")) {
        printMap(item);
        return;
      }
      nonNull(item).setOpen(true);
      System.out.println("Opened " + object.getName() + "\n\nContains:");
      object.displayInventory();
    } else
      System.out.println("You cannot open " + object.getName());
  }

  private Item nonNull(String item) {
    if (currentRoom.contains(item) != null)
      return currentRoom.contains(item);
    if (playerInventory.contains(item) != null)
      return playerInventory.contains(item);
    for (Item i : currentRoom.getInventory()) {
      if (i.contains(item) != null) {
        // itemName = i;
        return i;// .contains(item);
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
    if (currentRoom.getInventory().size() - currentRoom.numItemsCannotMove() <= 0) {
      System.out.println("There are no items to take.");
      return;
    }

    String shoe = command.getSecondWord();
    if (shoe.equals("shoe")) {
      System.out.println("A peculiar coin fell out of the shoe and it has been added to your inventory");
      Item coin = new Item(1, "Peculiar Coin", false, 0);
      playerInventory.addItem(coin);
    }

    if (command.getSecondWord().equals("all")) {
      ArrayList<Item> inventory = currentRoom.getInventory();
      int i = 0;
      String taken = "";
      while (inventory.size() - currentRoom.numItemsCannotMove() > 0) {
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
        } else {
          i++;
        }
      }
      System.out.println(taken.length() == 0 ? "There are no items to take." : "You took: " + taken.replaceFirst(", ", ""));
    } else if (nonNull(command.getSecondWord()) == null) {
      System.out.println(command.getSecondWord() + " is not a vaild item");
    } else {
      if (currentRoom.contains(command.getSecondWord()) == null)
        playerInventory.addItem(nonNull(command.getSecondWord()).removeItem(command.getSecondWord()));
      else
        playerInventory.addItem(currentRoom.removeItem(command.getSecondWord()));
      System.out.println("You took: " + command.getSecondWord());
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
    System.out.println("You dropped " + item.getName());
    if (command.getSecondWord().equalsIgnoreCase("Cheese")) {
      playerInventory.addItem(currentRoom.contains("PantryMouse").contains("MouseNote"));
      System.out.println("The mice take the cheese and retreat, leaving behind a note which you pick up.");
      System.out.println("The letter reads as"); // TODO incomplete
    }
  }

  private void bowling() {
    if (playerInventory.contains("bowling ball") == null) {
      System.out.println("You need a bowling ball, try to take one.");
      return;
    }
    if (currentRoom.contains("bowling pins") == null) {
      System.out.println("You bowling pins to bowl.");
      return;
    }
    currentRoom.addItem(playerInventory.removeItem("bowling ball"));
    if ((int) (Math.random() * 1) == 0) { // ! change * for chance for testing
      System.out.println("Strike!!");
      Item strikeKey = new Key("strikeKey", "Key", 1);
      playerInventory.addItem(strikeKey);
      currentRoom.removeItem("bowling pins");
    } else {
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
      System.out.println("Where do you want to put " + item + "?");
      return;
    }
    String area = command.getThirdWord();
    if (playerInventory.contains(area) != null) {
      ((Inventory) playerInventory).contains(area).addItem(playerInventory.removeItem(item));
    } else if (!currentRoom.contains(area).isOpen()) {
      System.out.println("You must open the " + area + " first.");
    } else if (currentRoom.contains(area) != null) {
      currentRoom.contains(area).addItem(playerInventory.removeItem(item));
      System.out.println("The " + command.getSecondWord() + " has been added to the " + area + ".");
    }
  }

  private void consumeItem(Command command) {
    if (!command.hasSecondWord()) {
      if (command.getCommandWord().equals("consume")) {
        System.out.println("Consume what?");
        return;
      }
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
    if (command.getSecondWord().equals("rotten milk")) {
      playerInventory.removeItem(command.getSecondWord());
      Item PantryKey = new Key("PantryKey", "Key from rotten milk", 1);
      playerInventory.addItem(PantryKey);
      System.out.println("A key has been added to your inventory");
    } else if (!command.getSecondWord().equals("cheese") && !command.getSecondWord().equals("coffee")) {
      if (command.getCommandWord().equals("consume"))
        System.out.println("You cannot consume the " + command.getSecondWord());
    } else {
      playerInventory.removeItem(command.getSecondWord());
      if (command.getCommandWord().equals("consume"))
        System.out.println("You consumed the " + command.getSecondWord());
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
}
