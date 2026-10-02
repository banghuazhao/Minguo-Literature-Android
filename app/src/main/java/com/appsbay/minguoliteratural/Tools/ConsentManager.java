package com.appsbay.minguoliteratural.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.appsbay.minguoliteratural.BuildConfig;
import com.appsbay.minguoliteratural.Controller.MyApplication;
import com.appsbay.minguoliteratural.R;
import com.google.android.gms.ads.AdView;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** The single gate for requesting ads in this app process. */
public final class ConsentManager {
    private static final String PREFS = "Advertising Privacy";
    private static final String KEY_ADS_CHOICE = "allowAdvertisingDataUse";
    private static final String TCF_PURPOSE_CONSENTS = "IABTCF_PurposeConsents";
    private static final String TCF_VENDOR_CONSENTS = "IABTCF_VendorConsents";
    private static final int GOOGLE_TCF_VENDOR_ID = 755;
    private static ConsentManager instance;

    private final MyApplication application;
    @Nullable
    private ConsentInformation consentInformation;
    private final SharedPreferences preferences;
    private boolean requestInFlight;
    private int requestGeneration;
    private volatile boolean requestCompleted;
    private volatile boolean adsAllowed;
    private Runnable pendingCallback;

    public static synchronized ConsentManager get(@NonNull Context context) {
        if (instance == null) {
            instance = new ConsentManager((MyApplication) context.getApplicationContext());
        }
        return instance;
    }

    private ConsentManager(MyApplication application) {
        this.application = application;
        preferences = application.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean hasCompletedRequest() {
        return requestCompleted;
    }

    public boolean canRequestAds() {
        return adsAllowed && new AgeGate(application).mayRequestAds()
                && consentInformation != null && consentInformation.canRequestAds();
    }

    public void request(@NonNull Activity activity, @NonNull Runnable onComplete) {
        boolean ageEligible = new AgeGate(application).mayRequestAds();
        if (BuildConfig.BUILD_TYPE.startsWith("privacy")) {
            Log.d("ConsentQA", "Age eligible for ads: " + ageEligible);
        }
        if (!ageEligible) {
            requestGeneration++;
            requestInFlight = false;
            pendingCallback = null;
            adsAllowed = false;
            requestCompleted = true;
            onComplete.run();
            return;
        }
        if (requestCompleted) {
            onComplete.run();
            return;
        }
        pendingCallback = onComplete;
        if (requestInFlight) {
            return;
        }
        requestInFlight = true;
        int generation = ++requestGeneration;
        if (consentInformation == null) {
            consentInformation = UserMessagingPlatform.getConsentInformation(application);
        }
        ConsentRequestParameters.Builder parametersBuilder = new ConsentRequestParameters.Builder();
        if (BuildConfig.BUILD_TYPE.startsWith("privacy")
                && !"NONE".equals(BuildConfig.CONSENT_TEST_GEOGRAPHY)) {
            int geography;
            switch (BuildConfig.CONSENT_TEST_GEOGRAPHY) {
                case "US":
                    geography = ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE;
                    break;
                case "EEA":
                    geography = ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA;
                    break;
                default:
                    geography = ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER;
                    break;
            }
            ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(geography)
                    .build();
            parametersBuilder.setConsentDebugSettings(debugSettings);
        }
        ConsentRequestParameters parameters = parametersBuilder.build();
        consentInformation.requestConsentInfoUpdate(activity, parameters,
                () -> {
                    if (!isActiveRequest(generation)) {
                        return;
                    }
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                        formError -> {
                            if (!isActiveRequest(generation)) {
                                return;
                            }
                            if (BuildConfig.BUILD_TYPE.startsWith("privacy")) {
                                Log.d("ConsentQA", "UMP form result: "
                                        + (formError == null ? "ok"
                                        : "error " + formError.getErrorCode())
                                        + ", canRequestAds=" + consentInformation.canRequestAds()
                                        + ", status=" + consentInformation.getConsentStatus());
                            }
                            if (formError != null || !consentInformation.canRequestAds()) {
                                finishRequest(false);
                            } else if (isOutsideEuropeanConsentFlow()) {
                                if (preferences.contains(KEY_ADS_CHOICE)) {
                                    finishRequest(true);
                                } else {
                                    showAdsChoice(activity, () -> {
                                        if (isActiveRequest(generation)) {
                                            finishRequest(true);
                                        }
                                    });
                                }
                            } else {
                                finishRequest(true);
                            }
                        });
                },
                requestError -> {
                    if (!isActiveRequest(generation)) {
                        return;
                    }
                    if (BuildConfig.BUILD_TYPE.startsWith("privacy")) {
                        Log.d("ConsentQA", "UMP update error: "
                                + requestError.getErrorCode());
                    }
                    finishRequest(false);
                });
    }

    private boolean isActiveRequest(int generation) {
        return requestInFlight && generation == requestGeneration
                && new AgeGate(application).mayRequestAds();
    }

    private void finishRequest(boolean succeeded) {
        requestInFlight = false;
        requestCompleted = true;
        boolean ownChoiceAllows = isOutsideEuropeanConsentFlow()
                ? preferences.getBoolean(KEY_ADS_CHOICE, false)
                : !preferences.contains(KEY_ADS_CHOICE)
                || preferences.getBoolean(KEY_ADS_CHOICE, false);
        adsAllowed = succeeded && new AgeGate(application).mayRequestAds()
                && consentInformation.canRequestAds() && ownChoiceAllows
                && hasAdvertisingConsent();
        if (adsAllowed) {
            application.startMobileAds();
        }
        Runnable callback = pendingCallback;
        pendingCallback = null;
        if (callback != null) {
            callback.run();
        }
    }

