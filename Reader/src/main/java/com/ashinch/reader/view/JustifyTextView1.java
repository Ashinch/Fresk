package com.ashinch.reader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by ccheng on 3/18/14.
 */
public class JustifyTextView1 extends AppCompatTextView {
    private int mLineY;
    private int mViewWidth;
    private Rect mBounds = new Rect();
    public int charNum = 0;

    public JustifyTextView1(Context context) {
        super(context);
    }

    public JustifyTextView1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 重写 测量事件
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        resize();
        getTextParams();
//        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec); //设置view的宽高为text的宽高
    }

    /**
     * 重写 布局事件
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resize();
    }

    /**
     * 重写 绘制事件
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());      // 设置颜色
        paint.drawableState = getDrawableState();   // 获取可绘制状态
        mViewWidth = getMeasuredWidth();            // 获取测量宽度
        String text = getText().toString();         // 获取文本
        mLineY = 0;                                 // 每行文本的y轴坐标
        getTextParams();                            // 获取文本宽高
        final int left = mBounds.left;
        final int bottom = mBounds.bottom;
        mBounds.offset(-mBounds.left, -mBounds.top);
        mLineY += mBounds.bottom - bottom;

//        mLineY += getTextSize() * 1.5;
//        if (!calculate) {
//            calculateLines();
//            calculate = true;
//        }

        Layout layout = getLayout();
        // 获取最大显示行数
        int lines = getMeasuredHeight() / getLineHeight();
        Log.i("draw", "lines: " + lines);
        // 循环每行绘制流程
//        int batch = layout.getLineCount() > lines ? lines : layout.getLineCount();
        int batch = layout.getLineCount() > lines ? lines : layout.getLineCount();
        Log.i("draw", "batch: " + batch);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < batch; i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String line = text.substring(lineStart, lineEnd);
            // 获取所需宽度
            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
//            width = getMeasuredWidth();
//            int paddingLeft = getPaddingLeft();
//            int paddingRight = getPaddingRight();
//            width = width - paddingLeft - paddingRight;

            // 是否需要对齐 且 不为最后一行
            if (isNeedScale(line) && i < batch - 1) { //layout.getLineCount()
                drawScaledText(canvas, lineStart, line, width - 12);
            } else {
                canvas.drawText(line, 0, mLineY, paint);
            }

            mLineY += getLineHeight();
            content.append(line);
            Log.i("draw", content.toString());
        }
    }

    /**
     * 绘制缩放文本
     *
     * @param canvas
     * @param lineStart
     * @param line
     * @param lineWidth
     */
    private void drawScaledText(Canvas canvas, int lineStart, String line, float lineWidth) {
        float x = 0;
        if (isFirstLineOfParagraph(lineStart, line)) {
            String blanks = "  ";
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.substring(3);
        }

        float d = (mViewWidth - lineWidth) / line.length() - 1;
        for (int i = 0; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            canvas.drawText(c, x, mLineY, getPaint());
            x += cw + d;
        }
    }

    /**
     * 判断是否为段落第一行
     *
     * @param lineStart
     * @param line
     *
     * @return
     */
    private boolean isFirstLineOfParagraph(int lineStart, String line) {
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    /**
     * 判断是否需要缩放
     *
     * @param line
     *
     * @return
     */
    private boolean isNeedScale(String line) {
        if (line.length() == 0) {
            return false;
        } else {
            return line.charAt(line.length() - 1) != '\n';
        }
    }

    /**
     * 获取文本宽高
     */
    private void getTextParams() {
        final String text = getText().toString();
        final int textLength = text.length();
        getPaint().getTextBounds(text, 0, textLength, mBounds);
        if (textLength == 0) {
            mBounds.right = mBounds.left;
        }
    }

    /**
     * 去除当前页无法显示的字
     *
     * @return 去掉的字数
     */
    public int resize() {
        CharSequence oldContent = getText();
        CharSequence newContent = oldContent.subSequence(0, getCharNum());
        setText(newContent);
        charNum = getCharNum();
        return oldContent.length() - newContent.length();
    }

    /**
     * 获取当前页总字数
     */
    public int getCharNum() {
        return getLayout().getLineEnd(getLineNum());
    }

    /**
     * 获取当前页总行数
     */
    public int getLineNum() {
        Layout layout = getLayout();
        int topOfLastLine = getHeight() - getPaddingTop() - getPaddingBottom() - getLineHeight();
//        setMaxLines(layout.getLineForVertical(topOfLastLine));
        return layout.getLineForVertical(topOfLastLine);
//        return getMeasuredHeight() / getLineHeight() - 1;
    }
}