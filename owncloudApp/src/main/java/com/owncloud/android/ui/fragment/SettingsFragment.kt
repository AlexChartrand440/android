/**
 * ownCloud Android client application
 *
 * @author Juan Carlos Garrote Gascón
 *
 * Copyright (C) 2021 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.owncloud.android.R
import com.owncloud.android.authentication.BiometricManager
import com.owncloud.android.data.preferences.datasources.SharedPreferencesProvider
import com.owncloud.android.data.preferences.datasources.implementation.SharedPreferencesProviderImpl
import com.owncloud.android.extensions.showMessageInSnackbar
import com.owncloud.android.ui.activity.BiometricActivity
import com.owncloud.android.ui.activity.PassCodeActivity
import com.owncloud.android.ui.activity.PatternLockActivity

class SettingsFragment : PreferenceFragmentCompat() {

    private val PREFERENCE_SECURITY_CATEGORY = "security_category"
    val PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS = "touches_with_other_visible_windows"
    private val ACTION_REQUEST_PASSCODE = 5
    private val ACTION_CONFIRM_PASSCODE = 6

    private var mPrefSecurityCategory: PreferenceCategory? = null
    private var mPasscode: CheckBoxPreference? = null
    private var mPattern: CheckBoxPreference? = null
    private var mBiometric: CheckBoxPreference? = null
    private var mBiometricManager: BiometricManager? = null
    private var patternSet = false
    private var passcodeSet = false
    private var mPrefTouchesWithOtherVisibleWindows: CheckBoxPreference? = null

    private val mPreferencesProvider = SharedPreferencesProviderImpl(getApplicationContext())

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        /*
        * Security
        */

        mPrefSecurityCategory = findPreference(PREFERENCE_SECURITY_CATEGORY)
        mPasscode = findPreference(PassCodeActivity.PREFERENCE_SET_PASSCODE)
        mPattern = findPreference(PatternLockActivity.PREFERENCE_SET_PATTERN)
        mBiometric = findPreference(BiometricActivity.PREFERENCE_SET_BIOMETRIC)
        mPrefTouchesWithOtherVisibleWindows = findPreference(PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS)

        // Passcode lock
        mPasscode?.setOnPreferenceChangeListener { preference: Preference?, newValue: Any ->
            val i = Intent(getApplicationContext(), PassCodeActivity::class.java)
            val incoming = newValue as Boolean
            patternSet = mPreferencesProvider.getBoolean(
                PatternLockActivity.PREFERENCE_SET_PATTERN,
                false
            )
            if (patternSet) {
                showMessageInSnackbar(getString(R.string.pattern_already_set))
            } else {
                i.action =
                    if (incoming) PassCodeActivity.ACTION_REQUEST_WITH_RESULT else PassCodeActivity.ACTION_CHECK_WITH_RESULT
                val requestCode = if (incoming) ACTION_REQUEST_PASSCODE else ACTION_CONFIRM_PASSCODE
                var activityLauncher: ActivityResultLauncher<Intent>? = null
                if (requestCode == ACTION_REQUEST_PASSCODE) {
                    activityLauncher =
                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                            if (result.resultCode == RESULT_OK) { // Enable passcode

                                val passcode: String? = result.data?.getStringExtra(PassCodeActivity.KEY_PASSCODE)
                                if (passcode != null && passcode.length == 4) {
                                    for (i in 1..4) {
                                        (mPreferencesProvider as SharedPreferencesProviderImpl).putString(
                                            PassCodeActivity.PREFERENCE_PASSCODE_D + i,
                                            passcode.substring(i - 1, i)
                                        )
                                    }
                                    mPreferencesProvider.putBoolean(
                                        PassCodeActivity.PREFERENCE_SET_PASSCODE,
                                        true
                                    )
                                    showMessageInSnackbar(getString(R.string.pass_code_stored))

                                    // Allow to use biometric lock since Passcode lock has been enabled
                                    //enableBiometric()
                                }
                            }
                        }
                } else if (requestCode == ACTION_CONFIRM_PASSCODE) {
                    activityLauncher =
                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                            if (result.resultCode == RESULT_OK) { // Disable passcode

                                val keyCheck: Boolean? =
                                    result.data?.getBooleanExtra(PassCodeActivity.KEY_CHECK_RESULT, false)
                                if (keyCheck != null && keyCheck) {
                                    mPreferencesProvider.putBoolean(
                                        PassCodeActivity.PREFERENCE_SET_PASSCODE,
                                        false
                                    )
                                    showMessageInSnackbar(getString(R.string.pass_code_removed))

                                    // Do not allow to use biometric lock since Passcode lock has been disabled
                                    //disableBiometric(getString(R.string.prefs_biometric_summary))
                                }
                            }
                        }
                }

                activityLauncher?.launch(i)
            }
            false
        }

        /*

        // Pattern lock
        if (mPattern != null) {
            mPattern.setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                val intent = Intent(
                    getApplicationContext(),
                    PatternLockActivity::class.java
                )
                val state = newValue as Boolean
                passcodeSet = mPreferencesProvider.getBoolean(PassCodeActivity.PREFERENCE_SET_PASSCODE, false)
                if (passcodeSet) {
                    showSnackMessage(R.string.passcode_already_set)
                } else {
                    intent.action =
                        if (state) PatternLockActivity.ACTION_REQUEST_WITH_RESULT else PatternLockActivity.ACTION_CHECK_WITH_RESULT
                    startActivityForResult(
                        intent,
                        if (state) Preferences.ACTION_REQUEST_PATTERN else Preferences.ACTION_CONFIRM_PATTERN
                    )
                }
                false
            })
        }

        // Biometric lock

        // Biometric lock
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPrefSecurityCategory.removePreference(mBiometric)
        } else if (mBiometric != null) {
            // Disable biometric lock if Passcode or Pattern locks are disabled
            if (mPasscode != null && mPattern != null && !mPasscode.isChecked() && !mPattern.isChecked()) {
                mBiometric.setEnabled(false)
                mBiometric.setSummary(R.string.prefs_biometric_summary)
            }
            mBiometric.setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                val incoming = newValue as Boolean

                // Biometric not supported
                if (incoming && mBiometricManager != null && !mBiometricManager.isHardwareDetected()) {
                    showSnackMessage(R.string.biometric_not_hardware_detected)
                    return@setOnPreferenceChangeListener false
                }

                // No biometric enrolled yet
                if (incoming && mBiometricManager != null && !mBiometricManager.hasEnrolledBiometric()) {
                    showSnackMessage(R.string.biometric_not_enrolled)
                    return@setOnPreferenceChangeListener false
                }
                true
            })
        }

        if (mPrefTouchesWithOtherVisibleWindows != null) {
            mPrefTouchesWithOtherVisibleWindows.setOnPreferenceChangeListener(
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    if (newValue as Boolean) {
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.confirmation_touches_with_other_windows_title))
                            .setMessage(getString(R.string.confirmation_touches_with_other_windows_message))
                            .setNegativeButton(getString(R.string.common_no), null)
                            .setPositiveButton(
                                getString(R.string.common_yes)
                            ) { dialog: DialogInterface?, which: Int ->
                                mPreferencesProvider.putBoolean(
                                    Preferences.PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS,
                                    true
                                )
                                mPrefTouchesWithOtherVisibleWindows.setChecked(true)
                            }
                            .show()
                        return@setOnPreferenceChangeListener false
                    }
                    true
                }
            )
        }

         */
    }

}
