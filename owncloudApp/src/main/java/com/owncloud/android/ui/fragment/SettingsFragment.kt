package com.owncloud.android.ui.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.owncloud.android.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}