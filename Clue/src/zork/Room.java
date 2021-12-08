package zork;

import java.util.ArrayList;

public class Room {

  private String roomName;
  private String description;
  private ArrayList<Exit> exits;
  private Inventory inventory;

  public ArrayList<Exit> getExits() {
    return exits;
  }

  public void setExits(ArrayList<Exit> exits) {
    this.exits = exits;
  }

  /**
   * Create a room described "description". Initially, it has no exits.
   * "description" is something like "a kitchen" or "an open court yard".
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

  public void addExit(Exit exit) throws Exception {
    exits.add(exit);
  }

  /**
   * Return the description of the room (the one that was defined in the
   * constructor).
   */
  public String shortDescription() {
    return "Room: " + roomName + "\n\n" + description;
  }

  /**
   * Return a long description of this room, on the form: You are in the kitchen.
   * Exits: north west
   */
  public String longDescription() {

    return "Room: " + roomName + "\n\n" + description + "\n" + exitString();
  }

  /**
   * Return a string describing the room's exits, for example "Exits: north west
   * ".
   */
  private String exitString() {
    String returnString = "Exits:\n";
    for (Exit exit : exits) {
      returnString += exit.getAdjacentRoom() + " (" + exit.getDirection() + ")\n";
    }

    return returnString;
  }

  /**
   * Return the room that is reached if we go from this room in direction
   * "direction". If there is no room in that direction, return null.
   */
  public Room nextRoom(String direction, Room currentRoom) {
    try {
      boolean non = false;
      for (Exit exit : exits) {

        if (exit.getDirection().equalsIgnoreCase(direction) && !exit.isLocked()) {
          String adjacentRoom = exit.getAdjacentRoom();
          non = true;
          return Game.roomMap.get(adjacentRoom);
        }
      }
      if (non)
        return currentRoom;
    } catch (IllegalArgumentException ex) {
      System.out.println(direction + " is not a valid direction.");
      return null;
    }
    System.out.println(direction + " is not a valid direction.");
    return null;
  }

  /*
   * private int getDirectionIndex(String direction) { int dirIndex = 0; for
   * (String dir : directions) { if (dir.equals(direction)) return dirIndex; else
   * dirIndex++; }
   *
   * throw new IllegalArgumentException("Invalid Direction"); }
   */
  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription(int i) {
    return inventory.getDescription(i);
  }

  public boolean addItem(Item item) {
    return inventory.addItem(item);
  }

  public Item contains(String itemName) {
    return inventory.contains(itemName);
  }

  public Item removeItem(String itemName) {
    return inventory.removeItem(itemName);
  }

  public void displayInventory() {
    for (Item i : inventory.getInventory()) {
      System.out.println(i.getName() + ": " + i.getDescription());
    }
  }

  public ArrayList<Item> getInventory() {
    return inventory.getInventory();
  }
}
