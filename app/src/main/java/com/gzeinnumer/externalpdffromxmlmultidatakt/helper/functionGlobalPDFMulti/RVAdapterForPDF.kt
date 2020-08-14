package com.gzeinnumer.externalpdffromxmlmultidatakt.helper.functionGlobalPDFMulti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gzeinnumer.externalpdffromxmlmultidatakt.R
import java.util.*

class RVAdapterForPDF : RecyclerView.Adapter<RVAdapterForPDF.MyHolder>() {
    private var list: List<MyModelPDF> = ArrayList()

    fun setList(list: List<MyModelPDF>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_rv, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyHolder,
        position: Int
    ) {
        holder.bindData(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyHolder(itemView: View) : ViewHolder(itemView) {
        var tv: TextView = itemView.findViewById(R.id.tv)
        fun bindData(data: MyModelPDF) {
            tv.text = data.name
        }

    }

    class MyModelPDF(
        val id: Int,
        val name: String,
        val code: String,
        val image: String
    )
}