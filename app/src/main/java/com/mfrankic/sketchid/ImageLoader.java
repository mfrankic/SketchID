package com.mfrankic.sketchid;

import static android.view.View.VISIBLE;
import static com.mfrankic.sketchid.Constants.SOURCE_DEFAULT;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

/**
 * Utility class for loading images into ImageViews with error handling
 */
public class ImageLoader {

  private ImageLoader() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Load an image into an ImageView with error handling
   *
   * @param imageView The ImageView to load the image into
   * @param errorText TextView to show error messages in
   * @param image     The image to load
   * @param context   The context
   */
  public static void load(ImageView imageView, TextView errorText, Image image, Context context) {

    if (errorText != null) {
      errorText.setVisibility(android.view.View.GONE);
    }

    if (image.source.equals(SOURCE_DEFAULT)) {
      loadDefaultImage(imageView, errorText, image, context);
    } else {
      loadCustomImage(imageView, errorText, image, context);
    }
  }

  /**
   * Load a default image from resources
   */
  private static void loadDefaultImage(
      ImageView imageView,
      TextView errorText,
      Image image,
      Context context
  ) {
    int resourceId = ResourceUtils.getDrawableResourceByName(image.name);
    if (resourceId != 0) {
      Glide
          .with(context)
          .load(resourceId)
          .transition(DrawableTransitionOptions.withCrossFade())
          .apply(RequestOptions.centerCropTransform())
          .into(imageView);
    } else {
      showErrorImage(imageView, errorText, "Invalid default image", context);
    }
  }

  /**
   * Display an error image with text
   */
  private static void showErrorImage(
      ImageView imageView,
      TextView errorText,
      String message,
      Context context
  ) {
    Glide
        .with(context)
        .load(android.R.drawable.ic_menu_gallery)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(RequestOptions.centerCropTransform())
        .into(imageView);

    if (errorText != null) {
      errorText.setText(message);
      errorText.setVisibility(VISIBLE);
    }
  }

  /**
   * Load a custom image from URI
   */
  private static void loadCustomImage(
      ImageView imageView,
      TextView errorText,
      Image image,
      Context context
  ) {
    try {
      Uri uri = Uri.parse(image.path);
      context
          .getContentResolver()
          .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

      Glide
          .with(context)
          .load(uri)
          .transition(DrawableTransitionOptions.withCrossFade())
          .apply(RequestOptions.centerCropTransform())
          .error(android.R.drawable.ic_menu_gallery)
          .into(imageView);
    } catch (SecurityException e) {
      showErrorImage(imageView, errorText, "Permission denied", context);
    } catch (Exception e) {
      showErrorImage(imageView, errorText, "Invalid image", context);
    }
  }

  /**
   * Simplified method to load an image into an ImageView without error text
   *
   * @param context   The context
   * @param item      The item to load
   * @param imageView The ImageView to load the image into
   */
  public static void loadImageIntoView(Context context, Item item, ImageView imageView) {
    if (item == null || imageView == null) {
      return;
    }

    if (item.getSource().equals(SOURCE_DEFAULT)) {
      try {
        int resourceId = Integer.parseInt(item.getPath());
        Glide
            .with(context)
            .load(resourceId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView);
      } catch (NumberFormatException e) {

        Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(imageView);
      }
    } else {
      try {
        Uri uri = Uri.parse(item.getPath());
        context
            .getContentResolver()
            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Glide
            .with(context)
            .load(uri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(android.R.drawable.ic_menu_gallery)
            .into(imageView);
      } catch (Exception e) {

        Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(imageView);
      }
    }
  }
} 
