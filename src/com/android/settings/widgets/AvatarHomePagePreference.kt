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
package com.android.settings.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.utils.UserUtils

/** A customized layout for homepage preference. */
class AvatarHomePagePreference
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0,
) :
  HomepagePreference(context, attrs, defStyleAttr, defStyleRes),
  HomepagePreferenceLayoutHelper.HomepagePreferenceLayout {

  private var avatarIcon: ImageView? = null
  private var userCard: View? = null
  private val userUtils: UserUtils = UserUtils.getInstance(context)

  private val handler = Handler()
  private val updateTileRunnable =
    object : Runnable {
      override fun run() {
        updateTile()
        handler.postDelayed(this, 1000)
      }
    }

  private var currentAvatarDrawable: Drawable? = null
  private var isUpdatesRunning = false

  init {
    setLayoutResource(R.layout.homepage_preference_user_v2)
    isVisible = true
  }

  private fun getSearchBarStyle(): Int {
    return Settings.System.getIntForUser(
      context.contentResolver,
      "search_bar_style",
      0,
      UserHandle.USER_CURRENT,
    )
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    if (!isVisible) return

    avatarIcon = holder.findViewById(R.id.user_avatar) as ImageView
    userCard = holder.findViewById(R.id.user_card_holder)

    avatarIcon?.let { userUtils.setClick(it) }
    userCard?.let { userUtils.setClick(it) }

    holder.itemView.post {
      val userName = userUtils.getUserName()
      if (userName != null && userName != title) {
        title = userName
      }
    }

    if (!isUpdatesRunning) {
      isUpdatesRunning = true
      handler.post(updateTileRunnable)
    }

    holder.itemView.setOnClickListener {
      val component =
        ComponentName("com.android.settings", "com.android.settings.Settings\$UserSettingsActivity")
      val intent = Intent().apply { this.component = component }
      context.startActivity(intent)
    }
  }

  override fun getHelper(): HomepagePreferenceLayoutHelper? {
    return null
  }

  private fun updateTile() {
    avatarIcon?.let {
      val newAvatarDrawable = userUtils.getCircularUserIcon()
      if (currentAvatarDrawable == null || currentAvatarDrawable != newAvatarDrawable) {
        it.setImageDrawable(newAvatarDrawable)
        currentAvatarDrawable = newAvatarDrawable
      }
    }

    userUtils.getUserName()?.let { userName ->
      if (userName != title) {
        title = userName
      }
    }
  }

  override fun onPrepareForRemoval() {
    super.onPrepareForRemoval()
    if (isUpdatesRunning) {
      isUpdatesRunning = false
      handler.removeCallbacks(updateTileRunnable)
    }
  }
}
