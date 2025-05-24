package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Image class.
 * Tests constructor, getters, setters, equals, and hashCode functionality.
 */
public class ImageTest {

  private Image image;

  @Before
  public void setup() {
    image = new Image("Test Image", "default", "/path/to/image.png");
  }

  @Test
  public void testConstructorWithValidParameters() {
    String name = "Sample Image";
    String source = "custom";
    String path = "/custom/path/image.jpg";

    Image testImage = new Image(name, source, path);

    assertEquals("Name should match constructor parameter", name, testImage.getName());
    assertEquals("Source should match constructor parameter", source, testImage.getSource());
    assertEquals("Path should match constructor parameter", path, testImage.getPath());
    assertEquals("ID should be initialized to 0", 0, testImage.getId());
  }

  @Test
  public void testConstructorWithNullParameters() {
    Image nullImage = new Image(null, null, null);

    assertNull("Null name should be handled", nullImage.getName());
    assertNull("Null source should be handled", nullImage.getSource());
    assertNull("Null path should be handled", nullImage.getPath());
    assertEquals("ID should be initialized to 0", 0, nullImage.getId());
  }

  @Test
  public void testConstructorWithEmptyStrings() {
    Image emptyImage = new Image("", "", "");

    assertEquals("Empty name should be handled", "", emptyImage.getName());
    assertEquals("Empty source should be handled", "", emptyImage.getSource());
    assertEquals("Empty path should be handled", "", emptyImage.getPath());
  }

  @Test
  public void testGettersReturnCorrectValues() {
    assertEquals("Name should be accessible", "Test Image", image.getName());
    assertEquals("Source should be accessible", "default", image.getSource());
    assertEquals("Path should be accessible", "/path/to/image.png", image.getPath());
    assertEquals("ID should be accessible", 0, image.getId());
  }

  @Test
  public void testIdSetterAndGetter() {
    int newId = 123;
    image.setId(newId);
    assertEquals("ID should be updated", newId, image.getId());
  }

  @Test
  public void testNameSetterAndGetter() {
    String newName = "Updated Image Name";
    image.setName(newName);
    assertEquals("Name should be updated", newName, image.getName());
  }

  @Test
  public void testSourceSetterAndGetter() {
    String newSource = "updated_source";
    image.setSource(newSource);
    assertEquals("Source should be updated", newSource, image.getSource());
  }

  @Test
  public void testPathSetterAndGetter() {
    String newPath = "/updated/path/image.png";
    image.setPath(newPath);
    assertEquals("Path should be updated", newPath, image.getPath());
  }

  @Test
  public void testSettersWithNullValues() {
    image.setName(null);
    image.setSource(null);
    image.setPath(null);

    assertNull("Name should be null after setting to null", image.getName());
    assertNull("Source should be null after setting to null", image.getSource());
    assertNull("Path should be null after setting to null", image.getPath());
  }

  @Test
  public void testSettersWithEmptyStrings() {
    image.setName("");
    image.setSource("");
    image.setPath("");

    assertEquals("Name should be empty after setting to empty", "", image.getName());
    assertEquals("Source should be empty after setting to empty", "", image.getSource());
    assertEquals("Path should be empty after setting to empty", "", image.getPath());
  }

  @Test
  public void testEqualsWithSameId() {
    Image image1 = new Image("Image 1", "source1", "path1");
    Image image2 = new Image("Image 2", "source2", "path2");

    image1.setId(100);
    image2.setId(100);

    assertEquals("Images with same ID should be equal", image1, image2);
  }

  @Test
  public void testEqualsWithDifferentId() {
    Image image1 = new Image("Same Name", "same_source", "same_path");
    Image image2 = new Image("Same Name", "same_source", "same_path");

    image1.setId(100);
    image2.setId(200);

    assertNotEquals("Images with different IDs should not be equal", image1, image2);
  }

  @Test
  public void testEqualsWithSameObject() {
    assertEquals("Image should be equal to itself", image, image);
  }

  @Test
  public void testEqualsWithNull() {
    assertNotEquals("Image should not be equal to null", null, image);
  }

  @Test
  public void testEqualsWithDifferentClass() {
    String notAnImage = "Not an image";
    assertNotEquals("Image should not be equal to different class", image, notAnImage);
  }

