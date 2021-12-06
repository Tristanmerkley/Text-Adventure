package zork;

public class Item extends OpenableObject {
  private int weight;
  private String name;
  private boolean isOpenable;
  private Inventory inventory;
  private String description;

  public Item() {
    inventory = new Inventory(0);
  }

  public Item(int weight, String name, boolean isOpenable, int maxWeight) {
    this.weight = weight;
    this.name = name;
    this.isOpenable = isOpenable;

    if (isOpenable)
      inventory = new Inventory(maxWeight);
  }

  public void displayInventory() {
    System.out.println(inventory);
  }

  public String getInventory() {
    return toString();
  }

  public String toString() {
    return name + ":" + description;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOpenable() {
    return isOpenable;
  }

  public void setOpenable(boolean isOpenable) {
    this.isOpenable = isOpenable;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void createInventory(long holdingWeight) {
    inventory = new Inventory(holdingWeight);
  }

  public void addItem(Item item) {
    inventory.addItem(item);
  }

}
