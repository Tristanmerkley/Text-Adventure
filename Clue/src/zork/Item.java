package zork;

import java.util.ArrayList;

public class Item extends OpenableObject {
  private int weight;
  private String name;
  private String alternateName;
  private boolean isOpenable;
  private boolean isEdible;
  private boolean isDrinkable;
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

  /**
   * displays inventory of an item
   */
  public void displayInventory() {
    inventory.displayInventory();
  }

  /**
   * get's inventory of an item
   */
  public ArrayList<Item> getInventory() {
    return inventory.getInventory();
  }

  /**
   * returns name
   */
  public String toString() {
    return name;
  }

  /**
   * returns the weight of an object
   */
  public int getWeight() {
    return weight;
  }

  /**
   * sets weight of an item
   *
   * @param weight
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }

  /**
   * returns the item name
   */
  public String getName() {
    return name;
  }

  /**
   * returns an items alternate name
   */
  public String getAlternateName() {
    return alternateName;
  }

  /**
   * set name for an item
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * set alternate name for an item
   */
  public void setAlternateName(String alternateName) {
    this.alternateName = alternateName;
  }

  /**
   * returns if an item is openable or not
   */
  public boolean isOpenable() {
    return isOpenable;
  }

  /**
   * can set to true or false
   *
   * @param isOpenable
   */
  public void setOpenable(boolean isOpenable) {
    this.isOpenable = isOpenable;
  }

  /**
   * returns true if item is edible, false if it isn't
   *
   * @return
   */
  public boolean isEdible() {
    return isEdible;
  }

  /**
   * set's isEdible to true or false
   *
   * @param isEdible
   */
  public void setEdible(boolean isEdible) {
    this.isEdible = isEdible;
  }

  /**
   * returns true if item is drinkable, false if it isn't
   *
   * @return
   */
  public boolean isDrinkable() {
    return isDrinkable;
  }

  /**
   * set's isDrinable to true or false
   *
   * @param isDrinkable
   */
  public void setDrinkable(boolean isDrinkable) {
    this.isDrinkable = isDrinkable;
  }

  /**
   * set description for an item
   *
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * creates inventory for an item with a max holding weight
   *
   * @param holdingWeight
   */
  public void createInventory(long holdingWeight) {
    inventory = new Inventory(holdingWeight);
  }

  /**
   * add item to another item's inventory
   *
   * @param item
   */
  public void addItem(Item item) {
    inventory.addItem(item);
  }

  /**
   * returns item description
   */
  public String getDescription() {
    return description;
  }

  /**
   * 
   * @param itemName
   * @return the Item with the itemName in the item inventory
   */
  public Item contains(String itemName) {
    return inventory.contains(itemName);
  }

  /**
   * sets inventory
   * 
   * @param items
   */
  public void setInventory(ArrayList<Item> items) {
    inventory.setInventory(items);
  }

  /**
   * removes item from an items inventory and returns the item that has been removed
   *
   * @param item
   */
  public Item removeItem(String item) {
    return inventory.removeItem(item);
  }

}
