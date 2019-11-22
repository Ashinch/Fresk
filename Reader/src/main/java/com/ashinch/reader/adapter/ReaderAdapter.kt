package com.ashinch.reader.adapter

import android.content.Context
import android.util.Log.i
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashinch.reader.R
import com.ashinch.reader.data.Config
import com.ashinch.reader.data.Page
import com.ashinch.reader.view.JustifyTextView
import kotlinx.android.synthetic.main.item_page.view.*


class ReaderAdapter(private val pageList: List<Page>, private val config: Config) : RecyclerView.Adapter<ReaderAdapter.ViewHolder>() {
    private var ctx: Context? = null
    private var preText: String? = null
    private var allText: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        ctx = parent.context
        allText = ctx!!.getString(R.string.test)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        //with(config) { view.setPadding(horizontalMargin, topMargin, horizontalMargin, bottomMargin) }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            tvContent.textSize = config.textSize.toFloat()
            tvChapter.text = pageList[position].chapterName
            tvPage.text = pageList[position].page
            tvContent.text = allText
//            var vto: ViewTreeObserver = tvContent.viewTreeObserver
//            vto.addOnGlobalLayoutListener {
//                if (preText == null) {
//                    tvContent.text = allText
//                    tvContent.resize()
//                    preText = tvContent.text.toString()
//                    i("vto", tvContent.text.toString())
//                } else {
//                    tvContent.text = preText!!.let { allText!!.replace(it, "") }
//                    tvContent.resize()
//                    preText = tvContent.text.toString()
//                }
//            }
            i("pagecontent", tvContent.text.toString())
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvChapter: TextView? = null
        private var tvContent: JustifyTextView? = null
        private var tvTime: TextView? = null
        private var tvPage: TextView? = null

        init {
            tvChapter = itemView.findViewById(R.id.tvChapter)
            tvContent = itemView.findViewById(R.id.tvContent)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvPage = itemView.findViewById(R.id.tvPage)

        }
    }
}