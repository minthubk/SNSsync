/**
 * 
 */
package com.syw.SNSsync;

import java.util.ArrayList;
import java.util.List;

import com.syw.SNSsync.R;
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
 * @author songyouwei
 *时间线的通用视图适配器
 */
public class TimeLineViewAdapter extends BaseAdapter {
	
	private List<Tweet> tweets;
	private LayoutInflater mInflater;
	
	private TextView nameTextView,timeTextView,contentTextView;
	private ImageView userHeadImageView;
	
//	private AsyncBitmapLoader asyncBitmapLoader;
	private ImageDownloader imageDownloader;

	public TimeLineViewAdapter(Context ctx ,List<Tweet> tweets) {
		this.tweets = tweets;
		mInflater = LayoutInflater.from(ctx);
//		asyncBitmapLoader = new AsyncBitmapLoader();
		imageDownloader = new ImageDownloader(ctx);
	}
	
	@Override
	public int getCount() {
		return tweets.size();
	}

	@Override
	public Object getItem(int position) {
		//-1是为了配合可下拉刷新的listView，不然评论/转发会出错
		return tweets.get(position-1);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder viewHolder = null;
		Tweet tweet = tweets.get(position);
//		如果空，即没有tag存在
//		if (convertView == null) {
			//通过反射得到组件对象
			convertView = mInflater.inflate(R.layout.timeline_item_left, null);
			nameTextView = (TextView) convertView.findViewById(R.id.tv_username);
			contentTextView = (TextView) convertView.findViewById(R.id.tv_tweetcontent);
			timeTextView = (TextView) convertView.findViewById(R.id.tv_sendtime);
			userHeadImageView = (ImageView) convertView.findViewById(R.id.iv_userhead);
			//为组件对象赋值
			nameTextView.setText(tweet.getName());
			contentTextView.setText(tweet.getText());
			timeTextView.setText(tweet.getTime());
			userHeadImageView.setImageBitmap(null);
			//将组件对象存到viewHolder中
//			viewHolder = new ViewHolder(nameTextView, contentTextView, timeTextView);
//			convertView.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}
//		asyncBitmapLoader.loadBitmap(userHeadImageView, tweet.getUserHeadImgUrl(), new AsyncBitmapLoader.ImageCallBack() {
//			@Override
//			public void imageLoaded(ImageView imageView, Bitmap bitmap) {
//				imageView.setImageBitmap(bitmap);
//			}
//		});

		//为图赋值
		if (RunningData.settings.picMode == true) {
			imageDownloader.download(tweet.getUserHeadImgUrl(), userHeadImageView);
		}
		
		//为组件赋值
//		viewHolder.nameTextView.setText(tweet.getName());
//		viewHolder.contentTextView.setText(tweet.getText());
//		viewHolder.timeTextView.setText(tweet.getTime());
		
		return convertView;
	}
	
	/**
	 * @author songyouwei
	 * 用于存储视图的缓存，一个ViewHolder就是一条微博的视图
	 */
	private static class ViewHolder {
		public TextView nameTextView,contentTextView,timeTextView;
		public ViewHolder(TextView nameTextView,TextView contentTextView,TextView timeTextView) {
			this.nameTextView = nameTextView;
			this.contentTextView = contentTextView;
			this.timeTextView = timeTextView;
		}
	}

}
