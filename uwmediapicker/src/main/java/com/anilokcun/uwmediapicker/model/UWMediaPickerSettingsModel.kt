package com.anilokcun.uwmediapicker.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.anilokcun.uwmediapicker.UwMediaPicker

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

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
) : Parcelable {
	constructor(parcel: Parcel) : this(
		UwMediaPicker.GalleryMode.valueOf(parcel.readString()),
		parcel.readValue(Int::class.java.classLoader) as? Int,
		parcel.readInt(),
		parcel.readByte() != 0.toByte(),
		parcel.readByte() != 0.toByte(),
		parcel.readFloat(),
		parcel.readFloat(),
		Bitmap.CompressFormat.valueOf(parcel.readString()),
		parcel.readInt(),
		parcel.readString())

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(galleryMode.name)
		parcel.writeValue(maxSelectableMediaCount)
		parcel.writeInt(gridColumnCount)
		parcel.writeByte(if (lightStatusBar) 1 else 0)
		parcel.writeByte(if (imageCompressionEnabled) 1 else 0)
		parcel.writeFloat(compressionMaxWidth)
		parcel.writeFloat(compressionMaxHeight)
		parcel.writeString(compressFormat.name)
		parcel.writeInt(compressionQuality)
		parcel.writeString(compressedFileDestinationPath)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<UWMediaPickerSettingsModel> {
		override fun createFromParcel(parcel: Parcel): UWMediaPickerSettingsModel {
			return UWMediaPickerSettingsModel(parcel)
		}

		override fun newArray(size: Int): Array<UWMediaPickerSettingsModel?> {
			return arrayOfNulls(size)
		}
	}
}