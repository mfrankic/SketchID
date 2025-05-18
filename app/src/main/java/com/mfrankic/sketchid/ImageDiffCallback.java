package com.mfrankic.sketchid;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

/**
 * Diff callback for Image objects to be used with ListAdapter
 * Efficiently calculates changes between lists of images
 */
public class ImageDiffCallback extends DiffUtil.ItemCallback<Image> {

  @Override
  public boolean areItemsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
    return oldItem.id == newItem.id;
  }

  @Override
  public boolean areContentsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
    return oldItem.id == newItem.id && oldItem.name.equals(newItem.name) && oldItem.source.equals(
        newItem.source) && oldItem.path.equals(newItem.path);
  }
} 
