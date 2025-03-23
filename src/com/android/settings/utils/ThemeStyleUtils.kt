package com.android.settings.utils

import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings
import com.android.internal.util.android.ThemeUtils

class ThemeStyleUtils(context: Context) {
  private val themeUtils: ThemeUtils = ThemeUtils.getInstance(context)
  private val contentResolver: ContentResolver = context.contentResolver
  private val mContext: Context = context
  private val handler = Handler(Looper.getMainLooper())

  companion object {
    private val OVERLAY_CATEGORIES =
      mapOf(
        "powermenu_style" to
          arrayOf(
            "com.android.theme.powermenu.cyberpunk",
            "com.android.theme.powermenu.duoline",
            "com.android.theme.powermenu.ios",
            "com.android.theme.powermenu.layers",
          ),
        "notification_style" to
          arrayOf(
            "com.android.theme.notification.cyberpunk",
            "com.android.theme.notification.duoline",
            "com.android.theme.notification.ios",
            "com.android.theme.notification.layers",
          ),
        "progress_bar_style" to
          arrayOf(
            "com.android.theme.progressbar.blocky_thumb",
            "com.android.theme.progressbar.minimal_thumb",
            "com.android.theme.progressbar.outline_thumb",
            "com.android.theme.progressbar.shishu",
          ),
        "brightness_bar_style" to
          arrayOf(
            "com.android.systemui.brightness_slider.acun",
            "com.android.systemui.brightness_slider.bang",
            "com.android.systemui.brightness_slider.cyberpunk",
            "com.android.systemui.brightness_slider.gradientroundedbar",
            "com.android.systemui.brightness_slider.leafyoutline",
            "com.android.systemui.brightness_slider.minimalthumb",
            "com.android.systemui.brightness_slider.outline",
            "com.android.systemui.brightness_slider.roundedclip",
            "com.android.systemui.brightness_slider.shaded",
            "com.android.systemui.brightness_slider.thin",
            "com.android.systemui.brightness_slider.translucent",
          ),
        "hide_ime_space_style" to
          arrayOf(
            "com.android.system.theme.hide_ime_space_narrow",
            "com.android.system.theme.hide_ime_space_no_space",
            "com.android.system.theme.hide_ime_space_hidden",
          ),
      )
  }

  fun updateThemeStyle(key: String, category: String, target: String, restartSystemUI: Boolean) {
    handler.postDelayed(
      {
        val overlays = OVERLAY_CATEGORIES[key] ?: return@postDelayed
        val style = Settings.System.getIntForUser(contentResolver, key, 0, UserHandle.USER_CURRENT)
        themeUtils.setOverlayEnabled(category, target, target)
        if (style in 1..overlays.size) {
          themeUtils.setOverlayEnabled(category, overlays[style - 1], target)
        }
        if (restartSystemUI) {
          SystemRestartUtils.restartSystemUI(mContext)
        }
      },
      500,
    )
  }
}
