package com.anilokcun.uwmediapicker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UwMediaPickerMediaModel(
	var mediaPath: String,
	val mediaType: UwMediaPickerMediaType
) : Parcelable