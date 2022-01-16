package zork;

import java.util.HashMap;

public class Save implements java.io.Serializable {
    public HashMap<String, Room> roomMap;
    public HashMap<String, Item> itemMap;

    private Room currentRoom;
    private Inventory playerInventory;
    private double timeElapsed;

    /**
     * constructs a new Save object with the following parameters
     * @param roomMap
     * @param itemMap
     * @param currentRoom
     * @param playerInventory
     * @param timeElapsed
     */
    public Save(HashMap<String, Room> roomMap, HashMap<String, Item> itemMap, Room currentRoom, Inventory playerInventory, double timeElapsed) {
        this.roomMap = roomMap;
        this.itemMap = itemMap;
        this.currentRoom = currentRoom;
        this.playerInventory = playerInventory;
        this.timeElapsed = timeElapsed;
    }

    /**
     * @return the saved roomMap
     */
    public HashMap<String, Room> getRoomMap() {
        return roomMap;
    }

    /**
     * @return the saved itemMap
     */
    public HashMap<String, Item> getItemMap() {
        return itemMap;
    }

    /**
     * @return the saved currentRoom
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * @return the saved player inventory
     */
    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    /**
     * @return timeElapsed when saved
     */
    public double getTimeElapsed() {
        return timeElapsed;
    }
}
