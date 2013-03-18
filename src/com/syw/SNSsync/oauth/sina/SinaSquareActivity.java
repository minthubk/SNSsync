package com.syw.SNSsync.oauth.sina;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.syw.SNSsync.Constants;
import com.syw.SNSsync.PullRefreshableListView;
import com.syw.SNSsync.R;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.SingleTweetActivity;
import com.syw.SNSsync.TimeLineViewAdapter;
import com.syw.SNSsync.entity.Tweet;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author songyouwei 新浪广场
 */
public class SinaSquareActivity extends Activity {

	// 存储数据的容器和适配器
	private TimeLineViewAdapter adapter;
	private List<Tweet> tweets = new ArrayList<Tweet>();

	// api
	private static StatusesAPI statusesAPI = RunningData.statusesAPI;

	// 视图组件
	private TextView statusInpuText;
	private PullRefreshableListView msgListView;

	// 用于异步处理json数据并更新UI的handler
	private TimeLineRefreshHandler timeLineRefreshHandler;
	private NotifyPublishResultHandler notifyPublishResultHandler;
	private ToastMsgHandler toastMsgHandler;
	
	// 用于显示正在加载的弹出式窗口
	private ProgressDialog loadingDialog;
	
	// 转发Dialog的UI组件
	private EditText forwardContentEditText;
	// 转发窗口
	private AlertDialog forwardDialog;
	// 当前操作的tweet
	private Tweet tweet;

