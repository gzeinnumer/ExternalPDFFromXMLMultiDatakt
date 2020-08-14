@file:Suppress("NAME_SHADOWING")

package com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gzeinnumer.externalpdffromxmlmultidatakt.helper.FunctionGlobalDir
import java.io.File


object AppUtils {
    fun findViewBitmap(
        currentPDFModels: List<RVAdapterForPDF.MyModelPDF>,
        deviceWidth: Int,
        deviceHeight: Int,
        pdfRootAdapter: RVAdapterForPDF,
        mPDFCreationRV: RecyclerView,
        mPDFCreationView: View
    ): Bitmap {
        pdfRootAdapter.setList(currentPDFModels)
        mPDFCreationRV.adapter = pdfRootAdapter
        return getViewBitmap(mPDFCreationView, deviceWidth, deviceHeight)
    }

    private fun getViewBitmap(
        view: View,
        deviceWidth: Int,
        deviceHeight: Int
    ): Bitmap {
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(deviceHeight, View.MeasureSpec.EXACTLY)
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val b = Bitmap.createBitmap(deviceWidth, deviceHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        view.draw(c)
        return getResizedBitmap(b, measuredWidth * 80 / 100, measuredHeight * 80 / 100)
    }

    private fun getResizedBitmap(image: Bitmap, width: Int, height: Int): Bitmap {
        var cwidth = width
        var cheight = height
        val bitmapRatio = cwidth.toFloat() / cheight.toFloat()
        if (bitmapRatio > 1) {
            cheight = (cwidth / bitmapRatio).toInt()
        } else {
            cwidth = (cheight * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, cwidth, cheight, true)
    }

    fun createPDFPath(): String {
        val folder = File(FunctionGlobalDir.getStorageCard + FunctionGlobalDir.appFolder)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder.toString() + File.separator + "pdf_.pdf"
    }
}
