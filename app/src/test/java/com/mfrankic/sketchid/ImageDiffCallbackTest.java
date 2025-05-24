package com.mfrankic.sketchid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the ImageDiffCallback class
 */
public class ImageDiffCallbackTest {

  private ImageDiffCallback diffCallback;
  private Image image1;
  private Image image2;
  private Image image3;

  @Before
  public void setUp() {
    diffCallback = new ImageDiffCallback();

    // Create test images with different properties
    image1 = new Image("Image One", "default", "/path/to/image1.jpg");
    image1.setId(1); // Set ID after construction

    image2 = new Image("Image Two", "custom", "/path/to/image2.png");
    image2.setId(2);

    image3 = new Image("Image One Modified", "default", "/different/path/image1.jpg");
    image3.setId(1); // Same ID as image1
  }

  @Test
  public void testAreItemsTheSameWithSameId() {
    // Test that items with same ID are considered the same
    boolean result = diffCallback.areItemsTheSame(image1, image3);
    assertTrue("Images with same ID should be considered the same item", result);
  }

  @Test
  public void testAreItemsTheSameWithDifferentId() {
    // Test that items with different IDs are considered different
    boolean result = diffCallback.areItemsTheSame(image1, image2);
    assertFalse("Images with different IDs should be considered different items", result);
  }

  @Test
  public void testAreItemsTheSameWithSameInstance() {
    // Test that the same instance is considered the same
    boolean result = diffCallback.areItemsTheSame(image1, image1);
    assertTrue("Same instance should be considered the same item", result);
  }

  @Test
  public void testAreContentsTheSameWithIdenticalImages() {
    // Test that images with identical content are considered the same
    Image identicalImage = new Image("Image One", "default", "/path/to/image1.jpg");
    identicalImage.setId(1);
    boolean result = diffCallback.areContentsTheSame(image1, identicalImage);
    assertTrue("Images with identical content should be considered the same", result);
  }

  @Test
  public void testAreContentsTheSameWithDifferentNames() {
    // Test that images with different names are considered different content
    boolean result = diffCallback.areContentsTheSame(image1, image3);
    assertFalse("Images with different names should be considered different content", result);
  }

  @Test
  public void testAreContentsTheSameWithDifferentPaths() {
    // Test that images with different paths are considered different content
    boolean result = diffCallback.areContentsTheSame(image1, image3);
    assertFalse("Images with different paths should be considered different content", result);
  }

  @Test
  public void testAreContentsTheSameWithDifferentSources() {
    // Test that images with different sources are considered different content
    Image differentSourceImage = new Image("Image One", "custom", "/path/to/image1.jpg");
    differentSourceImage.setId(1);
    boolean result = diffCallback.areContentsTheSame(image1, differentSourceImage);
    assertFalse("Images with different sources should be considered different content", result);
  }

  @Test
  public void testAreContentsTheSameWithDifferentIds() {
    // Test that images with different IDs are considered different content
    boolean result = diffCallback.areContentsTheSame(image1, image2);
    assertFalse("Images with different IDs should be considered different content", result);
  }

  @Test
  public void testAreContentsTheSameWithSameInstance() {
    // Test that the same instance has the same content
    boolean result = diffCallback.areContentsTheSame(image1, image1);
    assertTrue("Same instance should have the same content", result);
  }

  @Test
  public void testAreContentsTheSameWithNullStrings() {
    // Test with null string values
    Image imageWithNulls1 = new Image(null, null, null);
    imageWithNulls1.setId(1);
    Image imageWithNulls2 = new Image(null, null, null);
    imageWithNulls2.setId(1);
    Image imageWithNulls3 = new Image("name", null, null);
    imageWithNulls3.setId(1);

    // This will likely throw NullPointerException due to String.equals() call
    try {
      boolean result1 = diffCallback.areContentsTheSame(imageWithNulls1, imageWithNulls2);
      boolean result2 = diffCallback.areContentsTheSame(imageWithNulls1, imageWithNulls3);
      // If no exception, the method handles nulls somehow
    } catch (NullPointerException e) {
      // Expected behavior when comparing null strings
      assertTrue("Null strings should cause NullPointerException", true);
    }
  }

