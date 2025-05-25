package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ImageDaoTest {

  @Test
  public void testInterface_IsInterface() {
    assertTrue("ImageDao should be an interface", ImageDao.class.isInterface());
  }

  @Test
  public void testInterface_IsPublic() {
    assertTrue("ImageDao should be public", Modifier.isPublic(ImageDao.class.getModifiers()));
  }

  @Test
  public void testInterface_HasCorrectPackage() {
    assertEquals(
        "ImageDao should be in correct package",
        "com.mfrankic.sketchid",
        ImageDao.class.getPackage().getName()
    );
  }

  @Test
  public void testInsertAll_MethodExists() {
    try {
      Method insertAllMethod = ImageDao.class.getMethod("insertAll", List.class);
      assertNotNull("insertAll method should exist", insertAllMethod);
      assertEquals("insertAll should return void", void.class, insertAllMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("insertAll method should exist");
    }
  }

  @Test
  public void testInsertImage_MethodExists() {
    try {
      Method insertImageMethod = ImageDao.class.getMethod("insertImage", Image.class);
      assertNotNull("insertImage method should exist", insertImageMethod);
      assertEquals("insertImage should return long", long.class, insertImageMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("insertImage method should exist");
    }
  }

  @Test
  public void testUpdateImage_MethodExists() {
    try {
      Method updateImageMethod = ImageDao.class.getMethod("updateImage", Image.class);
      assertNotNull("updateImage method should exist", updateImageMethod);
      assertEquals("updateImage should return void", void.class, updateImageMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("updateImage method should exist");
    }
  }

  @Test
  public void testGetAllImages_MethodExists() {
    try {
      Method getAllImagesMethod = ImageDao.class.getMethod("getAllImages");
      assertNotNull("getAllImages method should exist", getAllImagesMethod);
      assertEquals(
          "getAllImages should return List",
          List.class,
          getAllImagesMethod.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("getAllImages method should exist");
    }
  }

  @Test
  public void testGetImagesBySource_MethodExists() {
    try {
      Method getImagesBySourceMethod = ImageDao.class.getMethod("getImagesBySource", String.class);
      assertNotNull("getImagesBySource method should exist", getImagesBySourceMethod);
      assertEquals(
          "getImagesBySource should return List",
          List.class,
          getImagesBySourceMethod.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("getImagesBySource method should exist");
    }
  }

  @Test
  public void testDeleteImage_MethodExists() {
    try {
      Method deleteImageMethod = ImageDao.class.getMethod("deleteImage", Image.class);
      assertNotNull("deleteImage method should exist", deleteImageMethod);
      assertEquals("deleteImage should return void", void.class, deleteImageMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("deleteImage method should exist");
    }
  }

  @Test
  public void testDeleteInvalidCustomImages_MethodExists() {
    try {
      Method deleteInvalidCustomImagesMethod
          = ImageDao.class.getMethod("deleteInvalidCustomImages");
      assertNotNull(
          "deleteInvalidCustomImages method should exist",
          deleteInvalidCustomImagesMethod
      );
      assertEquals(
          "deleteInvalidCustomImages should return void",
          void.class,
          deleteInvalidCustomImagesMethod.getReturnType()
      );
    } catch (NoSuchMethodException e) {
      fail("deleteInvalidCustomImages method should exist");
    }
  }

  @Test
  public void testInterface_HasCorrectMethodCount() {
    Method[] methods = ImageDao.class.getMethods();
    int nonObjectMethods = 0;
    for (Method method : methods) {
      if (method.getDeclaringClass() == ImageDao.class) {
        nonObjectMethods++;
      }
    }
    assertEquals("ImageDao should have exactly 7 methods", 7, nonObjectMethods);
  }

  @Test
  public void testParameterTypes() {
    try {
      Method insertAllMethod = ImageDao.class.getMethod("insertAll", List.class);
      assertEquals(
          "insertAll parameter should be List",
          List.class,
          insertAllMethod.getParameterTypes()[0]
      );

      Method insertImageMethod = ImageDao.class.getMethod("insertImage", Image.class);
      assertEquals(
          "insertImage parameter should be Image",
          Image.class,
          insertImageMethod.getParameterTypes()[0]
      );

      Method updateImageMethod = ImageDao.class.getMethod("updateImage", Image.class);
      assertEquals(
          "updateImage parameter should be Image",
          Image.class,
          updateImageMethod.getParameterTypes()[0]
      );

      Method getImagesBySourceMethod = ImageDao.class.getMethod("getImagesBySource", String.class);
      assertEquals(
          "getImagesBySource parameter should be String",
          String.class,
          getImagesBySourceMethod.getParameterTypes()[0]
      );

      Method deleteImageMethod = ImageDao.class.getMethod("deleteImage", Image.class);
      assertEquals(
          "deleteImage parameter should be Image",
          Image.class,
          deleteImageMethod.getParameterTypes()[0]
      );

      Method deleteInvalidCustomImagesMethod
          = ImageDao.class.getMethod("deleteInvalidCustomImages");
      assertEquals(
          "deleteInvalidCustomImages should have no parameters",
          0,
          deleteInvalidCustomImagesMethod.getParameterTypes().length
      );
    } catch (NoSuchMethodException e) {
      fail("Methods should exist with correct parameters");
    }
  }

  @Test
  public void testInterface_ExtendsNoOtherInterfaces() {
    Class<?>[] superInterfaces = ImageDao.class.getInterfaces();
    assertEquals("ImageDao should not extend other interfaces", 0, superInterfaces.length);
  }

  @Test
  public void testMethodNaming() {
    // Test that method names follow DAO patterns
    try {
      ImageDao.class.getMethod("insertAll", List.class);
      ImageDao.class.getMethod("insertImage", Image.class);
      ImageDao.class.getMethod("updateImage", Image.class);
      ImageDao.class.getMethod("getAllImages");
      ImageDao.class.getMethod("getImagesBySource", String.class);
      ImageDao.class.getMethod("deleteImage", Image.class);
      ImageDao.class.getMethod("deleteInvalidCustomImages");

      assertTrue("DAO methods should follow naming conventions", true);
    } catch (NoSuchMethodException e) {
      fail("DAO methods should exist with proper naming: " + e.getMessage());
    }
  }

  @Test
  public void testCrudOperations() {
    // Test that the interface provides CRUD operations
    try {
      // Create operations
      ImageDao.class.getMethod("insertAll", List.class);
      ImageDao.class.getMethod("insertImage", Image.class);

      // Read operations
      ImageDao.class.getMethod("getAllImages");
      ImageDao.class.getMethod("getImagesBySource", String.class);

      // Update operations
      ImageDao.class.getMethod("updateImage", Image.class);

      // Delete operations
      ImageDao.class.getMethod("deleteImage", Image.class);
      ImageDao.class.getMethod("deleteInvalidCustomImages");

      assertTrue("Should provide comprehensive CRUD operations", true);
    } catch (NoSuchMethodException e) {
      fail("Should provide CRUD operations: " + e.getMessage());
    }
  }

  @Test
  public void testDaoStructure() {
    // Test that the interface follows DAO structural patterns
    assertTrue("Should be an interface for Room DAO", ImageDao.class.isInterface());
    assertTrue(
        "Should be public for Room access",
        Modifier.isPublic(ImageDao.class.getModifiers())
    );

    // Should have appropriate return types
    try {
      Method insertImageMethod = ImageDao.class.getMethod("insertImage", Image.class);
      assertEquals("insertImage should return ID", long.class, insertImageMethod.getReturnType());

      Method getAllImagesMethod = ImageDao.class.getMethod("getAllImages");
      assertEquals(
          "getAllImages should return List",
          List.class,
          getAllImagesMethod.getReturnType()
      );

      assertTrue("Should have appropriate return types", true);
    } catch (NoSuchMethodException e) {
      fail("Should have proper method signatures: " + e.getMessage());
    }
  }
} 
