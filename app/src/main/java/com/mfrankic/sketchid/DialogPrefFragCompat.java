package com.mfrankic.sketchid;

import android.os.Bundle;

import androidx.preference.PreferenceDialogFragmentCompat;

public class DialogPrefFragCompat extends PreferenceDialogFragmentCompat {
  public static DialogPrefFragCompat newInstance(String key) {
    final DialogPrefFragCompat fragment = new DialogPrefFragCompat();
    final Bundle bundle = new Bundle(1);
    bundle.putString(ARG_KEY, key);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      CustomDialogPreference preference = (CustomDialogPreference) getPreference();
      String value = preference.getExtras().getString("value");
      if (preference.callChangeListener(value)) {
        preference.persistString(value);
      }
    }
  }
}
