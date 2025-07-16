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
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.Toast;

import com.android.settings.R;

public class AdaptivePreferenceUtils {

    public static boolean isLineageSettings(Context context, AttributeSet attrs) {
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.AdaptivePreference);
        boolean isLineage =
                typedArray.getBoolean(R.styleable.AdaptivePreference_isLineageSettings, false);
        typedArray.recycle();
        return isLineage;
    }
}
