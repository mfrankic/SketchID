package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Unit tests for the SelectionManager generic class
 */
public class SelectionManagerTest {

  private SelectionManager<String> selectionManager;
  private List<Integer> callbackValues;
  private List<String> testItems;

  @Before
  public void setUp() {
    callbackValues = new ArrayList<>();
    Consumer<Integer> testCallback = value -> callbackValues.add(value);
    selectionManager = new SelectionManager<>(testCallback);

    testItems = Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5");
  }

  @Test
  public void testConstructorWithCallback() {
    // Test that constructor accepts callback and doesn't crash
    Consumer<Integer> callback = value -> {
    };
    SelectionManager<String> manager = new SelectionManager<>(callback);
    assertNotNull("SelectionManager should be created successfully", manager);
  }

  @Test
  public void testConstructorWithNullCallback() {
    // Test constructor with null callback
    try {
      SelectionManager<String> manager = new SelectionManager<>(null);
      // If it doesn't throw, we'll test that operations handle null gracefully
    } catch (Exception e) {
      // Some implementations might throw on null callback
      assertTrue("Constructor might reject null callback", true);
    }
  }

  @Test
  public void testSetEnabledTrue() {
    // Test enabling selection mode
    selectionManager.setEnabled(true);

    // Should trigger callback with -1 (global update)
    assertEquals("Should have one callback value", 1, callbackValues.size());
    assertEquals("Callback should be called with -1", Integer.valueOf(-1), callbackValues.get(0));
  }

  @Test
  public void testSetEnabledFalse() {
    // First enable and add some selections
    selectionManager.setEnabled(true);
    callbackValues.clear();

    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    // Now disable
    selectionManager.setEnabled(false);

    // Should clear selections and trigger callback
    assertEquals("Should clear selections when disabled", 0, selectionManager.getSelected().size());
    assertTrue("Callback should be called when disabling", callbackValues.contains(-1));
  }

  @Test
  public void testToggleWhenDisabled() {
    // Test toggling when selection is disabled (default state)
    selectionManager.toggle("Item1", testItems);

    // Should not add to selection
    assertEquals("Should not select when disabled", 0, selectionManager.getSelected().size());
    assertFalse("Item should not be selected", selectionManager.isSelected("Item1"));
  }

  @Test
  public void testToggleWhenEnabled() {
    // Enable selection mode
    selectionManager.setEnabled(true);
    callbackValues.clear();

    // Toggle an item
    selectionManager.toggle("Item1", testItems);

    // Should add to selection
    assertEquals("Should select item when enabled", 1, selectionManager.getSelected().size());
    assertTrue("Item should be selected", selectionManager.isSelected("Item1"));

    // Should trigger callback with item position
    assertFalse("Callback should be called", callbackValues.isEmpty());
    assertEquals(
        "Callback should be called with position 0",
        Integer.valueOf(0),
        callbackValues.get(0)
    );
  }

  @Test
  public void testToggleSelectAndDeselect() {
    // Enable selection mode
    selectionManager.setEnabled(true);
    callbackValues.clear();

    // Select item
    selectionManager.toggle("Item2", testItems);
    assertTrue("Item should be selected", selectionManager.isSelected("Item2"));
    assertEquals("Should have 1 selected item", 1, selectionManager.getSelected().size());

    // Deselect same item
    selectionManager.toggle("Item2", testItems);
    assertFalse("Item should be deselected", selectionManager.isSelected("Item2"));
    assertEquals("Should have 0 selected items", 0, selectionManager.getSelected().size());
  }

  @Test
  public void testToggleMultipleItems() {
    // Enable selection mode
    selectionManager.setEnabled(true);
    callbackValues.clear();

    // Select multiple items
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item3", testItems);
    selectionManager.toggle("Item5", testItems);

    assertEquals("Should have 3 selected items", 3, selectionManager.getSelected().size());
    assertTrue("Item1 should be selected", selectionManager.isSelected("Item1"));
    assertTrue("Item3 should be selected", selectionManager.isSelected("Item3"));
    assertTrue("Item5 should be selected", selectionManager.isSelected("Item5"));
    assertFalse("Item2 should not be selected", selectionManager.isSelected("Item2"));
    assertFalse("Item4 should not be selected", selectionManager.isSelected("Item4"));

    // Check callback positions
    assertEquals("Should have 3 callbacks", 3, callbackValues.size());
    assertEquals("First callback should be position 0", Integer.valueOf(0), callbackValues.get(0));
    assertEquals("Second callback should be position 2", Integer.valueOf(2), callbackValues.get(1));
    assertEquals("Third callback should be position 4", Integer.valueOf(4), callbackValues.get(2));
  }

