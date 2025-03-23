/*
 * Copyright (C) 2023-2024 The risingOS Android Project
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
package com.android.settings.utils;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.internal.util.android.Utils;
import com.android.settings.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PreferenceLayoutUtil {

    private static int extraPreferenceOrder = -153;

    private static final String PACKAGE_WELLBEING = "com.google.android.apps.wellbeing";
    private static final String PACKAGE_GOOGLE_SERVICES = "com.google.android.gms";

    private static final Set<String> topPreferences =
            new HashSet<>(
                    Arrays.asList(
                            "top_level_network",
                            "top_level_apps",
                            "top_level_accessibility",
                            "top_level_emergency",
                            "top_level_display"));

    private static final Set<String> middlePreferences =
            new HashSet<>(
                    Arrays.asList(
                            "top_level_battery",
                            "top_level_security",
                            "top_level_privacy",
                            "top_level_storage",
                            "top_level_notifications",
                            "top_level_communal",
                            "top_level_safety_center",
                            "top_level_accounts"));

    private static final Set<String> bottomPreferences =
            new HashSet<>(
                    Arrays.asList(
                            "top_level_connected_devices",
                            "top_level_sound",
                            "top_level_wallpaper",
                            "top_level_location"));

    private static final Set<String> EXCLUDE_LIST =
            new HashSet<>(Arrays.asList("top_level_crdroid"));

    public static void updateStartOrder(int startingOrder) {
        extraPreferenceOrder = startingOrder;
    }

    public static void setUpPreferenceLayout(Preference preference, Context context) {
        boolean showHomePageShowcase =
                Settings.System.getInt(
                                context.getContentResolver(), "settings_homepage_showcase", 0)
                        != 0;
        boolean showAvatarCard =
                Settings.System.getInt(
                                context.getContentResolver(), "show_avatar_card_on_homepage", 0)
                        != 0;
        String key = preference.getKey();
        if (EXCLUDE_LIST.contains(key)) {
            return;
        }
        boolean isWellbeingInstalled = Utils.isPackageInstalled(context, PACKAGE_WELLBEING);
        boolean isGoogleServiceInstalled =
                Utils.isPackageInstalled(context, PACKAGE_GOOGLE_SERVICES);
        switch (key) {
            case "top_level_wellbeing":
                if (isWellbeingInstalled) {
                    setPreferenceLayout(preference, context, "wellbeing", true);
                }
                break;
            case "top_level_google":
                if (isGoogleServiceInstalled) {
                    setPreferenceLayout(preference, context, "google", true);
                }
                break;
            case "top_level_system":
                setPreferenceLayout(
                        preference, context, showHomePageShowcase ? "bottom" : "middle", true);
                break;
            case "top_level_about_device":
                preference.setLayoutResource(
                        showHomePageShowcase
                                ? R.layout.top_level_preference_about
                                : AdaptivePreferenceUtils.getLayoutResourceId(
                                        context, "bottom", true));
                preference.setOrder(showHomePageShowcase ? -151 : 11);
                break;
            case "top_level_usercard":
                if (showAvatarCard) {
                    preference.setVisible(true);
                    preference.setLayoutResource(R.layout.top_level_usercard);
                    preference.setOrder(-152);
                } else {
                    preference.setVisible(false);
                }
                break;
            default:
                // Handle other preferences (e.g., OEM parts)
                if (topPreferences.contains(key)) {
                    setPreferenceLayout(preference, context, "top", true);
                    if (key.equals("top_level_display")) {
                        preference.setOrder(-150);
                    }
                } else if (middlePreferences.contains(key)) {
                    setPreferenceLayout(preference, context, "middle", true);
                } else if (bottomPreferences.contains(key)) {
                    setPreferenceLayout(preference, context, "bottom", true);
                    if (key.equals("top_level_wallpaper")) {
                        preference.setOrder(-140);
                    }
                } else {
                    int order = extraPreferenceOrder - 1;
                    updateStartOrder(order);
                    preference.setOrder(order);
                    setPreferenceLayout(preference, context, "solo", true);
                }
                break;
        }
    }

    private static void setPreferenceLayout(
            Preference preference, Context context, String layoutType, boolean useAdaptive) {
        preference.setLayoutResource(
                AdaptivePreferenceUtils.getLayoutResourceId(context, layoutType, useAdaptive));
    }
}
