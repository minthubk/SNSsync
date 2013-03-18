package com.syw.SNSsync;

import java.io.IOException;
import java.util.List;

import com.syw.SNSsync.entity.Tweet;
import com.syw.SNSsync.oauth.sina.SinaWeiboJsonUtil;
import com.syw.SNSsync.oauth.tencent.TencentWeiboJsonUtil;
import com.tencent.weibo.api.StatusesAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * @author songyouwei 显示消息的通用Activity
 */
public class MessageActivity extends Activity {
	
	private ListView msgListView;
	private TimeLineViewAdapter adapter;
	//当前操作的tweet
	private Tweet tweet;
	
	//来源码
	private int fromCode;
	//请求码
	private int requestCode;
	
	//handler
	private LoadingCompleteHandler loadingCompleteHandler;
	
	//加载提示
	private ProgressDialog loadingDialog;
	
	// 转发Dialog的UI组件
	private EditText forwardContentEditText;
	// 转发窗口
	private AlertDialog forwardDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ated_message);
		
		msgListView = (ListView) findViewById(R.id.listview);
		msgListView.setOnItemClickListener(new ListViewItemOnClickListener());
		
		Intent intent = getIntent();
		fromCode = intent.getIntExtra(Constants.from_code.FROM_CODE, -1);
		requestCode = intent.getIntExtra(Constants.request_code.REQUEST_CODE, -1);
		
		loadingCompleteHandler = new LoadingCompleteHandler();
		
		loadMessages();		
	}

	/**
	 * 加载@消息
	 * */
	private void loadMessages() {
		loadingDialog = ProgressDialog.show(this,"请稍候","正在加载数据...",true,false);
		switch (fromCode) {
		case Constants.from_code.SINA:
			switch (requestCode) {
			case Constants.request_code.ATED_WEIBO:
				RunningData.statusesAPI.mentions(0l, 0l, RunningData.settings.statusCount, 1, WeiboAPI.AUTHOR_FILTER.ALL, WeiboAPI.SRC_FILTER.ALL, WeiboAPI.TYPE_FILTER.ALL, true, new APIRequestListener());
				break;
			case Constants.request_code.ATED_COMMENT:
				RunningData.commentsAPI.mentions(0l, 0l, RunningData.settings.statusCount, 1, WeiboAPI.AUTHOR_FILTER.ALL, WeiboAPI.SRC_FILTER.ALL, new APIRequestListener());
				break;
			case Constants.request_code.RECEIVED_COMMENT:
				RunningData.commentsAPI.toME(0l, 0l, RunningData.settings.statusCount, 1, WeiboAPI.AUTHOR_FILTER.ALL, WeiboAPI.SRC_FILTER.ALL, new APIRequestListener());
				break;
			}
			break;
			
		case Constants.from_code.TENCENT:
			//腾讯只有一种，无请求码
			new Thread() {
				public void run() {
					Message message = loadingCompleteHandler.obtainMessage();
					try {
						String jsonString = new StatusesAPI(OAuthConstants.OAUTH_VERSION_2_A).mentionsTimeline(RunningData.oAuthV2, "json", "0"," 0", RunningData.settings.statusCount+""," 0", "0x1", "0");
						List<Tweet> tweets = TencentWeiboJsonUtil.getOriginalTweets(jsonString);
						message.obj = tweets;
						message.arg1 = Constants.return_code.SUCCEED;
					} catch (Exception e) {
						e.printStackTrace();
						message.arg1 = Constants.return_code.FAILED;
					} finally {
						message.sendToTarget();
					}
				};
			}.start();
		}
	}
	
	/**
	 * 预绑定的方法
	 */
	//按下返回键
	public void back(View v) {
		this.onBackPressed();
	}
	
	
	/**
	 * 监听ListView的item点击事件
	 */
	private class ListViewItemOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,final int arg2,long arg3) {
			//弹出窗口评论
			View forwardDialogView = LayoutInflater.from(MessageActivity.this).inflate(R.layout.dialog_forward, null);
			//Dialog中的UI组件
			forwardContentEditText = (EditText) forwardDialogView.findViewById(R.id.et_forward_content);
			Button forwardButton = (Button) forwardDialogView.findViewById(R.id.bt_forward_commit);
			forwardButton.setText("回复");
			//添加监听
			forwardButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String forwardContent = forwardContentEditText.getText().toString();
					loadingDialog = ProgressDialog.show(MessageActivity.this,"请稍候","正在回复...",true,false);
					new Thread() {
						public void run() {
							//先把要转发的tweet拿到
							tweet = (Tweet)adapter.getItem(arg2);
							switch (requestCode) {
							case Constants.request_code.ATED_WEIBO:
								//设置请求码为评论
								requestCode = Constants.request_code.COMMENT;
								RunningData.commentsAPI.create(forwardContent, Long.parseLong(tweet.getId()), false, new APIRequestListener());
								break;

							case Constants.request_code.ATED_COMMENT:
								//设置请求码为评论
								requestCode = Constants.request_code.COMMENT;
								RunningData.commentsAPI.reply(Long.parseLong(tweet.getId()), Long.parseLong(tweet.getFid()), forwardContent, false, false, new APIRequestListener());
								break;
								
							case Constants.request_code.RECEIVED_COMMENT:
								//设置请求码为评论
								requestCode = Constants.request_code.COMMENT;
								RunningData.commentsAPI.reply(Long.parseLong(tweet.getId()), Long.parseLong(tweet.getFid()), forwardContent, false, false, new APIRequestListener());
								break;
							}
						};
					}.start();
				}
			});
			//组装并显示Dialog
			AlertDialog.Builder forwardDialogBuilder = new AlertDialog.Builder(MessageActivity.this);
			forwardDialogBuilder.setTitle("回复");
			forwardDialogBuilder.setView(forwardDialogView);
			forwardDialog = forwardDialogBuilder.show();
		}
	}
	
	
	/**
	 * @author songyouwei
	 * API请求回调监听
	 */
	private class APIRequestListener implements RequestListener {
		@Override
		public void onComplete(String arg0) {
			Message message = loadingCompleteHandler.obtainMessage();
			List<Tweet> tweets = null;
			switch (requestCode) {
			//若是查看@我的微博
			case Constants.request_code.ATED_WEIBO:
				tweets = SinaWeiboJsonUtil.getOriginalTweets(arg0);
				message.obj = tweets;
				break;

			//若是查看@我的评论
			case Constants.request_code.ATED_COMMENT:
				tweets = SinaWeiboJsonUtil.getComments(arg0);
				message.obj = tweets;
				break;
				
			case Constants.request_code.RECEIVED_COMMENT:
				tweets = SinaWeiboJsonUtil.getComments(arg0);
				message.obj = tweets;
				break;
				
			//若是对消息进行评论
			case Constants.request_code.COMMENT:
				//json数据没什么好解析的，关了窗口就行
				forwardDialog.dismiss();
				break;
			}
			message.arg1 = Constants.return_code.SUCCEED;
			message.sendToTarget();
		}
		@Override
		public void onError(WeiboException arg0) {
			arg0.printStackTrace();
			Message message = loadingCompleteHandler.obtainMessage();
			message.arg1 = Constants.return_code.FAILED;
		}
		@Override
		public void onIOException(IOException arg0) {
			arg0.printStackTrace();
			Message message = loadingCompleteHandler.obtainMessage();
			message.arg1 = Constants.return_code.FAILED;
		}
	}
	
	
	/**
	 * 加载完成后更新UI的Handler
	 */
	private class LoadingCompleteHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			//关闭加载提示
			loadingDialog.dismiss();
			if (msg.arg1 == Constants.return_code.SUCCEED) {
				//如果不是评论请求
				if (requestCode != Constants.request_code.COMMENT) {
					List<Tweet> tweets = (List<Tweet>) msg.obj;
					if (tweets.size() == 0) {
						Toast.makeText(MessageActivity.this, "没有消息~~", Toast.LENGTH_SHORT).show();
						return;
					}
					adapter = new TimeLineViewAdapter(MessageActivity.this, tweets);
					msgListView.setAdapter(adapter);
					msgListView.smoothScrollToPosition(0);
				} else {
					Toast.makeText(MessageActivity.this, "回复成功", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(MessageActivity.this, "出错了~~", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
