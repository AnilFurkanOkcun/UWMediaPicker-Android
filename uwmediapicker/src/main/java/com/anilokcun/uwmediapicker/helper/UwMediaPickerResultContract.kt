package com.anilokcun.uwmediapicker.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.anilokcun.uwmediapicker.constants.Constants
import com.anilokcun.uwmediapicker.model.UWMediaPickerSettingsModel
import com.anilokcun.uwmediapicker.model.UwMediaPickerMediaModel
import com.anilokcun.uwmediapicker.ui.activity.UwMediaPickerActivity

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	20.06.2020
 */

class UwMediaPickerResultContract : ActivityResultContract<UWMediaPickerSettingsModel, List<UwMediaPickerMediaModel>?>() {
	override fun createIntent(context: Context, input: UWMediaPickerSettingsModel): Intent {
		return Intent(context, UwMediaPickerActivity::class.java).apply {
			putExtra(Constants.UW_MEDIA_PICKER_SETTINGS_KEY, input)
		}
	}
	
	override fun parseResult(resultCode: Int, intent: Intent?): List<UwMediaPickerMediaModel>? {
		if (resultCode != Activity.RESULT_OK || intent == null) return null
		
		return intent.getParcelableArrayListExtra(Constants.UW_MEDIA_PICKER_RESULT_KEY)
	}
	
}