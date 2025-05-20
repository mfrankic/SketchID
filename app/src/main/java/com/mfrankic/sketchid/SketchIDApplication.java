package com.mfrankic.sketchid;

import android.app.Application;

/**
 * Custom Application class for initializing app-wide components
 */
public class SketchIDApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    ResourceUtils.initialize();
  }
} 
