package com.ashinch.reader.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Layout.Alignment
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log.i
import android.util.Log.w
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.ashinch.reader.R
import com.ashinch.reader.adapter.ReaderAdapter
import com.ashinch.reader.model.Cell
import com.ashinch.reader.model.Config
import com.ashinch.reader.model.Page
import com.ashinch.reader.utils.DimensionUtil.Companion.dp2px
import com.ashinch.reader.utils.DimensionUtil.Companion.px2dp
import kotlinx.android.synthetic.main.view_main.view.*


class ReaderView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    var mContent: StringBuffer? = null
    private var mConfig = Config(
            textSize = 30,              // 字体大小√
            firstLineIndent = true,     // 首行缩进
            lineSpacing = 1.3f,         // 行间距
            paragraphInterval = 0,      // 段落间隔√
            horizontalMargin = 20,      // 水平间距√
            topMargin = 20,             // 顶部间距√
            bottomMargin = 20           // 底部间距√
    )
    private var pages = mutableListOf(Page(
            mutableListOf(
                    Cell("巧了  我们公司现在就一个人在办公室  就是财务"),
                    Cell("有一天穿个JK来上班  她弯腰在保险柜拿文件的时候  真滴诱惑"),
                    Cell("陈天王  左5.0  右5.0"),
                    Cell("成品出来了发给我拿来看少妇白洁")
            ), "第一章 撒旦法撒旦法", 0, 0, 0)
    )

    private var mMaxRenderHeight: Int? = null
    private var mMaxRenderWidth: Int? = null
    var onClickListener: OnClick? = null

    interface OnClick {
        fun onSortClick()
        fun onLongClick()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_main, this, true)
        // 设置ViewPager滑动效果
        PagerSnapHelper().attachToRecyclerView(scrollView)
        scrollView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        scrollView.setOnClickListener {
            i("click", "scrollview")
        }
        scrollView.addOnItemTouchListener(RecyclerItemClickListener(context, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                onClickListener?.onSortClick()
            }

            override fun onLongClick(view: View?, posotion: Int) {
                onClickListener?.onLongClick()
            }
        }))

        mContent = StringBuffer(resources.getString(R.string.test1))
        afterMeasured {
            mMaxRenderHeight = px2dp(context, height) -
                    mConfig.topMargin -         // 顶部边距
                    mConfig.bottomMargin -      // 底部边距
                    20 * 2 -                    // 章节栏与信息栏高度
                    24 * 2                      // 渲染器上下边距
            mMaxRenderWidth = px2dp(context, width) - mConfig.horizontalMargin * 2

            Thread {
                rendering()
            }.start()
        }
    }

    fun rendering() {
        with(scrollView!!) {
            val pages = paging()
            this.post {
                adapter = ReaderAdapter(pages, mConfig)
                scrollToPosition(0)
//                progressBar!!.visibility = GONE
            }
        }
    }

    private fun paging(): List<Page> {
        var page = Page()
        val pages = mutableListOf<Page>()
        var cells = mutableListOf<Cell>()
//        i("mContent",mContent.toString())
        val lines = mContent!!.split("\n").toMutableList()
        // 还有待处理行内容时
        while (lines.isNotEmpty()) {
            val line = if (mConfig.firstLineIndent) "\u3000\u3000" + lines[0].trim() else lines[0].trim()
            i("lines", lines.size.toString() + " " + line)
            // 当前页面是否有剩余空间
            if (page.height < mMaxRenderHeight!!) {
                // 构造文本油漆桶
                val tp: TextPaint = TextPaint().apply {
                    color = Color.parseColor("#ff333333")
                    style = Paint.Style.FILL
                    textSize = mConfig.textSize.toFloat()
                }
                // 构造静态布局
                var staticLayout: StaticLayout?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StaticLayout.Builder.obtain(line, 0, line.length,
                            tp, dp2px(context, mMaxRenderWidth!!)).let {
                        it.setAlignment(Alignment.ALIGN_NORMAL)
                        it.setLineSpacing(0.0f, 1.0f)
                        it.setIncludePad(false)
                        staticLayout = it.build()
                    }
                } else {
                    staticLayout = StaticLayout(line, tp, dp2px(context, mMaxRenderWidth!!),
                            Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
                }
                val remain = mMaxRenderHeight!! - page.height
                val cellHeight = dp2px(context, staticLayout!!.height)
                // 剩余空间是否足够放下待添加单元
                when {
                    cellHeight == remain -> {
                        cells.add(Cell(line))
                        i("刚好放下", "刚好放下")
                        i("lastPageHeight", page.height.toString())
                        i("nowPageHeight", (page.height + cellHeight).toString())
                        i("mMaxRenderHeight", mMaxRenderHeight.toString())
                        i("cellHeight", cellHeight.toString())
                        w("------------------", "----------------------")
                        page.height = mMaxRenderHeight!!
                        lines.removeAt(0)
                    }
                    cellHeight < remain -> {
                        cells.add(Cell(line))
                        i("空间足够", "足够放下")
                        i("lastPageHeight", page.height.toString())
                        i("nowPageHeight", (page.height + cellHeight).toString())
                        i("mMaxRenderHeight", mMaxRenderHeight.toString())
                        i("cellHeight", cellHeight.toString())
                        w("------------------", "----------------------")
                        page.height += cellHeight + mConfig.paragraphInterval
                        lines.removeAt(0)
                    }
                    else -> {
                        val lineHeight = staticLayout!!.height / staticLayout!!.lineCount
                        // 剩余空间不足以放下一行
                        if (remain < lineHeight) {
                            i(remain.toString(), "不足放下一行 新开页面")
                            // 作一页提交不再处理
                            page.cells = cells
                            pages.add(page)
                            cells = mutableListOf()
                            page = Page()
                        } else {
                            // 能放多少行就截取多少行
//                            val lineCount = remain / lineHeight
//                            i("remain",remain.toString())
//                            i("lineHeight",lineHeight.toString())
//                            i("canLineCount",lineCount.toString())
//                            i("lineCount",staticLayout!!.lineCount.toString())
//                            i("height",dp2px(context,staticLayout!!.height).toString())
//                            i("lientext", staticLayout!!.text.toString())
//                            i("getLineEnd", staticLayout!!.getLineEnd(1).toString())
////                            i(line.length.toString(), (staticLayout!!.getLineEnd(lineCount)).toString())
////                            val tempLine = lines[0].subSequence(0,staticLayout!!.getLineEnd(lineCount-1))
////                            lines[0].drop(staticLayout!!.getLineEnd(lineCount-1))
//                            val tempLine = line.substring(0,staticLayout!!.getLineEnd(lineCount-1))
//                            lines[0].replace(tempLine,"")
//                            cells.add(Cell(tempLine.toString()))
//                            i("截取", tempLine.toString())
//                            i("lastPageHeight", page.height.toString())
//                            i("nowPageHeight", (page.height + cellHeight).toString())
//                            i("mMaxRenderHeight", mMaxRenderHeight.toString())
//                            i("cellHeight", cellHeight.toString())
//                            w("------111--------", "----------111---------")
                            page.cells = cells
                            pages.add(page)
                            cells = mutableListOf()
                            page = Page()
                        }
                    }
                }
            } else {
                // 刚好一页
                page.cells = cells
                pages.add(page)
                cells = mutableListOf()
                page = Page()
            }
        }
        for ((i, p) in pages.withIndex()) {
            i("page", "-----------${i + 1}-----------")
            for (c in p.cells!!) {
                i("cell", c.value.toString())
            }
        }
        return pages
    }

    private fun segment() {

    }

    inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

//    var listener: OnClickListener? = null //监听器类对象
//
//    //监听器类接口
//
//    //监听器类接口
//    interface OnClickListener {
//        fun onClick() //单击事件处理接口
//    }
//
//    override fun setOnClickListener(l: OnClickListener?) {
//        this.listener = l
//    }
}
