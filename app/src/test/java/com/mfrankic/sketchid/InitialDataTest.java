package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for InitialData utility class.
 * Tests default image data generation and validation.
 */
@RunWith(MockitoJUnitRunner.class)
public class InitialDataTest {

  @Test
  public void testConstructor_ThrowsIllegalStateException() {
    try {
      Constructor<InitialData> constructor = InitialData.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Constructor should throw IllegalStateException");
    } catch (InvocationTargetException e) {
      assertTrue(
          "Should throw IllegalStateException",
          e.getCause() instanceof IllegalStateException
      );
      assertEquals("Should have correct message", "Utility class", e.getCause().getMessage());
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test
  public void testClass_IsUtilityClass() {
    assertTrue("InitialData should be public", Modifier.isPublic(InitialData.class.getModifiers()));

    // Check that constructor is private
    try {
      Constructor<InitialData> constructor = InitialData.class.getDeclaredConstructor();
      assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("InitialData should have a default constructor");
    }

    // Verify all public methods are static
    Method[] methods = InitialData.class.getDeclaredMethods();
    for (Method method : methods) {
      if (Modifier.isPublic(method.getModifiers())) {
        assertTrue(
            "Public method " + method.getName() + " should be static",
            Modifier.isStatic(method.getModifiers())
        );
      }
    }
  }

  @Test
  public void testGetImages_ReturnsNonNullList() {
    List<Image> images = InitialData.getImages();
    assertNotNull("getImages should return a non-null list", images);
  }

  @Test
  public void testGetImages_ReturnsNonEmptyList() {
    List<Image> images = InitialData.getImages();
    assertFalse("getImages should return a non-empty list", images.isEmpty());
    assertTrue("getImages should return multiple images", images.size() > 1);
  }

  @Test
  public void testGetImages_ReturnsExpectedCount() {
    List<Image> images = InitialData.getImages();
    assertEquals("Should return exactly 10 images", 10, images.size());
  }

  @Test
  public void testGetImages_AllImagesHaveValidNames() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertNotNull("Image should not be null", image);
      assertNotNull("Image name should not be null", image.name);
      assertFalse("Image name should not be empty", image.name.isEmpty());
      assertTrue(
          "Image name should start with capital letter",
          Character.isUpperCase(image.name.charAt(0))
      );
    }
  }

