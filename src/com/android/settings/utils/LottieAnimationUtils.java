/*
 * Copyright (C) 2025 AxionAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.utils;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.android.settings.R;

import java.util.HashMap;
import java.util.Map;

public abstract class LottieAnimationUtils {

    private static final HashMap<String, Integer> colors = new HashMap<>();

    static {
        int color = R.color.ic_device_name_storage_rectangle_color;
        colors.put("wave path 1", color);
        colors.put("wave path 2 opacity0.5", color);
    }

    public static void applyAnimationColor(Context context, LottieAnimationView lottieAnimationView) {
        if (context == null) return;
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            String keyPath = entry.getKey();
            final int color = context.getColor(entry.getValue());
            if (lottieAnimationView != null) {
                lottieAnimationView.addValueCallback(
                    new KeyPath("**", keyPath, "**"),
                    LottieProperty.COLOR_FILTER,
                    new SimpleLottieValueCallback<ColorFilter>() {
                        @Override
                        public ColorFilter getValue(LottieFrameInfo<ColorFilter> frameInfo) {
                            return new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                );
            }
        }
    }
}
