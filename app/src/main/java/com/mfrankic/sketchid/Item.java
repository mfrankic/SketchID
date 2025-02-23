package com.mfrankic.sketchid;

public class Item {

  private final int resource;
  private final Type type;
  private int id;
  private String name;

  public Item(int id, String name, int resource, Type type) {
    this.id = id;
    this.name = name;
    this.resource = resource;
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getResource() {
    return resource;
  }

  public Type getType() {
    return type;
  }

  public enum Type {IMAGE}

}
