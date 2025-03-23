/*
 * Copyright (C) 2023-2024 the risingOS Android Project
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
package com.android.settings.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceCategory

class AutoHidePreferenceCategory
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = android.R.attr.preferenceCategoryStyle,
) : PreferenceCategory(context, attrs, defStyleAttr) {

  private val childPreferenceChangeListener =
    Preference.OnPreferenceChangeListener { _, _ ->
      updateVisibility()
      true
    }

  override fun onAttached() {
    super.onAttached()
    updateVisibility()
    addListenersToChildren()
  }

  override fun onDetached() {
    super.onDetached()
    removeListenersFromChildren()
  }

  override fun addPreference(preference: Preference): Boolean {
    val added = super.addPreference(preference)
    if (added) {
      preference.onPreferenceChangeListener = childPreferenceChangeListener
      updateVisibility()
    }
    return added
  }

  override fun removePreference(preference: Preference): Boolean {
    val removed = super.removePreference(preference)
    if (removed) {
      preference.onPreferenceChangeListener = null
      updateVisibility()
    }
    return removed
  }

  private fun updateVisibility() {
    isVisible = preferenceCount > 0
  }

  private fun addListenersToChildren() {
    for (i in 0 until preferenceCount) {
      getPreference(i).onPreferenceChangeListener = childPreferenceChangeListener
    }
  }

  private fun removeListenersFromChildren() {
    for (i in 0 until preferenceCount) {
      getPreference(i).onPreferenceChangeListener = null
    }
  }
}
