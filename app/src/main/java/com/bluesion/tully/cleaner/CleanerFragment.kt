package com.bluesion.tully.cleaner

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluesion.tully.R
import com.bluesion.tully.databinding.FragmentCleanerBinding
import java.io.File
import java.text.DecimalFormat

class CleanerFragment : Fragment() {

    private var binding: FragmentCleanerBinding? = null
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var fileScanner: FileScanner
    private val mAdapter = CleanerAdapter()
    private var isRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCleanerBinding.inflate(inflater, container, false)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding!!.scanButton.setOnClickListener {
            binding!!.mainLayout.visibility = View.GONE
            binding!!.resultText.text = getString(R.string.cleaner_scanning)
            binding!!.resultLayout.visibility = View.VISIBLE
            Thread {
                scan()
            }.start()
        }

        binding!!.cleanButton.setOnClickListener {
            binding!!.resultText.text = getString(R.string.cleaner_cleaning)
            binding!!.progressCircular.visibility = View.VISIBLE
            binding!!.cleanButton.visibility = View.GONE
            binding!!.closeButton.visibility = View.GONE
            Thread {
                clean()
            }.start()
        }

        binding!!.closeButton.setOnClickListener {
            activity?.finish()
        }

        binding!!.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding!!.recyclerView.adapter = mAdapter

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun getAbsoluteDir(): File {
        var rootPath = requireContext().getExternalFilesDir(null)!!.absolutePath
        val extraPortion = ("Android/data/com.bluesion.tully" + File.separator + "files")
        rootPath = rootPath.replace(extraPortion, "")
        return File(rootPath)
    }

    private fun scan() {
        Looper.prepare()
        isRunning = true
        val path = getAbsoluteDir()

        fileScanner = FileScanner(requireContext(), path)
        fileScanner.setCorpse(sharedPrefs.getBoolean("cleaner_corpse", true))
        fileScanner.setUpFilters(
            sharedPrefs.getBoolean("cleaner_apk", true)
        )

        // failed scan
        if (path.listFiles() == null) {
            activity?.runOnUiThread {
                binding!!.resultText.text = requireActivity().getString(R.string.cleaner_scan_fail)
            }
        }

        val totalSize = fileScanner.startScan()
        val totalList = fileScanner.getCleanList()

        activity?.runOnUiThread {
            mAdapter.setList(totalList)
            binding!!.resultText.text = getString(R.string.cleaner_scan_complete)
            binding!!.progressCircular.visibility = View.GONE
            binding!!.cleanButton.text = String.format(getString(R.string.cleaner_clean), totalList.size, convertSize(totalSize))
        }
        isRunning = false
        Looper.loop()
    }

    private fun clean() {
        Looper.prepare()
        isRunning = true

        fileScanner.startClean()

        activity?.runOnUiThread {
            binding!!.resultText.text = getString(R.string.cleaner_clean_complete)
            binding!!.progressCircular.visibility = View.GONE
            binding!!.recyclerView.visibility = View.INVISIBLE
            binding!!.closeButton.visibility = View.VISIBLE
        }
        isRunning = false
        Looper.loop()
    }

    private fun convertSize(length: Long): String {
        val format = DecimalFormat("#.##")
        val mb = (1024 * 1024).toLong()
        val kb: Long = 1024

        if (length > mb) {
            return format.format(length / mb).toString() + "MB"
        }

        return if (length > kb) {
            format.format(length / kb).toString() + "KB"
        } else {
            format.format(length).toString() + "B"
        }
    }
}