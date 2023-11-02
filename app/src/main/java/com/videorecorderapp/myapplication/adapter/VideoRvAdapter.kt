package com.videorecorderapp.myapplication.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.videorecorderapp.myapplication.R
import com.videorecorderapp.myapplication.model.VideoModel


class VideoRvAdapter(private var videoView: VideoView, private var txtView: TextView) :
    RecyclerView.Adapter<VideoRvAdapter.RvViewHolder>() {
    private var videoJsonList = ArrayList<VideoModel>()

    class RvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoJsonThumbNail: ImageView = itemView.findViewById(R.id.video_thumbNail)
    }

    fun addDataList(item: VideoModel) {
        videoJsonList.add(item)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rv_video_thumbnails, parent, false)
        return RvViewHolder(view)
    }

    override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
        val videojson = videoJsonList[position]
        val fileName = videojson.videoUriInModel

        holder.videoJsonThumbNail.setOnClickListener {
            Log.d("TAG", "onBindViewHolder: $fileName")
            loadVideoFromInternalStorage(videojson.videoUriInModel)
            txtView.text = (videojson.locationText)
        }
    }

    override fun getItemCount(): Int {
        return videoJsonList.size
    }

    private fun loadVideoFromInternalStorage(videoUri: Uri) {
        try {

            videoView.setVideoURI(videoUri)
            videoView.setOnPreparedListener { mediaPlayer ->
                // Adjust the size and start playing the video when it's prepared
                mediaPlayer.setOnVideoSizeChangedListener { mp, width, height ->
                    val videoProportion = width.toFloat() / height.toFloat()
                    val screenProportion = videoView.width.toFloat() / videoView.height.toFloat()
                    val newWidth: Int
                    val newHeight: Int

                    if (videoProportion > screenProportion) {
                        newWidth = videoView.width
                        newHeight = (videoView.width / videoProportion).toInt()
                    } else {
                        newWidth = (videoView.height * videoProportion).toInt()
                        newHeight = videoView.height
                    }

                    val lp = videoView.layoutParams
                    lp.width = newWidth
                    lp.height = newHeight
                    videoView.layoutParams = lp
                }
                videoView.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                videoView.context, "Error loading video", Toast.LENGTH_LONG
            ).show()
        }
    }
}