package zork;

import java.util.HashMap;

public class Save implements java.io.Serializable {
    public HashMap<String, Room> roomMap;
    public HashMap<String, Item> itemMap;

    private Room currentRoom;
    private Inventory playerInventory;
    private double timeElapsed;

    public Save(HashMap<String, Room> roomMap, HashMap<String, Item> itemMap, Room currentRoom, Inventory playerInventory, double timeElapsed) {
        this.roomMap = roomMap;
        this.itemMap = itemMap;
        this.currentRoom = currentRoom;
        this.playerInventory = playerInventory;
        this.timeElapsed = timeElapsed;
    }

    public HashMap<String, Room> getRoomMap() {
        return roomMap;
    }

    public HashMap<String, Item> getItemMap() {
        return itemMap;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    public double getTimeElapsed() {
        return timeElapsed;
    }
}