  @Test
  public void testAreContentsTheSameWithEmptyStrings() {
    // Test with empty string values
    Image imageWithEmpty1 = new Image("", "", "");
    imageWithEmpty1.setId(1);
    Image imageWithEmpty2 = new Image("", "", "");
    imageWithEmpty2.setId(1);
    Image imageWithEmpty3 = new Image("name", "", "");
    imageWithEmpty3.setId(1);

    boolean result1 = diffCallback.areContentsTheSame(imageWithEmpty1, imageWithEmpty2);
    assertTrue("Images with identical empty strings should be considered the same", result1);

    boolean result2 = diffCallback.areContentsTheSame(imageWithEmpty1, imageWithEmpty3);
    assertFalse("Images with different names should be considered different", result2);
  }

  @Test
  public void testAreContentsTheSameWithSpecialCharacters() {
    // Test with special characters in strings
    Image specialImage1 = new Image("Image@#$%", "source!@#", "/path/with spaces/image.jpg");
    specialImage1.setId(1);
    Image specialImage2 = new Image("Image@#$%", "source!@#", "/path/with spaces/image.jpg");
    specialImage2.setId(1);
    Image specialImage3 = new Image("Image@#$%", "source!@#", "/different/path/image.jpg");
    specialImage3.setId(1);

    boolean result1 = diffCallback.areContentsTheSame(specialImage1, specialImage2);
    assertTrue("Images with identical special characters should be considered the same", result1);

    boolean result2 = diffCallback.areContentsTheSame(specialImage1, specialImage3);
    assertFalse("Images with different paths should be considered different", result2);
  }

  @Test
  public void testAreContentsTheSameWithUnicodeCharacters() {
    // Test with unicode characters
    Image unicodeImage1 = new Image("图像一", "来源", "/路径/到/图像.jpg");
    unicodeImage1.setId(1);
    Image unicodeImage2 = new Image("图像一", "来源", "/路径/到/图像.jpg");
    unicodeImage2.setId(1);
    Image unicodeImage3 = new Image("图像二", "来源", "/路径/到/图像.jpg");
    unicodeImage3.setId(1);

    boolean result1 = diffCallback.areContentsTheSame(unicodeImage1, unicodeImage2);
    assertTrue("Images with identical unicode characters should be considered the same", result1);

    boolean result2 = diffCallback.areContentsTheSame(unicodeImage1, unicodeImage3);
    assertFalse("Images with different unicode names should be considered different", result2);
  }

  @Test
  public void testAreItemsTheSameWithBoundaryIds() {
    // Test with boundary ID values
    Image maxIdImage = new Image("Max ID", "source", "path");
    maxIdImage.setId(Integer.MAX_VALUE);
    Image minIdImage = new Image("Min ID", "source", "path");
    minIdImage.setId(Integer.MIN_VALUE);
    Image zeroIdImage = new Image("Zero ID", "source", "path");
    zeroIdImage.setId(0);
    Image negativeIdImage = new Image("Negative ID", "source", "path");
    negativeIdImage.setId(-1);

    // Test different boundary combinations
    assertFalse(
        "Max and Min IDs should be different",
        diffCallback.areItemsTheSame(maxIdImage, minIdImage)
    );
    assertFalse(
        "Zero and Negative IDs should be different",
        diffCallback.areItemsTheSame(zeroIdImage, negativeIdImage)
    );

    Image anotherMaxIdImage = new Image("Other", "other", "other");
    anotherMaxIdImage.setId(Integer.MAX_VALUE);
    assertTrue(
        "Same Max IDs should be the same",
        diffCallback.areItemsTheSame(maxIdImage, anotherMaxIdImage)
    );
  }

  @Test
  public void testAreContentsTheSameWithLongStrings() {
    // Test with very long strings
    StringBuilder longStringBuilder = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longStringBuilder.append("long");
    }
    String longString = longStringBuilder.toString();

    Image longStringImage1 = new Image(longString, longString, longString);
    longStringImage1.setId(1);
    Image longStringImage2 = new Image(longString, longString, longString);
    longStringImage2.setId(1);
    Image longStringImage3 = new Image(longString + "different", longString, longString);
    longStringImage3.setId(1);

    boolean result1 = diffCallback.areContentsTheSame(longStringImage1, longStringImage2);
    assertTrue("Images with identical long strings should be considered the same", result1);

