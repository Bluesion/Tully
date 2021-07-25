package com.bluesion.tully.deviceeditor

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluesion.tully.R
import com.bluesion.tully.databinding.FragmentDeviceEditorBinding
import java.lang.String.valueOf

class DeviceEditorFragment : Fragment() {

    private var binding: FragmentDeviceEditorBinding? = null
    private lateinit var mAdapter: DeviceEditorAdapter
    private val globalSettings = ArrayList<DeviceEditorItem>()
    private val systemSettings = ArrayList<DeviceEditorItem>()
    private val secureSettings = ArrayList<DeviceEditorItem>()
    private var listType = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentDeviceEditorBinding.inflate(inflater, container, false)

        getValues()
        globalSettings.sortWith { k1, k2 -> k1.key.uppercase().compareTo(k2.key.uppercase()) }
        systemSettings.sortWith { k1, k2 -> k1.key.uppercase().compareTo(k2.key.uppercase()) }
        secureSettings.sortWith { k1, k2 -> k1.key.uppercase().compareTo(k2.key.uppercase()) }

        mAdapter = DeviceEditorAdapter(object : DeviceEditorAdapter.MyAdapterListener {
            override fun onLayoutClick(position: Int) {
                val bottomSheetDialog = if (mAdapter.getList()[position].value == "null") {
                    DeviceEditorDialog(mAdapter.getList()[position].key, "")
                } else {
                    DeviceEditorDialog(
                        mAdapter.getList()[position].key,
                        mAdapter.getList()[position].value
                    )
                }
                bottomSheetDialog.setOnDialogCloseListener(object :
                    DeviceEditorDialog.OnDialogSaveListener {
                    override fun onSaveClicked(newValue: String) {
                        val settingsType = when (listType) {
                            0 -> "global"
                            1 -> "system"
                            else -> "secure"
                        }

                        try {
                            val contentValues = ContentValues(2)
                            contentValues.put("name", mAdapter.getList()[position].key)
                            contentValues.put("value", newValue)
                            context!!.contentResolver.insert(
                                Uri.parse("content://settings/$settingsType"),
                                contentValues
                            )
                            mAdapter.editValue(position, newValue)
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }
                    }

                    override fun onDeleteClicked() {
                        val settingsType = when (listType) {
                            0 -> "global"
                            1 -> "system"
                            else -> "secure"
                        }

                        try {
                            val strArr = arrayOf(mAdapter.getList()[position].key)
                            context!!.contentResolver.delete(
                                Uri.parse("content://settings/$settingsType"),
                                "name = ?",
                                strArr
                            )
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }

                        binding!!.searchBar.setText("")
                        when (listType) {
                            0 -> {
                                globalSettings.removeAt(position)
                                globalSettings.sortWith { k1, k2 ->
                                    k1.key.uppercase().compareTo(k2.key.uppercase())
                                }
                                mAdapter.setList(globalSettings)
                            }
                            1 -> {
                                systemSettings.removeAt(position)
                                systemSettings.sortWith { k1, k2 ->
                                    k1.key.uppercase().compareTo(k2.key.uppercase())
                                }
                                mAdapter.setList(systemSettings)
                            }
                            else -> {
                                secureSettings.removeAt(position)
                                secureSettings.sortWith { k1, k2 ->
                                    k1.key.uppercase().compareTo(k2.key.uppercase())
                                }
                                mAdapter.setList(secureSettings)
                            }
                        }
                    }
                })
                bottomSheetDialog.show(
                    requireActivity().supportFragmentManager,
                    bottomSheetDialog.tag
                )
            }
        })

        mAdapter.setList(globalSettings)

