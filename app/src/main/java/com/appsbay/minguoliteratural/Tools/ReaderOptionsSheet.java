package com.appsbay.minguoliteratural.Tools;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.appsbay.minguoliteratural.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

/**
 * Material bottom sheets for reader overflow actions and font size.
 */
public final class ReaderOptionsSheet {

    public interface MenuCallbacks {
        void onFontSizeSelected();

        void onBackground();

        void onShare();

        void onReadAloudSelected();
    }

    public interface FontSizeCallbacks {
        int currentFontSize();

        void onFontSizeChanged(int size);
    }

    private ReaderOptionsSheet() {
    }

    public static void showMenu(@NonNull Activity activity, @NonNull MenuCallbacks callbacks) {
        BottomSheetDialog dialog = DialogChrome.bottomSheet(activity);
        View content = LayoutInflater.from(activity).inflate(R.layout.sheet_reader_options, null, false);

        View handle = content.findViewById(R.id.sheet_handle);
        TextView title = content.findViewById(R.id.sheet_title);
        MaterialButton font = content.findViewById(R.id.sheet_action_font);
        MaterialButton background = content.findViewById(R.id.sheet_action_background);
        MaterialButton share = content.findViewById(R.id.sheet_action_share);
        MaterialButton tts = content.findViewById(R.id.sheet_action_tts);

        DialogChrome.tintSheetHandle(handle, activity);
        title.setTextColor(MyColor.getAccentColor(activity));
        DialogChrome.tintSheetAction(font, activity);
        DialogChrome.tintSheetAction(background, activity);
        DialogChrome.tintSheetAction(share, activity);
        DialogChrome.tintSheetAction(tts, activity);

        font.setOnClickListener(v -> {
            dialog.dismiss();
            callbacks.onFontSizeSelected();
        });
        background.setOnClickListener(v -> {
            dialog.dismiss();
            callbacks.onBackground();
        });
        share.setOnClickListener(v -> {
            dialog.dismiss();
            callbacks.onShare();
        });
        tts.setOnClickListener(v -> {
            dialog.dismiss();
            callbacks.onReadAloudSelected();
        });

        DialogChrome.prepareSheet(dialog, content, activity);
        dialog.show();
    }

    public static void showSpeech(@NonNull Activity activity,
                                  @NonNull ReaderSpeechController speech,
                                  @NonNull String chapterText) {
        BottomSheetDialog dialog = DialogChrome.bottomSheet(activity);
        View content = LayoutInflater.from(activity).inflate(R.layout.sheet_reader_speech, null, false);
        View handle = content.findViewById(R.id.sheet_handle);
        TextView title = content.findViewById(R.id.speech_title);
        TextView status = content.findViewById(R.id.speech_status);
        TextView rateLabel = content.findViewById(R.id.speech_rate_label);
        Spinner voice = content.findViewById(R.id.speech_voice);
        Slider rate = content.findViewById(R.id.speech_rate);
        MaterialButton toggle = content.findViewById(R.id.speech_toggle);
        MaterialButton stop = content.findViewById(R.id.speech_stop);

        DialogChrome.tintSheetHandle(handle, activity);
        title.setTextColor(MyColor.getAccentColor(activity));
        status.setTextColor(MyColor.getDetailTextColor(activity));
        rateLabel.setTextColor(MyColor.getTitleTextColor(activity));
        DialogChrome.tintSheetAction(toggle, activity);
        DialogChrome.tintSheetAction(stop, activity);
        rate.setValue(speech.getRate());
        rateLabel.setText(activity.getString(R.string.tts_speed_value, speech.getRate()));
        rate.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) speech.setRate(value);
            rateLabel.setText(activity.getString(R.string.tts_speed_value, value));
        });

        String[] voices = {
                activity.getString(R.string.tts_voice_auto),
                activity.getString(R.string.tts_voice_mainland),
                activity.getString(R.string.tts_voice_taiwan),
                activity.getString(R.string.tts_voice_hong_kong)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, voices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voice.setAdapter(adapter);
        voice.setSelection(speech.getVoiceIndex());
        voice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                  int position, long id) {
                if (position != speech.getVoiceIndex()) speech.setVoiceIndex(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        Runnable refresh = () -> {
            switch (speech.getState()) {
                case INITIALIZING:
                    status.setText(R.string.tts_initializing);
                    toggle.setText(R.string.tts_start);
                    toggle.setEnabled(false);
                    stop.setEnabled(true);
                    break;
                case PLAYING:
                    status.setText(R.string.tts_reading);
                    toggle.setText(R.string.tts_pause);
                    toggle.setEnabled(true);
                    stop.setEnabled(true);
                    break;
                case PAUSED:
                    status.setText(R.string.tts_paused);
                    toggle.setText(R.string.tts_resume);
                    toggle.setEnabled(true);
                    stop.setEnabled(true);
                    break;
                case ERROR:
                    status.setText(speech.getErrorRes());
                    toggle.setText(R.string.tts_start);
                    toggle.setEnabled(true);
                    stop.setEnabled(false);
                    break;
                case IDLE:
                default:
                    status.setText(R.string.tts_ready);
                    toggle.setText(R.string.tts_start);
                    toggle.setEnabled(true);
                    stop.setEnabled(false);
                    break;
            }
        };
        speech.setListener(refresh);
        toggle.setOnClickListener(v -> {
            switch (speech.getState()) {
                case PLAYING: speech.pause(); break;
                case PAUSED: speech.resume(); break;
                case IDLE:
                case ERROR: speech.start(chapterText); break;
                default: break;
            }
        });
        stop.setOnClickListener(v -> speech.stop());
        dialog.setOnDismissListener(v -> speech.setListener(null));
        DialogChrome.prepareSheet(dialog, content, activity);
        dialog.show();
    }

    public static void showFontSize(@NonNull Activity activity, @NonNull FontSizeCallbacks callbacks) {
        BottomSheetDialog dialog = DialogChrome.bottomSheet(activity);
        View content = LayoutInflater.from(activity).inflate(R.layout.sheet_font_size, null, false);

        View handle = content.findViewById(R.id.sheet_handle);
        TextView value = content.findViewById(R.id.sheet_font_size_value);
        Slider slider = content.findViewById(R.id.sheet_font_size_slider);

        DialogChrome.tintSheetHandle(handle, activity);
        int size = Math.max(12, Math.min(40, callbacks.currentFontSize()));
        value.setText(String.valueOf(size));
        value.setTextColor(MyColor.getTitleTextColor(activity));
        slider.setValue(size);
        slider.setHaloTintList(android.content.res.ColorStateList.valueOf(MyColor.getSeparatorColor(activity)));
        slider.setThumbTintList(android.content.res.ColorStateList.valueOf(MyColor.getAccentColor(activity)));
        slider.setTrackActiveTintList(android.content.res.ColorStateList.valueOf(MyColor.getAccentColor(activity)));
        slider.setTrackInactiveTintList(android.content.res.ColorStateList.valueOf(MyColor.getSeparatorColor(activity)));

        slider.addOnChangeListener((s, newValue, fromUser) -> {
            int next = Math.round(newValue);
            value.setText(String.valueOf(next));
            if (fromUser) {
                callbacks.onFontSizeChanged(next);
            }
        });

        DialogChrome.prepareSheet(dialog, content, activity);
        dialog.show();
    }
}
