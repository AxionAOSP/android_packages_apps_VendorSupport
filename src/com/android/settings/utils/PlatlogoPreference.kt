/*
 * Copyright (C) 2023-2025 The risingOS Android Project
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

import android.content.Context
import android.content.Intent
import android.os.SystemProperties
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.Preference

class PlatlogoPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
  private var clickCount = 0
  private var lastClickTime = 0L
  private var currentToast: Toast? = null

  init {
    val lineageVersion = SystemProperties.get("ro.lineage.version", "Unknown Version")
    summary = "$lineageVersion"

    onPreferenceClickListener =
      Preference.OnPreferenceClickListener {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime > 1500) {
          clickCount = 0
        }

        clickCount++
        lastClickTime = currentTime

        showToastForTapCount(clickCount)

        if (clickCount == 6) {
          showToast("Welcome to RisingOS!")
          val intent = Intent(context, PlatLogoActivity::class.java)
          context.startActivity(intent)
          clickCount = 0
        }

        true
      }
  }

  private fun showToastForTapCount(count: Int) {
    val message =
      when (count) {
        1 -> "Curiosity killed the cat."
        2 -> "Congratulations, you are now a developer. You can stop now."
        3 -> "Increasing FPS in games... Boop, okay, please stop now."
        4 -> "Are you an engineer performing a stress test?"
        5 -> "Okay."
        else -> null
      }
    message?.let {
      currentToast?.cancel()
      showToast(it)
    }
  }

  private fun showToast(message: String) {
    currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
    currentToast?.show()
  }
}
