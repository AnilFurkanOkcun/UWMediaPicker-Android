package com.anilokcun.uwmediapicker.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class ImagePreviewDialog : DialogFragment() {

	private var imageUri: Uri? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (imageUri == null) {
			dismiss()
			return null
		}
		// Set rootView
		val imgRootView = ImageView(inflater.context)

		val root = FrameLayout(context!!)
		root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setContentView(root)
		dialog.window.setBackgroundDrawable(ColorDrawable(Color.BLACK))
		dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

		Glide.with(this)
			.load(imageUri)
			.into(imgRootView)

		return imgRootView
	}

	internal fun showPreview(manager: FragmentManager?, imageUri: Uri?) {
		super.show(manager, "ImagePreviewDialogTag")
		this.imageUri = imageUri
	}
}