    public boolean isPrivacyOptionsRequired() {
        if (!requestCompleted || !new AgeGate(application).mayRequestAds()) {
            return false;
        }
        return consentInformation == null || isOutsideEuropeanConsentFlow()
                || consentInformation.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void onAgeChoiceChanged(@NonNull Activity activity) {
        requestGeneration++;
        requestInFlight = false;
        pendingCallback = null;
        adsAllowed = false;
        pauseVisibleBanners(activity.getWindow().getDecorView());
        AdCoordinator.get(application).clearPreloaded();
        RewardedAdHelper.get(application).clearPreloaded();
        application.clearPreloadedAppOpenAd();
        requestCompleted = false;
        request(activity, () -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.recreate();
            }
        });
    }

    public void showPrivacyOptions(@NonNull Activity activity) {
        if (!requestCompleted || !isPrivacyOptionsRequired()) {
            return;
        }
        adsAllowed = false;
        pauseVisibleBanners(activity.getWindow().getDecorView());
        AdCoordinator.get(application).clearPreloaded();
        RewardedAdHelper.get(application).clearPreloaded();
        application.clearPreloadedAppOpenAd();
        if (isOutsideEuropeanConsentFlow()) {
            showAdsChoice(activity, () -> {
                adsAllowed = new AgeGate(application).mayRequestAds()
                        && consentInformation.canRequestAds()
                        && preferences.getBoolean(KEY_ADS_CHOICE, false);
                if (adsAllowed) {
                    application.startMobileAds();
                }
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    activity.recreate();
                }
            });
            return;
        }
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
            adsAllowed = formError == null && new AgeGate(application).mayRequestAds()
                    && consentInformation.canRequestAds()
                    && (!preferences.contains(KEY_ADS_CHOICE)
                    || preferences.getBoolean(KEY_ADS_CHOICE, false))
                    && hasAdvertisingConsent();
            if (adsAllowed) {
                application.startMobileAds();
            }
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.recreate();
            }
        });
    }

    private boolean isOutsideEuropeanConsentFlow() {
        return consentInformation != null && consentInformation.getConsentStatus()
                == ConsentInformation.ConsentStatus.NOT_REQUIRED;
    }

    private boolean hasAdvertisingConsent() {
        if (isOutsideEuropeanConsentFlow()) {
            return true;
        }
        // UMP canRequestAds() also permits limited ads after a rejection. This app
        // initializes the SDK only after affirmative TCF advertising consent.
        SharedPreferences tcf = PreferenceManager.getDefaultSharedPreferences(application);
        String purposes = tcf.getString(TCF_PURPOSE_CONSENTS, "");
        String vendors = tcf.getString(TCF_VENDOR_CONSENTS, "");
        return allConsented(purposes) && hasConsent(vendors, GOOGLE_TCF_VENDOR_ID);
    }

    private static boolean allConsented(String bits) {
        if (bits.isEmpty()) {
            return false;
        }
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) != '1') {
                return false;
            }
        }
        return true;
    }

    private static boolean hasConsent(String bits, int oneBasedId) {
        int index = oneBasedId - 1;
        return bits.length() > index && bits.charAt(index) == '1';
    }

    private void showAdsChoice(@NonNull Activity activity, @NonNull Runnable onSelected) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            saveAdsChoice(false);
            onSelected.run();
            return;
        }
        SpannableStringBuilder message = new SpannableStringBuilder(
                activity.getString(R.string.ads_privacy_message));
        message.append("\n\n");
        int linkStart = message.length();
        message.append(activity.getString(R.string.ads_privacy_policy_link));
        message.setSpan(new URLSpan(activity.getString(R.string.privacy_policy_url)),
                linkStart, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        AlertDialog dialog = DialogChrome.alert(activity)
                .setTitle(R.string.ads_privacy_title)
                .setMessage(message)
                .setPositiveButton(R.string.ads_privacy_no_ads, (d, which) -> {
                    saveAdsChoice(false);
                    onSelected.run();
                })
                .setNegativeButton(R.string.ads_privacy_allow, (d, which) -> {
                    saveAdsChoice(true);
                    onSelected.run();
                })
                .setOnCancelListener(d -> {
                    saveAdsChoice(false);
                    onSelected.run();
                })
                .create();
        dialog.setOnShowListener(d -> {
            DialogChrome.styleAlertDialog(dialog, activity);
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    private void saveAdsChoice(boolean allowed) {
        preferences.edit().putBoolean(KEY_ADS_CHOICE, allowed).commit();
        SharedPreferences.Editor rdpEditor = PreferenceManager
                .getDefaultSharedPreferences(application).edit();
        if (allowed) {
            rdpEditor.remove("gad_rdp");
        } else {
            rdpEditor.putInt("gad_rdp", 1);
        }
        rdpEditor.commit();
    }

    private void pauseVisibleBanners(View view) {
        if (view instanceof AdView) {
            ((AdView) view).pause();
            view.setVisibility(View.GONE);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                pauseVisibleBanners(group.getChildAt(i));
            }
        }
    }
}
