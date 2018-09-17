package com.anilokcun.uwmediapicker.model

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal interface BaseGalleryMediaModel : BaseGalleryModel {
	val mediaPath: String?
	var selected: Boolean
}