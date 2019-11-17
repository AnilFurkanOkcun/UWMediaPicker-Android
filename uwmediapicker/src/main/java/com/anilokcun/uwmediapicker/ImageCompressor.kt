package com.anilokcun.uwmediapicker

import android.graphics.*
import androidx.exifinterface.media.ExifInterface
import com.anilokcun.uwmediapicker.helper.logError
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class ImageCompressor(private val maxWidth: Float, private val maxHeight: Float,
							   private val compressFormat: Bitmap.CompressFormat,
							   private val quality: Int, private val destinationPath: String?) {

	/** Compresses the given image and @return File*/
	@Throws(IOException::class)
	fun compress(imageFile: File): File {
		var compressedFilePath = destinationPath + File.separator + getImageFileName(imageFile.extension)
		var i = 0
		while (File(compressedFilePath).exists()) {
			compressedFilePath = destinationPath + File.separator + getImageFileName(imageFile.extension, i++)
		}
		var fileOutputStream: FileOutputStream? = null
		val destinationParentFile = File(compressedFilePath).parentFile
        if (destinationParentFile?.exists() == false) {
			destinationParentFile.mkdirs()
		}
		try {
			fileOutputStream = FileOutputStream(compressedFilePath)
			decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight)?.compress(compressFormat, quality, fileOutputStream)
		} catch (e: Exception) {
			e.message.logError()
		} finally {
			fileOutputStream?.flush()
			fileOutputStream?.close()
		}

		return File(compressedFilePath)
	}

	/** Decodes Sampled Bitmap from File and @return Bitmap with given maxWidth and maxHeight */
	@Throws(IOException::class)
	fun decodeSampledBitmapFromFile(imageFile: File, maxWidth: Float, maxHeight: Float): Bitmap? {
		var scaledBitmap: Bitmap? = null
		var bitmap: Bitmap?

		val bitmapFactoryOptions = BitmapFactory.Options()

		// Decode with inJustDecodeBounds=true to get dimensions
		bitmapFactoryOptions.inJustDecodeBounds = true
		bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, bitmapFactoryOptions)

		var imgHeight = bitmapFactoryOptions.outHeight
		var imgWidth = bitmapFactoryOptions.outWidth

		val imgAspectRatio = imgWidth.toFloat() / imgHeight.toFloat()
		val maxAspectRatio = maxWidth / maxHeight

		// Calculate width and height according to max size values
		if (imgHeight > maxHeight || imgWidth > maxWidth) {
			when {
				imgAspectRatio < maxAspectRatio -> {
					imgWidth = ((maxHeight / imgHeight) * imgWidth).toInt()
					imgHeight = maxHeight.toInt()

				}
				imgAspectRatio > maxAspectRatio -> {
					imgHeight = ((maxWidth / imgWidth) * imgHeight).toInt()
					imgWidth = maxWidth.toInt()
				}
				else -> {
					imgHeight = maxHeight.toInt()
					imgWidth = maxWidth.toInt()
				}
			}
		}

		bitmapFactoryOptions.inSampleSize = calculateInSampleSize(bitmapFactoryOptions, imgWidth, imgHeight)
		bitmapFactoryOptions.inJustDecodeBounds = false
		bitmapFactoryOptions.inTempStorage = ByteArray(16 * 1024)

		try {
			bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, bitmapFactoryOptions)
		} catch (e: OutOfMemoryError) {
			e.message.logError()
			e.printStackTrace()
		}

		try {
			scaledBitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888)
		} catch (e: OutOfMemoryError) {
			e.message.logError()
			e.printStackTrace()
		}

		val ratioX = imgWidth / bitmapFactoryOptions.outWidth.toFloat()
		val ratioY = imgHeight / bitmapFactoryOptions.outHeight.toFloat()
		val middleX = imgWidth / 2.0f
		val middleY = imgHeight / 2.0f

		val scaleMatrix = Matrix()
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

		val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
		canvas.drawBitmap(bitmap!!, middleX - bitmap.width / 2,
			middleY - bitmap.height / 2, Paint(Paint.FILTER_BITMAP_FLAG))
		bitmap.recycle()

		try {
			val exif = ExifInterface(imageFile.absolutePath)
			val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
			val matrix = Matrix()
			when (orientation) {
				ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
				ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
				ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width,
				scaledBitmap.height, matrix, true)
		} catch (e: IOException) {
			e.message.logError()
			e.printStackTrace()
		}

		return scaledBitmap
	}

	/** Calculates inSampleSize
	 * https://developer.android.com/topic/performance/graphics/load-bitmap*/
	private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
		// Raw height and width of image
		val (height: Int, width: Int) = options.run { outHeight to outWidth }
		var inSampleSize = 1

		if (height > reqHeight || width > reqWidth) {

			val halfHeight: Int = height / 2
			val halfWidth: Int = width / 2

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
				inSampleSize *= 2
			}
		}

		return inSampleSize
	}

	/** Returns image file name as IMG_yyyymmdd_hhmmss_index.extension */
	private fun getImageFileName(fileExtension: String, index: Int = 0): String {
		val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK)

		val calendar = Calendar.getInstance()
		calendar.timeInMillis = System.currentTimeMillis()
		return if (index == 0) {
			"IMG_${dateFormatter.format(calendar.time)}.$fileExtension"
		} else {
			"IMG_${dateFormatter.format(calendar.time)}_$index.$fileExtension"
		}
	}

}