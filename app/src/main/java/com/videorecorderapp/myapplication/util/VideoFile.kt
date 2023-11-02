package com.videorecorderapp.myapplication.util

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class VideoFile(internal val uri: Uri, val file: File) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader)!!,
        parcel.readSerializable() as File
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeSerializable(file)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoFile> {
        override fun createFromParcel(parcel: Parcel): VideoFile {
            return VideoFile(parcel)
        }

        override fun newArray(size: Int): Array<VideoFile?> {
            return arrayOfNulls(size)
        }
    }
}