/*
 * Copyright (C) 2024 The risingOS Android Project
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
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import kotlinx.coroutines.*

class AvatarImageView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  ImageView(context, attrs, defStyleAttr) {

  private val updateIntervalMillis = 1000L
  private var updateJob: Job? = null
  private var currentAvatar: Drawable? = null
  private val userUtils = UserUtils.getInstance(context)

  init {
    initJob()
    userUtils.setClick(this)
  }

  private fun initJob() {
    updateJob =
      CoroutineScope(Dispatchers.Main).launch {
        while (isActive) {
          updateAvatar()
          delay(updateIntervalMillis)
        }
      }
  }

  private suspend fun updateAvatar() {
    val newAvatar = withContext(Dispatchers.IO) { userUtils.getCircularUserIcon() }
    if (newAvatar != currentAvatar) {
      setImageDrawable(newAvatar)
      currentAvatar = newAvatar
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    updateJob?.cancel()
  }
}
