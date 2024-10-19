package com.mfrankic.sketchid;

public class Item {
    public enum Type {IMAGE}

    public Type type;
    public int id;
    public String name;
    public int resource;

    public Item(Type type, int id, String name, int resource) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.resource = resource;
    }
}
