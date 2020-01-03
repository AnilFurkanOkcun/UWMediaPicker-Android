package com.anilokcun.uwmediapicker.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.anilokcun.uwmediapicker.model.BaseGalleryMediaModel
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

	private fun getSelectionImageAndVideo() =
		"(${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
			" OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})"

	private fun getSelectionImageByBucketId(bucketId: String) = "${MediaStore.MediaColumns.BUCKET_ID}=$bucketId"
	private fun getSelectionVideoByBucketId(bucketId: String) = "${MediaStore.MediaColumns.BUCKET_ID}=$bucketId"
	private fun getSelectionImageAndVideoByBucketId(bucketId: String) = "${getSelectionImageAndVideo()} AND ${MediaStore.MediaColumns.BUCKET_ID}=$bucketId"

	fun getImageAndVideoBuckets(): ArrayList<GalleryMediaBucketModel> {
		val queryUri = MediaStore.Files.getContentUri("external")
		val cursor = context.contentResolver.query(
			queryUri,
			arrayOf(MediaStore.MediaColumns.BUCKET_ID, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATA),
			getSelectionImageAndVideo(), null, MediaStore.MediaColumns.DATE_ADDED)
		return getBuckets(cursor, queryUri, ::getSelectionImageAndVideoByBucketId)
	}

	/** Get Media Buckets that contains Images with BucketId, BucketName, BucketCoverImagePath, BucketMediaCount */
	fun getImageBuckets(): ArrayList<GalleryMediaBucketModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			arrayOf(MediaStore.MediaColumns.BUCKET_ID, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATA),
			null, null, MediaStore.Images.Media.DATE_ADDED)
		return getBuckets(cursor, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ::getSelectionImageByBucketId)
	}

	/** Get Media Buckets that contains Videos with BucketId, BucketName, BucketCoverImagePath, BucketMediaCount */
	fun getVideoBuckets(): ArrayList<GalleryMediaBucketModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			arrayOf(MediaStore.MediaColumns.BUCKET_ID, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATA),
			null, null, MediaStore.Video.Media.DATE_ADDED)

		return getBuckets(cursor, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ::getSelectionVideoByBucketId)
	}

	private fun getBuckets(cursor: Cursor?, queryUri: Uri, getSelectionByBucketIdFunction: (String) -> String): ArrayList<GalleryMediaBucketModel> {
		val bucketsList = arrayListOf<GalleryMediaBucketModel>()
		val bucketsIdList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID))
				// Skip this bucket if already checked
				if (bucketsIdList.contains(bucketId)) continue
				bucketsIdList.add(bucketId)
				val bucketImagePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
				// Skip this bucket if it's not a File
				if (!File(bucketImagePath).exists()) continue
				val bucketName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME))
				val bucketMediaCount = getMediaCount(queryUri, getSelectionByBucketIdFunction(bucketId))
				bucketsList.add(GalleryMediaBucketModel(bucketId, bucketName, bucketImagePath, bucketMediaCount))
			} while (cursor.moveToPrevious())
		}
		cursor?.close()
		return bucketsList
	}

	/** Get Images in the Bucket that has given id
	 * @param bucketId BucketId
	 * @param selectedMediaPathList, for look to whether media already selected and mark them selected again */
	fun getImagesOfBucket(bucketId: String, selectedMediaPathList: List<String>): ArrayList<GalleryImageModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			arrayOf(MediaStore.MediaColumns.DATA),
			"${MediaStore.MediaColumns.BUCKET_ID}=$bucketId", null, MediaStore.Images.Media.DATE_ADDED)
		val imagesList = arrayListOf<GalleryImageModel>()
		val imagesPathsList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
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
	fun getVideosOfBucket(bucketId: String, selectedMediaPathList: List<String>): ArrayList<GalleryVideoModel> {
		val cursor = context.contentResolver.query(
			MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.VideoColumns.SIZE),
			"${MediaStore.MediaColumns.BUCKET_ID}=$bucketId", null, MediaStore.Video.Media.DATE_ADDED)
		val videosList = arrayListOf<GalleryVideoModel>()
		val videosPathsList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
				// Skip this media if it's already checked or it's not a file
				if (videosPathsList.contains(videoPath)) continue
				if (!File(videoPath).exists()) continue
				val videoDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION))
				val videoSize = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE))
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

	fun getImagesAndVideosOfBucket(bucketId: String, selectedMediaPathList: List<String>): ArrayList<BaseGalleryMediaModel> {
		val queryUri = MediaStore.Files.getContentUri("external")
		val selection = "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
			" OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})" +
			" AND ${MediaStore.MediaColumns.BUCKET_ID}=$bucketId"
		val cursor = context.contentResolver.query(
			queryUri,
			arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.DATA, MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.VideoColumns.SIZE),
			selection, null, MediaStore.Files.FileColumns.DATE_ADDED)
		val imagesAndVideosList = arrayListOf<BaseGalleryMediaModel>()
		val imagesPathsList = HashSet<String>()
		val videosPathsList = HashSet<String>()
		if (cursor?.moveToLast() == true) {
			do {
				if (Thread.interrupted()) {
					return arrayListOf()
				}
				val mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE))
				val videoDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION))
				if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
					val videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
					// Skip this media if it's already checked or it's not a file
					if (videosPathsList.contains(videoPath)) continue
					if (!File(videoPath).exists()) continue

					val videoSize = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE))
					imagesAndVideosList.add(GalleryVideoModel(
						videoPath,
						selectedMediaPathList.contains(videoPath),
						videoDuration,
						videoSize))
					videosPathsList.add(videoPath)
				} else {
					val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
					// Skip this media if it's already checked or it's not a file
					if (imagesPathsList.contains(imagePath)) continue
					if (!File(imagePath).exists()) continue
					imagesAndVideosList.add(GalleryImageModel(
						imagePath,
						selectedMediaPathList.contains(imagePath)))
					imagesPathsList.add(imagePath)
				}
			} while (cursor.moveToPrevious())
		}
		cursor?.close()
		return imagesAndVideosList
	}

	/** Get ImageCount in the Bucket that has given id */
	private fun getMediaCount(queryUri: Uri, selection: String): Int {
		try {
			val cursor = context.contentResolver.query(
				queryUri, null,
				selection, null, MediaStore.MediaColumns.DATE_ADDED)
			if (cursor?.count != null && cursor.count >= 0) {
				return cursor.count
			}
			cursor?.close()
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return 0
	}
}