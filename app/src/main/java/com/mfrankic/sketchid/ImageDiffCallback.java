package com.mfrankic.sketchid;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

/**
 * A {@link DiffUtil.ItemCallback} for {@link Image} objects.
 * Used with {@link androidx.recyclerview.widget.ListAdapter} to efficiently calculate
 * differences between old and new lists of images, enabling smooth UI updates.
 */
public class ImageDiffCallback extends DiffUtil.ItemCallback<Image> {

  /**
   * Called to check whether two objects represent the same item.
   * For example, if your items have unique IDs, this method should check their id equality.
   *
   * @param oldItem The item in the old list.
   * @param newItem The item in the new list.
   * @return True if the two items represent the same object or false if they are different.
   */
  @Override
  public boolean areItemsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
    return oldItem.id == newItem.id;
  }

  /**
   * Called to check whether two items have the same data.
   * This information is used to detect if the contents of an item have changed.
   * This method is called only if {@link #areItemsTheSame(Image, Image)} returns true for these
   * items.
   *
   * @param oldItem The item in the old list.
   * @param newItem The item in the new list.
   * @return True if the contents of the items are the same or false if they are different.
   */
  @Override
  public boolean areContentsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
    return oldItem.id == newItem.id && oldItem.name.equals(newItem.name) && oldItem.source.equals(
        newItem.source) && oldItem.path.equals(newItem.path);
  }
} 