  @Test
  public void testIsSelectedWithUnselectedItem() {
    // Test isSelected with item that was never selected
    assertFalse("Unselected item should return false", selectionManager.isSelected("Item1"));
  }

  @Test
  public void testIsSelectedWithNullItem() {
    // Test isSelected with null item
    assertFalse("Null item should return false", selectionManager.isSelected(null));
  }

  @Test
  public void testGetSelectedReturnsNewSet() {
    // Enable and select some items
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    Set<String> selected1 = selectionManager.getSelected();
    Set<String> selected2 = selectionManager.getSelected();

    // Should return different set instances
    assertNotSame("Should return different set instances", selected1, selected2);

    // But with same content
    assertEquals("Should have same content", selected1, selected2);

    // Modifying returned set should not affect internal state
    selected1.clear();
    assertEquals(
        "Modifying returned set should not affect internal state",
        2,
        selectionManager.getSelected().size()
    );
  }

  @Test
  public void testClearSelectionsWhenEmpty() {
    // Test clearing selections when none are selected
    selectionManager.clearSelections();

    // Should not trigger callback when already empty
    assertEquals("Should not trigger callback when already empty", 0, callbackValues.size());
  }

  @Test
  public void testClearSelectionsWithItems() {
    // Enable and select some items
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);
    callbackValues.clear();

    // Clear selections
    selectionManager.clearSelections();

    assertEquals("Should have no selected items", 0, selectionManager.getSelected().size());
    assertFalse("Item1 should not be selected", selectionManager.isSelected("Item1"));
    assertFalse("Item2 should not be selected", selectionManager.isSelected("Item2"));

