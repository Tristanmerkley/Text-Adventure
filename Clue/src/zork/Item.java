package zork;

public class Item extends OpenableObject {
  private int weight;
  private String name;
  private boolean isOpenable;
  private Inventory inventory;

  public Item(int weight, String name, boolean isOpenable, int maxWeight ) {
    this.weight = weight;
    this.name = name;
    this.isOpenable = isOpenable;
    
    if(isOpenable)
    inventory = new Inventory(maxWeight);
  }

  public void displayInventory(){
    System.out.println(inventory);
  }

  public String toString(){
    //need to create a toString method to display the inventory
    return null;
  }


  public void open() {
    if (!isOpenable)
      System.out.println("The " + name + " cannot be opened.");

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

}
