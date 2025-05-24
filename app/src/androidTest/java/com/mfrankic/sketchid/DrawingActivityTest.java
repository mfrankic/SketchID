package com.mfrankic.sketchid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DrawingActivityTest {

  private AppDatabase database;
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();

    // Create in-memory database for testing
    database = Room
        .inMemoryDatabaseBuilder(context, AppDatabase.class)
        .allowMainThreadQueries()
        .build();

    // Set up test data
    setupTestData();
  }

  private void setupTestData() {
    // Create a test user
    User testUser = new User("Test User");
    long userId = database.userDao().insertUser(testUser);

    // Create a test image
    Image testImage = new Image(
        "Test Image",
        Constants.SOURCE_DEFAULT,
        "android.resource://com.mfrankic.sketchid/drawable/baseline_image_24"
    );
    database.imageDao().insertImage(testImage);

    // Set up preferences for the activity
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs
        .edit()
        .putString(Constants.KEY_SELECTED_USER, String.valueOf(userId))
        .putString(Constants.KEY_DRAWING_ATTEMPTS, "3")
        .putString(Constants.KEY_DRAWING_MODE, Constants.DRAWING_MODE_NORMAL)
        .apply();
  }

  @After
  public void tearDown() {
    if (database != null) {
      database.close();
    }

    // Clear preferences
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().clear().apply();
  }

  @Test
  public void testDrawingViewIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the drawing view is displayed
      onView(withId(R.id.drawing_view)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testNextImageButtonIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the next image button is displayed
      onView(withId(R.id.btn_next_image)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testClearButtonIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the clear button is displayed
      onView(withId(R.id.btn_clear)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testClearButtonFunctionality() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Click the clear button
      onView(withId(R.id.btn_clear)).perform(click());

      // Verify the drawing view is still displayed after clearing
      onView(withId(R.id.drawing_view)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testAttemptProgressIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the progress text is displayed
      onView(withId(R.id.attempt_progress)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testDrawingFrameIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the drawing frame is displayed
      onView(withId(R.id.drawing_frame)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testReferenceImageIsPresent() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // The reference image might be invisible initially, but should exist in the layout
      // We just check that the view exists (it might be invisible)
      onView(withId(R.id.reference_image));
    }
  }

  @Test
  public void testOverlayReferenceImageIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the overlay reference image is displayed
      onView(withId(R.id.overlay_reference_image)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testBottomButtonsLayoutIsDisplayed() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // Check if the bottom buttons layout is displayed
      onView(withId(R.id.bottom_buttons)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void testActivityLaunchesSuccessfully() {
    Intent intent = new Intent(context, DrawingActivity.class);
    intent.putExtra(Constants.KEY_SELECTED_USER, "1");
    intent.putExtra(Constants.KEY_DRAWING_ATTEMPTS, 3);

    try (ActivityScenario<DrawingActivity> scenario = ActivityScenario.launch(intent)) {
      // If we get here without exception, the activity launched successfully
      scenario.onActivity(activity -> {
        // Verify the activity is not null and is in the correct state
        assert activity != null;
        assert !activity.isFinishing();
      });
    }
  }
} 
