package com.gzeinnumer.externalpdffromxmlmultidatakt

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti.FunctionGlobalPDFMulti
import com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti.RVAdapterForPDF
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), FunctionGlobalPDFMulti.PDFCallBack {

    private lateinit var adapter: RVAdapterForPDF
    private val list: MutableList<RVAdapterForPDF.MyModelPDF> =  ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = RVAdapterForPDF()
        for (i in 0..29) {
            list.add(RVAdapterForPDF.MyModelPDF(i, "Item $i", "Code", ""))
        }
        adapter.setList(list)

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
        rv.hasFixedSize()

        btn_create_pdf.setOnClickListener {
            FunctionGlobalPDFMulti.generatePdfReport(list, applicationContext, this@MainActivity)
        }
    }

    override fun callBackPath(path: String) {
        btn_pdf_path.text = "PDF path : $path"
        btn_share_pdf.setOnClickListener { sharePdf(path) }
    }

    private fun sharePdf(fileName: String) {
        //kode ini penting ungutuk memaksa agar aplikasi luar bsa mengakses data yang kita butuh kan
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val uris = ArrayList<Uri>()
        val u = Uri.fromFile(File(fileName))
        uris.add(u)
        val sendToIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        sendToIntent.type = "application/pdf"
        sendToIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        sendToIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sendToIntent.putExtra(Intent.EXTRA_SUBJECT, "subject")
        sendToIntent.putExtra(Intent.EXTRA_TEXT, "message")
        sendToIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        try {
            startActivity(Intent.createChooser(sendToIntent, "Send mail..."))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Can't read pdf file", Toast.LENGTH_SHORT).show()
        }
    }
}