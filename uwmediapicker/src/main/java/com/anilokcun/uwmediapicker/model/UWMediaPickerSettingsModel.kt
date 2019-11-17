package com.anilokcun.uwmediapicker.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.anilokcun.uwmediapicker.UwMediaPicker
import kotlinx.android.parcel.Parcelize

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

@Parcelize
data class UWMediaPickerSettingsModel(
	val galleryMode: UwMediaPicker.GalleryMode,
	val maxSelectableMediaCount: Int?,
	val gridColumnCount: Int,
	val lightStatusBar: Boolean,
	val imageCompressionEnabled: Boolean,
	val compressionMaxWidth: Float,
	val compressionMaxHeight: Float,
	val compressFormat: Bitmap.CompressFormat,
	val compressionQuality: Int,
	val compressedFileDestinationPath: String
) : Parcelable