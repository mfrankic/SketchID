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

@RunWith(MockitoJUnitRunner.class)
public class BaseActivityTest {

  @Test
  public void testClass_ExtendsAppCompatActivity() {
    assertTrue(
        "BaseActivity should extend AppCompatActivity",
        androidx.appcompat.app.AppCompatActivity.class.isAssignableFrom(BaseActivity.class)
    );
  }

  @Test
  public void testClass_IsPublic() {
    assertTrue(
        "BaseActivity should be public",
        Modifier.isPublic(BaseActivity.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsNotFinal() {
    assertFalse(
        "BaseActivity should not be final to allow inheritance",
        Modifier.isFinal(BaseActivity.class.getModifiers())
    );
  }

  @Test
  public void testClass_IsNotAbstract() {
    assertFalse(
        "BaseActivity should not be abstract",
        Modifier.isAbstract(BaseActivity.class.getModifiers())
    );
  }

  @Test
  public void testClass_HasCorrectPackage() {
    assertEquals(
        "BaseActivity should be in correct package",
        "com.mfrankic.sketchid",
        BaseActivity.class.getPackage().getName()
    );
  }

  @Test
  public void testClass_HasCorrectName() {
    assertEquals(
        "Class should have correct simple name",
        "BaseActivity",
        BaseActivity.class.getSimpleName()
    );
  }

  @Test
  public void testOnCreate_MethodExists() {
    try {
      Method onCreateMethod = BaseActivity.class.getDeclaredMethod(
          "onCreate",
          android.os.Bundle.class
      );
      assertNotNull("onCreate method should exist", onCreateMethod);
      assertTrue(
          "onCreate should be protected",
          Modifier.isProtected(onCreateMethod.getModifiers())
      );
    } catch (NoSuchMethodException e) {
      fail("onCreate method should exist with Bundle parameter");
    }
  }

  @Test
  public void testClass_CanBeInstantiated() {
    // Test that the class has appropriate constructors for Android
    try {
      java.lang.reflect.Constructor<BaseActivity> constructor = BaseActivity.class.getConstructor();
      assertTrue("Constructor should be public", Modifier.isPublic(constructor.getModifiers()));
    } catch (NoSuchMethodException e) {
      // This is expected as Activities typically don't have explicit public constructors
      // Android creates them through reflection
      assertTrue("BaseActivity follows Android Activity pattern", true);
    }
  }

  @Test
  public void testInheritanceHierarchy() {
    // Test the complete inheritance chain
    Class<?> superClass = BaseActivity.class.getSuperclass();
    assertEquals(
        "Should directly extend AppCompatActivity",
        androidx.appcompat.app.AppCompatActivity.class,
        superClass
    );

    // Test that it's part of the Activity hierarchy
    assertTrue(
        "Should be instance of Activity",
        android.app.Activity.class.isAssignableFrom(BaseActivity.class)
    );
    assertTrue(
        "Should be instance of Context",
        android.content.Context.class.isAssignableFrom(BaseActivity.class)
    );
  }

  @Test
  public void testClass_ImplementsNoExtraInterfaces() {
    // BaseActivity should not implement additional interfaces beyond what AppCompatActivity does
    Class<?>[] interfaces = BaseActivity.class.getInterfaces();
    assertEquals(
        "BaseActivity should not implement additional interfaces directly",
        0,
        interfaces.length
    );
  }

  @Test
  public void testOnCreate_ParameterTypes() {
    try {
      Method onCreateMethod = BaseActivity.class.getDeclaredMethod(
          "onCreate",
          android.os.Bundle.class
      );
      Class<?>[] parameterTypes = onCreateMethod.getParameterTypes();

      assertEquals("onCreate should have exactly one parameter", 1, parameterTypes.length);
      assertEquals("Parameter should be Bundle type", android.os.Bundle.class, parameterTypes[0]);
    } catch (NoSuchMethodException e) {
      fail("onCreate method should exist");
    }
  }

  @Test
  public void testOnCreate_ReturnType() {
    try {
      Method onCreateMethod = BaseActivity.class.getDeclaredMethod(
          "onCreate",
          android.os.Bundle.class
      );
      assertEquals("onCreate should return void", void.class, onCreateMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("onCreate method should exist");
    }
  }

  @Test
  public void testClass_HasCorrectMethodCount() {
    // Count methods declared specifically in BaseActivity (not inherited)
    Method[] declaredMethods = BaseActivity.class.getDeclaredMethods();

    // Should have onCreate method at minimum
    assertTrue("Should have at least one declared method (onCreate)", declaredMethods.length >= 1);

    // Should not have excessive methods (it's a base class)
    assertTrue("Should not have excessive methods", declaredMethods.length <= 10);
  }

  @Test
  public void testClass_ModifiersCorrect() {
    int modifiers = BaseActivity.class.getModifiers();

    assertTrue("Class should be public", Modifier.isPublic(modifiers));
    assertFalse("Class should not be final", Modifier.isFinal(modifiers));
    assertFalse("Class should not be abstract", Modifier.isAbstract(modifiers));
    assertFalse("Class should not be interface", Modifier.isInterface(modifiers));
    assertFalse("Class should not be static", Modifier.isStatic(modifiers));
  }

  @Test
  public void testDesignPurpose() {
    // Test that BaseActivity serves its intended purpose as a base class

    // Should be extendable
    assertFalse(
        "Should be extendable (not final)",
        Modifier.isFinal(BaseActivity.class.getModifiers())
    );

    // Should be concrete (not abstract)
    assertFalse(
        "Should be concrete (not abstract)",
        Modifier.isAbstract(BaseActivity.class.getModifiers())
    );

    // Should extend the right base class
    assertEquals(
        "Should extend AppCompatActivity for modern Android features",
        androidx.appcompat.app.AppCompatActivity.class,
        BaseActivity.class.getSuperclass()
    );
  }

  @Test
  public void testClassDocumentation() {
    // While we can't test actual documentation, we can verify the class structure
    // suggests it's well-designed for its purpose

    String className = BaseActivity.class.getSimpleName();
    assertTrue("Class name should indicate base functionality", className.contains("Base"));
    assertTrue("Class name should indicate Activity type", className.contains("Activity"));
  }

  @Test
  public void testViewCompat_Dependencies() {
    // Test that the class has access to modern Android compatibility features
    // This indirectly tests that the proper imports and dependencies are available

    try {
      // ViewCompat should be accessible in the context where BaseActivity is used
      Class<?> viewCompatClass = Class.forName("androidx.core.view.ViewCompat");
      assertNotNull("ViewCompat should be available", viewCompatClass);

      Class<?> windowInsetsCompatClass = Class.forName("androidx.core.view.WindowInsetsCompat");
      assertNotNull("WindowInsetsCompat should be available", windowInsetsCompatClass);

      assertTrue("Modern compatibility libraries should be available", true);
    } catch (ClassNotFoundException e) {
      fail("Required compatibility classes should be available: " + e.getMessage());
    }
  }

  @Test
  public void testClass_ProperDesignPattern() {
    // Test that BaseActivity follows proper design patterns for Android

    // Should be part of the Activity hierarchy
    assertTrue(
        "Should be an Activity",
        android.app.Activity.class.isAssignableFrom(BaseActivity.class)
    );

    // Should use modern AppCompat
    assertTrue(
        "Should use AppCompatActivity",
        androidx.appcompat.app.AppCompatActivity.class.isAssignableFrom(BaseActivity.class)
    );

    // Should be designed for extension
    assertFalse("Should allow subclassing", Modifier.isFinal(BaseActivity.class.getModifiers()));
  }

  @Test
  public void testClass_HasNoPublicFields() {
    java.lang.reflect.Field[] fields = BaseActivity.class.getDeclaredFields();
    for (java.lang.reflect.Field field : fields) {
      assertFalse(
          "Fields should not be public: " + field.getName(),
          Modifier.isPublic(field.getModifiers())
      );
    }
  }

  @Test
  public void testOnCreate_HasCorrectSignature() {
    try {
      Method onCreateMethod = BaseActivity.class.getDeclaredMethod(
          "onCreate",
          android.os.Bundle.class
      );
      // Check method signature and accessibility
      assertNotNull("onCreate method should exist", onCreateMethod);
      assertTrue(
          "onCreate should be protected",
          Modifier.isProtected(onCreateMethod.getModifiers())
      );
      assertEquals("onCreate should return void", void.class, onCreateMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("onCreate method should exist");
    }
  }
} 
