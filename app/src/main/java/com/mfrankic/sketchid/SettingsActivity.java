package com.mfrankic.sketchid;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SketchIDPrefs";
    private static final String KEY_SELECTED_USER_ID = "selected_user_id";
    private static final String KEY_CURRENT_ITEM_INDEX = "current_item_index";
    private static final String KEY_CURRENT_ITEM_ATTEMPT = "current_item_attempt";
    private Spinner userSpinner;
    private EditText newUserInput;
    private User selectedUser;
    private Uri fileUri;
    private AppDatabase db;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        userSpinner = findViewById(R.id.user_spinner);
        newUserInput = findViewById(R.id.edit_new_user);
        Button createUserButton = findViewById(R.id.btn_create_user);
        Button exportDataButton = findViewById(R.id.btn_export_data);
        Button clearDataButton = findViewById(R.id.btn_clear_data);
        Button uploadDataButton = findViewById(R.id.btn_upload_data);

        uploadDataButton.setOnClickListener(v -> executor.execute(this::uploadDataToFirebase));

        loadUsersIntoSpinner();

        createUserButton.setOnClickListener(v -> {
            String newUserName = newUserInput.getText().toString();
            if (!newUserName.isEmpty()) {
                executor.execute(() -> {
                    User newUser = new User(newUserName);
                    db.userDao().insertUser(newUser);
                    saveSelectedUserID(newUser.id);
                    runOnUiThread(this::loadUsersIntoSpinner);
                });
            }
        });

        exportDataButton.setOnClickListener(v -> executor.execute(this::exportDataToCSV));

        clearDataButton.setOnClickListener(v -> new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Delete Drawing Data")
                .setMessage("Are you sure you want to delete all drawing data from database?")
                .setPositiveButton(
                        "Yes",
                        (dialog, which) -> executor.execute(() -> {
                            db.drawingDataDao().deleteAllDrawingData();
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                    .remove(KEY_CURRENT_ITEM_INDEX)
                                    .remove(KEY_CURRENT_ITEM_ATTEMPT)
                                    .apply();
                            runOnUiThread(() -> Toast.makeText(this, "Drawing data cleared", Toast.LENGTH_SHORT).show());
                        }))
                .setNegativeButton("No", null)
                .show());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                selectedUser = (User) userSpinner.getSelectedItem();
                if (selectedUser != null) {
                    saveSelectedUserID(selectedUser.id);
                }
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSelectedUserID(int userId) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putInt(KEY_SELECTED_USER_ID, userId)
                .apply();
    }

    private int getSelectedUserID() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_SELECTED_USER_ID, -1);
    }

    private void loadUsersIntoSpinner() {
        executor.execute(() -> {
            List<User> users = db.userDao().getAllUsers();

            if (getSelectedUserID() != -1) {
                selectedUser = users.stream()
                        .filter(user -> user.id == getSelectedUserID())
                        .findFirst()
                        .orElse(null);
            }

            runOnUiThread(() -> {
                ArrayAdapter<User> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, users);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSpinner.setAdapter(adapter);

                if (!users.isEmpty()) {
                    int selectedUserPosition = adapter.getPosition(selectedUser != null ? selectedUser : users.get(0));
                    userSpinner.setSelection(selectedUserPosition);
                }

                userSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedUser = (User) parent.getItemAtPosition(position);
                        saveSelectedUserID(selectedUser.id);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            });
        });
    }

    private void exportDataToCSV() {
        List<DrawingExportData> drawingDataList = db.drawingDataDao()
                .getAllDrawingDataWithUsersAndImagesByUserID(selectedUser.id);

        String fileName = selectedUser.name + "_drawing_data.csv";
        ContentResolver contentResolver = getContentResolver();

        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SketchIDData");
        if (!exportDir.exists()) {
            boolean created = exportDir.mkdirs();
            if (!created) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to create export directory", Toast.LENGTH_LONG).show());
                return;
            }
        }

        File file = new File(exportDir, fileName);
        fileUri = Uri.fromFile(file);

        if (fileUri != null) {
            try (OutputStream outputStream = contentResolver.openOutputStream(fileUri)) {
                if (outputStream != null) {
                    outputStream.write("id,userID,userName,attempt,time,x,y,action,itemType,imageID,imageName\n".getBytes());
                    for (DrawingExportData data : drawingDataList) {
                        String row = data.id + "," +
                                data.userID + "," +
                                data.userName + "," +
                                data.attempt + "," +
                                data.time + "," +
                                data.x + "," +
                                data.y + "," +
                                data.action + "," +
                                data.itemType + "," +
                                data.imageID + "," +
                                data.imageName + "\n";
                        outputStream.write(row.getBytes());
                    }
                    runOnUiThread(() -> Toast.makeText(this, "Data exported to Downloads/SketchIDData", Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                Log.d("SettingsActivity", "Failed to export data", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to export data", Toast.LENGTH_LONG).show());
            }
        }
    }

    private void uploadDataToFirebase() {
        exportDataToCSV();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String fileName = selectedUser.name + "_drawing_data.csv";
        StorageReference fileRef = storageRef.child("SketchIDData/" + fileName);

        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask
                .addOnSuccessListener(taskSnapshot -> runOnUiThread(
                        () -> Toast.makeText(this, "Data uploaded to Firebase", Toast.LENGTH_LONG).show()
                ))
                .addOnFailureListener(e -> runOnUiThread(
                        () -> Toast.makeText(this, "Failed to upload data to Firebase", Toast.LENGTH_LONG).show()
                ));

        fileUri = null;
    }

}
