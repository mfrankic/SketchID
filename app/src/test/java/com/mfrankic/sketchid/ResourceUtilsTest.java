package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Unit tests for ResourceUtils utility class.
 * Tests initialization, resource mapping, and edge cases.
 */
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
  public void testConstructorThrowsException() {
    try {
      // Use reflection to test private constructor
      java.lang.reflect.Constructor<ResourceUtils> constructor
          = ResourceUtils.class.getDeclaredConstructor();
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
        "House resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("house")
    );
    assertNotEquals(
        "Lightbulb resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("lightbulb")
    );
    assertNotEquals(
        "Moon resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("moon")
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
        "Sun resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("sun")
    );
    assertNotEquals(
        "Umbrella resource should be found",
        0,
        ResourceUtils.getDrawableResourceByName("umbrella")
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
        "crown",
        "envelope",
        "grid",
        "house",
        "lightbulb",
        "moon",
        "smiley",
        "star",
        "sun",
        "umbrella"
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
        "crown",
        "envelope",
        "grid",
        "house",
        "lightbulb",
        "moon",
        "smiley",
        "star",
        "sun",
        "umbrella"
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
        "crown",
        "envelope",
        "grid",
        "house",
        "lightbulb",
        "moon",
        "smiley",
        "star",
        "sun",
        "umbrella"
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
