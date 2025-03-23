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
package com.android.settings.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.settings.R

class PackagePickerDialogFragment(
  private val dialogTitleResId: Int,
  private val onPackageSelected: (String) -> Unit,
) : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
    val context = requireContext()
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    val apps =
      packageManager.queryIntentActivities(intent, 0).sortedBy {
        it.activityInfo.loadLabel(packageManager).toString()
      }

    val appItems =
      apps.map { appInfo ->
        AppItem(
          name = appInfo.activityInfo.loadLabel(packageManager).toString(),
          packageName = appInfo.activityInfo.packageName,
          icon = appInfo.activityInfo.loadIcon(packageManager),
        )
      }

    val adapter = AppListAdapter(context, appItems)

    val builder =
      AlertDialog.Builder(context)
        .setTitle(dialogTitleResId)
        .setAdapter(adapter) { _, which ->
          val selectedPackage = appItems[which].packageName
          onPackageSelected(selectedPackage)
          Toast.makeText(
              context,
              getString(R.string.toast_package_selected, selectedPackage),
              Toast.LENGTH_SHORT,
            )
            .show()
        }
        .setNegativeButton(android.R.string.cancel, null)

    return builder.create()
  }

  data class AppItem(val name: String, val packageName: String, val icon: Drawable)

  class AppListAdapter(context: Context, private val items: List<AppItem>) :
    ArrayAdapter<AppItem>(context, android.R.layout.simple_list_item_1, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      val item = items[position]
      val layout =
        LinearLayout(context).apply {
          orientation = LinearLayout.HORIZONTAL
          setPadding(24, 16, 24, 16)
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
      val appIcon =
        ImageView(context).apply {
          layoutParams = LinearLayout.LayoutParams(96, 96).apply { marginEnd = 32 }
          setImageDrawable(item.icon)
        }
      val appName =
        TextView(context).apply {
          layoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
              gravity = Gravity.CENTER_VERTICAL
            }
          text = item.name
          textSize = 18f
          setTextColor(
            context
              .obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
              .getColor(0, 0)
          )
        }
      layout.addView(appIcon)
      layout.addView(appName)
      return layout
    }
  }
}
