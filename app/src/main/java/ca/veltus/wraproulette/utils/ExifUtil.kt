package ca.veltus.wraproulette.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.lang.reflect.InvocationTargetException


object ExifUtil {
    /**
     *  See http://sylvana.net/jpegcrop/exif_orientation.html
     */
    fun rotateBitmap(src: String, bitmap: Bitmap): Bitmap {
        try {
            val orientation = getExifOrientation(src)
            if (orientation == 1) {
                return bitmap
            }
            val matrix = Matrix()
            when (orientation) {
                2 -> matrix.setScale(-1f, 1f)
                3 -> matrix.setRotate(180f)
                4 -> {
                    matrix.setRotate(180f)
                    matrix.postScale(-1f, 1f)
                }
                5 -> {
                    matrix.setRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                6 -> matrix.setRotate(90f)
                7 -> {
                    matrix.setRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }
                8 -> matrix.setRotate(-90f)
                else -> return bitmap
            }
            return try {
                val oriented =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                oriented
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                Firebase.crashlytics.recordException(e)
                bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        }
        return bitmap
    }

    @Throws(IOException::class)
    private fun getExifOrientation(src: String): Int {
        var orientation = 1
        try {
            val exifClass = Class.forName("android.media.ExifInterface")
            val exifConstructor = exifClass.getConstructor(
                String::class.java
            )
            val exifInstance = exifConstructor.newInstance(src)
            val getAttributeInt = exifClass.getMethod(
                "getAttributeInt", String::class.java, Int::class.javaPrimitiveType
            )
            val tagOrientationField = exifClass.getField("TAG_ORIENTATION")
            val tagOrientation = tagOrientationField[null] as String
            orientation = getAttributeInt.invoke(exifInstance, tagOrientation, 1) as Int
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: SecurityException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: InstantiationException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
        }
        return orientation
    }
}