package com.mfrankic.sketchid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class CustomDialogPreference extends DialogPreference {

  private int titleColor;

  public CustomDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs, R.attr.dialogPreferenceStyle);

    titleColor = 0xFF333331;
  }

  /**
   * Sets the color of the title text using a direct color value
   *
   * @param colorValue Direct color integer value (not a resource ID)
   */
  public void setTitleColor(int colorValue) {
    this.titleColor = colorValue;
    notifyChanged();
  }

  @Override
  public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);

    TextView titleView = (TextView) holder.findViewById(android.R.id.title);
    if (titleView != null && isEnabled()) {
      titleView.setTextColor(titleColor);
    }
  }
}
