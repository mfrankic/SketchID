package com.mfrankic.sketchid;

import android.content.Context;
import android.graphics.Color;
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
  private final CheckerboardDrawable checkerboardDrawable;
  private List<Image> images;
  private OnReorderListener reorderListener;
  private OnImageUnselectListener unselectListener;

  public ImageOrderAdapter(Context context, List<Image> images) {
    this.context = context;
    this.images = images;

    itemTouchHelper = createItemTouchHelper();

    int lightColor = Color.rgb(238, 238, 238);
    int darkColor = Color.rgb(204, 204, 204);
    checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 8);
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

        return false;
      }

      @Override
      public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {

        return 0.25f;
      }

      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // No swipe actions needed
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
      return false;
    }

    Collections.swap(images, fromPosition, toPosition);

    notifyItemMoved(fromPosition, toPosition);

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

    if (!originalElevations.containsKey(cardView)) {
      originalElevations.put(cardView, cardView.getCardElevation());
    }

    cardView.setCardElevation(16f);
    cardView.setScaleX(1.02f);
    cardView.setScaleY(1.02f);
    cardView.setStrokeWidth(3);

    int primaryColor = context.getResources().getColor(R.color.primary, context.getTheme());
    cardView.setStrokeColor(primaryColor);

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

    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
        new DiffUtil.Callback() {
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
        }, true
    );

    this.images = new ArrayList<>();
    this.images.addAll(newImages);

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

    applyCheckerboardBackground(holder);

    configureCardView(holder);

    loadImage(holder, image);

    configureDragHandle(holder);

    setupUnselectButton(holder, image);
  }

  /**
   * Applies a checkerboard pattern background to the image view
   */
  private void applyCheckerboardBackground(ImageViewHolder holder) {

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

    ImageLoader.load(holder.imageView, null, image, context);
  }

  /**
   * Configures the drag handle
   */
  private void configureDragHandle(ImageViewHolder holder) {

    DraggableImageButton dragHandle = (DraggableImageButton) holder.dragHandle;

    dragHandle.setVisibility(View.VISIBLE);
    dragHandle.setAlpha(1.0f);

    dragHandle.setColorFilter(context.getResources().getColor(R.color.primary, context.getTheme()));

    dragHandle.setContentDescription("Drag to reorder");

    dragHandle.setDragStartListener(view -> itemTouchHelper.startDrag(holder));

    dragHandle.setOnClickListener(v -> {
      v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
      itemTouchHelper.startDrag(holder);
    });

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
