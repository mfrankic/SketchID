package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for UserProgressManager.UserProgress inner class.
 * Tests constructor, getters, and data integrity.
 */
public class UserProgressTest {

  @Test
  public void testUserProgressConstructor() {
    long userId = 123L;
    int itemIndex = 5;
    int itemAttempt = 3;
    String userName = "TestUser";
    String sessionId = "session_abc123";

    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        userId,
                                                                                     itemIndex,
                                                                                     itemAttempt,
                                                                                     userName,
                                                                                     sessionId
    );

    assertNotNull("UserProgress should not be null", progress);
    assertEquals("User ID should match", userId, progress.getUserId());
    assertEquals("Item index should match", itemIndex, progress.getItemIndex());
    assertEquals("Item attempt should match", itemAttempt, progress.getItemAttempt());
    assertEquals("User name should match", userName, progress.getUserName());
    assertEquals("Session ID should match", sessionId, progress.getSessionId());
  }

  @Test
  public void testUserProgressConstructorWithNullUserName() {
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        1L,
                                                                                     0,
                                                                                     1,
                                                                                     null,
                                                                                     "session_123"
    );

    assertNotNull("UserProgress should not be null", progress);
    assertNull("User name should be null", progress.getUserName());
    assertEquals("Other fields should be preserved", 1L, progress.getUserId());
    assertEquals("Other fields should be preserved", 0, progress.getItemIndex());
    assertEquals("Other fields should be preserved", 1, progress.getItemAttempt());
    assertEquals("Other fields should be preserved", "session_123", progress.getSessionId());
  }

  @Test
  public void testUserProgressConstructorWithNullSessionId() {
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        100L,
                                                                                     2,
                                                                                     4,
                                                                                     "User",
                                                                                     null
    );

    assertNotNull("UserProgress should not be null", progress);
    assertNull("Session ID should be null", progress.getSessionId());
    assertEquals("Other fields should be preserved", 100L, progress.getUserId());
    assertEquals("Other fields should be preserved", 2, progress.getItemIndex());
    assertEquals("Other fields should be preserved", 4, progress.getItemAttempt());
    assertEquals("Other fields should be preserved", "User", progress.getUserName());
  }

  @Test
  public void testUserProgressConstructorWithEmptyStrings() {
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        42L,
                                                                                     1,
                                                                                     2,
                                                                                     "",
                                                                                     ""
    );

    assertNotNull("UserProgress should not be null", progress);
    assertEquals("Empty user name should be preserved", "", progress.getUserName());
    assertEquals("Empty session ID should be preserved", "", progress.getSessionId());
    assertEquals("Other fields should be preserved", 42L, progress.getUserId());
    assertEquals("Other fields should be preserved", 1, progress.getItemIndex());
    assertEquals("Other fields should be preserved", 2, progress.getItemAttempt());
  }

  @Test
  public void testUserProgressBoundaryValues() {
    // Test with boundary values
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        Long.MAX_VALUE,
                                                                                     Integer.MAX_VALUE,
                                                                                     Integer.MAX_VALUE,
                                                                                     "MaxUser",
                                                                                     "session_max"
    );

    assertEquals("Max long user ID should be preserved", Long.MAX_VALUE, progress.getUserId());
    assertEquals(
        "Max int item index should be preserved",
        Integer.MAX_VALUE,
        progress.getItemIndex()
    );
    assertEquals(
        "Max int item attempt should be preserved",
        Integer.MAX_VALUE,
        progress.getItemAttempt()
    );
    assertEquals("User name should be preserved", "MaxUser", progress.getUserName());
    assertEquals("Session ID should be preserved", "session_max", progress.getSessionId());
  }

  @Test
  public void testUserProgressMinValues() {
    // Test with minimum values
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        Long.MIN_VALUE,
                                                                                     Integer.MIN_VALUE,
                                                                                     Integer.MIN_VALUE,
                                                                                     "MinUser",
                                                                                     "session_min"
    );

    assertEquals("Min long user ID should be preserved", Long.MIN_VALUE, progress.getUserId());
    assertEquals(
        "Min int item index should be preserved",
        Integer.MIN_VALUE,
        progress.getItemIndex()
    );
    assertEquals(
        "Min int item attempt should be preserved",
        Integer.MIN_VALUE,
        progress.getItemAttempt()
    );
    assertEquals("User name should be preserved", "MinUser", progress.getUserName());
    assertEquals("Session ID should be preserved", "session_min", progress.getSessionId());
  }

  @Test
  public void testUserProgressWithZeroValues() {
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        0L,
                                                                                     0,
                                                                                     0,
                                                                                     "ZeroUser",
                                                                                     "session_zero"
    );

    assertEquals("Zero user ID should be allowed", 0L, progress.getUserId());
    assertEquals("Zero item index should be allowed", 0, progress.getItemIndex());
    assertEquals("Zero item attempt should be allowed", 0, progress.getItemAttempt());
    assertEquals("User name should be preserved", "ZeroUser", progress.getUserName());
    assertEquals("Session ID should be preserved", "session_zero", progress.getSessionId());
  }

  @Test
  public void testUserProgressWithNegativeValues() {
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        -1L,
                                                                                     -5,
                                                                                     -3,
                                                                                     "NegativeUser",
                                                                                     "session_negative"
    );

    assertEquals("Negative user ID should be allowed", -1L, progress.getUserId());
    assertEquals("Negative item index should be allowed", -5, progress.getItemIndex());
    assertEquals("Negative item attempt should be allowed", -3, progress.getItemAttempt());
    assertEquals("User name should be preserved", "NegativeUser", progress.getUserName());
    assertEquals("Session ID should be preserved", "session_negative", progress.getSessionId());
  }

  @Test
  public void testUserProgressDataIntegrity() {
    // Test that fields remain independent and don't affect each other
    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        999L,
                                                                                     10,
                                                                                     5,
                                                                                     "IntegrityUser",
                                                                                     "session_integrity"
    );

    // Verify all initial values
    assertEquals("User ID should be correct", 999L, progress.getUserId());
    assertEquals("Item index should be correct", 10, progress.getItemIndex());
    assertEquals("Item attempt should be correct", 5, progress.getItemAttempt());
    assertEquals("User name should be correct", "IntegrityUser", progress.getUserName());
    assertEquals("Session ID should be correct", "session_integrity", progress.getSessionId());

    // Since these are final fields, they should remain unchanged
    // Multiple calls should return the same values
    assertEquals("User ID should remain consistent", 999L, progress.getUserId());
    assertEquals("Item index should remain consistent", 10, progress.getItemIndex());
    assertEquals("Item attempt should remain consistent", 5, progress.getItemAttempt());
    assertEquals("User name should remain consistent", "IntegrityUser", progress.getUserName());
    assertEquals(
        "Session ID should remain consistent",
        "session_integrity",
        progress.getSessionId()
    );
  }

  @Test
  public void testUserProgressUnicodeStrings() {
    // Test with Unicode characters in strings
    String unicodeUserName = "用户名_αβγ_🌟";
    String unicodeSessionId = "session_αβγ_用户_🔬";

    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        777L,
                                                                                     7,
                                                                                     3,
                                                                                     unicodeUserName,
                                                                                     unicodeSessionId
    );

    assertEquals("Unicode user name should be preserved", unicodeUserName, progress.getUserName());
    assertEquals(
        "Unicode session ID should be preserved",
        unicodeSessionId,
        progress.getSessionId()
    );
    assertEquals("Other fields should be preserved", 777L, progress.getUserId());
    assertEquals("Other fields should be preserved", 7, progress.getItemIndex());
    assertEquals("Other fields should be preserved", 3, progress.getItemAttempt());
  }

  @Test
  public void testUserProgressLongStrings() {
    // Test with long strings
    StringBuilder longUserName = new StringBuilder();
    StringBuilder longSessionId = new StringBuilder();

    for (int i = 0; i < 1000; i++) {
      longUserName.append("A");
      longSessionId.append("S");
    }

    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        12345L,
                                                                                     100,
                                                                                     50,
                                                                                     longUserName.toString(),
                                                                                     longSessionId.toString()
    );

    assertEquals(
        "Long user name should be preserved",
        longUserName.toString(),
        progress.getUserName()
    );
    assertEquals(
        "Long session ID should be preserved",
        longSessionId.toString(),
        progress.getSessionId()
    );
    assertEquals("Long user name length should be correct", 1000, progress.getUserName().length());
    assertEquals(
        "Long session ID length should be correct",
        1000,
        progress.getSessionId().length()
    );
  }

  @Test
  public void testUserProgressSpecialCharacters() {
    // Test with special characters in strings
    String specialUserName = "User!@#$%^&*()_+-={}[]|\\:;\"'<>?,./~`";
    String specialSessionId = "session!@#$%^&*()_+-={}[]|\\:;\"'<>?,./~`";

    UserProgressManager.UserProgress progress = new UserProgressManager.UserProgress(
        555L,
                                                                                     25,
                                                                                     15,
                                                                                     specialUserName,
                                                                                     specialSessionId
    );

    assertEquals(
        "Special character user name should be preserved",
        specialUserName,
        progress.getUserName()
    );
    assertEquals(
        "Special character session ID should be preserved",
        specialSessionId,
        progress.getSessionId()
    );
    assertEquals("Other fields should be preserved", 555L, progress.getUserId());
    assertEquals("Other fields should be preserved", 25, progress.getItemIndex());
    assertEquals("Other fields should be preserved", 15, progress.getItemAttempt());
  }

  @Test
  public void testUserProgressRealisticScenarios() {
    // Test with realistic application scenarios

    // Scenario 1: New user starting first drawing
    UserProgressManager.UserProgress newUser = new UserProgressManager.UserProgress(
        1L,
                                                                                    0,
                                                                                    1,
                                                                                    "Alice",
                                                                                    "session_20241201_143022"
    );

    assertEquals("New user should have user ID 1", 1L, newUser.getUserId());
    assertEquals("New user should start at item index 0", 0, newUser.getItemIndex());
    assertEquals("New user should start at attempt 1", 1, newUser.getItemAttempt());
    assertEquals("New user name should be Alice", "Alice", newUser.getUserName());
    assertTrue(
        "Session ID should follow expected pattern",
        newUser.getSessionId().startsWith("session_")
    );

    // Scenario 2: User in middle of session
    UserProgressManager.UserProgress midSession = new UserProgressManager.UserProgress(
        25L,
                                                                                       3,
                                                                                       2,
                                                                                       "Bob Smith",
                                                                                       "session_456789"
    );

    assertEquals("Mid-session user should have correct progress", 3, midSession.getItemIndex());
    assertEquals("Mid-session user should be on attempt 2", 2, midSession.getItemAttempt());
    assertEquals(
        "Mid-session user name should handle spaces",
        "Bob Smith",
        midSession.getUserName()
    );

    // Scenario 3: User near completion
    UserProgressManager.UserProgress nearComplete = new UserProgressManager.UserProgress(
        99L,
                                                                                         9,
                                                                                         3,
                                                                                         "Charlie",
                                                                                         "session_final"
    );

    assertEquals("Near-complete user should be at index 9", 9, nearComplete.getItemIndex());
    assertEquals("Near-complete user should be at attempt 3", 3, nearComplete.getItemAttempt());
  }

  @Test
  public void testUserProgressEquality() {
    // Test that objects with same values are handled consistently
    UserProgressManager.UserProgress progress1 = new UserProgressManager.UserProgress(
        100L,
                                                                                      5,
                                                                                      2,
                                                                                      "TestUser",
                                                                                      "session_test"
    );

    UserProgressManager.UserProgress progress2 = new UserProgressManager.UserProgress(
        100L,
                                                                                      5,
                                                                                      2,
                                                                                      "TestUser",
                                                                                      "session_test"
    );

    // While UserProgress doesn't implement equals(), we can test that getters return same values
    assertEquals("User IDs should match", progress1.getUserId(), progress2.getUserId());
    assertEquals("Item indexes should match", progress1.getItemIndex(), progress2.getItemIndex());
    assertEquals(
        "Item attempts should match",
        progress1.getItemAttempt(),
        progress2.getItemAttempt()
    );
    assertEquals("User names should match", progress1.getUserName(), progress2.getUserName());
    assertEquals("Session IDs should match", progress1.getSessionId(), progress2.getSessionId());
  }
} 
