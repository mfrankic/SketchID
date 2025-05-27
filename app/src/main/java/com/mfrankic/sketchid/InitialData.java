package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for providing initial data for the application.
 * This class is not meant to be instantiated.
 */
public class InitialData {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private InitialData() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Returns a list of default {@link Image} objects to populate the database with.
   * These images are sourced from drawable resources.
   *
   * @return A list of {@link Image} objects.
   */
  public static List<Image> getImages() {
    List<Image> images = new ArrayList<>();
    images.add(new Image("Arrow", SOURCE_DEFAULT, String.valueOf(R.drawable.arrow)));
    images.add(new Image("Checkmark", SOURCE_DEFAULT, String.valueOf(R.drawable.check)));
    images.add(new Image("Crown", SOURCE_DEFAULT, String.valueOf(R.drawable.crown)));
    images.add(new Image("Envelope", SOURCE_DEFAULT, String.valueOf(R.drawable.envelope)));
    images.add(new Image("Heart", SOURCE_DEFAULT, String.valueOf(R.drawable.heart)));
    images.add(new Image("Lightbulb", SOURCE_DEFAULT, String.valueOf(R.drawable.lightbulb)));
    images.add(new Image("Smiley", SOURCE_DEFAULT, String.valueOf(R.drawable.smiley)));
    images.add(new Image("Star", SOURCE_DEFAULT, String.valueOf(R.drawable.star)));
    images.add(new Image("Umbrella", SOURCE_DEFAULT, String.valueOf(R.drawable.umbrella)));
    images.add(new Image("Upload", SOURCE_DEFAULT, String.valueOf(R.drawable.upload)));

    return images;
  }
}
