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
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageAdapter extends ListAdapter<Image, ImageAdapter.ImageViewHolder> {
  private final OnImageClickListener listener;
  private final OnImageOptionsClickListener optionsListener;
  private final OnImageLongClickListener longClickListener;
  private final Map<MaterialCardView, Float> originalElevations = new HashMap<>();
  private final SelectionManager<Image> selectionManager;
  private Context context;
  private boolean multiSelectMode = false;

  public ImageAdapter(
      List<Image> images,
      OnImageClickListener listener,
      OnImageOptionsClickListener optionsListener,
      OnImageLongClickListener longClickListener
  ) {
    super(new ImageDiffCallback());
    this.listener = listener;
    this.optionsListener = optionsListener;
    this.longClickListener = longClickListener;

    this.selectionManager = new SelectionManager<>(pos -> {
      if (pos >= 0) {
        notifyItemChanged(pos);
      } else {

        int count = getCurrentList().size();
        if (count > 0) {
          notifyItemRangeChanged(0, count);
        }
      }
    });

    submitList(new ArrayList<>(images));
  }

  public void updateImages(List<Image> newImages) {

    selectionManager.validateSelectionsAgainst(newImages);

    submitList(new ArrayList<>(newImages));
  }

  public void setMultiSelectMode(boolean multiSelectMode) {
    this.multiSelectMode = multiSelectMode;
    selectionManager.setEnabled(multiSelectMode);
  }

  public Set<Image> getMultiSelectedImages() {
    return selectionManager.getSelected();
  }

  public void clearSelections() {
    selectionManager.clearSelections();
  }

  public Image getImageAt(int position) {
    if (position >= 0 && position < getItemCount()) {
      return getItem(position);
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
    Image image = getItem(position);
    holder.imageName.setText(image.name);

    setupImageBackground(holder);
    storeOriginalCardElevation(holder.itemView);
    resetCardToNormal(holder.itemView);
    setupOptionsButton(holder, image);
    setupClickListeners(holder, image);

    ImageLoader.load(holder.imageView, holder.errorText, image, context);
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
    boolean isChecked = selectionManager.isSelected(image);

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
    List<Image> currentList = getCurrentList();
    selectionManager.toggle(image, currentList);
  }

  private void setupNormalOptionsButton(ImageViewHolder holder, Image image) {
    holder.btnOptions.setVisibility(View.VISIBLE);
    holder.btnOptions.setImageResource(android.R.drawable.ic_menu_more);
    holder.btnOptions.setOnClickListener(v -> optionsListener.onImageOptionsClick(image));
  }

  private void setupClickListeners(ImageViewHolder holder, Image image) {

    holder.itemView.setOnClickListener(v -> {
      if (multiSelectMode) {
        toggleImageSelection(image);
      } else {
        listener.onImageClick(image);
      }
    });

    holder.itemView.setOnLongClickListener(v -> longClickListener.onImageLongClick(image));
  }

  /**
   * Get IDs of all selected images
   */
  public List<Integer> getSelectedImageIds() {
    List<Integer> ids = new ArrayList<>();
    Set<Image> selectedImages = selectionManager.getSelected();
    for (Image image : selectedImages) {
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
    final ImageView imageView;
    final TextView imageName;
    final TextView errorText;
    final ImageButton btnOptions;

    ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_thumbnail);
      imageName = itemView.findViewById(R.id.imageName);
      errorText = itemView.findViewById(R.id.errorText);
      btnOptions = itemView.findViewById(R.id.btnOptions);
    }
  }
} 
