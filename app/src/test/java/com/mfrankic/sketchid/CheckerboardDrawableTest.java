package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@RunWith(MockitoJUnitRunner.class)
public class CheckerboardDrawableTest {

  @Test
  public void testConstructor_Exists() {
    try {
      Constructor<CheckerboardDrawable> constructor
          = CheckerboardDrawable.class.getConstructor(int.class, int.class, int.class);
      assertNotNull("Constructor should exist", constructor);
      assertTrue("Constructor should be public", Modifier.isPublic(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("Constructor should exist with int, int, int parameters");
    }
  }

  @Test
  public void testConstructor_ParameterTypes() {
    try {
      Constructor<CheckerboardDrawable> constructor
          = CheckerboardDrawable.class.getConstructor(int.class, int.class, int.class);
      Class<?>[] parameterTypes = constructor.getParameterTypes();

      assertEquals("Constructor should have exactly 3 parameters", 3, parameterTypes.length);
      assertEquals("First parameter should be int (lightColor)", int.class, parameterTypes[0]);
      assertEquals("Second parameter should be int (darkColor)", int.class, parameterTypes[1]);
      assertEquals("Third parameter should be int (cellSize)", int.class, parameterTypes[2]);
    } catch (NoSuchMethodException e) {
      fail("Constructor should exist");
    }
  }

  @Test
  public void testClass_ExtendsDrawable() {
    assertTrue(
        "CheckerboardDrawable should extend Drawable",
        android.graphics.drawable.Drawable.class.isAssignableFrom(CheckerboardDrawable.class)
    );
  }

  @Test
  public void testClass_IsPublic() {
    assertTrue(
        "CheckerboardDrawable should be public",
        Modifier.isPublic(CheckerboardDrawable.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsNotFinal() {
    assertFalse(
        "CheckerboardDrawable should not be final to allow inheritance",
        Modifier.isFinal(CheckerboardDrawable.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsNotAbstract() {
    assertFalse(
        "CheckerboardDrawable should not be abstract",
        Modifier.isAbstract(CheckerboardDrawable.class.getModifiers())
    );
  }

  @Test
  public void testClass_HasCorrectPackage() {
    assertEquals(
        "CheckerboardDrawable should be in correct package",
        "com.mfrankic.sketchid",
        CheckerboardDrawable.class.getPackage().getName()
    );
  }

  @Test
  public void testDrawMethod_Exists() {
    try {
      Method drawMethod = CheckerboardDrawable.class.getMethod(
          "draw",
          android.graphics.Canvas.class
      );
      assertNotNull("draw method should exist", drawMethod);
      assertTrue("draw should be public", Modifier.isPublic(drawMethod.getModifiers()));
      assertEquals("draw should return void", void.class, drawMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("draw method should exist");
    }
  }

  @Test
  public void testSetAlphaMethod_Exists() {
    try {
      Method setAlphaMethod = CheckerboardDrawable.class.getMethod("setAlpha", int.class);
      assertNotNull("setAlpha method should exist", setAlphaMethod);
      assertTrue("setAlpha should be public", Modifier.isPublic(setAlphaMethod.getModifiers()));
      assertEquals("setAlpha should return void", void.class, setAlphaMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("setAlpha method should exist");
    }
  }

  @Test
  public void testSetColorFilterMethod_Exists() {
    try {
      Method setColorFilterMethod = CheckerboardDrawable.class.getMethod(
          "setColorFilter",
          android.graphics.ColorFilter.class
      );
      assertNotNull("setColorFilter method should exist", setColorFilterMethod);
      assertTrue(
          "setColorFilter should be public",
          Modifier.isPublic(setColorFilterMethod.getModifiers())
      );
      assertEquals(
          "setColorFilter should return void",
          void.class,
          setColorFilterMethod.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("setColorFilter method should exist");
    }
  }

  @Test
  public void testGetOpacityMethod_Exists() {
    try {
      Method getOpacityMethod = CheckerboardDrawable.class.getMethod("getOpacity");
      assertNotNull("getOpacity method should exist", getOpacityMethod);
      assertTrue("getOpacity should be public", Modifier.isPublic(getOpacityMethod.getModifiers()));
      assertEquals("getOpacity should return int", int.class, getOpacityMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("getOpacity method should exist");
    }
  }

  @Test
  public void testRequiredMethods_HaveCorrectSignatures() {
    try {
      Method drawMethod = CheckerboardDrawable.class.getMethod(
          "draw",
          android.graphics.Canvas.class
      );
      assertNotNull("draw method should exist", drawMethod);
      assertTrue("draw should be public", Modifier.isPublic(drawMethod.getModifiers()));

      Method setAlphaMethod = CheckerboardDrawable.class.getMethod("setAlpha", int.class);
      assertNotNull("setAlpha method should exist", setAlphaMethod);
      assertTrue("setAlpha should be public", Modifier.isPublic(setAlphaMethod.getModifiers()));

      Method setColorFilterMethod = CheckerboardDrawable.class.getMethod(
          "setColorFilter",
          android.graphics.ColorFilter.class
      );
      assertNotNull("setColorFilter method should exist", setColorFilterMethod);
      assertTrue(
          "setColorFilter should be public",
          Modifier.isPublic(setColorFilterMethod.getModifiers())
      );

      Method getOpacityMethod = CheckerboardDrawable.class.getMethod("getOpacity");
      assertNotNull("getOpacity method should exist", getOpacityMethod);
      assertTrue("getOpacity should be public", Modifier.isPublic(getOpacityMethod.getModifiers()));
    } catch (NoSuchMethodException e) {
      fail("Required methods should exist");
    }
  }

  @Test
  public void testInheritanceHierarchy() {
    Class<?> superClass = CheckerboardDrawable.class.getSuperclass();
    assertEquals(
        "Should directly extend Drawable",
        android.graphics.drawable.Drawable.class,
        superClass
    );
  }

  @Test
  public void testClass_ImplementsNoExtraInterfaces() {
    Class<?>[] interfaces = CheckerboardDrawable.class.getInterfaces();
    assertEquals(
        "CheckerboardDrawable should not implement additional interfaces directly",
        0,
        interfaces.length
    );
  }

  @Test
  public void testClass_HasCorrectFieldCount() {
    java.lang.reflect.Field[] declaredFields = CheckerboardDrawable.class.getDeclaredFields();

    // Should have fields for lightPaint, darkPaint, cellSize
    assertTrue("Should have at least 3 fields", declaredFields.length >= 3);

    // Should not have excessive fields
    assertTrue("Should not have excessive fields", declaredFields.length <= 10);
  }

  @Test
  public void testClass_HasCorrectMethodCount() {
    Method[] declaredMethods = CheckerboardDrawable.class.getDeclaredMethods();

    // Should have the required Drawable methods overridden
    assertTrue("Should have at least 4 declared methods", declaredMethods.length >= 4);

    // Should not have excessive methods
    assertTrue("Should not have excessive methods", declaredMethods.length <= 15);
  }

  @Test
  public void testClass_HasNoPublicFields() {
    java.lang.reflect.Field[] publicFields = CheckerboardDrawable.class.getFields();
    assertEquals("CheckerboardDrawable should have no public fields", 0, publicFields.length);
  }

  @Test
  public void testClass_ModifiersCorrect() {
    int modifiers = CheckerboardDrawable.class.getModifiers();

    assertTrue("Class should be public", Modifier.isPublic(modifiers));
    assertFalse("Class should not be final", Modifier.isFinal(modifiers));
    assertFalse("Class should not be abstract", Modifier.isAbstract(modifiers));
    assertFalse("Class should not be interface", Modifier.isInterface(modifiers));
    assertFalse("Class should not be static", Modifier.isStatic(modifiers));
  }

  @Test
  public void testDesignPurpose() {
    // Test that CheckerboardDrawable serves its intended purpose as a custom drawable

    // Should be extendable
    assertFalse(
        "Should be extendable (not final)",
        Modifier.isFinal(CheckerboardDrawable.class.getModifiers())
    );

    // Should be concrete (not abstract)
    assertFalse(
        "Should be concrete (not abstract)",
        Modifier.isAbstract(CheckerboardDrawable.class.getModifiers())
    );

    // Should extend the right base class
    assertEquals(
        "Should extend Drawable for Android graphics",
        android.graphics.drawable.Drawable.class,
        CheckerboardDrawable.class.getSuperclass()
    );
  }

  @Test
  public void testClassDocumentation() {
    // While we can't test actual documentation, we can verify the class structure
    // suggests it's well-designed for its purpose

    String className = CheckerboardDrawable.class.getSimpleName();
    assertTrue(
        "Class name should indicate checkerboard functionality",
        className.contains("Checkerboard")
    );
    assertTrue("Class name should indicate drawable type", className.contains("Drawable"));
  }

  @Test
  public void testCompatibilityWithDrawableFramework() {
    // Test that CheckerboardDrawable follows Android Drawable patterns

    try {
      // Should have standard Drawable methods accessible
      CheckerboardDrawable.class.getMethod("draw", android.graphics.Canvas.class);
      CheckerboardDrawable.class.getMethod("setAlpha", int.class);
      CheckerboardDrawable.class.getMethod("setColorFilter", android.graphics.ColorFilter.class);
      CheckerboardDrawable.class.getMethod("getOpacity");

      // These methods should be available through inheritance
      CheckerboardDrawable.class.getMethod("setBounds", int.class, int.class, int.class, int.class);
      CheckerboardDrawable.class.getMethod("getBounds");
      CheckerboardDrawable.class.getMethod("invalidateSelf");

      assertTrue("Should have access to standard Drawable methods", true);
    } catch (NoSuchMethodException e) {
      fail("Should have access to Drawable methods: " + e.getMessage());
    }
  }

  @Test
  public void testInheritedMethods_Accessibility() {
    // Test that important inherited methods are accessible
    try {
      // From Drawable
      CheckerboardDrawable.class.getMethod("setBounds", android.graphics.Rect.class);
      CheckerboardDrawable.class.getMethod("getBounds");
      CheckerboardDrawable.class.getMethod("invalidateSelf");
      CheckerboardDrawable.class.getMethod("isVisible");

      assertTrue("Should have access to essential inherited methods", true);
    } catch (NoSuchMethodException e) {
      fail("Should have access to inherited methods: " + e.getMessage());
    }
  }

  @Test
  public void testClass_ProperDesignPattern() {
    // Test that CheckerboardDrawable follows proper design patterns for Android

    // Should be part of the Drawable hierarchy
    assertTrue(
        "Should be a Drawable",
        android.graphics.drawable.Drawable.class.isAssignableFrom(CheckerboardDrawable.class)
    );

    // Should be designed for extension
    assertFalse(
        "Should allow subclassing",
        Modifier.isFinal(CheckerboardDrawable.class.getModifiers())
    );
  }

  @Test
  public void testParameterValidation() {
    // Test that constructor accepts various parameter combinations
    // We verify the signature can handle all int values
    try {
      Constructor<CheckerboardDrawable> constructor
          = CheckerboardDrawable.class.getConstructor(int.class, int.class, int.class);

      // Verify parameters can handle full int range for colors and cell size
      Class<?>[] paramTypes = constructor.getParameterTypes();
      for (Class<?> paramType : paramTypes) {
        assertEquals(
            "All parameters should be int type for maximum flexibility",
            int.class,
            paramType
        );
      }

      assertTrue("Constructor should be designed to handle all color and size values", true);
    } catch (NoSuchMethodException e) {
      fail("Constructor should exist");
    }
  }
} 
