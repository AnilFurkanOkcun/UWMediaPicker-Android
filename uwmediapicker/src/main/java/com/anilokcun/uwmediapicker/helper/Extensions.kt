package com.anilokcun.uwmediapicker.helper

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.anilokcun.uwmediapicker.BuildConfig

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

/** Extension for simplify create basic Toast Message with String resource */
fun Context.toastStringRes(resId: Int, duration: Int = Toast.LENGTH_SHORT): Toast {
	return Toast.makeText(this, this.getString(resId), duration).apply { show() }
}

/** Converts strings to uri */
fun String.toUri(): Uri = Uri.parse("file://$this")

/** Logs Error */
fun String?.logError() {
	if (BuildConfig.DEBUG) {
		Log.e("UwMediaPicker", this.toString())
	}
}