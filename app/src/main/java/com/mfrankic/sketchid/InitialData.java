package com.mfrankic.sketchid;

import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;

import java.util.ArrayList;
import java.util.List;

public class InitialData {

  private InitialData() {
    throw new IllegalStateException("Utility class");
  }

  public static List<Image> getImages() {
    List<Image> images = new ArrayList<>();
    images.add(new Image("Arrow", SOURCE_DEFAULT, String.valueOf(R.drawable.arrow)));
    images.add(new Image("Checkmark", SOURCE_DEFAULT, String.valueOf(R.drawable.checkmark)));
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
