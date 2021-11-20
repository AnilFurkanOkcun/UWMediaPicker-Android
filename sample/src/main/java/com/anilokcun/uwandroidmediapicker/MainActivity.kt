package com.anilokcun.uwandroidmediapicker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.anilokcun.uwmediapicker.model.UwMediaPickerMediaModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Author   	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

class MainActivity : AppCompatActivity() {
	
	private val selectedMediaGridColumnCount = 5
	private val allSelectedMediaPaths by lazy { arrayListOf<String>() }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			lytRoot.systemUiVisibility =
				lytRoot.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
		}
		// Get Device Width in Px
		val displayMetrics = DisplayMetrics()
		windowManager.defaultDisplay.getMetrics(displayMetrics)
		val deviceWidth = displayMetrics.widthPixels
		val gridSize = deviceWidth / selectedMediaGridColumnCount
		// Make compression options inactive
		tvMaxWidth.isEnabled = false
		etMaxWidth.isEnabled = false
		tvMaxHeight.isEnabled = false
		etMaxHeight.isEnabled = false
		tvQuality.isEnabled = false
		etQuality.isEnabled = false
		// Set RecyclerView
		rvSelectedMedia.layoutManager =
			GridLayoutManager(applicationContext, selectedMediaGridColumnCount)
		rvSelectedMedia.itemAnimator = DefaultItemAnimator()
		rvSelectedMedia.addItemDecoration(GalleryItemDecoration(
			resources.getDimensionPixelSize(com.anilokcun.uwmediapicker.R.dimen.uwmediapicker_gallery_spacing),
			selectedMediaGridColumnCount
		))
		rvSelectedMedia.adapter = SelectedMediaRvAdapter(allSelectedMediaPaths, gridSize)
		setListeners()
	}
	
	private fun setListeners() {
		/* Image Compression Option Switch's CheckedChangeListener */
		switchImageCompression.setOnCheckedChangeListener { _, isChecked ->
			tvMaxWidth.isEnabled = isChecked
			etMaxWidth.isEnabled = isChecked
			tvMaxHeight.isEnabled = isChecked
			etMaxHeight.isEnabled = isChecked
			tvQuality.isEnabled = isChecked
			etQuality.isEnabled = isChecked
			lytRoot.requestFocus()
		}
		/* Image Gallery Button's ClickListener */
		btnOpenImageGallery.setOnClickListener {
			requestToOpenImagePicker(PERMISSION_REQUEST_CODE_PICK_IMAGE, UwMediaPicker.GalleryMode.ImageGallery, ::openUwMediaPicker)
		}
		/* Video Gallery Button's ClickListener */
		btnOpenVideoGallery.setOnClickListener {
			requestToOpenImagePicker(PERMISSION_REQUEST_CODE_PICK_VIDEO, UwMediaPicker.GalleryMode.VideoGallery, ::openUwMediaPicker)
		}
		/* Image and Video Gallery Button's ClickListener */
		btnImageAndVideoGallery.setOnClickListener {
			requestToOpenImagePicker(PERMISSION_REQUEST_CODE_PICK_IMAGE_VIDEO, UwMediaPicker.GalleryMode.ImageAndVideoGallery, ::openUwMediaPicker)
		}
	}
	
	/** Opens UwMediaPicker for select images*/
	private fun openUwMediaPicker(galleryMode: UwMediaPicker.GalleryMode) {
		val gridColumnCount =
			if (etGridColumnCount.text.isNotEmpty()) {
				etGridColumnCount.text.toString().toInt()
			} else {
				3
			}
		val maxSelectableMediaCount =
			if (etMaxSelectableMediaCount.text.isNotEmpty()) {
				etMaxSelectableMediaCount.text.toString().toInt()
			} else {
				null
			}
		val maxWidth =
			if (etMaxWidth.text.isNotEmpty()) {
				etMaxWidth.text.toString().toFloat()
			} else {
				1280F
			}
		val maxHeight =
			if (etMaxHeight.text.isNotEmpty()) {
				etMaxHeight.text.toString().toFloat()
			} else {
				720F
			}
		val quality =
			if (etQuality.text.isNotEmpty()) {
				etQuality.text.toString().toInt()
			} else {
				85
			}
		UwMediaPicker.with(this)
			.setGalleryMode(galleryMode)
			.setGridColumnCount(gridColumnCount)
			.setMaxSelectableMediaCount(maxSelectableMediaCount)
			.setLightStatusBar(true)
			.enableImageCompression(switchImageCompression.isChecked)
			.setCompressionMaxWidth(maxWidth)
			.setCompressionMaxHeight(maxHeight)
			.setCompressFormat(Bitmap.CompressFormat.JPEG)
			.setCompressionQuality(quality)
			.setCompressedFileDestinationPath("${application.getExternalFilesDir(null)!!.path}/Pictures")
			.setCancelCallback(::onMediaSelectionCancelled)
			.launch(::onMediaSelected)
		
	}
	
	private fun onMediaSelected(selectedMediaList: List<UwMediaPickerMediaModel>?) {
		if (selectedMediaList != null) {
			val selectedMediaPathList = selectedMediaList.map { it.mediaPath }
			allSelectedMediaPaths.addAll(selectedMediaPathList)
			(rvSelectedMedia.adapter as SelectedMediaRvAdapter).update(allSelectedMediaPaths)
			tvSelectedMediaTitle.visibility = View.VISIBLE
		} else {
			Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show()
		}
	}

	private fun onMediaSelectionCancelled(){
		Toast.makeText(this, "Media Selection Cancelled ", Toast.LENGTH_SHORT).show()
	}
	
	/** Request to open Image Picker Intent and Handle the permissions */
	private fun requestToOpenImagePicker(requestCode: Int, galleryMode: UwMediaPicker.GalleryMode, openMediaPickerFunc: (UwMediaPicker.GalleryMode) -> Unit) {
		if (ContextCompat.checkSelfPermission(this.applicationContext,
				Manifest.permission.READ_EXTERNAL_STORAGE)
			!= PackageManager.PERMISSION_GRANTED) {
			// Permission is not granted
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.READ_EXTERNAL_STORAGE)) {
				// First time
				ActivityCompat.requestPermissions(this,
					arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode)
			} else {
				// Not first time
				ActivityCompat.requestPermissions(this,
					arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode)
			}
		} else {
			// Permission has already been granted
			openMediaPickerFunc.invoke(galleryMode)
		}
	}
	
	/** Handles Media Pick Request */
	private fun handleMediaPickPermissionRequest(grantResults: IntArray, galleryMode: UwMediaPicker.GalleryMode, openMediaPicker: (UwMediaPicker.GalleryMode) -> Unit) {
		// If request is cancelled, the result arrays are empty.
		if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
			// Permission granted
			openMediaPicker.invoke(galleryMode)
		} else {
			// Permission denied
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				// Without "Never Ask Again" Checked
				Toast.makeText(this, "Without permission you cant do that.", Toast.LENGTH_SHORT).show()
			} else {
				// With "Never Ask Again" Checked
				Toast.makeText(this, "You need to give permission. Go to Settings...", Toast.LENGTH_SHORT).show()
			}
		}
		return
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			PERMISSION_REQUEST_CODE_PICK_IMAGE -> {
				handleMediaPickPermissionRequest(grantResults, UwMediaPicker.GalleryMode.ImageGallery, ::openUwMediaPicker)
			}
			PERMISSION_REQUEST_CODE_PICK_VIDEO -> {
				handleMediaPickPermissionRequest(grantResults, UwMediaPicker.GalleryMode.VideoGallery, ::openUwMediaPicker)
			}
			PERMISSION_REQUEST_CODE_PICK_IMAGE_VIDEO -> {
				handleMediaPickPermissionRequest(grantResults, UwMediaPicker.GalleryMode.ImageAndVideoGallery, ::openUwMediaPicker)
			}
		}
	}
	
	companion object {
		const val PERMISSION_REQUEST_CODE_PICK_IMAGE = 9
		const val PERMISSION_REQUEST_CODE_PICK_VIDEO = 10
		const val PERMISSION_REQUEST_CODE_PICK_IMAGE_VIDEO = 11
		
		init {
			// For using vector drawables on lower APIs
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}
