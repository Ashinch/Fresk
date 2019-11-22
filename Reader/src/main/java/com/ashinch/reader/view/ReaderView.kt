package com.ashinch.reader.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.ashinch.reader.R
import com.ashinch.reader.adapter.ReaderAdapter
import com.ashinch.reader.data.Config
import com.ashinch.reader.data.Page
import kotlinx.android.synthetic.main.view_main.view.*


class ReaderView// 设置LayoutManager，同时设置水平滑动//        i("jst", justifyTextView.text.toString())
//
//        val layout = justifyTextView.layout
//        var result = ""
//        var line = justifyTextView.lineCount
//        i("pageline",line.toString())
//        for (i in 0 until line) {
//            val start = layout.getLineStart(i)
//            val end = layout.getLineEnd(i)
//            result += justifyTextView.text.substring(start, end) + "\\n"
//        }
//        val start = layout.getLineStart(line - 1)
//        val end = layout.getLineEnd(line - 1)
//        result += justifyTextView.text.substring(start, end)
//        i("pageresult", result)
(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val pageList: List<Page> = listOf(
            Page("第一章", "1/1", "阿斯蒂芬撒旦法撒旦法萨芬"),
            Page("第二章", "1/3", "阿斯蒂芬撒旦法撒旦法萨芬阿斯蒂芬撒旦法撒旦法萨芬阿斯蒂芬撒旦法撒旦法萨芬")
    )

    private var textSize: Int? = null
    private var lineIndent: Boolean? = null
    private var lineSpacing: Int? = null
    private var paragraphInterval: Boolean? = null
    private var horizontalMargin: Int? = null
    private var TopMargin: Int? = null
    private var BottomMargin: Int? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_main, this, true)
        // 设置ViewPager滑动效果
        PagerSnapHelper().attachToRecyclerView(scrollView)
        with(scrollView!!) {
            // 设置LayoutManager，同时设置水平滑动
            layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
            val config = Config(
                    26,
                    true,
                    10,
                    true,
                    20,
                    20,
                    20)
            adapter = ReaderAdapter(pageList, config)
            scrollToPosition(0)
        }
    }
}
