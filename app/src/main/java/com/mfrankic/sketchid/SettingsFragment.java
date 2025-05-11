package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;
import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.KEY_SELECTED_USER;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsFragment extends PreferenceFragmentCompat {

  public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
  private static final int SELECT_IMAGE_REQUEST = 1;
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
  }

  @Override
  public void onDisplayPreferenceDialog(@NonNull Preference preference) {
    if (preference instanceof CustomDialogPreference) {
      DialogFragment dialogFragment = DialogPrefFragCompat.newInstance(preference.getKey());
      dialogFragment.setTargetFragment(this, 0);
      dialogFragment.show(requireFragmentManager(), null);
    } else {
      super.onDisplayPreferenceDialog(preference);
    }
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
      newUserPreference.setOnBindEditTextListener(editText -> editText.setHint("Name"));
      newUserPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        String newUserName = newValue.toString();
        if (newUserName.isEmpty()) {
          Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
          return false;
        }
        executor.execute(() -> {
          User newUser = new User(newUserName);
          String newUserID = Long.toString(db.userDao().insertUser(newUser));
          PreferenceManager
              .getDefaultSharedPreferences(requireContext())
              .edit()
              .putString(KEY_SELECTED_USER, newUserID)
              .apply();
          loadUsersIntoListPreference();
        });
        return false;
      });
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
      deleteUserPreference.setPositiveButtonText("Delete");
      deleteUserPreference.setNegativeButtonText("Cancel");
      deleteUserPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        executor.execute(() -> {
          int deletedRows = db.userDao().deleteUser(Long.parseLong(selectedUserID));
          if (deletedRows > 0) {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(getContext(), "User deleted", Toast.LENGTH_LONG)
                .show());

            // Delete user's drawing data from the database
            db.drawingDataDao().deleteDrawingDataByUserID(Long.parseLong(selectedUserID));

            // Delete user's session data
            UserProgressManager.deleteUserSessions(
                requireContext(),
                Long.parseLong(selectedUserID)
            );

            PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .edit()
                .remove(KEY_SELECTED_USER)
                .apply();
            loadUsersIntoListPreference();
            userListPreference.callChangeListener("-1");
            SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(requireContext());
            prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
          } else {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(getContext(), "Failed to delete user", Toast.LENGTH_LONG)
                .show());
          }
        });
        return false;
      });
      deleteUserPreference.setOnPreferenceClickListener(preference -> {
        deleteUserPreference.getExtras().putString("value", selectedUserID);
        deleteUserPreference.setDialogMessage(String.format(
            "Delete User: %s",
            userListPreference.getEntry()
        ));
        Drawable dialogIcon = Objects.requireNonNull(ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        ));
        dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        deleteUserPreference.setDialogIcon(dialogIcon);
        return true;
      });
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

  private void uploadDataToFirebase() {
    boolean success = exportDataToCSV(true);
    if (!success) {
      return;
    }
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
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

  private void setupUploadDataPreference() {
    if (uploadDataPreference != null) {
      uploadDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::uploadDataToFirebase);
        return true;
      });
    }
  }

  private boolean exportDataToCSV(boolean silent) {
    List<DrawingExportData> drawingDataList = db
        .drawingDataDao()
        .getAllDrawingDataWithUsersAndImagesByUserID(Long.parseLong(selectedUserID));
    String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
    String fileName = userListPreference.getEntry() + "_drawing_data_" + timestamp + ".csv";
    ContentResolver contentResolver = requireActivity().getContentResolver();

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

    File file = new File(exportDir, fileName);
    fileUri = Uri.fromFile(file);

    if (fileUri != null) {
      try (OutputStream outputStream = contentResolver.openOutputStream(fileUri)) {
        if (outputStream != null) {
          outputStream.write((
                                 "id,userID,userName,attempt,time,x,y,action,itemType,imageID,"
                                 + "imageName\n"
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

    return true;
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
    String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());

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
      Uri fileUri = Uri.fromFile(file);

      try (OutputStream outputStream = contentResolver.openOutputStream(fileUri)) {
        if (outputStream != null) {
          outputStream.write((
                                 "id,userID,userName,attempt,time,x,y,action,itemType,imageID,"
                                 + "imageName\n"
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
      requireActivity().runOnUiThread(() -> Toast
          .makeText(getContext(), "No users found to upload data", Toast.LENGTH_LONG)
          .show());
      return;
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
      return;
    }

    ContentResolver contentResolver = requireActivity().getContentResolver();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    AtomicInteger uploadedCount = new AtomicInteger();
    int totalUsers = users.size();
    String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());

    // Export and upload data for each user
    for (User user : users) {
      List<DrawingExportData> drawingDataList = db
          .drawingDataDao()
          .getAllDrawingDataWithUsersAndImagesByUserID(user.id);

      if (drawingDataList.isEmpty()) {
        continue; // Skip users with no data
      }

      String fileName = user.name + "_drawing_data_" + timestamp + ".csv";
      File file = new File(exportDir, fileName);
      Uri fileUri = Uri.fromFile(file);

      try (OutputStream outputStream = contentResolver.openOutputStream(fileUri)) {
        if (outputStream != null) {
          outputStream.write((
                                 "id,userID,userName,attempt,time,x,y,action,itemType,imageID,"
                                 + "imageName\n"
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

          // Upload the file to Firebase
          StorageReference fileRef = storageRef.child("SketchIDData/" + fileName);
          UploadTask uploadTask = fileRef.putFile(fileUri);

          final int currentUser = uploadedCount.get();
          uploadTask.addOnSuccessListener(taskSnapshot -> {
            uploadedCount.getAndIncrement();
            if (uploadedCount.get() == totalUsers) {
              requireActivity().runOnUiThread(() -> Toast
                  .makeText(getContext(), "All data uploaded to Firebase", Toast.LENGTH_LONG)
                  .show());
            }
          }).addOnFailureListener(e -> {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(
                    getContext(),
                    "Failed to upload data for user: " + user.name,
                    Toast.LENGTH_LONG
                )
                .show());
            uploadedCount.getAndIncrement();
          });
        }
      } catch (IOException e) {
        requireActivity().runOnUiThread(() -> Toast
            .makeText(
                getContext(),
                "Failed to export data for user: " + user.name,
                Toast.LENGTH_LONG
            )
            .show());
      }
    }
  }

  private void setupDeleteUserDataPreference() {
    if (deleteUserDataPreference != null) {
      deleteUserDataPreference.setPositiveButtonText("Delete");
      deleteUserDataPreference.setNegativeButtonText("Cancel");
      deleteUserDataPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        executor.execute(() -> {
          int deletedRows = db
              .drawingDataDao()
              .deleteDrawingDataByUserID(Long.parseLong(selectedUserID));
          if (deletedRows > 0) {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(getContext(), "User drawing data deleted", Toast.LENGTH_LONG)
                .show());
            SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(requireContext());
            prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
          } else {
            requireActivity().runOnUiThread(() -> Toast
                .makeText(getContext(), "Failed to delete user drawing data", Toast.LENGTH_LONG)
                .show());
          }
        });
        return false;
      });
      deleteUserDataPreference.setOnPreferenceClickListener(preference -> {
        deleteUserDataPreference.getExtras().putString("value", selectedUserID);
        deleteUserDataPreference.setDialogMessage(String.format(
            "Delete drawing data for user: %s",
            userListPreference.getEntry()
        ));
        Drawable dialogIcon = Objects.requireNonNull(ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        ));
        dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        deleteUserDataPreference.setDialogIcon(dialogIcon);
        return true;
      });
    }
  }

  private void setupClearDataPreference() {
    if (clearDataPreference != null) {
      clearDataPreference.setPositiveButtonText("Delete");
      clearDataPreference.setNegativeButtonText("Cancel");
      clearDataPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        executor.execute(() -> {
          db.drawingDataDao().deleteAllDrawingData();
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
          prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();

          requireActivity().runOnUiThread(() -> Toast
              .makeText(requireContext(), "All drawing data cleared!", Toast.LENGTH_LONG)
              .show());
        });
        return false;
      });

      clearDataPreference.setOnPreferenceClickListener(preference -> {
        clearDataPreference.getExtras().putString("value", "all");
        clearDataPreference.setDialogMessage(
            "Are you sure you want to delete ALL drawing data? This action cannot be undone.");

        Drawable dialogIcon = Objects.requireNonNull(ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        ));
        dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        clearDataPreference.setDialogIcon(dialogIcon);
        return true;
      });
    }
  }

  private void setupSelectImagePreference() {
    if (selectImagePreference != null) {
      selectImagePreference.setOnPreferenceClickListener(preference -> {
        Intent intent = new Intent(requireContext(), ImageSelectionActivity.class);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST);
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

      resetUserProgressPreference.setTitleColor(getResources().getColor(
          R.color.error,
          getContext().getTheme()
      ));

      resetUserProgressPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(() -> {
          db.drawingDataDao().deleteAllDrawingData();
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
          prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();

          requireActivity().runOnUiThread(() -> Toast
              .makeText(requireContext(), "All drawing data cleared!", Toast.LENGTH_LONG)
              .show());
        });
        return true;
      });
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == SELECT_IMAGE_REQUEST
        && resultCode == android.app.Activity.RESULT_OK
        && data != null) {
      int selectedImageId = data.getIntExtra("selected_image_id", -1);
      if (selectedImageId != -1) {
        // Handle the selected image
        Toast
            .makeText(getContext(), "Image selected: " + selectedImageId, Toast.LENGTH_SHORT)
            .show();
      }
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

  private void updatePreferencesVisibility() {
    boolean hasUnfinishedUsers = !unfinishedUsers.isEmpty();

    if (hasUnfinishedUsers) {
      // Show reset progress preference and disable certain categories
      if (resetUserProgressPreference != null) {
        resetUserProgressPreference.setVisible(true);
        resetUserProgressPreference.setTitleColor(getResources().getColor(
            R.color.error,
            getContext().getTheme()
        ));
        setupResetUserProgressDialog();
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

  private void setupResetUserProgressDialog() {
    if (resetUserProgressPreference != null) {
      resetUserProgressPreference.setPositiveButtonText("Reset");
      resetUserProgressPreference.setNegativeButtonText("Cancel");

      // Build list of unfinished user names
      StringBuilder userNamesBuilder = new StringBuilder();
      for (UserProgressManager.UserProgress progress : unfinishedUsers) {
        userNamesBuilder.append("• ").append(progress.getUserName()).append("\n");
      }

      String userNames = userNamesBuilder.toString().trim();

      resetUserProgressPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        executor.execute(() -> {
          // Reset progress for all unfinished users
          for (UserProgressManager.UserProgress progress : unfinishedUsers) {
            long userId = progress.getUserId();
            String sessionId = progress.getSessionId();

            // Get all unfinished sessions for this user
            List<UserProgressManager.Session> unfinishedSessions
                = UserProgressManager.getUnfinishedSessions(requireContext(), userId);

            // Mark all sessions as finished
            for (UserProgressManager.Session session : unfinishedSessions) {
              // Mark the session as finished
              UserProgressManager.markSessionFinished(
                  requireContext(),
                  userId,
                  session.getSessionId()
              );

              // Delete drawing data for this session from the database
              db.drawingDataDao().deleteUserSessionData(userId, session.getSessionId());
            }

            // For backward compatibility: use the legacy session ID if provided
            if (sessionId != null) {
              // Delete only the data for this specific session
              db.drawingDataDao().deleteUserSessionData(userId, sessionId);
            }

            // Clear progress in preferences
            UserProgressManager.clearUserProgress(requireContext(), userId);
          }

          // Refresh the UI
          requireActivity().runOnUiThread(() -> {
            Toast
                .makeText(getContext(), "User progress reset successfully", Toast.LENGTH_LONG)
                .show();
            unfinishedUsers.clear();
            updatePreferencesVisibility();
          });
        });
        return false;
      });

      resetUserProgressPreference.setOnPreferenceClickListener(preference -> {
        resetUserProgressPreference.getExtras().putString("value", "reset");
        String dialogMessage = String.format(
            "Resetting progress will delete only the current "
            + "unfinished session data.%n%n"
            + "The following users have unfinished drawing "
            + "sessions:%n%n%s", userNames
        );
        resetUserProgressPreference.setDialogMessage(dialogMessage);

        Drawable dialogIcon = Objects.requireNonNull(ResourcesCompat.getDrawable(
            getResources(),
            android.R.drawable.ic_dialog_alert,
            null
        ));
        dialogIcon.setTint(getResources().getColor(R.color.error, requireContext().getTheme()));
        resetUserProgressPreference.setDialogIcon(dialogIcon);
        resetUserProgressPreference.setTitleColor(getResources().getColor(
            R.color.error,
            requireContext().getTheme()
        ));
        return true;
      });
    }
  }

  /**
   * Checks for unfinished users during initial load
   * This is separate from the onResume check to prevent UI jumping
   */
  private void checkUnfinishedUsersInitial() {
    unfinishedUsers = UserProgressManager.getUnfinishedUsers(requireContext(), db);

    // Update UI on the main thread
    if (!unfinishedUsers.isEmpty()) {
      requireActivity().runOnUiThread(() -> {
        if (resetUserProgressPreference != null) {
          resetUserProgressPreference.setVisible(true);
          resetUserProgressPreference.setTitleColor(getResources().getColor(
              R.color.error,
              requireContext().getTheme()
          ));
          setupResetUserProgressDialog();
        }

        if (drawingSettingsCategory != null) {
          drawingSettingsCategory.setEnabled(false);
        }

        if (dataManagementCategory != null) {
          dataManagementCategory.setEnabled(false);
        }
      });
    }
  }
}
