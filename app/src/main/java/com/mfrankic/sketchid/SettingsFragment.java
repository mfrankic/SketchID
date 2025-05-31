package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.CANCEL_BUTTON;
import static com.mfrankic.sketchid.Constants.DELETE_BUTTON;
import static com.mfrankic.sketchid.Constants.ERROR_NAME_EMPTY;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;
import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.KEY_SELECTED_USER;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class SettingsFragment extends PreferenceFragmentCompat {

  private static final String DIALOG_PREFERENCE_KEY = "dialog_preference_key";
  private static final String DIALOG_RESULT_KEY = "dialog_result";
  private static final String UNKNOWN_VALUE = "unknown";
  private static final String UNKNOWN_IMAGE = "unknown_image";
  private static final String PROGRESS_ITEM_SUFFIX = " data";
  private static final int EXPORT_PAGE_SIZE = 2500;
  private static final int CSV_SB_INITIAL_CAPACITY = 256;
  private static final int BUFFERED_STREAM_SIZE = 32768;

  private EditTextPreference newUserPreference;
  private EditTextPreference drawingAttemptsPreference;
  private Preference exportDataPreference;
  private CustomDialogPreference clearDataPreference;
  private Executor executor;
  private AppDatabase db;
  private ListPreference userListPreference;
  private ListPreference drawingModePreference;
  private CustomDialogPreference deleteUserPreference;
  private CustomDialogPreference deleteUserDataPreference;
  private String selectedUserID;
  private Preference selectImagePreference;
  private CustomDialogPreference resetUserProgressPreference;
  private androidx.preference.PreferenceCategory drawingSettingsCategory;
  private androidx.preference.PreferenceCategory dataManagementCategory;
  private List<UserProgressManager.UserProgress> unfinishedUsers;

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    db = AppDatabase.getInstance(requireContext());
    executor = Executors.newSingleThreadExecutor();
    unfinishedUsers = new ArrayList<>();

    userListPreference = findPreference(getResources().getText(R.string.key_selected_user));
    deleteUserPreference = findPreference(getResources().getText(R.string.key_delete_user));
    deleteUserDataPreference
        = findPreference(getResources().getText(R.string.key_delete_user_data));
    newUserPreference = findPreference(getResources().getText(R.string.key_new_user));
    drawingAttemptsPreference
        = findPreference(getResources().getText(R.string.key_drawing_attempts));
    drawingModePreference = findPreference(getResources().getText(R.string.key_drawing_mode));
    exportDataPreference = findPreference(getResources().getText(R.string.key_export_data));
    clearDataPreference = findPreference(getResources().getText(R.string.key_clear_data));
    selectImagePreference = findPreference(getResources().getText(R.string.key_select_images));
    resetUserProgressPreference
        = findPreference(getResources().getText(R.string.key_reset_user_progress));
    drawingSettingsCategory = findPreference(getResources().getText(R.string.key_drawing_settings));
    dataManagementCategory = findPreference(getResources().getText(R.string.key_data_management));

    setupUserListPreference();
    setupNewUserPreference();
    setupDeleteUserPreference();
    setupDrawingAttemptsPreference();
    setupDrawingModePreference();
    setupExportDataPreference();
    setupDeleteUserDataPreference();
    setupClearDataPreference();
    setupSelectImagePreference();
    setupResetUserProgressPreference();

    loadUsersIntoListPreference();

    executor.execute(this::checkUnfinishedUsersInitial);

    getParentFragmentManager().setFragmentResultListener(
        DIALOG_RESULT_KEY, this, (requestKey, result) -> {

          String preferenceKey = result.getString(DIALOG_PREFERENCE_KEY);
          if (preferenceKey != null) {

            Preference preference = findPreference(preferenceKey);
            if (preference instanceof CustomDialogPreference) {

              boolean changed = result.getBoolean("changed", false);
              if (changed) {
                (preference).callChangeListener(true);
              }
            }
          }
        }
    );
  }

  @Override
  public void onDisplayPreferenceDialog(@NonNull Preference preference) {
    if (preference instanceof CustomDialogPreference) {

      return;
    }
    super.onDisplayPreferenceDialog(preference);
  }

  private void setupUserListPreference() {
    if (userListPreference != null) {
      userListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        int idx = userListPreference.findIndexOfValue(newValue.toString());
        if (idx == -1) {
          requireActivity().runOnUiThread(() -> userListPreference.setValue(""));
          return true;
        }
        userListPreference.setSummary(String.format(
            "Current User: %s",
            userListPreference.getEntries()[idx]
        ));
        selectedUserID = newValue.toString();
        if (deleteUserPreference != null) {
          deleteUserPreference.setEnabled(true);
        }
        return true;
      });
    }
  }

  private void setupNewUserPreference() {
    if (newUserPreference != null) {

      newUserPreference.setOnBindEditTextListener(editText -> {

        editText.setHint("Name");

        editText.setText("");
      });

      newUserPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        String name = newValue.toString().trim();
        if (name.isEmpty()) {
          Toast.makeText(getContext(), ERROR_NAME_EMPTY, Toast.LENGTH_LONG).show();
          return false;
        }

        executor.execute(() -> {
          User user = new User(name);
          String newUserID = Long.toString(db.userDao().insertUser(user));

          PreferenceManager
              .getDefaultSharedPreferences(requireContext())
              .edit()
              .putString(KEY_SELECTED_USER, newUserID)
              .apply();

          requireActivity().runOnUiThread(this::loadUsersIntoListPreference);
        });

        return true;
      });

      newUserPreference.setPersistent(false);
    }
  }

  private void loadUsersIntoListPreference() {
    executor.execute(() -> {
      List<User> users = db.userDao().getAllUsers();
      String[] userNames = new String[users.size()];
      String[] userIDs = new String[users.size()];

      for (int i = 0; i < users.size(); i++) {
        userNames[i] = users.get(i).name;
        userIDs[i] = String.valueOf(users.get(i).id);
      }

      requireActivity().runOnUiThread(() -> {
        userListPreference.setEntries(userNames);
        userListPreference.setEntryValues(userIDs);

        if (users.isEmpty()) {
          userListPreference.setEnabled(false);
          userListPreference.setSummary("No users found");
          deleteUserPreference.setEnabled(false);
          deleteUserDataPreference.setEnabled(false);
          exportDataPreference.setEnabled(false);
          return;
        }

        selectedUserID = PreferenceManager
            .getDefaultSharedPreferences(requireContext())
            .getString(KEY_SELECTED_USER, "-1");

        if (userListPreference.findIndexOfValue(selectedUserID) == -1) {
          selectedUserID = "-1";
          userListPreference.setSummary("Select user from the list");
          deleteUserPreference.setEnabled(false);
          deleteUserDataPreference.setEnabled(false);
          exportDataPreference.setEnabled(false);
        }

        if (!selectedUserID.equals("-1")) {
          userListPreference.setValue(selectedUserID);
          userListPreference.callChangeListener(selectedUserID);
          deleteUserPreference.setEnabled(true);
          deleteUserDataPreference.setEnabled(true);
          exportDataPreference.setEnabled(true);
        }

        userListPreference.setEnabled(true);
      });
    });
  }

  private void setupDeleteUserPreference() {
    if (deleteUserPreference != null) {
      deleteUserPreference.setTitleColor(getResources().getColor(
          R.color.error,
          requireContext().getTheme()
      ));

      deleteUserPreference.setOnPreferenceClickListener(preference -> {

        if (isNotValidUserSelected()) {
          return true;
        }

        String dialogMessage = createDeleteUserMessage();
        Drawable dialogIcon = createWarningIcon();

        AlertDialog alertDialog = createDeleteUserDialog(dialogMessage, dialogIcon);
        alertDialog.show();

        applyErrorTintToPositiveButton(alertDialog);

        return true;
      });
    }
  }

  private boolean isNotValidUserSelected() {
    return selectedUserID == null
           || selectedUserID.equals("-1")
           || userListPreference.getEntry() == null;
  }

  private String createDeleteUserMessage() {
    return String.format(
        "Are you sure you want to delete the user: "
        + "%s?%n%nThis will remove all user data "
        + "including drawing history.", userListPreference.getEntry()
    );
  }

  private Drawable createWarningIcon() {

    Drawable dialogIcon = ResourcesCompat.getDrawable(
        getResources(),
        android.R.drawable.ic_dialog_alert,
        null
    );

    if (dialogIcon != null) {
      dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
    }

    return dialogIcon;
  }

  private AlertDialog createDeleteUserDialog(String dialogMessage, Drawable dialogIcon) {
    return new AlertDialog.Builder(requireContext())
        .setTitle("Delete User?")
        .setMessage(dialogMessage)
        .setIcon(dialogIcon)
        .setPositiveButton(DELETE_BUTTON, (dialog, which) -> executeUserDeletion())
        .setNegativeButton(CANCEL_BUTTON, null)
        .create();
  }

  private void executeUserDeletion() {
    executor.execute(() -> {
      int deletedRows = db.userDao().deleteUser(Long.parseLong(selectedUserID));
      if (deletedRows > 0) {
        requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "User deleted", Toast.LENGTH_LONG)
            .show());

        db.drawingDataDao().deleteDrawingDataByUserID(Long.parseLong(selectedUserID));

        UserProgressManager.deleteUserSessions(requireContext(), Long.parseLong(selectedUserID));

        PreferenceManager
            .getDefaultSharedPreferences(requireContext())
            .edit()
            .remove(KEY_SELECTED_USER)
            .apply();
        loadUsersIntoListPreference();
        userListPreference.callChangeListener("-1");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
      } else {
        requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "Failed to delete user", Toast.LENGTH_LONG)
            .show());
      }
    });
  }

  private void applyErrorTintToPositiveButton(AlertDialog alertDialog) {
    try {
      int errorColor = getResources().getColor(R.color.error, requireContext().getTheme());
      alertDialog.setOnShowListener(dialog -> alertDialog
          .getButton(DialogInterface.BUTTON_POSITIVE)
          .setTextColor(errorColor));
    } catch (Exception ignored) {
      // Ignore any errors setting the color
    }
  }

  private void setupDrawingAttemptsPreference() {
    if (drawingAttemptsPreference == null) {
      return;
    }

    drawingAttemptsPreference.setOnBindEditTextListener(editText -> {
      editText.setInputType(InputType.TYPE_CLASS_NUMBER);
      editText.selectAll();
      editText.setFilters(new InputFilter[]{
          (source, start, end, dest, dstart, dend) -> {
            try {
              int input = Integer.parseInt(dest.toString().substring(0, dstart) + source + dest
                  .toString()
                  .substring(dend));
              if (input < 1 || input > 999 || (source.equals("0") && dstart == 0)) {
                throw new NumberFormatException();
              }
            } catch (NumberFormatException e) {
              return "";
            }
            return null;
          }
      });
    });

    drawingAttemptsPreference.setSummaryProvider(preference -> {
      String attempts = PreferenceManager
          .getDefaultSharedPreferences(requireContext())
          .getString(KEY_DRAWING_ATTEMPTS, "-1");
      return String.format("Current: %s", !attempts.equals("-1") ? attempts : "Not set");
    });

    drawingAttemptsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
      String value = newValue.toString().isBlank() ? "-1" : newValue.toString();
      int attempts = Integer.parseInt(value);
      if (attempts < 1) {
        Toast
            .makeText(
                getContext(),
                "Number of attempts per drawing must be at least 1",
                Toast.LENGTH_LONG
            )
            .show();
        return false;
      }
      return true;
    });
  }

  private void setupDrawingModePreference() {
    if (drawingModePreference != null) {

      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
      String currentMode = prefs.getString(
          Constants.KEY_DRAWING_MODE,
          Constants.DRAWING_MODE_NORMAL
      );
      int index = drawingModePreference.findIndexOfValue(currentMode);
      if (index >= 0) {
        drawingModePreference.setSummary(String.format(
            "Current mode: %s",
            drawingModePreference.getEntries()[index]
        ));
      }

      drawingModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
        int idx = drawingModePreference.findIndexOfValue(newValue.toString());
        if (idx >= 0) {
          drawingModePreference.setSummary(String.format(
              "Current mode: %s",
              drawingModePreference.getEntries()[idx]
          ));
        }
        return true;
      });
    }
  }

  private void setupExportDataPreference() {
    if (exportDataPreference != null) {
      exportDataPreference.setOnPreferenceClickListener(preference -> {
        SettingsActivity activity = (SettingsActivity) getActivity();
        if (activity != null) {
          activity.showExportOverlay();
          activity.clearProgressItems();
        }

        executor.execute(() -> {
          boolean success = exportDataToCSV(activity);

          requireActivity().runOnUiThread(() -> {
            if (activity != null) {
              activity.showOkButton();
            }
            if (!success && activity == null) {
              showExportMessage(getString(R.string.export_failed));
            }
          });
        });
        return true;
      });
    }
  }

  private boolean isValidUserForExport() {
    return selectedUserID != null && !selectedUserID.equals("-1");
  }

  /**
   * Export data to CSV for the currently selected user.
   * UI updates are handled via the activity.
   */
  private boolean exportDataToCSV(@Nullable SettingsActivity activity) {
    long userId = Long.parseLong(selectedUserID);
    String userName = getUserNameForExport(userId);

    File userDir = prepareUserExportDirectory(activity, userName);
    if (userDir == null) {
      return false; // Message already shown by prepareUserExportDirectory or isValidUserForExport
    }

    final String[] dataTypes = {
        "Drawing", "Gravity", "Gyroscope", "Magnetic Field", "Magnetic Baseline", "Accelerometer"
    };

    ContentResolver contentResolver = requireActivity().getContentResolver();
    Executor parallelExecutor = Executors.newFixedThreadPool(dataTypes.length);
    CountDownLatch dataTypeLatch = new CountDownLatch(dataTypes.length);
    ConcurrentHashMap<String, Boolean> exportResults = new ConcurrentHashMap<>();
    Map<String, UserProgressManager.Session> sessionCache = new HashMap<>();

    final String[] dataPrefixes = {
        Constants.DRAWING_DATA_PREFIX,
        Constants.GRAVITY_DATA_PREFIX,
        Constants.GYROSCOPE_DATA_PREFIX,
        Constants.MAGNETIC_FIELD_DATA_PREFIX,
        Constants.MAGNETIC_FIELD_BASELINE_DATA_PREFIX,
        Constants.ACCELEROMETER_DATA_PREFIX
    };

    if (activity != null) {
      for (String dataType : dataTypes) {
        activity.addProgressItem(dataType + PROGRESS_ITEM_SUFFIX);
      }
    }

    BiConsumer<String, Boolean> progressUpdater = (dataType, success) -> {
      if (activity != null) {
        activity.updateProgressItemStatus(dataType + PROGRESS_ITEM_SUFFIX, success);
      }
    };

    ExportTaskContext exportContext = new ExportTaskContext(
        userId,
        userDir,
        contentResolver,
        dataTypeLatch,
        exportResults,
        progressUpdater,
        sessionCache
    );

    // Launch Drawing Data Export
    parallelExecutor.execute(() -> launchDrawingDataExport(exportContext, dataTypes[0]));

    // Launch Sensor Data Exports
    for (int i = 1; i < dataTypes.length; i++) {
      final int index = i;
      parallelExecutor.execute(() -> launchSensorDataExport(
          exportContext,
          dataTypes,
          dataPrefixes,
          index
      ));
    }

    try {
      boolean completed = dataTypeLatch.await((12 * 60), TimeUnit.SECONDS);
      if (!completed) {
        android.util.Log.w(
            "ExportData",
            "Timeout waiting for data types to export for user: " + userName
        );
        showExportMessage("Export timed out for user: " + userName);
        // Potentially update specific UI elements if needed
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      if (activity != null) activity.showOkButton();
      return false;
    }

    boolean allDataTypesExported = exportResults.values().stream().allMatch(Boolean::booleanValue);

    if (activity != null) {
      if (allDataTypesExported) {
        showExportMessage(getString(R.string.export_success_location));
      } else {
        showExportMessage("Some data for " + userName + " failed to export. Check UI for details.");
      }
    }

    return allDataTypesExported;
  }

  private String getUserNameForExport(long userId) {
    CharSequence userNameCharSequence = userListPreference.getEntry();
    return (userNameCharSequence != null) ? userNameCharSequence.toString() : "User_" + userId;
  }

  private File prepareUserExportDirectory(@Nullable SettingsActivity activity, String userName) {
    if (!isValidUserForExport()) {
      if (activity != null) activity.showOkButton();

      showExportMessage("Cannot export: No valid user selected.");
      return null;
    }

    File exportDir = createExportDirectory(); // Shows toast on failure
    if (exportDir == null) {
      if (activity != null) activity.showOkButton();
      return null;
    }

    String timestamp = createTimestamp();
    String userDirName = userName + "_" + timestamp;
    File userDir = new File(exportDir, userDirName);

    if (!userDir.exists() && !userDir.mkdirs()) {
      showExportMessage("Failed to create user directory: " + userDirName);
      if (activity != null) activity.showOkButton();
      return null;
    }
    return userDir;
  }

  private void launchDrawingDataExport(ExportTaskContext context, String dataType) {
    boolean overallSuccess = true;
    Map<File, OutputStream> openStreams = new HashMap<>();
    Set<File> filesWithHeaders = new HashSet<>();

    try {
      int offset = 0;
      boolean hasMoreData;

      do {
        List<DrawingExportData> pageData = db
            .drawingDataDao()
            .getPaginatedAllDrawingDataWithUsersAndImagesByUserID(
                context.userId,
                EXPORT_PAGE_SIZE,
                offset
            );

        if (pageData != null && !pageData.isEmpty()) {
          processDrawingModes(pageData, context.sessionCache);
          // Use specific types for DrawingExportData
          Map<String, List<DrawingExportData>> dataByModeOnPage = groupDataByDrawingMode(pageData);

          for (Map.Entry<String, List<DrawingExportData>> modeEntry : dataByModeOnPage.entrySet()) {
            String mode = modeEntry.getKey();
            List<DrawingExportData> modeDataOnPage = modeEntry.getValue();
            File modeDir = createModeDirectory(context.userDir, mode);
            if (modeDir == null) {
              overallSuccess = false;
              continue; // Skip this mode if directory creation fails
            }

            Map<String, List<DrawingExportData>> dataByImageOnPage = groupDrawingDataByImage(
                modeDataOnPage);
            for (Map.Entry<String, List<DrawingExportData>> imageEntry :
                dataByImageOnPage.entrySet()) {
              String imageName = imageEntry.getKey();
              List<DrawingExportData> imageDataOnPage = imageEntry.getValue();

              String formattedImageName = imageName.toLowerCase().replace(" ", "_");
              String formattedDataType = formatDataTypeForFilename(Constants.DRAWING_DATA_PREFIX);
              String fileName = formattedImageName + "_" + formattedDataType + ".csv";
              File csvFile = new File(modeDir, fileName);

              try {
                OutputStream outputStream = openStreams.get(csvFile);
                if (outputStream == null) {
                  OutputStream rawOutputStream
                      = context.contentResolver.openOutputStream(Uri.fromFile(csvFile));
                  if (rawOutputStream != null) {
                    outputStream = new BufferedOutputStream(rawOutputStream, BUFFERED_STREAM_SIZE);
                    openStreams.put(csvFile, outputStream);
                  } else {
                    showExportMessage("Failed to open stream for: " + csvFile.getName());
                    overallSuccess = false;
                    continue;
                  }
                }

                if (!filesWithHeaders.contains(csvFile)) {
                  String currentHeaders = "";
                  switch (dataType) {
                    case "Drawing":
                      currentHeaders = Constants.COLUMN_HEADERS;
                      break;
                    case "Gravity":
                      currentHeaders = Constants.GRAVITY_COLUMN_HEADERS;
                      break;
                    case "Gyroscope":
                      currentHeaders = Constants.GYROSCOPE_COLUMN_HEADERS;
                      break;
                    case "Magnetic Field":
                      currentHeaders = Constants.MAGNETIC_FIELD_COLUMN_HEADERS;
                      break;
                    case "Magnetic Baseline":
                      currentHeaders = Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS;
                      break;
                    case "Accelerometer":
                      currentHeaders = Constants.ACCELEROMETER_COLUMN_HEADERS;
                      break;
                  }
                  if (!currentHeaders.isEmpty()) {
                    outputStream.write(currentHeaders.getBytes());
                  }
                  filesWithHeaders.add(csvFile);
                }

                // Call specific write method - for DrawingData, it's always
                // writeDrawingDataToStream
                // The switch (dataType) here was incorrect as dataType is always "Drawing"
                writeDrawingDataToStream(outputStream, imageDataOnPage);

              } catch (IOException e) {
                showExportMessage("Error writing " + dataType + " to: " + csvFile.getName());
                overallSuccess = false;
              }
            }
          }
          offset += pageData.size();
          hasMoreData = pageData.size() == EXPORT_PAGE_SIZE;
        } else {
          hasMoreData = false;
        }
      } while (hasMoreData);

    } catch (Exception e) {
      android.util.Log.e("ExportDrawingData", "Error exporting drawing data", e);
      overallSuccess = false;
    } finally {
      for (OutputStream stream : openStreams.values()) {
        try {
          stream.close();
        } catch (IOException e) {
          android.util.Log.e("ExportDrawingData", "Error closing stream", e);
        }
      }
      context.exportResults.put(dataType, overallSuccess);
      context.dataTypeLatch.countDown();
      context.progressUpdater.accept(dataType, overallSuccess);
    }
  }

  private void writeDrawingDataToStream(OutputStream outputStream, List<DrawingExportData> modeData)
  throws IOException {
    StringBuilder sb = new StringBuilder(CSV_SB_INITIAL_CAPACITY);
    for (DrawingExportData data : modeData) {
      sb.setLength(0); // Clear StringBuilder for new row
      sb.append(data.getId()).append(",");
      sb.append(data.getUserID()).append(",");
      sb.append("\"").append(data.getUserName().replace("\"", "\"\"")).append("\",");
      sb.append(data.getAttempt()).append(",");
      sb.append(data.getTime()).append(",");
      sb.append(data.getX()).append(",");
      sb.append(data.getY()).append(",");
      sb.append("\"").append(data.getAction().replace("\"", "\"\"")).append("\",");
      sb
          .append("\"")
          .append(data.getItemType() != null
                  ? data.getItemType().toString().replace("\"", "\"\"")
                  : "")
          .append("\",");
      sb.append(data.getImageID()).append(",");
      sb.append("\"").append(data.getImageName().replace("\"", "\"\"")).append("\",");
      sb.append("\"").append(data.getDrawingMode().replace("\"", "\"\"")).append("\",");
      sb.append(data.getSize()).append(",");
      sb.append(data.getPressure()).append(",");
      sb.append(data.getOrientation());
      sb.append("\n"); // Add newline
      outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private void showExportMessage(String message) {
    requireActivity().runOnUiThread(() -> Toast
        .makeText(getContext(), message, Toast.LENGTH_LONG)
        .show());
  }

  private void resetUserProgress() {

    for (UserProgressManager.UserProgress progress : unfinishedUsers) {
      resetSingleUserProgress(progress);
    }

    requireActivity().runOnUiThread(() -> {
      showToast("User progress reset successfully");
      unfinishedUsers.clear();
      updatePreferencesVisibility();
    });
  }

  private void showToast(String message) {
    requireActivity().runOnUiThread(() -> Toast
        .makeText(getContext(), message, Toast.LENGTH_LONG)
        .show());
  }

  private void setupResetUserProgressPreference() {
    if (resetUserProgressPreference != null) {
      resetUserProgressPreference.setTitleColor(getResources().getColor(
          R.color.error,
          requireContext().getTheme()
      ));

      if (unfinishedUsers.isEmpty()) {
        resetUserProgressPreference.setVisible(false);
      }

      resetUserProgressPreference.setOnPreferenceClickListener(preference -> {

        String userNames = buildUnfinishedUsersList();

        String dialogMessage = String.format(
            "Resetting progress will delete all unfinished drawing sessions for the listed users,"
            + " allowing them to start fresh.%n%n"
            + "This action will:%n"
            + " • Delete all in-progress drawing data%n"
            + " • Reset current attempts to zero%n%n"
            + "%s", userNames
        );

        Drawable dialogIcon = ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        );

        if (dialogIcon != null) {
          dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        }

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Reset User Progress")
            .setMessage(dialogMessage)
            .setIcon(dialogIcon)
            .setPositiveButton(
                DELETE_BUTTON,
                (dialog, which) -> executor.execute(this::resetUserProgress)
            )
            .setNegativeButton(CANCEL_BUTTON, null)
            .create();

        alertDialog.show();

        try {
          int errorColor = getResources().getColor(R.color.error, requireContext().getTheme());
          alertDialog.setOnShowListener(dialog -> alertDialog
              .getButton(DialogInterface.BUTTON_POSITIVE)
              .setTextColor(errorColor));
        } catch (Exception ignored) {
          // Ignore any errors setting the color
        }

        return true;
      });
    }
  }

  private String buildUnfinishedUsersList() {
    if (unfinishedUsers.isEmpty()) {
      return "No unfinished sessions found.";
    }

    StringBuilder builder = new StringBuilder();
    builder.append("The following users have unfinished drawing sessions:\n");

    for (UserProgressManager.UserProgress progress : unfinishedUsers) {
      String userProgressInfo = buildUserProgressInfo(progress);
      builder.append(userProgressInfo);
    }

    builder.append("\nDo you want to reset progress for these users?");
    return builder.toString();
  }

  private String buildUserProgressInfo(UserProgressManager.UserProgress progress) {
    UserProgressManager.Session session = UserProgressManager.getSession(
        requireContext(),
        progress.getUserId(),
        progress.getSessionId()
    );

    SessionInfo sessionInfo = extractSessionInfo(session);
    ProgressInfo progressInfo = calculateProgressInfo(progress, sessionInfo);

    return String.format(
        Locale.getDefault(),
        " • %s:%n" + "   ⸰ Progress: %d/%s (%d/%d)%n" + "   ⸰ Mode: %s%n",
        progress.getUserName(),
        progressInfo.currentItemAttempt,
        sessionInfo.attempts,
        progressInfo.overallAttempt,
        progressInfo.totalAttempts,
        sessionInfo.drawingMode
    );
  }

  private SessionInfo extractSessionInfo(UserProgressManager.Session session) {
    String attempts = UNKNOWN_VALUE;
    String drawingMode = "normal";
    int totalImages = 0;

    if (session != null && session.getSettings() != null) {
      attempts = extractAttempts(session);
      drawingMode = extractDrawingMode(session);
      totalImages = extractTotalImages(session);
    }

    if (totalImages == 0) {
      totalImages = 1;
    }

    return new SessionInfo(attempts, drawingMode, totalImages);
  }

  private String extractAttempts(UserProgressManager.Session session) {
    Object attemptsObj = session.getSettings().get(Constants.DRAWING_KEY_ATTEMPTS);
    return attemptsObj != null ? attemptsObj.toString() : UNKNOWN_VALUE;
  }

  private String extractDrawingMode(UserProgressManager.Session session) {
    Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
    return modeObj != null ? modeObj.toString() : "normal";
  }

  private int extractTotalImages(UserProgressManager.Session session) {
    try {
      Object selectedImagesObj = session
          .getSettings()
          .get(Constants.DRAWING_KEY_SELECTED_IMAGE_IDS);
      if (selectedImagesObj != null) {
        return parseSelectedImages(selectedImagesObj);
      }
    } catch (Exception ignored) {
      // Ignore any parsing errors
    }

    Set<Integer> selectedImages = SelectedImagesManager.getSelectedImages(requireContext());
    return selectedImages.size();
  }

  private int parseSelectedImages(Object selectedImagesObj) {
    if (selectedImagesObj instanceof List) {
      return ((List<?>) selectedImagesObj).size();
    } else if (selectedImagesObj.toString().startsWith("[") && selectedImagesObj
        .toString()
        .endsWith("]")) {
      String arrayStr = selectedImagesObj.toString();
      if (arrayStr.length() > 2) {
        return arrayStr.split(",").length;
      }
    }
    return 0;
  }

  private void resetSingleUserProgress(UserProgressManager.UserProgress progress) {
    long userId = progress.getUserId();

    List<UserProgressManager.Session> unfinishedSessions
        = UserProgressManager.getUnfinishedSessions(requireContext(), userId);

    for (UserProgressManager.Session session : unfinishedSessions) {
      resetSession(userId, session.getSessionId());
    }

    UserProgressManager.clearUserProgress(requireContext(), userId);
  }

  /**
   * Process drawing data to add drawing mode information from session settings
   */
  private void processDrawingModes(
      List<DrawingExportData> drawingDataList,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    processDataModes(
        drawingDataList,
        DrawingExportData::getSessionID,
        DrawingExportData::getUserID,
        DrawingExportData::setDrawingMode,
        sessionCache
    );
  }

  /**
   * Generic method to process any data type for drawing modes
   */
  private <T> void processDataModes(
      List<T> dataList,
      Function<T, String> sessionIdGetter,
      ToLongFunction<T> userIdGetter,
      BiConsumer<T, String> drawingModeSetter,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    for (T data : dataList) {
      String sessionId = sessionIdGetter.apply(data);

      if (sessionId != null) {
        long userId = userIdGetter.applyAsLong(data);
        UserProgressManager.Session session = sessionCache.get(sessionId);
        if (session == null) {
          session = UserProgressManager.getSession(requireContext(), userId, sessionId);
          if (session != null) { // Add to cache only if session is successfully loaded
            sessionCache.put(sessionId, session);
          }
        }

        String drawingMode = extractDrawingModeFromSession(session);
        drawingModeSetter.accept(data, drawingMode);
      } else {
        drawingModeSetter.accept(data, Constants.DRAWING_MODE_NORMAL);
      }
    }
  }

  private String extractDrawingModeFromSession(UserProgressManager.Session session) {
    if (session != null && session.getSettings() != null) {
      Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
      return modeObj != null ? modeObj.toString() : Constants.DRAWING_MODE_NORMAL;
    }
    return Constants.DRAWING_MODE_NORMAL;
  }

  /**
   * Checks for unfinished users during initial load
   * This is separate from the onResume check to prevent UI jumping
   */
  private void checkUnfinishedUsersInitial() {
    unfinishedUsers = UserProgressManager.getUnfinishedUsers(requireContext(), db);

    if (!unfinishedUsers.isEmpty()) {
      requireActivity().runOnUiThread(this::updatePreferencesVisibility);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    executor.execute(() -> {
      List<UserProgressManager.UserProgress> newUnfinishedUsers
          = UserProgressManager.getUnfinishedUsers(requireContext(), db);

      boolean wasEmpty = unfinishedUsers.isEmpty();
      boolean isEmpty = newUnfinishedUsers.isEmpty();

      if (wasEmpty != isEmpty) {
        unfinishedUsers = newUnfinishedUsers;
        requireActivity().runOnUiThread(this::updatePreferencesVisibility);
      }
    });
  }

  private void updatePreferencesVisibility() {
    boolean hasUnfinishedUsers = !unfinishedUsers.isEmpty();

    if (hasUnfinishedUsers) {

      if (resetUserProgressPreference != null) {
        resetUserProgressPreference.setVisible(true);
      }

      if (drawingSettingsCategory != null) {
        drawingSettingsCategory.setEnabled(false);
      }

      if (dataManagementCategory != null) {
        dataManagementCategory.setEnabled(false);
      }
    } else {

      if (resetUserProgressPreference != null) {
        resetUserProgressPreference.setVisible(false);
      }

      if (drawingSettingsCategory != null) {
        drawingSettingsCategory.setEnabled(true);
      }

      if (dataManagementCategory != null) {
        dataManagementCategory.setEnabled(true);
      }
    }
  }

  private void resetSession(long userId, String sessionId) {

    UserProgressManager.markSessionFinished(requireContext(), userId, sessionId);

    db.drawingDataDao().deleteUserSessionData(userId, sessionId);
  }

  private File createExportDirectory() {
    File exportDir
        = new File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Constants.SKETCHID_DATA_DIR
    );
    if (!exportDir.exists() && !exportDir.mkdirs()) {
      showToast(Constants.FAILED_EXPORT_MESSAGE);
      return null;
    }
    return exportDir;
  }

  private ProgressInfo calculateProgressInfo(
      UserProgressManager.UserProgress progress,
      SessionInfo sessionInfo
  ) {
    int currentItemIndex = progress.getItemIndex();
    int currentItemAttempt = progress.getItemAttempt();
    int attemptsPerImage = parseAttemptsPerImage(sessionInfo.attempts);

    int overallAttempt = (currentItemIndex * attemptsPerImage) + currentItemAttempt;
    int totalAttempts = sessionInfo.totalImages * attemptsPerImage;

    return new ProgressInfo(currentItemAttempt, overallAttempt, totalAttempts);
  }

  private int parseAttemptsPerImage(String attempts) {
    try {
      if (!attempts.equals(UNKNOWN_VALUE)) {
        return Integer.parseInt(attempts);
      }
    } catch (NumberFormatException ignored) {
      // Ignore any parsing errors
    }
    return 1;
  }

  private String createTimestamp() {
    return new SimpleDateFormat(
        Constants.EXPORT_DATE_FORMAT,
                                Locale.getDefault()
    ).format(new Date());
  }

  private Map<String, List<DrawingExportData>> groupDataByDrawingMode(List<DrawingExportData> drawingDataList) {
    return groupDataByMode(drawingDataList, DrawingExportData::getDrawingMode);
  }

  private void processGravityDrawingModes(
      List<GravityExportData> gravityDataList,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    processDataModes(
        gravityDataList,
        GravityExportData::getSessionId,
        GravityExportData::getUserId,
        GravityExportData::setDrawingMode,
        sessionCache
    );
  }

  private void processGyroscopeDrawingModes(
      List<GyroscopeExportData> gyroscopeDataList,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    processDataModes(
        gyroscopeDataList,
        GyroscopeExportData::getSessionId,
        GyroscopeExportData::getUserId,
        GyroscopeExportData::setDrawingMode,
        sessionCache
    );
  }

  private void processMagneticFieldDrawingModes(
      List<MagneticFieldExportData> magneticFieldDataList,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    processDataModes(
        magneticFieldDataList,
        MagneticFieldExportData::getSessionId,
        MagneticFieldExportData::getUserId,
        MagneticFieldExportData::setDrawingMode,
        sessionCache
    );
  }

  private void processMagneticFieldBaselineDrawingModes(
      List<MagneticFieldBaselineExportData> magneticFieldBaselineDataList,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    processDataModes(
        magneticFieldBaselineDataList,
        MagneticFieldBaselineExportData::getSessionId,
        MagneticFieldBaselineExportData::getUserId,
        MagneticFieldBaselineExportData::setDrawingMode,
        sessionCache
    );
  }

  private File createModeDirectory(File userDir, String mode) {
    File modeDir = new File(userDir, mode);
    if (!modeDir.exists() && !modeDir.mkdirs()) {
      showExportMessage("Failed to create mode directory: " + mode);
      return null;
    }
    return modeDir;
  }

  private void processAccelerometerDrawingModes(
      List<AccelerometerExportData> accelerometerDataList,
      Map<String, UserProgressManager.Session> sessionCache
  ) {
    processDataModes(
        accelerometerDataList,
        AccelerometerExportData::getSessionId,
        AccelerometerExportData::getUserId,
        AccelerometerExportData::setDrawingMode,
        sessionCache
    );
  }

  private Map<String, List<GravityExportData>> groupGravityDataByDrawingMode(List<GravityExportData> gravityDataList) {
    return groupDataByMode(gravityDataList, GravityExportData::getDrawingMode);
  }

  private <T> Map<String, List<T>> groupDataByMode(
      List<T> dataList,
      Function<T, String> modeGetter
  ) {
    Map<String, List<T>> dataByMode = new HashMap<>();
    dataByMode.put(Constants.DRAWING_MODE_NORMAL, new ArrayList<>());
    dataByMode.put(Constants.DRAWING_MODE_OVERLAY, new ArrayList<>());
    for (T data : dataList) {
      String mode = modeGetter.apply(data);
      dataByMode.computeIfAbsent(mode, k -> new ArrayList<>()).add(data);
    }
    return dataByMode;
  }

  private Map<String, List<DrawingExportData>> groupDrawingDataByImage(List<DrawingExportData> data) {
    Map<String, List<DrawingExportData>> dataByImage = new HashMap<>();
    for (DrawingExportData item : data) {
      String imageName = item.getImageName();
      if (imageName == null || imageName.isEmpty()) {
        imageName = UNKNOWN_IMAGE;
      }
      dataByImage.computeIfAbsent(imageName, k -> new ArrayList<>()).add(item);
    }
    return dataByImage;
  }

  private String formatDataTypeForFilename(String dataPrefix) {
    return dataPrefix.replaceAll("_$|^_", "").toLowerCase();
  }

  private void launchSensorDataExport(
      ExportTaskContext context,
      String[] dataTypes,
      String[] dataPrefixes,
      int index
  ) {
    boolean overallSuccess = true;
    String dataType = dataTypes[index];
    String dataPrefix = dataPrefixes[index];
    Map<String, UserProgressManager.Session> sessionCache = context.sessionCache;

    Map<File, OutputStream> openStreams = new HashMap<>();
    Set<File> filesWithHeaders = new HashSet<>();

    try {
      int offset = 0;
      boolean hasMoreData;

      do {
        List<? extends ExportableData> pageData = null;
        // String specificColumnHeaders = ""; // No longer needed here
        // SensorDataWriter<?> specificWriter = null; // No longer needed

        switch (index) {
          case 1: // Gravity
            pageData = db
                .gravityDataDao()
                .getPaginatedGravityDataWithUsersAndImagesByUserId(
                    context.userId,
                    EXPORT_PAGE_SIZE,
                    offset
                );
            if (pageData != null && !pageData.isEmpty())
              processGravityDrawingModes((List<GravityExportData>) pageData, sessionCache);
            // specificColumnHeaders = Constants.GRAVITY_COLUMN_HEADERS; // Moved
            // specificWriter = (outputStream, dataList) -> writeGravityDataToStream
            // (outputStream, (List<GravityExportData>) dataList); // Removed
            break;
          case 2: // Gyroscope
            pageData = db
                .gyroscopeDataDao()
                .getPaginatedGyroscopeDataWithUsersAndImagesByUserId(
                    context.userId,
                    EXPORT_PAGE_SIZE,
                    offset
                );
            if (pageData != null && !pageData.isEmpty())
              processGyroscopeDrawingModes((List<GyroscopeExportData>) pageData, sessionCache);
            // specificColumnHeaders = Constants.GYROSCOPE_COLUMN_HEADERS; // Moved
            // specificWriter = (outputStream, dataList) -> writeGyroscopeDataToStream
            // (outputStream, (List<GyroscopeExportData>) dataList); // Removed
            break;
          case 3: // Magnetic Field
            pageData = db
                .magneticFieldDataDao()
                .getPaginatedMagneticFieldDataWithUsersAndImagesByUserId(
                    context.userId,
                    EXPORT_PAGE_SIZE,
                    offset
                );
            if (pageData != null && !pageData.isEmpty())
              processMagneticFieldDrawingModes(
                  (List<MagneticFieldExportData>) pageData,
                  sessionCache
              );
            // specificColumnHeaders = Constants.MAGNETIC_FIELD_COLUMN_HEADERS; // Moved
            // specificWriter = (outputStream, dataList) -> writeMagneticFieldDataToStream
            // (outputStream, (List<MagneticFieldExportData>) dataList); // Removed
            break;
          case 4: // Magnetic Baseline
            pageData = db
                .magneticFieldBaselineDataDao()
                .getPaginatedBaselineDataWithUsersAndImagesByUserId(
                    context.userId,
                    EXPORT_PAGE_SIZE,
                    offset
                );
            if (pageData != null && !pageData.isEmpty())
              processMagneticFieldBaselineDrawingModes(
                  (List<MagneticFieldBaselineExportData>) pageData,
                  sessionCache
              );
            // specificColumnHeaders = Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS; // Moved
            // specificWriter = (outputStream, dataList) ->
            // writeMagneticFieldBaselineDataToStream(outputStream,
            // (List<MagneticFieldBaselineExportData>) dataList); // Removed
            break;
          case 5: // Accelerometer
            pageData = db
                .accelerometerDataDao()
                .getPaginatedAccelerometerDataWithUsersAndImagesByUserId(
                    context.userId,
                    EXPORT_PAGE_SIZE,
                    offset
                );
            if (pageData != null && !pageData.isEmpty())
              processAccelerometerDrawingModes(
                  (List<AccelerometerExportData>) pageData,
                  sessionCache
              );
            // specificColumnHeaders = Constants.ACCELEROMETER_COLUMN_HEADERS; // Moved
            // specificWriter = (outputStream, dataList) -> writeAccelerometerDataToStream
            // (outputStream, (List<AccelerometerExportData>) dataList); // Removed
            break;
          default:
            throw new IllegalArgumentException("Invalid data type index: " + index);
        }

        if (pageData != null && !pageData.isEmpty()) {
          Map<String, List<? extends ExportableData>> dataByModeOnPage
              = (Map<String, List<? extends ExportableData>>) (Map<?, ?>) groupDataByMode(
              pageData,
              ExportableData::getDrawingMode
          );

          for (Map.Entry<String, List<? extends ExportableData>> modeEntry :
              dataByModeOnPage.entrySet()) {
            String mode = modeEntry.getKey();
            List<? extends ExportableData> modeDataOnPage = modeEntry.getValue();
            File modeDir = createModeDirectory(context.userDir, mode);
            if (modeDir == null) {
              overallSuccess = false;
              continue;
            }

            Map<String, List<? extends ExportableData>> dataByImageOnPage
                = (Map<String, List<? extends ExportableData>>) (Map<?, ?>) groupSensorDataByImage(
                modeDataOnPage);

            for (Map.Entry<String, List<? extends ExportableData>> imageEntry :
                dataByImageOnPage.entrySet()) {
              String imageName = imageEntry.getKey();
              List<? extends ExportableData> imageDataOnPage = imageEntry.getValue();

              String formattedImageName = imageName.toLowerCase().replace(" ", "_");
              String formattedDataType = formatDataTypeForFilename(dataPrefix);
              String fileName = formattedImageName + "_" + formattedDataType + ".csv";
              File csvFile = new File(modeDir, fileName);

              try {
                OutputStream outputStream = openStreams.get(csvFile);
                if (outputStream == null) {
                  OutputStream rawOutputStream
                      = context.contentResolver.openOutputStream(Uri.fromFile(csvFile));
                  if (rawOutputStream != null) {
                    outputStream = new BufferedOutputStream(rawOutputStream, BUFFERED_STREAM_SIZE);
                    openStreams.put(csvFile, outputStream);
                  } else {
                    showExportMessage("Failed to open stream for: " + csvFile.getName());
                    overallSuccess = false;
                    continue;
                  }
                }

                if (!filesWithHeaders.contains(csvFile)) {
                  String currentHeaders = "";
                  switch (index) {
                    case 1:
                      currentHeaders = Constants.GRAVITY_COLUMN_HEADERS;
                      break;
                    case 2:
                      currentHeaders = Constants.GYROSCOPE_COLUMN_HEADERS;
                      break;
                    case 3:
                      currentHeaders = Constants.MAGNETIC_FIELD_COLUMN_HEADERS;
                      break;
                    case 4:
                      currentHeaders = Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS;
                      break;
                    case 5:
                      currentHeaders = Constants.ACCELEROMETER_COLUMN_HEADERS;
                      break;
                  }
                  if (!currentHeaders.isEmpty()) {
                    outputStream.write(currentHeaders.getBytes());
                  }
                  filesWithHeaders.add(csvFile);
                }

                // Call specific write method
                switch (index) {
                  case 1:
                    writeGravityDataToStream(
                        outputStream,
                        (List<GravityExportData>) imageDataOnPage
                    );
                    break;
                  case 2:
                    writeGyroscopeDataToStream(
                        outputStream,
                        (List<GyroscopeExportData>) imageDataOnPage
                    );
                    break;
                  case 3:
                    writeMagneticFieldDataToStream(
                        outputStream,
                        (List<MagneticFieldExportData>) imageDataOnPage
                    );
                    break;
                  case 4:
                    writeMagneticFieldBaselineDataToStream(
                        outputStream,
                        (List<MagneticFieldBaselineExportData>) imageDataOnPage
                    );
                    break;
                  case 5:
                    writeAccelerometerDataToStream(
                        outputStream,
                        (List<AccelerometerExportData>) imageDataOnPage
                    );
                    break;
                }

              } catch (IOException e) {
                showExportMessage("Error writing " + dataPrefix + " to: " + csvFile.getName());
                overallSuccess = false;
              }
            }
          }
          offset += pageData.size();
          hasMoreData = pageData.size() == EXPORT_PAGE_SIZE;
        } else {
          hasMoreData = false;
        }
      } while (hasMoreData);

    } catch (Exception e) {
      android.util.Log.e(
          "ExportSensorData",
          "Error exporting " + dataType + PROGRESS_ITEM_SUFFIX,
          e
      );
      overallSuccess = false;
    } finally {
      for (OutputStream stream : openStreams.values()) {
        try {
          stream.close();
        } catch (IOException e) {
          android.util.Log.e("ExportSensorData", "Error closing stream for " + dataType, e);
        }
      }
      context.exportResults.put(dataType, overallSuccess);
      context.dataTypeLatch.countDown();
      context.progressUpdater.accept(dataType, overallSuccess);
    }
  }

  private Map<String, List<GyroscopeExportData>> groupGyroscopeDataByDrawingMode(List<GyroscopeExportData> gyroscopeDataList) {
    return groupDataByMode(gyroscopeDataList, GyroscopeExportData::getDrawingMode);
  }

  private <T> Map<String, List<T>> groupSensorDataByImage(List<T> data) {
    Map<String, List<T>> dataByImage = new HashMap<>();
    for (T item : data) {
      String imageName = getSensorDataImageName(item);
      if (imageName == null || imageName.isEmpty()) {
        imageName = UNKNOWN_IMAGE;
      }
      dataByImage.computeIfAbsent(imageName, k -> new ArrayList<>()).add(item);
    }
    return dataByImage;
  }

  private String getSensorDataImageName(Object sensorData) {
    if (sensorData instanceof GravityExportData) {
      return ((GravityExportData) sensorData).getImageName();
    } else if (sensorData instanceof GyroscopeExportData) {
      return ((GyroscopeExportData) sensorData).getImageName();
    } else if (sensorData instanceof MagneticFieldExportData) {
      return ((MagneticFieldExportData) sensorData).getImageName();
    } else if (sensorData instanceof MagneticFieldBaselineExportData) {
      return ((MagneticFieldBaselineExportData) sensorData).getImageName();
    } else if (sensorData instanceof AccelerometerExportData) {
      return ((AccelerometerExportData) sensorData).getImageName();
    }
    return UNKNOWN_IMAGE;
  }

  private void writeGravityDataToStream(OutputStream outputStream, List<GravityExportData> data)
  throws IOException {
    StringBuilder sb = new StringBuilder(CSV_SB_INITIAL_CAPACITY);
    for (GravityExportData item : data) {
      sb.setLength(0);
      sb.append(item.getId()).append(",");
      sb.append(item.getUserId()).append(",");
      sb.append("\"").append(item.getUserName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getImageId()).append(",");
      sb.append("\"").append(item.getImageName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getAttempt()).append(",");
      sb.append(item.getTimestamp()).append(",");
      sb.append(item.getX()).append(",");
      sb.append(item.getY()).append(",");
      sb.append(item.getZ()).append(",");
      sb.append("\"").append(item.getDrawingMode().replace("\"", "\"\"")).append("\"");
      sb.append("\n");
      outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private void writeGyroscopeDataToStream(OutputStream outputStream, List<GyroscopeExportData> data)
  throws IOException {
    StringBuilder sb = new StringBuilder(CSV_SB_INITIAL_CAPACITY);
    for (GyroscopeExportData item : data) {
      sb.setLength(0);
      sb.append(item.getId()).append(",");
      sb.append(item.getUserId()).append(",");
      sb.append("\"").append(item.getUserName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getImageId()).append(",");
      sb.append("\"").append(item.getImageName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getAttempt()).append(",");
      sb.append(item.getTimestamp()).append(",");
      sb.append(item.getX()).append(",");
      sb.append(item.getY()).append(",");
      sb.append(item.getZ()).append(",");
      sb.append("\"").append(item.getDrawingMode().replace("\"", "\"\"")).append("\"");
      sb.append("\n");
      outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private void writeMagneticFieldDataToStream(
      OutputStream outputStream,
      List<MagneticFieldExportData> data
  )
  throws IOException {
    StringBuilder sb = new StringBuilder(CSV_SB_INITIAL_CAPACITY);
    for (MagneticFieldExportData item : data) {
      sb.setLength(0);
      sb.append(item.getId()).append(",");
      sb.append(item.getUserId()).append(",");
      sb.append("\"").append(item.getUserName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getImageId()).append(",");
      sb.append("\"").append(item.getImageName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getAttempt()).append(",");
      sb.append(item.getTimestamp()).append(",");
      sb.append(item.getX()).append(",");
      sb.append(item.getY()).append(",");
      sb.append(item.getZ()).append(",");
      sb.append("\"").append(item.getDrawingMode().replace("\"", "\"\"")).append("\"");
      sb.append("\n");
      outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private void writeMagneticFieldBaselineDataToStream(
      OutputStream outputStream,
      List<MagneticFieldBaselineExportData> data
  )
  throws IOException {
    StringBuilder sb = new StringBuilder(CSV_SB_INITIAL_CAPACITY);
    for (MagneticFieldBaselineExportData item : data) {
      sb.setLength(0);
      sb.append(item.getId()).append(",");
      sb.append(item.getUserId()).append(",");
      sb.append("\"").append(item.getUserName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getImageId()).append(",");
      sb.append("\"").append(item.getImageName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getAttempt()).append(",");
      sb.append(item.getTimestamp()).append(",");
      sb.append(item.getAvgX()).append(",");
      sb.append(item.getAvgY()).append(",");
      sb.append(item.getAvgZ()).append(",");
      sb.append(item.getDurationMs()).append(",");
      sb.append(item.getSampleCount()).append(",");
      sb.append(item.getStdDevX()).append(",");
      sb.append(item.getStdDevY()).append(",");
      sb.append(item.getStdDevZ()).append(",");
      sb.append("\"").append(item.getDrawingMode().replace("\"", "\"\"")).append("\"");
      sb.append("\n");
      outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private void writeAccelerometerDataToStream(
      OutputStream outputStream,
      List<AccelerometerExportData> data
  )
  throws IOException {
    StringBuilder sb = new StringBuilder(CSV_SB_INITIAL_CAPACITY);
    for (AccelerometerExportData item : data) {
      sb.setLength(0);
      sb.append(item.getId()).append(",");
      sb.append(item.getUserId()).append(",");
      sb.append("\"").append(item.getUserName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getImageId()).append(",");
      sb.append("\"").append(item.getImageName().replace("\"", "\"\"")).append("\",");
      sb.append(item.getAttempt()).append(",");
      sb.append(item.getTimestamp()).append(",");
      sb.append(item.getX()).append(",");
      sb.append(item.getY()).append(",");
      sb.append(item.getZ()).append(",");
      sb.append("\"").append(item.getDrawingMode().replace("\"", "\"\"")).append("\"");
      sb.append("\n");
      outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private Map<String, List<MagneticFieldExportData>> groupMagneticFieldDataByDrawingMode(List<MagneticFieldExportData> magneticFieldDataList) {
    return groupDataByMode(magneticFieldDataList, MagneticFieldExportData::getDrawingMode);
  }

  private Map<String, List<MagneticFieldBaselineExportData>> groupMagneticFieldBaselineDataByDrawingMode(
      List<MagneticFieldBaselineExportData> magneticFieldBaselineDataList
  ) {
    return groupDataByMode(
        magneticFieldBaselineDataList,
        MagneticFieldBaselineExportData::getDrawingMode
    );
  }

  private Map<String, List<AccelerometerExportData>> groupAccelerometerDataByDrawingMode(List<AccelerometerExportData> accelerometerDataList) {
    return groupDataByMode(accelerometerDataList, AccelerometerExportData::getDrawingMode);
  }

  private void setupSelectImagePreference() {
    if (selectImagePreference != null) {
      ActivityResultLauncher<Intent> imageSelectionLauncher = registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK
                && result.getData() != null) {
              int selectedImageId = result.getData().getIntExtra("selected_image_id", -1);
              if (selectedImageId != -1) {
                Toast
                    .makeText(
                        getContext(),
                        "Image selected: " + selectedImageId,
                        Toast.LENGTH_SHORT
                    )
                    .show();
              }
            }
          }
      );

      selectImagePreference.setOnPreferenceClickListener(preference -> {
        Intent intent = new Intent(getActivity(), ImageSelectionActivity.class);
        imageSelectionLauncher.launch(intent);
        return true;
      });
    }
  }

  private void setupDeleteUserDataPreference() {
    if (deleteUserDataPreference != null) {
      deleteUserDataPreference.setTitleColor(getResources().getColor(
          R.color.error,
          requireContext().getTheme()
      ));

      deleteUserDataPreference.setOnPreferenceClickListener(preference -> {
        if (isNotValidUserSelected()) {
          return true;
        }
        String dialogMessage = createDeleteUserDataMessage();
        Drawable dialogIcon = createWarningIcon();
        AlertDialog alertDialog = createDeleteUserDataDialog(dialogMessage, dialogIcon);
        alertDialog.show();
        applyErrorTintToPositiveButton(alertDialog);
        return true;
      });
    }
  }

  private String createDeleteUserDataMessage() {
    return String.format(
        "Are you sure you want to delete drawing data for "
        + "user: %s?%n%nThis will remove all "
        + "drawing history but keep the user profile.", userListPreference.getEntry()
    );
  }

  private AlertDialog createDeleteUserDataDialog(String dialogMessage, Drawable dialogIcon) {
    return new AlertDialog.Builder(requireContext())
        .setTitle("Delete User Data?")
        .setMessage(dialogMessage)
        .setIcon(dialogIcon)
        .setPositiveButton(DELETE_BUTTON, (dialog, which) -> executeUserDataDeletion())
        .setNegativeButton(CANCEL_BUTTON, null)
        .create();
  }

  private void executeUserDataDeletion() {
    executor.execute(() -> {
      int deletedRows = db
          .drawingDataDao()
          .deleteDrawingDataByUserID(Long.parseLong(selectedUserID));
      if (deletedRows > 0) {
        requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "User drawing data deleted", Toast.LENGTH_LONG)
            .show());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
      } else {
        requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "Failed to delete user drawing data", Toast.LENGTH_LONG)
            .show());
      }
    });
  }

  private void setupClearDataPreference() {
    if (clearDataPreference != null) {
      clearDataPreference.setTitleColor(getResources().getColor(
          R.color.error,
          requireContext().getTheme()
      ));
      clearDataPreference.setOnPreferenceClickListener(preference -> {
        String dialogMessage = "Are you sure you want to delete ALL drawing data for ALL users?\n\n"
                               + "This action will permanently remove all drawing data from the "
                               + "database and cannot be undone.";
        Drawable dialogIcon = ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        );
        if (dialogIcon != null) {
          dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        }
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Clear All Drawing Data?")
            .setMessage(dialogMessage)
            .setIcon(dialogIcon)
            .setPositiveButton(
                DELETE_BUTTON, (dialog, which) -> executor.execute(() -> {
                  db.drawingDataDao().deleteAllDrawingData();
                  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                      requireContext());
                  prefs
                      .edit()
                      .remove(KEY_CURRENT_ITEM_ATTEMPT)
                      .remove(KEY_CURRENT_ITEM_INDEX)
                      .apply();
                  requireActivity().runOnUiThread(() -> Toast
                      .makeText(requireContext(), "All drawing data cleared!", Toast.LENGTH_LONG)
                      .show());
                })
            )
            .setNegativeButton(CANCEL_BUTTON, null)
            .create();
        alertDialog.show();
        try {
          int errorColor = getResources().getColor(R.color.error, requireContext().getTheme());
          alertDialog.setOnShowListener(dialog -> alertDialog
              .getButton(DialogInterface.BUTTON_POSITIVE)
              .setTextColor(errorColor));
        } catch (Exception ignored) {
          // Ignore any errors setting the color
        }
        return true;
      });
    }
  }

  @FunctionalInterface
  private interface SensorDataWriter<T> {
    void writeData(OutputStream outputStream, List<T> data)
    throws IOException;
  }

  private static class ExportTaskContext {
    final long userId;
    final File userDir;
    final ContentResolver contentResolver;
    final CountDownLatch dataTypeLatch;
    final ConcurrentHashMap<String, Boolean> exportResults;
    final BiConsumer<String, Boolean> progressUpdater;
    final Map<String, UserProgressManager.Session> sessionCache;

    ExportTaskContext(
        long userId,
        File userDir,
        ContentResolver contentResolver,
        CountDownLatch dataTypeLatch,
        ConcurrentHashMap<String, Boolean> exportResults,
        BiConsumer<String, Boolean> progressUpdater,
        Map<String, UserProgressManager.Session> sessionCache
    ) {
      this.userId = userId;
      this.userDir = userDir;
      this.contentResolver = contentResolver;
      this.dataTypeLatch = dataTypeLatch;
      this.exportResults = exportResults;
      this.progressUpdater = progressUpdater;
      this.sessionCache = sessionCache;
    }
  }

  private static class SessionInfo {
    final String attempts;
    final String drawingMode;
    final int totalImages;

    SessionInfo(String attempts, String drawingMode, int totalImages) {
      this.attempts = attempts;
      this.drawingMode = drawingMode;
      this.totalImages = totalImages;
    }
  }

  private static class ProgressInfo {
    final int currentItemAttempt;
    final int overallAttempt;
    final int totalAttempts;

    ProgressInfo(int currentItemAttempt, int overallAttempt, int totalAttempts) {
      this.currentItemAttempt = currentItemAttempt;
      this.overallAttempt = overallAttempt;
      this.totalAttempts = totalAttempts;
    }
  }
}
