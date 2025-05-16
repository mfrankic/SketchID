package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;

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
import androidx.recyclerview.widget.DiffUtil;
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

    // Update the data
    this.images = new ArrayList<>(newImages);

    // Dispatch the updates
    diffResult.dispatchUpdatesTo(this);
  }

  public void setMultiSelectMode(boolean multiSelectMode) {
    this.multiSelectMode = multiSelectMode;
    if (!multiSelectMode) {
      multiSelectedImages.clear();
    }
    int itemCount = getItemCount();
    if (itemCount > 0) {
      notifyItemRangeChanged(0, itemCount);
    }
  }

  public Set<Image> getMultiSelectedImages() {
    return new HashSet<>(multiSelectedImages);
  }

  public void clearSelections() {
    if (multiSelectedImages.isEmpty()) return;

    multiSelectedImages.clear();
    for (int i = 0; i < images.size(); i++) {
      notifyItemChanged(i);
    }
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

    setupImageBackground(holder);
    storeOriginalCardElevation(holder.itemView);
    resetCardToNormal(holder.itemView);
    setupOptionsButton(holder, image);
    setupClickListeners(holder, image);
    loadImageIntoView(holder, image);
  }

  private void resetCardToNormal(View view) {
    MaterialCardView cardView = view.findViewById(R.id.cardViewRoot);
    if (cardView == null) return;

    Float originalElevation = originalElevations.get(cardView);

    if (originalElevation == null) {
      originalElevation = 3f;
    }

    cardView.setCardElevation(originalElevation);
    cardView.setScaleX(1f);
    cardView.setScaleY(1f);
    cardView.setStrokeWidth(0);
    cardView.setStrokeColor(Color.TRANSPARENT);
  }

  private void setupImageBackground(ImageViewHolder holder) {
    int lightColor = Color.rgb(238, 238, 238);
    int darkColor = Color.rgb(204, 204, 204);
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 16);
    holder.imageView.setBackground(checkerboardDrawable);
  }

  private void storeOriginalCardElevation(View itemView) {
    MaterialCardView cardView = itemView.findViewById(R.id.cardViewRoot);
    if (cardView != null) {
      originalElevations.put(cardView, cardView.getCardElevation());
    }
  }

  private void setupOptionsButton(ImageViewHolder holder, Image image) {
    if (multiSelectMode) {
      setupMultiSelectCheckbox(holder, image);
    } else {
      setupNormalOptionsButton(holder, image);
    }
  }

  private void setupMultiSelectCheckbox(ImageViewHolder holder, Image image) {
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

    holder.btnOptions.setOnClickListener(v -> toggleImageSelection(image));
  }

  public void toggleImageSelection(Image image) {
    int position = images.indexOf(image);
    if (position < 0) {
      return;
    }

    boolean wasSelected = multiSelectedImages.contains(image);

    if (wasSelected) {
      multiSelectedImages.remove(image);
    } else {
      multiSelectedImages.add(image);
    }

    // Notify about this specific item changing
    notifyItemChanged(position);
  }

  private void setupNormalOptionsButton(ImageViewHolder holder, Image image) {
    holder.btnOptions.setVisibility(View.VISIBLE);
    holder.btnOptions.setImageResource(android.R.drawable.ic_menu_more);
    holder.btnOptions.setOnClickListener(v -> optionsListener.onImageOptionsClick(image));
  }

  private void setupClickListeners(ImageViewHolder holder, Image image) {
    // Setup click listener
    holder.itemView.setOnClickListener(v -> {
      if (multiSelectMode) {
        toggleImageSelection(image);
      } else {
        listener.onImageClick(image);
      }
    });

    // Setup long-press listener
    holder.itemView.setOnLongClickListener(v -> longClickListener.onImageLongClick(image));
  }

  private void loadImageIntoView(ImageViewHolder holder, Image image) {
    if (image.source.equals(SOURCE_DEFAULT)) {
      loadDefaultImage(holder, image);
    } else {
      loadCustomImage(holder, image);
    }
  }

  private void loadDefaultImage(ImageViewHolder holder, Image image) {
    int resourceId = ResourceUtils.getDrawableResourceByName(image.name);
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

  private void loadCustomImage(ImageViewHolder holder, Image image) {
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

  @Override
  public int getItemCount() {
    return images.size();
  }

  /**
   * Get IDs of all selected images
   */
  public List<Integer> getSelectedImageIds() {
    List<Integer> ids = new ArrayList<>();
    for (Image image : multiSelectedImages) {
      ids.add(image.id);
    }
    return ids;
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

  public static class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView imageName;
    TextView errorText;
    ImageButton btnOptions;

    ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_thumbnail);
      imageName = itemView.findViewById(R.id.imageName);
      errorText = itemView.findViewById(R.id.errorText);
      btnOptions = itemView.findViewById(R.id.btnOptions);
    }
  }
} 
