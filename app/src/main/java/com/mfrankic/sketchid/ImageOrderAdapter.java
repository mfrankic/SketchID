package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageOrderAdapter extends RecyclerView.Adapter<ImageOrderAdapter.ImageViewHolder> {
  private final Context context;
  private final ItemTouchHelper itemTouchHelper;
  private final Map<MaterialCardView, Float> originalElevations = new HashMap<>();
  private List<Image> images;
  private OnReorderListener reorderListener;
  private OnImageUnselectListener unselectListener;

  public ImageOrderAdapter(Context context, List<Image> images) {
    this.context = context;
    this.images = images;  // Use the same list reference

    itemTouchHelper = createItemTouchHelper();
  }

  /**
   * Creates and configures the ItemTouchHelper for drag and drop functionality
   */
  private ItemTouchHelper createItemTouchHelper() {
    ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP
                                                                           | ItemTouchHelper.DOWN, 0
    ) {
      @Override
      public boolean onMove(
          @NonNull RecyclerView recyclerView,
          @NonNull RecyclerView.ViewHolder viewHolder,
          @NonNull RecyclerView.ViewHolder target
      ) {
        return handleItemMove(viewHolder, target);
      }

      @Override
      public boolean isLongPressDragEnabled() {
        // Disable long press drag by default - we'll handle it manually
        // This makes the drag behavior more responsive through the drag handle
        return false;
      }

      @Override
      public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // Lower threshold for moving items - makes reordering more responsive
        return 0.25f;
      }

      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used
      }

      @Override
      public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        applyDragEffects(viewHolder, actionState);
      }

      @Override
      public void clearView(
          @NonNull RecyclerView recyclerView,
          @NonNull RecyclerView.ViewHolder viewHolder
      ) {
        super.clearView(recyclerView, viewHolder);
        resetViewAfterDrag(viewHolder);
      }
    };
    return new ItemTouchHelper(callback);
  }

  /**
   * Handles the movement of items in the list during drag and drop
   */
  private boolean handleItemMove(
      RecyclerView.ViewHolder viewHolder,
      RecyclerView.ViewHolder target
  ) {
    int fromPosition = viewHolder.getAdapterPosition();
    int toPosition = target.getAdapterPosition();

    if (fromPosition < 0
        || toPosition < 0
        || fromPosition >= images.size()
        || toPosition >= images.size()) {
      return false;  // Invalid positions
    }

    // Swap items in our internal list
    Collections.swap(images, fromPosition, toPosition);

    // Notify about the move for animation
    notifyItemMoved(fromPosition, toPosition);

    // Important: Notify the activity that ordering has changed 
    // so it can update its master list and save the changes
    if (reorderListener != null) {
      reorderListener.onReorder();
    }
    return true;
  }

  /**
   * Applies visual effects when an item starts being dragged
   */
  private void applyDragEffects(RecyclerView.ViewHolder viewHolder, int actionState) {
    if (viewHolder == null || actionState != ItemTouchHelper.ACTION_STATE_DRAG) {
      return;
    }

    View itemView = viewHolder.itemView;
    MaterialCardView cardView = itemView.findViewById(R.id.cardViewRoot);
    if (cardView == null) {
      return;
    }

    // Store original elevation if not already stored
    if (!originalElevations.containsKey(cardView)) {
      originalElevations.put(cardView, cardView.getCardElevation());
    }

    // Apply visual effect when dragging starts
    cardView.setCardElevation(16f); // Increased elevation
    cardView.setScaleX(1.02f);      // Slight scale up
    cardView.setScaleY(1.02f);
    cardView.setStrokeWidth(3);     // Add a border

    // Use primary color from resources instead of hardcoded value
    int primaryColor = context.getResources().getColor(R.color.primary, context.getTheme());
    cardView.setStrokeColor(primaryColor);

    // Add haptic feedback
    itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
  }

  /**
   * Resets the visual effects after dragging ends
   */
  private void resetViewAfterDrag(RecyclerView.ViewHolder viewHolder) {
    View itemView = viewHolder.itemView;
    MaterialCardView cardView = itemView.findViewById(R.id.cardViewRoot);
    if (cardView == null) {
      return;
    }

    // Restore original elevation or use default if not found
    Float originalElevation = originalElevations.get(cardView);

    if (originalElevation == null) {
      originalElevation = 4f;
    }

    cardView.setCardElevation(originalElevation);
    cardView.setScaleX(1f);
    cardView.setScaleY(1f);
    cardView.setStrokeWidth(0);
    cardView.setStrokeColor(Color.TRANSPARENT);
  }

  public void setOnImageUnselectListener(OnImageUnselectListener listener) {
    this.unselectListener = listener;
  }

  public void setOnReorderListener(OnReorderListener listener) {
    this.reorderListener = listener;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> newImages) {
    if (newImages == null) {
      return;
    }

    // Use DiffUtil to calculate the difference and dispatch minimal updates
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
      @Override
      public int getOldListSize() {
        return images.size();
      }

      @Override
      public int getNewListSize() {
        return newImages.size();
      }

      @Override
      public boolean areItemsTheSame(int oldPosition, int newPosition) {
        return images.get(oldPosition).id == newImages.get(newPosition).id;
      }

      @Override
      public boolean areContentsTheSame(int oldPosition, int newPosition) {
        Image oldImage = images.get(oldPosition);
        Image newImage = newImages.get(newPosition);
        return oldImage.id == newImage.id
               && oldImage.name.equals(newImage.name)
               && oldImage.source.equals(newImage.source)
               && oldImage.path.equals(newImage.path);
      }
    });

    // Update the data - create a completely fresh copy
    this.images = new ArrayList<>();
    this.images.addAll(newImages);

    // Dispatch the updates
    diffResult.dispatchUpdatesTo(this);
  }

  public void attachToRecyclerView(RecyclerView recyclerView) {
    itemTouchHelper.attachToRecyclerView(recyclerView);
  }

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_image_order, parent, false);
    return new ImageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    Image image = images.get(position);
    holder.imageName.setText(image.name);

    // Apply checkerboard pattern background
    applyCheckerboardBackground(holder);

    // Configure card view
    configureCardView(holder);

    // Load appropriate image
    loadImage(holder, image);

    // Configure drag handle
    configureDragHandle(holder);

    // Setup unselect button
    setupUnselectButton(holder, image);
  }

  /**
   * Applies a checkerboard pattern background to the image view
   */
  private void applyCheckerboardBackground(ImageViewHolder holder) {
    int lightColor = Color.rgb(238, 238, 238); // #EEEEEE
    int darkColor = Color.rgb(204, 204, 204);  // #CCCCCC
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 8);
    holder.imageView.setBackground(checkerboardDrawable);
  }

  /**
   * Configures the card view styling
   */
  private void configureCardView(ImageViewHolder holder) {
    MaterialCardView cardView = holder.itemView.findViewById(R.id.cardViewRoot);
    if (cardView == null) {
      return;
    }

    float defaultElevation = 4f;
    cardView.setCardElevation(defaultElevation);
    cardView.setStrokeWidth(0);
    cardView.setStrokeColor(Color.TRANSPARENT);
  }

  /**
   * Loads the appropriate image into the ImageView
   */
  private void loadImage(ImageViewHolder holder, Image image) {
    if (SOURCE_DEFAULT.equals(image.source)) {
      loadDefaultImage(holder, image);
    } else {
      loadCustomImage(holder, image);
    }
  }

  /**
   * Loads a default image from resources
   */
  private void loadDefaultImage(ImageViewHolder holder, Image image) {
    int imageId = ResourceUtils.getDrawableResourceByName(image.name);
    if (imageId != 0) {
      holder.imageView.setImageResource(imageId);
    } else {
      holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
    }
  }

  /**
   * Loads a custom image from URI
   */
  private void loadCustomImage(ImageViewHolder holder, Image image) {
    Uri imageUri = Uri.parse(image.path);
    Glide
        .with(context)
        .load(imageUri)
        .apply(new RequestOptions().centerCrop())
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.imageView);
  }

  /**
   * Configures the drag handle
   */
  private void configureDragHandle(ImageViewHolder holder) {
    // Get a reference to our custom DraggableImageButton
    DraggableImageButton dragHandle = (DraggableImageButton) holder.dragHandle;

    // Set visibility and appearance
    dragHandle.setVisibility(View.VISIBLE);
    dragHandle.setAlpha(1.0f);

    // Set a more prominent tint color for the drag handle icon
    dragHandle.setColorFilter(context.getResources().getColor(R.color.primary, context.getTheme()));

    // Set accessibility description
    dragHandle.setContentDescription("Drag to reorder");

    // Set a drag start listener that will be called when performClick is triggered
    dragHandle.setDragStartListener(view -> itemTouchHelper.startDrag(holder));

    // Also set a direct click listener to start drag immediately on tap
    dragHandle.setOnClickListener(v -> {
      v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
      itemTouchHelper.startDrag(holder);
    });

    // Make the entire card draggable on long press
    holder.itemView.setOnLongClickListener(v -> {
      v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
      itemTouchHelper.startDrag(holder);
      return true;
    });
  }

  /**
   * Sets up the unselect button
   */
  private void setupUnselectButton(ImageViewHolder holder, Image image) {
    final OnImageUnselectListener localUnselectListener = this.unselectListener;
    holder.btnUnselect.setOnClickListener(v -> {
      if (localUnselectListener != null) {
        localUnselectListener.onImageUnselect(image);
      }
    });
  }

  @Override
  public int getItemCount() {
    return images.size();
  }

  public interface OnImageUnselectListener {
    void onImageUnselect(Image image);
  }

  public interface OnReorderListener {
    void onReorder();
  }

  public static class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView imageName;
    View dragHandle;
    ImageButton btnUnselect;

    ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_thumbnail);
      imageName = itemView.findViewById(R.id.imageName);
      dragHandle = itemView.findViewById(R.id.dragHandle);
      btnUnselect = itemView.findViewById(R.id.btnUnselect);

      // Add content descriptions for accessibility
      setupAccessibility();
    }

    /**
     * Setup accessibility properties for the views
     */
    private void setupAccessibility() {
      if (btnUnselect != null) {
        btnUnselect.setContentDescription("Unselect image");
      }
    }
  }
} 
