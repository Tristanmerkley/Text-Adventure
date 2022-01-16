package zork;

import java.util.ArrayList;

public class Room implements java.io.Serializable {

  private String roomName;
  private String description;
  private ArrayList<Exit> exits;
  private Inventory inventory;

  /**
   * @return all of the rooms exits
   */
  public ArrayList<Exit> getExits() {
    return exits;
  }

  /**
   * @param exits
   * @return sets all of the rooms exits
   */
  public void setExits(ArrayList<Exit> exits) {
    this.exits = exits;
  }

  /**
   * Create a room described "description". Initially, it has no exits. "description" is something
   * like "a kitchen" or "an open court yard".
   */
  public Room(String description) {
    inventory = new Inventory(Long.MAX_VALUE);
    this.description = description;
    exits = new ArrayList<Exit>();
  }

  public Room() {
    inventory = new Inventory(Long.MAX_VALUE);
    roomName = "DEFAULT ROOM";
    description = "DEFAULT DESCRIPTION";
    exits = new ArrayList<Exit>();
  }

  /**
   * adds an exit to a room
   * @param exit
   * @throws Exception
   */
  public void addExit(Exit exit) throws Exception {
    exits.add(exit);
  }

  /**
   * Return the description of the room (the one that was defined in the constructor).
   */
  public String shortDescription() {
    return "Room: " + roomName + "\n\n" + description;
  }

  /**
   * Return a long description of this room, on the form: You are in the kitchen. Exits: roomName(north west)
   */
  public String longDescription() {
    return "Room: " + roomName + "\n\n" + description + "\n" + exitString();
  }

  /**
   * Return a string describing the room's exits, for example "Exits: roomName (north west) ".
   */
  private String exitString() {
    String returnString = "Exits:\n";
    for (Exit exit : exits) {
      returnString += Game.roomMap.get(exit.getAdjacentRoom()).getRoomName() + " (" + exit.getDirection() + ")\n";
    }
    return returnString;
  }

  /**
   * Return the room that is reached if we go from this room in direction "direction". If there is no
   * room in that direction, return null. if the room is locked, return the currentRoom.
   * @return
   */
  public Room nextRoom(String direction, Room currentRoom) {
    for (Exit exit : exits) {
      if (exit.getDirection().equalsIgnoreCase(direction)) {
        String adjacentRoom = exit.getAdjacentRoom();
        if (exit.isLocked())
          return currentRoom;
        return Game.roomMap.get(adjacentRoom);
      }
    }
    return null;
  }

  /**
   * gets the room's name
   * @return
   */
  public String getRoomName() {
    return roomName;
  }

  /**
   * set a name for the room
   *
   * @param roomName
   */
  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  /**
   * set a description for the room
   *
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * adds an item to the room inventory and returns the item that was added
   *
   * @param item
   */
  public boolean addItem(Item item) {
    return inventory.addItem(item);
  }

  public Item contains(String itemName) {
    return inventory.contains(itemName);
  }

  /**
   * removes an item from the room inventory and returns the removed item
   *
   * @param itemName
   */
  public Item removeItem(String itemName) {
    return inventory.removeItem(itemName);
  }

  /**
   * displays the room inventory and the inventory of items in the room
   */
  public void displayInventory() {
    if (inventory.getInventory().size() > 0)
      System.out.println("Contains:");
    for (Item i : inventory.getInventory()) {
      System.out.print(i.getName() + " - " + i.getDescription());
      if (i.isOpen() && i.getInventory().size() > 0) {
        String res = "";
        System.out.print("\n        " + "Contains: ");// ! formatting imcomplete
        ArrayList<Item> items = i.getInventory();
        for (Item j : items) {
          res = ", " + j.getName();
        }
        System.out.println(res.replaceFirst(", ", ""));
      }
      System.out.println();
    }
  }

  /**
   * returns the room inventory
   * @return
   */
  public ArrayList<Item> getInventory() {
    return inventory.getInventory();
  }

  /**
   * returns the number of items you cannot move in the room inventory
   */
  public int numItemsCannotMove() {
    return inventory.numItemsCannotMove();
  }

  /**
   * gets the total amount of items in a room inventory as an integer, including items in other items
   * @return
   */
  public int getTotalInventorySize() {
    int total = 0;
    for (Item i : getInventory()) {
      total++;
      if (i.isOpen()) {
        ArrayList<Item> itemInventory = new ArrayList<Item>(i.getInventory());
        while (itemInventory.size() > 0) {
          itemInventory.remove(0);
          total++;
        }
      }
    }
    return total;
  }

}
