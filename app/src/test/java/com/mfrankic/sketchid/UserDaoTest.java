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
public class UserDaoTest {

  @Test
  public void testInterface_IsInterface() {
    assertTrue("UserDao should be an interface", UserDao.class.isInterface());
  }

  @Test
  public void testInterface_IsPublic() {
    assertTrue("UserDao should be public", Modifier.isPublic(UserDao.class.getModifiers()));
  }

  @Test
  public void testInterface_HasCorrectPackage() {
    assertEquals(
        "UserDao should be in correct package",
        "com.mfrankic.sketchid",
        UserDao.class.getPackage().getName()
    );
  }

  @Test
  public void testInsertUser_MethodExists() {
    try {
      Method insertUserMethod = UserDao.class.getMethod("insertUser", User.class);
      assertNotNull("insertUser method should exist", insertUserMethod);
      assertEquals("insertUser should return long", long.class, insertUserMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("insertUser method should exist");
    }
  }

  @Test
  public void testDeleteUser_MethodExists() {
    try {
      Method deleteUserMethod = UserDao.class.getMethod("deleteUser", long.class);
      assertNotNull("deleteUser method should exist", deleteUserMethod);
      assertEquals("deleteUser should return int", int.class, deleteUserMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("deleteUser method should exist");
    }
  }

  @Test
  public void testGetUserByID_MethodExists() {
    try {
      Method getUserByIDMethod = UserDao.class.getMethod("getUserByID", long.class);
      assertNotNull("getUserByID method should exist", getUserByIDMethod);
      assertEquals("getUserByID should return User", User.class, getUserByIDMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("getUserByID method should exist");
    }
  }

  @Test
  public void testGetAllUsers_MethodExists() {
    try {
      Method getAllUsersMethod = UserDao.class.getMethod("getAllUsers");
      assertNotNull("getAllUsers method should exist", getAllUsersMethod);
      assertEquals("getAllUsers should return List", List.class, getAllUsersMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("getAllUsers method should exist");
    }
  }

  @Test
  public void testInterface_HasCorrectMethodCount() {
    Method[] methods = UserDao.class.getMethods();
    int nonObjectMethods = 0;
    for (Method method : methods) {
      if (method.getDeclaringClass() == UserDao.class) {
        nonObjectMethods++;
      }
    }
    assertEquals("UserDao should have exactly 4 methods", 4, nonObjectMethods);
  }

  @Test
  public void testParameterTypes() {
    try {
      Method insertUserMethod = UserDao.class.getMethod("insertUser", User.class);
      assertEquals(
          "insertUser parameter should be User",
          User.class,
          insertUserMethod.getParameterTypes()[0]
      );

      Method deleteUserMethod = UserDao.class.getMethod("deleteUser", long.class);
      assertEquals(
          "deleteUser parameter should be long",
          long.class,
          deleteUserMethod.getParameterTypes()[0]
      );

      Method getUserByIDMethod = UserDao.class.getMethod("getUserByID", long.class);
      assertEquals(
          "getUserByID parameter should be long",
          long.class,
          getUserByIDMethod.getParameterTypes()[0]
      );

      Method getAllUsersMethod = UserDao.class.getMethod("getAllUsers");
      assertEquals(
          "getAllUsers should have no parameters",
          0,
          getAllUsersMethod.getParameterTypes().length
      );
    } catch (NoSuchMethodException e) {
      fail("Methods should exist with correct parameters");
    }
  }

  @Test
  public void testInterface_ExtendsNoOtherInterfaces() {
    Class<?>[] superInterfaces = UserDao.class.getInterfaces();
    assertEquals("UserDao should not extend other interfaces", 0, superInterfaces.length);
  }

  @Test
  public void testMethodNaming() {
    // Test that method names follow DAO patterns
    try {
      UserDao.class.getMethod("insertUser", User.class);
      UserDao.class.getMethod("deleteUser", long.class);
      UserDao.class.getMethod("getUserByID", long.class);
      UserDao.class.getMethod("getAllUsers");

      assertTrue("DAO methods should follow naming conventions", true);
    } catch (NoSuchMethodException e) {
      fail("DAO methods should exist with proper naming: " + e.getMessage());
    }
  }

  @Test
  public void testReturnTypes() {
    try {
      Method insertUserMethod = UserDao.class.getMethod("insertUser", User.class);
      assertEquals(
          "insertUser should return long for ID",
          long.class,
          insertUserMethod.getReturnType()
      );

      Method deleteUserMethod = UserDao.class.getMethod("deleteUser", long.class);
      assertEquals(
          "deleteUser should return int for affected rows",
          int.class,
          deleteUserMethod.getReturnType()
      );

      Method getUserByIDMethod = UserDao.class.getMethod("getUserByID", long.class);
      assertEquals(
          "getUserByID should return User object",
          User.class,
          getUserByIDMethod.getReturnType()
      );

      Method getAllUsersMethod = UserDao.class.getMethod("getAllUsers");
      assertEquals("getAllUsers should return List", List.class, getAllUsersMethod.getReturnType());
    } catch (NoSuchMethodException e) {
      fail("Methods should exist");
    }
  }

  @Test
  public void testDaoStructure() {
    // Test that the interface follows DAO structural patterns
    assertTrue("Should be an interface for Room DAO", UserDao.class.isInterface());
    assertTrue("Should be public for Room access", Modifier.isPublic(UserDao.class.getModifiers()));

    // Should have CRUD operations
    try {
      UserDao.class.getMethod("insertUser", User.class); // Create
      UserDao.class.getMethod("getUserByID", long.class); // Read
      UserDao.class.getMethod("getAllUsers"); // Read all
      UserDao.class.getMethod("deleteUser", long.class); // Delete

      assertTrue("Should provide basic CRUD operations", true);
    } catch (NoSuchMethodException e) {
      fail("Should provide basic CRUD operations: " + e.getMessage());
    }
  }
} 
