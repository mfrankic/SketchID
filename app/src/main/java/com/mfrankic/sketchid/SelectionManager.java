package com.mfrankic.sketchid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Generic selection manager to handle multi-select state
 *
 * @param <T> The type of items to manage selection for
 */
public class SelectionManager<T> {
  private final Set<T> selected = new HashSet<>();
  private final Consumer<Integer> onChanged;
  // For tracking affected items in bulk operations
  private final Set<Integer> affectedPositions = new HashSet<>();
  private boolean enabled = false;

  /**
   * Create a new SelectionManager
   *
   * @param onChanged Callback to be invoked when selection changes, with position of changed item
   *                  or -1 for a global update
   */
  public SelectionManager(Consumer<Integer> onChanged) {
    this.onChanged = onChanged;
  }

  /**
   * Enable or disable selection mode
   *
   * @param enabled Whether selection mode is enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (!enabled) {
      selected.clear();
    }
    onChanged.accept(-1); // Still need full refresh when mode changes
  }

  /**
   * Toggle selection state of an item
   *
   * @param item     The item to toggle
   * @param allItems The list containing all items (to find index)
   */
  public void toggle(T item, List<T> allItems) {
    if (!enabled) return;

    if (!selected.remove(item)) {
      selected.add(item);
    }

    int position = allItems.indexOf(item);
    onChanged.accept(position);
  }

  /**
   * Check if an item is selected
   *
   * @param item The item to check
   * @return true if the item is selected, false otherwise
   */
  public boolean isSelected(T item) {
    return selected.contains(item);
  }

  /**
   * Get all selected items
   *
   * @return A new set containing all selected items
   */
  public Set<T> getSelected() {
    return new HashSet<>(selected);
  }

  /**
   * Clear all selections
   */
  public void clearSelections() {
    if (selected.isEmpty()) return;

    // Track which items were selected before clearing
    affectedPositions.clear();

    // We still need a full refresh here since multiple items are affected
    selected.clear();
    onChanged.accept(-1);
  }

  /**
   * Validate selections against a new list, removing any that no longer exist
   *
   * @param newList The new list of items
   */
  public void validateSelectionsAgainst(List<T> newList) {
    if (selected.isEmpty()) return;

    affectedPositions.clear();
    Set<T> toRemove = new HashSet<>();

    // Find items to remove
    for (T item : selected) {
      if (!newList.contains(item)) {
        toRemove.add(item);
      }
    }

    if (!toRemove.isEmpty()) {
      selected.removeAll(toRemove);

      // If only a few items were affected, we could track them
      // but since this is called during list changes, it's safer
      // to do a full refresh
      onChanged.accept(-1);
    }
  }
} 
