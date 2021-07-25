package com.bluesion.tully.deviceeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bluesion.tully.databinding.FragmentDeviceEditorPermissionBinding

class DeviceEditorPermissionFragment : Fragment() {

    private var binding: FragmentDeviceEditorPermissionBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceEditorPermissionBinding.inflate(inflater, container, false)

        binding!!.permissionButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${requireContext().packageName}")
            startActivity(intent)
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
