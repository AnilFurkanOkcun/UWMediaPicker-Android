package com.anilokcun.uwmediapicker

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.anilokcun.uwmediapicker.model.UWMediaPickerSettingsModel
import com.anilokcun.uwmediapicker.model.UwMediaPickerMediaModel
import com.anilokcun.uwmediapicker.ui.activity.UwMediaPickerDialogFragment
import java.lang.ref.WeakReference

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

class UwMediaPicker private constructor() {
	
	private var activityWeakReference: WeakReference<AppCompatActivity>? = null
	private var fragmentWeakReference: WeakReference<Fragment>? = null
	
	private var galleryMode: GalleryMode = GalleryMode.ImageGallery
	private var maxSelectableMediaCount: Int? = null
	private var gridColumnCount: Int = 3
	private var lightStatusBar: Boolean = true
	
	private var imageCompressionEnabled: Boolean = false
	private var compressionMaxWidth = 1280F
	private var compressionMaxHeight = 720F
	private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
	private var compressionQuality = 85
	private var compressedFileDestinationPath: String? = null
	private var cancelCallback: (() -> Unit)? = null

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

	fun setCancelCallback(cancelCallback: (() -> Unit)): UwMediaPicker {
		this.cancelCallback = cancelCallback
		return this
	}
	
	fun launch(resultCallback: (List<UwMediaPickerMediaModel>?) -> Unit) {
		val compressedFileDestinationPathValue = if (compressedFileDestinationPath == null) {
			val application = if (activityWeakReference != null) {
				activityWeakReference!!.get()!!.application
			} else {
				fragmentWeakReference!!.get()!!.requireActivity().application
			}
			"${application.getExternalFilesDir(null)!!.path}/Pictures"
		} else {
			compressedFileDestinationPath!!
		}
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
			compressedFileDestinationPathValue
		)
		
		val uwMediaPickerDialogFragment = UwMediaPickerDialogFragment.newInstance(uwMediaPickerSettings)
		uwMediaPickerDialogFragment.setResultCallback(resultCallback)
		cancelCallback?.let(uwMediaPickerDialogFragment::setCancelCallback)
		val fragmentManager = if (activityWeakReference != null) {
			activityWeakReference!!.get()!!.supportFragmentManager
		} else {
			fragmentWeakReference!!.get()!!.parentFragmentManager
		}
		uwMediaPickerDialogFragment.show(fragmentManager, "UwMediaPicker")
	}
	
	enum class GalleryMode { ImageGallery, VideoGallery, ImageAndVideoGallery }
	
	companion object {
		fun with(activity: AppCompatActivity) =
			UwMediaPicker().apply { this.activityWeakReference = WeakReference(activity) }
		
		fun with(fragment: Fragment) =
			UwMediaPicker().apply { this.fragmentWeakReference = WeakReference(fragment) }
	}
}
