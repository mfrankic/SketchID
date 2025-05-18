package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.FINISHED;
import static com.mfrankic.sketchid.Constants.IMAGES_VIEWED;
import static com.mfrankic.sketchid.Constants.KEY_USER_PROGRESS_PREFIX;
import static com.mfrankic.sketchid.Constants.SETTINGS;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserProgressManager {

  private static final String KEY_USER_SESSION = KEY_USER_PROGRESS_PREFIX + "session_";
  private static final String KEY_USER_SESSIONS = KEY_USER_PROGRESS_PREFIX + "sessions_";

  private UserProgressManager() {
    // Private constructor to prevent instantiation
  }

  /**
   * Updates progress within a session
   */
  public static void updateSessionProgress(
      Context context,
      long userId,
      String sessionId,
      int itemIndex,
      int itemAttempt
  ) {
    Session session = getSession(context, userId, sessionId);
    if (session != null) {
      session.setItemIndex(itemIndex);
      session.setItemAttempt(itemAttempt);
      saveSession(context, userId, session);
    }
  }

  /**
   * Saves a session to SharedPreferences
   */
  private static void saveSession(Context context, long userId, Session session) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    // Get existing sessions
    Set<String> sessionsSet = prefs.getStringSet(KEY_USER_SESSIONS + userId, new HashSet<>());
    Set<String> updatedSessions = new HashSet<>(sessionsSet);

    // Add or update this session
    updatedSessions.add(session.getSessionId());

    // Save the session data and update the sessions list
    prefs
        .edit()
        .putString(KEY_USER_SESSION + userId + "_" + session.getSessionId(), session.toJson())
        .putStringSet(KEY_USER_SESSIONS + userId, updatedSessions)
        .apply();
  }

  /**
   * Gets a specific session
   */
  public static Session getSession(Context context, long userId, String sessionId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String sessionJson = prefs.getString(KEY_USER_SESSION + userId + "_" + sessionId, null);

    if (sessionJson != null) {
      try {
        return Session.fromJson(sessionJson);
      } catch (JSONException e) {
        return null;
      }
    }

    return null;
  }

  /**
   * Creates a new drawing session for a user
   */
  public static Session createDrawingSession(
      Context context,
      long userId,
      Map<String, Object> settings
  ) {
    String sessionId = generateNewSessionId();
    Session session = new Session(sessionId, userId, settings);

    // Save the session
    saveSession(context, userId, session);

    return session;
  }

  /**
   * Generate a new session ID for a user
   */
  public static String generateNewSessionId() {
    return "session_" + UUID.randomUUID().toString();
  }

  /**
   * Records when user moves to next image in session
   */
  public static void recordNextImage(Context context, long userId, String sessionId) {
    Session session = getSession(context, userId, sessionId);
    if (session != null) {
      session.incrementImagesViewed();
      saveSession(context, userId, session);
    }
  }

  /**
   * Marks a session as finished
   */
  public static void markSessionFinished(Context context, long userId, String sessionId) {
    Session session = getSession(context, userId, sessionId);
    if (session != null) {
      session.setFinished(true);
      saveSession(context, userId, session);
    }
  }

  /**
   * Gets all users with unfinished sessions
   */
  public static List<UserProgress> getUnfinishedUsers(Context context, AppDatabase db) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Map<String, ?> allPrefs = prefs.getAll();
    Map<Long, UserProgress> progressMap = new HashMap<>();

    // Look for user session entries
    for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith(KEY_USER_SESSIONS)) {
        String userIdStr = key.substring(KEY_USER_SESSIONS.length());
        try {
          long userId = Long.parseLong(userIdStr);

          // Get user's unfinished sessions
          List<Session> unfinishedSessions = getUnfinishedSessions(context, userId);
          if (!unfinishedSessions.isEmpty()) {
            User user = db.userDao().getUserByID(userId);
            if (user != null) {
              // Use the first unfinished session for backward compatibility
              Session firstSession = unfinishedSessions.get(0);
              progressMap.put(
                  userId, new UserProgress(
                      userId,
                      firstSession.getItemIndex(),
                      firstSession.getItemAttempt(),
                      user.name,
                      firstSession.getSessionId()
                  )
              );
            }
          }
        } catch (NumberFormatException e) {
          // Skip invalid entries
        }
      }
    }

    return new ArrayList<>(progressMap.values());
  }

  /**
   * Gets unfinished sessions for a user
   */
  public static List<Session> getUnfinishedSessions(Context context, long userId) {
    List<Session> allSessions = getUserSessions(context, userId);
    List<Session> unfinishedSessions = new ArrayList<>();

    for (Session session : allSessions) {
      if (!session.isFinished()) {
        unfinishedSessions.add(session);
      }
    }

    return unfinishedSessions;
  }

  /**
   * Gets all sessions for a user
   */
  public static List<Session> getUserSessions(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Set<String> sessionIds = prefs.getStringSet(KEY_USER_SESSIONS + userId, new HashSet<>());
    List<Session> sessions = new ArrayList<>();

    for (String sessionId : sessionIds) {
      Session session = getSession(context, userId, sessionId);
      if (session != null) {
        sessions.add(session);
      }
    }

    return sessions;
  }

  /**
   * Deletes all sessions for a user
   */
  public static void deleteUserSessions(Context context, long userId) {
    clearUserProgress(context, userId);
  }

  /**
   * Clears progress for a specific user
   */
  public static void clearUserProgress(Context context, long userId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    // Get all sessions for this user
    Set<String> sessionIds = prefs.getStringSet(KEY_USER_SESSIONS + userId, new HashSet<>());

    // Create editor for batch operations
    SharedPreferences.Editor editor = prefs.edit();

    // Remove all session data
    for (String sessionId : sessionIds) {
      editor.remove(KEY_USER_SESSION + userId + "_" + sessionId);
    }

    // Remove the sessions list
    editor.remove(KEY_USER_SESSIONS + userId).apply();
  }

  public static class UserProgress {
    private final long userId;
    private final int itemIndex;
    private final int itemAttempt;
    private final String sessionId;
    private final String userName;

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
  }

  /**
   * Represents a drawing session for a user
   */
  public static class Session {
    private final String sessionId;
    private final long userId;
    private int itemIndex;
    private int itemAttempt;
    private int imagesViewed;
    private boolean finished;
    private Map<String, Object> settings;

    public Session(String sessionId, long userId, Map<String, Object> settings) {
      this.sessionId = sessionId;
      this.userId = userId;
      this.itemIndex = 0;
      this.itemAttempt = 1;
      this.imagesViewed = 0;
      this.finished = false;
      this.settings = settings != null ? settings : new HashMap<>();
    }

    public static Session fromJson(String jsonStr)
    throws JSONException {
      JSONObject json = new JSONObject(jsonStr);
      String sessionId = json.getString("sessionId");
      long userId = json.getLong("userId");

      Session session = new Session(sessionId, userId, new HashMap<>());
      session.setItemIndex(json.getInt("itemIndex"));
      session.setItemAttempt(json.getInt("itemAttempt"));

      // Handle optional fields with defaults
      if (json.has(IMAGES_VIEWED)) {
        session.imagesViewed = json.getInt(IMAGES_VIEWED);
      }

      if (json.has(FINISHED)) {
        session.finished = json.getBoolean(FINISHED);
      }

      // Load settings
      if (json.has(SETTINGS)) {
        JSONObject settingsJson = json.getJSONObject(SETTINGS);
        Map<String, Object> settings = new HashMap<>();

        // Extract all keys from the settings object
        for (Iterator<String> it = settingsJson.keys(); it.hasNext(); ) {
          String key = it.next();
          settings.put(key, settingsJson.get(key));
        }

        session.settings = settings;
      }

      return session;
    }

    public String getSessionId() {
      return sessionId;
    }

    public int getItemIndex() {
      return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
      this.itemIndex = itemIndex;
    }

    public int getItemAttempt() {
      return itemAttempt;
    }

    public void setItemAttempt(int itemAttempt) {
      this.itemAttempt = itemAttempt;
    }

    public void incrementImagesViewed() {
      this.imagesViewed++;
    }

    public boolean isFinished() {
      return finished;
    }

    public void setFinished(boolean finished) {
      this.finished = finished;
    }

    public String toJson() {
      JSONObject json = new JSONObject();
      try {
        json.put("sessionId", sessionId);
        json.put("userId", userId);
        json.put("itemIndex", itemIndex);
        json.put("itemAttempt", itemAttempt);
        json.put(IMAGES_VIEWED, imagesViewed);
        json.put(FINISHED, finished);

        // Convert settings to JSON
        JSONObject settingsJson = new JSONObject();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
          settingsJson.put(entry.getKey(), entry.getValue());
        }
        json.put(SETTINGS, settingsJson);

        return json.toString();
      } catch (JSONException e) {
        return "{}";
      }
    }
  }
} 
 