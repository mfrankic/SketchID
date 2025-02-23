package com.mfrankic.sketchid;

import java.util.ArrayList;
import java.util.List;

public class InitialData {

  private InitialData() {
    throw new IllegalStateException("Utility class");
  }

  public static List<Image> getImages() {
    List<Image> images = new ArrayList<>();
    images.add(new Image("Arrow", R.drawable.arrow));
    images.add(new Image("Crown", R.drawable.crown));
//        images.add(new Image("Envelope", R.drawable.envelope));
//        images.add(new Image("House", R.drawable.house));
//        images.add(new Image("Lightbulb", R.drawable.lightbulb));
//        images.add(new Image("Moon", R.drawable.moon));
//        images.add(new Image("Smiley", R.drawable.smiley));
//        images.add(new Image("Star", R.drawable.star));
//        images.add(new Image("Sun", R.drawable.sun));
//        images.add(new Image("Umbrella", R.drawable.umbrella));

    return images;
  }
}
