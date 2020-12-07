package com.example.zipunzipsample

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.zipunzipsample.UnzipUtility.Companion.unzip
import com.example.zipunzipsample.UnzipUtility.Companion.unzip2
import com.example.zipunzipsample.UnzipUtility.Companion.unzip3
import com.example.zipunzipsample.UnzipUtility.Companion.zip
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    val backupDBPath1: String = Environment.getExternalStorageDirectory().getPath().toString() + "/ZipUnzipSample/Agreement_FSLibre3_de-DE_terms_of_use.zip"
    val backupDBPath2: String = Environment.getExternalStoragePublicDirectory("DIRECTORY_DOCUMENTS").getPath().toString() + "/ZipUnzipSample/Agreement_FSLibre3_de-DE_terms_of_use.zip"
    val sd = Environment.getExternalStorageDirectory()
    val spd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val useSd = true
    var isWritable = false
    // Storage Permissions
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            Log.d("Test", "sdcard mounted and writable")
            Log.d("Test","sdpath->${sd.path}")
            isWritable = true
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            Log.d("Test", "sdcard mounted readonly")
            isWritable = false
        } else {
            Log.d("Test", "sdcard state: $state")
            isWritable = false
        }
    }

    fun btnZipClick(view: View) {
        val backupDBPath: String = Environment.getExternalStorageDirectory().getPath().toString() + "/ZipUnzipSample"
        val backUpDbFolder = File(backupDBPath)
        backUpDbFolder.mkdir()
        val backupDB = File(backUpDbFolder,  "/DOC38901-005_rev-C_de-DE.html")
        val s = arrayOf<String>(backupDB.absolutePath)
        zip(s, "$backupDBPath/pos_demo.zip");

    }
    fun btnUnZipClick(view: View) {
        verifyStoragePermissions(this@MainActivity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission approved",
                        Toast.LENGTH_SHORT
                    ).show()
                    UnzipFile()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission denied to read your External storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private fun UnzipFile() {
        if(useSd){
            val backupDBPath: String = Environment.getExternalStorageDirectory().getPath().toString() + "/ZipUnzipSample/Agreement_FSLibre3_de-DE_terms_of_use.zip"
            if (isWritable) {
                val backupDBFolder = File(sd.path)
                txtLog.append(unzip(backupDBPath, backupDBFolder.path+ "/ZipUnzipSample/Extracted"))
            }
        } else {
            val backupDBPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + "/ZipUnzipSample/Agreement_FSLibre3_de-DE_terms_of_use.zip"
            if (isWritable) {
                val backupDBFolder = File(spd.path)
                txtLog.append(unzip(backupDBPath2, backupDBFolder.path+ "/ZipUnzipSample/Extracted"))
            }
        }
    }

    fun verifyStoragePermissions(activity: Activity?) { // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) { // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        } else {
            UnzipFile()
        }
    }

    fun btnUnZip2Click(view: View) {
        val backupDBPath: String = Environment.getExternalStorageDirectory().getPath().toString() + "/ZipUnzipSample/Agreement_FSLibre3_de-DE_terms_of_use.zip"
        if (isWritable) {
            val backupDBFolder = File(sd.path)
            txtLog.append(unzip2(backupDBPath, backupDBFolder.path+ "/ZipUnzipSample/Extracted"))
        }
    }

    fun btnUnZip3Click(view: View) {
        val backupDBPath: String = Environment.getExternalStorageDirectory().getPath().toString() + "/ZipUnzipSample/Agreement_FSLibre3_de-DE_terms_of_use.zip"
        if (isWritable) {
            val backupDBFolder = File(sd.path)
            txtLog.append(unzip3(backupDBPath, backupDBFolder.path+ "/ZipUnzipSample/Extracted",null))
        }
    }
}
