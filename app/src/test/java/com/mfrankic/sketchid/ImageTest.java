package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Unit tests for Image class.
 * Tests constructor, getters, setters, equals, and hashCode functionality.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageTest {

  private Image image;

  @Before
  public void setUp() {
    image = new Image("TestImage", "test_source", "test_path");
  }

  @Test
  public void testConstructor_SetsAllFields() {
    Image testImage = new Image("Arrow", Constants.SOURCE_DEFAULT, "/path/to/arrow");

    assertEquals("Constructor should set name", "Arrow", testImage.name);
    assertEquals("Constructor should set source", Constants.SOURCE_DEFAULT, testImage.source);
    assertEquals("Constructor should set path", "/path/to/arrow", testImage.path);
    assertEquals("Constructor should initialize id to 0", 0, testImage.id);
  }

  @Test
  public void testConstructor_WithNullValues() {
    Image nullImage = new Image(null, null, null);

    assertNull("Constructor should accept null name", nullImage.name);
    assertNull("Constructor should accept null source", nullImage.source);
    assertNull("Constructor should accept null path", nullImage.path);
    assertEquals("Constructor should initialize id to 0", 0, nullImage.id);
  }

  @Test
  public void testConstructor_WithEmptyStrings() {
    Image emptyImage = new Image("", "", "");

    assertEquals("Constructor should accept empty name", "", emptyImage.name);
    assertEquals("Constructor should accept empty source", "", emptyImage.source);
    assertEquals("Constructor should accept empty path", "", emptyImage.path);
  }

  @Test
  public void testGetId_InitialValue() {
    assertEquals("Initial ID should be 0", 0, image.getId());
  }

  @Test
  public void testSetId_GetId() {
    image.setId(42);
    assertEquals("SetId should update the ID", 42, image.getId());
  }

  @Test
  public void testSetId_NegativeValue() {
    image.setId(-1);
    assertEquals("SetId should accept negative values", -1, image.getId());
  }

  @Test
  public void testSetId_MaxValue() {
    image.setId(Integer.MAX_VALUE);
    assertEquals("SetId should accept max integer value", Integer.MAX_VALUE, image.getId());
  }

  @Test
  public void testGetName() {
    assertEquals("GetName should return constructor value", "TestImage", image.getName());
  }

  @Test
  public void testSetName_GetName() {
    image.setName("NewImageName");
    assertEquals("SetName should update the name", "NewImageName", image.getName());
  }

  @Test
  public void testSetName_Null() {
    image.setName(null);
    assertNull("SetName should accept null", image.getName());
  }

  @Test
  public void testSetName_EmptyString() {
    image.setName("");
    assertEquals("SetName should accept empty string", "", image.getName());
  }

  @Test
  public void testGetSource() {
    assertEquals("GetSource should return constructor value", "test_source", image.getSource());
  }

  @Test
  public void testSetSource_GetSource() {
    image.setSource(Constants.SOURCE_CUSTOM);
    assertEquals("SetSource should update the source", Constants.SOURCE_CUSTOM, image.getSource());
  }

  @Test
  public void testSetSource_Null() {
    image.setSource(null);
    assertNull("SetSource should accept null", image.getSource());
  }

  @Test
  public void testGetPath() {
    assertEquals("GetPath should return constructor value", "test_path", image.getPath());
  }

  @Test
  public void testSetPath_GetPath() {
    image.setPath("/new/path/to/image");
    assertEquals("SetPath should update the path", "/new/path/to/image", image.getPath());
  }

  @Test
  public void testSetPath_Null() {
    image.setPath(null);
    assertNull("SetPath should accept null", image.getPath());
  }

  @Test
  public void testEquals_SameObject() {
    assertEquals("Object should equal itself", image, image);
  }

  @Test
  public void testEquals_NullObject() {
    assertNotEquals("Object should not equal null", null, image);
  }

  @Test
  public void testEquals_DifferentClass() {
    assertNotEquals("Object should not equal different class", "NotAnImage", image);
  }

  @Test
  public void testEquals_SameId() {
    Image image1 = new Image("Image1", "source1", "path1");
    Image image2 = new Image("Image2", "source2", "path2");

    image1.setId(5);
    image2.setId(5);

    assertEquals("Images with same ID should be equal", image1, image2);
  }

  @Test
  public void testEquals_DifferentId() {
    Image image1 = new Image("SameName", "SameSource", "SamePath");
    Image image2 = new Image("SameName", "SameSource", "SamePath");

    image1.setId(1);
    image2.setId(2);

    assertNotEquals("Images with different IDs should not be equal", image1, image2);
  }

  @Test
  public void testEquals_BothZeroId() {
    Image image1 = new Image("Image1", "source1", "path1");
    Image image2 = new Image("Image2", "source2", "path2");

    // Both have default ID of 0
    assertEquals("Images with same ID (0) should be equal", image1, image2);
  }

  @Test
  public void testHashCode_SameId_SameHashCode() {
    Image image1 = new Image("Image1", "source1", "path1");
    Image image2 = new Image("Image2", "source2", "path2");

    image1.setId(10);
    image2.setId(10);

    assertEquals(
        "Images with same ID should have same hash code",
        image1.hashCode(),
        image2.hashCode()
    );
  }

  @Test
  public void testHashCode_DifferentId_DifferentHashCode() {
    Image image1 = new Image("Image", "source", "path");
    Image image2 = new Image("Image", "source", "path");

    image1.setId(1);
    image2.setId(2);

    assertNotEquals(
        "Images with different IDs should have different hash codes",
        image1.hashCode(),
        image2.hashCode()
    );
  }

  @Test
  public void testHashCode_ConsistentWithEquals() {
    Image image1 = new Image("Image1", "source1", "path1");
    Image image2 = new Image("Image2", "source2", "path2");

    image1.setId(7);
    image2.setId(7);

    assertTrue(
        "Equal objects should have equal hash codes",
        image1.equals(image2) && image1.hashCode() == image2.hashCode()
    );
  }

  @Test
  public void testHashCode_EqualsId() {
    image.setId(42);
    assertEquals("Hash code should equal ID", 42, image.hashCode());
  }

  @Test
  public void testPublicFields_Accessibility() {
    // Test that all fields are public as expected by Room
    try {
      Field idField = Image.class.getField("id");
      Field nameField = Image.class.getField("name");
      Field sourceField = Image.class.getField("source");
      Field pathField = Image.class.getField("path");

      assertTrue("id field should be public", Modifier.isPublic(idField.getModifiers()));
      assertTrue("name field should be public", Modifier.isPublic(nameField.getModifiers()));
      assertTrue("source field should be public", Modifier.isPublic(sourceField.getModifiers()));
      assertTrue("path field should be public", Modifier.isPublic(pathField.getModifiers()));
    } catch (NoSuchFieldException e) {
      fail("Expected public fields should exist");
    }
  }

  @Test
  public void testDirectFieldAccess() {
    // Test direct field access (since fields are public)
    image.id = 100;
    image.name = "DirectAccess";
    image.source = "direct_source";
    image.path = "direct_path";

    assertEquals("Direct id access should work", 100, image.id);
    assertEquals("Direct name access should work", "DirectAccess", image.name);
    assertEquals("Direct source access should work", "direct_source", image.source);
    assertEquals("Direct path access should work", "direct_path", image.path);
  }

  @Test
  public void testGetterSetterConsistency() {
    // Test that getters and setters work with direct field access
    image.setId(200);
    assertEquals("Setter should match direct field access", 200, image.id);

    image.id = 300;
    assertEquals("Getter should match direct field access", 300, image.getId());

    image.setName("SetterName");
    assertEquals("Name setter should match field", "SetterName", image.name);

    image.name = "FieldName";
    assertEquals("Name getter should match field", "FieldName", image.getName());
  }

  @Test
  public void testFieldTypes() {
    // Verify field types are correct
    assertEquals("id field should be int", int.class, getFieldType("id"));
    assertEquals("name field should be String", String.class, getFieldType("name"));
    assertEquals("source field should be String", String.class, getFieldType("source"));
    assertEquals("path field should be String", String.class, getFieldType("path"));
  }

  private Class<?> getFieldType(String fieldName) {
    try {
      Field field = Image.class.getField(fieldName);
      return field.getType();
    } catch (NoSuchFieldException e) {
      fail("Field " + fieldName + " should exist");
      return null;
    }
  }

  @Test
  public void testImmutableAfterConstruction_FieldsCanBeModified() {
    // Test that fields can be modified after construction (they should be mutable)
    String originalName = image.name;
    String originalSource = image.source;
    String originalPath = image.path;

    image.name = "ModifiedName";
    image.source = "ModifiedSource";
    image.path = "ModifiedPath";

    assertNotEquals("Name should be modifiable", originalName, image.name);
    assertNotEquals("Source should be modifiable", originalSource, image.source);
    assertNotEquals("Path should be modifiable", originalPath, image.path);
  }

  @Test
  public void testToStringBehavior() {
    // Test toString behavior (even though not overridden, test default behavior)
    String toString = image.toString();
    assertNotNull("toString should not return null", toString);
    assertTrue("toString should contain class name", toString.contains("Image"));
  }

  @Test
  public void testEqualsSymmetric() {
    Image image1 = new Image("Test", "source", "path");
    Image image2 = new Image("Test", "source", "path");

    image1.setId(5);
    image2.setId(5);

    assertTrue("Equals should be symmetric", image1.equals(image2) && image2.equals(image1));
  }

  @Test
  public void testEqualsTransitive() {
    Image image1 = new Image("A", "sourceA", "pathA");
    Image image2 = new Image("B", "sourceB", "pathB");
    Image image3 = new Image("C", "sourceC", "pathC");

    image1.setId(10);
    image2.setId(10);
    image3.setId(10);

    assertTrue(
        "Equals should be transitive",
        image1.equals(image2) && image2.equals(image3) && image1.equals(image3)
    );
  }

  @Test
  public void testEqualsConsistent() {
    Image image1 = new Image("Test", "source", "path");
    Image image2 = new Image("Test", "source", "path");

    image1.setId(15);
    image2.setId(15);

    // Multiple calls should return consistent results
    assertEquals("First equals call", image1, image2);
    assertEquals("Second equals call", image1, image2);
    assertEquals("Third equals call", image1, image2);
  }

  @Test
  public void testHashCodeConsistent() {
    image.setId(25);

    int hashCode1 = image.hashCode();
    int hashCode2 = image.hashCode();
    int hashCode3 = image.hashCode();

    assertEquals("Hash code should be consistent", hashCode1, hashCode2);
    assertEquals("Hash code should be consistent", hashCode2, hashCode3);
  }

  @Test
  public void testCloneLikeCreation() {
    // Test creating a "copy" of an image
    Image original = new Image("Original", Constants.SOURCE_DEFAULT, "original_path");
    original.setId(50);

    Image copy = new Image(original.getName(), original.getSource(), original.getPath());
    copy.setId(original.getId());

    assertEquals("Copy should equal original", original, copy);
    assertEquals("Copy should have same hash code", original.hashCode(), copy.hashCode());
    assertNotSame("Copy should be different object", original, copy);
  }

  @Test
  public void testEqualsWithSubclass() {
    // Test equals behavior with potential subclass
    Image baseImage = new Image("Base", "source", "path");
    baseImage.setId(1);

    Object subclassLike = new Object() {
      @Override
      public boolean equals(Object obj) {
        return obj instanceof Image && ((Image) obj).getId() == 1;
      }
    };

    assertNotEquals(
        "Image should not equal different class even with same ID",
        baseImage,
        subclassLike
    );
  }
} 
