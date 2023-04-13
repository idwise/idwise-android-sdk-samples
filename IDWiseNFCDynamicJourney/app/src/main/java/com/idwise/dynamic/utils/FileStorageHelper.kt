package com.idwise.dynamic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileStorageHelper {

    private const val IMAGES_DIR = "images"

    fun saveBitmap(context: Context, fileName: String, bitmap: Bitmap) {
        val dir = File(context.filesDir, IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdir()
        }
        try {
            val file = File(dir, "${fileName}.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            Log.v("FileStorageHelper", "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getBitmap(context: Context, fileName: String): Bitmap? {
        val dir = File(context.filesDir, IMAGES_DIR)
        val image = File(dir, "${fileName}.png")
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        return BitmapFactory.decodeFile(image.absolutePath, bmOptions)
    }
}