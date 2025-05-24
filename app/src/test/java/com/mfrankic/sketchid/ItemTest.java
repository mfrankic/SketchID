package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Item class.
 * Tests constructor, getters, setters, and enum functionality.
 */
public class ItemTest {

  private Item item;

  @Before
  public void setup() {
    item = new Item(1, "Test Image", "default", "/path/to/image.png", Item.Type.IMAGE);
  }

  @Test
  public void testConstructorWithValidParameters() {
    int id = 123;
    String name = "Sample Image";
    String source = "custom";
    String path = "/custom/path/image.jpg";
    Item.Type type = Item.Type.IMAGE;

    Item testItem = new Item(id, name, source, path, type);

    assertEquals("ID should match", id, testItem.getId());
    assertEquals("Name should match", name, testItem.getName());
    assertEquals("Source should match", source, testItem.getSource());
    assertEquals("Path should match", path, testItem.getPath());
    assertEquals("Type should match", type, testItem.getType());
  }

  @Test
  public void testGettersReturnCorrectValues() {
    assertEquals("ID should be accessible", 1, item.getId());
    assertEquals("Name should be accessible", "Test Image", item.getName());
    assertEquals("Source should be accessible", "default", item.getSource());
    assertEquals("Path should be accessible", "/path/to/image.png", item.getPath());
    assertEquals("Type should be accessible", Item.Type.IMAGE, item.getType());
  }

  @Test
  public void testIdSetterAndGetter() {
    int newId = 456;
    item.setId(newId);
    assertEquals("ID should be updated", newId, item.getId());
  }

  @Test
  public void testNameSetterAndGetter() {
    String newName = "Updated Image Name";
    item.setName(newName);
    assertEquals("Name should be updated", newName, item.getName());
  }

  @Test
  public void testZeroId() {
    Item zeroItem = new Item(0, "Zero ID", "source", "path", Item.Type.IMAGE);
    assertEquals("Zero ID should be handled", 0, zeroItem.getId());
  }

  @Test
  public void testNegativeId() {
    Item negativeItem = new Item(-1, "Negative ID", "source", "path", Item.Type.IMAGE);
    assertEquals("Negative ID should be handled", -1, negativeItem.getId());
  }

  @Test
  public void testMaximumIntId() {
    Item maxItem = new Item(Integer.MAX_VALUE, "Max ID", "source", "path", Item.Type.IMAGE);
    assertEquals("Maximum integer ID should be handled", Integer.MAX_VALUE, maxItem.getId());
  }

  @Test
  public void testMinimumIntId() {
    Item minItem = new Item(Integer.MIN_VALUE, "Min ID", "source", "path", Item.Type.IMAGE);
    assertEquals("Minimum integer ID should be handled", Integer.MIN_VALUE, minItem.getId());
  }

  @Test
  public void testNullName() {
    Item nullNameItem = new Item(1, null, "source", "path", Item.Type.IMAGE);
    assertNull("Null name should be handled", nullNameItem.getName());
  }

  @Test
  public void testEmptyName() {
    Item emptyNameItem = new Item(1, "", "source", "path", Item.Type.IMAGE);
    assertEquals("Empty name should be handled", "", emptyNameItem.getName());
  }

  @Test
  public void testNullSource() {
    Item nullSourceItem = new Item(1, "name", null, "path", Item.Type.IMAGE);
    assertNull("Null source should be handled", nullSourceItem.getSource());
  }

  @Test
  public void testEmptySource() {
    Item emptySourceItem = new Item(1, "name", "", "path", Item.Type.IMAGE);
    assertEquals("Empty source should be handled", "", emptySourceItem.getSource());
  }

  @Test
  public void testNullPath() {
    Item nullPathItem = new Item(1, "name", "source", null, Item.Type.IMAGE);
    assertNull("Null path should be handled", nullPathItem.getPath());
  }

  @Test
  public void testEmptyPath() {
    Item emptyPathItem = new Item(1, "name", "source", "", Item.Type.IMAGE);
    assertEquals("Empty path should be handled", "", emptyPathItem.getPath());
  }

  @Test
  public void testTypeFinalField() {
    // Type is final, so it should not be modifiable after construction
    assertEquals("Type should remain IMAGE", Item.Type.IMAGE, item.getType());

    // Create another item with the same type to verify it works consistently
    Item anotherItem = new Item(2, "Another", "source", "path", Item.Type.IMAGE);
    assertEquals("Type should be IMAGE for new item", Item.Type.IMAGE, anotherItem.getType());
  }

