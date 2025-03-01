package cc.winboll.studio.mymessagemanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.SMSActivity;
import cc.winboll.studio.mymessagemanager.beans.PhoneBean;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.mymessagemanager.utils.AddressUtils;
import cc.winboll.studio.mymessagemanager.utils.PhoneUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.shared.log.LogUtils;
import java.util.ArrayList;
import java.util.List;

public class PhoneArrayAdapter extends BaseAdapter {

    public final static String TAG = "PhoneArrayAdapter";

    Context mContext;
    ArrayList<SMSBean> mData;
    List<PhoneBean> mlistContacts;
    PhoneUtil mPhoneUtil;

    public PhoneArrayAdapter(Context context) {
        mContext = context;
		mData = new ArrayList<SMSBean>();
    }

    public void loadData() {
        ArrayList<SMSBean> listTemp = SMSUtil.getAllSMSList(mContext);
        mData.clear();
        mData.addAll(listTemp);
        mPhoneUtil = new PhoneUtil(mContext);
        mlistContacts = mPhoneUtil.getPhoneList();
        LogUtils.i(TAG, "SMS List Reload.");
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int p1) {
        return mData.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_phone, parent, false);

            //分别获取 image view 和 textview 的实例
			viewHolder.tvAddress = convertView.findViewById(R.id.listviewphoneTextView1);
			viewHolder.tvName = convertView.findViewById(R.id.listviewphoneTextView2);
			viewHolder.ll = convertView.findViewById(R.id.listviewphoneLinearLayout1);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

		final String szAddress = ((SMSBean)getItem(position)).getAddress();

		viewHolder.tvAddress.setText(AddressUtils.getFormattedAddress(szAddress));
		viewHolder.tvName.setText(getName(szAddress));

        //Drawable drawableFrame = AppCompatResources.getDrawable(mContext, R.drawable.bg_frame);
        //viewHolder.ll.setBackground(drawableFrame);
        viewHolder.ll.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    //Toast.makeText(mContext, tv.getText(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, SMSActivity.class);
                    intent.putExtra(SMSActivity.EXTRA_PHONE, szAddress);
                    mContext.startActivity(intent);
                }

            });
        return convertView;

    }

    String getName(String szAddress) {
        for (int i = 0; i < mlistContacts.size(); i++) {
            if (mlistContacts.get(i).getTelPhone().equals(szAddress)) {
                return mlistContacts.get(i).getName();
            }
        }
        return mContext.getString(R.string.text_notincontacts);
    }

	class ViewHolder {
        TextView tvAddress;
		TextView tvName;
		LinearLayout ll;
    }

}
