package cc.winboll.studio.libaes;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public abstract class DrawerMenuDataAdapter<T> extends BaseAdapter {

    private ArrayList<T> mData;
    private int mLayoutResource;           //布局id


    public DrawerMenuDataAdapter() {
    }

    public DrawerMenuDataAdapter(ArrayList<T> mData, int mLayoutRes) {
        this.mData = mData;
        this.mLayoutResource = mLayoutRes;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.bind(parent.getContext(), convertView, parent, mLayoutResource
                                                , position);
        bindView(viewHolder, getItem(position));
        return viewHolder.getItemView();
    }

    public abstract void bindView(ViewHolder holder, T obj);

    // 添加数据项
    //
    public void add(T item) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(item);
        notifyDataSetChanged();
    }

    // 添加数据项在指定位置
    //
    public void add(int position, T item) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(position, item);
        notifyDataSetChanged();
    }

    // 删除数据项
    //
    public void remove(T item) {
        if (mData != null) {
            mData.remove(item);
        }
        notifyDataSetChanged();
    }

    // 删除指定位置数据项
    //
    public void remove(int position) {
        if (mData != null) {
            mData.remove(position);
        }
        notifyDataSetChanged();
    }

    // 清理所有数据项
    //
    public void clear() {
        if (mData != null) {
            mData.clear();
        }
        notifyDataSetChanged();
    }


    public static class ViewHolder {

        // 存储在 ListView 的 item 中的 View
        SparseArray<View> mSparseArrayView;
        // 存放convertView
        View mViewItem;
        // 游标
        int mnPosition;
        Context mContext;      

        //构造方法
        //
        private ViewHolder(Context context, ViewGroup parent, int layoutResource) {
            mSparseArrayView = new SparseArray<>();
            this.mContext = context;
            View convertView = LayoutInflater.from(context).inflate(layoutResource, parent, false);
            convertView.setTag(this);
            mViewItem = convertView;
        }

        //绑定 ViewHolder 与数据项
        //
        public static ViewHolder bind(Context context, View convertView, ViewGroup parent,
                                      int layoutResource, int position) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder(context, parent, layoutResource);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.mViewItem = convertView;
            }
            viewHolder.mnPosition = position;
            return viewHolder;
        }

        @SuppressWarnings("unchecked")
        public <T extends View> T getView(int id) {
            T t = (T) mSparseArrayView.get(id);
            if (t == null) {
                t = (T) mViewItem.findViewById(id);
                mSparseArrayView.put(id, t);
            }
            return t;
        }



        // 获取当前条目
        //
        public View getItemView() {
            return mViewItem;
        }


        // 获取条目位置
        //
        public int getItemPosition() {
            return mnPosition;
        }


        // 设置文字
        //
        public ViewHolder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }


        // 设置图片
        //
        public ViewHolder setImageResource(int id, int drawableResource) {
            View view = getView(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableResource);
            } else {
                view.setBackgroundResource(drawableResource);
            }
            return this;
        }

        // 设置点击监听
        //
        public ViewHolder setOnClickListener(int id, View.OnClickListener listener) {
            getView(id).setOnClickListener(listener);
            return this;
        }

        // 设置可见
        //
        public ViewHolder setVisibility(int id, int visible) {
            getView(id).setVisibility(visible);
            return this;
        }

        // 设置标签
        //
        public ViewHolder setTag(int id, Object obj) {
            getView(id).setTag(obj);
            return this;
        }

    }

}
