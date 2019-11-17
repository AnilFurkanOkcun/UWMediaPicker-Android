package com.anilokcun.uwandroidmediapicker

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.*

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class SelectedMediaRvAdapter(
	private var itemList: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	/** Creates View for each item in the List */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return MediaVH(ImageView(parent.context).apply {
			RecyclerView.LayoutParams(MainActivity.GRID_SIZE, MainActivity.GRID_SIZE)
			adjustViewBounds = true
		})
	}

	/** Binds the data on the List */
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		Glide.with(holder.itemView.context)
			.load(itemList[position])
			.apply(
				RequestOptions()
					.override(MainActivity.GRID_SIZE).centerCrop()
					.placeholder(ColorDrawable(ContextCompat
						.getColor(holder.itemView.context, R.color.colorImagePlaceHolder))))
			.into(holder.itemView as ImageView)
	}

	/** Gets the 'Size of the List' or 'Item count in the RecyclerView'*/
	override fun getItemCount(): Int = itemList.size

	fun update(list: ArrayList<String>) {
		itemList = list
		notifyDataSetChanged()
	}

	class MediaVH(itemView: View) : RecyclerView.ViewHolder(itemView)

}