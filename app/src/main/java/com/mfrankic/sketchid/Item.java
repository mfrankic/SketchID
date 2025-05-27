package com.mfrankic.sketchid;

/**
 * Represents a generic item, which can be an image.
 * This class encapsulates properties common to different types of items handled in the application.
 */
public class Item {

  private final String path;
  private final String source;
  private final Type type;
  private int id;
  private String name;

  /**
   * Constructs a new Item.
   *
   * @param id     The unique identifier of the item.
   * @param name   The name of the item.
   * @param source The source of the item (e.g., URL, local path).
   * @param path   The local storage path of the item.
   * @param type   The {@link Type} of the item.
   */
  public Item(int id, String name, String source, String path, Type type) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.path = path;
    this.type = type;
  }

  /**
   * Gets the unique identifier of the item.
   *
   * @return The item ID.
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the item.
   *
   * @param id The new item ID.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Gets the name of the item.
   *
   * @return The item name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the item.
   *
   * @param name The new item name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the local storage path of the item.
   *
   * @return The item path.
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the source of the item.
   *
   * @return The item source.
   */
  public String getSource() {
    return source;
  }

  /**
   * Gets the type of the item.
   *
   * @return The item {@link Type}.
   */
  public Type getType() {
    return type;
  }

  /**
   * Defines the type of an {@link Item}.
   */
  public enum Type {
    /**
     * Represents an image item.
     */
    IMAGE
  }

}
