package com.anilokcun.uwmediapicker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.enum.Enums
import com.anilokcun.uwmediapicker.listener.GalleryMediaOnLongClickListener
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.BaseGalleryModel
import com.anilokcun.uwmediapicker.model.GalleryImageModel
import com.anilokcun.uwmediapicker.model.GalleryMediaBucketModel
import com.anilokcun.uwmediapicker.model.GalleryVideoModel
import com.anilokcun.uwmediapicker.viewholder.GalleryImageVH
import com.anilokcun.uwmediapicker.viewholder.GalleryMediaBucketVH
import com.anilokcun.uwmediapicker.viewholder.GalleryVideoVH
import java.util.*

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class GalleryMediaRvAdapter(
	private var itemList: ArrayList<out BaseGalleryModel>,
	private val onRVItemClickListener: OnRVItemClickListener,
	private val galleryMediaOnLongClickListener: GalleryMediaOnLongClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	var context: Context? = null

	/** Creates View for each item in the List */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		// Get context
		if (context == null) context = parent.context

		return when (viewType) {
			// Gallery Media Bucket;
			BaseGalleryModel.TYPE_GALLERY_MEDIA_BUCKET -> GalleryMediaBucketVH(LayoutInflater.from(context)
				.inflate(R.layout.uwmediapicker_item_gallery_media_bucket, parent, false))
			// Gallery Video;
			BaseGalleryModel.TYPE_GALLERY_VIDEO -> GalleryVideoVH(LayoutInflater.from(context)
				.inflate(R.layout.uwmediapicker_item_gallery_video, parent, false))
			// Gallery Image;
			BaseGalleryModel.TYPE_GALLERY_IMAGE -> GalleryImageVH(LayoutInflater.from(context)
				.inflate(R.layout.uwmediapicker_item_gallery_image, parent, false))
			// Error;
			else -> throw RuntimeException(Enums.MissingViewTypeException.toString() + " ViewType: " + viewType)
		}
	}

	/** Binds the data on the List */
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (itemList[position].itemType) {
			// Gallery Media Bucket;
			BaseGalleryModel.TYPE_GALLERY_MEDIA_BUCKET -> (holder as GalleryMediaBucketVH).bind(itemList[position] as GalleryMediaBucketModel, onRVItemClickListener)
			// Gallery Video;
			BaseGalleryModel.TYPE_GALLERY_VIDEO -> (holder as GalleryVideoVH).bind(itemList[position] as GalleryVideoModel, onRVItemClickListener)
			// Gallery Image;
			BaseGalleryModel.TYPE_GALLERY_IMAGE -> (holder as GalleryImageVH).bind(itemList[position] as GalleryImageModel, onRVItemClickListener, galleryMediaOnLongClickListener)
			// Error;
			else -> throw RuntimeException(Enums.MissingViewTypeException.toString() + " ViewType: " + itemList[position].itemType)
		}
	}

	/** Gets the 'Size of the List' or 'Item count in the RecyclerView'*/
	override fun getItemCount(): Int = itemList.size

	/** Gets the Type of the Item in the List*/
	override fun getItemViewType(position: Int): Int = itemList[position].itemType
}