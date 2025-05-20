package com.mfrankic.sketchid;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogPrefFragCompat extends DialogFragment {
  private static final String TAG = "DialogPrefFragCompat";
  private static final String ARG_KEY = "key";
  private static final String ARG_TITLE = "title";
  private static final String ARG_MESSAGE = "message";
  private static final String ARG_POSITIVE_TEXT = "positive_text";
  private static final String ARG_NEGATIVE_TEXT = "negative_text";
  private static final String ARG_VALUE = "value";
  private static final String KEY_PARAM = "key";
  private static final String VALUE_PARAM = "value";
  private static final String CHANGED_PARAM = "changed";

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Bundle args = getArguments();
    if (args == null) {
      Log.e(TAG, "No arguments provided to dialog fragment");
      return createErrorDialog("No arguments provided");
    }

    final String prefKey = args.getString(ARG_KEY);
    if (prefKey == null || prefKey.isEmpty()) {
      Log.e(TAG, "Preference key is null or empty");
      return createErrorDialog("Invalid preference key");
    }

    CharSequence title = args.getCharSequence(ARG_TITLE);
    CharSequence message = args.getCharSequence(ARG_MESSAGE);
    CharSequence positiveText = args.getCharSequence(ARG_POSITIVE_TEXT);
    CharSequence negativeText = args.getCharSequence(ARG_NEGATIVE_TEXT);
    final String value = args.getString(ARG_VALUE);

    if (TextUtils.isEmpty(message)) {
      message = "No message available";
    }

    return new AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(
            positiveText, (dialog1, which) -> {
              Bundle result = new Bundle();
              result.putString(KEY_PARAM, prefKey);
              result.putString(VALUE_PARAM, value);
              result.putBoolean(CHANGED_PARAM, true);
              getParentFragmentManager().setFragmentResult(prefKey, result);
            }
        )
        .setNegativeButton(negativeText, (dialog1, which) -> dismiss())
        .create();
  }

  private AlertDialog createErrorDialog(String errorMessage) {
    return new AlertDialog.Builder(requireContext())
        .setTitle("Error")
        .setMessage(errorMessage)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> dismiss())
        .create();
  }
}