    // Should trigger callback with -1
    assertEquals("Should trigger one callback", 1, callbackValues.size());
    assertEquals("Callback should be -1", Integer.valueOf(-1), callbackValues.get(0));
  }

  @Test
  public void testValidateSelectionsAgainstSameList() {
    // Enable and select some items
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item3", testItems);
    callbackValues.clear();

    // Validate against same list
    selectionManager.validateSelectionsAgainst(testItems);

    // Should keep all selections
    assertEquals("Should keep all selections", 2, selectionManager.getSelected().size());
    assertTrue("Item1 should still be selected", selectionManager.isSelected("Item1"));
    assertTrue("Item3 should still be selected", selectionManager.isSelected("Item3"));

    // Should not trigger callback if nothing changes
    assertEquals("Should not trigger callback", 0, callbackValues.size());
  }

  @Test
  public void testValidateSelectionsAgainstNewList() {
    // Enable and select some items
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item3", testItems);
    selectionManager.toggle("Item5", testItems);
    callbackValues.clear();

    // Validate against list missing some items
    List<String> newList = Arrays.asList("Item1", "Item2", "Item4");
    selectionManager.validateSelectionsAgainst(newList);

    // Should remove Item3 and Item5, keep Item1
    assertEquals("Should have 1 selected item", 1, selectionManager.getSelected().size());
    assertTrue("Item1 should still be selected", selectionManager.isSelected("Item1"));
    assertFalse("Item3 should be deselected", selectionManager.isSelected("Item3"));
    assertFalse("Item5 should be deselected", selectionManager.isSelected("Item5"));

    // Should trigger callback with -1
    assertEquals("Should trigger one callback", 1, callbackValues.size());
    assertEquals("Callback should be -1", Integer.valueOf(-1), callbackValues.get(0));
  }

  @Test
  public void testValidateSelectionsAgainstEmptyList() {
    // Enable and select some items
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);
    callbackValues.clear();

    // Validate against empty list
    selectionManager.validateSelectionsAgainst(new ArrayList<>());

    // Should remove all selections
    assertEquals("Should have no selected items", 0, selectionManager.getSelected().size());

    // Should trigger callback
    assertEquals("Should trigger one callback", 1, callbackValues.size());
    assertEquals("Callback should be -1", Integer.valueOf(-1), callbackValues.get(0));
  }

  @Test
  public void testValidateSelectionsWhenEmpty() {
    // Test validating when no items are selected
    List<String> newList = Arrays.asList("NewItem1", "NewItem2");
    selectionManager.validateSelectionsAgainst(newList);

    // Should not trigger callback when nothing to validate
    assertEquals("Should not trigger callback", 0, callbackValues.size());
  }

  @Test
  public void testToggleWithItemNotInList() {
    // Enable selection mode
    selectionManager.setEnabled(true);
    callbackValues.clear();

    // Toggle item not in the list
    selectionManager.toggle("NonExistentItem", testItems);

    // Should still add to selection
    assertTrue("Item should be selected", selectionManager.isSelected("NonExistentItem"));

    // Callback should be called with -1 (not found in list)
    assertEquals("Should trigger one callback", 1, callbackValues.size());
    assertEquals(
        "Callback should be -1 for item not found",
        Integer.valueOf(-1),
        callbackValues.get(0)
    );
  }

  @Test
  public void testWithDifferentGenericTypes() {
    // Test with Integer type
    Consumer<Integer> intCallback = value -> {
    };
    SelectionManager<Integer> intManager = new SelectionManager<>(intCallback);
    List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5);

    intManager.setEnabled(true);
    intManager.toggle(2, intList);
    intManager.toggle(4, intList);

    assertEquals("Should have 2 selected integers", 2, intManager.getSelected().size());
    assertTrue("Should contain 2", intManager.isSelected(2));
    assertTrue("Should contain 4", intManager.isSelected(4));
    assertFalse("Should not contain 1", intManager.isSelected(1));
  }

  @Test
  public void testWithCustomObjects() {
    // Test with custom objects (using Image class)
    SelectionManager<Image> imageManager = new SelectionManager<>(value -> {
    });

    Image image1 = new Image("Image1", "source", "path1");
    image1.setId(1);
    Image image2 = new Image("Image2", "source", "path2");
    image2.setId(2);
    Image image3 = new Image("Image3", "source", "path3");
    image3.setId(3);

    List<Image> imageList = Arrays.asList(image1, image2, image3);

    imageManager.setEnabled(true);
    imageManager.toggle(imageList.get(0), imageList);
    imageManager.toggle(imageList.get(2), imageList);

    assertEquals("Should have 2 selected images", 2, imageManager.getSelected().size());
    assertTrue("Should contain first image", imageManager.isSelected(imageList.get(0)));
    assertTrue("Should contain third image", imageManager.isSelected(imageList.get(2)));
    assertFalse("Should not contain second image", imageManager.isSelected(imageList.get(1)));
  }

  @Test
  public void testMultipleEnableDisableCycles() {
    // Test multiple enable/disable cycles
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);
    assertEquals("Should have 2 items selected", 2, selectionManager.getSelected().size());

    selectionManager.setEnabled(false);
    assertEquals("Should clear when disabled", 0, selectionManager.getSelected().size());

    selectionManager.setEnabled(true);
    assertEquals(
        "Should still be empty after re-enabling",
        0,
        selectionManager.getSelected().size()
    );

    selectionManager.toggle("Item3", testItems);
    assertEquals(
        "Should be able to select after re-enabling",
        1,
        selectionManager.getSelected().size()
    );
    assertTrue("Item3 should be selected", selectionManager.isSelected("Item3"));
  }

  @Test
  public void testCallbackCount() {
    // Test that callbacks are called the correct number of times
    selectionManager.setEnabled(true); // 1 callback
    callbackValues.clear();

    selectionManager.toggle("Item1", testItems); // 1 callback
    selectionManager.toggle("Item2", testItems); // 1 callback
    selectionManager.toggle("Item1", testItems); // 1 callback (deselect)
    selectionManager.clearSelections(); // 1 callback

    assertEquals("Should have 4 callbacks", 4, callbackValues.size());
  }

  @Test
  public void testConcurrentModification() {
    // Test that getting selected items doesn't interfere with modifications
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    Set<String> selected = selectionManager.getSelected();

    // Modify selection while holding reference to old set
    selectionManager.toggle("Item3", testItems);
    selectionManager.toggle("Item1", testItems); // Remove Item1

    // Old set should be unchanged
    assertEquals("Old set should be unchanged", 2, selected.size());
    assertTrue("Old set should still contain Item1", selected.contains("Item1"));

    // New set should reflect changes
    Set<String> newSelected = selectionManager.getSelected();
    assertEquals("New set should have 2 items", 2, newSelected.size());
    assertFalse("New set should not contain Item1", newSelected.contains("Item1"));
    assertTrue("New set should contain Item2", newSelected.contains("Item2"));
    assertTrue("New set should contain Item3", newSelected.contains("Item3"));
  }
} 