  @Test
  public void testHashCodeConsistency() {
    int initialHashCode = image.hashCode();

    // Hash code should remain the same if object doesn't change
    assertEquals("Hash code should be consistent", initialHashCode, image.hashCode());
    assertEquals(
        "Hash code should be consistent on multiple calls",
        initialHashCode,
        image.hashCode()
    );
  }

  @Test
  public void testHashCodeEquality() {
    Image image1 = new Image("Image 1", "source1", "path1");
    Image image2 = new Image("Image 2", "source2", "path2");

    image1.setId(100);
    image2.setId(100);

    assertEquals(
        "Equal objects should have equal hash codes",
        image1.hashCode(),
        image2.hashCode()
    );
  }

  @Test
  public void testHashCodeBasedOnId() {
    Image zeroIdImage = new Image("Test", "source", "path");
    zeroIdImage.setId(0);
    assertEquals("Hash code should be based on ID", 0, zeroIdImage.hashCode());

    Image positiveIdImage = new Image("Test", "source", "path");
    positiveIdImage.setId(123);
    assertEquals("Hash code should be based on ID", 123, positiveIdImage.hashCode());

    Image negativeIdImage = new Image("Test", "source", "path");
    negativeIdImage.setId(-456);
    assertEquals("Hash code should be based on ID", -456, negativeIdImage.hashCode());
  }

  @Test
  public void testIdBoundaryValues() {
    int[] testIds = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 100, 999, 1000};

