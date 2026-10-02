package com.appsbay.minguoliteratural.Controller.Menu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsbay.minguoliteratural.R;
import com.appsbay.minguoliteratural.Tools.AdsHelper;
import com.appsbay.minguoliteratural.Tools.AgeGate;
import com.appsbay.minguoliteratural.Tools.BillingManager;
import com.appsbay.minguoliteratural.Tools.ConsentManager;
import com.appsbay.minguoliteratural.Tools.DialogChrome;
import com.appsbay.minguoliteratural.Tools.LocalBroadcastHelper;
import com.appsbay.minguoliteratural.Tools.LocaleHelper;
import com.appsbay.minguoliteratural.Tools.MyColor;
import com.appsbay.minguoliteratural.Tools.MyImage;
import com.appsbay.minguoliteratural.Tools.RewardedAdHelper;
import com.appsbay.minguoliteratural.Tools.ScreenChrome;
import com.appsbay.minguoliteratural.Tools.TemporaryAdFree;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MenuFragment extends Fragment {

    private final ArrayList<MenuItem> menuItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private MenuItemRecyclerViewAdapter adapter;
    private AdView mAdView;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        mContext = requireContext();

        mAdView = view.findViewById(R.id.adViewBanner);
        AdsHelper.bindBanner(mAdView);

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        buildMenuItems();
        adapter = new MenuItemRecyclerViewAdapter(mContext, menuItems);
        recyclerView = view.findViewById(R.id.recycler_view_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        BillingManager.get(mContext).setPurchaseListener(new BillingManager.PurchaseListener() {
            @Override
            public void onPurchaseCompleted(boolean restored) {
                View anchor = recyclerView != null ? recyclerView : view;
                DialogChrome.snack(anchor,
                        getString(restored ? R.string.purchase_restored : R.string.ad_free_active));
                refreshAfterAdFreeChange();
            }

            @Override
            public void onPurchaseFailed(String message) {
                if (message == null || message.isEmpty()) {
                    return;
                }
                View anchor = recyclerView != null ? recyclerView : view;
                DialogChrome.snack(anchor, message);
            }

            @Override
            public void onProductsUpdated() {
                buildMenuItems();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });

        LocalBroadcastManager.getInstance(mContext).registerReceiver(adFreeReceiver,
                new IntentFilter(LocalBroadcastHelper.ACTION_AD_FREE_CHANGED));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(backgroundReceiver,
                new IntentFilter("NotificationBackgroundChange"));

        configColor();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        buildMenuItems();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        configColor();
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(adFreeReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(backgroundReceiver);
        BillingManager.get(mContext).setPurchaseListener(null);
        super.onDestroyView();
    }

    private void buildMenuItems() {
        menuItems.clear();
        int tint = MyColor.getButtonTintColor(mContext);
        menuItems.add(new MenuItem(MenuItem.ACTION_BACKGROUND,
                getString(R.string.Background),
                MyImage.changeDrawableColor(mContext, R.drawable.nav_sun, tint)));
        menuItems.add(new MenuItem(MenuItem.ACTION_LANGUAGE,
                getString(R.string.Language) + " · " + LocaleHelper.getSelectedLabel(mContext),
                MyImage.changeDrawableColor(mContext, R.drawable.ic_language, tint)));
        int bookLanguage = mContext.getSharedPreferences("Language Preference", Context.MODE_PRIVATE)
                .getInt("language", 0);
        menuItems.add(new MenuItem(MenuItem.ACTION_BOOK_LANGUAGE,
                getString(R.string.book_language) + " · "
                        + (bookLanguage == 0 ? "简体中文" : "繁體中文"),
                MyImage.changeDrawableColor(mContext, R.drawable.nav_language, tint)));
        if (ConsentManager.get(mContext).hasCompletedRequest()) {
            menuItems.add(new MenuItem(MenuItem.ACTION_ADVERTISING_AGE,
                    getString(R.string.age_gate_settings),
                    MyImage.changeDrawableColor(mContext, R.drawable.nav_bookmark_circle, tint)));
        }
        if (ConsentManager.get(mContext).isPrivacyOptionsRequired()) {
            menuItems.add(new MenuItem(MenuItem.ACTION_PRIVACY,
                    getString(R.string.privacy_settings),
                    MyImage.changeDrawableColor(mContext, R.drawable.nav_bookmark_circle, tint)));
        }
        menuItems.add(new MenuItem(MenuItem.ACTION_PRIVACY_POLICY,
                getString(R.string.privacy_policy),
                MyImage.changeDrawableColor(mContext, R.drawable.icon_right_arrow, tint)));
        menuItems.add(new MenuItem(MenuItem.ACTION_FEEDBACK,
                getString(R.string.Feedback),
                MyImage.changeDrawableColor(mContext, R.drawable.icon_feedback, tint)));
        menuItems.add(new MenuItem(MenuItem.ACTION_RATE,
                getString(R.string.Rate),
                MyImage.changeDrawableColor(mContext, R.drawable.icon_rate, tint)));
        menuItems.add(new MenuItem(MenuItem.ACTION_SHARE,
                getString(R.string.Share),
                MyImage.changeDrawableColor(mContext, R.drawable.icon_share2, tint)));
        if (new AgeGate(mContext).mayRequestAds()
                && (BillingManager.get(mContext).getRemoveAdsPrice() != null
                || BillingManager.get(mContext).isAdFree())) {
            menuItems.add(new MenuItem(MenuItem.ACTION_REMOVE_ADS,
                    BillingManager.get(mContext).getRemoveAdsTitle(mContext),
                    MyImage.changeDrawableColor(mContext, R.drawable.nav_bookmark, tint)));
        }
        if (!BillingManager.get(mContext).isAdFree()) {
            if (AdsHelper.shouldShowAds(mContext)
                    && !getString(R.string.adRewardedID).isEmpty()) {
                String watchTitle = TemporaryAdFree.isActive(mContext)
                        ? getString(R.string.temp_ad_free_active)
                        : getString(R.string.watch_ad_for_24h);
                menuItems.add(new MenuItem(MenuItem.ACTION_WATCH_AD_FREE,
                        watchTitle,
                        MyImage.changeDrawableColor(mContext, R.drawable.nav_speaker, tint)));
                RewardedAdHelper.get(mContext).preload();
            }
            menuItems.add(new MenuItem(MenuItem.ACTION_RESTORE,
                    getString(R.string.restore_purchases),
                    MyImage.changeDrawableColor(mContext, R.drawable.nav_bookmark_circle, tint)));
        }
        menuItems.add(new MenuItem(MenuItem.ACTION_MORE_APPS,
                getString(R.string.MoreApps),
                MyImage.changeDrawableColor(mContext, R.drawable.ic_tab_more, tint)));
    }

    private void refreshAfterAdFreeChange() {
        buildMenuItems();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        AdsHelper.bindBanner(mAdView);
    }

    private final BroadcastReceiver adFreeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshAfterAdFreeChange();
        }
    };

    private final BroadcastReceiver backgroundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            configColor();
            if (adapter != null) {
                buildMenuItems();
                adapter.notifyDataSetChanged();
            }
        }
    };

    private void configColor() {
        if (recyclerView == null || mContext == null) {
            return;
        }
        recyclerView.setBackgroundColor(MyColor.getBackgroundColor(mContext));
        MyImage.setBackgroundImage(mContext, recyclerView);
        ScreenChrome.tintHomeChrome((AppCompatActivity) requireActivity());
        BottomNavigationView navigation = requireActivity().findViewById(R.id.bottom_navigation_main);
        MyColor.applyBottomNavigation(mContext, navigation);
    }

    public void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }
}
