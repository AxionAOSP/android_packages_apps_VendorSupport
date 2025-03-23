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
import android.util.AttributeSet
import kotlin.random.Random

class BannerView : WallpaperView {

  private val bannerProbability = 0.9

  constructor(context: Context) : super(context) {}

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
  ) : super(context, attrs, defStyleAttr) {}

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    updateBannerOrWallpaper()
  }

  private fun updateBannerOrWallpaper() {
    val showBanner = Random.nextFloat() < bannerProbability
    val randomBannerImage = Random.nextInt(1, 99)
    val bannerImage = "banner_$randomBannerImage"
    val resId = resources.getIdentifier(bannerImage, "drawable", context.packageName)
    if (showBanner && resId != 0) {
      val drawable = resources.getDrawable(resId, context.theme)
      setImageDrawable(drawable)
    } else {
      updateWallpaper()
    }
  }

  override protected fun setWallpaperPreview() {}
}
