package com.anilokcun.uwmediapicker.viewholder

import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.helper.toUri
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.GalleryMediaBucketModel
import com.anilokcun.uwmediapicker.ui.activity.UwMediaPickerActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class GalleryMediaBucketVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val imgThumbnail = itemView.findViewById<ImageView>(R.id.item_gallery_media_bucket_img_thumbnail)
	private val tvName = itemView.findViewById<TextView>(R.id.item_gallery_media_bucket_tv_name)
	private val tvMediaCount = itemView.findViewById<TextView>(R.id.item_gallery_media_bucket_tv_media_count)

	fun bind(item: GalleryMediaBucketModel, onMediaBucketClickListener: OnRVItemClickListener) {
		// Thumbnail
		imgThumbnail.layoutParams.height = UwMediaPickerActivity.GALLERY_GRID_SIZE
		imgThumbnail.layoutParams.width = UwMediaPickerActivity.GALLERY_GRID_SIZE

		Glide.with(itemView).apply {
			this.load(item.coverImagePath?.toUri())
				.apply(RequestOptions
					.overrideOf(UwMediaPickerActivity.GALLERY_GRID_SIZE, UwMediaPickerActivity.GALLERY_GRID_SIZE)
					.centerCrop()
					.placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.colorUwMediaPickerImagePlaceHolder))))
				.into(imgThumbnail)
		}

		// Name
		tvName.text = item.name
		tvName.textSize = UwMediaPickerActivity.GALLERY_TEXT_SIZE_SP
		// Media Count
		tvMediaCount.text = item.mediaCount.toString()
		tvMediaCount.textSize = UwMediaPickerActivity.GALLERY_TEXT_SIZE_SP

		// Item Click Event
		itemView.setOnClickListener {
			onMediaBucketClickListener.onClick(adapterPosition)
		}

	}
}