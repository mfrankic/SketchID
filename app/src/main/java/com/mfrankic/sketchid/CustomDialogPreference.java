package com.mfrankic.sketchid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class CustomDialogPreference extends DialogPreference {

  private int titleColor = getContext()
      .getResources()
      .getColor(R.color.onSurface, getContext().getTheme());

  public CustomDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs, R.attr.dialogPreferenceStyle);
  }

  /**
   * Sets the color of the title text using a color resource ID
   *
   * @param colorResId The color resource ID to use for the title
   */
  public void setTitleColor(int colorResId) {
    this.titleColor = colorResId;
    notifyChanged();
  }

  @Override
  public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);

    // Set the title color
    TextView titleView = (TextView) holder.findViewById(android.R.id.title);
    if (titleView != null && isEnabled()) {
      titleView.setTextColor(titleColor);
    }
  }

  @Override
  public boolean persistString(String value) {
    super.persistString(value);
    return true;
  }
}
