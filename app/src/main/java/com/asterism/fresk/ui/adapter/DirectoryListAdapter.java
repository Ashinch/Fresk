package com.asterism.fresk.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.asterism.fresk.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件目录ListView适配器类
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-08-04 16:48
 */
public class DirectoryListAdapter extends BaseAdapter {

    public HashMap<String, Integer> book;       // 书籍信息集合
    private Context context;                    // 上下文对象
    private List<Map<String, Object>> list;     // 文件信息集合

    //MyAdapter需要一个Context，通过Context获得Layout.inflater，然后通过inflater加载item的布局
    public DirectoryListAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        for (int i = 0; i < data.size(); i++) {
            data.get(i).put("check", false);
        }
        this.list = data;
        this.book = new HashMap<>();
    }

    /**
     * 为文件信息集合设置数据
     *
     * @param data 欲设置的List<Map<String, Object>>对象
     */
    public void setData(List<Map<String, Object>> data) {
        this.list = data;
    }

    /**
     * 设置书籍
     *
     * @param position position
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setBook(int position) {

        String path = list.get(position).get("path").toString();
        if (book.getOrDefault(path, (-1)) == position) {
            book.remove(path);
        } else {
            book.put(path, position);
        }
    }

    /**
     * 获取item总数
     *
     * @return 返回集合长度
     */
    @Override
    public int getCount() {
        //  返回 ListView Item 条目的总数
        return list.size();
    }

    /**
     * 获取item对应的对象
     *
     * @param position 指定item位置
     *
     * @return 返回文件信息集合中指定位置的对象
     */
    @Override
    public Map<String, Object> getItem(int position) {
        //  返回 ListView Item 条目代表的对象
        return list.get(position);
    }

    /**
     * 获取item对应的id
     *
     * @param position 指定item位置
     *
     * @return 返回item位置
     */
    @Override
    public long getItemId(int position) {
        //  返回 ListView Item 的 id
        return position;
    }

    /**
     * 获取item对应的View对象
     *
     * @param position    指定item位置
     * @param convertView item布局文件转换的View对象
     * @param parent      parent
     *
     * @return 返回当前item对应的View对象
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {

            // 将list_item.xml文件找出来并转换成View对象
            view = View.inflate(context, R.layout.item_list_file, null);
            holder = new ViewHolder();
            holder.ivIcon = view.findViewById(R.id.img_item_list_icon);
            holder.tvName = view.findViewById(R.id.tv_item_list_text);
            holder.ivOption = view.findViewById(R.id.img_item_list_folder_forward);
            holder.cbOption = view.findViewById(R.id.cb_item_list_book);
            holder.tvAlready = view.findViewById(R.id.tv_item_list_already_text);
            holder.tvSize = view.findViewById(R.id.tv_item_list_file_size);
            holder.tvTime = view.findViewById(R.id.tv_item_list_file_time);
            // 把ViewHolder对象封装到View对象中
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.cbOption.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String path = list.get(position).get("path").toString();

                if (isChecked) {
                    if (book.getOrDefault(path, (-1)) != position) {
                        book.put(path, position);
                    }
                } else {
                    if (book.getOrDefault(path, (-1)) == position) {
                        book.remove(path);
                    }
                }
            }
        });

        //当为文件夹时
        if ("dir".equals(list.get(position).get("type"))) {
            holder.ivIcon.setImageResource(R.drawable.icon_folder);
            holder.ivOption.setVisibility(View.VISIBLE);
            holder.cbOption.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
            holder.tvSize.setVisibility(View.GONE);

            //当为以添加书籍时
        } else if ("already_file".equals(list.get(position).get("type"))) {
            holder.ivIcon.setImageResource(R.drawable.icon_file);
            holder.ivOption.setVisibility(View.GONE);
            holder.cbOption.setVisibility(View.GONE);
            holder.tvAlready.setVisibility(View.VISIBLE);
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvSize.setVisibility(View.VISIBLE);

            holder.tvSize.setText(list.get(position).get("size").toString());
            holder.tvTime.setText(list.get(position).get("time").toString());

        } else if ("file".equals(list.get(position).get("type"))) {
            holder.ivIcon.setImageResource(R.drawable.icon_file);
            holder.ivOption.setVisibility(View.GONE);
            holder.cbOption.setVisibility(View.VISIBLE);
            holder.tvAlready.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvSize.setVisibility(View.VISIBLE);

            holder.tvSize.setText(list.get(position).get("size").toString());
            holder.tvTime.setText(list.get(position).get("time").toString());

            String path = list.get(position).get("path").toString() + "" + list.get(position).get("name").toString();
            if (book.getOrDefault(path, (-1)) == position) {
                holder.cbOption.setChecked(true);
            } else {
                holder.cbOption.setChecked(false);
            }
        } else {
            Log.w("报错！", "没有找到对应的类型");
        }

        holder.tvName.setText(list.get(position).get("name").toString());
        return view;
    }

    /**
     * ViewHolder
     */
    public static class ViewHolder {
        public ImageView ivIcon;    // item 文件图标 图片框
        public TextView tvName;     // item 文件名称 文本框
        public ImageView ivOption;  // item 目录下级指示图标 图片框
        public CheckBox cbOption;   // item 文件可选图标 复选框
        public TextView tvAlready;  // item 以添加 文本框
        public TextView tvSize;     // item 文件大小 文本框
        public TextView tvTime;     // item 文件时间 文本框
    }
}