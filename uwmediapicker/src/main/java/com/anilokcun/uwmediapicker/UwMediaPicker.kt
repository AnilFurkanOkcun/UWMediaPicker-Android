package com.anilokcun.uwmediapicker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import com.anilokcun.uwmediapicker.constants.Constants
import com.anilokcun.uwmediapicker.model.UWMediaPickerSettingsModel
import com.anilokcun.uwmediapicker.ui.activity.UwMediaPickerActivity
import java.lang.ref.WeakReference

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

class UwMediaPicker private constructor() {

	private var activityWeakReference: WeakReference<Activity>? = null
	private var fragmentWeakReference: WeakReference<androidx.fragment.app.Fragment>? = null

	private var requestCode: Int = 0

	private var galleryMode: GalleryMode = GalleryMode.ImageGallery
	private var maxSelectableMediaCount: Int? = null
	private var gridColumnCount: Int = 3
	private var lightStatusBar: Boolean = true

	private var imageCompressionEnabled: Boolean = false
	private var compressionMaxWidth = 1280F
	private var compressionMaxHeight = 720F
	private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
	private var compressionQuality = 85
	private var compressedFileDestinationPath: String = Environment
		.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
		.absolutePath

	fun setRequestCode(requestCode: Int): UwMediaPicker {
		this.requestCode = requestCode
		return this
	}

	fun setGalleryMode(galleryMode: GalleryMode): UwMediaPicker {
		this.galleryMode = galleryMode
		return this
	}

	fun setMaxSelectableMediaCount(maxSelectableMediaCount: Int?): UwMediaPicker {
		this.maxSelectableMediaCount =
				if (maxSelectableMediaCount != null && maxSelectableMediaCount >= 1) {
					maxSelectableMediaCount
				} else null
		return this
	}

	fun setGridColumnCount(gridColumnCount: Int): UwMediaPicker {
		this.gridColumnCount =
				if (gridColumnCount >= 1) gridColumnCount
				else 1
		return this
	}

	fun setLightStatusBar(lightStatusBar: Boolean): UwMediaPicker {
		this.lightStatusBar = lightStatusBar
		return this
	}

	fun enableImageCompression(imageCompressionEnabled: Boolean): UwMediaPicker {
		this.imageCompressionEnabled = imageCompressionEnabled
		return this
	}

	fun setCompressionMaxWidth(maxWidth: Float): UwMediaPicker {
		this.compressionMaxHeight =
				if (maxWidth < 1)
					1F
				else
					maxWidth
		return this
	}

	fun setCompressionMaxHeight(maxHeight: Float): UwMediaPicker {
		this.compressionMaxHeight =
				if (maxHeight < 1)
					1F
				else
					maxHeight
		return this
	}

	fun setCompressFormat(compressFormat: Bitmap.CompressFormat): UwMediaPicker {
		this.compressFormat = compressFormat
		return this
	}

	fun setCompressionQuality(quality: Int): UwMediaPicker {
		this.compressionQuality =
				when {
					quality < 0 -> 0
					quality > 100 -> 100
					else -> quality
				}
		return this
	}

	fun setCompressedFileDestinationPath(destinationDirectoryPath: String): UwMediaPicker {
		this.compressedFileDestinationPath = destinationDirectoryPath
		return this
	}

	fun open() {
		val uwMediaPickerSettings = UWMediaPickerSettingsModel(
			galleryMode,
			maxSelectableMediaCount,
			gridColumnCount,
			lightStatusBar,
			imageCompressionEnabled,
			compressionMaxWidth,
			compressionMaxHeight,
			compressFormat,
			compressionQuality,
			compressedFileDestinationPath
		)
		if (activityWeakReference != null) {
			val uwMediaPickerIntent = Intent(activityWeakReference?.get(), UwMediaPickerActivity::class.java)
			uwMediaPickerIntent.putExtra(Constants.UW_MEDIA_PICKER_SETTINGS_KEY, uwMediaPickerSettings)
			activityWeakReference?.get()?.startActivityForResult(uwMediaPickerIntent, requestCode)
		} else if (fragmentWeakReference != null) {
			val uwMediaPickerIntent = Intent(fragmentWeakReference?.get()?.context, UwMediaPickerActivity::class.java)
			uwMediaPickerIntent.putExtra(Constants.UW_MEDIA_PICKER_SETTINGS_KEY, uwMediaPickerSettings)
			fragmentWeakReference?.get()?.startActivityForResult(uwMediaPickerIntent, requestCode)
		}
	}

	enum class GalleryMode { ImageGallery, VideoGallery }

	companion object {
		const val UwMediaPickerResultKey = "UwMediaPickerResultKey"

		fun with(activity: Activity) =
			UwMediaPicker().apply { this.activityWeakReference = WeakReference(activity) }

		fun with(fragment: androidx.fragment.app.Fragment) =
			UwMediaPicker().apply { this.fragmentWeakReference = WeakReference(fragment) }
	}
}
