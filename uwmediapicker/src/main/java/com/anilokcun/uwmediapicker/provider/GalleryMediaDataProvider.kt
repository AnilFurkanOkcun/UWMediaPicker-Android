package com.anilokcun.uwmediapicker.provider

import android.content.Context
import android.provider.MediaStore
import com.anilokcun.uwmediapicker.model.GalleryImageModel
import com.anilokcun.uwmediapicker.model.GalleryMediaBucketModel
import com.anilokcun.uwmediapicker.model.GalleryVideoModel
import java.io.File
import java.util.*

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class GalleryMediaDataProvider(private val context: Context) {

	private val projectionImageBucketId by lazy { MediaStore.Images.Media.BUCKET_ID }
	private val projectionImageBucketName by lazy { MediaStore.Images.Media.BUCKET_DISPLAY_NAME }
	private val projectionImagePath by lazy { MediaStore.Images.Media.DATA }
	private val projectionVideoBucketId by lazy { MediaStore.Video.Media.BUCKET_ID }
	private val projectionVideoBucketName by lazy { MediaStore.Video.Media.BUCKET_DISPLAY_NAME }
	private val projectionVideoPath by lazy { MediaStore.Video.Media.DATA }
	private val projectionVideoDuration by lazy { MediaStore.Video.VideoColumns.DURATION }
	private val projectionVideoSize by lazy { MediaStore.Video.VideoColumns.SIZE }

	/** Get Media Buckets that contains Images with BucketId, BucketName, BucketCoverImagePath, BucketMediaCount */
	fun getImageBuckets(): ArrayList<GalleryMediaBucketModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			arrayOf(projectionImageBucketId, projectionImageBucketName, projectionImagePath),
			null, null, MediaStore.Images.Media.DATE_ADDED)
		val bucketsList = arrayListOf<GalleryMediaBucketModel>()
		val bucketsIdList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val bucketId = cursor.getString(cursor.getColumnIndex(projectionImageBucketId))
				// Skip this bucket if already checked
				if (bucketsIdList.contains(bucketId)) continue
				bucketsIdList.add(bucketId)
				val bucketPath = cursor.getString(cursor.getColumnIndex(projectionImagePath))
				// Skip this bucket if it's not a File
				if (!File(bucketPath).exists()) continue
				val bucketName = cursor.getString(cursor.getColumnIndex(projectionImageBucketName))
				val bucketMediaCount = getImageCountByBucket(bucketId)
				bucketsList.add(GalleryMediaBucketModel(bucketId, bucketName, bucketPath, bucketMediaCount))
			} while (cursor.moveToPrevious())
		}
		cursor?.close()
		return bucketsList
	}

	/** Get Media Buckets that contains Videos with BucketId, BucketName, BucketCoverImagePath, BucketMediaCount */
	fun getVideoBuckets(): ArrayList<GalleryMediaBucketModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			arrayOf(projectionVideoBucketId, projectionVideoBucketName, projectionVideoPath),
			null, null, MediaStore.Video.Media.DATE_ADDED)
		val bucketsList = arrayListOf<GalleryMediaBucketModel>()
		val bucketsIdList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val bucketId = cursor.getString(cursor.getColumnIndex(projectionVideoBucketId))
				// Skip this bucket if already checked
				if (bucketsIdList.contains(bucketId)) continue
				bucketsIdList.add(bucketId)
				val bucketThumbnailBitmapPath = cursor.getString(cursor.getColumnIndex(projectionVideoPath))
				// Skip this bucket if it's not a File
				if (!File(bucketThumbnailBitmapPath).exists()) continue
				val bucketName = cursor.getString(cursor.getColumnIndex(projectionVideoBucketName))
				val bucketMediaCount = getVideoCountByBucket(bucketId)
				bucketsList.add(GalleryMediaBucketModel(bucketId, bucketName, bucketThumbnailBitmapPath, bucketMediaCount))
			} while (cursor.moveToPrevious())
		}
		cursor?.close()
		return bucketsList
	}

	/** Get Images in the Bucket that has given id
	 * @param bucketId BucketId
	 * @param selectedMediaPathList, for look to whether media already selected and mark them selected again */
	fun getImages(bucketId: String, selectedMediaPathList: ArrayList<String>): ArrayList<GalleryImageModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			arrayOf(projectionImagePath),
			"$projectionImageBucketId =?", arrayOf(bucketId), MediaStore.Images.Media.DATE_ADDED)
		val imagesList = arrayListOf<GalleryImageModel>()
		val imagesPathsList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val imagePath = cursor.getString(cursor.getColumnIndex(projectionImagePath))
				// Skip this media if it's already checked or it's not a file
				if (imagesPathsList.contains(imagePath)) continue
				if (!File(imagePath).exists()) continue
				imagesList.add(GalleryImageModel(
					imagePath,
					selectedMediaPathList.contains(imagePath)))
				imagesPathsList.add(imagePath)
			} while (cursor.moveToPrevious())
		}
		cursor?.close()
		return imagesList
	}

	/** Get Videos in the Bucket that has given id
	 * @param bucketId BucketId
	 * @param selectedMediaPathList, for look to whether media already selected and mark them selected again */
	fun getVideos(bucketId: String, selectedMediaPathList: ArrayList<String>): ArrayList<GalleryVideoModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			arrayOf(projectionVideoPath, projectionVideoDuration, projectionVideoSize),
			"$projectionVideoBucketId =?", arrayOf(bucketId), MediaStore.Video.Media.DATE_ADDED)
		val videosList = arrayListOf<GalleryVideoModel>()
		val videosPathsList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val videoPath = cursor.getString(cursor.getColumnIndex(projectionVideoPath))
				// Skip this media if it's already checked or it's not a file
				if (videosPathsList.contains(videoPath)) continue
				if (!File(videoPath).exists()) continue
				val videoDuration = cursor.getString(cursor.getColumnIndex(projectionVideoDuration))
				val videoSize = cursor.getString(cursor.getColumnIndex(projectionVideoSize))
				videosList.add(GalleryVideoModel(
					videoPath,
					selectedMediaPathList.contains(videoPath),
					videoDuration,
					videoSize))
				videosPathsList.add(videoPath)
			} while (cursor.moveToPrevious())
		}
		cursor?.close()
		return videosList
	}

	/** Get ImageCount in the Bucket that has given id */
	private fun getImageCountByBucket(bucketId: String): Int {
		try {
			val cursor = context.contentResolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
				"$projectionImageBucketId =?", arrayOf(bucketId), MediaStore.Images.Media.DATE_ADDED)
			if (cursor?.count != null && cursor.count > 0) {
				return cursor.count
			}
			cursor?.close()
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return 0
	}

	/** Get VideoCount in the Bucket that has given id */
	private fun getVideoCountByBucket(bucketId: String): Int {
		try {
			val cursor = context.contentResolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
				"$projectionVideoBucketId =?", arrayOf(bucketId), MediaStore.Video.Media.DATE_ADDED)
			if (cursor?.count != null && cursor.count > 0) {
				return cursor.count
			}
			cursor?.close()
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return 0
	}

}