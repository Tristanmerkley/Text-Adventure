package zork;

public class Key extends Item {
  private String keyId;

  public Key(String keyId, String keyName, int weight) {
    super(weight, keyName, false, 10);
    this.keyId = keyId;
  }

  /**
   * returns the keyId
   */
  public String getKeyId() {
    return keyId;
  }
}