    for (int testId : testIds) {
      image.setId(testId);
      assertEquals("ID " + testId + " should be handled correctly", testId, image.getId());
      assertEquals("Hash code should match ID", testId, image.hashCode());
    }
  }

  @Test
  public void testLongStringValues() {
    String longName =
        "This is a very long image name that might be used to test string handling capabilities"
        + " of the Image class";
    String longSource
        =
        "very_long_source_identifier_that_could_potentially_cause_issues_with_string_processing_in_the_image_class";
    String longPath =
        "/very/long/path/that/might/include/many/directories/and/subdirectories/to/test/path"
        + "/handling/in/image/class.png";

    image.setName(longName);
    image.setSource(longSource);
    image.setPath(longPath);

    assertEquals("Long name should be handled", longName, image.getName());
    assertEquals("Long source should be handled", longSource, image.getSource());
    assertEquals("Long path should be handled", longPath, image.getPath());
  }

  @Test
  public void testSpecialCharacters() {
    String specialName = "Image@#$%^&*()_+-=[]{}|;':\",./<>?";
    String specialSource = "source!@#$%^&*()";
    String specialPath = "/path/with spaces/and-special_chars/image@file.png";

    image.setName(specialName);
    image.setSource(specialSource);
    image.setPath(specialPath);

    assertEquals("Special characters in name should be handled", specialName, image.getName());
    assertEquals(
        "Special characters in source should be handled",
        specialSource,
        image.getSource()
    );
    assertEquals("Special characters in path should be handled", specialPath, image.getPath());
  }

  @Test
  public void testUnicodeCharacters() {
    String unicodeName = "图像测试 🖼️ Imágén";
    String unicodeSource = "مصدر 源码";
    String unicodePath = "/путь/к/файлу/图像.png";

    image.setName(unicodeName);
    image.setSource(unicodeSource);
    image.setPath(unicodePath);

    assertEquals("Unicode characters in name should be handled", unicodeName, image.getName());
    assertEquals(
        "Unicode characters in source should be handled",
        unicodeSource,
        image.getSource()
    );
    assertEquals("Unicode characters in path should be handled", unicodePath, image.getPath());
  }

  @Test
  public void testMultipleImages() {
    Image image1 = new Image("Image1", "source1", "path1");
    Image image2 = new Image("Image2", "source2", "path2");

    image1.setId(1);
    image2.setId(2);

    assertEquals("First image name should be correct", "Image1", image1.getName());
    assertEquals("Second image name should be correct", "Image2", image2.getName());
    assertNotEquals("Images should not be equal", image1, image2);
    assertNotEquals("Hash codes should be different", image1.hashCode(), image2.hashCode());
  }

  @Test
  public void testImageIndependence() {
    Image image1 = new Image("Original", "original_source", "original_path");
    Image image2 = new Image("Original", "original_source", "original_path");

    // Modify one image
    image1.setId(100);
    image1.setName("Modified");
    image1.setSource("modified_source");
    image1.setPath("modified_path");

    // Other image should remain unchanged
    assertEquals("Second image name should remain unchanged", "Original", image2.getName());
    assertEquals(
        "Second image source should remain unchanged",
        "original_source",
        image2.getSource()
    );
    assertEquals("Second image path should remain unchanged", "original_path", image2.getPath());
    assertEquals("Second image ID should remain unchanged", 0, image2.getId());
  }

  @Test
  public void testNameWithWhitespace() {
    String nameWithSpaces = "  Image With Spaces  ";
    image.setName(nameWithSpaces);
    assertEquals("Name with spaces should be preserved", nameWithSpaces, image.getName());
  }

  @Test
  public void testSourceWithWhitespace() {
    String sourceWithSpaces = "  source with spaces  ";
    image.setSource(sourceWithSpaces);
    assertEquals("Source with spaces should be preserved", sourceWithSpaces, image.getSource());
  }

  @Test
  public void testPathWithWhitespace() {
    String pathWithSpaces = "  /path/with spaces/  ";
    image.setPath(pathWithSpaces);
    assertEquals("Path with spaces should be preserved", pathWithSpaces, image.getPath());
  }

  @Test
  public void testTypicalImageScenarios() {
    // Test common real-world scenarios
    String[][] scenarios = {
        {"default_arrow", "default", String.valueOf(12345)},
        {"custom_photo.jpg", "custom", "/storage/emulated/0/Pictures/photo.jpg"},
        {"user_drawing", "user", "file:///android_asset/drawings/sketch.png"},
        {"downloaded_image", "download", "/Download/image.png"},
        {"camera_photo", "camera", "content://media/external/images/media/123"}
    };

    for (String[] scenario : scenarios) {
      Image testImage = new Image(scenario[0], scenario[1], scenario[2]);
      assertEquals(
          "Scenario name should be handled: " + scenario[0],
          scenario[0],
          testImage.getName()
      );
      assertEquals(
          "Scenario source should be handled: " + scenario[1],
          scenario[1],
          testImage.getSource()
      );
      assertEquals(
          "Scenario path should be handled: " + scenario[2],
          scenario[2],
          testImage.getPath()
      );
    }
  }

  @Test
  public void testImageFieldsAfterMultipleChanges() {
    // Test multiple modifications to verify all fields work correctly
    image.setId(1);
    image.setName("First");
    image.setSource("first_source");
    image.setPath("first_path");

    assertEquals("First ID change should work", 1, image.getId());
    assertEquals("First name change should work", "First", image.getName());
    assertEquals("First source change should work", "first_source", image.getSource());
    assertEquals("First path change should work", "first_path", image.getPath());

    image.setId(2);
    image.setName("Second");
    image.setSource("second_source");
    image.setPath("second_path");

    assertEquals("Second ID change should work", 2, image.getId());
    assertEquals("Second name change should work", "Second", image.getName());
    assertEquals("Second source change should work", "second_source", image.getSource());
    assertEquals("Second path change should work", "second_path", image.getPath());

    image.setId(0);
    image.setName(null);
    image.setSource(null);
    image.setPath(null);

    assertEquals("Setting ID to 0 should work", 0, image.getId());
    assertNull("Setting name to null should work", image.getName());
    assertNull("Setting source to null should work", image.getSource());
    assertNull("Setting path to null should work", image.getPath());
  }

  @Test
  public void testEqualsAndHashCodeContract() {
    // Test equals and hashCode contract
    Image image1 = new Image("Test", "source", "path");
    Image image2 = new Image("Different", "different", "different");
    Image image3 = new Image("Another", "another", "another");

    image1.setId(100);
    image2.setId(100);
    image3.setId(100);

    // Reflexive: x.equals(x) should return true
    assertEquals("Reflexive property should hold", image1, image1);

    // Symmetric: x.equals(y) should return true iff y.equals(x) returns true
    assertEquals("Symmetric property should hold", image1, image2);
    assertEquals("Symmetric property should hold", image2, image1);

    // Transitive: if x.equals(y) and y.equals(z), then x.equals(z)
    assertEquals("Transitive property should hold", image1, image2);
    assertEquals("Transitive property should hold", image2, image3);
    assertEquals("Transitive property should hold", image1, image3);

    // Consistent hash codes for equal objects
    assertEquals(
        "Equal objects should have equal hash codes",
        image1.hashCode(),
        image2.hashCode()
    );
    assertEquals(
        "Equal objects should have equal hash codes",
        image2.hashCode(),
        image3.hashCode()
    );
  }
} 