	// 请求码
	private static final int REQ_CODE_FORWARD = 0;// 转发请求
	private static final int REQ_CODE_PUBLISH = 1;// 发送新鲜事请求
	// 长按TweetListItem弹出菜单的内容和index
	private static final String ITEM_LONG_PRESS_CONTENT_FORWARD = "转发";
	private static final String ITEM_LONG_PRESS_CONTENT_COMMENT = "回复";
	private static final int ITEM_LONG_PRESS_INDEX_FORWARD = 0;
	private static final int ITEM_LONG_PRESS_INDEX_COMMENT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sina_weibo);
		//修改标题栏
		TextView titleTextView = (TextView)findViewById(R.id.tv_title);
		titleTextView.setText("新浪最新公共微博");
		// 设置不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		statusInpuText = (EditText) findViewById(R.id.et_send_status);
		msgListView = (PullRefreshableListView) findViewById(R.id.listview);
		
		//绑定监听
		msgListView.setOnItemClickListener(new ListViewItemOnClickListener());
		msgListView.setonRefreshListener(new PullRefreshableListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshTimeLine();
			}
		});

		timeLineRefreshHandler = new TimeLineRefreshHandler();
		notifyPublishResultHandler = new NotifyPublishResultHandler();
		toastMsgHandler = new ToastMsgHandler();

		// 根据是否存在缓存的适配器，显示首页
		if (RunningData.cache.sinaSquareTimeLineViewAdapter != null) {
			setCacheListViewAdapter();
		} else {
			refreshTimeLine();
		}
	}

	/**
	 * 以下是预先绑定的方法
	 */
	// 按下Title左边的返回按钮
	public void back(View v) {
		super.onBackPressed();
	}

	// 点击顶部
	public void goTop(View v) {
		msgListView.smoothScrollToPosition(0);
	}

	// 按下刷新按钮
	public void refresh(View v) {
		refreshTimeLine();
	}

	// 按下发送按钮
	public void send(View v) {
		String content = statusInpuText.getText().toString();
		if (content.length() == 0) {
			return;
		}
		loadingDialog = ProgressDialog
				.show(this, "请稍候", "正在发表...", true, false);
		statusesAPI.update(content, "0", "0", new APIRequestListener(
				Constants.sina_api.RERUEST_TYPE_UPDATE));
	}

	/**
	 * 更新时间线
	 */
	private void refreshTimeLine() {
		// 显示正在加载
		loadingDialog = ProgressDialog.show(this, "请稍候", "正在加载首页数据...", true,false);
		// 请求
		new Thread() {
			public void run() {
				statusesAPI.publicTimeline(RunningData.settings.statusCount, 1, false, new APIRequestListener(Constants.sina_api.RERUEST_TYPE_HOME_TIME_LINE));
			};
		}.start();
	}

	/**
	 * 为listView设置缓存的适配器
	 * */
	private void setCacheListViewAdapter() {
		adapter = RunningData.cache.sinaSquareTimeLineViewAdapter;
		msgListView.setAdapter(adapter);
		msgListView.smoothScrollToPosition(0);
	}
	
	/**
	 * 监听ListView的item点击事件【转发，回复】
	 */
	private class ListViewItemOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,long arg3) {
			AlertDialog.Builder pickDialog = new AlertDialog.Builder(SinaSquareActivity.this);
			pickDialog.setItems(new String[]{ITEM_LONG_PRESS_CONTENT_FORWARD,ITEM_LONG_PRESS_CONTENT_COMMENT}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//根据长按弹出菜单的位置来作相应的操作
					switch (which) {
					case ITEM_LONG_PRESS_INDEX_FORWARD:
						View forwardDialogView = LayoutInflater.from(SinaSquareActivity.this).inflate(R.layout.dialog_forward, null);
						//Dialog中的UI组件
						forwardContentEditText = (EditText) forwardDialogView.findViewById(R.id.et_forward_content);
						Button forwardButton = (Button) forwardDialogView.findViewById(R.id.bt_forward_commit);
						//添加监听
						forwardButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								final String forwardContent = forwardContentEditText.getText().toString();
								loadingDialog = ProgressDialog.show(SinaSquareActivity.this,"请稍候","正在转发...",true,false);
								new Thread() {
									public void run() {
										//先把要转发的tweet拿到
										tweet = (Tweet)adapter.getItem(arg2);
										//转发[异步]
										statusesAPI.repost(Long.parseLong(tweet.getId()), forwardContent, WeiboAPI.COMMENTS_TYPE.NONE, new APIRequestListener(Constants.sina_api.RERUEST_TYPE_FORWARD));
									};
								}.start();
							}
						});
						//组装并显示Dialog
						AlertDialog.Builder forwardDialogBuilder = new AlertDialog.Builder(SinaSquareActivity.this);
						forwardDialogBuilder.setTitle("转发");
						forwardDialogBuilder.setView(forwardDialogView);
						forwardDialog = forwardDialogBuilder.show();
						break;

					case ITEM_LONG_PRESS_INDEX_COMMENT:
						//作和查看详情相同的操作
						Intent intent = new Intent(SinaSquareActivity.this, SingleTweetActivity.class);
						tweet = (Tweet)adapter.getItem(arg2);
						intent.putExtra("tweet", tweet);
						//赋值来源吗
						intent.setFlags(SingleTweetActivity.FROM_CODE_SINA);
						startActivity(intent);
						break;
					}
				}
			});
			pickDialog.setTitle("新浪");
			pickDialog.show();
		}
	}

	/**
	 * API请求监听回调
	 * @author songyouwei
	 */
	private class APIRequestListener implements RequestListener {

		int requestType;

		public APIRequestListener(int requestType) {
			// TODO Auto-generated constructor stub
			this.requestType = requestType;
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			//关闭加载提示
			loadingDialog.dismiss();
			Message message = toastMsgHandler.obtainMessage();
			message.obj = "网络出错了~~";
			message.sendToTarget();
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			//关闭加载提示
			loadingDialog.dismiss();
			Message message = toastMsgHandler.obtainMessage();
			message.obj = "新浪系统出错了~~";
			message.sendToTarget();
		}

		@Override
		public void onComplete(final String data) {
			Message message = null;
			// TODO Auto-generated method stub
			// 关闭加载提示
			loadingDialog.dismiss();
			// 为消息赋值
			switch (requestType) {
			case Constants.sina_api.RERUEST_TYPE_UPDATE:
				message = notifyPublishResultHandler.obtainMessage();
				break;

			case Constants.sina_api.RERUEST_TYPE_HOME_TIME_LINE:
				message = timeLineRefreshHandler.obtainMessage();
				message.obj = data;
				break;
				
			case Constants.sina_api.RERUEST_TYPE_FORWARD:
				//json信息就是个微博信息，没什么好解析的
				message = notifyPublishResultHandler.obtainMessage();
				message.what = Constants.sina_api.RERUEST_TYPE_FORWARD;
				break;
			}
			// 发送消息
			message.sendToTarget();
		}
	}

	/**
	 * @author songyouwei 用于接收异步传来的消息，它在主线程中被创建，更新TimeLine
	 */
	private class TimeLineRefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			String jsonString = msg.obj.toString();
			tweets = SinaWeiboJsonUtil.getOriginalTweets(jsonString);
			adapter = new TimeLineViewAdapter(SinaSquareActivity.this, tweets);
			msgListView.setAdapter(adapter);
			// 置顶部
			msgListView.smoothScrollToPosition(0);
			// 将listView缓存下来
			RunningData.cache.sinaSquareTimeLineViewAdapter = adapter;
			
			//下拉刷新需要的
			msgListView.onRefreshComplete();
		}
	}

	/**
	 * @author songyouwei 用于接收异步传来的消息，它在主线程中被创建，提示用户发布消息的成功与否
	 */
	private class NotifyPublishResultHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			//关闭加载提示
			loadingDialog.dismiss();
			
			String content = null;
			switch (msg.what) {
			//若是发布请求
			case Constants.sina_api.RERUEST_TYPE_UPDATE:
				content = statusInpuText.getText().toString();
				//发送文本框清空
				statusInpuText.setText("");
				break;

			//若是转发请求
			case Constants.sina_api.RERUEST_TYPE_FORWARD:
				//内容为转发的内容和原内容
				content = forwardContentEditText.getText().toString()+" // "+tweet.getText();
				forwardDialog.dismiss();
				break;
			}
			Toast.makeText(SinaSquareActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
			//将发送的内容添加到内容容器，并提示更新
			Tweet newTweet= new Tweet(RunningData.nicks.sinaNick, content , new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(System.currentTimeMillis()));
			newTweet.setUserHeadImgUrl(RunningData.heads.sinaHead);
			tweets.add(0, newTweet);
			adapter.notifyDataSetChanged();
			//置顶部
			msgListView.smoothScrollToPosition(0);
		}
	}
	
	/**
	 * @author songyouwei
	 * 显示消息的Handler，消息为msg.obj.toString()
	 */
	private class ToastMsgHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(SinaSquareActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
		}
	}

}
