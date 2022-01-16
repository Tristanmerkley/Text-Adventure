package zork;

public class OpenableObject implements java.io.Serializable {
  private Boolean isLocked;
  private String keyId;
  private Boolean isOpen;

  public OpenableObject() {
    this.isLocked = false;
    this.keyId = null;
    this.isOpen = false;
  }

  public OpenableObject(boolean isLocked, String keyId, Boolean isOpen) {
    this.isLocked = isLocked;
    this.keyId = keyId;
    this.isOpen = isOpen;
  }

  public OpenableObject(boolean isLocked, String keyId) {
    this.isLocked = isLocked;
    this.keyId = keyId;
    this.isOpen = false;
  }

  /**
   * returns if an item isLocked or not
   */
  public boolean isLocked() {
    return isLocked;
  }

  /**
   * can set isLocked to true or false for an item
   */
  public void setLocked(boolean isLocked) {
    this.isLocked = isLocked;
  }

  /**
   * returns the keyId for an item
   */
  public String getKeyId() {
    return keyId;
  }

  /**
   * returns if an item is open or not
   */
  public boolean isOpen() {
    return isOpen;
  }

  /**
   * set isOpen to true or false for an item
   *
   * @param isOpen
   */
  public void setOpen(boolean isOpen) {
    this.isOpen = isOpen;
  }
}