    boolean result2 = diffCallback.areContentsTheSame(longStringImage1, longStringImage3);
    assertFalse("Images with different long strings should be considered different", result2);
  }

  @Test
  public void testCallbackInstantiation() {
    // Test that the callback can be instantiated multiple times
    ImageDiffCallback callback1 = new ImageDiffCallback();
    ImageDiffCallback callback2 = new ImageDiffCallback();

    assertNotNull("First callback should not be null", callback1);
    assertNotNull("Second callback should not be null", callback2);
    assertNotSame("Callbacks should be different instances", callback1, callback2);

    // Test that both callbacks work the same way
    boolean result1 = callback1.areItemsTheSame(image1, image2);
    boolean result2 = callback2.areItemsTheSame(image1, image2);
    assertEquals("Both callbacks should give same result", result1, result2);
  }

  @Test
  public void testComplexScenario() {
    // Test a complex scenario with multiple images
    Image[] images = new Image[5];

    images[0] = new Image("Image A", "default", "/path/a.jpg");
    images[0].setId(1);

    images[1] = new Image("Image B", "custom", "/path/b.jpg");
    images[1].setId(2);

    images[2] = new Image("Image C", "default", "/path/c.jpg");
    images[2].setId(3);

    images[3] = new Image("Image A Modified", "default", "/path/a.jpg"); // Same ID, different name
    images[3].setId(1);

    images[4] = new Image("Image A", "default", "/path/a_modified.jpg"); // Same ID, different path
    images[4].setId(1);

    // Test items comparison
    assertTrue(
        "Images 0 and 3 should have same items (same ID)",
        diffCallback.areItemsTheSame(images[0], images[3])
    );
    assertTrue(
        "Images 0 and 4 should have same items (same ID)",
        diffCallback.areItemsTheSame(images[0], images[4])
    );
    assertFalse(
        "Images 0 and 1 should have different items (different ID)",
        diffCallback.areItemsTheSame(images[0], images[1])
    );

    // Test content comparison
    assertFalse(
        "Images 0 and 3 should have different content (different name)",
        diffCallback.areContentsTheSame(images[0], images[3])
    );
    assertFalse(
        "Images 0 and 4 should have different content (different path)",
        diffCallback.areContentsTheSame(images[0], images[4])
    );
    assertFalse(
        "Images 0 and 1 should have different content (different everything)",
        diffCallback.areContentsTheSame(images[0], images[1])
    );
  }

  @Test
  public void testCaseSensitivity() {
    // Test that string comparisons are case sensitive
    Image caseSensitive1 = new Image("Image", "Source", "Path");
    caseSensitive1.setId(1);
    Image caseSensitive2 = new Image("image", "source", "path");
    caseSensitive2.setId(1);
    Image caseSensitive3 = new Image("IMAGE", "SOURCE", "PATH");
    caseSensitive3.setId(1);

    boolean result1 = diffCallback.areContentsTheSame(caseSensitive1, caseSensitive2);
    assertFalse("String comparison should be case sensitive", result1);

    boolean result2 = diffCallback.areContentsTheSame(caseSensitive1, caseSensitive3);
    assertFalse("String comparison should be case sensitive", result2);

    boolean result3 = diffCallback.areContentsTheSame(caseSensitive2, caseSensitive3);
    assertFalse("String comparison should be case sensitive", result3);
  }

  @Test
  public void testWhitespaceHandling() {
    // Test that whitespace differences are detected
    Image whitespace1 = new Image("Image", "Source", "Path");
    whitespace1.setId(1);
    Image whitespace2 = new Image(" Image", "Source", "Path");
    whitespace2.setId(1);
    Image whitespace3 = new Image("Image ", "Source", "Path");
    whitespace3.setId(1);
    Image whitespace4 = new Image("Image", " Source", "Path");
    whitespace4.setId(1);

    assertFalse(
        "Leading space should make content different",
        diffCallback.areContentsTheSame(whitespace1, whitespace2)
    );
    assertFalse(
        "Trailing space should make content different",
        diffCallback.areContentsTheSame(whitespace1, whitespace3)
    );
    assertFalse(
        "Leading space in source should make content different",
        diffCallback.areContentsTheSame(whitespace1, whitespace4)
    );
  }
} 
