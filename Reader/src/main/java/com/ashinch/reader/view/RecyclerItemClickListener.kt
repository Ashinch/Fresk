package com.ashinch.reader.view

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener


class RecyclerItemClickListener(context: Context?, mListener: OnItemClickListener?) : OnItemTouchListener {
    var mGestureDetector: GestureDetector
    private var childView: View? = null
    private var touchView: RecyclerView? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onLongClick(view: View?, posotion: Int)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(e)
        childView = rv.findChildViewUnder(e.x, e.y)
        touchView = rv
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    init {
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(ev: MotionEvent): Boolean {
                if (childView != null && mListener != null) {
                    mListener.onItemClick(childView, touchView!!.getChildPosition(childView!!))
                }
                return true
            }

            override fun onLongPress(ev: MotionEvent) {
                if (childView != null && mListener != null) {
                    mListener.onLongClick(childView, touchView!!.getChildPosition(childView!!))
                }
            }
        })
    }
}