  @Test
  public void testGetImages_AllImagesHaveDefaultSource() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertEquals("All images should have default source", Constants.SOURCE_DEFAULT, image.source);
    }
  }

  @Test
  public void testGetImages_AllImagesHaveValidPaths() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      assertNotNull("Image path should not be null", image.path);
      assertFalse("Image path should not be empty", image.path.isEmpty());

      // Path should be a string representation of an integer (resource ID)
      try {
        int resourceId = Integer.parseInt(image.path);
        assertTrue("Resource ID should be positive", resourceId > 0);
      } catch (NumberFormatException e) {
        fail("Image path should be a valid integer string: " + image.path);
      }
    }
  }

  @Test
  public void testGetImages_ContainsExpectedImageNames() {
    List<Image> images = InitialData.getImages();
    Set<String> imageNames = new HashSet<>();

    for (Image image : images) {
      imageNames.add(image.name);
    }

    // Verify specific expected images are present
    assertTrue("Should contain Arrow image", imageNames.contains("Arrow"));
    assertTrue("Should contain Checkmark image", imageNames.contains("Checkmark"));
    assertTrue("Should contain Crown image", imageNames.contains("Crown"));
    assertTrue("Should contain Envelope image", imageNames.contains("Envelope"));
    assertTrue("Should contain Heart image", imageNames.contains("Heart"));
    assertTrue("Should contain Lightbulb image", imageNames.contains("Lightbulb"));
    assertTrue("Should contain Smiley image", imageNames.contains("Smiley"));
    assertTrue("Should contain Star image", imageNames.contains("Star"));
    assertTrue("Should contain Umbrella image", imageNames.contains("Umbrella"));
    assertTrue("Should contain Upload image", imageNames.contains("Upload"));
  }

  @Test
  public void testGetImages_NoDuplicateNames() {
    List<Image> images = InitialData.getImages();
    Set<String> imageNames = new HashSet<>();

    for (Image image : images) {
      assertFalse(
          "Image name should not be duplicate: " + image.name,
          imageNames.contains(image.name)
      );
      imageNames.add(image.name);
    }

    assertEquals("All image names should be unique", images.size(), imageNames.size());
  }

  @Test
  public void testGetImages_NoDuplicatePaths() {
    List<Image> images = InitialData.getImages();
    Set<String> imagePaths = new HashSet<>();

    for (Image image : images) {
      assertFalse(
          "Image path should not be duplicate: " + image.path,
          imagePaths.contains(image.path)
      );
      imagePaths.add(image.path);
    }

    assertEquals("All image paths should be unique", images.size(), imagePaths.size());
  }

  @Test
  public void testGetImages_ReturnsNewListEachTime() {
    List<Image> images1 = InitialData.getImages();
    List<Image> images2 = InitialData.getImages();

    assertNotSame("Should return different list instances", images1, images2);
    assertEquals("Lists should have same content", images1.size(), images2.size());

    // Verify the lists are independent (modifying one doesn't affect the other)
    images1.clear();
    assertFalse("Second list should still have images after clearing first", images2.isEmpty());
  }

  @Test
  public void testGetImages_ImageObjectsAreNewInstances() {
    List<Image> images1 = InitialData.getImages();
    List<Image> images2 = InitialData.getImages();

    for (int i = 0; i < images1.size(); i++) {
      Image img1 = images1.get(i);
      Image img2 = images2.get(i);

      assertNotSame("Image objects should be different instances", img1, img2);
      assertEquals("Image names should be equal", img1.name, img2.name);
      assertEquals("Image sources should be equal", img1.source, img2.source);
      assertEquals("Image paths should be equal", img1.path, img2.path);
    }
  }

  @Test
  public void testGetImages_MethodExists() {
    try {
      Method method = InitialData.class.getMethod("getImages");
      assertTrue("getImages method should be static", Modifier.isStatic(method.getModifiers()));
      assertTrue("getImages method should be public", Modifier.isPublic(method.getModifiers()));
      assertEquals(
          "getImages method should return List",
          java.util.List.class,
          method.getReturnType()
      );
      assertEquals(
          "getImages method should have no parameters",
          0,
          method.getParameterTypes().length
      );
    } catch (NoSuchMethodException e) {
      fail("getImages method should exist with correct signature");
    }
  }

  @Test
  public void testClass_HasCorrectPackage() {
    assertEquals(
        "InitialData should be in correct package",
        "com.mfrankic.sketchid",
        InitialData.class.getPackage().getName()
    );
  }

  @Test
  public void testClass_HasCorrectName() {
    assertEquals(
        "Class should have correct simple name",
        "InitialData",
        InitialData.class.getSimpleName()
    );
  }

  @Test
  public void testClass_HasNoPublicFields() {
    java.lang.reflect.Field[] fields = InitialData.class.getFields();
    assertEquals("Utility class should have no public fields", 0, fields.length);
  }

  @Test
  public void testClass_HasCorrectMethodCount() {
    Method[] publicMethods = InitialData.class.getMethods();
    int utilityMethods = 0;

    for (Method method : publicMethods) {
      if (method.getDeclaringClass() == InitialData.class) {
        utilityMethods++;
      }
    }

    assertEquals("Should have exactly 1 utility method", 1, utilityMethods);
  }

  @Test
  public void testClass_ImplementsNoInterfaces() {
    Class<?>[] interfaces = InitialData.class.getInterfaces();
    assertEquals("Utility class should implement no interfaces", 0, interfaces.length);
  }

  @Test
  public void testClass_ExtendsObject() {
    assertEquals("Should extend only Object", Object.class, InitialData.class.getSuperclass());
  }

  @Test
  public void testGetImages_ImagesHaveValidStructure() {
    List<Image> images = InitialData.getImages();

    for (Image image : images) {
      // Test Image object structure
      assertNotNull("Image should have name", image.name);
      assertNotNull("Image should have source", image.source);
      assertNotNull("Image should have path", image.path);

      // Test that name follows expected pattern (capital letter + lowercase)
      assertTrue(
          "Image name should start with capital letter",
          Character.isUpperCase(image.name.charAt(0))
      );

      // Test that source is the expected constant
      assertEquals("Image source should be default", Constants.SOURCE_DEFAULT, image.source);

      // Test that path represents a valid drawable resource
      assertTrue("Image path should be numeric string", image.path.matches("\\d+"));
    }
  }

  @Test
  public void testGetImages_ConsistentOrdering() {
    List<Image> images1 = InitialData.getImages();
    List<Image> images2 = InitialData.getImages();

    assertEquals("Lists should have same size", images1.size(), images2.size());

    for (int i = 0; i < images1.size(); i++) {
      assertEquals(
          "Image order should be consistent: position " + i,
          images1.get(i).name,
          images2.get(i).name
      );
    }
  }

  @Test
  public void testGetImages_CorrectImageOrder() {
    List<Image> images = InitialData.getImages();

    // Test that images are in expected alphabetical-ish order
    String[] expectedOrder = {
        "Arrow",
        "Checkmark",
        "Crown",
        "Envelope",
        "Heart",
        "Lightbulb",
        "Smiley",
        "Star",
        "Umbrella",
        "Upload"
    };

    assertEquals("Should have expected number of images", expectedOrder.length, images.size());

    for (int i = 0; i < expectedOrder.length; i++) {
      assertEquals(
          "Image at position " + i + " should be " + expectedOrder[i],
          expectedOrder[i],
          images.get(i).name
      );
    }
  }

  @Test
  public void testGetImages_ImagesAreModifiable() {
    List<Image> images = InitialData.getImages();

    // Test that the returned list is modifiable (not unmodifiable)
    int originalSize = images.size();
    Image testImage = new Image("Test", "test", "test");

    try {
      images.add(testImage);
      assertEquals("List should be modifiable", originalSize + 1, images.size());
    } catch (UnsupportedOperationException e) {
      fail("Returned list should be modifiable");
    }
  }

  @Test
  public void testClass_CannotBeInstantiatedNormally() {
    Constructor<?>[] constructors = InitialData.class.getConstructors();
    assertEquals("Should have no public constructors", 0, constructors.length);
  }

  @Test
  public void testClass_HasPrivateConstructor() {
    Constructor<?>[] allConstructors = InitialData.class.getDeclaredConstructors();
    assertEquals("Should have exactly one constructor", 1, allConstructors.length);

    Constructor<?> constructor = allConstructors[0];
    assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
    assertEquals(
        "Constructor should have no parameters",
        0,
        constructor.getParameterTypes().length
    );
  }
} 
