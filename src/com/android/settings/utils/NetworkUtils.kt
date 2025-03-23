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
import android.os.UserManager
import com.android.settingslib.RestrictedLockUtilsInternal
import com.android.settingslib.Utils

object NetworkUtils {

  @JvmStatic
  fun isNetworkAvailable(context: Context): Boolean {
    return !isUserRestricted(context) && !Utils.isWifiOnly(context)
  }

  @JvmStatic
  private fun isUserRestricted(context: Context): Boolean {
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val isSecondaryUser = !userManager.isAdminUser
    return isSecondaryUser ||
      RestrictedLockUtilsInternal.hasBaseUserRestriction(
        context,
        UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,
        android.os.Process.myUserHandle().identifier,
      )
  }
}
