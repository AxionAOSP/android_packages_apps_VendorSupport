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

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.widget.TextView
import com.android.settings.R

class SettingsSearchTextView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  TextView(context, attrs, defStyleAttr) {

  private val handler = Handler(Looper.getMainLooper())
  private var updateInterval = 15000L

  private val searchMessages = context.resources.getStringArray(R.array.settings_random)

  private val updateTextRunnable =
    object : Runnable {
      override fun run() {
        updateMessageBasedOnTime()
        handler.postDelayed(this, updateInterval)
      }
    }

  private val settingsObserver =
    object : ContentObserver(handler) {
      override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        updateMessageBasedOnTime()
      }
    }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    context.contentResolver.registerContentObserver(
      Settings.System.getUriFor("dashboard_greetings"),
      false,
      settingsObserver,
    )
    startUpdatingText()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    context.contentResolver.unregisterContentObserver(settingsObserver)
    stopUpdatingText()
  }

  private fun startUpdatingText() {
    handler.post(updateTextRunnable)
  }

  private fun stopUpdatingText() {
    handler.removeCallbacks(updateTextRunnable)
  }

  private fun updateMessageBasedOnTime() {
    val dashboardGreetings =
      Settings.System.getInt(context.contentResolver, "dashboard_greetings", 0)
    if (dashboardGreetings != 1) {
      text = context.getString(R.string.search_settings)
      return
    }
    val message = searchMessages.random()
    text = message
  }
}
