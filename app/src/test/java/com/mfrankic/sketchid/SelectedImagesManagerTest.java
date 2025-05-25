package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Unit tests for the SelectedImagesManager utility class
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectedImagesManagerTest {

  @Test
  public void testConstructor_ThrowsIllegalStateException() {
    try {
      Constructor<SelectedImagesManager> constructor
          = SelectedImagesManager.class.getDeclaredConstructor();
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
    assertTrue(
        "SelectedImagesManager should be final",
        Modifier.isFinal(SelectedImagesManager.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsUtilityClass() {
    // Verify that SelectedImagesManager is designed as a utility class
    assertTrue(
        "SelectedImagesManager should be public",
        Modifier.isPublic(SelectedImagesManager.class.getModifiers())
    );

    // Check that constructor is private
    try {
      Constructor<SelectedImagesManager> constructor
          = SelectedImagesManager.class.getDeclaredConstructor();
      assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("SelectedImagesManager should have a default constructor");
    }

    // Verify all public methods are static
    Method[] methods = SelectedImagesManager.class.getDeclaredMethods();
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
  public void testGetSelectedImages_MethodExists() {
    // Verify the getSelectedImages method signature exists
    try {
      Method method = SelectedImagesManager.class.getMethod(
          "getSelectedImages",
          android.content.Context.class
      );
      assertTrue(
          "getSelectedImages method should be static",
          Modifier.isStatic(method.getModifiers())
      );
      assertTrue(
          "getSelectedImages method should be public",
          Modifier.isPublic(method.getModifiers())
      );
      assertEquals(
          "getSelectedImages method should return Set",
          java.util.Set.class,
          method.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("getSelectedImages method should exist with correct signature");
    }
  }

  @Test
  public void testSaveSelectedImages_MethodExists() {
    // Verify the saveSelectedImages method signature exists
    try {
      Method method = SelectedImagesManager.class.getMethod(
          "saveSelectedImages",
          android.content.Context.class,
          java.util.Set.class
      );
      assertTrue(
          "saveSelectedImages method should be static",
          Modifier.isStatic(method.getModifiers())
      );
      assertTrue(
          "saveSelectedImages method should be public",
          Modifier.isPublic(method.getModifiers())
      );
      assertEquals(
          "saveSelectedImages method should return void",
          void.class,
          method.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("saveSelectedImages method should exist with correct signature");
    }
  }

  @Test
  public void testClass_HasCorrectPackage() {
    assertEquals(
        "SelectedImagesManager should be in correct package",
        "com.mfrankic.sketchid",
        SelectedImagesManager.class.getPackage().getName()
    );
  }

  @Test
  public void testClass_HasCorrectName() {
    assertEquals(
        "Class should have correct simple name",
        "SelectedImagesManager",
        SelectedImagesManager.class.getSimpleName()
    );
  }

  @Test
  public void testClass_HasNoPublicFields() {
    // Utility classes should not have public fields
    java.lang.reflect.Field[] fields = SelectedImagesManager.class.getFields();
    assertEquals("Utility class should have no public fields", 0, fields.length);
  }

  @Test
  public void testClass_HasCorrectMethodCount() {
    // Should have exactly 2 public methods: getSelectedImages and saveSelectedImages
    Method[] publicMethods = SelectedImagesManager.class.getMethods();
    int utilityMethods = 0;

    for (Method method : publicMethods) {
      // Count only methods declared in this class (not inherited from Object)
      if (method.getDeclaringClass() == SelectedImagesManager.class) {
        utilityMethods++;
      }
    }

    assertEquals("Should have exactly 2 utility methods", 2, utilityMethods);
  }

  @Test
  public void testGetSelectedImages_ParameterTypes() {
    try {
      Method method = SelectedImagesManager.class.getMethod(
          "getSelectedImages",
          android.content.Context.class
      );

      Class<?>[] parameterTypes = method.getParameterTypes();
      assertEquals("Should have exactly one parameter", 1, parameterTypes.length);
      assertEquals(
          "Parameter should be Context type",
          android.content.Context.class,
          parameterTypes[0]
      );
    } catch (NoSuchMethodException e) {
      fail("getSelectedImages method should exist");
    }
  }

  @Test
  public void testSaveSelectedImages_ParameterTypes() {
    try {
      Method method = SelectedImagesManager.class.getMethod(
          "saveSelectedImages",
          android.content.Context.class,
          java.util.Set.class
      );

      Class<?>[] parameterTypes = method.getParameterTypes();
      assertEquals("Should have exactly two parameters", 2, parameterTypes.length);
      assertEquals(
          "First parameter should be Context type",
          android.content.Context.class,
          parameterTypes[0]
      );
      assertEquals("Second parameter should be Set type", java.util.Set.class, parameterTypes[1]);
    } catch (NoSuchMethodException e) {
      fail("saveSelectedImages method should exist");
    }
  }

  @Test
  public void testClass_ImplementsNoInterfaces() {
    // Utility classes typically don't implement interfaces
    Class<?>[] interfaces = SelectedImagesManager.class.getInterfaces();
    assertEquals("Utility class should implement no interfaces", 0, interfaces.length);
  }

  @Test
  public void testClass_ExtendsObject() {
    // Should extend only Object
    assertEquals(
        "Should extend only Object",
        Object.class,
        SelectedImagesManager.class.getSuperclass()
    );
  }

  @Test
  public void testClass_HasCorrectModifiers() {
    int modifiers = SelectedImagesManager.class.getModifiers();

    assertTrue("Class should be public", Modifier.isPublic(modifiers));
    assertTrue("Class should be final", Modifier.isFinal(modifiers));
    assertFalse("Class should not be abstract", Modifier.isAbstract(modifiers));
    assertFalse("Class should not be interface", Modifier.isInterface(modifiers));
  }

  @Test
  public void testMethodsAreNotSynchronized() {
    // Check that utility methods are not unnecessarily synchronized
    try {
      Method getMethod = SelectedImagesManager.class.getMethod(
          "getSelectedImages",
          android.content.Context.class
      );
      Method saveMethod = SelectedImagesManager.class.getMethod(
          "saveSelectedImages",
          android.content.Context.class,
          java.util.Set.class
      );

      assertFalse(
          "getSelectedImages should not be synchronized",
          Modifier.isSynchronized(getMethod.getModifiers())
      );
      assertFalse(
          "saveSelectedImages should not be synchronized",
          Modifier.isSynchronized(saveMethod.getModifiers())
      );
    } catch (NoSuchMethodException e) {
      fail("Methods should exist");
    }
  }

  @Test
  public void testClass_CannotBeInstantiatedNormally() {
    // Verify that normal instantiation is not possible
    Constructor<?>[] constructors = SelectedImagesManager.class.getConstructors();
    assertEquals("Should have no public constructors", 0, constructors.length);
  }

  @Test
  public void testClass_HasPrivateConstructor() {
    Constructor<?>[] allConstructors = SelectedImagesManager.class.getDeclaredConstructors();
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
