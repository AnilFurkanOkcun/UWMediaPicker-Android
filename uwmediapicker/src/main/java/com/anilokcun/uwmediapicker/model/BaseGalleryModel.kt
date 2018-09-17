package com.anilokcun.uwmediapicker.model

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal interface BaseGalleryModel {
	val itemType: Int

	companion object {
		/* Gallery Item Types */
		const val TYPE_GALLERY_MEDIA_BUCKET = 600
		const val TYPE_GALLERY_IMAGE = 601
		const val TYPE_GALLERY_VIDEO = 602
	}
}