package com.owncloud.android.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.owncloud.android.R
import com.owncloud.android.ui.fragment.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}