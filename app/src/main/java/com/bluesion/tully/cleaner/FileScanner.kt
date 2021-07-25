package com.bluesion.tully.cleaner

import android.content.Context
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import com.bluesion.tully.R
import java.io.File
import java.util.*

class FileScanner internal constructor(private val context: Context, private val path: File) {
    private var sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private var filesRemoved = 0
    private var kilobytesTotal = 0L
    private var corpse = true
    private val cleanList = ArrayList<String>()
    private val listFiles: List<File>
        get() = getListFiles(path)
    private var foundFiles: List<File>? = null
    private val filters = ArrayList<String>()
    private val protectedFileList = arrayOf(
        "backup", "copy", "copies", "important", "do_not_edit"
    )

    /**
     * Used to generate a list of all files on device
     * @param parentDirectory where to start searching from
     * @return List of all files on device (besides whitelisted ones)
     */
    private fun getListFiles(parentDirectory: File): List<File> {
        val inFiles = ArrayList<File>()
        val files = parentDirectory.listFiles()
        if (files != null) {
            for (file in files) {
                if (!isWhiteListed(file)) {
                    if (file.isDirectory) {
                        if (!autoWhiteList(file)) {
                            inFiles.add(file)
                        }
                        inFiles.addAll(getListFiles(file))
                    } else {
                        inFiles.add(file)
                    }
                }
            }
        }
        return inFiles
    }

    /**
     * Runs a for each loop through the white list, and compares the path of the file
     * to each path in the list
     * @param file file to check if in the whitelist
     * @return true if is the file is in the white list, false if not
     */
    private fun isWhiteListed(file: File): Boolean {
        for (path in getWhiteList()) {
            if (path.equals(file.absolutePath, ignoreCase = true) || path.equals(
                    file.name,
                    ignoreCase = true
                )
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Runs before anything is filtered/cleaned. Automatically adds folders to the whitelist
     * based on the name of the folder itself
     * @param file file to check whether it should be added to the whitelist
     */
    private fun autoWhiteList(file: File): Boolean {
        for (protectedFile in protectedFileList) {
            if (file.name.lowercase(Locale.US).contains(protectedFile) &&
                !getWhiteList().contains(file.absolutePath.lowercase(Locale.US))
            ) {
                getWhiteList().add(file.absolutePath.lowercase(Locale.US))
                sharedPrefs.edit().putString("whiteList", getWhiteList().toString()).apply()
                return true
            }
        }
        return false
    }

    private fun getWhiteList(): ArrayList<String> {
        val whiteListStrings = sharedPrefs.getString("whiteList", "no whitelist")!!
        val temp = ArrayList<String>()
        for (element in whiteListStrings.split(" , ")) {
            temp.add(element)
        }
        return temp
    }

    /**
     * Runs as for each loop through the filter, and checks if
     * the file matches any filters
     * @param file file to check
     * @return true if the file's extension is in the filter, false otherwise
     */
    private fun filter(file: File): Boolean {
        // corpse checking
        if (file.parentFile != null && file.parentFile!!.parentFile != null && corpse) {
            if (file.parentFile!!.name == "data" && file.parentFile!!.parentFile!!.name == "Android") {
                if (!installedPackages.contains(file.name) && file.name != ".nomedia") {
                    return true
                }
            }
        }

        // empty folder
        if (file.isDirectory && isDirectoryEmpty(file)) {
            return true
        }

        // file
        for (filter in filters) {
            if (file.absolutePath.lowercase(Locale.US)
                    .matches(filter.lowercase(Locale.US).toRegex())
            ) {
                return true
            }
        }

        // not empty folder or file in filter
        return false
    }

    private val installedPackages: List<String>
        get() {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val packagesString: MutableList<String> = LinkedList()
            for (packageInfo in packages) {
                packagesString.add(packageInfo.packageName)
            }
            return packagesString
        }

    /**
     * lists the contents of the file to an array, if the array length is 0, then return true,
     * else false
     * @param directory directory to test
     * @return true if empty, false if containing a file(s)
     */
    private fun isDirectoryEmpty(directory: File): Boolean {
        return if (directory.list() != null) {
            directory.list()!!.isEmpty()
        } else {
            false
        }
    }

    /**
     * Adds paths to the white list that are not to be cleaned. As well as adds
     * extensions to filter. 'generic', 'aggressive', and 'apk' should be assigned
     * by calling preferences.getBoolean()
     */
    fun setUpFilters(apk: Boolean) {
        val folders = ArrayList<String>()
        val files = ArrayList<String>()

        folders.addAll(context.resources.getStringArray(R.array.cleaner_filter_folders))
        files.addAll(context.resources.getStringArray(R.array.cleaner_filter_files))

        filters.clear()

        for (folder in folders) {
            filters.add(getRegexForFolder(folder))
        }

        for (file in files) {
            filters.add(getRegexForFile(file))
        }

        if (apk) {
            filters.add(getRegexForFile(".apk"))
        }
    }

    fun startScan(): Long {
        var cycles = 0
        val maxCycles = 1

        while (cycles < maxCycles) {
            // find files
            foundFiles = listFiles

            // scan & delete
            for (file in foundFiles!!) {
                if (filter(file)) {
                    cleanList.add(file.absolutePath)
                    kilobytesTotal += file.length()
                }
            }

            // nothing found this run, no need to run again
            if (filesRemoved == 0) {
                break
            }

            // reset for next cycle
            filesRemoved = 0
            ++cycles
        }
        return kilobytesTotal
    }

    fun startClean() {
        var cycles: Byte = 0
        val maxCycles: Byte = 10

        // removes the need to 'clean' multiple times to get everything
        while (cycles < maxCycles) {
            // delete
            for (file in foundFiles!!) {
                if (filter(file)) {
                    ++filesRemoved
                    file.delete()
                }
            }

            // nothing found this run, no need to run again
            if (filesRemoved == 0) {
                break
            }

            // reset for next cycle
            filesRemoved = 0
            ++cycles
        }
    }

    private fun getRegexForFolder(folder: String): String {
        return ".*(\\\\|/)$folder(\\\\|/|$).*"
    }

    private fun getRegexForFile(file: String): String {
        return ".+" + file.replace(".", "\\.") + "$"
    }

    fun setCorpse(corpse: Boolean) {
        this.corpse = corpse
    }

    fun getCleanList(): ArrayList<String> {
        return cleanList
    }
}