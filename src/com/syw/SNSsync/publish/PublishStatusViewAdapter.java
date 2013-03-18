package com.syw.SNSsync.publish;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.syw.SNSsync.MainActivity;
import com.syw.SNSsync.R;
import com.syw.SNSsync.entity.PublishStatusEntity;

public class PublishStatusViewAdapter extends BaseAdapter {

	//消息类型
	public static final int MSG_TYPE_SYSTEM = -1;
	public static final int MSG_TYPE_TO = 0;
	public static final int MSG_TYPE_FROM_SINA = 1;
	public static final int MSG_TYPE_FROM_TENCENT = 2;
	public static final int MSG_TYPE_FROM_RENREN = 3;
	public static final int MSG_TYPE_FROM_QZONE = 4;
	//消息类型总数
	public static final int MSG_TYPE_COUNT = 5;
	
	private List<PublishStatusEntity> coll;
	private LayoutInflater mInflater;

	public PublishStatusViewAdapter(Context context, List<PublishStatusEntity> coll) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		PublishStatusEntity entity = coll.get(position);
		return entity.getMsgType();
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return MSG_TYPE_COUNT;
	}

	//返回一个包装好的View，使用了缓存
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PublishStatusEntity entity = coll.get(position);
		int msgType = entity.getMsgType();

		ViewHolder viewHolder = null;
		//如果View无法reuse
		if (convertView == null) {
			if (msgType != MSG_TYPE_TO) {
				//若是来信
				convertView = mInflater.inflate(R.layout.publisher_item_msg_left, null);
			} else {
				//若是去信
				convertView = mInflater.inflate(R.layout.publisher_item_msg_right, null);
			}

			//通过inflate得到的view挑出view中的控件
			viewHolder = new ViewHolder();
			viewHolder.head = (ImageView) convertView.findViewById(R.id.iv_head);
			viewHolder.content = (TextView) convertView.findViewById(R.id.tv_content);
			//为convertView设置tag以复用
			convertView.setTag(viewHolder);
			
		} else {
			//如果view可reuse
			viewHolder = (ViewHolder) convertView.getTag();
		}

		//给viewHolder中的控件赋值
		viewHolder.content.setText(entity.getContent());
		switch (entity.getMsgType()) {
		case MSG_TYPE_SYSTEM:
			viewHolder.head.setImageResource(R.drawable.ic_launcher);
			break;

		case MSG_TYPE_TO:
			viewHolder.head.setImageResource(R.drawable.ic_launcher);
			break;

		case MSG_TYPE_FROM_SINA:
			viewHolder.head.setImageResource(R.drawable.sina);
			break;
			
		case MSG_TYPE_FROM_TENCENT:
			viewHolder.head.setImageResource(R.drawable.tencent);
			break;
			
		case MSG_TYPE_FROM_RENREN:
			viewHolder.head.setImageResource(R.drawable.renren);
			break;
			
		case MSG_TYPE_FROM_QZONE:
			viewHolder.head.setImageResource(R.drawable.qzone);
			break;
		}

		//返回赋值后的view
		return convertView;
	}

	private static class ViewHolder {
		public ImageView head;
		public TextView content;
	}

}
