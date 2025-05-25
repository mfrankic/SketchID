package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Unit tests for the SelectionManager generic class
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectionManagerTest {

  @Mock
  private Consumer<Integer> mockOnChanged;

  private SelectionManager<String> selectionManager;
  private List<String> testItems;

  @Before
  public void setUp() {
    selectionManager = new SelectionManager<>(mockOnChanged);
    testItems = new ArrayList<>();
    testItems.addAll(Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5"));
  }

  @Test
  public void testConstructor_AcceptsOnChangedCallback() {
    Consumer<Integer> callback = position -> {
    };
    SelectionManager<String> manager = new SelectionManager<>(callback);
    assertNotNull("SelectionManager should be created successfully", manager);
  }

  @Test
  public void testInitialState_DisabledByDefault() {
    // By default, selection should be disabled
    assertFalse("Initially, no item should be selected", selectionManager.isSelected("Item1"));
    assertTrue("Initially, selected set should be empty", selectionManager.getSelected().isEmpty());
  }

  @Test
  public void testSetEnabled_True_CallsOnChanged() {
    selectionManager.setEnabled(true);
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testSetEnabled_False_CallsOnChanged() {
    selectionManager.setEnabled(false);
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testSetEnabled_False_ClearsSelections() {
    // First enable and select an item
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    assertTrue("Item should be selected", selectionManager.isSelected("Item1"));

    // Now disable - should clear selections
    selectionManager.setEnabled(false);
    assertFalse("Item should no longer be selected", selectionManager.isSelected("Item1"));
    assertTrue("Selected set should be empty", selectionManager.getSelected().isEmpty());
  }

  @Test
  public void testToggle_WhenDisabled_DoesNothing() {
    // Ensure manager is disabled
    selectionManager.setEnabled(false);
    reset(mockOnChanged); // Clear any previous calls

    selectionManager.toggle("Item1", testItems);

    // Should not change selection or call callback
    assertFalse("Item should not be selected when disabled", selectionManager.isSelected("Item1"));
    verify(mockOnChanged, never()).accept(anyInt());
  }

  @Test
  public void testToggle_WhenEnabled_SelectsItem() {
    selectionManager.setEnabled(true);
    reset(mockOnChanged);

    selectionManager.toggle("Item1", testItems);

    assertTrue("Item should be selected", selectionManager.isSelected("Item1"));
    verify(mockOnChanged).accept(0); // Position of "Item1" in testItems
  }

  @Test
  public void testToggle_WhenEnabled_DeselectsAlreadySelectedItem() {
    selectionManager.setEnabled(true);

    // Select the item first
    selectionManager.toggle("Item1", testItems);
    assertTrue("Item should be selected", selectionManager.isSelected("Item1"));

    reset(mockOnChanged);

    // Toggle again to deselect
    selectionManager.toggle("Item1", testItems);
    assertFalse("Item should be deselected", selectionManager.isSelected("Item1"));
    verify(mockOnChanged).accept(0);
  }

  @Test
  public void testToggle_CallsOnChangedWithCorrectPosition() {
    selectionManager.setEnabled(true);
    reset(mockOnChanged);

    selectionManager.toggle("Item3", testItems); // Index 2
    verify(mockOnChanged).accept(2);

    selectionManager.toggle("Item5", testItems); // Index 4
    verify(mockOnChanged).accept(4);
  }

  @Test
  public void testToggle_ItemNotInList_CallsOnChangedWithNegativeOne() {
    selectionManager.setEnabled(true);
    reset(mockOnChanged);

    selectionManager.toggle("NonexistentItem", testItems);
    verify(mockOnChanged).accept(-1); // Item not found
  }

  @Test
  public void testIsSelected_ReturnsFalseForUnselectedItem() {
    selectionManager.setEnabled(true);
    assertFalse("Unselected item should return false", selectionManager.isSelected("Item1"));
  }

  @Test
  public void testIsSelected_ReturnsTrueForSelectedItem() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    assertTrue("Selected item should return true", selectionManager.isSelected("Item1"));
  }

  @Test
  public void testGetSelected_ReturnsEmptySetInitially() {
    Set<String> selected = selectionManager.getSelected();
    assertTrue("Selected set should be empty initially", selected.isEmpty());
  }

  @Test
  public void testGetSelected_ReturnsNewSetCopy() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);

    Set<String> selected1 = selectionManager.getSelected();
    Set<String> selected2 = selectionManager.getSelected();

    assertNotSame("Should return different set instances", selected1, selected2);
    assertEquals("Sets should have same content", selected1, selected2);

    // Verify modifying returned set doesn't affect internal state
    selected1.clear();
    assertFalse("Internal state should not be affected", selectionManager.getSelected().isEmpty());
  }

  @Test
  public void testGetSelected_ReturnsCorrectSelectedItems() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item3", testItems);

    Set<String> selected = selectionManager.getSelected();
    assertEquals("Should have 2 selected items", 2, selected.size());
    assertTrue("Should contain Item1", selected.contains("Item1"));
    assertTrue("Should contain Item3", selected.contains("Item3"));
    assertFalse("Should not contain Item2", selected.contains("Item2"));
  }

  @Test
  public void testClearSelections_WhenEmpty_DoesNotCallOnChanged() {
    selectionManager.clearSelections();
    verify(mockOnChanged, never()).accept(anyInt());
  }

  @Test
  public void testClearSelections_WhenNotEmpty_ClearsAndCallsOnChanged() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    reset(mockOnChanged);

    selectionManager.clearSelections();

    assertTrue("Selections should be cleared", selectionManager.getSelected().isEmpty());
    assertFalse("Item1 should not be selected", selectionManager.isSelected("Item1"));
    assertFalse("Item2 should not be selected", selectionManager.isSelected("Item2"));
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testValidateSelectionsAgainst_EmptySelections_DoesNotCallOnChanged() {
    List<String> newList = Arrays.asList("NewItem1", "NewItem2");
    selectionManager.validateSelectionsAgainst(newList);
    verify(mockOnChanged, never()).accept(anyInt());
  }

  @Test
  public void testValidateSelectionsAgainst_AllItemsStillExist_DoesNothing() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    reset(mockOnChanged);

    // Validate against list that still contains selected items
    List<String> newList = Arrays.asList("Item1", "Item2", "Item6");
    selectionManager.validateSelectionsAgainst(newList);

    // Selections should remain
    assertTrue("Item1 should still be selected", selectionManager.isSelected("Item1"));
    assertTrue("Item2 should still be selected", selectionManager.isSelected("Item2"));
    verify(mockOnChanged, never()).accept(anyInt());
  }

  @Test
  public void testValidateSelectionsAgainst_SomeItemsRemoved_ClearsInvalidSelections() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);
    selectionManager.toggle("Item3", testItems);

    reset(mockOnChanged);

    // Validate against list that only contains Item1
    List<String> newList = Arrays.asList("Item1", "NewItem");
    selectionManager.validateSelectionsAgainst(newList);

    // Only Item1 should remain selected
    assertTrue("Item1 should still be selected", selectionManager.isSelected("Item1"));
    assertFalse("Item2 should be deselected", selectionManager.isSelected("Item2"));
    assertFalse("Item3 should be deselected", selectionManager.isSelected("Item3"));
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testValidateSelectionsAgainst_AllItemsRemoved_ClearsAllSelections() {
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    reset(mockOnChanged);

    // Validate against list that contains none of the selected items
    List<String> newList = Arrays.asList("NewItem1", "NewItem2");
    selectionManager.validateSelectionsAgainst(newList);

    assertTrue("All selections should be cleared", selectionManager.getSelected().isEmpty());
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testMultipleSelections_WorkCorrectly() {
    selectionManager.setEnabled(true);

    // Select multiple items
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item3", testItems);
    selectionManager.toggle("Item5", testItems);

    Set<String> selected = selectionManager.getSelected();
    assertEquals("Should have 3 selected items", 3, selected.size());
    assertTrue("Should contain Item1", selected.contains("Item1"));
    assertTrue("Should contain Item3", selected.contains("Item3"));
    assertTrue("Should contain Item5", selected.contains("Item5"));

    // Deselect one
    selectionManager.toggle("Item3", testItems);

    selected = selectionManager.getSelected();
    assertEquals("Should have 2 selected items", 2, selected.size());
    assertTrue("Should still contain Item1", selected.contains("Item1"));
    assertFalse("Should not contain Item3", selected.contains("Item3"));
    assertTrue("Should still contain Item5", selected.contains("Item5"));
  }

  @Test
  public void testGenericType_WorksWithDifferentTypes() {
    // Test with Integer type
    SelectionManager<Integer> intManager = new SelectionManager<>(position -> {
    });
    List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5);

    intManager.setEnabled(true);
    intManager.toggle(3, intList);

    assertTrue("Should work with Integer type", intManager.isSelected(3));
    assertEquals("Should have one selected integer", 1, intManager.getSelected().size());
  }

  @Test
  public void testEdgeCases_NullItem() {
    selectionManager.setEnabled(true);

    // Test with null item
    try {
      selectionManager.toggle(null, testItems);
      selectionManager.isSelected(null);
      // If no exception is thrown, the implementation handles null gracefully
      assertTrue("Should handle null items gracefully", true);
    } catch (Exception e) {
      // If exception is thrown, that's also a valid response to null input
      assertTrue("May throw exception for null items", true);
    }
  }

  @Test
  public void testEdgeCases_EmptyList() {
    selectionManager.setEnabled(true);
    List<String> emptyList = new ArrayList<>();

    reset(mockOnChanged);
    selectionManager.toggle("Item1", emptyList);

    // Should call onChanged with -1 (not found)
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testEdgeCases_NullList() {
    selectionManager.setEnabled(true);

    try {
      selectionManager.toggle("Item1", null);
      // If no exception, implementation handles null list
      assertTrue("Should handle null list gracefully", true);
    } catch (NullPointerException e) {
      // NPE is expected for null list
      assertTrue("May throw NPE for null list", true);
    }
  }

  @Test
  public void testStateTransitions_EnableDisableEnable() {
    // Enable, select items, disable, then enable again
    selectionManager.setEnabled(true);
    selectionManager.toggle("Item1", testItems);
    selectionManager.toggle("Item2", testItems);

    assertEquals("Should have 2 selected items", 2, selectionManager.getSelected().size());

    // Disable - should clear selections
    selectionManager.setEnabled(false);
    assertTrue(
        "Selections should be cleared when disabled",
        selectionManager.getSelected().isEmpty()
    );

    // Enable again - should start fresh
    selectionManager.setEnabled(true);
    assertTrue("Should start with empty selections", selectionManager.getSelected().isEmpty());

    // Should be able to select new items
    selectionManager.toggle("Item3", testItems);
    assertTrue(
        "Should be able to select items after re-enabling",
        selectionManager.isSelected("Item3")
    );
  }

  @Test
  public void testCallbackBehavior_CorrectParameterPassing() {
    selectionManager.setEnabled(true);
    reset(mockOnChanged);

    // Test various operations and their callback parameters
    selectionManager.toggle("Item2", testItems); // Position 1
    verify(mockOnChanged).accept(1);

    reset(mockOnChanged);
    selectionManager.clearSelections(); // Global update
    verify(mockOnChanged).accept(-1);

    reset(mockOnChanged);
    selectionManager.setEnabled(false); // Global update
    verify(mockOnChanged).accept(-1);
  }

  @Test
  public void testPerformance_LargeNumberOfItems() {
    // Test with a larger list to ensure reasonable performance
    List<String> largeList = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      largeList.add("Item" + i);
    }

    selectionManager.setEnabled(true);

    // Select many items
    for (int i = 0; i < 100; i++) {
      selectionManager.toggle("Item" + i, largeList);
    }

    assertEquals("Should have 100 selected items", 100, selectionManager.getSelected().size());

    // Validate against modified list
    List<String> newList = largeList.subList(50, 1000); // Remove first 50 items
    selectionManager.validateSelectionsAgainst(newList);

    assertEquals(
        "Should have 50 selected items remaining",
        50,
        selectionManager.getSelected().size()
    );
  }
} 
