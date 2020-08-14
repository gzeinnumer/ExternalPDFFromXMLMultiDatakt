package com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gzeinnumer.externalpdffromxmlmultidatakt.R
import com.gzeinnumer.externalpdffromxmlmultidatakt.helper.FunctionGlobalDir
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class PDFCreationUtils {

    private var deviceHeight = 0
    private var deviceWidth = 0
    private lateinit var bitmapOfView: Bitmap
    private lateinit var document: PdfDocument
    private var pdfModelListSize = 0
    private var SECTOR = 8 // default value
    private var NUMBER_OF_PAGE = 0
    private lateinit var mCurrentPDFModels: List<RVAdapterForPDF.MyModelPDF>
    private lateinit var pdfRootAdapter: RVAdapterForPDF
    private lateinit var mPDFCreationView: View
    private lateinit var mPDFCreationRV: RecyclerView
    private var mCurrentPDFIndex = 0
    private lateinit var activity: Activity
    private lateinit var finalPdfFile: String
    private lateinit var pathForEveryPdfFile: String

    private constructor() {}

    companion object {
        private const val TAG = "PDFCreationUtils_"
        var TOTAL_PROGRESS_BAR = 0
        var filePath: MutableList<String> =
            ArrayList()
        var progressCount = 1
    }

    constructor(
        activity: Activity,
        currentPdfModels: List<RVAdapterForPDF.MyModelPDF>,
        totalPDFModelSize: Int,
        currentPDFIndex: Int
    ) {
        this.activity = activity
        mCurrentPDFModels = currentPdfModels
        mCurrentPDFIndex = currentPDFIndex
        wH
        createForEveryPDFFilePath()
        val sizeInPixel = activity.resources.getDimensionPixelSize(R.dimen.dp_90) + activity.resources
                .getDimensionPixelSize(R.dimen.dp_30)
        mPDFCreationView = LayoutInflater.from(activity).inflate(R.layout.pdf_creation_view, null, false)
        SECTOR = deviceHeight / sizeInPixel
        TOTAL_PROGRESS_BAR = totalPDFModelSize / SECTOR
        mPDFCreationRV = mPDFCreationView.findViewById<View>(R.id.recycler_view_for_pdf) as RecyclerView
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        mPDFCreationRV.layoutManager = mLayoutManager
        pdfRootAdapter = RVAdapterForPDF()
        document = PdfDocument()
        pdfModelListSize = currentPdfModels.size
    }

    private var callback: PDFCallback? = null
    fun createPDF(callback: PDFCallback?) {
        this.callback = callback
        if (pdfModelListSize <= SECTOR) {
            NUMBER_OF_PAGE = 1
            bitmapOfView = AppUtils.findViewBitmap(mCurrentPDFModels, deviceWidth, deviceHeight, pdfRootAdapter, mPDFCreationRV, mPDFCreationView)
            PdfBitmapCache.addBitmapToMemoryCache(NUMBER_OF_PAGE, bitmapOfView)
            createPdf()
        } else {
            NUMBER_OF_PAGE = pdfModelListSize / SECTOR
            if (pdfModelListSize % SECTOR != 0) {
                NUMBER_OF_PAGE++
            }
            val listMap: Map<Int, List<RVAdapterForPDF.MyModelPDF>> = createFinalData()
            for (PAGE_INDEX in 1..NUMBER_OF_PAGE) {
                val list: List<RVAdapterForPDF.MyModelPDF> = listMap[PAGE_INDEX]!!
                bitmapOfView = AppUtils.findViewBitmap(list, deviceWidth, deviceHeight, pdfRootAdapter, mPDFCreationRV, mPDFCreationView)
                PdfBitmapCache.addBitmapToMemoryCache(PAGE_INDEX, bitmapOfView)
            }
            createPdf()
        }
    }

    private fun createFinalData(): Map<Int, List<RVAdapterForPDF.MyModelPDF>> {
        var START = 0
        var END = SECTOR
        val map: MutableMap<Int, List<RVAdapterForPDF.MyModelPDF>> = LinkedHashMap<Int, List<RVAdapterForPDF.MyModelPDF>>()
        var INDEX = 1
        for (i in 0 until NUMBER_OF_PAGE) {
            if (pdfModelListSize % SECTOR != 0) {
                if (i == NUMBER_OF_PAGE - 1) {
                    END = START + pdfModelListSize % SECTOR
                }
            }
            val list: List<RVAdapterForPDF.MyModelPDF> = mCurrentPDFModels.subList(START, END)
            START = END
            END += SECTOR
            map[INDEX] = list
            INDEX++
        }
        return map
    }

    private fun createPdf() {
        Thread(Runnable {
            for (PAGE_INDEX in 1..NUMBER_OF_PAGE) {
                val b = PdfBitmapCache.getBitmapFromMemCache(PAGE_INDEX)
                val pageInfo = PageInfo.Builder(b!!.width, b.height, PAGE_INDEX).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                paint.color = Color.parseColor("#ffffff")
                canvas.drawPaint(paint)
                canvas.drawBitmap(b, 0f, 0f, null)
                document.finishPage(page)
                val filePath = File(pathForEveryPdfFile)
                try {
                    document.writeTo(FileOutputStream(filePath))
                } catch (e: IOException) {
                    if (callback != null) {
                        if (e != null) {
                            callback!!.onError(e)
                        } else {
                            callback!!.onError(Exception("IOException"))
                        }
                    }
                }
                activity.runOnUiThread { callback!!.onProgress(progressCount++) }
                if (PAGE_INDEX == NUMBER_OF_PAGE) {
                    document.close()
                    activity.runOnUiThread { callback!!.onCreateEveryPdfFile() }
                }
            }
        }).start()
    }

    fun downloadAndCombinePDFs() {
        Thread(Runnable {
            if (mCurrentPDFIndex == 1) {
                activity.runOnUiThread {
                    if (callback != null) {
                        callback!!.onComplete(pathForEveryPdfFile)
                    }
                }
            } else {
                try {
                    val ut = PDFMergerUtility()
                    for (s in filePath) {
                        ut.addSource(s)
                    }
                    val fileOutputStream = FileOutputStream(File(createFinalPdfFilePath()))
                    try {
                        ut.destinationStream = fileOutputStream
                        ut.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
                    } finally {
                        fileOutputStream.close()
                    }
                } catch (e: Exception) {

                }
                // delete of other pdf file
                for (s in filePath) {
                    File(s).delete()
                }
                activity.runOnUiThread {
                    if (callback != null) {
                        callback!!.onComplete(finalPdfFile)
                    }
                }
            }
        }).start()
    }

    private fun createForEveryPDFFilePath() {
        pathForEveryPdfFile = AppUtils.createPDFPath()
        filePath.add(pathForEveryPdfFile)
    }

    private fun createFinalPdfFilePath(): String {
        finalPdfFile = AppUtils.createPDFPath()
        return finalPdfFile
    }

    private val wH: Unit
        get() {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            deviceHeight = displayMetrics.heightPixels
            deviceWidth = displayMetrics.widthPixels
            FunctionGlobalDir.myLogD(TAG, "getWH: " + deviceHeight + "_" + deviceWidth)
        }

    interface PDFCallback {
        fun onProgress(progress: Int)
        fun onCreateEveryPdfFile()
        fun onComplete(filePath: String?)
        fun onError(e: Exception?)
    }
}
