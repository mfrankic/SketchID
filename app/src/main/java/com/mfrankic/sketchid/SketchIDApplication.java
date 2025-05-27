package com.mfrankic.sketchid;

import android.app.Application;

/**
 * Custom {@link Application} class for the SketchID application.
 * This class is used for initializing application-wide components and resources.
 */
public class SketchIDApplication extends Application {

  /**
   * Called when the application is starting, before any other application objects have been
   * created.
   * Initializes application-wide components, such as {@link ResourceUtils}.
   */
  @Override
  public void onCreate() {
    super.onCreate();

    ResourceUtils.initialize();
  }
} 
