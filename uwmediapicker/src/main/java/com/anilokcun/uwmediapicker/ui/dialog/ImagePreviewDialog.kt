package com.anilokcun.uwmediapicker.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class ImagePreviewDialog : androidx.fragment.app.DialogFragment() {

	private var imageUri: Uri? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (imageUri == null) {
			dismiss()
			return null
		}
		// Set rootView
		val imgRootView = ImageView(inflater.context)

		val root = FrameLayout(context!!)
		root.layoutParams =
				ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		dialog?.apply {
			requestWindowFeature(Window.FEATURE_NO_TITLE)
			setContentView(root)
			window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
			window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		}


		Glide.with(this)
			.load(imageUri)
			.into(imgRootView)

		return imgRootView
	}

	internal fun showPreview(manager: FragmentManager?, imageUri: Uri?) {
		if (manager == null) return
		super.show(manager, "ImagePreviewDialogTag")
		this.imageUri = imageUri
	}
}