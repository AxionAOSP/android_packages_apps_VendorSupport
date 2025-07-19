/*
 * Copyright (C) 2025 AxionOS Project
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
package com.android.settings.preferences

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.PromptInfo
import android.os.Bundle
import android.os.CancellationSignal
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import androidx.preference.*
import com.android.settings.R
import java.util.concurrent.Executor

abstract class BaseAppListFragment : PreferenceFragmentCompat() {

    private val selectedPackages: MutableSet<String> = mutableSetOf()
    private var isAuthenticated = false
    private var biometricPrompt: BiometricPrompt? = null
    private var cancellationSignal: CancellationSignal? = null
    private var selectedCategory: PreferenceCategory? = null
    private var unselectedCategory: PreferenceCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getFragmentTitle()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val xmlResId = getInitialPreferencesXmlResId()
        if (xmlResId != 0) {
            setPreferencesFromResource(xmlResId, rootKey)
        } else {
            preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
        }
        if (requiresSecureAuthentication() && !isAuthenticated) {
            showSecureAuthenticationPrompt()
        } else {
            initializePreferences()
        }
    }

    override fun onResume() {
        super.onResume()
        if (requiresSecureAuthentication() && !isAuthenticated) {
            showSecureAuthenticationPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        cancellationSignal?.cancel()
        cancellationSignal = null
        if (shouldResetAuthenticationOnPause()) {
            isAuthenticated = false
        }
    }

    private fun showSecureAuthenticationPrompt() {
        if (cancellationSignal != null) {
            return
        }
        val biometricManager = requireContext().getSystemService(BiometricManager::class.java)
        val authenticators = when (biometricManager?.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            }
            else -> {
                Log.w("BaseAppListFragment", "No authentication methods available")
                isAuthenticated = true
                initializePreferences()
                return
            }
        }

        val executor = requireContext().mainExecutor
        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                cancellationSignal = null
                handleAuthenticationError(errorCode, errString)
            }
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                cancellationSignal = null
                isAuthenticated = true
                initializePreferences()
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                handleAuthenticationFailed()
            }
        }

        cancellationSignal = CancellationSignal()
        
        try {
            biometricPrompt = BiometricPrompt.Builder(requireContext())
                .setTitle(getAuthenticationTitle())
                .setSubtitle(getAuthenticationSubtitle())
                .setDescription(getAuthenticationDescription())
                .setAllowedAuthenticators(authenticators)
                .setConfirmationRequired(true)
                .build()

            biometricPrompt?.authenticateUser(
                cancellationSignal!!,
                executor,
                authenticationCallback,
                UserHandle.myUserId()
            )
        } catch (e: Exception) {
            Log.e("BaseAppListFragment", "Failed to start biometric authentication", e)
            cancellationSignal = null
            requireActivity().onBackPressed()
        }
    }

    private fun handleAuthenticationError(errorCode: Int, errString: CharSequence) {
        when (errorCode) {
            BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED,
            BiometricPrompt.BIOMETRIC_ERROR_CANCELED -> {
                requireActivity().onBackPressed()
            }
            else -> {
                Log.e("BaseAppListFragment", "Authentication error: $errorCode - $errString")
                onAuthenticationError(errorCode, errString.toString())
            }
        }
    }

    private fun handleAuthenticationFailed() {
        Log.w("BaseAppListFragment", "Authentication failed, user can retry")
        onAuthenticationFailed()
    }

    private fun initializePreferences() {
        selectedPackages.clear()
        selectedPackages.addAll(getSavedPackages())

        val category = findOrCreateDynamicAppCategory()

        val recyclerView = listView ?: return
        recyclerView.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                populateAppPreferences(category)
                recyclerView.alpha = 0f
                recyclerView.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }.start()
    }

    private fun findOrCreateDynamicAppCategory(): PreferenceCategory {
        val key = getAppListCategoryKey()
        var category = findPreference<PreferenceCategory>(key)
        if (category == null) {
            category = PreferenceCategory(requireContext()).apply {
                title = getCategoryTitle()
                this.key = key
            }
            preferenceScreen.addPreference(category)
        }
        return category
    }

    private fun populateAppPreferences(parentCategory: PreferenceCategory) {
        val pm = requireContext().packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
            .filter {
                val pkgName = it.activityInfo.packageName
                !getExcludedPackages().contains(pkgName)
            }

        val selectedApps = resolveInfos
            .filter { selectedPackages.contains(it.activityInfo.packageName) }
            .sortedBy { it.loadLabel(pm).toString() }

        val unselectedApps = resolveInfos
            .filter { !selectedPackages.contains(it.activityInfo.packageName) }
            .sortedBy { it.loadLabel(pm).toString() }

        parentCategory.removeAll()

        selectedCategory = null
        unselectedCategory = null

        selectedCategory = PreferenceCategory(requireContext()).apply {
            title = getSelectedCategoryTitle()
        }
        selectedCategory?.let { parentCategory.addPreference(it) }

        if (selectedApps.isNotEmpty()) {
            for (resolveInfo in selectedApps) {
                selectedCategory?.addPreference(createAppPreference(resolveInfo, true))
            }
        } else {
            val emptyPref = Preference(requireContext()).apply {
                title = getString(R.string.no_selected_apps)
                isSelectable = false
                key = "pref_no_selected_apps"
            }
            selectedCategory?.addPreference(emptyPref)
        }

        if (unselectedApps.isNotEmpty()) {
            unselectedCategory = PreferenceCategory(requireContext()).apply {
                title = getUnselectedCategoryTitle()
            }
            unselectedCategory?.let { parentCategory.addPreference(it) }

            for (resolveInfo in unselectedApps) {
                unselectedCategory?.addPreference(createAppPreference(resolveInfo, false))
            }
        }
    }

    private fun createAppPreference(resolveInfo: ResolveInfo, isChecked: Boolean): CheckBoxPreference {
        val pm = requireContext().packageManager
        val packageName = resolveInfo.activityInfo.packageName
        val label = resolveInfo.loadLabel(pm).toString()

        return CheckBoxPreference(requireContext()).apply {
            key = "pref_app_toggle_$packageName"
            title = label
            icon = resolveInfo.loadIcon(pm)
            setDefaultValue(isChecked)
            isPersistent = false
            this.isChecked = isChecked

            setOnPreferenceChangeListener { pref, newValue ->
                val enabled = newValue as Boolean
                val targetCategory = if (enabled) selectedCategory else unselectedCategory
                val sourceCategory = if (enabled) unselectedCategory else selectedCategory

                selectedPackages.run {
                    if (enabled) add(packageName) else remove(packageName)
                }

                sourceCategory?.removePreference(pref)

                selectedCategory?.findPreference<Preference>("pref_no_selected_apps")?.let {
                    selectedCategory?.removePreference(it)
                }

                if (!enabled && selectedPackages.isEmpty()) {
                    val emptyPref = Preference(requireContext()).apply {
                        title = getString(R.string.no_selected_apps)
                        isSelectable = false
                        key = "pref_no_selected_apps"
                    }
                    selectedCategory?.addPreference(emptyPref)
                }

                targetCategory?.addPreference(pref)

                onCollectChanges()
                true
            }
        }
    }

    private fun getSavedPackages(): Set<String> {
        val str = Settings.Secure.getString(
            requireContext().contentResolver,
            getSettingsKey()
        )
        return str?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    private fun onCollectChanges() {
        val value = selectedPackages.joinToString(",")
        try {
            Settings.Secure.putStringForUser(
                requireContext().contentResolver,
                getSettingsKey(),
                value,
                UserHandle.myUserId()
            )
        } catch (e: Exception) {
            Log.e("BaseAppListFragment", "Failed to save to Settings.Secure", e)
        }
    }

    protected abstract fun getSettingsKey(): String
    protected abstract fun getCategoryTitle(): String
    protected open fun getExcludedPackages(): List<String> = listOf()
    protected open fun getInitialPreferencesXmlResId(): Int = 0
    protected open fun getAppListCategoryKey(): String = "app_list_category"
    protected open fun getFragmentTitle(): String = getString(R.string.fragment_title)
    protected open fun requiresSecureAuthentication(): Boolean = false
    protected open fun getAuthenticationTitle(): String = getString(R.string.app_lock_auth_title)
    protected open fun getAuthenticationSubtitle(): String = getString(R.string.app_lock_auth_subtitle)
    protected open fun getAuthenticationDescription(): String = getString(R.string.authentication_description)
    protected open fun shouldResetAuthenticationOnPause(): Boolean = true
    protected open fun onAuthenticationFailed() {}
    protected open fun onAuthenticationError(errorCode: Int, errorMessage: String) {
        Log.e("BaseAppListFragment", "Authentication error $errorCode: $errorMessage")
    }
    protected open fun getSelectedCategoryTitle(): String = getString(R.string.selected_category_title)
    protected open fun getUnselectedCategoryTitle(): String = getString(R.string.unselected_category_title)
}
