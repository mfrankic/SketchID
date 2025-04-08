package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;
import static com.mfrankic.sketchid.Constants.KEY_USER_PROGRESS_PREFIX;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserProgressManager {

  private static final String KEY_USER_INDEX = KEY_USER_PROGRESS_PREFIX + "index_";
  private static final String KEY_USER_ATTEMPT = KEY_USER_PROGRESS_PREFIX + "attempt_";
  private static final String KEY_USER_SESSION = KEY_USER_PROGRESS_PREFIX + "session_";
  private static final String KEY_FINISHED_USERS = "finished_users";

  private UserProgressManager() {
    // Private constructor to prevent instantiation
  }

  /**
   * Saves progress for a specific user
   */
  public static void saveUserProgress(
      Context context,
      long userId,
      int itemIndex,
      int itemAttempt,
      String sessionId
  ) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs
        .edit()
        .putInt(KEY_USER_INDEX + userId, itemIndex)
        .putInt(KEY_USER_ATTEMPT + userId, itemAttempt)
        .putString(KEY_USER_SESSION + userId, sessionId)
        .apply();
  }

  /**
   * Loads progress for a specific user
   */
  public static UserProgress loadUserProgress(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int itemIndex = prefs.getInt(KEY_USER_INDEX + userId, 0);
    int itemAttempt = prefs.getInt(KEY_USER_ATTEMPT + userId, 1);
    String sessionId = prefs.getString(KEY_USER_SESSION + userId, null);

    // If no session ID exists, generate one
    if (sessionId == null) {
      sessionId = generateNewSessionId();
      prefs.edit().putString(KEY_USER_SESSION + userId, sessionId).apply();
    }

    return new UserProgress(userId, itemIndex, itemAttempt, sessionId);
  }

  /**
   * Generate a new session ID for a user
   */
  public static String generateNewSessionId() {
    return "session_" + UUID.randomUUID().toString();
  }

  /**
   * Gets the current session ID for a user
   */
  public static String getUserSessionId(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String sessionId = prefs.getString(KEY_USER_SESSION + userId, null);

    // If no session ID exists, generate one
    if (sessionId == null) {
      sessionId = generateNewSessionId();
      prefs.edit().putString(KEY_USER_SESSION + userId, sessionId).apply();
    }

    return sessionId;
  }

  /**
   * Checks if a user has unfinished progress
   */
  public static boolean hasUnfinishedProgress(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.contains(KEY_USER_INDEX + userId) || prefs.contains(KEY_USER_ATTEMPT + userId);
  }

  /**
   * Gets all users with unfinished progress
   */
  public static List<UserProgress> getUnfinishedUsers(Context context, AppDatabase db) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Map<String, ?> allPrefs = prefs.getAll();
    Map<Long, UserProgress> progressMap = new HashMap<>();
    Set<String> finishedUsers = prefs.getStringSet(KEY_FINISHED_USERS, new HashSet<>());

    // Look for all user index entries
    for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith(KEY_USER_INDEX)) {
        String userIdStr = key.substring(KEY_USER_INDEX.length());
        try {
          long userId = Long.parseLong(userIdStr);

          // Skip if user is marked as finished
          if (finishedUsers.contains(String.valueOf(userId))) {
            continue;
          }

          int itemIndex = prefs.getInt(key, 0);
          int itemAttempt = prefs.getInt(KEY_USER_ATTEMPT + userId, 1);
          String sessionId = prefs.getString(KEY_USER_SESSION + userId, generateNewSessionId());

          User user = db.userDao().getUserByID(userId);
          if (user != null) {
            progressMap.put(
                userId,
                new UserProgress(userId, itemIndex, itemAttempt, user.name, sessionId)
            );
          }
        } catch (NumberFormatException e) {
          // Skip invalid entries
        }
      }
    }

    return new ArrayList<>(progressMap.values());
  }

  /**
   * Marks a user as finished
   */
  public static void markUserAsFinished(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Set<String> finishedUsers = new HashSet<>(prefs.getStringSet(
        KEY_FINISHED_USERS,
        new HashSet<>()
    ));
    finishedUsers.add(String.valueOf(userId));
    prefs.edit().putStringSet(KEY_FINISHED_USERS, finishedUsers).apply();
  }

  /**
   * Clears progress for a specific user
   */
  public static void clearUserProgress(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs
        .edit()
        .remove(KEY_USER_INDEX + userId)
        .remove(KEY_USER_ATTEMPT + userId)
        .remove(KEY_USER_SESSION + userId)
        .apply();
  }

  /**
   * Resets global progress keys
   */
  public static void resetGlobalProgress(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().remove(KEY_CURRENT_ITEM_INDEX).remove(KEY_CURRENT_ITEM_ATTEMPT).apply();
  }

  public static class UserProgress {
    private final long userId;
    private final int itemIndex;
    private final int itemAttempt;
    private final String sessionId;
    private String userName;

    public UserProgress(long userId, int itemIndex, int itemAttempt) {
      this.userId = userId;
      this.itemIndex = itemIndex;
      this.itemAttempt = itemAttempt;
      this.sessionId = null;
    }

    public UserProgress(long userId, int itemIndex, int itemAttempt, String sessionId) {
      this.userId = userId;
      this.itemIndex = itemIndex;
      this.itemAttempt = itemAttempt;
      this.sessionId = sessionId;
    }

    public UserProgress(
        long userId,
        int itemIndex,
        int itemAttempt,
        String userName,
        String sessionId
    ) {
      this.userId = userId;
      this.itemIndex = itemIndex;
      this.itemAttempt = itemAttempt;
      this.userName = userName;
      this.sessionId = sessionId;
    }

    public long getUserId() {
      return userId;
    }

    public int getItemIndex() {
      return itemIndex;
    }

    public int getItemAttempt() {
      return itemAttempt;
    }

    public String getUserName() {
      return userName;
    }

    public String getSessionId() {
      return sessionId;
    }
  }
} 
 