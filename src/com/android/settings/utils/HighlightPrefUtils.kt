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

package com.android.settings.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import com.android.settingslib.widget.LayoutPreference

class HighlightPrefUtils {

  companion object {
    private const val PACKAGE_NAME = "com.android.settings"
    private const val ACTIVITY_PREFIX = ".Settings\$"

    fun setupHighlightPref(
      context: Context,
      layoutPreference: LayoutPreference,
      tiles: Map<Int, String>,
    ) {
      tiles.forEach { (viewId, activityName) ->
        val view = layoutPreference.findViewById<View>(viewId)
        view?.setOnClickListener { launchActivity(context, activityName) }
      }
    }

    private fun launchActivity(context: Context, activityName: String) {
      val intent =
        Intent()
          .setComponent(ComponentName(PACKAGE_NAME, "$PACKAGE_NAME$ACTIVITY_PREFIX$activityName"))
      context.startActivity(intent)
    }
  }
}
