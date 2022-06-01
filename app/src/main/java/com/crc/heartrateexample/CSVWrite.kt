package com.crc.heartrateexample

import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CSVWrite {
    private val TAG : String = "CSVWrite"

    lateinit var directory : File
    lateinit var writer : FileWriter
    var fileDirectory : String = ""


    init {
        Log.d(TAG, "init CSV")

        if (Build.VERSION.SDK_INT <= 29) {
            val dirPath : String = Environment.getExternalStorageDirectory().absolutePath + "/HCILabData"
            directory = File(dirPath)
            if (!directory.exists()) { directory.mkdir() }
        } else {
            directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)
        }
    }

    fun createCsv(data : String, fileName : String, dataType: String) {
        Log.i(TAG, "create CSV")
        fileDirectory = "$directory/$fileName"
        try {
            writer = FileWriter(fileDirectory + "_$dataType.csv", true)
            try {
                writer.write(data)
            } finally {
                writer.close()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Can't create file")
            e.printStackTrace()
        }
    }

    fun writeCsv(data : String, dataType : String) {
        Log.i(TAG, "write CSV")

        try {
            writer = FileWriter(fileDirectory + "_$dataType.csv", true)
            try {
                writer.append(data)
            } finally {
                writer.close()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Can't save file")
            e.printStackTrace()
        }
    }

    fun setDate(date: String, mode: String) {
        val fileName = date + "_" + mode
        fileDirectory = "$directory/$fileName"
    }
}