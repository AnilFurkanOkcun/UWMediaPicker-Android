package com.anilokcun.uwmediapicker.viewholder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.GalleryVideoModel
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class GalleryVideoVH(itemView: View) : BaseGalleryMediaVH(itemView) {
	
	private val imgThumbnail = itemView.findViewById<ImageView>(R.id.item_gallery_video_img_thumbnail)
	private val imgSelected = itemView.findViewById<ImageView>(R.id.item_gallery_video_img_selected)
	private val tvVideoDuration = itemView.findViewById<TextView>(R.id.item_gallery_video_tv_video_duration)
	private val tvVideoSize = itemView.findViewById<TextView>(R.id.item_gallery_video_tv_video_size)
	
	fun bind(item: GalleryVideoModel, galleryGridSize: Int, galleryTextSize: Float, onMediaClickListener: OnRVItemClickListener) {
		// Bind item click listener, thumbnail and selected icon in the base class
		super.bind(item, galleryGridSize, onMediaClickListener, imgThumbnail, imgSelected)
		
		// Video Duration
		val videoDurationMillisecond = item.videoDuration
		val videoDurationSecond = TimeUnit.MILLISECONDS.toSeconds(videoDurationMillisecond) % 60
		val videoDurationMinute = TimeUnit.MILLISECONDS.toMinutes(videoDurationMillisecond) % 60
		val videoDurationHour = TimeUnit.MILLISECONDS.toHours(videoDurationMillisecond)
		tvVideoDuration.textSize = galleryTextSize
		tvVideoDuration.text = if (videoDurationHour > 0) {
			itemView.context
				.getString(R.string.uwmediapicker_time_format_hour_min_sec,
					videoDurationHour, videoDurationMinute, videoDurationSecond)
		} else {
			itemView.context
				.getString(R.string.uwmediapicker_time_format_min_sec,
					videoDurationMinute, videoDurationSecond)
		}
		
		// Video Size
		tvVideoSize.textSize = galleryTextSize
		tvVideoSize.text = if (item.videoSize != null) {
			if (item.videoSize == "0") {
				"0"
			} else {
				val units = arrayListOf("B", "KB", "MB", "GB", "TB")
				val digitGroups = (log10(item.videoSize.toDouble()) / log10(1024.0)).toInt()
				TextUtils.concat(DecimalFormat("#,##0.#").format(item.videoSize.toDouble()
					/ (1024.0.pow(digitGroups.toDouble()))), " ", units[digitGroups])
			}
		} else ""
	}
}