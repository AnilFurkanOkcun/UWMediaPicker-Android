package com.anilokcun.uwmediapicker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SelectedMediaModel(
	val mediaPath: String,
	val mediaType: MediaType
) : Parcelable