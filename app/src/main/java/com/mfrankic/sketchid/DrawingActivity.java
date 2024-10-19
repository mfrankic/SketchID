package com.mfrankic.sketchid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DrawingActivity extends AppCompatActivity {

    private CustomDrawingView drawingView;
    private TextView pressureDisplay;
    private ImageView referenceImage;
    private List<Image> images;
    private int currentImageIndex = 0;
    private int selectedUserId;
    private AppDatabase db;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        db = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        // Get userId from intent
        selectedUserId = getIntent().getIntExtra("userId", -1);

        // Validate userId in a background thread
        executor.execute(() -> {
            if (selectedUserId == -1 || db.userDao().getUserById(selectedUserId) == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Invalid user selected. Returning to Home.", Toast.LENGTH_LONG).show();
                    finish(); // Close the activity if userId is invalid
                });
                return; // Stop further execution if user is invalid
            }

            // Continue with UI initialization if userId is valid
            runOnUiThread(this::initializeDrawingActivity);
        });
    }

    // Initialize UI components in a separate method to call only if userId is valid
    private void initializeDrawingActivity() {
        drawingView = findViewById(R.id.drawing_view);
        referenceImage = findViewById(R.id.reference_image);
        Button nextImageButton = findViewById(R.id.btn_next_image);
        Button clearButton = findViewById(R.id.btn_clear);

        // Load images asynchronously
        executor.execute(this::loadImages);

        // Set up the "Next Image" button
        nextImageButton.setOnClickListener(v -> {
            if (images != null && !images.isEmpty()) {
                if (currentImageIndex < images.size() - 1) {
                    currentImageIndex++;
                    loadCurrentImage();
                    drawingView.clearCanvas(); // Clear canvas for the new image

                    if (currentImageIndex == images.size() - 1) {
                        nextImageButton.setText(R.string.finish); // Update button to say "Finish"
                    }
                } else {
                    // Return to HomeActivity when "Finish" is clicked
                    Intent intent = new Intent(DrawingActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Close DrawingActivity
                }
            } else {
                Toast.makeText(this, "No images available.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the "Clear Canvas" button
        clearButton.setOnClickListener(v -> drawingView.clearCanvas());
    }

    private void loadImages() {
        ImageDao imageDao = db.imageDao();
        images = imageDao.getAllImages();  // Load images from the database in a background thread

        runOnUiThread(() -> {
            if (images != null && !images.isEmpty()) {
                loadCurrentImage();
                drawingView.init(
                        db.drawingDataDao(),
                        pressureDisplay,
                        selectedUserId,
                        getCurrentImageId(),
                        isValid -> {
                            if (!isValid) {
                                Toast.makeText(this, "Invalid user or image configuration!", Toast.LENGTH_LONG).show();
                                finish(); // Close activity if validation fails
                            }
                        }
                );
            } else {
                Toast.makeText(this, "No images found. Please add images to draw.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCurrentImage() {
        if (images != null && !images.isEmpty()) {
            String imageSource = images.get(currentImageIndex).imageSource;
            int imageResId = getResources().getIdentifier(imageSource, "drawable", getPackageName());
            referenceImage.setImageResource(imageResId);
        }
    }

    private int getCurrentImageId() {
        if (images != null && !images.isEmpty()) {
            return images.get(currentImageIndex).imageId;
        } else {
            throw new IndexOutOfBoundsException("No images available");
        }
    }
}
