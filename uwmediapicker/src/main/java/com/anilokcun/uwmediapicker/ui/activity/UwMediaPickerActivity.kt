package com.anilokcun.uwmediapicker.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anilokcun.uwmediapicker.ImageCompressor
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.anilokcun.uwmediapicker.adapter.GalleryMediaRvAdapter
import com.anilokcun.uwmediapicker.constants.Constants
import com.anilokcun.uwmediapicker.helper.logError
import com.anilokcun.uwmediapicker.helper.toUri
import com.anilokcun.uwmediapicker.helper.toastStringRes
import com.anilokcun.uwmediapicker.listener.GalleryMediaOnLongClickListener
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.BaseGalleryMediaModel
import com.anilokcun.uwmediapicker.model.GalleryMediaBucketModel
import com.anilokcun.uwmediapicker.model.UWMediaPickerSettingsModel
import com.anilokcun.uwmediapicker.provider.GalleryMediaDataProvider
import com.anilokcun.uwmediapicker.ui.GalleryItemDecoration
import com.anilokcun.uwmediapicker.ui.dialog.ImagePreviewDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.File

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class UwMediaPickerActivity : AppCompatActivity() {

	private val galleryMediaProvider by lazy { GalleryMediaDataProvider(this) }

	private lateinit var settings: UWMediaPickerSettingsModel

	// UI Values
	private val imgToolbarBack by lazy { findViewById<ImageView>(R.id.activity_uw_media_picker_img_toolbar_back) }
	private val tvToolbarTitle by lazy { findViewById<TextView>(R.id.activity_uw_media_picker_tv_toolbar_title) }
	private val tvToolbarMediaSelectCount by lazy { findViewById<TextView>(R.id.activity_uw_media_picker_tv_toolbar_selected_media_count) }
	private val tvToolbarDone by lazy { findViewById<TextView>(R.id.activity_uw_media_picker_tv_toolbar_done) }
	private val lytProgressBar by lazy { findViewById<FrameLayout>(R.id.activity_uw_media_picker_lyt_progressbar) }
	private val recyclerView by lazy { findViewById<RecyclerView>(R.id.activity_uw_media_picker_rv) }

	private var toastMaxMediaCountError: Toast? = null

	private val blinkAnimation: AlphaAnimation by lazy {
		// Change alpha from fully visible to invisible
		AlphaAnimation(1f, 0f).apply {
			this.duration = 300
			this.interpolator = LinearInterpolator()
			this.repeatCount = 3
			this.repeatMode = Animation.REVERSE
		}
	}
	private val imagePreviewDialog by lazy { ImagePreviewDialog() }

	private var taskOpenMediaBucket: Deferred<ArrayList<out BaseGalleryMediaModel>>? = null

	private val mediaBucketsList by lazy { arrayListOf<GalleryMediaBucketModel>() }
	private val mediaList by lazy { arrayListOf<BaseGalleryMediaModel>() }
	private val selectedMediaPathList by lazy { arrayListOf<String>() }

	private var lastOpenedBucketName: String? = null
	private var lastOpenedBucketId: String? = null
	private var isBucketOpened: Boolean = false

	// MediaBucket Click Listener
	private val galleryMediaBucketClickListener: OnRVItemClickListener by lazy {
		object : OnRVItemClickListener {
			override fun onClick(position: Int) {
				onGalleryMediaBucketClick(position)
			}
		}
	}

	// GalleryMedia Click Listener
	private val galleryMediaClickListener by lazy {
		object : OnRVItemClickListener {
			override fun onClick(position: Int) {
				onGalleryMediaClick(position)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_uw_media_picker)
		initPage()
		addListeners()
	}

	override fun onBackPressed() {
		// If any bucked opened; close it
		if (isBucketOpened) {
			// Hide progress screen
			lytProgressBar.visibility = View.GONE
			// Cancel the open media bucket task
			if (taskOpenMediaBucket != null) taskOpenMediaBucket?.cancel()
			// Set RecyclerView with MediaBucketList
			recyclerView.adapter = GalleryMediaRvAdapter(mediaBucketsList, galleryMediaBucketClickListener)
			// Set isBucketOpened false
			isBucketOpened = false
			// Update ToolbarTitle
			updateToolbarTitle()
		} else {
			super.onBackPressed()
		}
	}

	/** Does initial actions */
	private fun initPage() {
		// Get Settings
		settings = intent?.extras?.getParcelable(Constants.UWMEDIA_PICKER_SETTINGS_KEY)!!
		// Set Status bar icon colors
		if (settings.lightStatusBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			window.statusBarColor = getColor(R.color.colorUwMediaPickerStatusBar)
			tvToolbarDone.systemUiVisibility =
				tvToolbarDone.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

		}
		// Get Device Width in Px
		val displayMetrics = DisplayMetrics()
		windowManager.defaultDisplay.getMetrics(displayMetrics)
		val deviceWidth = displayMetrics.widthPixels
		// Calculate gallery grid width (size)
		GALLERY_GRID_SIZE = deviceWidth / settings.gridColumnCount
		// Set gallery items' texts size according to gridColumnCount
		GALLERY_TEXT_SIZE_SP = when (settings.gridColumnCount) {
			1 -> 19F
			2 -> 14F
			else -> (14 - settings.gridColumnCount).toFloat()
		}
		// Set Title
		updateToolbarTitle()

		// Set RecyclerView
		recyclerView.layoutManager =
				GridLayoutManager(applicationContext, settings.gridColumnCount)
		recyclerView.itemAnimator = DefaultItemAnimator()
		recyclerView.addItemDecoration(GalleryItemDecoration(
			resources.getDimensionPixelSize(R.dimen.uwmediapicker_gallery_spacing),
			settings.gridColumnCount
		))

		// Get Data for RecyclerView
		// Async task started, show progress screen
		lytProgressBar.visibility = View.VISIBLE
		GlobalScope.launch(Dispatchers.Main) {
			try {
				val task = async(Dispatchers.IO) {
					// This is background thread.
					when (settings.galleryMode) {
						UwMediaPicker.GalleryMode.ImageGallery -> {
							galleryMediaProvider.getImageBuckets()
						}
						UwMediaPicker.GalleryMode.VideoGallery -> {
							galleryMediaProvider.getVideoBuckets()
						}
					}.apply {
					}
				}
				mediaBucketsList.addAll(task.await())
				// This is UI/Main thread
				recyclerView.adapter = GalleryMediaRvAdapter(mediaBucketsList, galleryMediaBucketClickListener)
			} catch (e: Exception) {
				Snackbar
					.make(
						tvToolbarDone,
						getString(R.string.uwmediapicker_snackbar_error_gallery_open_failed),
						Snackbar.LENGTH_INDEFINITE)
					.setAction(
						getString(R.string.uwmediapicker_snackbar_action_retry)) {
						initPage()
					}.show()
			} finally {
				// Hide progress screen
				lytProgressBar.visibility = View.GONE
			}
		}
	}

	/** Adds Listeners to UI elements */
	private fun addListeners() {
		// Toolbar Back Button
		imgToolbarBack.setOnClickListener { onBackPressed() }

		// Toolbar Done Button
		tvToolbarDone.setOnClickListener {
			if (!tvToolbarDone.isActivated) return@setOnClickListener
			returnResult()
		}
	}

	/** Returns selected image paths as result */
	private fun returnResult() {
		// If image compression enabled and it's an image gallery;
		if (settings.imageCompressionEnabled && settings.galleryMode == UwMediaPicker.GalleryMode.ImageGallery) {
			val progressDialog = getProgressDialog().apply { show() }
			val compressedMediaPathList = arrayListOf<String>()
			var hasErrorOccurred = false
			GlobalScope.launch {
				val task = async(Dispatchers.IO) {
					// This is background thread.
					val imageCompressor = ImageCompressor(
						settings.compressionMaxWidth,
						settings.compressionMaxHeight,
						settings.compressFormat,
						settings.compressionQuality,
						settings.compressedFileDestinationPath)
					for (item in selectedMediaPathList) {
						try {
							val compressedImageFile = imageCompressor.compress(File(item))
							compressedMediaPathList.add(compressedImageFile.absolutePath)
						} catch (e: Exception) {
							e.message.logError()
							hasErrorOccurred = true
						}
					}
				}
				task.await()
				// This is UI/Main thread
				progressDialog.dismiss()
				// If an error has occurred and some media can still be selected; show toast message about it
				if (compressedMediaPathList.isEmpty()) {
					this@UwMediaPickerActivity.toastStringRes(R.string.uwmediapicker_toast_error_media_select_failed)
					setResult(0, Intent())
					finish()
					return@launch
				}
				if (hasErrorOccurred && compressedMediaPathList.isNotEmpty()) {
					this@UwMediaPickerActivity.toastStringRes(R.string.uwmediapicker_toast_error_some_media_select_failed)
				}
				val resultIntent = Intent()
				resultIntent.putExtra(
					UwMediaPicker.UwMediaPickerResultKey,
					arrayOf<String>().plus(compressedMediaPathList)
				)
				setResult(Activity.RESULT_OK, resultIntent)
				finish()
			}
		} else {
			val resultIntent = Intent()
			resultIntent.putExtra(
				UwMediaPicker.UwMediaPickerResultKey,
				arrayOf<String>().plus(selectedMediaPathList)
			)
			setResult(Activity.RESULT_OK, resultIntent)
			finish()
		}
	}

	/** Gallery Media Bucket's click function */
	private fun onGalleryMediaBucketClick(position: Int) {
		if (mediaBucketsList[position].id == null) {
			this.toastStringRes(R.string.uwmediapicker_toast_error_media_bucket_open_failed)
			return
		}
		// Set isBucketOpened True
		isBucketOpened = true
		// If clicked bucket is not the last opened bucket; get this bucket's media to mediaList
		if (lastOpenedBucketId != mediaBucketsList[position].id) {
			// Save this bucket as a last opened bucket for this action and title updates
			lastOpenedBucketName = mediaBucketsList[position].name
			lastOpenedBucketId = mediaBucketsList[position].id
			// Update ToolbarTitle
			updateToolbarTitle()
			mediaList.clear()
			// Async task started, show progress screen
			lytProgressBar.visibility = View.VISIBLE
			GlobalScope.launch(Dispatchers.Main) {
				try {
					taskOpenMediaBucket = async(Dispatchers.IO) {
						// This is background thread.
						when (settings.galleryMode) {
							UwMediaPicker.GalleryMode.ImageGallery -> {
								galleryMediaProvider.getImages(mediaBucketsList[position].id!!, selectedMediaPathList)
							}
							UwMediaPicker.GalleryMode.VideoGallery -> {
								galleryMediaProvider.getVideos(mediaBucketsList[position].id!!, selectedMediaPathList)
							}
						}
					}
					if (taskOpenMediaBucket != null) {
						mediaList.addAll(taskOpenMediaBucket!!.await())
					}
					setMediaBucketRecyclerView()
				} catch (e: Exception) {
					// If error occurred, do not save this bucket as a lastOpenedBucket and go back
					e.toString().logError()
					this@UwMediaPickerActivity.toastStringRes(R.string.uwmediapicker_toast_error_media_bucket_open_failed)
					lastOpenedBucketName = ""
					lastOpenedBucketId = ""
					onBackPressed()
				} finally {
					// Hide progress screen
					lytProgressBar.visibility = View.GONE
				}
			}
		} else {
			// Hide progress screen
			lytProgressBar.visibility = View.GONE
			// Update ToolbarTitle
			updateToolbarTitle()
			// Set RecyclerView
			setMediaBucketRecyclerView()
		}
	}

	/** Sets Media Buckets(Albums) Recycler View */
	private fun setMediaBucketRecyclerView() {
		// Set recyclerview with mediaList, GalleryMediaClickListener and OnLongClickListener
		recyclerView.adapter = GalleryMediaRvAdapter(mediaList, galleryMediaClickListener, object : GalleryMediaOnLongClickListener {
			// Open ImagePreviewDialog when long clicked to image
			override fun onLongClick(imagePath: String?) {
				imagePreviewDialog
					.showPreview(supportFragmentManager, imagePath?.toUri())
			}
		})
		// Add OnItemTouchListener to RecyclerView
		recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
			override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

			override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
				// When touch intercept if user quit holding, close ImagePreviewDialog
				if (imagePreviewDialog.isVisible && e.action == MotionEvent.ACTION_UP)
					imagePreviewDialog.dismiss()
				return false
			}

			override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
		})
	}

	/** Gallery Media's click function */
	private fun onGalleryMediaClick(position: Int) {
		if (mediaList[position].mediaPath == null) {
			this.toastStringRes(R.string.uwmediapicker_toast_error_media_select_failed)
			return
		}
		// If media already selected;
		if (mediaList[position].selected) {
			// Mark clicked media as unselected, remove media's path from selectedMediaPathList
			selectedMediaPathList.remove(mediaList[position].mediaPath)
			mediaList[position].selected = false
		} else {
			// If the maximum number of media is selected
			if (settings.maxSelectableMediaCount != null && selectedMediaPathList.size == settings.maxSelectableMediaCount) {
				toastMaxMediaCountError?.cancel()
				// Show error toast about max selectable media count
				toastMaxMediaCountError = this.toastStringRes(R.string.uwmediapicker_toast_error_max_media_selected)
				// Animate(Blink) MediaSelectCount Text
				tvToolbarMediaSelectCount.startAnimation(blinkAnimation)
				return
			}//Else, select it;
			// Mark clicked media as selected, add media's path to selectedMediaPathList
			mediaList[position].mediaPath?.let { selectedMediaPathList.add(it) }
			mediaList[position].selected = true
		}
		updateSelectedMediaCountTextAndDoneButton()
		recyclerView.adapter?.notifyItemChanged(position)
	}

	/** Updates the ToolbarTitle according to opened library type or opened bucket's name */
	private fun updateToolbarTitle() {
		tvToolbarTitle.text =
			if (!isBucketOpened) {
				when (settings.galleryMode) {
					UwMediaPicker.GalleryMode.ImageGallery -> getString(R.string.uwmediapicker_toolbar_title_image_library)
					UwMediaPicker.GalleryMode.VideoGallery -> getString(R.string.uwmediapicker_toolbar_title_video_library)
				}
			} else {
				lastOpenedBucketName
			}
	}

	/** Updates the SelectedMediaCount Text, hide it if no media selected
	 * And if any media is selected activate the Done Button */
	private fun updateSelectedMediaCountTextAndDoneButton() {
		if (selectedMediaPathList.isNotEmpty()) {
			if (settings.maxSelectableMediaCount != null) {
				tvToolbarMediaSelectCount.text = getString(
					R.string.uwmediapicker_toolbar_text_uw_media_picker_selected_media_count,
					selectedMediaPathList.size, settings.maxSelectableMediaCount)
				tvToolbarMediaSelectCount.visibility = View.VISIBLE
			} else {
				tvToolbarMediaSelectCount.visibility = View.GONE
			}
			tvToolbarDone.isActivated = true
		} else {
			tvToolbarMediaSelectCount.visibility = View.GONE
			tvToolbarDone.isActivated = false
		}
	}

	@SuppressLint("InflateParams")
			/** Opens simple progress dialog */
	fun getProgressDialog(): AlertDialog {
		// Inflates the dialog with custom view
		val dialogView = LayoutInflater.from(this).inflate(R.layout.uwmediapicker_dialog_progress, null)

		return AlertDialog.Builder(this)
			.setView(dialogView)
			.setCancelable(false)
			.create().apply {
					window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			}
	}

	companion object {

		var GALLERY_GRID_SIZE = 0
		var GALLERY_TEXT_SIZE_SP = 10F

		init {
			// For using vector drawables on lower APIs
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}