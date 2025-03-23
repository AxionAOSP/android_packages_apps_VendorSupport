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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.preference.internal.PreferenceImageView;

import com.android.settings.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DynamicIconPreferenceView extends PreferenceImageView {

    private int maxWidth = dpToPx(getContext(), 52);
    private int maxHeight = dpToPx(getContext(), 52);

    private String[] colorMap = {
        "#007aff", "#2fb151", "#fb7c47",
        "#fa7d4d", "#fbb404", "#e13e39"
    };

    private Map<String, StyleAttributes> styleMap;

    public DynamicIconPreferenceView(Context context) {
        this(context, null);
    }

    public DynamicIconPreferenceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicIconPreferenceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeStyleMap(context);
    }

    private void initializeStyleMap(Context context) {
        styleMap = new HashMap<>();
        Resources resources = context.getResources();

        styleMap.put(
                "0",
                new StyleAttributes(
                        dpToPx(context, 6),
                        dpToPx(context, 40),
                        dpToPx(context, 40),
                        null,
                        R.color.top_level_preference_text_color_primary,
                        null,
                        ImageView.ScaleType.CENTER_INSIDE));
        styleMap.put(
                "1",
                new StyleAttributes(
                        resources.getDimensionPixelSize(R.dimen.top_level_icon_padding),
                        dpToPx(context, 48),
                        dpToPx(context, 48),
                        R.drawable.custom_surface_color,
                        R.color.top_level_preference_icon_tint,
                        null,
                        null));
        styleMap.put(
                "2",
                new StyleAttributes(
                        resources.getDimensionPixelSize(R.dimen.top_level_icon_padding),
                        dpToPx(context, 48),
                        dpToPx(context, 48),
                        R.drawable.custom_surface_color_rounded,
                        R.color.top_level_preference_icon_tint,
                        null,
                        null));
        styleMap.put(
                "3",
                new StyleAttributes(
                        resources.getDimensionPixelSize(R.dimen.top_level_icon_padding),
                        dpToPx(context, 48),
                        dpToPx(context, 48),
                        R.drawable.custom_surface_color_oos,
                        R.color.top_level_preference_text_color_primary,
                        null,
                        null));
        styleMap.put(
                "4",
                new StyleAttributes(
                        resources.getDimensionPixelSize(R.dimen.top_level_icon_padding),
                        dpToPx(context, 48),
                        dpToPx(context, 48),
                        R.drawable.custom_surface_color_rounded,
                        Color.WHITE,
                        getRandomColor(),
                        null));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateIconStyle();
    }

    private void updateIconStyle() {
        String styleValue = String.valueOf(getSettingsIconStyle());
        StyleAttributes attributes = styleMap.get(styleValue);
        if (attributes != null) {
            applyStyle(attributes);
        }
    }

    private int getSettingsIconStyle() {
        return Settings.System.getIntForUser(
                getContext().getContentResolver(),
                "settings_icon_style",
                0,
                android.os.UserHandle.USER_CURRENT);
    }

    private void applyStyle(StyleAttributes attributes) {
        setPadding(attributes.padding, attributes.padding, attributes.padding, attributes.padding);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = attributes.width;
        layoutParams.height = attributes.height;
        setLayoutParams(layoutParams);

        if (attributes.background != null) {
            Drawable background = ContextCompat.getDrawable(getContext(), attributes.background);
            setBackground(background);
        }

        if (attributes.tint instanceof Integer) {
            int color = (Integer) attributes.tint;
            try {
                setImageTintList(ContextCompat.getColorStateList(getContext(), color));
            } catch (Resources.NotFoundException e) {
                setImageTintList(ColorStateList.valueOf(color));
            }
        }
        if (attributes.bgTint != null) {
            int bgColor = (Integer) attributes.bgTint;
            setBackgroundTintList(ColorStateList.valueOf(bgColor));
        }
    }

    private int getRandomColor() {
        Random random = new Random();
        String colorString = colorMap[random.nextInt(colorMap.length)];
        return Color.parseColor(colorString);
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private static class StyleAttributes {
        final int padding;
        final int width;
        final int height;
        final Integer background;
        final Object tint;
        final Object bgTint;
        final ImageView.ScaleType scaleType;

        StyleAttributes(
                int padding,
                int width,
                int height,
                Integer background,
                Object tint,
                Object bgTint,
                ImageView.ScaleType scaleType) {
            this.padding = padding;
            this.width = width;
            this.height = height;
            this.background = background;
            this.tint = tint;
            this.bgTint = bgTint;
            this.scaleType = scaleType;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) {
            widthSize = Math.min(widthSize, maxWidth);
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.EXACTLY) {
            heightSize = Math.min(heightSize, maxHeight);
        }
        setMeasuredDimension(widthSize, heightSize);
    }
}
