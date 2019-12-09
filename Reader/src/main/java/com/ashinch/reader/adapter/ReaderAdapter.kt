package com.ashinch.reader.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashinch.reader.R
import com.ashinch.reader.model.Config
import com.ashinch.reader.model.Page
import com.ashinch.reader.utils.DimensionUtil.Companion.dp2px
import com.ashinch.reader.view.JustifyTextView
import kotlinx.android.synthetic.main.item_page.view.*


class ReaderAdapter(private val pages: List<Page>, private val config: Config) : RecyclerView.Adapter<ReaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_page, parent, false)
        with(config) {
            view.setPadding(dp2px(context, horizontalMargin),
                    dp2px(context, topMargin),
                    dp2px(context, horizontalMargin),
                    dp2px(context, bottomMargin))
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            tvChapter.text = pages[position].chapter
            tvPage.text = "${position + 1}/${pages.size}"
            tvTime.text = "12:09"

            val page = pages[position]
            container.removeAllViews()
            for ((index, cell) in page.cells!!.withIndex()) {
                JustifyTextView(context).let {
                    layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    it.text = cell.value
                    it.textSize = config.textSize.toFloat()
                    it.setTextColor(Color.parseColor("#ff333333"))
//                    it.setLineSpacing(it.lineSpacingExtra, config.lineSpacing)
                    if (index != page.cells!!.size - 1) {
                        it.setPadding(0, 0, 0, dp2px(context, config.paragraphInterval))
                    }
                    container.addView(it)
                }
            }
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvChapter: TextView? = null
        private var container: LinearLayout? = null
        private var tvTime: TextView? = null
        private var tvPage: TextView? = null

        init {
            tvChapter = itemView.findViewById(R.id.tvChapter)
            container = itemView.findViewById(R.id.container)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvPage = itemView.findViewById(R.id.tvPage)
        }
    }
}