        binding!!.recyclerView.adapter = mAdapter
        binding!!.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding!!.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), VERTICAL))

        binding!!.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mAdapter.filter.filter(s.toString())
            }
        })

        binding!!.fab.setOnClickListener {
            val bottomSheetDialog = DeviceEditorDialogNew()
            bottomSheetDialog.setOnDialogCloseListener(object :
                DeviceEditorDialogNew.OnDialogSaveListener {
                override fun onSaveClicked(newKey: String, newValue: String) {
                    val settingsType = when (listType) {
                        0 -> "global"
                        1 -> "system"
                        else -> "secure"
                    }

                    try {
                        val contentResolver = context!!.contentResolver
                        val contentValues = ContentValues(2)
                        contentValues.put("name", newKey)
                        contentValues.put("value", newValue)
                        contentResolver.insert(
                            Uri.parse("content://settings/$settingsType"),
                            contentValues
                        )

                        when (listType) {
                            0 -> {
                                globalSettings.add(DeviceEditorItem(newKey, newValue))
                                globalSettings.sortWith { k1, k2 ->
                                    k1.key.uppercase().compareTo(k2.key.uppercase())
                                }
                                mAdapter.setList(globalSettings)
                            }
                            1 -> {
                                systemSettings.add(DeviceEditorItem(newKey, newValue))
                                systemSettings.sortWith { k1, k2 ->
                                    k1.key.uppercase().compareTo(k2.key.uppercase())
                                }
                                mAdapter.setList(systemSettings)
                            }
                            else -> {
                                secureSettings.add(DeviceEditorItem(newKey, newValue))
                                secureSettings.sortWith { k1, k2 ->
                                    k1.key.uppercase().compareTo(k2.key.uppercase())
                                }
                                mAdapter.setList(secureSettings)
                            }
                        }
                        binding!!.searchBar.setText("")
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
            })
            bottomSheetDialog.show(
                requireActivity().supportFragmentManager,
                bottomSheetDialog.tag
            )
        }

        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_device_editor, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        listType = when (item.itemId) {
            R.id.global -> {
                mAdapter.setList(globalSettings)
                0
            }
            R.id.system -> {
                DeviceEditorDialogSystem().show(requireActivity().supportFragmentManager, tag)
                mAdapter.setList(systemSettings)
                1
            }
            else -> {
                mAdapter.setList(secureSettings)
                2
            }
        }
        binding!!.searchBar.setText("")
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun getValues() {
        val globalCursor =
            requireActivity().contentResolver.query(
                Settings.Global.CONTENT_URI,
                null,
                null,
                null,
                null
            )

        if (globalCursor!!.moveToFirst()) {
            globalSettings.add(
                DeviceEditorItem(
                    globalCursor.getString(globalCursor.getColumnIndex("name")),
                    valueOf(globalCursor.getString(globalCursor.getColumnIndex("value")))
                )
            )
            while (globalCursor.moveToNext()) {
                globalSettings.add(
                    DeviceEditorItem(
                        globalCursor.getString(globalCursor.getColumnIndex("name")),
                        valueOf(globalCursor.getString(globalCursor.getColumnIndex("value")))
                    )
                )
            }
        }
        globalCursor.close()

        val systemCursor =
            requireActivity().contentResolver.query(
                Settings.System.CONTENT_URI,
                null,
                null,
                null,
                null
            )
        if (systemCursor!!.moveToFirst()) {
            systemSettings.add(
                DeviceEditorItem(
                    systemCursor.getString(systemCursor.getColumnIndex("name")),
                    valueOf(systemCursor.getString(systemCursor.getColumnIndex("value")))
                )
            )
            while (systemCursor.moveToNext()) {
                systemSettings.add(
                    DeviceEditorItem(
                        systemCursor.getString(systemCursor.getColumnIndex("name")),
                        valueOf(systemCursor.getString(systemCursor.getColumnIndex("value")))
                    )
                )
            }
        }
        systemCursor.close()

        val secureCursor =
            requireActivity().contentResolver.query(
                Settings.Secure.CONTENT_URI,
                null,
                null,
                null,
                null
            )
        if (secureCursor!!.moveToFirst()) {
            secureSettings.add(
                DeviceEditorItem(
                    secureCursor.getString(secureCursor.getColumnIndex("name")),
                    valueOf(secureCursor.getString(secureCursor.getColumnIndex("value")))
                )
            )
            while (secureCursor.moveToNext()) {
                secureSettings.add(
                    DeviceEditorItem(
                        secureCursor.getString(secureCursor.getColumnIndex("name")),
                        valueOf(secureCursor.getString(secureCursor.getColumnIndex("value")))
                    )
                )
            }
        }
        secureCursor.close()
    }
}
