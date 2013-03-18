/**
 * 
 */
package com.syw.SNSsync;

import java.util.List;

import com.syw.SNSsync.entity.Tweet;
import com.syw.SNSsync.utils.ImageDownloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author songyouwei Tweet详情list view的适配器
 */
public class SingleTweetListViewAdapter extends BaseAdapter {

	// 消息类型
	public static final int MSG_TYPE_AUTHOR = 0;
	public static final int MSG_TYPE_COMMENTER = 1;
	// 消息类型总数
	public static final int MSG_TYPE_COUNT = 2;

	private List<Tweet> coll;
	private LayoutInflater mInflater;
	
	private TextView nameTextView,timeTextView,contentTextView;
	private ImageView userHeadImageView;
	
	private ImageDownloader imageDownloader;

	public SingleTweetListViewAdapter(Context context, List<Tweet> coll) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		imageDownloader = new ImageDownloader(context);
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
		Tweet entity = coll.get(position);
		return entity.getMsgType();
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return MSG_TYPE_COUNT;
	}

	// 返回一个包装好的View，使用了缓存
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Tweet entity = coll.get(position);
		int msgType = entity.getMsgType();

//		ViewHolder viewHolder = null;
		// 如果View无法reuse
//		if (convertView == null) {
			if (msgType == MSG_TYPE_AUTHOR) {
				// 若是作者
				convertView = mInflater.inflate(R.layout.timeline_item_left, null);
			} else {
				// 若是评论者
				convertView = mInflater.inflate(R.layout.timeline_item_right, null);
			}

			// 通过inflate得到的view挑出view中的控件
//			viewHolder = new ViewHolder();
			timeTextView = (TextView) convertView.findViewById(R.id.tv_sendtime);
			nameTextView = (TextView) convertView.findViewById(R.id.tv_username);
			contentTextView = (TextView) convertView.findViewById(R.id.tv_tweetcontent);
			userHeadImageView = (ImageView) convertView.findViewById(R.id.iv_userhead);
			
			timeTextView.setText(entity.getTime());
			nameTextView.setText(entity.getName());
			contentTextView.setText(entity.getText());
			
			if (RunningData.settings.picMode == true) {
				imageDownloader.download(entity.getUserHeadImgUrl(), userHeadImageView);
			}
			
//			// 为convertView设置tag以复用
//			convertView.setTag(viewHolder);

//		} else {
			// 如果view可reuse
//			viewHolder = (ViewHolder) convertView.getTag();
//		}

		// 给viewHolder中的控件赋值
//		viewHolder.time.setText(entity.getTime());
//		viewHolder.head.setImageResource(R.drawable.ic_launcher);
//		viewHolder.name.setText(entity.getName());
//		viewHolder.content.setText(entity.getText());

		// 返回赋值后的view
		return convertView;
	}

	private static class ViewHolder {
		public ImageView head;
		public TextView name;
		public TextView content;
		public TextView time;
	}

}
