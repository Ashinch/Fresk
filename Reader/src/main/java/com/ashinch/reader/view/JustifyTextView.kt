package com.ashinch.reader.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.Log.i
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.min


/**
 * Created by ccheng on 3/18/14.
 */
class JustifyTextView : AppCompatTextView {
    private var mLineY = 0
    private var mViewWidth = 0
    private var mLastLineShowRect = Rect()
    private var mLastLineActualIndexRect = Rect()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight - calculateExtraSpace())
    }

    
    override fun onDraw(canvas: Canvas) {
        val paint = paint
        paint.color = currentTextColor
        paint.drawableState = drawableState
        // 此处使用 dashengz 提出的 pull-request 增加内填充支持
        // mViewWidth = measuredWidth
        mViewWidth = measuredWidth - (paddingLeft + paddingRight)
        val text = text as String
        // mLineY = 0
        mLineY = paddingTop
        // 去除原来的1.5倍行高
        // mLineY += (textSize * 1.5).toInt()
        mLineY += textSize.toInt()
        val layout = layout
        for (i in 0 until layout.lineCount) {
            val lineStart = layout.getLineStart(i)
            val lineEnd = layout.getLineEnd(i)
            val line = text.substring(lineStart, lineEnd)
            val width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint())
            if (needScale(line) && i < layout.lineCount - 1) {
                drawScaledText(canvas, line, width)
            } else {
                // canvas.drawText(line, 0f, mLineY.toFloat(), paint)
                canvas.drawText(line, paddingLeft.toFloat(), mLineY.toFloat(), paint)
            }
            mLineY += lineHeight
        }
        mLineY += paddingBottom
    }

    private fun calculateExtraSpace(): Int {
        var lastRowSpace = 0
        if (lineCount > 0) {
            // 实际最后一行
            val actualLastRowIndex = lineCount - 1
            // 显示的最后一行
            val lastRowIndex = min(maxLines, lineCount) - 1
            if (lastRowIndex >= 0) {
                val layout = layout
                // 显示的最后一行文字基线坐标
                val baseLine = getLineBounds(lastRowIndex, mLastLineShowRect)
                getLineBounds(actualLastRowIndex, mLastLineActualIndexRect)
                // 测量显示的高度（measureHeight）等于TextView实际高度（layout.height）
                // 或者等于实际高度减去不可见部分的高度（mLastLineActualIndexRect.bottom - mLastLineShowRect.bottom）
                if (measuredHeight == layout.height - (mLastLineActualIndexRect.bottom - mLastLineShowRect.bottom)) {
                    lastRowSpace = mLastLineShowRect.bottom - (baseLine + layout.paint.fontMetricsInt.descent)
                }
            }
        }
        return lastRowSpace
    }

    private fun drawScaledText(canvas: Canvas, mLine: String, lineWidth: Float) {
        var line = mLine
        // var x = 0f
        var x = paddingLeft.toFloat()
        if (isFirstLineOfParagraph(line)) {
            val blanks = "  "
            canvas.drawText(blanks, x, mLineY.toFloat(), paint)
            val bw = StaticLayout.getDesiredWidth(blanks, paint)
            x += bw
            line = line.substring(3)
        }
        // 此处使用 albertmiro 提出的 pull-request 修复右填充的bug
        // val d = (mViewWidth - lineWidth) / line.length - 1
        val d = (mViewWidth - lineWidth) / (line.length - 1)
        for (char in line) {
            val c = char.toString()
            val cw = StaticLayout.getDesiredWidth(c, paint)
            canvas.drawText(c, x, mLineY.toFloat(), paint)
            x += cw + d
        }
    }

    private fun isFirstLineOfParagraph(line: String): Boolean {
        return line.length > 3 && line[0] == ' ' && line[1] == ' '
    }

    private fun needScale(line: String): Boolean {
        return if (line.isEmpty()) {
            false
        } else {
            line[line.length - 1] != '\n'
        }
    }
}