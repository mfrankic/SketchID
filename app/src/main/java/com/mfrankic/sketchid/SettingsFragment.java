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
      // Ignore any exceptions related to tinting
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
        executor.execute(this::exportDataToCSV);
        return true;
      });
    }
  }

  private void exportDataToCSV() {
    exportDataToCSV(false);
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
          // Ignore any exceptions related to tinting
        }

        return true;
      });
    }
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
          // Ignore any exceptions related to tinting
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

      UserProgressManager.Session session = UserProgressManager.getSession(
          requireContext(),
          progress.getUserId(),
          progress.getSessionId()
      );

      String attempts = "unknown";
      String drawingMode = "normal";
      int totalImages = 0;

      if (session != null && session.getSettings() != null) {

        Object attemptsObj = session.getSettings().get(Constants.DRAWING_KEY_ATTEMPTS);
        if (attemptsObj != null) {
          attempts = attemptsObj.toString();
        }

        Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
        if (modeObj != null) {
          drawingMode = modeObj.toString();
        }

        try {
          Object selectedImagesObj = session
              .getSettings()
              .get(Constants.DRAWING_KEY_SELECTED_IMAGE_IDS);
          if (selectedImagesObj != null) {
            if (selectedImagesObj instanceof List) {
              totalImages = ((List<?>) selectedImagesObj).size();
            } else if (selectedImagesObj.toString().startsWith("[") && selectedImagesObj
                .toString()
                .endsWith("]")) {

              String arrayStr = selectedImagesObj.toString();

              if (arrayStr.length() > 2) {
                totalImages = arrayStr.split(",").length;
              }
            }
          }

          if (totalImages == 0) {
            Set<Integer> selectedImages = SelectedImagesManager.getSelectedImages(requireContext());
            totalImages = selectedImages.size();
          }
        } catch (Exception ignored) {
          // Ignore any exceptions related to parsing selected images
        }
      }

      if (totalImages == 0) {
        totalImages = 1;
      }

      int currentItemIndex = progress.getItemIndex();
      int currentItemAttempt = progress.getItemAttempt();
      int attemptsPerImage = 1;
      try {
        if (!attempts.equals("unknown")) {
          attemptsPerImage = Integer.parseInt(attempts);
        }
      } catch (NumberFormatException ignored) {
        // Ignore any exceptions related to parsing attempts
      }

      int overallAttempt = (currentItemIndex * attemptsPerImage) + currentItemAttempt;
      int totalAttempts = totalImages * attemptsPerImage;

      builder.append(String.format(
          Locale.getDefault(),
          " • %s:%n" + "   ⸰ Progress: %d/%s (%d/%d)%n" + "   ⸰ Mode: %s%n",
          progress.getUserName(),
          currentItemAttempt,
          attempts,
          overallAttempt,
          totalAttempts,
          drawingMode
      ));
    }

    builder.append("\nDo you want to reset progress for these users?");
    return builder.toString();
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

  private void setupUploadAllDataPreference() {
    if (uploadAllDataPreference != null) {
      uploadAllDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::uploadAllDataToFirebase);
        return true;
      });
    }
  }

  private void showToast(String message) {
    requireActivity().runOnUiThread(() -> Toast
        .makeText(getContext(), message, Toast.LENGTH_LONG)
        .show());
  }

  private boolean exportDataToCSV(boolean silent) {
    List<DrawingExportData> drawingDataList = db
        .drawingDataDao()
        .getAllDrawingDataWithUsersAndImagesByUserID(Long.parseLong(selectedUserID));

    List<GravityExportData> gravityDataList = db
        .gravityDataDao()
        .getGravityDataWithUsersAndImagesByUserId(Long.parseLong(selectedUserID));

    List<GyroscopeExportData> gyroscopeDataList = db
        .gyroscopeDataDao()
        .getGyroscopeDataWithUsersAndImagesByUserId(Long.parseLong(selectedUserID));

    List<MagneticFieldExportData> magneticFieldDataList = db
        .magneticFieldDataDao()
        .getMagneticFieldDataWithUsersAndImagesByUserId(Long.parseLong(selectedUserID));

    List<AccelerometerExportData> accelerometerDataList = db
        .accelerometerDataDao()
        .getAccelerometerDataWithUsersAndImagesByUserId(Long.parseLong(selectedUserID));

    processDrawingModes(drawingDataList);
    processGravityDrawingModes(gravityDataList);
    processGyroscopeDrawingModes(gyroscopeDataList);
    processMagneticFieldDrawingModes(magneticFieldDataList);
    processAccelerometerDrawingModes(accelerometerDataList);

    Map<String, List<DrawingExportData>> dataByMode = groupDataByDrawingMode(drawingDataList);
    Map<String, List<GravityExportData>> gravityDataByMode = groupGravityDataByDrawingMode(
        gravityDataList);
    Map<String, List<GyroscopeExportData>> gyroscopeDataByMode = groupGyroscopeDataByDrawingMode(
        gyroscopeDataList);
    Map<String, List<MagneticFieldExportData>> magneticFieldDataByMode
        = groupMagneticFieldDataByDrawingMode(magneticFieldDataList);
    Map<String, List<AccelerometerExportData>> accelerometerDataByMode
        = groupAccelerometerDataByDrawingMode(accelerometerDataList);

    ContentResolver contentResolver = requireActivity().getContentResolver();
    boolean allExported = true;

    File exportDir = createExportDirectory();
    if (exportDir == null) {
      return false;
    }

    for (Map.Entry<String, List<DrawingExportData>> entry : dataByMode.entrySet()) {
      String mode = entry.getKey();
      List<DrawingExportData> modeData = entry.getValue();

      if (modeData.isEmpty()) {
        continue;
      }

      String timestamp = new SimpleDateFormat(
          Constants.EXPORT_DATE_FORMAT,
                                              Locale.getDefault()
      ).format(new Date());

      String fileName = userListPreference.getEntry()
                        + Constants.DRAWING_DATA_PREFIX
                        + "mode_"
                        + mode
                        + "_"
                        + timestamp
                        + ".csv";

      File file = new File(exportDir, fileName);
      Uri exportFileUri = Uri.fromFile(file);

      if (exportFileUri != null) {
        try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
          if (outputStream != null) {
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

            if (mode.equals(Constants.DRAWING_MODE_NORMAL)) {
              fileUri = exportFileUri;
            }
          }
        } catch (IOException e) {
          allExported = false;
          requireActivity().runOnUiThread(() -> Toast
              .makeText(getContext(), "Failed to export data for mode: " + mode, Toast.LENGTH_LONG)
              .show());
        }
      }
    }

    for (Map.Entry<String, List<GravityExportData>> entry : gravityDataByMode.entrySet()) {
      String mode = entry.getKey();
      List<GravityExportData> modeData = entry.getValue();

      if (modeData.isEmpty()) {
        continue;
      }

      String timestamp = new SimpleDateFormat(
          Constants.EXPORT_DATE_FORMAT,
                                              Locale.getDefault()
      ).format(new Date());

      String fileName = userListPreference.getEntry()
                        + Constants.GRAVITY_DATA_PREFIX
                        + "mode_"
                        + mode
                        + "_"
                        + timestamp
                        + ".csv";

      File file = new File(exportDir, fileName);
      Uri exportFileUri = Uri.fromFile(file);

      if (exportFileUri != null) {
        try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
          if (outputStream != null) {
            outputStream.write(Constants.GRAVITY_COLUMN_HEADERS.getBytes());
            for (GravityExportData data : modeData) {
              String row = String.format(
                  Locale.getDefault(),
                  Constants.GRAVITY_EXPORT_PATTERN,
                  data.getId(),
                  data.getUserId(),
                  data.getUserName(),
                  data.getImageId(),
                  data.getImageName(),
                  data.getAttempt(),
                  data.getTimestamp(),
                  data.getX(),
                  data.getY(),
                  data.getZ(),
                  data.getDrawingMode()
              );
              outputStream.write(row.getBytes());
            }
          }
        } catch (IOException e) {
          allExported = false;
          requireActivity().runOnUiThread(() -> Toast
              .makeText(
                  getContext(),
                  "Failed to export gravity data for mode: " + mode,
                  Toast.LENGTH_LONG
              )
              .show());
        }
      }
    }

    for (Map.Entry<String, List<GyroscopeExportData>> entry : gyroscopeDataByMode.entrySet()) {
      String mode = entry.getKey();
      List<GyroscopeExportData> modeData = entry.getValue();

      if (modeData.isEmpty()) {
        continue;
      }

      String timestamp = new SimpleDateFormat(
          Constants.EXPORT_DATE_FORMAT,
                                              Locale.getDefault()
      ).format(new Date());

      String fileName = userListPreference.getEntry()
                        + Constants.GYROSCOPE_DATA_PREFIX
                        + "mode_"
                        + mode
                        + "_"
                        + timestamp
                        + ".csv";

      File file = new File(exportDir, fileName);
      Uri exportFileUri = Uri.fromFile(file);

      if (exportFileUri != null) {
        try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
          if (outputStream != null) {
            outputStream.write(Constants.GYROSCOPE_COLUMN_HEADERS.getBytes());
            for (GyroscopeExportData data : modeData) {
              String row = String.format(
                  Locale.getDefault(),
                  Constants.GYROSCOPE_EXPORT_PATTERN,
                  data.getId(),
                  data.getUserId(),
                  data.getUserName(),
                  data.getImageId(),
                  data.getImageName(),
                  data.getAttempt(),
                  data.getTimestamp(),
                  data.getX(),
                  data.getY(),
                  data.getZ(),
                  data.getDrawingMode()
              );
              outputStream.write(row.getBytes());
            }
          }
        } catch (IOException e) {
          allExported = false;
          requireActivity().runOnUiThread(() -> Toast
              .makeText(
                  getContext(),
                  "Failed to export gyroscope data for mode: " + mode,
                  Toast.LENGTH_LONG
              )
              .show());
        }
      }
    }

    for (Map.Entry<String, List<MagneticFieldExportData>> entry :
        magneticFieldDataByMode.entrySet()) {
      String mode = entry.getKey();
      List<MagneticFieldExportData> modeData = entry.getValue();

      if (modeData.isEmpty()) {
        continue;
      }

      String timestamp = new SimpleDateFormat(
          Constants.EXPORT_DATE_FORMAT,
                                              Locale.getDefault()
      ).format(new Date());

      String fileName = userListPreference.getEntry()
                        + Constants.MAGNETIC_FIELD_DATA_PREFIX
                        + "mode_"
                        + mode
                        + "_"
                        + timestamp
                        + ".csv";

      File file = new File(exportDir, fileName);
      Uri exportFileUri = Uri.fromFile(file);

      if (exportFileUri != null) {
        try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
          if (outputStream != null) {
            outputStream.write(Constants.MAGNETIC_FIELD_COLUMN_HEADERS.getBytes());
            for (MagneticFieldExportData data : modeData) {
              String row = String.format(
                  Locale.getDefault(),
                  Constants.MAGNETIC_FIELD_EXPORT_PATTERN,
                  data.getId(),
                  data.getUserId(),
                  data.getUserName(),
                  data.getImageId(),
                  data.getImageName(),
                  data.getAttempt(),
                  data.getTimestamp(),
                  data.getX(),
                  data.getY(),
                  data.getZ(),
                  data.getDrawingMode()
              );
              outputStream.write(row.getBytes());
            }
          }
        } catch (IOException e) {
          allExported = false;
          requireActivity().runOnUiThread(() -> Toast.makeText(
              getContext(),
              "Failed to export magnetic field data for mode: "
              + mode,
              Toast.LENGTH_LONG
          ).show());
        }
      }
    }

    for (Map.Entry<String, List<AccelerometerExportData>> entry :
        accelerometerDataByMode.entrySet()) {
      String mode = entry.getKey();
      List<AccelerometerExportData> modeData = entry.getValue();

      if (modeData.isEmpty()) {
        continue;
      }

      String timestamp = new SimpleDateFormat(
          Constants.EXPORT_DATE_FORMAT,
                                              Locale.getDefault()
      ).format(new Date());

      String fileName = userListPreference.getEntry()
                        + Constants.ACCELEROMETER_DATA_PREFIX
                        + "mode_"
                        + mode
                        + "_"
                        + timestamp
                        + ".csv";

      File file = new File(exportDir, fileName);
      Uri exportFileUri = Uri.fromFile(file);

      if (exportFileUri != null) {
        try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
          if (outputStream != null) {
            outputStream.write(Constants.ACCELEROMETER_COLUMN_HEADERS.getBytes());
            for (AccelerometerExportData data : modeData) {
              String row = String.format(
                  Locale.getDefault(),
                  Constants.ACCELEROMETER_EXPORT_PATTERN,
                  data.getId(),
                  data.getUserId(),
                  data.getUserName(),
                  data.getImageId(),
                  data.getImageName(),
                  data.getAttempt(),
                  data.getTimestamp(),
                  data.getX(),
                  data.getY(),
                  data.getZ(),
                  data.getDrawingMode()
              );
              outputStream.write(row.getBytes());
            }
          }
        } catch (IOException e) {
          allExported = false;
          requireActivity().runOnUiThread(() -> Toast.makeText(
              getContext(),
              "Failed to export accelerometer data for mode: "
              + mode,
              Toast.LENGTH_LONG
          ).show());
        }
      }
    }

    if (allExported && !silent) {
      requireActivity().runOnUiThread(() -> Toast
          .makeText(getContext(), "Data exported to Downloads/SketchIDData", Toast.LENGTH_LONG)
          .show());
    }

    return allExported;
  }

  /**
   * Process drawing data to add drawing mode information from session settings
   */
  private void processDrawingModes(List<DrawingExportData> drawingDataList) {
    for (DrawingExportData data : drawingDataList) {
      if (data.getSessionID() != null) {
        UserProgressManager.Session session = UserProgressManager.getSession(
            requireContext(),
            data.getUserID(),
            data.getSessionID()
        );

        if (session != null && session.getSettings() != null) {
          Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
          if (modeObj != null) {
            data.setDrawingMode(modeObj.toString());
          } else {
            data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
          }
        } else {
          data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
        }
      } else {
        data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
      }
    }
  }

  /**
   * Process gravity data to add drawing mode information from session settings
   */
  private void processGravityDrawingModes(List<GravityExportData> gravityDataList) {
    for (GravityExportData data : gravityDataList) {
      if (data.getSessionId() != null) {
        UserProgressManager.Session session = UserProgressManager.getSession(
            requireContext(),
            data.getUserId(),
            data.getSessionId()
        );

        if (session != null && session.getSettings() != null) {
          Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
          if (modeObj != null) {
            data.setDrawingMode(modeObj.toString());
          } else {
            data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
          }
        } else {
          data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
        }
      } else {
        data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
      }
    }
  }

  /**
   * Process gyroscope data to add drawing mode information from session settings
   */
  private void processGyroscopeDrawingModes(List<GyroscopeExportData> gyroscopeDataList) {
    for (GyroscopeExportData data : gyroscopeDataList) {
      if (data.getSessionId() != null) {
        UserProgressManager.Session session = UserProgressManager.getSession(
            requireContext(),
            data.getUserId(),
            data.getSessionId()
        );

        if (session != null && session.getSettings() != null) {
          Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
          if (modeObj != null) {
            data.setDrawingMode(modeObj.toString());
          } else {
            data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
          }
        } else {
          data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
        }
      } else {
        data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
      }
    }
  }

  /**
   * Process magnetic field data to add drawing mode information from session settings
   */
  private void processMagneticFieldDrawingModes(List<MagneticFieldExportData> magneticFieldDataList) {
    for (MagneticFieldExportData data : magneticFieldDataList) {
      if (data.getSessionId() != null) {
        UserProgressManager.Session session = UserProgressManager.getSession(
            requireContext(),
            data.getUserId(),
            data.getSessionId()
        );

        if (session != null && session.getSettings() != null) {
          Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
          if (modeObj != null) {
            data.setDrawingMode(modeObj.toString());
          } else {
            data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
          }
        } else {
          data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
        }
      } else {
        data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
      }
    }
  }

  /**
   * Process accelerometer data to add drawing mode information from session settings
   */
  private void processAccelerometerDrawingModes(List<AccelerometerExportData> accelerometerDataList) {
    for (AccelerometerExportData data : accelerometerDataList) {
      if (data.getSessionId() != null) {
        UserProgressManager.Session session = UserProgressManager.getSession(
            requireContext(),
            data.getUserId(),
            data.getSessionId()
        );

        if (session != null && session.getSettings() != null) {
          Object modeObj = session.getSettings().get(Constants.DRAWING_KEY_MODE);
          if (modeObj != null) {
            data.setDrawingMode(modeObj.toString());
          } else {
            data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
          }
        } else {
          data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
        }
      } else {
        data.setDrawingMode(Constants.DRAWING_MODE_NORMAL);
      }
    }
  }

  /**
   * Group drawing data by drawing mode
   */
  private Map<String, List<DrawingExportData>> groupDataByDrawingMode(
      List<DrawingExportData> drawingDataList
  ) {
    Map<String, List<DrawingExportData>> dataByMode = new HashMap<>();

    dataByMode.put(Constants.DRAWING_MODE_NORMAL, new ArrayList<>());
    dataByMode.put(Constants.DRAWING_MODE_OVERLAY, new ArrayList<>());

    for (DrawingExportData data : drawingDataList) {
      String mode = data.getDrawingMode();
      dataByMode.computeIfAbsent(mode, k -> new ArrayList<>()).add(data);
    }

    return dataByMode;
  }

  /**
   * Group gravity data by drawing mode
   */
  private Map<String, List<GravityExportData>> groupGravityDataByDrawingMode(
      List<GravityExportData> gravityDataList
  ) {
    Map<String, List<GravityExportData>> dataByMode = new HashMap<>();

    dataByMode.put(Constants.DRAWING_MODE_NORMAL, new ArrayList<>());
    dataByMode.put(Constants.DRAWING_MODE_OVERLAY, new ArrayList<>());

    for (GravityExportData data : gravityDataList) {
      String mode = data.getDrawingMode();
      dataByMode.computeIfAbsent(mode, k -> new ArrayList<>()).add(data);
    }

    return dataByMode;
  }

  /**
   * Group gyroscope data by drawing mode
   */
  private Map<String, List<GyroscopeExportData>> groupGyroscopeDataByDrawingMode(
      List<GyroscopeExportData> gyroscopeDataList
  ) {
    Map<String, List<GyroscopeExportData>> dataByMode = new HashMap<>();

    dataByMode.put(Constants.DRAWING_MODE_NORMAL, new ArrayList<>());
    dataByMode.put(Constants.DRAWING_MODE_OVERLAY, new ArrayList<>());

    for (GyroscopeExportData data : gyroscopeDataList) {
      String mode = data.getDrawingMode();
      dataByMode.computeIfAbsent(mode, k -> new ArrayList<>()).add(data);
    }

    return dataByMode;
  }

  /**
   * Group magnetic field data by drawing mode
   */
  private Map<String, List<MagneticFieldExportData>> groupMagneticFieldDataByDrawingMode(
      List<MagneticFieldExportData> magneticFieldDataList
  ) {
    Map<String, List<MagneticFieldExportData>> dataByMode = new HashMap<>();

    dataByMode.put(Constants.DRAWING_MODE_NORMAL, new ArrayList<>());
    dataByMode.put(Constants.DRAWING_MODE_OVERLAY, new ArrayList<>());

    for (MagneticFieldExportData data : magneticFieldDataList) {
      String mode = data.getDrawingMode();
      dataByMode.computeIfAbsent(mode, k -> new ArrayList<>()).add(data);
    }

    return dataByMode;
  }

  /**
   * Group accelerometer data by drawing mode
   */
  private Map<String, List<AccelerometerExportData>> groupAccelerometerDataByDrawingMode(
      List<AccelerometerExportData> accelerometerDataList
  ) {
    Map<String, List<AccelerometerExportData>> dataByMode = new HashMap<>();

    dataByMode.put(Constants.DRAWING_MODE_NORMAL, new ArrayList<>());
    dataByMode.put(Constants.DRAWING_MODE_OVERLAY, new ArrayList<>());

    for (AccelerometerExportData data : accelerometerDataList) {
      String mode = data.getDrawingMode();
      dataByMode.computeIfAbsent(mode, k -> new ArrayList<>()).add(data);
    }

    return dataByMode;
  }

  private boolean exportAllDataToCSV() {

    List<User> users = db.userDao().getAllUsers();
    if (users.isEmpty()) {
      requireActivity().runOnUiThread(() -> Toast
          .makeText(getContext(), "No users found to export data", Toast.LENGTH_LONG)
          .show());
      return false;
    }

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

    for (User user : users) {
      List<DrawingExportData> drawingDataList = db
          .drawingDataDao()
          .getAllDrawingDataWithUsersAndImagesByUserID(user.id);

      if (drawingDataList.isEmpty()) {
        continue;
      }

      processDrawingModes(drawingDataList);

      Map<String, List<DrawingExportData>> dataByMode = groupDataByDrawingMode(drawingDataList);

      for (Map.Entry<String, List<DrawingExportData>> entry : dataByMode.entrySet()) {
        String mode = entry.getKey();
        List<DrawingExportData> modeData = entry.getValue();

        if (modeData.isEmpty()) {
          continue;
        }

        String fileName = user.name
                          + Constants.DRAWING_DATA_PREFIX
                          + "mode_"
                          + mode
                          + "_"
                          + timestamp
                          + ".csv";

        File file = new File(exportDir, fileName);
        Uri exportFileUri = Uri.fromFile(file);

        try (OutputStream outputStream = contentResolver.openOutputStream(exportFileUri)) {
          if (outputStream != null) {
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
        } catch (IOException e) {
          allExported = false;
          requireActivity().runOnUiThread(() -> Toast.makeText(
              getContext(),
              "Failed to export data for user: " + user.name + ", mode: " + mode,
              Toast.LENGTH_LONG
          ).show());
        }
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

  private void uploadAllDataToFirebase() {

    List<User> users = db.userDao().getAllUsers();
    if (users.isEmpty()) {
      showToast("No users found to upload data");
      return;
    }

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

    for (User user : users) {
      List<DrawingExportData> drawingDataList = db
          .drawingDataDao()
          .getAllDrawingDataWithUsersAndImagesByUserID(user.id);

      if (drawingDataList.isEmpty()) {
        continue;
      }

      processUserData(user, drawingDataList, context);
    }
  }

  private void processUserData(
      User user,
      List<DrawingExportData> drawingDataList,
      UploadContext context
  ) {

    processDrawingModes(drawingDataList);

    Map<String, List<DrawingExportData>> dataByMode = groupDataByDrawingMode(drawingDataList);

    for (Map.Entry<String, List<DrawingExportData>> entry : dataByMode.entrySet()) {
      String mode = entry.getKey();
      List<DrawingExportData> modeData = entry.getValue();

      if (modeData.isEmpty()) {
        continue;
      }

      String fileName = user.name
                        + Constants.DRAWING_DATA_PREFIX
                        + "mode_"
                        + mode
                        + "_"
                        + context.timestamp
                        + ".csv";

      File file = new File(context.exportDir, fileName);
      Uri uploadFileUri = Uri.fromFile(file);

      try (OutputStream outputStream = context.contentResolver.openOutputStream(uploadFileUri)) {
        if (outputStream != null) {

          writeUserDataToFile(outputStream, modeData);

          if (mode.equals(Constants.DRAWING_MODE_NORMAL)) {
            uploadFileToFirebase(
                user,
                fileName,
                uploadFileUri,
                context.storageRef,
                context.uploadedCount,
                context.totalUsers
            );
          }
        }
      } catch (IOException e) {
        showToast("Failed to export data for user: " + user.name + ", mode: " + mode);
      }
    }
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

  private void writeUserDataToFile(
      OutputStream outputStream,
      List<DrawingExportData> drawingDataList
  )
  throws IOException {

    processDrawingModes(drawingDataList);

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
          data.getImageName(),
          data.getDrawingMode(),
          data.getSize(),
          data.getPressure(),
          data.getOrientation()
      );
      outputStream.write(row.getBytes());
    }
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
