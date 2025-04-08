package com.mfrankic.sketchid;

public class Item {

  private final String path;
  private final String source;
  private final Type type;
  private int id;
  private String name;

  public Item(int id, String name, String source, String path, Type type) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.path = path;
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

  public String getPath() {
    return path;
  }

  public String getSource() {
    return source;
  }

  public Type getType() {
    return type;
  }

  public enum Type {IMAGE}

}
