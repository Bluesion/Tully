package com.bluesion.tully.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.bluesion.tully.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val cleanCorpse = findPreference<CheckBoxPreference>("cleaner_corpse")!!
        cleanCorpse.isChecked = sharedPrefs.getBoolean("cleaner_corpse", true)
        cleanCorpse.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isChecked = newValue as Boolean
                if (isChecked) {
                    sharedPrefs.edit().putBoolean("cleaner_corpse", true).apply()
                } else {
                    sharedPrefs.edit().putBoolean("cleaner_corpse", false).apply()
                }
                true
            }

        val cleanApk = findPreference<CheckBoxPreference>("cleaner_apk")!!
        cleanApk.isChecked = sharedPrefs.getBoolean("cleaner_apk", true)
        cleanApk.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isChecked = newValue as Boolean
                if (isChecked) {
                    sharedPrefs.edit().putBoolean("cleaner_apk", true).apply()
                } else {
                    sharedPrefs.edit().putBoolean("cleaner_apk", false).apply()
                }
                true
            }
    }
}