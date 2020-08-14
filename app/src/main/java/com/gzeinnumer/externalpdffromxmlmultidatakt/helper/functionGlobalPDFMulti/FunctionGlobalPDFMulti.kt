package com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast


object FunctionGlobalPDFMulti {
    private var IS_MANY_PDF_FILE = false
    private const val SECTOR = 100 // Default value for one pdf file.
    private var START = 0
    private var END = SECTOR
    private var NO_OF_PDF_FILE = 1
    private var NO_OF_FILE = 0
    private var LIST_SIZE = 0
    private lateinit var progressDialog: ProgressDialog
    private lateinit var pdfCallBack: PDFCallBack
    private const val TAG = "FunctionGlobalPDFMulti_"

    fun generatePdfReport(
        list: List<RVAdapterForPDF.MyModelPDF>,
        applicationContext: Context,
        activity: Activity
    ) {
        pdfCallBack = activity as PDFCallBack
        LIST_SIZE = list.size
        NO_OF_FILE =
            LIST_SIZE / SECTOR
        if (LIST_SIZE % SECTOR != 0) {
            NO_OF_FILE++
        }
        if (LIST_SIZE > SECTOR) {
            IS_MANY_PDF_FILE = true
        } else {
            END = LIST_SIZE
        }
        createPDFFile(list, applicationContext, activity)
    }

    fun createPDFFile(
        list: List<RVAdapterForPDF.MyModelPDF>,
        context: Context,
        activity: Activity
    ) {
        val pdfDataList: List<RVAdapterForPDF.MyModelPDF> = list.subList(START, END)
        PdfBitmapCache.clearMemory()
        PdfBitmapCache.initBitmapCache(context)
        val pdfCreationUtils = PDFCreationUtils(activity, pdfDataList, LIST_SIZE, NO_OF_PDF_FILE)
        if (NO_OF_PDF_FILE == 1) {
            createProgressBarForPDFCreation(PDFCreationUtils.TOTAL_PROGRESS_BAR, activity)
        }
        pdfCreationUtils.createPDF(object : PDFCreationUtils.PDFCallback{
            override fun onProgress(i: Int) {
                setProgressNumber(i)
            }

            override fun onCreateEveryPdfFile() {
                if (IS_MANY_PDF_FILE) {
                    NO_OF_PDF_FILE++
                    if (NO_OF_FILE == NO_OF_PDF_FILE - 1) {
                        progressDialog.dismiss()
                        createProgressBarForMergePDF(activity)
                        pdfCreationUtils.downloadAndCombinePDFs()
                    } else {
                        START = END
                        if (LIST_SIZE % SECTOR != 0) {
                            if (NO_OF_FILE == NO_OF_PDF_FILE) {
                                END = START - SECTOR + LIST_SIZE % SECTOR
                            }
                        }
                        END += SECTOR
                        createPDFFile(list, context, activity)
                    }
                } else {
                    progressDialog.dismiss()
                    createProgressBarForMergePDF(activity)
                    pdfCreationUtils.downloadAndCombinePDFs()
                }
            }

            override fun onComplete(filePath: String?) {
                progressDialog.dismiss()
                filePath?.let {
                    pdfCallBack.callBackPath(it)
                }
            }

            override fun onError(e: Exception?) {
                e?.let {
                    Toast.makeText(context, "Error  " + it.message, Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun createProgressBarForPDFCreation(totalProgressBar: Int, activity: Activity) {
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("XML layout is in created PDF file. Please wait few moment.\\nTotal PDF page :$totalProgressBar")
        progressDialog.setCancelable(false)
        progressDialog.isIndeterminate = false
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.max = totalProgressBar
        progressDialog.show()
    }

    fun createProgressBarForMergePDF(activity: Activity?) {
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("All pdf file merge into one file. Please wait few moment. After done of mergig pdf file, go to pdf file path.")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun setProgressNumber(progressNumber: Int) {
        progressDialog.progress = progressNumber
    }

    interface PDFCallBack {
        fun callBackPath(path: String)
    }
}
