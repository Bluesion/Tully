package com.bluesion.tully.cleaner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bluesion.tully.databinding.FragmentCleanerPermissionBinding

class CleanerPermissionFragment(private val type: Int) : Fragment() {

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionListener?.onButtonClick()
        }

    interface PermissionListener {
        fun onButtonClick()
    }

    private var binding: FragmentCleanerPermissionBinding? = null
    private var permissionListener: PermissionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCleanerPermissionBinding.inflate(inflater, container, false)

        when (type) {
            0 -> {
                binding!!.commonPermissionLayout.visibility = View.VISIBLE
                binding!!.aboveRPermissionLayout.visibility = View.VISIBLE
            }
            1 -> {
                binding!!.commonPermissionLayout.visibility = View.GONE
                binding!!.aboveRPermissionLayout.visibility = View.VISIBLE
            }
            else -> {
                binding!!.commonPermissionLayout.visibility = View.VISIBLE
                binding!!.aboveRPermissionLayout.visibility = View.GONE
            }
        }

        binding!!.commonPermissionButton.setOnClickListener {
            startForResult.launch(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:com.bluesion.tully")
                }
            )
        }

        binding!!.aboveRPermissionButton.setOnClickListener {
            startForResult.launch(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:com.bluesion.tully")
                )
            )
        }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PermissionListener) {
            permissionListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        permissionListener = null
    }
}