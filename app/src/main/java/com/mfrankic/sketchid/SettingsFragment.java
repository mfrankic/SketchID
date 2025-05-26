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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class SettingsFragment extends PreferenceFragmentCompat {

  private static final String DIALOG_PREFERENCE_KEY = "dialog_preference_key";
  private static final String DIALOG_RESULT_KEY = "dialog_result";
  private static final String UNKNOWN_VALUE = "unknown";
  private static final String UNKNOWN_IMAGE = "unknown_image";
  private static final String SKETCHID_DATA_PATH = "SketchIDData/";
  private static final String FIREBASE_PATH_SEPARATOR = "/";

  private EditTextPreference newUserPreference;
  private EditTextPreference drawingAttemptsPreference;
  private Preference exportDataPreference;
  private Preference uploadDataPreference;
  private Preference exportAllDataPreference;
  private Preference uploadAllDataPreference;
  private CustomDialogPreference clearDataPreference;
  private Executor executor;
  private AppDatabase db;
  private ListPreference userListPreference;
  private ListPreference drawingModePreference;
  private CustomDialogPreference deleteUserPreference;
  private CustomDialogPreference deleteUserDataPreference;
  private String selectedUserID;
  private Uri fileUri;
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
    uploadDataPreference = findPreference(getResources().getText(R.string.key_upload_data));
    exportAllDataPreference = findPreference(getResources().getText(R.string.key_export_all_data));
    uploadAllDataPreference = findPreference(getResources().getText(R.string.key_upload_all_data));
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
    setupUploadDataPreference();
    setupExportAllDataPreference();
    setupUploadAllDataPreference();
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
          uploadDataPreference.setEnabled(false);
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
          uploadDataPreference.setEnabled(false);
        }

        if (!selectedUserID.equals("-1")) {
          userListPreference.setValue(selectedUserID);
          userListPreference.callChangeListener(selectedUserID);
          deleteUserPreference.setEnabled(true);
          deleteUserDataPreference.setEnabled(true);
          exportDataPreference.setEnabled(true);
          uploadDataPreference.setEnabled(true);
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
          activity.showExportOverlay(true);
        }

        executor.execute(() -> {
          long startTime = System.currentTimeMillis();
          boolean success = exportDataToCSV(false);
          long endTime = System.currentTimeMillis();

          // Log the export time for performance monitoring
          android.util.Log.d(
              "ExportPerformance",
              "Parallel export completed in: " + (endTime - startTime) + "ms (" + String.format(
                  "%.1f",
                  (endTime - startTime) / 1000.0
              ) + " seconds)"
          );

          requireActivity().runOnUiThread(() -> {
            if (activity != null) {
              activity.hideExportOverlay();
            }

            if (!success) {
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

  private void uploadDataToFirebase(SettingsActivity activity) {
    boolean success = exportDataToCSV(true);
    if (!success || fileUri == null) {
      handleUploadFailure(activity);
      return;
    }

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    // Extract the file path from the fileUri to maintain the new directory structure
    String filePath = fileUri.getPath();
    if (filePath != null && filePath.contains(SKETCHID_DATA_PATH)) {
      uploadFileToFirebaseStorage(activity, storageRef, filePath);
    } else {
      handleUploadFailure(activity);
    }

    fileUri = null;
  }

  private void handleUploadFailure(SettingsActivity activity) {
    requireActivity().runOnUiThread(() -> {
      if (activity != null) {
        activity.hideUploadOverlay();
      }
      showExportMessage(getString(R.string.upload_failed));
    });
  }

  private void uploadFileToFirebaseStorage(
      SettingsActivity activity,
      StorageReference storageRef,
      String filePath
  ) {
    // Extract the relative path from SketchIDData onwards
    String relativePath = filePath.substring(filePath.indexOf(SKETCHID_DATA_PATH));
    StorageReference fileRef = storageRef.child(relativePath);

    UploadTask uploadTask = fileRef.putFile(fileUri);
    uploadTask
        .addOnSuccessListener(taskSnapshot -> handleUploadSuccess(activity))
        .addOnFailureListener(e -> handleUploadFailure(activity));
  }

  private void handleUploadSuccess(SettingsActivity activity) {
    requireActivity().runOnUiThread(() -> {
      if (activity != null) {
        activity.hideUploadOverlay();
      }
      showExportMessage(getString(R.string.upload_complete));
    });
  }

  /**
   * Export data to CSV using fully parallel approach: Query and export each data type in parallel
   * This approach pipelines the query and export operations for maximum performance
   */
  private boolean exportDataToCSV(boolean silent) {
    android.util.Log.d("ExportPerformance", "Starting parallel export (query + export)...");

    if (!isValidUserForExport()) {
      return false;
    }

    File exportDir = createExportDirectory();
    if (exportDir == null) {
      return false;
    }

    long userId = Long.parseLong(selectedUserID);
    ContentResolver contentResolver = requireActivity().getContentResolver();
    String timestamp = createTimestamp();

    // Create user directory with timestamp
    String userDirName = userListPreference.getEntry() + "_" + timestamp;
    File userDir = new File(exportDir, userDirName);
    if (!userDir.exists() && !userDir.mkdirs()) {
      showExportMessage("Failed to create user directory");
      return false;
    }

    android.util.Log.d("ExportPerformance", "Starting parallel query+export pipeline...");
    long startTime = System.currentTimeMillis();

    // Use thread pool for parallel query+export operations
    Executor parallelExecutor = Executors.newFixedThreadPool(6);

    // Use CountDownLatch for synchronization
    java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(6);

    // Thread-safe collection to track export results
    ConcurrentHashMap<String, Boolean> exportResults = new ConcurrentHashMap<>();

    // Parallel query + export for drawing data
    parallelExecutor.execute(() -> {
      try {
        android.util.Log.d("ExportPerformance", "Pipeline 1: Drawing data query+export started");
        long pipelineStart = System.currentTimeMillis();

        List<DrawingExportData> drawingData = db
            .drawingDataDao()
            .getAllDrawingDataWithUsersAndImagesByUserID(userId);
        processDrawingModes(drawingData);
        Map<String, List<DrawingExportData>> drawingDataByMode
            = groupDataByDrawingMode(drawingData);

        boolean exported = exportDataByModeAndImage(drawingDataByMode, userDir, contentResolver);

        exportResults.put("drawing", exported);
        long pipelineEnd = System.currentTimeMillis();
        android.util.Log.d(
            "ExportPerformance",
            "Pipeline 1: Drawing data completed in "
            + (pipelineEnd - pipelineStart)
            + "ms - "
            + drawingData.size()
            + " records"
        );
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "Pipeline 1: Drawing data failed", e);
        exportResults.put("drawing", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for gravity data
    parallelExecutor.execute(() -> {
      try {
        android.util.Log.d("ExportPerformance", "Pipeline 2: Gravity data query+export started");
        long pipelineStart = System.currentTimeMillis();

        List<GravityExportData> gravityData = db
            .gravityDataDao()
            .getGravityDataWithUsersAndImagesByUserId(userId);
        processGravityDrawingModes(gravityData);
        Map<String, List<GravityExportData>> gravityDataByMode = groupGravityDataByDrawingMode(
            gravityData);

        boolean exported = exportSensorDataByModeAndImage(
            gravityDataByMode,
            userDir,
            contentResolver,
            Constants.GRAVITY_DATA_PREFIX,
            this::writeGravityDataToStream
        );

        exportResults.put("gravity", exported);
        long pipelineEnd = System.currentTimeMillis();
        android.util.Log.d(
            "ExportPerformance",
            "Pipeline 2: Gravity data completed in "
            + (pipelineEnd - pipelineStart)
            + "ms - "
            + gravityData.size()
            + " records"
        );
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "Pipeline 2: Gravity data failed", e);
        exportResults.put("gravity", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for gyroscope data
    parallelExecutor.execute(() -> {
      try {
        android.util.Log.d("ExportPerformance", "Pipeline 3: Gyroscope data query+export started");
        long pipelineStart = System.currentTimeMillis();

        List<GyroscopeExportData> gyroscopeData = db
            .gyroscopeDataDao()
            .getGyroscopeDataWithUsersAndImagesByUserId(userId);
        processGyroscopeDrawingModes(gyroscopeData);
        Map<String, List<GyroscopeExportData>> gyroscopeDataByMode
            = groupGyroscopeDataByDrawingMode(gyroscopeData);

        boolean exported = exportSensorDataByModeAndImage(
            gyroscopeDataByMode,
            userDir,
            contentResolver,
            Constants.GYROSCOPE_DATA_PREFIX,
            this::writeGyroscopeDataToStream
        );

        exportResults.put("gyroscope", exported);
        long pipelineEnd = System.currentTimeMillis();
        android.util.Log.d(
            "ExportPerformance", "Pipeline 3: Gyroscope data completed in " + (
                pipelineEnd - pipelineStart
            ) + "ms - " + gyroscopeData.size() + " records"
        );
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "Pipeline 3: Gyroscope data failed", e);
        exportResults.put("gyroscope", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for magnetic field data
    parallelExecutor.execute(() -> {
      try {
        android.util.Log.d(
            "ExportPerformance",
            "Pipeline 4: Magnetic field data query+export started"
        );
        long pipelineStart = System.currentTimeMillis();

        List<MagneticFieldExportData> magneticFieldData = db
            .magneticFieldDataDao()
            .getMagneticFieldDataWithUsersAndImagesByUserId(userId);
        processMagneticFieldDrawingModes(magneticFieldData);
        Map<String, List<MagneticFieldExportData>> magneticFieldDataByMode
            = groupMagneticFieldDataByDrawingMode(magneticFieldData);

        boolean exported = exportSensorDataByModeAndImage(
            magneticFieldDataByMode,
            userDir,
            contentResolver,
            Constants.MAGNETIC_FIELD_DATA_PREFIX,
            this::writeMagneticFieldDataToStream
        );

        exportResults.put("magneticField", exported);
        long pipelineEnd = System.currentTimeMillis();
        android.util.Log.d(
            "ExportPerformance", "Pipeline 4: Magnetic field data completed in " + (
                pipelineEnd - pipelineStart
            ) + "ms - " + magneticFieldData.size() + " records"
        );
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "Pipeline 4: Magnetic field data failed", e);
        exportResults.put("magneticField", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for magnetic field baseline data
    parallelExecutor.execute(() -> {
      try {
        android.util.Log.d(
            "ExportPerformance",
            "Pipeline 5: Magnetic field baseline data query+export started"
        );
        long pipelineStart = System.currentTimeMillis();

        List<MagneticFieldBaselineExportData> magneticFieldBaselineData = db
            .magneticFieldBaselineDataDao()
            .getBaselineDataWithUsersAndImagesByUserId(userId);
        processMagneticFieldBaselineDrawingModes(magneticFieldBaselineData);
        Map<String, List<MagneticFieldBaselineExportData>> magneticFieldBaselineDataByMode
            = groupMagneticFieldBaselineDataByDrawingMode(magneticFieldBaselineData);

        boolean exported = exportSensorDataByModeAndImage(
            magneticFieldBaselineDataByMode,
            userDir,
            contentResolver,
            Constants.MAGNETIC_FIELD_BASELINE_DATA_PREFIX,
            this::writeMagneticFieldBaselineDataToStream
        );

        exportResults.put("magneticFieldBaseline", exported);
        long pipelineEnd = System.currentTimeMillis();
        android.util.Log.d(
            "ExportPerformance", "Pipeline 5: Magnetic field baseline data completed in " + (
                pipelineEnd - pipelineStart
            ) + "ms - " + magneticFieldBaselineData.size() + " records"
        );
      } catch (Exception e) {
        android.util.Log.e(
            "ExportPerformance",
            "Pipeline 5: Magnetic field baseline data failed",
            e
        );
        exportResults.put("magneticFieldBaseline", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for accelerometer data
    parallelExecutor.execute(() -> {
      try {
        android.util.Log.d(
            "ExportPerformance",
            "Pipeline 6: Accelerometer data query+export started"
        );
        long pipelineStart = System.currentTimeMillis();

        List<AccelerometerExportData> accelerometerData = db
            .accelerometerDataDao()
            .getAccelerometerDataWithUsersAndImagesByUserId(userId);
        processAccelerometerDrawingModes(accelerometerData);
        Map<String, List<AccelerometerExportData>> accelerometerDataByMode
            = groupAccelerometerDataByDrawingMode(accelerometerData);

        boolean exported = exportSensorDataByModeAndImage(
            accelerometerDataByMode,
            userDir,
            contentResolver,
            Constants.ACCELEROMETER_DATA_PREFIX,
            this::writeAccelerometerDataToStream
        );

        exportResults.put("accelerometer", exported);
        long pipelineEnd = System.currentTimeMillis();
        android.util.Log.d(
            "ExportPerformance", "Pipeline 6: Accelerometer data completed in " + (
                pipelineEnd - pipelineStart
            ) + "ms - " + accelerometerData.size() + " records"
        );
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "Pipeline 6: Accelerometer data failed", e);
        exportResults.put("accelerometer", false);
      } finally {
        latch.countDown();
      }
    });

    // Wait for all pipelines to complete
    try {
      android.util.Log.d("ExportPerformance", "Waiting for all parallel pipelines to complete...");
      boolean completed = latch.await(
          180,
          java.util.concurrent.TimeUnit.SECONDS
      ); // 3 minutes timeout
      if (!completed) {
        android.util.Log.w("ExportPerformance", "Some pipelines are still running after 3 minutes");
        return false;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      android.util.Log.w("ExportPerformance", "Parallel pipeline execution interrupted");
      return false;
    }

    long endTime = System.currentTimeMillis();
    android.util.Log.d(
        "ExportPerformance",
        "All parallel pipelines completed in: " + (endTime - startTime) + "ms"
    );

    // Check if all exports were successful
    boolean allExported = exportResults.values().stream().allMatch(Boolean::booleanValue);

    android.util.Log.d("ExportPerformance", "Export results: " + exportResults);

    if (allExported && !silent) {
      showExportMessage(getString(R.string.export_success_location));
    } else if (!allExported) {
      showExportMessage("Some data types failed to export. Check logs for details.");
    }

    return allExported;
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
  private void processDrawingModes(List<DrawingExportData> drawingDataList) {
    processDataModes(
        drawingDataList,
        DrawingExportData::getSessionID,
        DrawingExportData::getUserID,
        DrawingExportData::setDrawingMode
    );
  }

  /**
   * Generic method to process any data type for drawing modes
   */
  private <T> void processDataModes(
      List<T> dataList,
      Function<T, String> sessionIdGetter,
      ToLongFunction<T> userIdGetter,
      BiConsumer<T, String> drawingModeSetter
  ) {

    for (T data : dataList) {
      String sessionId = sessionIdGetter.apply(data);

      if (sessionId != null) {
        long userId = userIdGetter.applyAsLong(data);
        UserProgressManager.Session session = UserProgressManager.getSession(
            requireContext(),
            userId,
            sessionId
        );

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

  private void setupUploadAllDataPreference() {
    if (uploadAllDataPreference != null) {
      uploadAllDataPreference.setOnPreferenceClickListener(preference -> {
        SettingsActivity activity = (SettingsActivity) getActivity();
        if (activity != null) {
          activity.showUploadOverlay(true);
        }

        executor.execute(() -> uploadAllDataToFirebase(activity));
        return true;
      });
    }
  }

  private void uploadAllDataToFirebase(SettingsActivity activity) {

    List<User> users = db.userDao().getAllUsers();
    if (users.isEmpty()) {
      requireActivity().runOnUiThread(() -> {
        if (activity != null) {
          activity.hideUploadOverlay();
        }
        showToast("No users found to upload data");
      });
      return;
    }

    File exportDir = createExportDirectory();
    if (exportDir == null) {
      requireActivity().runOnUiThread(() -> {
        if (activity != null) {
          activity.hideUploadOverlay();
        }
      });
      return;
    }

    ContentResolver contentResolver = requireActivity().getContentResolver();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    AtomicInteger uploadedCount = new AtomicInteger();
    int totalUsers = users.size();
    String timestamp = new SimpleDateFormat(
        Constants.EXPORT_DATE_FORMAT,
                                            Locale.getDefault()
    ).format(new Date());

    UploadContext context = new UploadContext(
        contentResolver,
                                              storageRef,
                                              uploadedCount,
                                              totalUsers,
                                              timestamp,
                                              exportDir
    );

    for (User user : users) {
      List<DrawingExportData> drawingDataList = db
          .drawingDataDao()
          .getAllDrawingDataWithUsersAndImagesByUserID(user.id);

      if (drawingDataList.isEmpty()) {
        continue;
      }

      processUserData(user, drawingDataList, context, activity);
    }
  }

  private boolean exportDataByModeAndImage(
      Map<String, List<DrawingExportData>> dataByMode,
      File userDir,
      ContentResolver contentResolver
  ) {
    boolean allExported = true;

    for (Map.Entry<String, List<DrawingExportData>> modeEntry : dataByMode.entrySet()) {
      String mode = modeEntry.getKey();
      List<DrawingExportData> modeData = modeEntry.getValue();

      if (!modeData.isEmpty()) {
        File modeDir = createModeDirectory(userDir, mode);
        if (modeDir != null) {
          boolean modeExported = exportDataForMode(modeData, modeDir, contentResolver, mode);
          allExported &= modeExported;
        } else {
          allExported = false;
        }
      }
    }

    return allExported;
  }

  private void processUploadDataForMode(
      User user,
      List<DrawingExportData> modeData,
      File modeDir,
      String mode,
      String userDirName,
      UploadContext context,
      SettingsActivity activity
  ) {
    // Group data by image
    Map<String, List<DrawingExportData>> dataByImage = groupDrawingDataByImage(modeData);

    UploadImageParams params = new UploadImageParams(modeDir, mode, userDirName, context, activity);

    for (Map.Entry<String, List<DrawingExportData>> imageEntry : dataByImage.entrySet()) {
      String imageName = imageEntry.getKey();
      List<DrawingExportData> imageData = imageEntry.getValue();

      processUploadDataForImage(user, imageName, imageData, params);
    }
  }

  private void processUploadDataForImage(
      User user,
      String imageName,
      List<DrawingExportData> imageData,
      UploadImageParams params
  ) {
    // Convert to lowercase and add underscores
    String formattedImageName = imageName.toLowerCase();
    String fileName = formattedImageName + "_drawing_data.csv";

    File file = new File(params.modeDir, fileName);
    Uri uploadFileUri = Uri.fromFile(file);

    try (
        OutputStream outputStream = params.context.contentResolver.openOutputStream(uploadFileUri)
    ) {
      if (outputStream != null) {
        writeDrawingDataToStream(outputStream, imageData);

        if (params.mode.equals(Constants.DRAWING_MODE_NORMAL)) {
          // Upload with new path structure: SketchIDData/username_timestamp/mode/filename
          String firebasePath = SKETCHID_DATA_PATH
                                + params.userDirName
                                + FIREBASE_PATH_SEPARATOR
                                + params.mode
                                + FIREBASE_PATH_SEPARATOR
                                + fileName;
          uploadFileToFirebase(
              user,
              firebasePath,
              uploadFileUri,
              params.context.storageRef,
              params.context.uploadedCount,
              params.context.totalUsers,
              params.activity
          );
        }
      }
    } catch (IOException e) {
      showToast("Failed to export data for user: " + user.name + ", image: " + imageName);
    }
  }

  private void uploadFileToFirebase(
      User user,
      String firebasePath,
      Uri uploadFileUri,
      StorageReference storageRef,
      AtomicInteger uploadedCount,
      int totalUsers,
      SettingsActivity activity
  ) {
    StorageReference fileRef = storageRef.child(firebasePath);
    UploadTask uploadTask = fileRef.putFile(uploadFileUri);

    uploadTask.addOnSuccessListener(taskSnapshot -> {
      uploadedCount.getAndIncrement();
      if (uploadedCount.get() == totalUsers) {
        requireActivity().runOnUiThread(() -> {
          if (activity != null) {
            activity.hideUploadOverlay();
          }
          showExportMessage(getString(R.string.upload_complete));
        });
      }
    }).addOnFailureListener(e -> {
      uploadedCount.getAndIncrement();
      if (uploadedCount.get() == totalUsers) {
        requireActivity().runOnUiThread(() -> {
          if (activity != null) {
            activity.hideUploadOverlay();
          }
          showExportMessage(getString(R.string.upload_failed));
        });
      } else {
        showToast("Failed to upload data for user: " + user.name);
      }
    });
  }

  /**
   * Process gravity data to add drawing mode information from session settings
   */
  private void processGravityDrawingModes(List<GravityExportData> gravityDataList) {
    processDataModes(
        gravityDataList,
        GravityExportData::getSessionId,
        GravityExportData::getUserId,
        GravityExportData::setDrawingMode
    );
  }

  /**
   * Process gyroscope data to add drawing mode information from session settings
   */
  private void processGyroscopeDrawingModes(List<GyroscopeExportData> gyroscopeDataList) {
    processDataModes(
        gyroscopeDataList,
        GyroscopeExportData::getSessionId,
        GyroscopeExportData::getUserId,
        GyroscopeExportData::setDrawingMode
    );
  }

  /**
   * Process magnetic field data to add drawing mode information from session settings
   */
  private void processMagneticFieldDrawingModes(List<MagneticFieldExportData> magneticFieldDataList) {
    processDataModes(
        magneticFieldDataList,
        MagneticFieldExportData::getSessionId,
        MagneticFieldExportData::getUserId,
        MagneticFieldExportData::setDrawingMode
    );
  }

  /**
   * Process magnetic field baseline data to add drawing mode information from session settings
   */
  private void processMagneticFieldBaselineDrawingModes(List<MagneticFieldBaselineExportData> magneticFieldBaselineDataList) {
    processDataModes(
        magneticFieldBaselineDataList,
        MagneticFieldBaselineExportData::getSessionId,
        MagneticFieldBaselineExportData::getUserId,
        MagneticFieldBaselineExportData::setDrawingMode
    );
  }

  /**
   * Process accelerometer data to add drawing mode information from session settings
   */
  private void processAccelerometerDrawingModes(List<AccelerometerExportData> accelerometerDataList) {
    processDataModes(
        accelerometerDataList,
        AccelerometerExportData::getSessionId,
        AccelerometerExportData::getUserId,
        AccelerometerExportData::setDrawingMode
    );
  }

  /**
   * Group gravity data by drawing mode
   */
  private Map<String, List<GravityExportData>> groupGravityDataByDrawingMode(List<GravityExportData> gravityDataList) {
    return groupDataByMode(gravityDataList, GravityExportData::getDrawingMode);
  }

  /**
   * Group gyroscope data by drawing mode
   */
  private Map<String, List<GyroscopeExportData>> groupGyroscopeDataByDrawingMode(List<GyroscopeExportData> gyroscopeDataList) {
    return groupDataByMode(gyroscopeDataList, GyroscopeExportData::getDrawingMode);
  }

  /**
   * Group magnetic field data by drawing mode
   */
  private Map<String, List<MagneticFieldExportData>> groupMagneticFieldDataByDrawingMode(List<MagneticFieldExportData> magneticFieldDataList) {
    return groupDataByMode(magneticFieldDataList, MagneticFieldExportData::getDrawingMode);
  }

  /**
   * Group magnetic field baseline data by drawing mode
   */
  private Map<String, List<MagneticFieldBaselineExportData>> groupMagneticFieldBaselineDataByDrawingMode(
      List<MagneticFieldBaselineExportData> magneticFieldBaselineDataList
  ) {
    return groupDataByMode(
        magneticFieldBaselineDataList,
        MagneticFieldBaselineExportData::getDrawingMode
    );
  }

  /**
   * Group accelerometer data by drawing mode
   */
  private Map<String, List<AccelerometerExportData>> groupAccelerometerDataByDrawingMode(List<AccelerometerExportData> accelerometerDataList) {
    return groupDataByMode(accelerometerDataList, AccelerometerExportData::getDrawingMode);
  }

  /**
   * Group drawing data by drawing mode
   */
  private Map<String, List<DrawingExportData>> groupDataByDrawingMode(List<DrawingExportData> drawingDataList) {
    return groupDataByMode(drawingDataList, DrawingExportData::getDrawingMode);
  }

  /**
   * Generic method to group any data type by drawing mode
   */
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

  private void processUserData(
      User user,
      List<DrawingExportData> drawingDataList,
      UploadContext context,
      SettingsActivity activity
  ) {

    processDrawingModes(drawingDataList);

    Map<String, List<DrawingExportData>> dataByMode = groupDataByDrawingMode(drawingDataList);

    // Create user directory with timestamp
    String userDirName = user.name + "_" + context.timestamp;
    File userDir = new File(context.exportDir, userDirName);
    if (!userDir.exists() && !userDir.mkdirs()) {
      showToast("Failed to create user directory for upload: " + user.name);
      return;
    }

    for (Map.Entry<String, List<DrawingExportData>> entry : dataByMode.entrySet()) {
      String mode = entry.getKey();
      List<DrawingExportData> modeData = entry.getValue();

      if (!modeData.isEmpty()) {
        File modeDir = createModeDirectory(userDir, mode);
        if (modeDir != null) {
          processUploadDataForMode(user, modeData, modeDir, mode, userDirName, context, activity);
        } else {
          showToast("Failed to create mode directory for upload: " + mode);
        }
      }
    }
  }

  private File createModeDirectory(File userDir, String mode) {
    File modeDir = new File(userDir, mode);
    if (!modeDir.exists() && !modeDir.mkdirs()) {
      showExportMessage("Failed to create mode directory: " + mode);
      return null;
    }
    return modeDir;
  }

  private boolean exportDataForMode(
      List<DrawingExportData> modeData,
      File modeDir,
      ContentResolver contentResolver,
      String mode
  ) {
    boolean allExported = true;
    Map<String, List<DrawingExportData>> dataByImage = groupDrawingDataByImage(modeData);

    for (Map.Entry<String, List<DrawingExportData>> imageEntry : dataByImage.entrySet()) {
      String imageName = imageEntry.getKey();
      List<DrawingExportData> imageData = imageEntry.getValue();

      boolean imageExported = exportDataForImage(
          imageName,
          imageData,
          modeDir,
          contentResolver,
          mode
      );
      allExported &= imageExported;
    }

    return allExported;
  }

  private boolean exportDataForImage(
      String imageName,
      List<DrawingExportData> imageData,
      File modeDir,
      ContentResolver contentResolver,
      String mode
  ) {
    // Convert to lowercase and add underscores
    String formattedImageName = imageName.toLowerCase();
    String formattedDataType = formatDataTypeForFilename(Constants.DRAWING_DATA_PREFIX);
    String fileName = formattedImageName + "_" + formattedDataType + ".csv";

    File file = new File(modeDir, fileName);
    Uri exportFileUri = Uri.fromFile(file);

    try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
      if (outputStream != null) {
        writeDrawingDataToStream(outputStream, imageData);

        if (mode.equals(Constants.DRAWING_MODE_NORMAL)) {
          fileUri = exportFileUri;
        }
      }
      return true;
    } catch (IOException e) {
      showExportMessage(String.format(
          Locale.getDefault(),
          "Failed to export %s data for image: %s",
          Constants.DRAWING_DATA_PREFIX,
          imageName
      ));
      return false;
    }
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

  private void writeDrawingDataToStream(OutputStream outputStream, List<DrawingExportData> modeData)
  throws IOException {
    outputStream.write(Constants.COLUMN_HEADERS.getBytes());

    for (DrawingExportData data : modeData) {
      String row = String.format(
          Locale.getDefault(),
          Constants.FILE_EXPORT_PATTERN,
          data.getId(),
          data.getUserID(),
          data.getUserName(),
          data.getAttempt(),
          data.getTime(),
          data.getX(),
          data.getY(),
          data.getAction(),
          data.getItemType(),
          data.getImageID(),
          data.getImageName(),
          data.getDrawingMode(),
          data.getSize(),
          data.getPressure(),
          data.getOrientation()
      );
      outputStream.write(row.getBytes());
    }
  }

  private <T> boolean exportSensorDataByModeAndImage(
      Map<String, List<T>> dataByMode,
      File userDir,
      ContentResolver contentResolver,
      String dataPrefix,
      SensorDataWriter<T> writer
  ) {
    boolean allExported = true;

    for (Map.Entry<String, List<T>> modeEntry : dataByMode.entrySet()) {
      String mode = modeEntry.getKey();
      List<T> modeData = modeEntry.getValue();

      if (!shouldSkipSensorModeData(modeData)) {
        File modeDir = createModeDirectory(userDir, mode);
        if (modeDir != null) {
          boolean modeExported = exportSensorDataForMode(
              modeData,
              modeDir,
              contentResolver,
              dataPrefix,
              writer
          );
          allExported &= modeExported;
        } else {
          allExported = false;
        }
      }
    }

    return allExported;
  }

  private <T> boolean shouldSkipSensorModeData(List<T> modeData) {
    return modeData.isEmpty();
  }

  private <T> boolean exportSensorDataForMode(
      List<T> modeData,
      File modeDir,
      ContentResolver contentResolver,
      String dataPrefix,
      SensorDataWriter<T> writer
  ) {
    boolean allExported = true;
    Map<String, List<T>> dataByImage = groupSensorDataByImage(modeData);

    for (Map.Entry<String, List<T>> imageEntry : dataByImage.entrySet()) {
      String imageName = imageEntry.getKey();
      List<T> imageData = imageEntry.getValue();

      boolean imageExported = exportSensorDataForImage(
          imageName,
          imageData,
          modeDir,
          contentResolver,
          dataPrefix,
          writer
      );
      allExported &= imageExported;
    }

    return allExported;
  }

  private <T> boolean exportSensorDataForImage(
      String imageName,
      List<T> imageData,
      File modeDir,
      ContentResolver contentResolver,
      String dataPrefix,
      SensorDataWriter<T> writer
  ) {
    // Convert to lowercase and add underscores
    String formattedImageName = imageName.toLowerCase();
    String formattedDataType = formatDataTypeForFilename(dataPrefix);
    String fileName = formattedImageName + "_" + formattedDataType + ".csv";

    File file = new File(modeDir, fileName);
    Uri exportFileUri = Uri.fromFile(file);

    try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
      if (outputStream != null) {
        writer.writeData(outputStream, imageData);
      }
      return true;
    } catch (IOException e) {
      showExportMessage("Failed to export " + dataPrefix + " data for image: " + imageName);
      return false;
    }
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
    outputStream.write(Constants.GRAVITY_COLUMN_HEADERS.getBytes());
    for (GravityExportData item : data) {
      String row = String.format(
          Locale.getDefault(),
          Constants.GRAVITY_EXPORT_PATTERN,
          item.getId(),
          item.getUserId(),
          item.getUserName(),
          item.getImageId(),
          item.getImageName(),
          item.getAttempt(),
          item.getTimestamp(),
          item.getX(),
          item.getY(),
          item.getZ(),
          item.getDrawingMode()
      );
      outputStream.write(row.getBytes());
    }
  }

  private void writeGyroscopeDataToStream(OutputStream outputStream, List<GyroscopeExportData> data)
  throws IOException {
    outputStream.write(Constants.GYROSCOPE_COLUMN_HEADERS.getBytes());
    for (GyroscopeExportData item : data) {
      String row = String.format(
          Locale.getDefault(),
          Constants.GYROSCOPE_EXPORT_PATTERN,
          item.getId(),
          item.getUserId(),
          item.getUserName(),
          item.getImageId(),
          item.getImageName(),
          item.getAttempt(),
          item.getTimestamp(),
          item.getX(),
          item.getY(),
          item.getZ(),
          item.getDrawingMode()
      );
      outputStream.write(row.getBytes());
    }
  }

  private void writeMagneticFieldDataToStream(
      OutputStream outputStream,
      List<MagneticFieldExportData> data
  )
  throws IOException {
    outputStream.write(Constants.MAGNETIC_FIELD_COLUMN_HEADERS.getBytes());
    for (MagneticFieldExportData item : data) {
      String row = String.format(
          Locale.getDefault(),
          Constants.MAGNETIC_FIELD_EXPORT_PATTERN,
          item.getId(),
          item.getUserId(),
          item.getUserName(),
          item.getImageId(),
          item.getImageName(),
          item.getAttempt(),
          item.getTimestamp(),
          item.getX(),
          item.getY(),
          item.getZ(),
          item.getDrawingMode()
      );
      outputStream.write(row.getBytes());
    }
  }

  private void writeMagneticFieldBaselineDataToStream(
      OutputStream outputStream,
      List<MagneticFieldBaselineExportData> data
  )
  throws IOException {
    outputStream.write(Constants.MAGNETIC_FIELD_BASELINE_COLUMN_HEADERS.getBytes());
    for (MagneticFieldBaselineExportData item : data) {
      String row = String.format(
          Locale.getDefault(),
          Constants.MAGNETIC_FIELD_BASELINE_EXPORT_PATTERN,
          item.getId(),
          item.getUserId(),
          item.getUserName(),
          item.getImageId(),
          item.getImageName(),
          item.getAttempt(),
          item.getTimestamp(),
          item.getAvgX(),
          item.getAvgY(),
          item.getAvgZ(),
          item.getDurationMs(),
          item.getSampleCount(),
          item.getStdDevX(),
          item.getStdDevY(),
          item.getStdDevZ(),
          item.getDrawingMode()
      );
      outputStream.write(row.getBytes());
    }
  }

  private void writeAccelerometerDataToStream(
      OutputStream outputStream,
      List<AccelerometerExportData> data
  )
  throws IOException {
    outputStream.write(Constants.ACCELEROMETER_COLUMN_HEADERS.getBytes());
    for (AccelerometerExportData item : data) {
      String row = String.format(
          Locale.getDefault(),
          Constants.ACCELEROMETER_EXPORT_PATTERN,
          item.getId(),
          item.getUserId(),
          item.getUserName(),
          item.getImageId(),
          item.getImageName(),
          item.getAttempt(),
          item.getTimestamp(),
          item.getX(),
          item.getY(),
          item.getZ(),
          item.getDrawingMode()
      );
      outputStream.write(row.getBytes());
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
        + "drawing history but keep the user profile.",
        userListPreference.getEntry()
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
        String dialogMessage = "Are you sure you want to delete ALL drawing data for ALL users?%n%n"
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
                DELETE_BUTTON, (dialog, which) ->

                    executor.execute(() -> {
                      db.drawingDataDao().deleteAllDrawingData();
                      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                          requireContext());
                      prefs
                          .edit()
                          .remove(KEY_CURRENT_ITEM_ATTEMPT)
                          .remove(KEY_CURRENT_ITEM_INDEX)
                          .apply();

                      requireActivity().runOnUiThread(() -> Toast
                          .makeText(
                              requireContext(),
                              "All drawing data cleared!",
                              Toast.LENGTH_LONG
                          )
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

  private void setupUploadDataPreference() {
    if (uploadDataPreference != null) {
      uploadDataPreference.setOnPreferenceClickListener(preference -> {
        SettingsActivity activity = (SettingsActivity) getActivity();
        if (activity != null) {
          activity.showUploadOverlay(false);
        }

        executor.execute(() -> uploadDataToFirebase(activity));
        return true;
      });
    }
  }

  private void setupExportAllDataPreference() {
    if (exportAllDataPreference != null) {
      exportAllDataPreference.setOnPreferenceClickListener(preference -> {
        SettingsActivity activity = (SettingsActivity) getActivity();
        if (activity != null) {
          activity.showExportOverlay(true);
        }

        executor.execute(() -> {
          long startTime = System.currentTimeMillis();
          boolean success = exportAllDataToCSV();
          long endTime = System.currentTimeMillis();

          // Log the export time for performance monitoring
          android.util.Log.d(
              "ExportPerformance",
              "Parallel export completed in: " + (endTime - startTime) + "ms (" + String.format(
                  "%.1f",
                  (endTime - startTime) / 1000.0
              ) + " seconds)"
          );

          requireActivity().runOnUiThread(() -> {
            if (activity != null) {
              activity.hideExportOverlay();
            }

            if (!success) {
              showExportMessage(getString(R.string.export_failed));
            }
          });
        });
        return true;
      });
    }
  }

  private boolean exportAllDataToCSV() {
    List<User> users = db.userDao().getAllUsers();
    if (users.isEmpty()) {
      showExportMessage("No users found to export data");
      return false;
    }

    File exportDir = createExportDirectory();
    if (exportDir == null) {
      return false;
    }

    android.util.Log.d(
        "ExportPerformance",
        "Starting parallel export for " + users.size() + " users..."
    );
    long startTime = System.currentTimeMillis();

    // Use thread pool for parallel user processing
    Executor parallelExecutor = Executors.newFixedThreadPool(Math.min(
        users.size(),
        4
    )); // Limit to 4 concurrent users

    // Use CountDownLatch for synchronization
    java.util.concurrent.CountDownLatch latch
        = new java.util.concurrent.CountDownLatch(users.size());

    // Thread-safe collection to track export results
    ConcurrentHashMap<String, Boolean> userExportResults = new ConcurrentHashMap<>();
    ContentResolver contentResolver = requireActivity().getContentResolver();
    String timestamp = createTimestamp();

    // Process each user in parallel
    for (User user : users) {
      parallelExecutor.execute(() -> {
        try {
          android.util.Log.d("ExportPerformance", "User pipeline: " + user.name + " started");
          long userStartTime = System.currentTimeMillis();

          boolean userExported = exportDataForSingleUser(
              user,
              exportDir,
              contentResolver,
              timestamp
          );

          userExportResults.put(user.name, userExported);
          long userEndTime = System.currentTimeMillis();
          android.util.Log.d(
              "ExportPerformance", "User pipeline: " + user.name + " completed in " + (
                  userEndTime - userStartTime
              ) + "ms"
          );
        } catch (Exception e) {
          android.util.Log.e("ExportPerformance", "User pipeline: " + user.name + " failed", e);
          userExportResults.put(user.name, false);
        } finally {
          latch.countDown();
        }
      });
    }

    // Wait for all user exports to complete
    try {
      android.util.Log.d("ExportPerformance", "Waiting for all user pipelines to complete...");
      boolean completed = latch.await(
          300,
          java.util.concurrent.TimeUnit.SECONDS
      ); // 5 minutes timeout
      if (!completed) {
        android.util.Log.w(
            "ExportPerformance",
            "Some user pipelines are still running after 5 minutes"
        );
        return false;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      android.util.Log.w("ExportPerformance", "Parallel user export execution interrupted");
      return false;
    }

    long endTime = System.currentTimeMillis();
    android.util.Log.d(
        "ExportPerformance",
        "All user pipelines completed in: " + (endTime - startTime) + "ms"
    );

    // Check if all exports were successful
    boolean allExported = userExportResults.values().stream().allMatch(Boolean::booleanValue);

    android.util.Log.d("ExportPerformance", "User export results: " + userExportResults);

    if (allExported) {
      showExportMessage("All drawing data exported to Downloads/SketchIDData");
    } else {
      showExportMessage("Some users failed to export. Check logs for details.");
    }

    return allExported;
  }

  /**
   * Parallel export for a single user: Query and export each data type in parallel
   */
  private boolean exportDataForSingleUser(
      User user,
      File exportDir,
      ContentResolver contentResolver,
      String timestamp
  ) {
    // Create user directory with timestamp
    String userDirName = user.name + "_" + timestamp;
    File userDir = new File(exportDir, userDirName);
    if (!userDir.exists() && !userDir.mkdirs()) {
      android.util.Log.e("ExportPerformance", "Failed to create user directory for: " + user.name);
      return false;
    }

    // Use thread pool for parallel data type processing
    Executor parallelExecutor = Executors.newFixedThreadPool(6);

    // Use CountDownLatch for synchronization
    java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(6);

    // Thread-safe collection to track export results
    ConcurrentHashMap<String, Boolean> exportResults = new ConcurrentHashMap<>();

    // Parallel query + export for drawing data
    parallelExecutor.execute(() -> {
      try {
        List<DrawingExportData> drawingData = db
            .drawingDataDao()
            .getAllDrawingDataWithUsersAndImagesByUserID(user.id);
        processDrawingModes(drawingData);
        Map<String, List<DrawingExportData>> drawingDataByMode
            = groupDataByDrawingMode(drawingData);

        boolean exported = exportDataByModeAndImage(drawingDataByMode, userDir, contentResolver);

        exportResults.put("drawing", exported);
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "User " + user.name + " drawing data failed", e);
        exportResults.put("drawing", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for gravity data
    parallelExecutor.execute(() -> {
      try {
        List<GravityExportData> gravityData = db
            .gravityDataDao()
            .getGravityDataWithUsersAndImagesByUserId(user.id);
        processGravityDrawingModes(gravityData);
        Map<String, List<GravityExportData>> gravityDataByMode = groupGravityDataByDrawingMode(
            gravityData);

        boolean exported = exportSensorDataByModeAndImage(
            gravityDataByMode,
            userDir,
            contentResolver,
            Constants.GRAVITY_DATA_PREFIX,
            this::writeGravityDataToStream
        );

        exportResults.put("gravity", exported);
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "User " + user.name + " gravity data failed", e);
        exportResults.put("gravity", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for gyroscope data
    parallelExecutor.execute(() -> {
      try {
        List<GyroscopeExportData> gyroscopeData = db
            .gyroscopeDataDao()
            .getGyroscopeDataWithUsersAndImagesByUserId(user.id);
        processGyroscopeDrawingModes(gyroscopeData);
        Map<String, List<GyroscopeExportData>> gyroscopeDataByMode
            = groupGyroscopeDataByDrawingMode(gyroscopeData);

        boolean exported = exportSensorDataByModeAndImage(
            gyroscopeDataByMode,
            userDir,
            contentResolver,
            Constants.GYROSCOPE_DATA_PREFIX,
            this::writeGyroscopeDataToStream
        );

        exportResults.put("gyroscope", exported);
      } catch (Exception e) {
        android.util.Log.e("ExportPerformance", "User " + user.name + " gyroscope data failed", e);
        exportResults.put("gyroscope", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for magnetic field data
    parallelExecutor.execute(() -> {
      try {
        List<MagneticFieldExportData> magneticFieldData = db
            .magneticFieldDataDao()
            .getMagneticFieldDataWithUsersAndImagesByUserId(user.id);
        processMagneticFieldDrawingModes(magneticFieldData);
        Map<String, List<MagneticFieldExportData>> magneticFieldDataByMode
            = groupMagneticFieldDataByDrawingMode(magneticFieldData);

        boolean exported = exportSensorDataByModeAndImage(
            magneticFieldDataByMode,
            userDir,
            contentResolver,
            Constants.MAGNETIC_FIELD_DATA_PREFIX,
            this::writeMagneticFieldDataToStream
        );

        exportResults.put("magneticField", exported);
      } catch (Exception e) {
        android.util.Log.e(
            "ExportPerformance",
            "User " + user.name + " magnetic field data failed",
            e
        );
        exportResults.put("magneticField", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for magnetic field baseline data
    parallelExecutor.execute(() -> {
      try {
        List<MagneticFieldBaselineExportData> magneticFieldBaselineData = db
            .magneticFieldBaselineDataDao()
            .getBaselineDataWithUsersAndImagesByUserId(user.id);
        processMagneticFieldBaselineDrawingModes(magneticFieldBaselineData);
        Map<String, List<MagneticFieldBaselineExportData>> magneticFieldBaselineDataByMode
            = groupMagneticFieldBaselineDataByDrawingMode(magneticFieldBaselineData);

        boolean exported = exportSensorDataByModeAndImage(
            magneticFieldBaselineDataByMode,
            userDir,
            contentResolver,
            Constants.MAGNETIC_FIELD_BASELINE_DATA_PREFIX,
            this::writeMagneticFieldBaselineDataToStream
        );

        exportResults.put("magneticFieldBaseline", exported);
      } catch (Exception e) {
        android.util.Log.e(
            "ExportPerformance",
            "User " + user.name + " magnetic field baseline data failed",
            e
        );
        exportResults.put("magneticFieldBaseline", false);
      } finally {
        latch.countDown();
      }
    });

    // Parallel query + export for accelerometer data
    parallelExecutor.execute(() -> {
      try {
        List<AccelerometerExportData> accelerometerData = db
            .accelerometerDataDao()
            .getAccelerometerDataWithUsersAndImagesByUserId(user.id);
        processAccelerometerDrawingModes(accelerometerData);
        Map<String, List<AccelerometerExportData>> accelerometerDataByMode
            = groupAccelerometerDataByDrawingMode(accelerometerData);

        boolean exported = exportSensorDataByModeAndImage(
            accelerometerDataByMode,
            userDir,
            contentResolver,
            Constants.ACCELEROMETER_DATA_PREFIX,
            this::writeAccelerometerDataToStream
        );

        exportResults.put("accelerometer", exported);
      } catch (Exception e) {
        android.util.Log.e(
            "ExportPerformance",
            "User " + user.name + " accelerometer data failed",
            e
        );
        exportResults.put("accelerometer", false);
      } finally {
        latch.countDown();
      }
    });

    // Wait for all data type exports to complete
    try {
      boolean completed = latch.await(
          180,
          java.util.concurrent.TimeUnit.SECONDS
      ); // 3 minutes timeout
      if (!completed) {
        android.util.Log.w(
            "ExportPerformance",
            "Some data type pipelines for user "
            + user.name
            + " are still running after 3 minutes"
        );
        return false;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      android.util.Log.w(
          "ExportPerformance",
          "Parallel data type export execution interrupted for user " + user.name
      );
      return false;
    }

    // Check if all exports were successful
    boolean allExported = exportResults.values().stream().allMatch(Boolean::booleanValue);

    if (!allExported) {
      android.util.Log.w(
          "ExportPerformance",
          "Some data types failed for user " + user.name + ": " + exportResults
      );
    }

    return allExported;
  }

  private String createTimestamp() {
    return new SimpleDateFormat(
        Constants.EXPORT_DATE_FORMAT,
                                Locale.getDefault()
    ).format(new Date());
  }

  @FunctionalInterface
  private interface SensorDataWriter<T> {
    void writeData(OutputStream outputStream, List<T> data)
    throws IOException;
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

  /**
   * Context class to group upload-related parameters
   */
  private static class UploadContext {
    final ContentResolver contentResolver;
    final StorageReference storageRef;
    final AtomicInteger uploadedCount;
    final int totalUsers;
    final String timestamp;
    final File exportDir;

    UploadContext(
        ContentResolver contentResolver,
        StorageReference storageRef,
        AtomicInteger uploadedCount,
        int totalUsers,
        String timestamp,
        File exportDir
    ) {
      this.contentResolver = contentResolver;
      this.storageRef = storageRef;
      this.uploadedCount = uploadedCount;
      this.totalUsers = totalUsers;
      this.timestamp = timestamp;
      this.exportDir = exportDir;
    }
  }

  /**
   * Parameter object for upload image processing
   */
  private static class UploadImageParams {
    final File modeDir;
    final String mode;
    final String userDirName;
    final UploadContext context;
    final SettingsActivity activity;

    UploadImageParams(
        File modeDir,
        String mode,
        String userDirName,
        UploadContext context,
        SettingsActivity activity
    ) {
      this.modeDir = modeDir;
      this.mode = mode;
      this.userDirName = userDirName;
      this.context = context;
      this.activity = activity;
    }
  }

}
