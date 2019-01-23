package appdesign.example.com.assignment

import java.io.File
import java.io.IOException

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.text.TextUtils

object Util {

    /**
     * check directory whether exist, if not then make one;
     * @param path absolute path of directory
     * @return
     */
    fun checkDir(path: String): Boolean {
        var result = true
        val f = File(path)
        if (!f.exists()) {
            result = f.mkdirs()
        } else if (f.isFile) {
            f.delete()
            result = f.mkdirs()
        }
        return result
    }

    fun loadBitmap(path: String): Bitmap? {
        val options = BitmapFactory.Options()
        var b: Bitmap? = null
        try {
            options.inSampleSize = 1
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                return null
            }
            options.inSampleSize = 2
            options.inJustDecodeBounds = false
            options.inDither = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            b = BitmapFactory.decodeFile(path, options)
            if (b == null)
                return null

            val orientation = getOrientation(path)
            if (orientation != 1) {
                val m = Matrix()
                m.postRotate(getRotation(orientation).toFloat())
                b = Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, false)
            }

        } catch (ex: OutOfMemoryError) {
            ex.printStackTrace()
            System.gc()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return b
    }

    /**
     * get rotation degrees
     * @param orientation values in {1, 3, 6, 8}
     * @return values in {0, 90, 180, 270}
     */
    private fun getRotation(orientation: Int): Int {
        when (orientation) {
            1 -> return 0
            8 -> return 270
            3 -> return 180
            6 -> return 90
            else -> return 0
        }
    }

    /**
     * read orientation from Image file
     * @param file
     * @return
     */
    private fun getOrientation(file: String): Int {
        var orientation = 1
        val exif: ExifInterface
        if (!TextUtils.isEmpty(file)) {
            try {
                exif = ExifInterface(file)
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ExceptionInInitializerError) {
                e.printStackTrace()
            }

        }
        return orientation
    }
}