  @Test
  public void testSourceFinalField() {
    // Source is final, so it should not be modifiable after construction
    assertEquals("Source should remain 'default'", "default", item.getSource());

    Item customItem = new Item(2, "Custom", "custom", "path", Item.Type.IMAGE);
    assertEquals("Source should be 'custom' for custom item", "custom", customItem.getSource());
  }

  @Test
  public void testPathFinalField() {
    // Path is final, so it should not be modifiable after construction
    assertEquals("Path should remain original", "/path/to/image.png", item.getPath());

    Item newPathItem = new Item(2, "New", "source", "/new/path.jpg", Item.Type.IMAGE);
    assertEquals(
        "Path should be '/new/path.jpg' for new item",
        "/new/path.jpg",
        newPathItem.getPath()
    );
  }

  @Test
  public void testTypeEnum() {
    // Test that the Type enum exists and has IMAGE value
    assertNotNull("Type.IMAGE should exist", Item.Type.IMAGE);
    assertEquals("Type enum should have IMAGE value", "IMAGE", Item.Type.IMAGE.name());
  }

  @Test
  public void testTypeEnumValues() {
    // Test that Type enum has the expected values
    Item.Type[] types = Item.Type.values();
    assertNotNull("Type values should not be null", types);
    assertTrue("Type should have at least one value", types.length > 0);
    assertEquals("First type should be IMAGE", Item.Type.IMAGE, types[0]);
  }

  @Test
  public void testTypeEnumValueOf() {
    // Test that valueOf works for Type enum
    Item.Type type = Item.Type.valueOf("IMAGE");
    assertEquals("valueOf should return IMAGE", Item.Type.IMAGE, type);
  }

  @Test
  public void testLongStringValues() {
    String longName
        = "This is a very long image name that might be used to test string handling capabilities";
    String longSource
        = "very_long_source_identifier_that_could_potentially_cause_issues_with_string_processing";
    String longPath
        = "/very/long/path/that/might/include/many/directories/and/subdirectories/image.png";

    Item longItem = new Item(1, longName, longSource, longPath, Item.Type.IMAGE);

    assertEquals("Long name should be handled", longName, longItem.getName());
    assertEquals("Long source should be handled", longSource, longItem.getSource());
    assertEquals("Long path should be handled", longPath, longItem.getPath());
  }

  @Test
  public void testSpecialCharacters() {
    String specialName = "Image@#$%^&*()";
    String specialSource = "source!@#$%";
    String specialPath = "/path/with spaces/and-dashes/image_file.png";

    Item specialItem = new Item(1, specialName, specialSource, specialPath, Item.Type.IMAGE);

    assertEquals(
        "Special characters in name should be handled",
        specialName,
        specialItem.getName()
    );
    assertEquals(
        "Special characters in source should be handled",
        specialSource,
        specialItem.getSource()
    );
    assertEquals(
        "Special characters in path should be handled",
        specialPath,
        specialItem.getPath()
    );
  }

  @Test
  public void testUnicodeCharacters() {
    String unicodeName = "图像测试";
    String unicodeSource = "مصدر";
    String unicodePath = "/путь/к/файлу.png";

    Item unicodeItem = new Item(1, unicodeName, unicodeSource, unicodePath, Item.Type.IMAGE);

    assertEquals(
        "Unicode characters in name should be handled",
        unicodeName,
        unicodeItem.getName()
    );
    assertEquals(
        "Unicode characters in source should be handled",
        unicodeSource,
        unicodeItem.getSource()
    );
    assertEquals(
        "Unicode characters in path should be handled",
        unicodePath,
        unicodeItem.getPath()
    );
  }

  @Test
  public void testModifyingNameAfterConstruction() {
    String originalName = item.getName();
    String newName = "Modified Name";

    item.setName(newName);

    assertNotEquals("Name should be different from original", originalName, item.getName());
    assertEquals("Name should be the new value", newName, item.getName());
  }

  @Test
  public void testMultipleNameChanges() {
    item.setName("First Change");
    assertEquals("First name change should work", "First Change", item.getName());

    item.setName("Second Change");
    assertEquals("Second name change should work", "Second Change", item.getName());

    item.setName(null);
    assertNull("Setting name to null should work", item.getName());

    item.setName("Final Change");
    assertEquals("Final name change should work", "Final Change", item.getName());
  }

  @Test
  public void testIdBoundaryValues() {
    // Test common boundary values for ID
    int[] testIds = {0, 1, -1, 100, 999, 1000, 9999, 10000};

    for (int testId : testIds) {
      Item testItem = new Item(testId, "Test", "source", "path", Item.Type.IMAGE);
      assertEquals("ID " + testId + " should be handled correctly", testId, testItem.getId());
    }
  }
} 
