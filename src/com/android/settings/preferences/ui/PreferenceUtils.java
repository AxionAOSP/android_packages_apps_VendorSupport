/*
 * Copyright (C) 2023 The risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.preferences.ui;

import android.content.Context;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.fuelgauge.batteryusage.PowerGaugePreference;
import com.android.settings.preferences.*;
import com.android.settings.spa.preference.ComposePreference;
import com.android.settings.utils.AdaptivePreferenceUtils;
import com.android.settings.widget.CardPreference;
import com.android.settingslib.widget.FooterPreference;
import com.android.settingslib.widget.IllustrationPreference;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.widget.UsageProgressBarPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

    public static void setupExtraPreferences(
            List<String> topPrefs,
            List<String> middlePrefs,
            List<String> bottomPrefs,
            List<String> soloPrefs,
            PreferenceGroup screen,
            Context context) {
        setupExtraPreferences(
                topPrefs, middlePrefs, bottomPrefs, soloPrefs, screen, context, false);
    }

    public static void setupExtraPreferences(
            List<String> topPrefs,
            List<String> middlePrefs,
            List<String> bottomPrefs,
            List<String> soloPrefs,
            PreferenceGroup screen,
            Context context,
            boolean forceThemeMiddle) {
        if (screen == null
                || (topPrefs.isEmpty()
                        && middlePrefs.isEmpty()
                        && bottomPrefs.isEmpty()
                        && soloPrefs.isEmpty())) {
            return;
        }
        List<Preference> allPreferences = getAllPreferences(screen);
        for (Preference preference : allPreferences) {
            String key = preference.getKey();
            if (key != null) {
                int layoutResource =
                        getLayoutResourceForKey(
                                context,
                                key,
                                topPrefs,
                                middlePrefs,
                                bottomPrefs,
                                soloPrefs,
                                forceThemeMiddle);
                if (layoutResource != 0
                        && !getExcludedPrefClass().contains(preference.getClass())) {
                    preference.setLayoutResource(layoutResource);
                    int bottomLayout =
                            AdaptivePreferenceUtils.getLayoutResourceId(context, "bottom", false);
                    if (forceThemeMiddle
                            && layoutResource == bottomLayout
                            && bottomPrefs.size() == 1) {
                        preference.setOrder(1001);
                    }
                }
            }
        }
    }

    private static List<Class<?>> getExcludedPrefClass() {
        List<Class<?>> exclusionList =
                Arrays.asList(
                        CardPreference.class,
                        FooterPreference.class,
                        IllustrationPreference.class,
                        LayoutPreference.class,
                        PowerGaugePreference.class,
                        PreferenceCategory.class,
                        UsageProgressBarPreference.class,
                        ComposePreference.class,
                        CustomSeekBarPreference.class,
                        SystemSettingSeekBarPreference.class,
                        SecureSettingSeekBarPreference.class);
        return exclusionList;
    }

    private static int getLayoutResourceForKey(
            Context context,
            String key,
            List<String> topPrefs,
            List<String> middlePrefs,
            List<String> bottomPrefs,
            List<String> soloPrefs,
            boolean forceThemeMiddle) {
        if (!topPrefs.isEmpty() && topPrefs.contains(key)) {
            return AdaptivePreferenceUtils.getLayoutResourceId(context, "top", false);
        } else if (!middlePrefs.isEmpty() && middlePrefs.contains(key)) {
            return AdaptivePreferenceUtils.getLayoutResourceId(context, "middle", false);
        } else if (!bottomPrefs.isEmpty() && bottomPrefs.contains(key)) {
            return AdaptivePreferenceUtils.getLayoutResourceId(context, "bottom", false);
        } else if (!soloPrefs.isEmpty() && soloPrefs.contains(key)) {
            return AdaptivePreferenceUtils.getLayoutResourceId(context, "solo", false);
        } else if (forceThemeMiddle && middlePrefs.isEmpty()) {
            return AdaptivePreferenceUtils.getLayoutResourceId(context, "middle", false);
        } else {
            return 0;
        }
    }

    public static void setLayoutResources(Context context, List<Preference> preferences) {
        int minOrder = Integer.MAX_VALUE;
        int maxOrder = Integer.MIN_VALUE;

        for (Preference preference : preferences) {
            if (preference.isVisible()) {
                int order = preference.getOrder();
                if (order < minOrder) {
                    minOrder = order;
                }
                if (order > maxOrder) {
                    maxOrder = order;
                }
            }
        }

        for (Preference preference : preferences) {
            if (preference.isVisible() && !getExcludedPrefClass().contains(preference.getClass())) {
                int order = preference.getOrder();
                if (order == minOrder) {
                    preference.setLayoutResource(
                            AdaptivePreferenceUtils.getLayoutResourceId(context, "top", false));
                } else if (order == maxOrder) {
                    preference.setLayoutResource(
                            AdaptivePreferenceUtils.getLayoutResourceId(context, "bottom", false));
                } else {
                    preference.setLayoutResource(
                            AdaptivePreferenceUtils.getLayoutResourceId(context, "middle", false));
                }
            }
        }
    }

    private static void logPreferenceKey(Preference preference) {
        String key = preference.getKey();
        if (key != null) {
            Log.d(TAG, "Preference Key: " + key);
        }
    }

    public static List<Preference> getAllPreferences(PreferenceGroup preferenceGroup) {
        List<Preference> preferences = new ArrayList<>();
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference.isVisible()) {
                preferences.add(preference);
                if (preference instanceof PreferenceGroup) {
                    preferences.addAll(getAllPreferences((PreferenceGroup) preference));
                }
                logPreferenceKey(preference);
            }
        }
        return preferences;
    }

    public static void hideEmptyCategory(PreferenceCategory category, PreferenceScreen screen) {
        if (category != null && category.getPreferenceCount() == 0) {
            screen.removePreference(category);
        }
    }
}
