package com.bluesion.tully.deviceeditor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bluesion.tully.R
import com.bluesion.tully.databinding.ActivityDeviceEditorBinding

class DeviceEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDeviceEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onResume() {
        super.onResume()

        if (Settings.System.canWrite(this) && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_SECURE_SETTINGS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DeviceEditorFragment()).commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DeviceEditorPermissionFragment()).commit()
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
