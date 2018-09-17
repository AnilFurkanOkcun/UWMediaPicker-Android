package com.anilokcun.uwmediapicker.model

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal data class GalleryMediaBucketModel(
	val id: String?,
	val name: String?,
	val coverImagePath: String?,
	val mediaCount: Int?,
	override val itemType: Int = BaseGalleryModel.TYPE_GALLERY_MEDIA_BUCKET
) : BaseGalleryModel