package com.mfrankic.sketchid;

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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageOrderAdapter extends RecyclerView.Adapter<ImageOrderAdapter.ImageViewHolder> {
  private final Context context;
  private final ItemTouchHelper itemTouchHelper;
  private final Map<MaterialCardView, Float> originalElevations = new HashMap<>();
  private List<Image> images;
  private OnImageUnselectListener unselectListener;
  private OnReorderListener reorderListener;
  private boolean dragEnabled = true;
  private RecyclerView recyclerViewInstance; // Store reference to recyclerView

  public ImageOrderAdapter(Context context, List<Image> images) {
    this.context = context;
    this.images = images;  // Use the same list reference

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
        // Don't allow movement if drag is disabled
        if (!dragEnabled) return false;

        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        Collections.swap(ImageOrderAdapter.this.images, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (reorderListener != null) {
          reorderListener.onReorder();
        }
        return true;
      }

      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used
      }

      @Override
      public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        if (viewHolder == null) return;

        // Apply enhanced visual effect when being dragged
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
          // Don't proceed if drag is disabled
          if (!dragEnabled) return;

          View itemView = viewHolder.itemView;

          // Provide haptic feedback when drag starts
          itemView.performHapticFeedback(
              android.view.HapticFeedbackConstants.LONG_PRESS,
              android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
          );

          MaterialCardView cardView = itemView.findViewById(R.id.cardViewRoot);
          if (cardView != null) {
            // Store the original elevation before changing it
            if (!originalElevations.containsKey(cardView)) {
              originalElevations.put(cardView, cardView.getCardElevation());
            }

            // Apply visual effect to indicate dragging state
            cardView.setCardElevation(16f);
            cardView.setScaleX(1.05f);
            cardView.setScaleY(1.05f);
            cardView.setStrokeWidth(3);
            cardView.setStrokeColor(context
                                        .getResources()
                                        .getColor(R.color.secondary, context.getTheme()));
          }
        }
      }

      @Override
      public void clearView(
          @NonNull RecyclerView recyclerView,
          @NonNull RecyclerView.ViewHolder viewHolder
      ) {
        super.clearView(recyclerView, viewHolder);

        // Reset visual effect when dragging ends
        View itemView = viewHolder.itemView;
        MaterialCardView cardView = itemView.findViewById(R.id.cardViewRoot);
        if (cardView != null) {
          // Restore original elevation or use default if not found
          float originalElevation = originalElevations.containsKey(cardView)
                                    ? originalElevations.get(cardView)
                                    : 4f; // Default elevation from layout

          cardView.setCardElevation(originalElevation);
          cardView.setScaleX(1f);
          cardView.setScaleY(1f);
          cardView.setStrokeWidth(0);
          cardView.setStrokeColor(Color.TRANSPARENT);
        }
      }
    };
    itemTouchHelper = new ItemTouchHelper(callback);
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

  public void setImages(List<Image> images) {
    this.images = images;
    notifyDataSetChanged();
  }

  public void attachToRecyclerView(RecyclerView recyclerView) {
    this.recyclerViewInstance = recyclerView; // Store reference
    if (dragEnabled) {
      itemTouchHelper.attachToRecyclerView(recyclerView);
    }
  }

  /**
   * Enable or disable drag functionality
   *
   * @param enabled Whether drag functionality should be enabled
   */
  public void setDragEnabled(boolean enabled) {
    if (this.dragEnabled == enabled) return; // No change

    this.dragEnabled = enabled;

    // Detach/reattach ItemTouchHelper based on drag state
    if (!enabled) {
      itemTouchHelper.attachToRecyclerView(null); // Detach to disable all drag functionality
    } else {
      // Only attach if not already attached
      itemTouchHelper.attachToRecyclerView(recyclerViewInstance);
    }

    // Force complete refresh to update elevation and visual state
    notifyDataSetChanged();
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

    // Apply checkboard pattern background for transparency
    int lightColor = Color.rgb(238, 238, 238); // #EEEEEE
    int darkColor = Color.rgb(204, 204, 204);  // #CCCCCC
    CheckerboardDrawable checkerboardDrawable = new CheckerboardDrawable(lightColor, darkColor, 8);
    holder.imageView.setBackground(checkerboardDrawable);

    // Store the original elevation when binding
    MaterialCardView cardView = holder.itemView.findViewById(R.id.cardViewRoot);
    if (cardView != null) {
      // Default elevation as defined in the XML layout
      float defaultElevation = 4f;

      // Apply visual styling based on drag enabled state
      if (!dragEnabled) {
        // Apply a subtle disabled appearance to the card without border
        cardView.setStrokeWidth(0);
        cardView.setStrokeColor(Color.TRANSPARENT);
        cardView.setCardElevation(0.5f); // Very flat elevation for disabled state

        // Disable long press effects on the entire card
        holder.itemView.setOnLongClickListener(v -> true); // Consume the event
      } else {
        // Normal appearance with standard elevation
        cardView.setStrokeWidth(0);
        cardView.setStrokeColor(Color.TRANSPARENT);
        cardView.setCardElevation(defaultElevation); // Use the XML-defined elevation

        // Allow normal long press behavior
        holder.itemView.setOnLongClickListener(null);
      }
    }

    if (image.source.equals("default")) {
      int resourceId = context
          .getResources()
          .getIdentifier(image.name.toLowerCase(), "drawable", context.getPackageName());

      // If not found by name, try to use the path which should contain the resource ID
      if (resourceId == 0 && image.path != null && !image.path.isEmpty()) {
        try {
          resourceId = Integer.parseInt(image.path);
        } catch (NumberFormatException e) {
          // Path is not a valid resource ID
          resourceId = 0;
        }
      }

      if (resourceId != 0) {
        Glide
            .with(context)
            .load(resourceId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.centerCropTransform())
            .into(holder.imageView);
      } else {
        // Show error place holder if no valid resource found
        holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
      }
    } else {
      Uri uri = Uri.parse(image.path);
      Glide
          .with(context)
          .load(uri)
          .transition(DrawableTransitionOptions.withCrossFade())
          .apply(RequestOptions.centerCropTransform())
          .into(holder.imageView);
    }

    // Set drag handle visibility and functionality based on dragEnabled state
    if (dragEnabled) {
      holder.dragHandle.setVisibility(View.VISIBLE);
      holder.dragHandle.setAlpha(1.0f);
      holder.dragHandle.setOnTouchListener((v, event) -> {
        if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
          itemTouchHelper.startDrag(holder);
        }
        return false;
      });
    } else {
      // When drag is disabled, either hide the drag handle or show it as disabled
      holder.dragHandle.setVisibility(View.VISIBLE);
      holder.dragHandle.setAlpha(0.3f); // Show as disabled
      holder.dragHandle.setOnTouchListener(null); // Remove the drag functionality
    }

    holder.btnUnselect.setOnClickListener(v -> {
      if (unselectListener != null) {
        unselectListener.onImageUnselect(image);
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

  static class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView imageName;
    ImageButton dragHandle;
    ImageButton btnUnselect;

    ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.imageView);
      imageName = itemView.findViewById(R.id.imageName);
      dragHandle = itemView.findViewById(R.id.dragHandle);
      btnUnselect = itemView.findViewById(R.id.btnUnselect);
    }
  }
} 
