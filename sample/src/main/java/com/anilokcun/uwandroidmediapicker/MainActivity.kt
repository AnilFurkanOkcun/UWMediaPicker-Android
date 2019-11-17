package com.anilokcun.uwandroidmediapicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anilokcun.uwmediapicker.UwMediaPicker

/**
 * Author   	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

class MainActivity : AppCompatActivity() {

	private val lytRoot by lazy { findViewById<FrameLayout>(R.id.activity_main_lyt_root) }
	private val etMaxSelectableMediaCount by lazy { findViewById<EditText>(R.id.activity_main_et_max_selectable_media_count) }
	private val etGridColumnCount by lazy { findViewById<EditText>(R.id.activity_main_et_grid_column_count) }
	private val switchImageCompression by lazy { findViewById<Switch>(R.id.activity_main_switch_image_compression) }
	private val tvMaxWidth by lazy { findViewById<TextView>(R.id.activity_main_tv_max_width) }
	private val etMaxWidth by lazy { findViewById<EditText>(R.id.activity_main_et_max_width) }
	private val tvMaxHeight by lazy { findViewById<TextView>(R.id.activity_main_tv_max_height) }
	private val etMaxHeight by lazy { findViewById<EditText>(R.id.activity_main_et_max_height) }
	private val tvQuality by lazy { findViewById<TextView>(R.id.activity_main_tv_quality) }
	private val etQuality by lazy { findViewById<EditText>(R.id.activity_main_et_quality) }
	private val btnOpenImageGallery by lazy { findViewById<TextView>(R.id.activity_main_btn_open_image_gallery) }
	private val btnOpenVideoGallery by lazy { findViewById<TextView>(R.id.activity_main_btn_open_video_gallery) }
	private val tvSelectedMediaTitle by lazy { findViewById<TextView>(R.id.activity_main_tv_selected_media_title) }
	private val rvSelectedMedia by lazy { findViewById<RecyclerView>(R.id.activity_main_rv_selected_media) }


	private val selectedMediaGridColumnCount = 5

	private val selectedMediaPaths by lazy { arrayListOf<String>() }

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
		GRID_SIZE = deviceWidth / selectedMediaGridColumnCount
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
		rvSelectedMedia.adapter = SelectedMediaRvAdapter(selectedMediaPaths)
		addListeners()
	}

	override fun onRequestPermissionsResult(requestCode: Int,
											permissions: Array<String>, grantResults: IntArray) {
		when (requestCode) {
			REQUEST_CODE_PICK_IMAGE, REQUEST_CODE_PICK_VIDEO -> {
				handleMediaPickRequest(grantResults, requestCode, ::openUwImagePicker)
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (data == null || resultCode != Activity.RESULT_OK) return
		when (requestCode) {
			REQUEST_CODE_PICK_IMAGE, REQUEST_CODE_PICK_VIDEO -> {
				try {
					selectedMediaPaths.addAll(data.getStringArrayExtra(UwMediaPicker.UwMediaPickerResultKey))
					if (selectedMediaPaths.isNotEmpty()) {
						(rvSelectedMedia.adapter as SelectedMediaRvAdapter).update(selectedMediaPaths)
						tvSelectedMediaTitle.visibility = View.VISIBLE
					} else {
						// Error with Image Selecting
						Log.e("UwMediaPicker", "Error")
					}
				} catch (e: Exception) {
					// Error with Image Selecting
					Log.e("UwMediaPicker", e.message)
				}
			}
		}
	}

	private fun addListeners() {
		/** Image Compression Option Switch's CheckedChangeListener */
		switchImageCompression.setOnCheckedChangeListener { _, isChecked ->
			tvMaxWidth.isEnabled = isChecked
			etMaxWidth.isEnabled = isChecked
			tvMaxHeight.isEnabled = isChecked
			etMaxHeight.isEnabled = isChecked
			tvQuality.isEnabled = isChecked
			etQuality.isEnabled = isChecked
			lytRoot.requestFocus()
		}
		/** Open Image Gallery Button's ClickListener */
		btnOpenImageGallery.setOnClickListener {
			requestToOpenImagePicker(REQUEST_CODE_PICK_IMAGE, ::openUwImagePicker)
		}
		/** Open Video Gallery Button's ClickListener */
		btnOpenVideoGallery.setOnClickListener {
			requestToOpenImagePicker(REQUEST_CODE_PICK_VIDEO, ::openUwImagePicker)
		}
	}

	/** Opens UwMediaPicker for select images*/
	private fun openUwImagePicker(requestCode: Int) {
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
		when (requestCode) {
			REQUEST_CODE_PICK_IMAGE ->
				UwMediaPicker.with(this)
					.setRequestCode(requestCode)
					.setGalleryMode(UwMediaPicker.GalleryMode.ImageGallery)
					.setGridColumnCount(gridColumnCount)
					.setMaxSelectableMediaCount(maxSelectableMediaCount)
					.setLightStatusBar(true)
					.enableImageCompression(switchImageCompression.isChecked)
					.setCompressionMaxWidth(maxWidth)
					.setCompressionMaxHeight(maxHeight)
					.setCompressFormat(Bitmap.CompressFormat.JPEG)
					.setCompressionQuality(quality)
					.setCompressedFileDestinationPath(Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
						.absolutePath)
					.open()
			REQUEST_CODE_PICK_VIDEO -> UwMediaPicker.with(this)
				.setRequestCode(requestCode)
				.setGalleryMode(UwMediaPicker.GalleryMode.VideoGallery)
				.setGridColumnCount(gridColumnCount)
				.setMaxSelectableMediaCount(maxSelectableMediaCount)
				.open()
		}
	}

	/** Request to open Image Picker Intent and Handle the permissions */
	private fun requestToOpenImagePicker(requestCode: Int, openMediaPickerFunc: ((Int) -> Unit)) {
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
			openMediaPickerFunc.invoke(requestCode)
		}
	}

	/** Handles Media Pick Request */
	private fun handleMediaPickRequest(grantResults: IntArray, requestCode: Int, openImagePickerFunc: ((Int) -> Unit)) {
		// If request is cancelled, the result arrays are empty.
		if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
			// Permission granted
			openImagePickerFunc.invoke(requestCode)
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

	companion object {
		var GRID_SIZE = 0

		const val REQUEST_CODE_PICK_IMAGE = 10
		const val REQUEST_CODE_PICK_VIDEO = 11

		init {
			// For using vector drawables on lower APIs
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}
