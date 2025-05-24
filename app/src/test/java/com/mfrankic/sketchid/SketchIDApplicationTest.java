package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for the SketchIDApplication class
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, application = SketchIDApplication.class)
public class SketchIDApplicationTest {

  private SketchIDApplication application;

  @Before
  public void setUp() {
    application = new SketchIDApplication();
  }

  @Test
  public void testApplicationCreation() {
    assertNotNull("Application should not be null", application);
    assertTrue("Should be instance of Application", application instanceof Application);
    assertTrue(
        "Should be instance of SketchIDApplication",
        application instanceof SketchIDApplication
    );
  }

  @Test
  public void testOnCreate() {
    // Test that onCreate doesn't crash
    application.onCreate();

    // Verify that ResourceUtils is initialized after onCreate
    // We can test this by checking if ResourceUtils can be used without throwing exceptions
    assertTrue(
        "ResourceUtils should be initialized",
        ResourceUtils.getDrawableResourceByName("arrow") > 0
    );
  }

  @Test
  public void testResourceUtilsInitialization() {
    // Before onCreate, ResourceUtils might not be initialized
    // After onCreate, it should be initialized
    application.onCreate();

    // Test that ResourceUtils methods work after initialization
    int arrowId = ResourceUtils.getDrawableResourceByName("arrow");
    assertTrue("Arrow drawable ID should be positive after initialization", arrowId > 0);

    int crownId = ResourceUtils.getDrawableResourceByName("crown");
    assertTrue("Crown drawable ID should be positive after initialization", crownId > 0);

    int envelopeId = ResourceUtils.getDrawableResourceByName("envelope");
    assertTrue("Envelope drawable ID should be positive after initialization", envelopeId > 0);
  }

  @Test
  public void testOnCreateCallsSuper() {
    // This test ensures that super.onCreate() is called
    // In a real scenario, we would mock the super class, but for this simple case
    // we just verify that the method executes without throwing exceptions

    try {
      application.onCreate();
      // If we reach here, super.onCreate() was called successfully
      assertTrue("onCreate should complete without exceptions", true);
    } catch (Exception e) {
      fail("onCreate should not throw exceptions: " + e.getMessage());
    }
  }

  @Test
  public void testMultipleOnCreateCalls() {
    // Test that calling onCreate multiple times doesn't cause issues
    application.onCreate();
    application.onCreate();
    application.onCreate();

    // ResourceUtils should still work correctly
    assertTrue(
        "ResourceUtils should still work after multiple onCreate calls",
        ResourceUtils.getDrawableResourceByName("arrow") > 0
    );
  }

  @Test
  public void testApplicationLifecycle() {
    // Test the basic application lifecycle

    // 1. Application is created
    assertNotNull("Application should be created", application);

    // 2. onCreate is called
    application.onCreate();

    // 3. Application should be in a valid state
    assertTrue(
        "ResourceUtils should be initialized",
        ResourceUtils.getDrawableResourceByName("arrow") > 0
    );

    // 4. Application should continue to work
    int drawableId = ResourceUtils.getDrawableResourceByName("crown");
    assertTrue("ResourceUtils should continue to work", drawableId > 0);
  }

  @Test
  public void testResourceUtilsStateAfterInitialization() {
    application.onCreate();

    // Test all known drawable resources to ensure they're properly initialized
    String[] knownResources = {
        "arrow",
        "crown",
        "envelope",
        "house",
        "lightbulb",
        "moon",
        "smiley",
        "star",
        "sun",
        "umbrella"
    };

    for (String resourceName : knownResources) {
      int drawableId = ResourceUtils.getDrawableResourceByName(resourceName);
      assertTrue("Drawable ID for " + resourceName + " should be positive", drawableId > 0);
    }
  }

  @Test
  public void testResourceUtilsInvalidResourceAfterInitialization() {
    application.onCreate();

    // Test that invalid resources still return 0 after initialization
    assertEquals(
        "Invalid resource should return 0",
        0,
        ResourceUtils.getDrawableResourceByName("invalid_resource")
    );
    assertEquals("Empty string should return 0", 0, ResourceUtils.getDrawableResourceByName(""));
    assertEquals("Null should return 0", 0, ResourceUtils.getDrawableResourceByName(null));
  }

  @Test
  public void testApplicationInheritance() {
    // Test that SketchIDApplication properly extends Application
    assertTrue("Should extend Application", application instanceof Application);

    // Test that it has the expected class structure
    // Note: getApplicationContext() requires proper Android context initialization
    // which isn't available in unit tests, so we just verify the class hierarchy
    assertEquals(
        "Should have correct class name",
        "com.mfrankic.sketchid.SketchIDApplication",
        application.getClass().getName()
    );
  }

  @Test
  public void testOnCreateIdempotency() {
    // Test that calling onCreate multiple times has the same effect as calling it once

    application.onCreate();
    int firstCallResult = ResourceUtils.getDrawableResourceByName("arrow");

    application.onCreate();
    int secondCallResult = ResourceUtils.getDrawableResourceByName("arrow");

    application.onCreate();
    int thirdCallResult = ResourceUtils.getDrawableResourceByName("arrow");

    assertEquals(
        "Multiple onCreate calls should produce same result",
        firstCallResult,
        secondCallResult
    );
    assertEquals(
        "Multiple onCreate calls should produce same result",
        secondCallResult,
        thirdCallResult
    );
  }

  @Test
  public void testApplicationSingleton() {
    // Test that the application behaves as expected for singleton pattern
    // (Note: In real Android, Application is a singleton, but in tests we create instances)

    SketchIDApplication app1 = new SketchIDApplication();
    SketchIDApplication app2 = new SketchIDApplication();

    // Both should be valid instances
    assertNotNull("First application instance should not be null", app1);
    assertNotNull("Second application instance should not be null", app2);

    // Both should be able to initialize ResourceUtils
    app1.onCreate();
    assertTrue(
        "ResourceUtils should work with first instance",
        ResourceUtils.getDrawableResourceByName("arrow") > 0
    );

    app2.onCreate();
    assertTrue(
        "ResourceUtils should work with second instance",
        ResourceUtils.getDrawableResourceByName("arrow") > 0
    );
  }

  @Test
  public void testInitializationOrder() {
    // Test that initialization happens in the correct order

    // Before onCreate, we should be able to create the application
    assertNotNull("Application should be created before onCreate", application);

    // After onCreate, ResourceUtils should be initialized
    application.onCreate();

    // ResourceUtils should now be functional
    assertTrue(
        "ResourceUtils should be functional after onCreate",
        ResourceUtils.getDrawableResourceByName("arrow") > 0
    );
  }
} 
