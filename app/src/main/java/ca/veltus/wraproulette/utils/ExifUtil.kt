package ca.veltus.wraproulette.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.IOException
import java.lang.reflect.InvocationTargetException


object ExifUtil {
    /**
     * @see http://sylvana.net/jpegcrop/exif_orientation.html
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
                bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    @Throws(IOException::class)
    private fun getExifOrientation(src: String): Int {
        var orientation = 1
        try {
            val exifClass = Class.forName("android.media.ExifInterface")
            val exifConstructor = exifClass.getConstructor(
                *arrayOf<Class<*>>(
                    String::class.java
                )
            )
            val exifInstance = exifConstructor.newInstance(*arrayOf<Any>(src))
            val getAttributeInt = exifClass.getMethod(
                "getAttributeInt", *arrayOf<Class<*>?>(
                    String::class.java, Int::class.javaPrimitiveType
                )
            )
            val tagOrientationField = exifClass.getField("TAG_ORIENTATION")
            val tagOrientation = tagOrientationField[null] as String
            orientation =
                getAttributeInt.invoke(exifInstance, *arrayOf<Any>(tagOrientation, 1)) as Int
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        return orientation
    }
}