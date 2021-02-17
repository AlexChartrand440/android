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

package com.owncloud.android.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.owncloud.android.R
import com.owncloud.android.ui.fragment.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Don't know if this is useful for the action bar or not
        /*
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        delegate.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        delegate.supportActionBar?.setTitle(R.string.actionbar_settings)

        // For adding content description tag to a title field in the action bar
        val actionBarTitleId = resources.getIdentifier("action_bar_title", "id", "android")
        val actionBarTitleView = window.decorView.findViewById<View>(actionBarTitleId)
        if (actionBarTitleView != null) {    // it's null in Android 2.x
            window.decorView.findViewById<View>(actionBarTitleId).contentDescription =
                getString(R.string.actionbar_settings)
        }*/

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}
