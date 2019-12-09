package com.asterism.fresk.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asterism.fresk.R;
import com.asterism.fresk.dao.bean.TocBean;

import java.io.File;
import java.util.List;

import nl.siegmann.epublib.domain.TOCReference;

/**
 * 书架GridView适配器类
 *
 * @author Lshz
 * @email 528118879@qq.com
 * @date on 2019-08-04 16:37
 */
public class ChapterListAdapter extends BaseAdapter {

    private Context context;                   // 上下文对象
    private List<TocBean> tocList;        // 书籍信息集合


    public ChapterListAdapter(Context context, List<TocBean> tocList) {
        this.context = context;
        this.tocList = tocList;
    }

    /**
     * 获取item总数
     *
     * @return 返回集合长度
     */
    @Override
    public int getCount() {
        return tocList.size();
    }

    /**
     * 获取item对应的对象
     *
     * @param position 指定item位置
     *
     * @return 返回book对象
     */
    @Override
    public Object getItem(int position) {
        return tocList.get(position);
    }

    /**
     * 获取item的id
     *
     * @param position 指定item位置
     *
     * @return 返回id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item对应的View对象
     *
     * @param position    item位置
     * @param convertView item文件转换的View
     * @param parent      parent
     *
     * @return 返回item对应的View对象
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_list_chapter, null);
            holder = new ViewHolder();
            holder.tvChapterName = convertView.findViewById(R.id.tv_chapter_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvChapterName.setText(tocList.get(position).getTitle());
        return convertView;
    }

    static class ViewHolder {
        TextView tvChapterName;  // 章节名 文本框
    }

}
