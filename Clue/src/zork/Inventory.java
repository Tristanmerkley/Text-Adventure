package zork;

import java.util.ArrayList;

public class Inventory implements java.io.Serializable {
  private ArrayList<Item> items;
  private long maxWeight;
  private int currentWeight;

  public Inventory(long maxWeight) {
    this.items = new ArrayList<Item>();
    this.maxWeight = maxWeight;
    this.currentWeight = 0;
  }

  /**
   * 
   * @return the max weight
   */
  public long getMaxWeight() {
    return maxWeight;
  }

  /**
   * @return the current weight
   */
  public int getCurrentWeight() {
    return currentWeight;
  }

  /**
   * adds an item to the inventory if it will not excide the max inventory weight
   * 
   * @param item
   * @return
   */
  public boolean addItem(Item item) {
    if (item.getWeight() + currentWeight <= maxWeight) {
      currentWeight += item.getWeight();
      return items.add(item);
    } else {
      System.out.println("There is no room to add the item.");
      return false;
    }
  }

  /**
   * checks if the inventory contains a item using its name
   * 
   * @param itemName
   * @return the item
   */
  public Item contains(String itemName) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).getName().toLowerCase().equals(itemName.toLowerCase()) || (items.get(i).getAlternateName() != null && items.get(i).getAlternateName().toLowerCase().equals(itemName.toLowerCase())))
        return items.get(i);
    }
    return null;
  }

  /**
   * removes an item from the inventory
   * 
   * @param itemName
   * @return the removed item
   */
  public Item removeItem(String itemName) {
    Item item = contains(itemName);
    if (item.getWeight() == Integer.MAX_VALUE) {
      System.out.println("You cannot take the " + item.getName());
      return null;
    }
    currentWeight -= item.getWeight();
    items.remove(item);
    return item;
  }

  /**
   * display's the entire inventory in the format: Name - Description
   */
  public void displayInventory() {
    for (Item i : items) {
      System.out.println(i + " - " + i.getDescription());
    }
  }

  /**
   * @return the inventory as a list
   */
  public ArrayList<Item> getInventory() {
    return items;
  }

  /**
   * @return the number of items which cannot be moved
   */
  public int numItemsCannotMove() {
    int count = 0;
    for (Item i : items) {
      if (i.getWeight() == Integer.MAX_VALUE)
        count++;
    }
    return count;
  }

  /**
   * sets the entire inventory to the parameter
   * 
   * @param items
   */
  public void setInventory(ArrayList<Item> items) {
    this.items = items;
    for (Item i : items) {
      currentWeight += i.getWeight();
    }
  }
}
