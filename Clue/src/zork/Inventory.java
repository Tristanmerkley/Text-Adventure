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

  public long getMaxWeight() {
    return maxWeight;
  }

  public int getCurrentWeight() {
    return currentWeight;
  }

  public boolean addItem(Item item) {
    if (item.getWeight() + currentWeight <= maxWeight)
      return items.add(item);
    else {
      System.out.println("There is no room to add the item.");
      return false;
    }
  }

  public Item contains(String itemName) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).getName().toLowerCase().equals(itemName.toLowerCase()) || (items.get(i).getAlternateName() != null && items.get(i).getAlternateName().toLowerCase().equals(itemName.toLowerCase())))
        return items.get(i);
    }
    return null;
  }

  public Item removeItem(String itemName) {
    Item item = contains(itemName);
    if (item.getWeight() == Integer.MAX_VALUE) {
      System.out.println("You cannot take the " + item.getName());
      return null;
    }
    items.remove(item);
    return item;
  }

  public void displayInventory() {
    for (Item i : items) {
      System.out.println(i + " - " + i.getDescription());
    }
  }

  public ArrayList<Item> getInventory() {
    return items;
  }

  public String getDescription(int i) {
    return items.get(i).getDescription();
  }

  public int numItemsCannotMove() {
    int count = 0;
    for (Item i : items) {
      if (i.getWeight() == Integer.MAX_VALUE)
        count++;
    }
    return count;
  }

  public void setInventory(ArrayList<Item> items) {
    this.items = items;
  }
}
