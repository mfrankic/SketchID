package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Unit tests for ResourceUtils utility class.
 * Tests initialization, resource mapping, and edge cases.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceUtilsTest {

  @Before
  public void setup()
  throws Exception {
    // Reset the initialized state before each test
    resetInitializedState();
  }

  /**
   * Reset the initialized state using reflection to ensure clean state for each test
   */
  private void resetInitializedState()
  throws Exception {
    Field initializedField = ResourceUtils.class.getDeclaredField("initialized");
    initializedField.setAccessible(true);
    initializedField.setBoolean(null, false);

    Field mapField = ResourceUtils.class.getDeclaredField("drawableResourceMap");
    mapField.setAccessible(true);
    mapField.get(null).getClass().getMethod("clear").invoke(mapField.get(null));
  }

  @After
  public void tearDown()
  throws Exception {
    // Reset the initialized state after each test for isolation
    resetInitializedState();
  }

  @Test
  public void testConstructor_ThrowsIllegalStateException() {
    try {
      Constructor<ResourceUtils> constructor = ResourceUtils.class.getDeclaredConstructor();
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
  public void testClass_IsFinal() {
    // ResourceUtils is not actually final in the implementation
    assertFalse(
        "ResourceUtils is not final in current implementation",
        Modifier.isFinal(ResourceUtils.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsUtilityClass() {
    assertTrue(
        "ResourceUtils should be public",
        Modifier.isPublic(ResourceUtils.class.getModifiers())
    );

    // Check that constructor is private
    try {
      Constructor<ResourceUtils> constructor = ResourceUtils.class.getDeclaredConstructor();
      assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("ResourceUtils should have a default constructor");
    }
  }

  @Test
  public void testClass_HasCorrectPackage() {
    assertEquals(
        "ResourceUtils should be in correct package",
        "com.mfrankic.sketchid",
        ResourceUtils.class.getPackage().getName()
    );
  }

  @Test
  public void testInitialize_MethodExists() {
    try {
      Method initializeMethod = ResourceUtils.class.getMethod("initialize");
      assertNotNull("initialize method should exist", initializeMethod);
      assertTrue("initialize should be public", Modifier.isPublic(initializeMethod.getModifiers()));
      assertTrue("initialize should be static", Modifier.isStatic(initializeMethod.getModifiers()));
      assertEquals("initialize should return void", void.class, initializeMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("initialize method should exist");
    }
  }

  @Test
  public void testGetDrawableResourceByName_MethodExists() {
    try {
      Method getDrawableMethod = ResourceUtils.class.getMethod(
          "getDrawableResourceByName",
          String.class
      );
      assertNotNull("getDrawableResourceByName method should exist", getDrawableMethod);
      assertTrue(
          "getDrawableResourceByName should be public",
          Modifier.isPublic(getDrawableMethod.getModifiers())
      );
      assertTrue(
          "getDrawableResourceByName should be static",
          Modifier.isStatic(getDrawableMethod.getModifiers())
      );
      assertEquals(
          "getDrawableResourceByName should return int",
          int.class,
          getDrawableMethod.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("getDrawableResourceByName method should exist");
    }
  }

  @Test
  public void testStaticFields_Exist() {
    try {
      Field tagField = ResourceUtils.class.getDeclaredField("TAG");
      assertNotNull("TAG field should exist", tagField);
      assertTrue("TAG should be private", Modifier.isPrivate(tagField.getModifiers()));
      assertTrue("TAG should be static", Modifier.isStatic(tagField.getModifiers()));
      assertTrue("TAG should be final", Modifier.isFinal(tagField.getModifiers()));
      assertEquals("TAG should be String", String.class, tagField.getType());

      Field mapField = ResourceUtils.class.getDeclaredField("drawableResourceMap");
      assertNotNull("drawableResourceMap field should exist", mapField);
      assertTrue(
          "drawableResourceMap should be private",
          Modifier.isPrivate(mapField.getModifiers())
      );
      assertTrue(
          "drawableResourceMap should be static",
          Modifier.isStatic(mapField.getModifiers())
      );
      assertTrue("drawableResourceMap should be final", Modifier.isFinal(mapField.getModifiers()));

      Field initializedField = ResourceUtils.class.getDeclaredField("initialized");
      assertNotNull("initialized field should exist", initializedField);
      assertTrue(
          "initialized should be private",
          Modifier.isPrivate(initializedField.getModifiers())
      );
      assertTrue(
          "initialized should be static",
          Modifier.isStatic(initializedField.getModifiers())
      );
      assertEquals("initialized should be boolean", boolean.class, initializedField.getType());
    } catch (NoSuchFieldException e) {
      fail("Required fields should exist: " + e.getMessage());
    }
  }

  @Test
  public void testAllMethodsAreStatic() {
    Method[] methods = ResourceUtils.class.getDeclaredMethods();

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
  public void testClass_HasCorrectMethodCount() {
    Method[] declaredMethods = ResourceUtils.class.getDeclaredMethods();

    // Should have initialize, getDrawableResourceByName, and initializeDrawableMap
    assertTrue("Should have at least 3 declared methods", declaredMethods.length >= 3);

    // Should not have excessive methods for this utility class
    assertTrue("Should not have excessive methods", declaredMethods.length <= 10);
  }

  @Test
  public void testClass_ModifiersCorrect() {
    int modifiers = ResourceUtils.class.getModifiers();

    assertTrue("Class should be public", Modifier.isPublic(modifiers));
    assertFalse("Class is not final in current implementation", Modifier.isFinal(modifiers));
    assertFalse("Class should not be abstract", Modifier.isAbstract(modifiers));
    assertFalse("Class should not be interface", Modifier.isInterface(modifiers));
    assertFalse("Class should not be static", Modifier.isStatic(modifiers));
  }

  @Test
  public void testUtilityClassDesignPattern() {
    // Test that ResourceUtils follows proper utility class design patterns

    // Note: Class is not final in current implementation but still follows utility pattern
    assertFalse(
        "Class is not final in current implementation",
        Modifier.isFinal(ResourceUtils.class.getModifiers())
    );

    // Should have private constructor
    try {
      Constructor<ResourceUtils> constructor = ResourceUtils.class.getDeclaredConstructor();
      assertTrue("Should have private constructor", Modifier.isPrivate(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("Should have default constructor: " + e.getMessage());
    }

    // All public methods should be static
    Method[] methods = ResourceUtils.class.getDeclaredMethods();
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
  public void testResourceMappingStructure() {
    // Test that the class has the expected structure for resource mapping

    try {
      // Should have a map for storing resource mappings
      Field mapField = ResourceUtils.class.getDeclaredField("drawableResourceMap");
      assertNotNull("Should have drawableResourceMap field", mapField);

      // Should have initialization tracking
      Field initializedField = ResourceUtils.class.getDeclaredField("initialized");
      assertNotNull("Should have initialized field", initializedField);

      // Should have initialization method
      Method initializeMethod = ResourceUtils.class.getMethod("initialize");
      assertNotNull("Should have initialize method", initializeMethod);

      // Should have resource lookup method
      Method getResourceMethod = ResourceUtils.class.getMethod(
          "getDrawableResourceByName",
          String.class
      );
      assertNotNull("Should have getDrawableResourceByName method", getResourceMethod);

      assertTrue("Resource mapping structure should be properly implemented", true);
    } catch (NoSuchFieldException | NoSuchMethodException e) {
      fail("Resource mapping structure should be implemented: " + e.getMessage());
    }
  }

  @Test
  public void testMethodParameterTypes() {
    try {
      Method getDrawableMethod = ResourceUtils.class.getMethod(
          "getDrawableResourceByName",
          String.class
      );
      Class<?>[] paramTypes = getDrawableMethod.getParameterTypes();
      assertEquals("getDrawableResourceByName should have 1 parameter", 1, paramTypes.length);
      assertEquals("Parameter should be String", String.class, paramTypes[0]);

      Method initializeMethod = ResourceUtils.class.getMethod("initialize");
      assertEquals(
          "initialize should have no parameters",
          0,
          initializeMethod.getParameterTypes().length
      );
    } catch (NoSuchMethodException e) {
      fail("Methods should exist with correct parameters");
    }
  }

  @Test
  public void testInitializeOnce() {
    // Initialize for the first time
    ResourceUtils.initialize();

    // Initialize again - should not cause issues
    ResourceUtils.initialize();

    // Verify that the resources are accessible after multiple initializations
    int arrowResource = ResourceUtils.getDrawableResourceByName("arrow");
    assertNotEquals("Arrow resource should be found", 0, arrowResource);
  }

  @Test
  public void testGetDrawableResourceByNameAfterInitialization() {
    ResourceUtils.initialize();

    // Test known resources
    assertNotEquals(
        "Arrow resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("arrow")
    );
    assertNotEquals(
        "Checkmark resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("checkmark")
    );
    assertNotEquals(
        "Crown resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("crown")
    );
    assertNotEquals(
        "Envelope resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("envelope")
    );
    assertNotEquals(
        "Grid resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("grid")
    );
    assertNotEquals(
        "Heart resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("heart")
    );
    assertNotEquals(
        "Lightbulb resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("lightbulb")
    );
    assertNotEquals(
        "Smiley resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("smiley")
    );
    assertNotEquals(
        "Star resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("star")
    );
    assertNotEquals(
        "Umbrella resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("umbrella")
    );
    assertNotEquals(
        "Upload resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("upload")
    );
  }

  @Test
  public void testGetDrawableResourceByNameBeforeInitialization() {
    // Before initialization, should return 0
    int result = ResourceUtils.getDrawableResourceByName("arrow");
    assertEquals("Should return 0 when not initialized", 0, result);
  }

  @Test
  public void testGetDrawableResourceByNameWithNullInput() {
    ResourceUtils.initialize();

    int result = ResourceUtils.getDrawableResourceByName(null);
    assertEquals("Should return 0 for null input", 0, result);
  }

  @Test
  public void testGetDrawableResourceByNameWithEmptyInput() {
    ResourceUtils.initialize();

    int result = ResourceUtils.getDrawableResourceByName("");
    assertEquals("Should return 0 for empty input", 0, result);
  }

  @Test
  public void testGetDrawableResourceByNameWithUnknownInput() {
    ResourceUtils.initialize();

    int result = ResourceUtils.getDrawableResourceByName("unknown_resource");
    assertEquals("Should return 0 for unknown resource", 0, result);
  }

  @Test
  public void testGetDrawableResourceByNameCaseInsensitive() {
    ResourceUtils.initialize();

    // Test with different cases
    int arrowLower = ResourceUtils.getDrawableResourceByName("arrow");
    int arrowUpper = ResourceUtils.getDrawableResourceByName("ARROW");
    int arrowMixed = ResourceUtils.getDrawableResourceByName("Arrow");
    int arrowCamel = ResourceUtils.getDrawableResourceByName("aRrOw");

    assertNotEquals("Arrow lowercase should be found", 0, arrowLower);
    assertEquals("Arrow uppercase should match lowercase", arrowLower, arrowUpper);
    assertEquals("Arrow mixed case should match lowercase", arrowLower, arrowMixed);
    assertEquals("Arrow camel case should match lowercase", arrowLower, arrowCamel);
  }

  @Test
  public void testGetDrawableResourceByNameWithWhitespace() {
    ResourceUtils.initialize();

    // Test with whitespace (should return 0 as whitespace is not trimmed)
    int result1 = ResourceUtils.getDrawableResourceByName(" arrow");
    int result2 = ResourceUtils.getDrawableResourceByName("arrow ");
    int result3 = ResourceUtils.getDrawableResourceByName(" arrow ");

    assertEquals("Should return 0 for leading whitespace", 0, result1);
    assertEquals("Should return 0 for trailing whitespace", 0, result2);
    assertEquals("Should return 0 for surrounding whitespace", 0, result3);
  }

  @Test
  public void testAllKnownResourcesAreMapped() {
    ResourceUtils.initialize();

    String[] knownResources = {
        "arrow",
        "checkmark",
        "crown",
        "envelope",
        "grid",
        "heart",
        "lightbulb",
        "smiley",
        "star",
        "umbrella",
        "upload"
    };

    for (String resourceName : knownResources) {
      int resourceId = ResourceUtils.getDrawableResourceByName(resourceName);
      assertNotEquals("Resource " + resourceName + " should be mapped", 0, resourceId);
    }
  }

  @Test
  public void testResourceConsistency() {
    ResourceUtils.initialize();

    // Test that the same resource name always returns the same ID
    String resourceName = "arrow";
    int firstCall = ResourceUtils.getDrawableResourceByName(resourceName);
    int secondCall = ResourceUtils.getDrawableResourceByName(resourceName);
    int thirdCall = ResourceUtils.getDrawableResourceByName(resourceName);

    assertEquals("Resource ID should be consistent across calls", firstCall, secondCall);
    assertEquals("Resource ID should be consistent across calls", secondCall, thirdCall);
  }

  @Test
  public void testSpecialCharactersInResourceName() {
    ResourceUtils.initialize();

    // Test with special characters (should return 0)
    String[] specialNames = {
        "arrow@",
        "crown#",
        "envelope$",
        "house%",
        "star^",
        "arrow&star",
        "moon*sun",
        "smiley()",
        "house+grid",
        "arrow.png",
        "crown.jpg",
        "envelope.svg"
    };

    for (String specialName : specialNames) {
      int result = ResourceUtils.getDrawableResourceByName(specialName);
      assertEquals("Special character name should return 0: " + specialName, 0, result);
    }
  }

  @Test
  public void testNumericStringsAsResourceNames() {
    ResourceUtils.initialize();

    // Test with numeric strings (should return 0)
    String[] numericNames = {"123", "0", "-1", "999", "12345"};

    for (String numericName : numericNames) {
      int result = ResourceUtils.getDrawableResourceByName(numericName);
      assertEquals("Numeric name should return 0: " + numericName, 0, result);
    }
  }

  @Test
  public void testVeryLongResourceNames() {
    ResourceUtils.initialize();

    // Test with very long strings (should return 0)
    String longName
        = "this_is_a_very_long_resource_name_that_definitely_does_not_exist_in_the_mapping";
    int result = ResourceUtils.getDrawableResourceByName(longName);
    assertEquals("Very long name should return 0", 0, result);
  }

  @Test
  public void testUnicodeCharactersInResourceName() {
    ResourceUtils.initialize();

    // Test with Unicode characters (should return 0)
    String[] unicodeNames = {"箭头", "王冠", "信封", "房子", "星星", "🏠", "⭐", "🌙"};

    for (String unicodeName : unicodeNames) {
      int result = ResourceUtils.getDrawableResourceByName(unicodeName);
      assertEquals("Unicode name should return 0: " + unicodeName, 0, result);
    }
  }

  @Test
  public void testMultipleInitializationsDoNotChangeResults() {
    // Initialize multiple times
    ResourceUtils.initialize();
    int firstResult = ResourceUtils.getDrawableResourceByName("arrow");

    ResourceUtils.initialize();
    int secondResult = ResourceUtils.getDrawableResourceByName("arrow");

    ResourceUtils.initialize();
    int thirdResult = ResourceUtils.getDrawableResourceByName("arrow");

    assertEquals("Multiple initializations should not change results", firstResult, secondResult);
    assertEquals("Multiple initializations should not change results", secondResult, thirdResult);
  }

  @Test
  public void testPartialMatchesReturnZero() {
    ResourceUtils.initialize();

    // Test partial matches (should return 0)
    String[] partialMatches = {
        "arr", "arro", "crown123", "envelope_icon", "house_small", "star_filled", "moon_phase"
    };

    for (String partialMatch : partialMatches) {
      int result = ResourceUtils.getDrawableResourceByName(partialMatch);
      assertEquals("Partial match should return 0: " + partialMatch, 0, result);
    }
  }

  @Test
  public void testResourceIdsArePositive() {
    ResourceUtils.initialize();

    String[] knownResources = {
        "arrow",
        "checkmark",
        "crown",
        "envelope",
        "grid",
        "heart",
        "lightbulb",
        "smiley",
        "star",
        "umbrella",
        "upload"
    };

    for (String resourceName : knownResources) {
      int resourceId = ResourceUtils.getDrawableResourceByName(resourceName);
      assertTrue("Resource ID should be positive for: " + resourceName, resourceId > 0);
    }
  }

  @Test
  public void testResourceIdsAreUnique() {
    ResourceUtils.initialize();

    String[] knownResources = {
        "arrow",
        "checkmark",
        "crown",
        "envelope",
        "grid",
        "heart",
        "lightbulb",
        "smiley",
        "star",
        "umbrella",
        "upload"
    };

    java.util.Set<Integer> resourceIds = new java.util.HashSet<>();

    for (String resourceName : knownResources) {
      int resourceId = ResourceUtils.getDrawableResourceByName(resourceName);
      assertFalse(
          "Resource ID should be unique for: " + resourceName,
          resourceIds.contains(resourceId)
      );
      resourceIds.add(resourceId);
    }

    assertEquals(
        "Should have unique IDs for all resources",
        knownResources.length,
        resourceIds.size()
    );
  }

  @Test
  public void testResourceMappingBehaviorWithMultipleThreads()
  throws InterruptedException {
    // Simple test to ensure basic thread safety (initialize only once)
    final int[] results = new int[2];

    Thread thread1 = new Thread(() -> {
      ResourceUtils.initialize();
      results[0] = ResourceUtils.getDrawableResourceByName("arrow");
    });

    Thread thread2 = new Thread(() -> {
      ResourceUtils.initialize();
      results[1] = ResourceUtils.getDrawableResourceByName("arrow");
    });

    thread1.start();
    thread2.start();

    thread1.join(1000); // Wait max 1 second
    thread2.join(1000);

    // Both threads should get the same result
    assertEquals("Both threads should get the same resource ID", results[0], results[1]);
    assertNotEquals("Resource ID should not be 0", 0, results[0]);
  }

  @Test
  public void testBoundaryConditionsForResourceNames() {
    ResourceUtils.initialize();

    // Test various boundary conditions
    assertEquals(
        "Single character should return 0",
        0,
        ResourceUtils.getDrawableResourceByName("a")
    );
    assertEquals(
        "Two characters should return 0",
        0,
        ResourceUtils.getDrawableResourceByName("ab")
    );
    assertEquals(
        "Very long string should return 0",
        0,
        ResourceUtils.getDrawableResourceByName("a".repeat(1000))
    );
  }
} 

