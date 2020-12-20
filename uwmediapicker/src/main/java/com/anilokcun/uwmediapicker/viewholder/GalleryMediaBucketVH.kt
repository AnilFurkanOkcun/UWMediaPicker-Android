package com.anilokcun.uwmediapicker.viewholder

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.helper.toUri
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.GalleryMediaBucketModel
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
	
	fun bind(item: GalleryMediaBucketModel, galleryGridSize: Int, galleryTextSize: Float, onMediaBucketClickListener: OnRVItemClickListener) {
		// Thumbnail
		imgThumbnail.layoutParams.height = galleryGridSize
		imgThumbnail.layoutParams.width = galleryGridSize
		
		Glide.with(itemView).apply {
			this.load(item.coverImagePath?.toUri())
				.apply(RequestOptions
					.overrideOf(galleryGridSize, galleryGridSize)
					.centerCrop()
					.placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.colorUwMediaPickerImagePlaceHolder))))
				.into(imgThumbnail)
		}
		
		// Name
		tvName.text = item.name
		tvName.textSize = galleryTextSize
		// Media Count
		tvMediaCount.text = item.mediaCount.toString()
		tvMediaCount.textSize = galleryTextSize
		
		// Item Click Event
		itemView.setOnClickListener {
			onMediaBucketClickListener.onClick(adapterPosition)
		}
		
	}
}