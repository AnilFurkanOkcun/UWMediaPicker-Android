package com.anilokcun.uwandroidmediapicker

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class GalleryItemDecoration(
	private val gridSpacingPx: Int,
	private val gridColumnCount: Int
) : RecyclerView.ItemDecoration() {

	private var isNeedLeftSpacing = false

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		val frameWidth = ((parent.width - gridSpacingPx.toFloat() * (gridColumnCount - 1)) / gridColumnCount).toInt()
		val padding = parent.width / gridColumnCount - frameWidth
		val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
		if (itemPosition < gridColumnCount) {
			outRect.top = 0
		} else {
			outRect.top = gridSpacingPx
		}
		if (itemPosition % gridColumnCount == 0) {
			outRect.left = 0
			outRect.right = padding
			isNeedLeftSpacing = true
		} else if ((itemPosition + 1) % gridColumnCount == 0) {
			isNeedLeftSpacing = false
			outRect.right = 0
			outRect.left = padding
		} else if (isNeedLeftSpacing) {
			isNeedLeftSpacing = false
			outRect.left = gridSpacingPx - padding
			if ((itemPosition + 2) % gridColumnCount == 0) {
				outRect.right = gridSpacingPx - padding
			} else {
				outRect.right = gridSpacingPx / 2
			}
		} else if ((itemPosition + 2) % gridColumnCount == 0) {
			isNeedLeftSpacing = false
			outRect.left = gridSpacingPx / 2
			outRect.right = gridSpacingPx - padding
		} else {
			isNeedLeftSpacing = false
			outRect.left = gridSpacingPx / 2
			outRect.right = gridSpacingPx / 2
		}
		outRect.bottom = 0
	}
}