package com.videorecorderapp.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.videorecorderapp.myapplication.adapter.VideoRvAdapter
import com.videorecorderapp.myapplication.databinding.ActivityMainBinding
import com.videorecorderapp.myapplication.model.JsonLocationModel
import com.videorecorderapp.myapplication.model.VideoModel
import com.videorecorderapp.myapplication.util.Constants
import com.videorecorderapp.myapplication.util.PermissionUtil
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var videoUri: Uri
    private lateinit var textUri: Uri

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var videoRvAdapter: VideoRvAdapter


    private lateinit var textFileName: String
    private lateinit var videoFileName: String


    private lateinit var jsonArray: ArrayList<JsonLocationModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        jsonArray = ArrayList()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

        recordVideoAndTrackLocation()

    }

    private fun recordVideoAndTrackLocation() {
        binding.recordButton.setOnClickListener {
            if (PermissionUtil.hasReadExternalStoragePermission(this) || PermissionUtil.hasWriteExternalStoragePermission(
                    this
                )
            ) {
                listenLocationUpdates()
                videoRecordIntent()
            } else {
                checkAndRequestStoragePermissions()
            }
        }
        initRecyclerView()
    }

    private fun videoRecordIntent() {
        val videoFile = createVideoFile()
        videoUri = FileProvider.getUriForFile(
            this, "com.videorecorderapp.myapplication.provider", videoFile
        )
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        startActivityForResult(intent, Constants.VIDEO_RETURN_CODE)
    }

    private fun checkLocationPermission() {
        if (!PermissionUtil.requestFineLocationPermission(this) && !PermissionUtil.requestCoarseLocationPermission(
                this
            )
        ) {
            requestLocationPermission()
            Toast.makeText(this, "Turn On Location", Toast.LENGTH_SHORT).show()
        } else {
            getLocation()
        }
    }

    private fun checkAndRequestStoragePermissions() {

        val permissionsToRequest = mutableListOf<String>()

        if (!PermissionUtil.hasReadExternalStoragePermission(this)) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!PermissionUtil.hasWriteExternalStoragePermission(this)) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val permissionArray = permissionsToRequest.toTypedArray()

        ActivityCompat.requestPermissions(
            this, permissionArray, Constants.REQUEST_STORAGE_PERMISSION
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUEST_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            videoRecordIntent()
        }
        if (requestCode == Constants.PERMISSON_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun createVideoFile(): File {
        var currentTime: Date = Calendar.getInstance().time
        var formatedName = currentTime.toString().replace(" ", "")
        videoFileName = "Rec.${formatedName}.mp4"
        textFileName = "Txt.${formatedName}.txt"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (!storageDir!!.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, videoFileName)
    }

    private fun createTextFile(textFileName: String, jsonArray: ArrayList<JsonLocationModel>) {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val txtFile = File(storageDir, textFileName)
        if (txtFile.exists()) {
            txtFile.createNewFile()
        }
        textUri = FileProvider.getUriForFile(
            this, "com.videorecorderapp.myapplication.provider", txtFile
        )

        // Create a FileOutputStream object for the text file.
        val fos = FileOutputStream(txtFile)

        // Write the text to the file.
        fos.write(jsonArray.toString().toByteArray())

        // Close the FileOutputStream object.
        fos.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        stopLocationUpdates()
        if (requestCode == Constants.VIDEO_RETURN_CODE && resultCode == Activity.RESULT_OK) {
            val videoUri = data?.data
            if (videoUri != null) {
                if (jsonArray.isNotEmpty()) {
                    createTextFile(textFileName, jsonArray)

                    val videoModel = VideoModel(
                        videoUriInModel = videoUri,
                        jsonUriInModel = textUri,
                        locationText = jsonArray.toString()
                    )
                    videoRvAdapter.addDataList(videoModel)
                } else {
                    Log.d("TAG", "onActivityResult: emptyJsonArray}")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initRecyclerView() {
        binding.recylerView.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        binding.recylerView.setHasFixedSize(true)
        videoRvAdapter = VideoRvAdapter(binding.videoView, binding.jsonTv)
        binding.recylerView.adapter = videoRvAdapter

    }


    //checking the uses permission
    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    //for allow us to get user permission
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            ), Constants.PERMISSON_REQUEST_ACCESS_LOCATION
        )
    }

    //for checking if the location service of the device is enable
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //get current location and address
    private fun getLocation() {

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                locationRequest = LocationRequest.create().apply {
                    interval = 1000 // Update interval in milliseconds (1 seconds)
                    fastestInterval = 1000 // Fastest update interval (1 seconds)
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {

                        //get the last location
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                            val list: MutableList<Address>? =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)

                            // Get the address.
                            var address = ""
                            if (!list.isNullOrEmpty()) {
                                address = list[0].getAddressLine(0)
                            }

                            // Create a JsonLocationModel object
                            val locationDetail = JsonLocationModel(
                                distanceTravelled = "",
                                longitude = location.longitude.toString(),
                                latitude = location.latitude.toString(),
                                videotimestamp = videoUri.lastPathSegment!!.replace(".mp4", ""),
                                Address = address,
                                timestamp = ""
                            )

                            // Add the location detail to the jsonArray.
                            jsonArray.add(locationDetail)
                            Log.d("TAG", "getLocation  : jsonArray :$jsonArray ")
                        }
                    }

                }

            } else {
                Toast.makeText(
                    this, "Please turn on location", Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun listenLocationUpdates() {
        jsonArray.clear()
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProvider.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationProvider.removeLocationUpdates(locationCallback)
    }

}