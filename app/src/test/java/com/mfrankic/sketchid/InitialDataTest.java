package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for InitialData utility class.
 * Tests default image data generation and validation.
 */
public class InitialDataTest {

  @Test
  public void testConstructorThrowsException() {
    try {
      // Use reflection to test private constructor
      java.lang.reflect.Constructor<InitialData> constructor
          = InitialData.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Constructor should throw IllegalStateException");
    } catch (Exception e) {
      assertTrue(
          "Should throw IllegalStateException",
          e.getCause() instanceof IllegalStateException
      );
      assertEquals("Should have correct message", "Utility class", e.getCause().getMessage());
    }
  }

  @Test
  public void testGetImagesNotNull() {
    List<Image> images = InitialData.getImages();
    assertNotNull("Images list should not be null", images);
  }

  @Test
  public void testGetImagesNotEmpty() {
    List<Image> images = InitialData.getImages();
    assertFalse("Images list should not be empty", images.isEmpty());
    assertTrue("Images list should have at least one image", images.size() > 0);
  }

  @Test
  public void testGetImagesExpectedCount() {
    List<Image> images = InitialData.getImages();
    // Based on the implementation, we expect 10 default images
    assertEquals("Should have exactly 10 default images", 10, images.size());
  }

  @Test
  public void testAllImagesHaveValidNames() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertNotNull("Image should not be null", image);
      assertNotNull("Image name should not be null", image.getName());
      assertFalse("Image name should not be empty", image.getName().isEmpty());
      assertFalse("Image name should not be blank", image.getName().trim().isEmpty());
    }
  }

  @Test
  public void testAllImagesHaveDefaultSource() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertEquals(
          "All images should have default source",
          Constants.SOURCE_DEFAULT,
          image.getSource()
      );
    }
  }

  @Test
  public void testAllImagesHaveValidPaths() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertNotNull("Image path should not be null", image.getPath());
      assertFalse("Image path should not be empty", image.getPath().isEmpty());

      // Path should be a string representation of a drawable resource ID
      try {
        int resourceId = Integer.parseInt(image.getPath());
        assertTrue("Resource ID should be positive", resourceId > 0);
      } catch (NumberFormatException e) {
        fail("Image path should be a valid integer: " + image.getPath());
      }
    }
  }

  @Test
  public void testExpectedImageNames() {
    List<Image> images = InitialData.getImages();

    // Expected image names based on the implementation
    String[] expectedNames = {
        "Arrow",
        "Crown",
        "Envelope",
        "House",
        "Lightbulb",
        "Moon",
        "Smiley",
        "Star",
        "Sun",
        "Umbrella"
    };

    assertEquals("Should have expected number of images", expectedNames.length, images.size());

    Set<String> actualNames = new HashSet<>();
    for (Image image : images) {
      actualNames.add(image.getName());
    }

    for (String expectedName : expectedNames) {
      assertTrue("Should contain image: " + expectedName, actualNames.contains(expectedName));
    }
  }

  @Test
  public void testImageNamesAreUnique() {
    List<Image> images = InitialData.getImages();

    Set<String> uniqueNames = new HashSet<>();
    for (Image image : images) {
      String name = image.getName();
      assertFalse("Image name should be unique: " + name, uniqueNames.contains(name));
      uniqueNames.add(name);
    }

    assertEquals("All image names should be unique", images.size(), uniqueNames.size());
  }

  @Test
  public void testImagePathsAreUnique() {
    List<Image> images = InitialData.getImages();

    Set<String> uniquePaths = new HashSet<>();
    for (Image image : images) {
      String path = image.getPath();
      assertFalse("Image path should be unique: " + path, uniquePaths.contains(path));
      uniquePaths.add(path);
    }

    assertEquals("All image paths should be unique", images.size(), uniquePaths.size());
  }

  @Test
  public void testImageIdsAreInitializedToZero() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertEquals("Image ID should be initialized to 0", 0, image.getId());
    }
  }

  @Test
  public void testResourceIdValidation() {
    List<Image> images = InitialData.getImages();

    // These should match the expected R.drawable resource IDs
    for (Image image : images) {
      String path = image.getPath();
      int resourceId = Integer.parseInt(path);

      // Verify the resource ID corresponds to the expected drawable based on name
      switch (image.getName()) {
        case "Arrow":
          assertEquals(
              "Arrow should map to R.drawable.arrow",
              String.valueOf(R.drawable.arrow),
              path
          );
          break;
        case "Crown":
          assertEquals(
              "Crown should map to R.drawable.crown",
              String.valueOf(R.drawable.crown),
              path
          );
          break;
        case "Envelope":
          assertEquals(
              "Envelope should map to R.drawable.envelope",
              String.valueOf(R.drawable.envelope),
              path
          );
          break;
        case "House":
          assertEquals(
              "House should map to R.drawable.house",
              String.valueOf(R.drawable.house),
              path
          );
          break;
        case "Lightbulb":
          assertEquals(
              "Lightbulb should map to R.drawable.lightbulb",
              String.valueOf(R.drawable.lightbulb),
              path
          );
          break;
        case "Moon":
          assertEquals("Moon should map to R.drawable.moon", String.valueOf(R.drawable.moon), path);
          break;
        case "Smiley":
          assertEquals(
              "Smiley should map to R.drawable.smiley",
              String.valueOf(R.drawable.smiley),
              path
          );
          break;
        case "Star":
          assertEquals("Star should map to R.drawable.star", String.valueOf(R.drawable.star), path);
          break;
        case "Sun":
          assertEquals("Sun should map to R.drawable.sun", String.valueOf(R.drawable.sun), path);
          break;
        case "Umbrella":
          assertEquals(
              "Umbrella should map to R.drawable.umbrella",
              String.valueOf(R.drawable.umbrella),
              path
          );
          break;
        default:
          fail("Unexpected image name: " + image.getName());
      }
    }
  }

  @Test
  public void testGetImagesConsistency() {
    // Test that multiple calls return consistent results
    List<Image> images1 = InitialData.getImages();
    List<Image> images2 = InitialData.getImages();

    assertEquals(
        "Multiple calls should return same number of images",
        images1.size(),
        images2.size()
    );

    for (int i = 0; i < images1.size(); i++) {
      Image img1 = images1.get(i);
      Image img2 = images2.get(i);

      assertEquals("Image names should be consistent", img1.getName(), img2.getName());
      assertEquals("Image sources should be consistent", img1.getSource(), img2.getSource());
      assertEquals("Image paths should be consistent", img1.getPath(), img2.getPath());
      assertEquals("Image IDs should be consistent", img1.getId(), img2.getId());
    }
  }

  @Test
  public void testGetImagesIndependence() {
    // Test that returned lists are independent (modifying one doesn't affect others)
    List<Image> images1 = InitialData.getImages();
    List<Image> images2 = InitialData.getImages();

    // Modify first list
    images1.clear();

    // Second list should be unaffected
    assertFalse(
        "Second list should not be affected by modifications to first list",
        images2.isEmpty()
    );
    assertEquals("Second list should still have all images", 10, images2.size());
  }

  @Test
  public void testImageObjectIndependence() {
    // Test that image objects are independent
    List<Image> images1 = InitialData.getImages();
    List<Image> images2 = InitialData.getImages();

    if (!images1.isEmpty() && !images2.isEmpty()) {
      Image img1 = images1.get(0);
      Image img2 = images2.get(0);

      // They should have same data but be different objects
      assertEquals("Images should have same name", img1.getName(), img2.getName());
      assertNotSame("Images should be different objects", img1, img2);

      // Modifying one shouldn't affect the other
      img1.setName("Modified");
      assertNotEquals(
          "Modifying one image shouldn't affect the other",
          img1.getName(),
          img2.getName()
      );
    }
  }

  @Test
  public void testImageNameCapitalization() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      String name = image.getName();
      // All names should start with capital letter (title case)
      assertTrue(
          "Image name should start with capital letter: " + name,
          Character.isUpperCase(name.charAt(0))
      );
    }
  }

  @Test
  public void testNoNullImagesInList() {
    List<Image> images = InitialData.getImages();

    for (int i = 0; i < images.size(); i++) {
      assertNotNull("Image at index " + i + " should not be null", images.get(i));
    }
  }

  @Test
  public void testImageFieldsNotEmpty() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertNotNull("Image name should not be null", image.getName());
      assertNotNull("Image source should not be null", image.getSource());
      assertNotNull("Image path should not be null", image.getPath());

      assertFalse("Image name should not be empty", image.getName().isEmpty());
      assertFalse("Image source should not be empty", image.getSource().isEmpty());
      assertFalse("Image path should not be empty", image.getPath().isEmpty());
    }
  }

  @Test
  public void testExpectedImageOrder() {
    List<Image> images = InitialData.getImages();

    // Test that images are in expected order
    String[] expectedOrder = {
        "Arrow",
        "Crown",
        "Envelope",
        "House",
        "Lightbulb",
        "Moon",
        "Smiley",
        "Star",
        "Sun",
        "Umbrella"
    };

    for (int i = 0; i < expectedOrder.length && i < images.size(); i++) {
      assertEquals(
          "Image at position " + i + " should be " + expectedOrder[i],
          expectedOrder[i],
          images.get(i).getName()
      );
    }
  }

  @Test
  public void testListIsModifiable() {
    // The returned list should be modifiable (new ArrayList)
    List<Image> images = InitialData.getImages();

    int originalSize = images.size();

    // Should be able to add to the list
    images.add(new Image("Test", "test", "test"));
    assertEquals("Should be able to add to returned list", originalSize + 1, images.size());

    // Should be able to remove from the list
    images.remove(images.size() - 1);
    assertEquals("Should be able to remove from returned list", originalSize, images.size());
  }

  @Test
  public void testEmptyOperationsOnList() {
    List<Image> images = InitialData.getImages();

    // Test various list operations
    assertFalse("List should not be empty", images.isEmpty());
    assertTrue("List should contain images", images.size() > 0);

    // Test that we can iterate through the list
    int count = 0;
    for (Image image : images) {
      assertNotNull("Each image should not be null", image);
      count++;
    }
    assertEquals("Iteration count should match list size", images.size(), count);
  }

  @Test
  public void testAllResourceIdsArePositive() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      int resourceId = Integer.parseInt(image.getPath());
      assertTrue("Resource ID should be positive for " + image.getName(), resourceId > 0);
    }
  }
} 
