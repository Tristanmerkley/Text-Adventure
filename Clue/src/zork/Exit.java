package zork;

/**
 * Exit
 */
public class Exit extends OpenableObject {
  private String direction;
  private String adjacentRoom;

  public Exit(String direction, String adjacentRoom, boolean isLocked, String keyId) {
    super(isLocked, keyId);
    this.direction = direction;
    this.adjacentRoom = adjacentRoom;
  }

  public Exit(String direction, String adjacentRoom, boolean isLocked, String keyId, Boolean isOpen) {
    super(isLocked, keyId, isOpen);
    this.direction = direction;
    this.adjacentRoom = adjacentRoom;
  }

  public Exit(String direction, String adjacentRoom) {
    this.direction = direction;
    this.adjacentRoom = adjacentRoom;
  }

  /**
   * gets the direction of the exit object
   * @return
   */
  public String getDirection() {
    return direction;
  }

  /**
   * sets the direction of the exit object
   * @param direction
   */
  public void setDirection(String direction) {
    this.direction = direction;
  }

  /**
   * gets the name of the room the exit leads to
   * @return
   */
  public String getAdjacentRoom() {
    return adjacentRoom;
  }

  /**
   * sets the name of the room the exit leads to
   * @param adjacentRoom
   */
  public void setAdjacentRoom(String adjacentRoom) {
    this.adjacentRoom = adjacentRoom;
  }

  /**
   * sets the exit object as locked or unlocked
   */
  public void setLocked(boolean isLocked) {
    super.setLocked(isLocked);
  }
}
