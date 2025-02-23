package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.KEY_CLEAR_DATA;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_ATTEMPT;
import static com.mfrankic.sketchid.Constants.KEY_CURRENT_ITEM_INDEX;
import static com.mfrankic.sketchid.Constants.KEY_DELETE_USER;
import static com.mfrankic.sketchid.Constants.KEY_DELETE_USER_DATA;
import static com.mfrankic.sketchid.Constants.KEY_DRAWING_ATTEMPTS;
import static com.mfrankic.sketchid.Constants.KEY_EXPORT_DATA;
import static com.mfrankic.sketchid.Constants.KEY_NEW_USER;
import static com.mfrankic.sketchid.Constants.KEY_SELECTED_USER;
import static com.mfrankic.sketchid.Constants.KEY_UPLOAD_DATA;

import android.content.ContentResolver;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsFragment extends PreferenceFragmentCompat {

  private EditTextPreference newUserPreference;
  private EditTextPreference drawingAttemptsPreference;
  private Preference exportDataPreference;
  private Preference uploadDataPreference;
  private Preference clearDataPreference;
  private Executor executor;
  private AppDatabase db;
  private ListPreference userListPreference;
  private CustomDialogPreference deleteUserPreference;
  private CustomDialogPreference deleteUserDataPreference;
  private String selectedUserID;
  private Uri fileUri;

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    db = AppDatabase.getInstance(requireContext());
    executor = Executors.newSingleThreadExecutor();

    userListPreference = findPreference(KEY_SELECTED_USER);
    deleteUserPreference = findPreference(KEY_DELETE_USER);
    deleteUserDataPreference = findPreference(KEY_DELETE_USER_DATA);
    newUserPreference = findPreference(KEY_NEW_USER);
    drawingAttemptsPreference = findPreference(KEY_DRAWING_ATTEMPTS);
    exportDataPreference = findPreference(KEY_EXPORT_DATA);
    uploadDataPreference = findPreference(KEY_UPLOAD_DATA);
    clearDataPreference = findPreference(KEY_CLEAR_DATA);

    setupUserListPreference();
    setupNewUserPreference();
    setupDeleteUserPreference();
    setupDrawingAttemptsPreference();
    setupExportDataPreference();
    setupUploadDataPreference();
    setupDeleteUserDataPreference();
    setupClearDataPreference();

    loadUsersIntoListPreference();
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
        userListPreference.setSummary(
            String.format("Current User: %s", userListPreference.getEntries()[idx]));
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
          PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putString(
              KEY_SELECTED_USER, newUserID).apply();
          loadUsersIntoListPreference();
        });
        return false;
      });
    }
  }

  private void setupDeleteUserPreference() {
    if (deleteUserPreference != null) {
      deleteUserPreference.setPositiveButtonText("Delete");
      deleteUserPreference.setNegativeButtonText("Cancel");
      deleteUserPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        executor.execute(() -> {
          int deletedRows = db.userDao().deleteUser(Long.parseLong(selectedUserID));
          if (deletedRows > 0) {
            requireActivity().runOnUiThread(
                () -> Toast.makeText(getContext(), "User deleted", Toast.LENGTH_LONG).show());
            db.drawingDataDao().deleteDrawingDataByUserID(Long.parseLong(selectedUserID));
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().remove(
                KEY_SELECTED_USER).apply();
            loadUsersIntoListPreference();
            userListPreference.callChangeListener("-1");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                requireContext());
            prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
          } else {
            requireActivity().runOnUiThread(
                () -> Toast.makeText(getContext(), "Failed to delete user",
                    Toast.LENGTH_LONG).show());
          }
        });
        return false;
      });
      deleteUserPreference.setOnPreferenceClickListener(preference -> {
        deleteUserPreference.getExtras().putString("value", selectedUserID);
        deleteUserPreference.setDialogMessage(
            String.format("Delete User: %s", userListPreference.getEntry()));
        Drawable dialogIcon = ResourcesCompat.getDrawable(getResources(),
            android.R.drawable.ic_dialog_alert, null);
        assert dialogIcon != null;
        dialogIcon.setTint(android.graphics.Color.RED);
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
      editText.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
        try {
          int input = Integer.parseInt(
              dest.toString().substring(0, dstart) + source + dest.toString().substring(dend));
          if (input < 1 || input > 999 || (source.equals("0") && dstart == 0)) {
            throw new NumberFormatException();
          }
        } catch (NumberFormatException e) {
          return "";
        }
        return null;
      }});
    });

    drawingAttemptsPreference.setSummaryProvider(preference -> {
      String attempts = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(
          KEY_DRAWING_ATTEMPTS, "-1");
      return String.format("Current: %s", !attempts.equals("-1") ? attempts : "Not set");
    });

    drawingAttemptsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
      String value = newValue.toString().isBlank() ? "-1" : newValue.toString();
      int attempts = Integer.parseInt(value);
      if (attempts < 1) {
        Toast.makeText(getContext(), "Number of attempts per drawing must be at least 1",
            Toast.LENGTH_LONG).show();
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

  private void setupUploadDataPreference() {
    if (uploadDataPreference != null) {
      uploadDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::uploadDataToFirebase);
        return true;
      });
    }
  }

  private void setupDeleteUserDataPreference() {
    if (deleteUserDataPreference != null) {
      deleteUserDataPreference.setPositiveButtonText("Delete");
      deleteUserDataPreference.setNegativeButtonText("Cancel");
      deleteUserDataPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        executor.execute(() -> {
          int deletedRows = db.drawingDataDao().deleteDrawingDataByUserID(
              Long.parseLong(selectedUserID));
          if (deletedRows > 0) {
            requireActivity().runOnUiThread(
                () -> Toast.makeText(getContext(), "User drawing data deleted",
                    Toast.LENGTH_LONG).show());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                requireContext());
            prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
          } else {
            requireActivity().runOnUiThread(
                () -> Toast.makeText(getContext(), "Failed to delete user drawing data",
                    Toast.LENGTH_LONG).show());
          }
        });
        return false;
      });
      deleteUserDataPreference.setOnPreferenceClickListener(preference -> {
        deleteUserDataPreference.getExtras().putString("value", selectedUserID);
        deleteUserDataPreference.setDialogMessage(
            String.format("Delete drawing data for user: %s", userListPreference.getEntry()));
        Drawable dialogIcon = ResourcesCompat.getDrawable(getResources(),
            android.R.drawable.ic_dialog_alert, null);
        assert dialogIcon != null;
        dialogIcon.setTint(android.graphics.Color.RED);
        deleteUserDataPreference.setDialogIcon(dialogIcon);
        return true;
      });
    }
  }

  private void setupClearDataPreference() {
    if (clearDataPreference != null) {
      clearDataPreference.setOnPreferenceClickListener(preference -> {
        executor.execute(this::clearAllData);
        return true;
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
          return;
        }

        selectedUserID = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(
            KEY_SELECTED_USER, "-1");

        if (userListPreference.findIndexOfValue(selectedUserID) == -1) {
          selectedUserID = "-1";
          userListPreference.setSummary("Select user from the list");
          deleteUserPreference.setEnabled(false);
        }

        if (!selectedUserID.equals("-1")) {
          userListPreference.setValue(selectedUserID);
          userListPreference.callChangeListener(selectedUserID);
          deleteUserPreference.setEnabled(true);
        }

        userListPreference.setEnabled(true);
      });
    });
  }

  private void exportDataToCSV() {
    exportDataToCSV(false);
  }

  private boolean exportDataToCSV(boolean silent) {
    List<DrawingExportData> drawingDataList =
        db.drawingDataDao().getAllDrawingDataWithUsersAndImagesByUserID(
            Long.parseLong(selectedUserID));
    String fileName = userListPreference.getEntry() + "_drawing_data.csv";
    ContentResolver contentResolver = requireActivity().getContentResolver();

    File exportDir = new File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SketchIDData");
    if (!exportDir.exists() && !exportDir.mkdirs()) {
      requireActivity().runOnUiThread(
          () -> Toast.makeText(getContext(), "Failed to create export directory",
              Toast.LENGTH_LONG).show());
      return false;
    }

    File file = new File(exportDir, fileName);
    fileUri = Uri.fromFile(file);

    if (fileUri != null) {
      try (OutputStream outputStream = contentResolver.openOutputStream(fileUri)) {
        if (outputStream != null) {
          outputStream.write(
              ("id,userID,userName,attempt,time,x,y,action,itemType,imageID,imageName\n").getBytes());
          for (DrawingExportData data : drawingDataList) {
            String row = String.format(Locale.getDefault(), "%d,%d,%s,%d,%d,%s,%s,%s,%s,%d,%s%n",
                data.getId(), data.getUserID(), data.getUserName(), data.getAttempt(),
                data.getTime(), data.getX(), data.getY(), data.getAction(), data.getItemType(),
                data.getImageID(), data.getImageName());
            outputStream.write(row.getBytes());
          }
          if (!silent) {
            requireActivity().runOnUiThread(
                () -> Toast.makeText(getContext(), "Data exported to Downloads/SketchIDData",
                    Toast.LENGTH_LONG).show());
          }
        }
      } catch (IOException e) {
        requireActivity().runOnUiThread(
            () -> Toast.makeText(getContext(), "Failed to export data", Toast.LENGTH_LONG).show());
        return false;
      }
    }

    return true;
  }

  private void uploadDataToFirebase() {
    boolean success = exportDataToCSV(true);
    if (!success) {
      return;
    }
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    String fileName = userListPreference.getEntry() + "_drawing_data.csv";
    StorageReference fileRef = storageRef.child("SketchIDData/" + fileName);

    UploadTask uploadTask = fileRef.putFile(fileUri);
    uploadTask.addOnSuccessListener(taskSnapshot -> requireActivity().runOnUiThread(
        () -> Toast.makeText(getContext(), "Data uploaded to Firebase",
            Toast.LENGTH_LONG).show())).addOnFailureListener(e -> requireActivity().runOnUiThread(
        () -> Toast.makeText(getContext(), "Failed to upload data to Firebase",
            Toast.LENGTH_LONG).show()));

    fileUri = null;
  }

  private void clearAllData() {
    executor.execute(() -> {
      db.drawingDataDao().deleteAllDrawingData();
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
      prefs.edit().remove(KEY_CURRENT_ITEM_ATTEMPT).remove(KEY_CURRENT_ITEM_INDEX).apply();
      requireActivity().runOnUiThread(
          () -> Toast.makeText(requireContext(), "All data cleared!", Toast.LENGTH_LONG).show());
    });
  }
}