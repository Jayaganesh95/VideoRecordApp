package com.videorecorderapp.myapplication.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {
    private const val READ_EXTERNAL_STORAGE_REQUEST = 1
    private const val WRITE_EXTERNAL_STORAGE_REQUEST = 2

    fun hasReadExternalStoragePermission(activity: AppCompatActivity?): Boolean {
        val permission =
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    fun hasWriteExternalStoragePermission(activity: AppCompatActivity?): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    fun requestFineLocationPermission(activity: AppCompatActivity?): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    fun requestCoarseLocationPermission(activity: AppCompatActivity?): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    fun requestWriteExternalStoragePermission(activity: AppCompatActivity?) {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_EXTERNAL_STORAGE_REQUEST
        )
    }

    fun isReadExternalStoragePermissionGranted(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ): Boolean {
        return requestCode == READ_EXTERNAL_STORAGE_REQUEST && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    fun isWriteExternalStoragePermissionGranted(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ): Boolean {
        return requestCode == WRITE_EXTERNAL_STORAGE_REQUEST && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}