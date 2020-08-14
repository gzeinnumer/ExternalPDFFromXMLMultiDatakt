package com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache

object PdfBitmapCache {
    private var memoryCache: LruCache<Int, Bitmap>? = null

    fun initBitmapCache(context: Context) {
        if (memoryCache == null) {
            val memClass = (context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
            val cacheSize = 1024 * 1024 * memClass
            memoryCache =
                object : LruCache<Int, Bitmap>(cacheSize) {
                    override fun sizeOf(key: Int, bitmap: Bitmap): Int {
                        return bitmap.byteCount / 1024
                    }
                }
        }
    }

    fun clearMemory() {
        if (memoryCache != null) {
            memoryCache!!.evictAll()
        }
    }

    fun addBitmapToMemoryCache(key: Int, bitmap: Bitmap) {
        memoryCache!!.put(key, bitmap)
    }

    fun getBitmapFromMemCache(key: Int): Bitmap? {
        return memoryCache!![key]
    }
}