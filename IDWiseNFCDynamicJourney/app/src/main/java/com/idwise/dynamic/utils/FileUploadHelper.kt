package com.idwise.dynamic.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class FileUploadHelper {
    private var activity: AppCompatActivity? = null
    private var getImagePickerResult: ActivityResultLauncher<Intent>? = null
    private var imagePickedCallback: ((Bitmap?, Uri?, Boolean) -> Unit?)? = null

    fun init(activity: AppCompatActivity): FileUploadHelper {

        this.activity = activity

        getImagePickerResult = this.activity?.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val imageUri = it.data?.data

                if (imageUri != null) {
                    var bitmap: Bitmap? = null

                    runBlocking {
                        bitmap = async { getBitmapFromUri(imageUri) }.await()
                    }

                    if (bitmap != null) {
                        imagePickedCallback?.invoke(bitmap, imageUri, true)
                    } else {
                        imagePickedCallback?.invoke(null, null, false)
                    }

                } else {
                    imagePickedCallback?.invoke(null, null, false)
                }
            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                imagePickedCallback?.invoke(null, null, false)
            }
        }
        return this
    }

    private fun getBitmapFromUri(imageUri: Uri): Bitmap? {
        try {
            return MediaStore.Images.Media.getBitmap(
                activity!!.contentResolver,
                imageUri
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun pickImage(): FileUploadHelper {
        getImagePickerResult?.launch(pickImageChooserIntent)
        return this
    }

    fun withCallback(imagePickedCallback: (bitmap: Bitmap?, uri: Uri?, isSuccess: Boolean) -> Unit): FileUploadHelper {
        this.imagePickedCallback = imagePickedCallback
        return this
    }

    private val pickImageChooserIntent: Intent
        get() {

            val allIntents: MutableList<Intent> = ArrayList()
            val packageManager = activity!!.packageManager

            // collect all gallery intents
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)

            for (res in listGallery) {
                val intent = Intent(galleryIntent)
                intent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                allIntents.add(intent)
            }

            val mainIntent = allIntents.last()
            allIntents.remove(mainIntent)
            val chooserIntent = Intent.createChooser(mainIntent, "Select source")

            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                allIntents.toTypedArray<Parcelable>()
            )
            return chooserIntent
        }


}