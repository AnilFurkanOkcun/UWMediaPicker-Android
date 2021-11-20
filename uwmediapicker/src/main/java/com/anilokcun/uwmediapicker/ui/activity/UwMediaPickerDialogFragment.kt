package com.anilokcun.uwmediapicker.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anilokcun.uwmediapicker.ImageCompressor
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.anilokcun.uwmediapicker.adapter.GalleryMediaRvAdapter
import com.anilokcun.uwmediapicker.helper.logError
import com.anilokcun.uwmediapicker.helper.toUri
import com.anilokcun.uwmediapicker.helper.toastStringRes
import com.anilokcun.uwmediapicker.listener.GalleryMediaOnLongClickListener
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.*
import com.anilokcun.uwmediapicker.provider.GalleryMediaDataProvider
import com.anilokcun.uwmediapicker.ui.GalleryItemDecoration
import com.anilokcun.uwmediapicker.ui.dialog.ImagePreviewDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_uw_media_picker.*
import kotlinx.coroutines.*
import java.io.File

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class UwMediaPickerDialogFragment : DialogFragment() {

    private var job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

	private val galleryMediaProvider by lazy { GalleryMediaDataProvider(requireContext()) }

	private var resultCallback: ((List<UwMediaPickerMediaModel>?) -> Unit)? = null
	private var cancelCallback: (() -> Unit)? = null

	private lateinit var settings: UWMediaPickerSettingsModel
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
	private val selectedMediaList by lazy { arrayListOf<UwMediaPickerMediaModel>() }
	
	private var lastOpenedBucketName: String? = null
	private var lastOpenedBucketId: String? = null
	private var isBucketOpened: Boolean = false
	
	private var galleryGridSize = 0
	private var galleryTextSize = 10f
	
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
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = super.onCreateDialog(savedInstanceState)
		// Add back button listener
		dialog.setOnKeyListener { _, keyCode, keyEvent ->
			// getAction to make sure this doesn't double fire
			if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
				onBackPressed()
				true // Capture onKey
			} else false
			// Don't capture
		}
		return dialog
	}
	
	override fun onStart() {
		super.onStart()
		dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.activity_uw_media_picker, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initPage()
		setListeners()
	}
	
	override fun getTheme() = R.style.Theme_AppCompat_Light_NoActionBar
	
	/** Adds Listeners to UI elements */
	private fun setListeners() {
		// Toolbar Back Button
		imgToolbarBack.setOnClickListener {
			onBackPressed()
		}
		// Toolbar Done Button
		tvToolbarDone.setOnClickListener {
			returnResult()
		}
	}
	
	/** Does initial actions */
	private fun initPage() {
		// Get Settings
		settings = arguments?.getParcelable(KEY_SETTINGS)!!
		// Set Status bar icon colors
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			dialog?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorUwMediaPickerStatusBar)
		}
		if (settings.lightStatusBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			tvToolbarDone.systemUiVisibility =
				tvToolbarDone.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
		}
		// Get Device Width in Px
		val deviceWidth = resources.displayMetrics.widthPixels
		// Calculate gallery grid width (size)
		galleryGridSize = deviceWidth / settings.gridColumnCount
		// Set gallery items' texts size according to gridColumnCount
		galleryTextSize = when (settings.gridColumnCount) {
			1 -> 19F
			2 -> 14F
			else -> (14 - settings.gridColumnCount).toFloat()
		}
		// Set Title
		updateToolbarTitle()
		
		// Set RecyclerView
		recyclerView.layoutManager = GridLayoutManager(requireContext(), settings.gridColumnCount)
		recyclerView.itemAnimator = DefaultItemAnimator()
		recyclerView.addItemDecoration(GalleryItemDecoration(
			resources.getDimensionPixelSize(R.dimen.uwmediapicker_gallery_spacing),
			settings.gridColumnCount
		))
		
		// Get Data for RecyclerView
		// Async task started, show progress screen
		lytProgressBar.visibility = View.VISIBLE
		coroutineScope.launch {
			try {
				val buckets = withContext(Dispatchers.IO) {
					when (settings.galleryMode) {
						UwMediaPicker.GalleryMode.ImageGallery -> {
							galleryMediaProvider.getImageBuckets()
						}
						UwMediaPicker.GalleryMode.VideoGallery -> {
							galleryMediaProvider.getVideoBuckets()
						}
						UwMediaPicker.GalleryMode.ImageAndVideoGallery -> {
							galleryMediaProvider.getImageAndVideoBuckets()
						}
					}
				}
				mediaBucketsList.addAll(buckets)
				// This is UI/Main thread
				recyclerView.adapter =
					GalleryMediaRvAdapter(mediaBucketsList, galleryGridSize, galleryTextSize, galleryMediaBucketClickListener)
			} catch (e: CancellationException) {
				throw e
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
				lytProgressBar?.visibility = View.GONE
			}
		}
	}
	
	/** Sets Media Buckets(Albums) Recycler View */
	private fun setupMediaBucketRecyclerView() {
		// Setup recyclerView with mediaList, GalleryMediaClickListener and OnLongClickListener
		recyclerView.adapter = GalleryMediaRvAdapter(mediaList, galleryGridSize, galleryTextSize, galleryMediaClickListener, object : GalleryMediaOnLongClickListener {
			// Open ImagePreviewDialog when long clicked to image
			override fun onLongClick(imagePath: String?) {
				imagePreviewDialog.showPreview(parentFragmentManager, imagePath?.toUri())
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
	
	/** Gallery Media Bucket's click function */
	private fun onGalleryMediaBucketClick(position: Int) {
		if (mediaBucketsList[position].id == null) {
			requireContext().toastStringRes(R.string.uwmediapicker_toast_error_media_bucket_open_failed)
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
			coroutineScope.launch {
				try {
					taskOpenMediaBucket = async(Dispatchers.IO) {
						val selectedMediaPaths = selectedMediaList.map { it.mediaPath }
						// This is background thread.
						when (settings.galleryMode) {
							UwMediaPicker.GalleryMode.ImageGallery -> {
								galleryMediaProvider.getImagesOfBucket(mediaBucketsList[position].id!!, selectedMediaPaths)
							}
							UwMediaPicker.GalleryMode.VideoGallery -> {
								galleryMediaProvider.getVideosOfBucket(mediaBucketsList[position].id!!, selectedMediaPaths)
							}
							UwMediaPicker.GalleryMode.ImageAndVideoGallery -> {
								galleryMediaProvider.getImagesAndVideosOfBucket(mediaBucketsList[position].id!!, selectedMediaPaths)
							}
						}
					}
					if (taskOpenMediaBucket != null) {
						mediaList.addAll(taskOpenMediaBucket!!.await())
					}
					setupMediaBucketRecyclerView()
				} catch (e: Exception) {
					// If error occurred, do not save this bucket as a lastOpenedBucket and go back
					e.toString().logError()
					requireContext().toastStringRes(R.string.uwmediapicker_toast_error_media_bucket_open_failed)
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
			// Setup RecyclerView
			setupMediaBucketRecyclerView()
		}
	}
	
	/** Gallery Media's click function */
	private fun onGalleryMediaClick(position: Int) {
		if (mediaList[position].mediaPath == null) {
			requireContext().toastStringRes(R.string.uwmediapicker_toast_error_media_select_failed)
			return
		}
		// If media already selected;
		if (mediaList[position].selected) {
			// Mark clicked media as unselected, remove media's path from selectedMediaPathList
			selectedMediaList.removeAll {
				it.mediaPath == mediaList[position].mediaPath
			}
			
			mediaList[position].selected = false
		} else {
			// If the maximum number of media is selected
			if (settings.maxSelectableMediaCount != null && selectedMediaList.size == settings.maxSelectableMediaCount) {
				toastMaxMediaCountError?.cancel()
				// Show error toast about max selectable media count
				toastMaxMediaCountError = requireContext().toastStringRes(R.string.uwmediapicker_toast_error_max_media_selected)
				// Animate(Blink) MediaSelectCount Text
				tvToolbarMediaSelectCount.startAnimation(blinkAnimation)
				return
			}//Else, select it;
			// Mark clicked media as selected, add media's path to selectedMediaPathList
			mediaList[position].mediaPath?.let {
				val mediaType = if (mediaList[position] is GalleryImageModel) UwMediaPickerMediaType.IMAGE else UwMediaPickerMediaType.VIDEO
				selectedMediaList.add(UwMediaPickerMediaModel(it, mediaType))
				mediaList[position].selected = true
			}
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
						UwMediaPicker.GalleryMode.ImageAndVideoGallery -> getString(R.string.uwmediapicker_toolbar_title_image_and_video_library)
					}
				} else {
					lastOpenedBucketName
				}
	}
	
	/** Updates the SelectedMediaCount Text, hide it if no media selected
	 * And if any media is selected activate the Done Button */
	private fun updateSelectedMediaCountTextAndDoneButton() {
		if (selectedMediaList.isNotEmpty()) {
			if (settings.maxSelectableMediaCount != null) {
				tvToolbarMediaSelectCount.text = getString(
						R.string.uwmediapicker_toolbar_text_uw_media_picker_selected_media_count,
						selectedMediaList.size, settings.maxSelectableMediaCount)
				tvToolbarMediaSelectCount.visibility = View.VISIBLE
			} else {
				tvToolbarMediaSelectCount.visibility = View.GONE
			}
			tvToolbarDone.isEnabled = true
		} else {
			tvToolbarMediaSelectCount.visibility = View.GONE
			tvToolbarDone.isEnabled = false
		}
	}
	
	@SuppressLint("InflateParams")
	/** Opens simple progress dialog */
	private fun getProgressDialog(): AlertDialog {
		// Inflates the dialog with custom view
		val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.uwmediapicker_dialog_progress, null)
		
		return AlertDialog.Builder(requireContext())
			.setView(dialogView)
			.setCancelable(false)
			.create().apply {
				window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			}
	}
	
	/** Returns selected image paths as result */
	private fun returnResult() {
		if (selectedMediaList.isEmpty()) {
			requireContext().toastStringRes(R.string.uwmediapicker_toast_error_media_select_failed)
			dismiss()
			return
		}
		coroutineScope.launch {
			val progressDialog = getProgressDialog().apply { show() }
			var hasErrorOccurred = false
			
			if (settings.imageCompressionEnabled) {
				withContext(Dispatchers.IO) {
					// This is background thread.
					val imageCompressor = ImageCompressor(
							settings.compressionMaxWidth,
							settings.compressionMaxHeight,
							settings.compressFormat,
							settings.compressionQuality,
							settings.compressedFileDestinationPath)
					for ((index, selectedMedia) in selectedMediaList.withIndex()) {
						if (selectedMedia.mediaType == UwMediaPickerMediaType.IMAGE) {
							try {
								val compressedImageFile = imageCompressor.compress(File(selectedMedia.mediaPath))
								selectedMediaList[index].mediaPath = compressedImageFile.absolutePath
							} catch (e: Exception) {
								e.message.logError()
								hasErrorOccurred = true
							}
						}
					}
				}
			}
			// This is UI/Main thread
			progressDialog.dismiss()
			// If an error has occurred and some media can still be selected; show toast message about it
			if (hasErrorOccurred) {
				requireContext().toastStringRes(R.string.uwmediapicker_toast_error_some_media_select_failed)
			}
			resultCallback?.invoke(selectedMediaList)
			dismiss()
		}
	}
	
	private fun onBackPressed() {
		// If any bucked opened; close it
		if (isBucketOpened) {
			// Hide progress screen
			lytProgressBar.visibility = View.GONE
			// Cancel the open media bucket task
			if (taskOpenMediaBucket != null) taskOpenMediaBucket?.cancel()
			// Set RecyclerView with MediaBucketList
			recyclerView.adapter = GalleryMediaRvAdapter(mediaBucketsList, galleryGridSize, galleryTextSize, galleryMediaBucketClickListener)
			// Set isBucketOpened false
			isBucketOpened = false
			// Update ToolbarTitle
			updateToolbarTitle()
		} else {
			cancelCallback?.invoke()
			dismiss()
		}
	}
	
	override fun onDestroy() {
		job.cancel()
		super.onDestroy()
	}
	
	fun setResultCallback(resultCallback: (List<UwMediaPickerMediaModel>?) -> Unit) {
		this.resultCallback = resultCallback
	}

	fun setCancelCallback(cancelCallback: () -> Unit) {
		this.cancelCallback = cancelCallback
	}
	
	companion object {
		private const val KEY_SETTINGS = "KEY_SETTINGS"
		
		fun newInstance(settings: UWMediaPickerSettingsModel): UwMediaPickerDialogFragment {
			return UwMediaPickerDialogFragment().apply {
				arguments = Bundle().apply {
					putParcelable(KEY_SETTINGS, settings)
				}
			}
		}
		
		
		init {
			// For using vector drawables on lower APIs
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		}
	}
}