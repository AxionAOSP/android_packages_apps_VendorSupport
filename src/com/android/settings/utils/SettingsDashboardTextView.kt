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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsDashboardTextView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  TextView(context, attrs, defStyleAttr) {

  private val handler = Handler(Looper.getMainLooper())
  private var updateInterval = 15000L

  private val midnightMessages = context.resources.getStringArray(R.array.dashboard_midnight)
  private val morningMessages = context.resources.getStringArray(R.array.dashboard_morning)
  private val randomMessages = context.resources.getStringArray(R.array.dashboard_random)
  private val noonMessages = context.resources.getStringArray(R.array.dashboard_noon)
  private val earlyNightMessages = context.resources.getStringArray(R.array.dashboard_early_night)
  private val nightMessages = context.resources.getStringArray(R.array.dashboard_night)

  private val timeFormat = SimpleDateFormat("HH", Locale.getDefault())

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
      text = context.getString(R.string.dashboard_title)
      return
    }
    val username = UserUtils.getInstance(context).getUserName()
    val currentHour = timeFormat.format(Calendar.getInstance().time).toInt()
    val message =
      when (currentHour) {
        in 0..4 -> midnightMessages.random()
        in 5..11 -> morningMessages.random()
        in 12..13 -> noonMessages.random()
        in 14..16 -> randomMessages.random()
        in 17..20 -> earlyNightMessages.random()
        else -> nightMessages.random()
      }
    text =
      if (message.contains("%s")) {
        String.format(message, username)
      } else {
        message
      }
  }
}
