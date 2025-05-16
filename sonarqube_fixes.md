# SonarQube Issue Fixes

## Fixed Issues

### Code Organization and Constants

1. Created string constants in `Constants.java` for commonly duplicated strings:
    - Error messages like `ERROR_NAME_EMPTY`
    - Source indicators like `SOURCE_DEFAULT`
    - Database field names like `IMAGES_VIEWED`, `FINISHED`, `SETTINGS`
    - File export patterns and directory names
    - Button labels like `DELETE_BUTTON` and `CANCEL_BUTTON`

### Removed Unused or Misplaced Fields

1. Removed `drawingSettings` field in `DrawingActivity.java` (S1450)
2. Removed unused `prefs` variable in `collectDrawingSettings()` method in `DrawingActivity.java` (
   S1854)
3. Removed unused `currentDraggablePosition` field in `ImageAdapter.java` (S1068)
4. Converted `recyclerViewUnselected` field to local variable in `ImageSelectionActivity.java` (
   S1450)

### Improved Code Structure

1. Replaced switch statement with if statement in `loadCurrentItem()` method of
   `DrawingActivity.java` (S1301)
2. Removed useless curly braces in lambda expression in `ImageAdapter.java` (S1602)
3. Added private constructor to `SelectedImagesManager` to hide implicit public one (S1118)
4. Removed commented out code in `backup_rules.xml` (S125)
5. Completed TODO task in `data_extraction_rules.xml` (S1135)

### Proper Constants Usage

1. Replaced string literals with constants throughout the codebase:
    - Replaced `"Name cannot be empty"` with `ERROR_NAME_EMPTY`
    - Replaced `"default"` with `SOURCE_DEFAULT`

## Remaining Issues

### High Cognitive Complexity

Several methods still have high cognitive complexity that should be refactored:

1. `loadItems()` method in `DrawingActivity.java` - Cognitive Complexity: 40 (limit: 15)
2. `onCreateViewHolder()` in `ImageAdapter.java` - Cognitive Complexity: 19 (limit: 15)
3. `ImageOrderAdapter` constructor - Cognitive Complexity: 23 (limit: 15)
4. `showImageOptionsDialog()` in `ImageSelectionActivity.java` - Cognitive Complexity: 22 (limit:
    15)
5. `resetUserProgress()` in `SettingsFragment.java` - Cognitive Complexity: 77 (limit: 15)

### Duplicated String Literals

1. Some string literals still need to be extracted as constants, particularly in
   `UserProgressManager.java` and `SettingsFragment.java`

### Security Issues

1. SonarQube token in `sonar-project.properties` should be revoked and removed (S6702)

### Other Issues

1. Singleton implementation in `AppDatabase.java` was flagged for review (S6548)
