package com.mfrankic.sketchid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private Spinner userSpinner;
    private EditText newUserInput;
    private AppDatabase db;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userSpinner = findViewById(R.id.user_spinner);
        newUserInput = findViewById(R.id.edit_new_user);
        Button createUserButton = findViewById(R.id.btn_create_user);
        Button exportDataButton = findViewById(R.id.btn_export_data);
        Button backToHomeButton = findViewById(R.id.btn_back_to_home);

        db = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();  // Executor for background threads

        // Load all users into the spinner asynchronously
        loadUsersIntoSpinner();

        // Create new user when button clicked
        createUserButton.setOnClickListener(v -> {
            String newUserName = newUserInput.getText().toString();
            if (!newUserName.isEmpty()) {
                // Run database insertion in background thread
                executor.execute(() -> {
                    User newUser = new User(newUserName);
                    db.userDao().insertUser(newUser);
                    // Once user is added, update the spinner on the main thread
                    runOnUiThread(this::loadUsersIntoSpinner);
                });
            }
        });

        exportDataButton.setOnClickListener(v -> executor.execute(this::exportDataToCSV));

        // Go back to HomeActivity when "Back to Home" button is clicked
        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);

            // Retrieve selected user ID, if any
            int selectedUserId = getSelectedUserId(); // returns -1 if no user is selected
            intent.putExtra("userId", selectedUserId); // Pass userId to HomeActivity

            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);

                // Retrieve selected user ID, if any
                int selectedUserId = getSelectedUserId(); // returns -1 if no user is selected
                intent.putExtra("userId", selectedUserId); // Pass userId to HomeActivity

                startActivity(intent);
            }
        });
    }

    private void loadUsersIntoSpinner() {
        executor.execute(() -> {
            List<User> users = db.userDao().getAllUsers();
            // Updating the UI must be done on the main thread
            runOnUiThread(() -> {
                ArrayAdapter<User> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, users);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSpinner.setAdapter(adapter);
            });
        });
    }

    public int getSelectedUserId() {
        User selectedUser = (User) userSpinner.getSelectedItem();

        if (selectedUser != null) {
            return selectedUser.userId;
        }

        return -1; // Return -1 if no user is selected
    }

    private void exportDataToCSV() {
        List<DrawingData> drawingDataList = db.drawingDataDao().getAllDrawingData();

        String fileName = "drawing_data.csv";
        ContentResolver contentResolver = getContentResolver();
        Uri fileUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore to save in the Downloads directory for Android 10 and above
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SketchIDData");
            fileUri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        } else {
            // Fallback for Android 9 and below
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SketchIDData");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, fileName);
            fileUri = Uri.fromFile(file);
        }

        if (fileUri != null) {
            try (OutputStream outputStream = contentResolver.openOutputStream(fileUri)) {
                if (outputStream != null) {
                    outputStream.write("ID,Timestamp,X,Y,Pressure,UserID,ImageID\n".getBytes());
                    for (DrawingData data : drawingDataList) {
                        String row = data.id + "," +
                                data.timestamp + "," +
                                data.x + "," +
                                data.y + "," +
                                data.pressure + "," +
                                data.userId + "," +
                                data.imageId + "\n";
                        outputStream.write(row.getBytes());
                    }
                    runOnUiThread(() -> Toast.makeText(this, "Data exported to Downloads/SketchIDData", Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to export data", Toast.LENGTH_LONG).show());
            }
        }
    }
}
