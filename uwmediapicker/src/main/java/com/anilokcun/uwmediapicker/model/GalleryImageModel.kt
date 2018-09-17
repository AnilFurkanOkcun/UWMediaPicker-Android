package com.anilokcun.uwmediapicker.model

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal data class GalleryImageModel(
	override val mediaPath: String?,
	override var selected: Boolean,
	override val itemType: Int = BaseGalleryModel.TYPE_GALLERY_IMAGE
) : BaseGalleryModel, BaseGalleryMediaModel