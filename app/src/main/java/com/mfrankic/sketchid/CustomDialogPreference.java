package com.mfrankic.sketchid;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class CustomDialogPreference extends DialogPreference {
  public CustomDialogPreference(Context context) {
    this(context, null);
  }

  public CustomDialogPreference(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.dialogPreferenceStyle);
  }

  public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, defStyleAttr);
  }

  public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public boolean persistString(String value) {
    super.persistString(value);
    return true;
  }
}
