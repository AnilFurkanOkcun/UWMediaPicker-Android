UW Media Picker-Android
=======
[![Release](https://jitpack.io/v/AnilFurkanOkcun/UWMediaPicker-Android.svg)](https://jitpack.io/#AnilFurkanOkcun/UWMediaPicker-Android)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/AnilFurkanOkcun/UWMediaPicker-Android/blob/master/LICENSE)

Easy to use and customizable media picker library to pick multiple images(with compression) and videos for Android.

<p align="center">
	<img src="https://github.com/AnilFurkanOkcun/UWMediaPicker-Android/blob/master/uw_media_picker_demo.gif?raw=true" alt="UW Media Picker"/>
</p>

[*See all screenshots*](https://github.com/AnilFurkanOkcun/UWMediaPicker-Android#screenshots)

## Supported Features

* Multiple image or video selecting
* Image compression
* Image preview by press and holding the image
* Async album loading with Kotlin Coroutine
* Limit the maximum number of selectable media
* Customizable grid column count
* All colors customizable by overriding it 

## Installation
*You can have a look at the [sample project.](https://github.com/AnilFurkanOkcun/UWMediaPicker-Android/tree/master/sample)*

**1. Include library**

Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Add the dependency to your app level build.gradle:

```gradle
dependencies {
	...
	implementation 'com.github.AnilFurkanOkcun:UWMediaPicker-Android:1.0.1'
}
```
**2. Add `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` permission to manifest**

 ```xml
 <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/> <!--For compression-->
 ```
**3. Add `UwMediaPickerActivity` into your AndroidManifest.xml**
 ```xml
 <activity
    android:name="com.anilokcun.uwmediapicker.ui.activity.UwMediaPickerActivity"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.AppCompat.NoActionBar" />
 ```
 
## Usage
*Make sure you have `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE`(For compression) permission before use.*

**Initializing UW Media Picker**
```kotlin
 UwMediaPicker
	.with(this)						// Activity or Fragment
 	.setRequestCode(REQUEST_CODE)				// Give request code, default is 0
    	.setGalleryMode(UwMediaPicker.GalleryMode.ImageGallery) // GalleryMode: ImageGallery or VideoGallery, default is ImageGallery
 	.setGridColumnCount(4)                                  // Grid column count, default is 3
    	.setMaxSelectableMediaCount(10)                         // Maximum selectable media count, default is null which means infinite
    	.setLightStatusBar(true)                                // Is llight status bar enable, default is true
	.enableImageCompression(true)				// Is image compression enable, default is false
	.setCompressionMaxWidth(1280F)				// Compressed image's max width px, default is 1280
	.setCompressionMaxHeight(720F)				// Compressed image's max height px, default is 720
	.setCompressFormat(Bitmap.CompressFormat.JPEG)		// Compressed image's format, default is JPEG
	.setCompressionQuality(85)				// Image compression quality, default is 85
	.setCompressedFileDestinationPath(destinationPath)	// Compressed image file's destination path, default is Pictures Dir
 	.open()
```

**Getting results**
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	super.onActivityResult(requestCode, resultCode, data)
	if (data != null && resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
		selectedMediaPaths = data.getStringArrayExtra(UwMediaPicker.UwMediaPickerResultKey)
	}
}
```

## UI Customization

* You can override these colors in your `colors.xml` for ui customization.

```xml
<color name="colorUwMediaPickerStatusBar">#F6F6F6</color>
<color name="colorUwMediaPickerProgressBar">#CDCDCD</color>
<color name="colorUwMediaPickerPageBackground">#FFFFFF</color>
<color name="colorUwMediaPickerProgressDialogBg">#F9F9F9</color>
<color name="colorUwMediaPickerProgressDialogStroke">#D9D9D9</color>

<!-- Toolbar Colors -->
<color name="colorUwMediaPickerToolbarBg">#FFFFFF</color>
<color name="colorUwMediaPickerBackButton">#272727</color>
<color name="colorUwMediaPickerToolbarTitle">#404040</color>
<color name="colorUwMediaPickerToolbarSubtext">#8D8D8D</color>
<color name="colorUwMediaPickerDoneActive">#0192D2</color>
<color name="colorUwMediaPickerDoneInactive">#B3B3B3</color>

<!-- Gallery Item Colors-->
<color name="colorUwMediaPickerBucketBottomStrip">#8C000000</color>
<color name="colorUwMediaPickerImagePlaceHolder">#F6F6F6</color>
<color name="colorUwMediaPickerUnselectedIconBg">#80FFFFFF</color>
<color name="colorUwMediaPickerSelectedIconBg">#0192D2</color>
<color name="colorUwMediaPickerSelectedIcon">#FFF</color>
<color name="colorUwMediaPickerMediaName">#FFFFFF</color>
<color name="colorUwMediaPickerAlbumMediaCount">#D9D9D9</color>
```
* You can override these strings in your `strings.xml` for ui and language changes.

```xml
<!-- Toolbar Texts -->
<string name="uwmediapicker_toolbar_title_image_library">Image Library</string>
<string name="uwmediapicker_toolbar_title_video_library">Video Library</string>
<string name="uwmediapicker_toolbar_text_uw_media_picker_selected_media_count">%d/%d selected</string>
<string name="uwmediapicker_toolbar_done">Done</string>

<!-- Snackbar Messages -->
<string name="uwmediapicker_snackbar_error_gallery_open_failed">Gallery could not be opened.</string>
<string name="uwmediapicker_snackbar_action_retry">Retry</string>

<!-- Toast Messages-->
<string name="uwmediapicker_toast_error_media_bucket_open_failed">Album could not be opened.</string>
<string name="uwmediapicker_toast_error_media_select_failed">Media could not be selected.</string>
<string name="uwmediapicker_toast_error_max_media_selected">You can not select any more media</string>
<string name="uwmediapicker_toast_error_some_media_select_failed">Some media could not be selected</string>

<!-- Time Formats -->
<string name="uwmediapicker_time_format_hour_min_sec">%02d:%02d:%02d</string>
<string name="uwmediapicker_time_format_min_sec">%02d:%02d</string>

<!-- Content Descriptions -->
<string name="uwmediapicker_content_description_back_button">Back button</string>
<string name="uwmediapicker_content_description_album_thumbnail">Album thumbnail</string>
<string name="uwmediapicker_content_description_image_thumbnail">Image thumbnail</string>
<string name="uwmediapicker_content_description_video_thumbnail">Video thumbnail</string>
<string name="uwmediapicker_content_description_selected_icon">Selected icon</string>
```

## Screenshots
![UW Media Picker](https://github.com/AnilFurkanOkcun/UWMediaPicker-Android/blob/master/screenshots.jpg?raw=true)
## License
```
Copyright (c) 2018 Anıl Furkan Ökçün

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
