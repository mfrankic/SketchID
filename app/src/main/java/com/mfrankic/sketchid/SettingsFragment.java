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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsFragment extends PreferenceFragmentCompat {

  private static final String DIALOG_PREFERENCE_KEY = "dialog_preference_key";
  private static final String DIALOG_RESULT_KEY = "dialog_result";

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

    // Initialize visibility states
    if (resetUserProgressPreference != null) {
      resetUserProgressPreference.setVisible(false);
    }

    setupUserListPreference();
    setupNewUserPreference();
    setupDeleteUserPreference();
    setupDrawingAttemptsPreference();
    setupExportDataPreference();
    setupUploadDataPreference();
    setupExportAllDataPreference();
    setupUploadAllDataPreference();
    setupDeleteUserDataPreference();
    setupClearDataPreference();
    setupSelectImagePreference();
    setupResetUserProgressPreference();

    loadUsersIntoListPreference();

    // Check for unfinished users in a background thread
    executor.execute(this::checkUnfinishedUsersInitial);

    // Register fragment result listener for dialog preference results
    getParentFragmentManager().setFragmentResultListener(
        DIALOG_RESULT_KEY, this, (requestKey, result) -> {
          // Get preference key from result bundle
          String preferenceKey = result.getString(DIALOG_PREFERENCE_KEY);
          if (preferenceKey != null) {
            // Find the preference that was clicked
            Preference preference = findPreference(preferenceKey);
            if (preference instanceof CustomDialogPreference) {
              // Call the preference's change listener with the result value
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
      // Skip showing dialog for all CustomDialogPreference instances
      // We're handling the dialogs with AlertDialog directly
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
      // Set dialog properties
      newUserPreference.setOnBindEditTextListener(editText -> {
        // Set the hint for the text field
        editText.setHint("Name");
        // Clear any existing text and set cursor at the beginning
        editText.setText("");
      });

      // Handle user submission
      newUserPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        String name = newValue.toString().trim();
        if (name.isEmpty()) {
          Toast.makeText(getContext(), ERROR_NAME_EMPTY, Toast.LENGTH_LONG).show();
          return false;
        }

        // Add the new user to the database
        executor.execute(() -> {
          User user = new User(name);
          String newUserID = Long.toString(db.userDao().insertUser(user));

          // Update the selected user preference
          PreferenceManager
              .getDefaultSharedPreferences(requireContext())
              .edit()
              .putString(KEY_SELECTED_USER, newUserID)
              .apply();

          // Reload the user list preference
          requireActivity().runOnUiThread(this::loadUsersIntoListPreference);
        });

        return true;
      });

      // Don't store text in preferences
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
      // Set up click listener to show a direct AlertDialog
      deleteUserPreference.setOnPreferenceClickListener(preference -> {
        // Only proceed if a user is selected
        if (isNotValidUserSelected()) {
          return true;
        }

        String dialogMessage = createDeleteUserMessage();
        Drawable dialogIcon = createWarningIcon();

        // Create and show the alert dialog
        AlertDialog alertDialog = createDeleteUserDialog(dialogMessage, dialogIcon);
        alertDialog.show();

        // Apply tint to the positive button
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
    // Get and tint the icon
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

        // Delete user's drawing data from the database
        db.drawingDataDao().deleteDrawingDataByUserID(Long.parseLong(selectedUserID));

        // Delete user's session data
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
    } catch (Exception e) {
      // Ignore styling errors
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

  private void setupExportDataPreference() {
    if (exportDataPreference != null) {
      exportDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::exportDataToCSV);
        return true;
      });
    }
  }

  private void exportDataToCSV() {
    exportDataToCSV(false);
  }

  private boolean exportDataToCSV(boolean silent) {
    List<DrawingExportData> drawingDataList = db
        .drawingDataDao()
        .getAllDrawingDataWithUsersAndImagesByUserID(Long.parseLong(selectedUserID));
    String timestamp = new SimpleDateFormat(
        Constants.EXPORT_DATE_FORMAT,
                                            Locale.getDefault()
    ).format(new Date());
    String fileName = userListPreference.getEntry()
                      + Constants.DRAWING_DATA_PREFIX
                      + timestamp
                      + ".csv";
    ContentResolver contentResolver = requireActivity().getContentResolver();

    File exportDir
        = new File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Constants.SKETCHID_DATA_DIR
    );
    if (!exportDir.exists() && !exportDir.mkdirs()) {
      requireActivity().runOnUiThread(() -> Toast
          .makeText(getContext(), Constants.FAILED_EXPORT_MESSAGE, Toast.LENGTH_LONG)
          .show());
      return false;
    }

    File file = new File(exportDir, fileName);
    Uri exportFileUri = Uri.fromFile(file);

    if (exportFileUri != null) {
      try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
        if (outputStream != null) {
          outputStream.write(Constants.COLUMN_HEADERS.getBytes());
          for (DrawingExportData data : drawingDataList) {
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
                data.getImageName()
            );
            outputStream.write(row.getBytes());
          }
          if (!silent) {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(
                    getContext(),
                    "Data exported to Downloads/SketchIDData",
                    Toast.LENGTH_LONG
                )
                .show());
          }
        }
      } catch (IOException e) {
        requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "Failed to export data", Toast.LENGTH_LONG)
            .show());
        return false;
      }
    }

    // For use in uploadDataToFirebase method
    fileUri = exportFileUri;
    return true;
  }

  private void setupUploadDataPreference() {
    if (uploadDataPreference != null) {
      uploadDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::uploadDataToFirebase);
        return true;
      });
    }
  }

  private void uploadDataToFirebase() {
    boolean success = exportDataToCSV(true);
    if (!success) {
      return;
    }
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    String timestamp = new SimpleDateFormat(
        Constants.EXPORT_DATE_FORMAT,
                                            Locale.getDefault()
    ).format(new Date());
    String fileName = userListPreference.getEntry() + "_drawing_data_" + timestamp + ".csv";
    StorageReference fileRef = storageRef.child("SketchIDData/" + fileName);

    UploadTask uploadTask = fileRef.putFile(fileUri);
    uploadTask
        .addOnSuccessListener(taskSnapshot -> requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "Data uploaded to Firebase", Toast.LENGTH_LONG)
            .show()))
        .addOnFailureListener(e -> requireActivity().runOnUiThread(() -> Toast
            .makeText(getContext(), "Failed to upload data to Firebase", Toast.LENGTH_LONG)
            .show()));

    fileUri = null;
  }

  private void setupExportAllDataPreference() {
    if (exportAllDataPreference != null) {
      exportAllDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(() -> {
          boolean success = exportAllDataToCSV();
          if (!success) {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(getContext(), "Failed to export all data", Toast.LENGTH_LONG)
                .show());
          }
        });
        return true;
      });
    }
  }

  private boolean exportAllDataToCSV() {
    // Get all users
    List<User> users = db.userDao().getAllUsers();
    if (users.isEmpty()) {
      requireActivity().runOnUiThread(() -> Toast
          .makeText(getContext(), "No users found to export data", Toast.LENGTH_LONG)
          .show());
      return false;
    }

    // Create export directory
    File exportDir = new File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SketchIDData"
    );
    if (!exportDir.exists() && !exportDir.mkdirs()) {
      requireActivity().runOnUiThread(() -> Toast
          .makeText(getContext(), "Failed to create export directory", Toast.LENGTH_LONG)
          .show());
      return false;
    }

    ContentResolver contentResolver = requireActivity().getContentResolver();
    boolean allExported = true;
    String timestamp = new SimpleDateFormat(
        Constants.EXPORT_DATE_FORMAT,
                                            Locale.getDefault()
    ).format(new Date());

    // Export data for each user
    for (User user : users) {
      List<DrawingExportData> drawingDataList = db
          .drawingDataDao()
          .getAllDrawingDataWithUsersAndImagesByUserID(user.id);

      if (drawingDataList.isEmpty()) {
        continue; // Skip users with no data
      }

      String fileName = user.name + "_drawing_data_" + timestamp + ".csv";
      File file = new File(exportDir, fileName);
      Uri exportFileUri = Uri.fromFile(file);

      try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
        if (outputStream != null) {
          outputStream.write((
                                 "id,userID,userName,attempt,time,x,y,action,itemType,imageID,"
                                 + "imageName%n"
                             ).getBytes());

          for (DrawingExportData data : drawingDataList) {
            String row = String.format(
                Locale.getDefault(),
                "%d,%d,%s,%d,%d,%s,%s,%s,%s,%d,%s%n",
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
                data.getImageName()
            );
            outputStream.write(row.getBytes());
          }
        }
      } catch (IOException e) {
        allExported = false;
        requireActivity().runOnUiThread(() -> Toast
            .makeText(
                getContext(),
                "Failed to export data for user: " + user.name,
                Toast.LENGTH_LONG
            )
            .show());
      }
    }

    if (allExported) {
      requireActivity().runOnUiThread(() -> Toast
          .makeText(
              getContext(),
              "All drawing data exported to Downloads/SketchIDData",
              Toast.LENGTH_LONG
          )
          .show());
    }

    return allExported;
  }

  private void setupDeleteUserDataPreference() {
    if (deleteUserDataPreference != null) {
      // Set up click listener to show a direct AlertDialog
      deleteUserDataPreference.setOnPreferenceClickListener(preference -> {
        // Only proceed if a user is selected
        if (isNotValidUserSelected()) {
          return true;
        }

        String dialogMessage = createDeleteUserDataMessage();
        Drawable dialogIcon = createWarningIcon();

        // Create and show the alert dialog
        AlertDialog alertDialog = createDeleteUserDataDialog(dialogMessage, dialogIcon);
        alertDialog.show();

        // Apply tint to the positive button
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
    // Execute data deletion
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

  private void setupUploadAllDataPreference() {
    if (uploadAllDataPreference != null) {
      uploadAllDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::uploadAllDataToFirebase);
        return true;
      });
    }
  }

  private void uploadAllDataToFirebase() {
    // Get all users
    List<User> users = db.userDao().getAllUsers();
    if (users.isEmpty()) {
      showToast("No users found to upload data");
      return;
    }

    // Create export directory
    File exportDir = createExportDirectory();
    if (exportDir == null) {
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

    // Export and upload data for each user
    for (User user : users) {
      List<DrawingExportData> drawingDataList = db
          .drawingDataDao()
          .getAllDrawingDataWithUsersAndImagesByUserID(user.id);

      if (drawingDataList.isEmpty()) {
        continue; // Skip users with no data
      }

      processUserData(user, drawingDataList, context);
    }
  }

  private void showToast(String message) {
    requireActivity().runOnUiThread(() -> Toast
        .makeText(getContext(), message, Toast.LENGTH_LONG)
        .show());
  }

  private void processUserData(
      User user,
      List<DrawingExportData> drawingDataList,
      UploadContext context
  ) {
    String fileName = user.name + Constants.DRAWING_DATA_PREFIX + context.timestamp + ".csv";
    File file = new File(context.exportDir, fileName);
    Uri uploadFileUri = Uri.fromFile(file);

    try (OutputStream outputStream = context.contentResolver.openOutputStream(uploadFileUri)) {
      if (outputStream != null) {
        writeUserDataToFile(outputStream, drawingDataList);
        uploadFileToFirebase(
            user,
            fileName,
            uploadFileUri,
            context.storageRef,
            context.uploadedCount,
            context.totalUsers
        );
      }
    } catch (IOException e) {
      showToast("Failed to export data for user: " + user.name);
    }
  }

  private void writeUserDataToFile(
      OutputStream outputStream,
      List<DrawingExportData> drawingDataList
  )
  throws IOException {
    outputStream.write(Constants.COLUMN_HEADERS.getBytes());

    for (DrawingExportData data : drawingDataList) {
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
          data.getImageName()
      );
      outputStream.write(row.getBytes());
    }
  }

  private void uploadFileToFirebase(
      User user,
      String fileName,
      Uri uploadFileUri,
      StorageReference storageRef,
      AtomicInteger uploadedCount,
      int totalUsers
  ) {
    StorageReference fileRef = storageRef.child("SketchIDData/" + fileName);
    UploadTask uploadTask = fileRef.putFile(uploadFileUri);

    uploadTask.addOnSuccessListener(taskSnapshot -> {
      uploadedCount.getAndIncrement();
      if (uploadedCount.get() == totalUsers) {
        showToast("All data uploaded to Firebase");
      }
    }).addOnFailureListener(e -> {
      showToast("Failed to upload data for user: " + user.name);
      uploadedCount.getAndIncrement();
    });
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

  private void setupClearDataPreference() {
    if (clearDataPreference != null) {
      // Set up click listener to show a direct AlertDialog
      clearDataPreference.setOnPreferenceClickListener(preference -> {
        String dialogMessage = "Are you sure you want to delete ALL drawing data for ALL users?%n%n"
                               + "This action will permanently remove all drawing data from the "
                               + "database and cannot be undone.";

        // Get and tint the icon
        Drawable dialogIcon = ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        );

        if (dialogIcon != null) {
          dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        }

        // Create and show the alert dialog
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Clear All Drawing Data?")
            .setMessage(dialogMessage)
            .setIcon(dialogIcon)
            .setPositiveButton(
                DELETE_BUTTON, (dialog, which) ->
                    // Execute clearing all data
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

        // Apply tint to the positive button
        try {
          int errorColor = getResources().getColor(R.color.error, requireContext().getTheme());
          alertDialog.setOnShowListener(dialog -> alertDialog
              .getButton(DialogInterface.BUTTON_POSITIVE)
              .setTextColor(errorColor));
        } catch (Exception e) {
          // Ignore styling errors
        }

        return true;
      });
    }
  }

  private void setupSelectImagePreference() {
    if (selectImagePreference != null) {
      ActivityResultLauncher<Intent> imageSelectionLauncher = registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK
                && result.getData() != null) {
              int selectedImageId = result.getData().getIntExtra("selected_image_id", -1);
              if (selectedImageId != -1) {
                // Handle the selected image
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

  private void setupResetUserProgressPreference() {
    if (resetUserProgressPreference != null) {
      // Initialize unfinishedUsers list to prevent NPE
      if (unfinishedUsers == null) {
        unfinishedUsers = new ArrayList<>();
        resetUserProgressPreference.setVisible(false);
      }

      resetUserProgressPreference.setTitleColor(0xFFB3261E);

      // Skip setting dialog properties, we'll show a custom AlertDialog instead

      resetUserProgressPreference.setOnPreferenceClickListener(preference -> {
        // Show a custom AlertDialog directly
        String userNames = buildUnfinishedUsersList();

        String dialogMessage = String.format(
            "Resetting progress will delete all unfinished drawing sessions for the listed users,"
            + " allowing them to start fresh.%n%n"
            + "This action will:%n"
            + "• Delete all in-progress drawing data%n"
            + "• Reset current attempts to zero%n"
            + "• Allow users to start drawing again%n%n"
            + "The following users have unfinished drawing sessions:%n%n%s", userNames
        );

        // Get and tint the icon
        Drawable dialogIcon = ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        );

        if (dialogIcon != null) {
          dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        }

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Reset User Progress?")
            .setMessage(dialogMessage)
            .setIcon(dialogIcon)
            .setPositiveButton(
                DELETE_BUTTON,
                (dialog, which) -> executor.execute(this::resetUserProgress)
            )
            .setNegativeButton(CANCEL_BUTTON, null)
            .create();

        alertDialog.show();

        // Apply tint to the positive button
        try {
          int errorColor = getResources().getColor(R.color.error, requireContext().getTheme());
          alertDialog.setOnShowListener(dialog -> alertDialog
              .getButton(DialogInterface.BUTTON_POSITIVE)
              .setTextColor(errorColor));
        } catch (Exception e) {
          // Ignore styling errors
        }

        return true;
      });
    }
  }

  private String buildUnfinishedUsersList() {
    StringBuilder userNamesBuilder = new StringBuilder();
    for (UserProgressManager.UserProgress progress : unfinishedUsers) {
      // Get details about the session
      int itemIndex = progress.getItemIndex();
      int attemptCount = progress.getItemAttempt();

      userNamesBuilder
          .append("• ")
          .append(progress.getUserName())
          .append(" (Drawing #")
          .append(itemIndex + 1)
          .append(", Attempt: ")
          .append(attemptCount)
          .append(")%n");
    }
    return userNamesBuilder.toString().trim();
  }

  private void resetUserProgress() {
    // Reset progress for all unfinished users
    for (UserProgressManager.UserProgress progress : unfinishedUsers) {
      resetSingleUserProgress(progress);
    }

    // Refresh the UI
    requireActivity().runOnUiThread(() -> {
      showToast("User progress reset successfully");
      unfinishedUsers.clear();
      updatePreferencesVisibility();
    });
  }

  private void updatePreferencesVisibility() {
    boolean hasUnfinishedUsers = !unfinishedUsers.isEmpty();

    if (hasUnfinishedUsers) {
      // Show reset progress preference and disable certain categories
      if (resetUserProgressPreference != null) {
        resetUserProgressPreference.setVisible(true);
        resetUserProgressPreference.setTitleColor(0xFFB3261E);
      }

      if (drawingSettingsCategory != null) {
        drawingSettingsCategory.setEnabled(false);
      }

      if (dataManagementCategory != null) {
        dataManagementCategory.setEnabled(false);
      }
    } else {
      // Hide reset progress preference and enable all categories
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

  private void resetSingleUserProgress(UserProgressManager.UserProgress progress) {
    long userId = progress.getUserId();
    String sessionId = progress.getSessionId();

    // Get all unfinished sessions for this user
    List<UserProgressManager.Session> unfinishedSessions
        = UserProgressManager.getUnfinishedSessions(requireContext(), userId);

    // Mark all sessions as finished
    for (UserProgressManager.Session session : unfinishedSessions) {
      resetSession(userId, session.getSessionId());
    }

    // For backward compatibility: use the legacy session ID if provided
    if (sessionId != null) {
      db.drawingDataDao().deleteUserSessionData(userId, sessionId);
    }

    // Clear progress in preferences
    UserProgressManager.clearUserProgress(requireContext(), userId);
  }

  private void resetSession(long userId, String sessionId) {
    // Mark the session as finished
    UserProgressManager.markSessionFinished(requireContext(), userId, sessionId);

    // Delete drawing data for this session from the database
    db.drawingDataDao().deleteUserSessionData(userId, sessionId);
  }

  /**
   * Checks for unfinished users during initial load
   * This is separate from the onResume check to prevent UI jumping
   */
  private void checkUnfinishedUsersInitial() {
    unfinishedUsers = UserProgressManager.getUnfinishedUsers(requireContext(), db);

    // Update UI on the main thread if needed
    if (!unfinishedUsers.isEmpty()) {
      requireActivity().runOnUiThread(this::updatePreferencesVisibility);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    // Only check for unfinished users if we need to
    // This prevents UI jumping on each resume
    executor.execute(() -> {
      List<UserProgressManager.UserProgress> newUnfinishedUsers
          = UserProgressManager.getUnfinishedUsers(requireContext(), db);

      // Only update UI if the state has changed
      boolean wasEmpty = unfinishedUsers.isEmpty();
      boolean isEmpty = newUnfinishedUsers.isEmpty();

      if (wasEmpty != isEmpty) {
        unfinishedUsers = newUnfinishedUsers;
        requireActivity().runOnUiThread(this::updatePreferencesVisibility);
      }
    });
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
}
