package com.appsbay.minguoliteratural.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.appsbay.minguoliteratural.R;

import java.util.Calendar;

/** A neutral, local-only birth-date screen before consent or ad SDK startup. */
public final class AgeGate {
    private static final String PREFS = "Advertising Age";
    private static final String KEY_SKIPPED = "skipped";
    private static final String KEY_YEAR = "birthYear";
    private static final String KEY_MONTH = "birthMonth";
    private static final String KEY_DAY = "birthDay";

    private final SharedPreferences preferences;

    public AgeGate(@NonNull Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isResolved() {
        return preferences.getBoolean(KEY_SKIPPED, false) || hasValidBirthDate();
    }

    public boolean mayRequestAds() {
        return !preferences.getBoolean(KEY_SKIPPED, false)
                && AdvertisingAge.mayRequestAds(
                preferences.getInt(KEY_YEAR, 0),
                preferences.getInt(KEY_MONTH, 0),
                preferences.getInt(KEY_DAY, 0), Calendar.getInstance());
    }

    public void request(@NonNull Activity activity, @NonNull Runnable onResolved) {
        if (isResolved()) {
            onResolved.run();
        } else {
            show(activity, onResolved);
        }
    }

    public void show(@NonNull Activity activity, @NonNull Runnable onResolved) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        int pad = (int) (24 * activity.getResources().getDisplayMetrics().density);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad / 2, pad, 0);
        TextView description = new TextView(activity);
        description.setText(R.string.age_gate_message);
        description.setTextColor(MyColor.getDetailTextColor(activity));
        layout.addView(description);

        LinearLayout fields = new LinearLayout(activity);
        fields.setOrientation(LinearLayout.HORIZONTAL);
        EditText year = numberField(activity, R.string.age_gate_year, 4);
        EditText month = numberField(activity, R.string.age_gate_month, 2);
        EditText day = numberField(activity, R.string.age_gate_day, 2);
        fields.addView(year, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        fields.addView(month, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        fields.addView(day, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(fields);

        AlertDialog dialog = DialogChrome.alert(activity)
                .setTitle(R.string.age_gate_title)
                .setView(layout)
                .setPositiveButton(R.string.age_gate_continue, null)
                .setNegativeButton(R.string.age_gate_skip, (d, which) -> {
                    saveSkipped();
                    onResolved.run();
                })
                .setOnCancelListener(d -> {
                    saveSkipped();
                    onResolved.run();
                })
                .create();
        dialog.setOnShowListener(d -> {
            DialogChrome.styleAlertDialog(dialog, activity);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {
                    int y = Integer.parseInt(year.getText().toString());
                    int m = Integer.parseInt(month.getText().toString());
                    int dayOfMonth = Integer.parseInt(day.getText().toString());
                    if (AdvertisingAge.isValidBirthDate(y, m, dayOfMonth,
                            Calendar.getInstance())) {
                        preferences.edit().putInt(KEY_YEAR, y).putInt(KEY_MONTH, m)
                                .putInt(KEY_DAY, dayOfMonth).remove(KEY_SKIPPED).apply();
                        dialog.dismiss();
                        onResolved.run();
                        return;
                    }
                } catch (NumberFormatException ignored) {
                    // Empty or incomplete input stays on the screen for correction.
                }
                year.setError(activity.getString(R.string.age_gate_invalid));
            });
        });
        dialog.show();
    }

    private boolean hasValidBirthDate() {
        return AdvertisingAge.isValidBirthDate(
                preferences.getInt(KEY_YEAR, 0), preferences.getInt(KEY_MONTH, 0),
                preferences.getInt(KEY_DAY, 0), Calendar.getInstance());
    }

    private void saveSkipped() {
        preferences.edit().putBoolean(KEY_SKIPPED, true)
                .remove(KEY_YEAR).remove(KEY_MONTH).remove(KEY_DAY).apply();
    }

    private static EditText numberField(Activity activity, int hint, int maxLength) {
        EditText field = new EditText(activity);
        field.setHint(hint);
        field.setInputType(InputType.TYPE_CLASS_NUMBER);
        field.setSingleLine(true);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        field.setTextColor(MyColor.getTitleTextColor(activity));
        field.setHintTextColor(MyColor.getDetailTextColor(activity));
        field.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return field;
    }
}
