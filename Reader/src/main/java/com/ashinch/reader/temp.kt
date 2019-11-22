//import android.view.ViewGroup
//import android.widget.TextView
//
//class ContentAdapter(private val mPage: IntArray, private val mContent: String) : PagerAdapter() {
//    internal var mCache: MutableList<*>? = null
//    val count: Int
//        get() = mPage.size
//
//    fun isViewFromObject(view: View, `object`: Any): Boolean {
//        return view === `object`
//    }
//
//    private fun getText(page: Int): String {
//        return if (page == 0) {
//            mContent.substring(0, mPage[0])
//        } else mContent.substring(mPage[page - 1], mPage[page])
//    }
//
//    fun instantiateItem(container: ViewGroup, position: Int): Any {
//        var textView: TextView? = null
//        if (mCache == null) {
//            mCache = LinkedList()
//        }
//        if (mCache!!.size > 0) {
//            textView = mCache!!.removeAt(0) as TextView
//        } else {
//            textView = TextView(container.context)
//        }
//        textView.text = getText(position)
//        container.addView(textView)
//        return textView
//    }
//
//    fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        container.removeView(`object` as View)
//        mCache!!.add(`object`)
//    }
//}