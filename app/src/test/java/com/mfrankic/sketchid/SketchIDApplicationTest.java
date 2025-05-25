package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Unit tests for the SketchIDApplication class
 */
@RunWith(MockitoJUnitRunner.class)
public class SketchIDApplicationTest {

  @Test
  public void testClass_IsPublic() {
    assertTrue(
        "SketchIDApplication should be public",
        Modifier.isPublic(SketchIDApplication.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsNotFinal() {
    assertFalse(
        "SketchIDApplication should not be final to allow inheritance",
        Modifier.isFinal(SketchIDApplication.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsNotAbstract() {
    assertFalse(
        "SketchIDApplication should not be abstract",
        Modifier.isAbstract(SketchIDApplication.class.getModifiers())
    );
  }

  @Test
  public void testClass_HasCorrectPackage() {
    assertEquals(
        "SketchIDApplication should be in correct package",
        "com.mfrankic.sketchid",
        SketchIDApplication.class.getPackage().getName()
    );
  }

  @Test
  public void testOnCreate_MethodExists() {
    try {
      Method onCreateMethod = SketchIDApplication.class.getDeclaredMethod("onCreate");
      assertNotNull("onCreate method should exist", onCreateMethod);
      assertTrue("onCreate should be public", Modifier.isPublic(onCreateMethod.getModifiers()));
      assertEquals("onCreate should return void", void.class, onCreateMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("onCreate method should exist");
    }
  }

  @Test
  public void testClass_HasCorrectMethodCount() {
    Method[] declaredMethods = SketchIDApplication.class.getDeclaredMethods();

    // Should have onCreate method
    assertTrue("Should have at least 1 declared method", declaredMethods.length >= 1);

    // Should not have excessive methods for this simple application class
    assertTrue("Should not have excessive methods", declaredMethods.length <= 5);
  }

  @Test
  public void testClass_ModifiersCorrect() {
    int modifiers = SketchIDApplication.class.getModifiers();

    assertTrue("Class should be public", Modifier.isPublic(modifiers));
    assertFalse("Class should not be final", Modifier.isFinal(modifiers));
    assertFalse("Class should not be abstract", Modifier.isAbstract(modifiers));
    assertFalse("Class should not be interface", Modifier.isInterface(modifiers));
    assertFalse("Class should not be static", Modifier.isStatic(modifiers));
  }

  @Test
  public void testApplicationDesignPattern() {
    // Test that SketchIDApplication follows proper Android application design patterns

    // Should be designed for extension
    assertFalse(
        "Should allow subclassing",
        Modifier.isFinal(SketchIDApplication.class.getModifiers())
    );

    // Should have onCreate method for initialization
    try {
      Method onCreateMethod = SketchIDApplication.class.getDeclaredMethod("onCreate");
      assertNotNull("Should have onCreate for initialization", onCreateMethod);
      assertTrue("onCreate should be public", Modifier.isPublic(onCreateMethod.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("Should have onCreate method: " + e.getMessage());
    }
  }

  @Test
  public void testClass_HasNoFields() {
    // Application class should typically not have instance fields
    java.lang.reflect.Field[] declaredFields = SketchIDApplication.class.getDeclaredFields();
    assertEquals("Application class should not have instance fields", 0, declaredFields.length);
  }

  @Test
  public void testClass_HasDefaultConstructor() {
    // Should have a default constructor for Android framework
    try {
      java.lang.reflect.Constructor<SketchIDApplication> constructor
          = SketchIDApplication.class.getDeclaredConstructor();
      assertNotNull("Should have default constructor", constructor);
      assertTrue("Constructor should be public", Modifier.isPublic(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("Should have default constructor: " + e.getMessage());
    }
  }
} 
