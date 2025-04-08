package com.mfrankic.sketchid;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
  private final OnImageClickListener listener;
  private final OnImageOptionsClickListener optionsListener;
  private final OnImageLongClickListener longClickListener;
  private final Set<Image> multiSelectedImages = new HashSet<>();
  private final Map<MaterialCardView, Float> originalElevations = new HashMap<>();
  private List<Image> images;
  private Context context;
  private boolean multiSelectMode = false;
  private View currentDraggableView = null;
  private AnimatorSet currentDraggableAnimation = null;
  private int currentDraggablePosition = -1;
  private Image currentDraggableImage = null;

  public ImageAdapter(
      List<Image> images,
      OnImageClickListener listener,
      OnImageOptionsClickListener optionsListener,
      OnImageLongClickListener longClickListener
  ) {
    this.images = new ArrayList<>(images);
    this.listener = listener;
    this.optionsListener = optionsListener;
    this.longClickListener = longClickListener;
  }

  public void updateImages(List<Image> newImages) {
    this.images = new ArrayList<>(newImages);
    notifyDataSetChanged();
  }

  public boolean isMultiSelectMode() {
    return multiSelectMode;
  }

  public void setMultiSelectMode(boolean multiSelectMode) {
    this.multiSelectMode = multiSelectMode;
    if (!multiSelectMode) {
      multiSelectedImages.clear();
      // Cancel any active draggable animation
      cancelDraggableAnimation();
    }
    notifyDataSetChanged();
  }

  private void cancelDraggableAnimation() {
    if (currentDraggableAnimation != null && currentDraggableAnimation.isRunning()) {
      currentDraggableAnimation.cancel();
    }
    if (currentDraggableView != null) {
      resetCardToNormal(currentDraggableView);
    }
    currentDraggableView = null;
    currentDraggableAnimation = null;
    currentDraggablePosition = -1;
    currentDraggableImage = null;
  }

  private void resetCardToNormal(View view) {
    MaterialCardView cardView = view.findViewById(R.id.cardViewRoot);
    if (cardView == null) return;

    // Restore original elevation or use default if not found
    float originalElevation = originalElevations.getOrDefault(
        cardView,
        3f
    ); // Default from ImageCardViewStyle

    cardView.setCardElevation(originalElevation);
    cardView.setScaleX(1f);
    cardView.setScaleY(1f);
    cardView.setStrokeWidth(0);
    cardView.setStrokeColor(Color.TRANSPARENT);
  }

  public Set<Image> getMultiSelectedImages() {
    return new HashSet<>(multiSelectedImages);
  }

  public void clearSelections() {
    multiSelectedImages.clear();
    notifyDataSetChanged();
  }

  public Image getImageAt(int position) {
    if (position >= 0 && position < images.size()) {
      return images.get(position);
    }
    return null;
  }

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    context = parent.getContext();
    View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
    return new ImageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    Image image = images.get(position);
    holder.imageName.setText(image.name);

    // Apply checkboard pattern background for transparency
    int lightColor = Color.rgb(238, 238, 238); // #EEEEEE
    int darkColor = Color.rgb(204, 204, 204);  // #CCCCCC
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 16);
    holder.imageView.setBackground(checkerboardDrawable);

    // Store the original elevation of the card
    MaterialCardView cardView = holder.itemView.findViewById(R.id.cardViewRoot);
    if (cardView != null) {
      originalElevations.put(cardView, cardView.getCardElevation());
    }

    // Reset any previous styling
    resetCardToNormal(holder.itemView);

    // Apply draggable state if this is the same image that was long-pressed
    if (currentDraggableImage != null && currentDraggableImage.equals(image)) {
      applyDraggableStyling(holder.itemView);
    }

    // Handle visibility of options button based on multi-select mode
    if (multiSelectMode) {
      holder.btnOptions.setVisibility(View.VISIBLE);

      boolean isChecked = multiSelectedImages.contains(image);

      int backgroundColor = CheckboxUtils.applyAlpha(Color.BLACK, 0.5f);
      int borderColor = context.getResources().getColor(R.color.primary, context.getTheme());
      int checkmarkColor = context.getResources().getColor(R.color.primary, context.getTheme());
      CheckboxUtils.styleCustomCheckbox(
          context,
          holder.btnOptions,
          isChecked,
          backgroundColor,
          borderColor,
          checkmarkColor
      );

      // In multi-select mode, clicking the options button toggles selection
      holder.btnOptions.setOnClickListener(v -> {
        toggleImageSelection(image);
      });
    } else {
      // Normal mode - show options button for all images
      holder.btnOptions.setVisibility(View.VISIBLE);
      holder.btnOptions.setImageResource(android.R.drawable.ic_menu_more);
      holder.btnOptions.setOnClickListener(v -> optionsListener.onImageOptionsClick(image));
    }

    // Handle item clicks based on multi-select mode
    holder.itemView.setOnClickListener(v -> {
      if (multiSelectMode) {
        toggleImageSelection(image);
      } else {
        listener.onImageClick(image);
      }
    });

    // Set up long click listener for all images
    holder.itemView.setOnLongClickListener(v -> {
      boolean result = longClickListener.onImageLongClick(image);
      if (result) {
        // Show draggable state animation with position
        showDraggableState(holder.itemView, holder.getAdapterPosition());
      }
      return result;
    });

    if (image.source.equals("default")) {
      // Load default image from resources
      int resourceId = context
          .getResources()
          .getIdentifier(image.name.toLowerCase(), "drawable", context.getPackageName());
      if (resourceId != 0) {
        Glide
            .with(context)
            .load(resourceId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.centerCropTransform())
            .into(holder.imageView);
      } else {
        showErrorImage(holder, "Invalid default image");
      }
    } else {
      // Load custom image from URI
      Uri uri = Uri.parse(image.path);
      try {
        context
            .getContentResolver()
            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Glide
            .with(context)
            .load(uri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.centerCropTransform())
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.imageView);
      } catch (SecurityException e) {
        showErrorImage(holder, "Permission denied");
      } catch (Exception e) {
        showErrorImage(holder, "Invalid image");
      }
    }
  }

  /**
   * Shows a visual effect to indicate the card is ready to be dragged
   *
   * @param view     The view to apply the draggable effect to
   * @param position The position of the item in the adapter
   */
  public void showDraggableState(View view, int position) {
    // Cancel any existing animations
    cancelDraggableAnimation();

    // Store the current image being highlighted
    if (position >= 0 && position < images.size()) {
      currentDraggableImage = images.get(position);
    }

    // Store the current view being animated and its position
    currentDraggableView = view;
    currentDraggablePosition = position;

    // Apply styling
    applyDraggableStyling(view);
  }

  /**
   * Applies draggable styling to a view
   *
   * @param view The view to style
   */
  private void applyDraggableStyling(View view) {
    // Find the card view
    MaterialCardView cardView = view.findViewById(R.id.cardViewRoot);
    if (cardView == null) return;

    // Store original elevation if not already stored
    if (!originalElevations.containsKey(cardView)) {
      originalElevations.put(cardView, cardView.getCardElevation());
    }

    // Provide haptic feedback
    view.performHapticFeedback(
        android.view.HapticFeedbackConstants.LONG_PRESS,
        android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    );

    // Apply elevation change
    cardView.setCardElevation(16f);
    cardView.setScaleX(1.05f);
    cardView.setScaleY(1.05f);
    cardView.setStrokeWidth(3);
    cardView.setStrokeColor(context.getResources().getColor(R.color.secondary, context.getTheme()));
  }

  public boolean toggleImageSelection(Image image) {
    if (multiSelectedImages.contains(image)) {
      multiSelectedImages.remove(image);
      notifyDataSetChanged();
      return false;
    } else {
      multiSelectedImages.add(image);
      notifyDataSetChanged();
      return true;
    }
  }

  private void showErrorImage(ImageViewHolder holder, String errorText) {
    Glide
        .with(context)
        .load(android.R.drawable.ic_menu_gallery)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(RequestOptions.centerCropTransform())
        .into(holder.imageView);
    holder.errorText.setText(errorText);
    holder.errorText.setVisibility(View.VISIBLE);
  }

  @Override
  public int getItemCount() {
    return images.size();
  }

  public interface OnImageClickListener {
    void onImageClick(Image image);
  }

  public interface OnImageOptionsClickListener {
    void onImageOptionsClick(Image image);
  }

  public interface OnImageLongClickListener {
    boolean onImageLongClick(Image image);
  }

  static class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView imageName;
    TextView errorText;
    ImageButton btnOptions;

    ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.imageView);
      imageName = itemView.findViewById(R.id.imageName);
      errorText = itemView.findViewById(R.id.errorText);
      btnOptions = itemView.findViewById(R.id.btnOptions);
    }
  }
} 
