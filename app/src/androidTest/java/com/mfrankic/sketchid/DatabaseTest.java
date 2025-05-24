package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

  private AppDatabase database;
  private UserDao userDao;
  private ImageDao imageDao;
  private DrawingDataDao drawingDataDao;

  @Before
  public void createDb() {
    Context context = ApplicationProvider.getApplicationContext();
    database = Room
        .inMemoryDatabaseBuilder(context, AppDatabase.class)
        .allowMainThreadQueries()
        .build();
    userDao = database.userDao();
    imageDao = database.imageDao();
    drawingDataDao = database.drawingDataDao();
  }

  @After
  public void closeDb() {
    database.close();
  }

  @Test
  public void testUserInsertion() {
    User user = new User("Test User");

    long userId = userDao.insertUser(user);

    assertTrue("User ID should be positive", userId > 0);

    User retrievedUser = userDao.getUserByID(userId);
    assertNotNull("Retrieved user should not be null", retrievedUser);
    assertEquals("User name should match", "Test User", retrievedUser.name);
    assertEquals("User ID should match", userId, retrievedUser.id);
  }

  @Test
  public void testUserDeletion() {
    User user = new User("User to Delete");

    long userId = userDao.insertUser(user);
    User userToDelete = userDao.getUserByID(userId);
    assertNotNull("User should exist before deletion", userToDelete);

    int deletedRows = userDao.deleteUser(userId);
    assertEquals("Should delete exactly one row", 1, deletedRows);

    User deletedUser = userDao.getUserByID(userId);
    assertNull("User should not exist after deletion", deletedUser);
  }

  @Test
  public void testImageInsertion() {
    Image image = new Image("Test Image", Constants.SOURCE_CUSTOM, "/test/path/image.jpg");

    long imageId = imageDao.insertImage(image);

    assertTrue("Image ID should be positive", imageId > 0);

    List<Image> allImages = imageDao.getAllImages();
    assertFalse("Should have at least one image", allImages.isEmpty());

    Image retrievedImage = null;
    for (Image img : allImages) {
      if (img.id == imageId) {
        retrievedImage = img;
        break;
      }
    }

    assertNotNull("Retrieved image should not be null", retrievedImage);
    assertEquals("Image name should match", "Test Image", retrievedImage.name);
    assertEquals("Image path should match", "/test/path/image.jpg", retrievedImage.path);
  }

  @Test
  public void testImageDeletion() {
    Image image = new Image("Image to Delete", Constants.SOURCE_CUSTOM, "/test/path/delete.jpg");

    long imageId = imageDao.insertImage(image);

    List<Image> allImages = imageDao.getAllImages();
    Image imageToDelete = null;
    for (Image img : allImages) {
      if (img.id == imageId) {
        imageToDelete = img;
        break;
      }
    }
    assertNotNull("Image should exist before deletion", imageToDelete);

    imageDao.deleteImage(imageToDelete);

    allImages = imageDao.getAllImages();
    boolean imageExists = false;
    for (Image img : allImages) {
      if (img.id == imageId) {
        imageExists = true;
        break;
      }
    }
    assertFalse("Image should not exist after deletion", imageExists);
  }

  @Test
  public void testDrawingDataInsertion() {
    // First create a user and image
    User user = new User("Drawing User");
    long userId = userDao.insertUser(user);

    Image image = new Image("Drawing Image", Constants.SOURCE_CUSTOM, "/test/path/drawing.jpg");
    long imageId = imageDao.insertImage(image);

    // Create drawing data using the builder pattern
    DrawingData drawingData = new DrawingData.Builder()
        .userID((int) userId)
        .imageID((int) imageId)
        .sessionID("test_session")
        .x(100.0f)
        .y(200.0f)
        .time(System.currentTimeMillis())
        .action("ACTION_DOWN")
        .itemType(Item.Type.IMAGE)
        .attempt(1)
        .size(5.0f)
        .pressure(1.0f)
        .orientation(0.0f)
        .build();

    drawingDataDao.insertAll(Collections.singletonList(drawingData));

    // Verify the data was inserted by getting all drawing data for the user
    List<DrawingExportData> retrievedData
        = drawingDataDao.getAllDrawingDataWithUsersAndImagesByUserID(userId);
    assertFalse("Should have drawing data", retrievedData.isEmpty());

    DrawingExportData exportData = retrievedData.get(0);
    assertEquals("User ID should match", userId, exportData.getUserID());
    assertEquals("Image ID should match", imageId, exportData.getImageID());
    assertEquals("Session ID should match", "test_session", exportData.getSessionID());
    assertEquals("X coordinate should match", 100.0f, exportData.getX(), 0.01f);
    assertEquals("Y coordinate should match", 200.0f, exportData.getY(), 0.01f);
  }

  @Test
  public void testGetAllUsers() {
    User user1 = new User("User 1");
    User user2 = new User("User 2");

    userDao.insertUser(user1);
    userDao.insertUser(user2);

    List<User> allUsers = userDao.getAllUsers();
    assertTrue("Should have at least 2 users", allUsers.size() >= 2);

    boolean foundUser1 = false;
    boolean foundUser2 = false;
    for (User user : allUsers) {
      if ("User 1".equals(user.name)) foundUser1 = true;
      if ("User 2".equals(user.name)) foundUser2 = true;
    }
    assertTrue("Should find User 1", foundUser1);
    assertTrue("Should find User 2", foundUser2);
  }

  @Test
  public void testGetImagesBySource() {
    Image customImage = new Image("Custom Image", Constants.SOURCE_CUSTOM, "/custom/path.jpg");
    Image defaultImage = new Image("Default Image", Constants.SOURCE_DEFAULT, "/default/path.jpg");

    imageDao.insertImage(customImage);
    imageDao.insertImage(defaultImage);

    List<Image> customImages = imageDao.getImagesBySource(Constants.SOURCE_CUSTOM);
    List<Image> defaultImages = imageDao.getImagesBySource(Constants.SOURCE_DEFAULT);

    assertFalse("Should have custom images", customImages.isEmpty());
    assertFalse("Should have default images", defaultImages.isEmpty());

    // Verify the images are in the correct source lists
    boolean foundCustom = false;
    boolean foundDefault = false;
    for (Image img : customImages) {
      if ("Custom Image".equals(img.name)) {
        foundCustom = true;
        break;
      }
    }
    for (Image img : defaultImages) {
      if ("Default Image".equals(img.name)) {
        foundDefault = true;
        break;
      }
    }
    assertTrue("Should find custom image in custom list", foundCustom);
    assertTrue("Should find default image in default list", foundDefault);
  }

  @Test
  public void testInsertMultipleImages() {
    Image image1 = new Image("Batch Image 1", Constants.SOURCE_CUSTOM, "/batch/1.jpg");
    Image image2 = new Image("Batch Image 2", Constants.SOURCE_CUSTOM, "/batch/2.jpg");

    imageDao.insertAll(Arrays.asList(image1, image2));

    List<Image> allImages = imageDao.getAllImages();
    assertTrue("Should have at least 2 images", allImages.size() >= 2);

    boolean foundImage1 = false;
    boolean foundImage2 = false;
    for (Image img : allImages) {
      if ("Batch Image 1".equals(img.name)) foundImage1 = true;
      if ("Batch Image 2".equals(img.name)) foundImage2 = true;
    }
    assertTrue("Should find batch image 1", foundImage1);
    assertTrue("Should find batch image 2", foundImage2);
  }
} 
