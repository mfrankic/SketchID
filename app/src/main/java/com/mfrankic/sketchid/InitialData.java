package com.mfrankic.sketchid;

import java.util.ArrayList;
import java.util.List;

public class InitialData {

  public static final String DEFAULT_IMAGE = "default";

  private InitialData() {
    throw new IllegalStateException("Utility class");
  }

  public static List<Image> getImages() {
    List<Image> images = new ArrayList<>();
    images.add(new Image("Arrow", DEFAULT_IMAGE, String.valueOf(R.drawable.arrow)));
    images.add(new Image("Crown", DEFAULT_IMAGE, String.valueOf(R.drawable.crown)));
    images.add(new Image("Envelope", DEFAULT_IMAGE, String.valueOf(R.drawable.envelope)));
    images.add(new Image("House", DEFAULT_IMAGE, String.valueOf(R.drawable.house)));
    images.add(new Image("Lightbulb", DEFAULT_IMAGE, String.valueOf(R.drawable.lightbulb)));
    images.add(new Image("Moon", DEFAULT_IMAGE, String.valueOf(R.drawable.moon)));
    images.add(new Image("Smiley", DEFAULT_IMAGE, String.valueOf(R.drawable.smiley)));
    images.add(new Image("Star", DEFAULT_IMAGE, String.valueOf(R.drawable.star)));
    images.add(new Image("Sun", DEFAULT_IMAGE, String.valueOf(R.drawable.sun)));
    images.add(new Image("Umbrella", DEFAULT_IMAGE, String.valueOf(R.drawable.umbrella)));

    return images;
  }
}
