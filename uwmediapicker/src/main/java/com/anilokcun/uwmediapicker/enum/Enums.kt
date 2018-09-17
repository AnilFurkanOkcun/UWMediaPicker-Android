package com.anilokcun.uwmediapicker.enum

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal enum class Enums {
	MissingViewTypeException {
		override fun toString(): String {
			return "UW Media Picker, Missing view type exception"
		}
	},
	UWMediaPickerSettingsKey
}