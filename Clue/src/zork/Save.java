package zork;

import java.util.HashMap;

public class Save implements java.io.Serializable {
    public HashMap<String, Room> roomMap;
    public HashMap<String, Item> itemMap;

    private Room currentRoom;
    private Inventory playerInventory;

    public Save(HashMap<String, Room> roomMap, HashMap<String, Item> itemMap, Room currentRoom, Inventory playerInventory) {
        this.roomMap = roomMap;
        this.itemMap = itemMap;
        this.currentRoom = currentRoom;
        this.playerInventory = playerInventory;
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
}
