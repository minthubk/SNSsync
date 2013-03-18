/**
 * 
 */
package com.syw.SNSsync;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.renren.api.connect.android.Renren;
import com.syw.SNSsync.entity.Tweet;
import com.syw.SNSsync.oauth.renren.RenrenJsonUtil;
import com.syw.SNSsync.oauth.sina.SinaWeiboJsonUtil;
import com.syw.SNSsync.oauth.tencent.TencentWeiboJsonUtil;
import com.tencent.weibo.api.StatusesAPI;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author songyouwei 单条Tweet的详情页面
 */
public class SingleTweetActivity extends Activity {

	// 视图组件
	private TextView statusInpuText;
	private ListView msgListView;
	
	//最终放在listView中的集合
	private List<Tweet> tweets = new ArrayList<Tweet>();
	//主Tweet，从上一个Activity传来
	private Tweet tweet;
	//适配器
	private SingleTweetListViewAdapter adapter;
	
	//handler
	private DetailUpdateHandler detailUpdateHandler;
	private NotifyCommentCompleteHandler notifyCommentHandler;
	
	// 用于显示正在加载的弹出式窗口
	private ProgressDialog loadingDialog;
	
	// api相关
	private Renren renren;//人人
	private CommentsAPI commentsAPI = RunningData.commentsAPI;;//新浪
	private static TAPI tAPI = new TAPI(OAuthConstants.OAUTH_VERSION_2_A);//腾讯
	
	private String nick;
	private String head;
	
	//Tweet来源码
	private int fromCode = -1;
	//来源码列表
	public static final int FROM_CODE_SINA = 0;
	public static final int FROM_CODE_TENCENT = 1;
	public static final int FROM_CODE_RENREN = 2;
	
	//新浪commentAPI的请求类型码
	private static final int SINA_API_REQUEST_TYPE_SHOW = 0;
	private static final int SINA_API_REQUEST_TYPE_CREATE = 1;
	
	//返回码
	private static final int SUCCEED_CODE = 1;
	private static final int ERROR_CODE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_tweet);
		// 设置不自动弹出软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//初始化UI组件
		msgListView = (ListView)findViewById(R.id.listview);
		statusInpuText = (EditText) findViewById(R.id.et_send_status);
		
		//初始化人人API对象【新浪和腾讯不需要context，已初始化】
		renren = new Renren(Constants.renren.API_KEY, Constants.renren.SECRET_KEY, Constants.renren.APP_ID, this);
		
		//创建handler
		detailUpdateHandler = new DetailUpdateHandler();
		notifyCommentHandler = new NotifyCommentCompleteHandler();
		
		//从intent中提取tweet和来源码
		Intent intent = getIntent();
		tweet = (Tweet)intent.getSerializableExtra("tweet");
		fromCode = intent.getFlags();
		
		initNicksAndHeads();
		
		
		//拉取评论
		loadComments(tweet);
	}

	/**
	 * 预绑定的方法
	 */
	// 返回
	public void back(View v) {
		super.onBackPressed();
	}
	//回复
	public void send(View v) {
		final String content = statusInpuText.getText().toString();
		if (content.length() == 0) {
			return;
		}
		loadingDialog = ProgressDialog.show(this,"请稍候","正在发送回复...",true,false);
		new Thread() {
			@Override
			public void run() {
				Message message = notifyCommentHandler.obtainMessage();
				//根据来源码调用API
				switch (fromCode) {
				case FROM_CODE_SINA:
					commentsAPI.create(content, Long.parseLong(tweet.getId()), false, new SinaCommentsAPIRequestListener(SINA_API_REQUEST_TYPE_CREATE));
					break;

				case FROM_CODE_TENCENT:
					try {
						String jsonString = tAPI.comment(RunningData.oAuthV2, "json", content, "127.0.0.1", tweet.getId());
						message.what = TencentWeiboJsonUtil.publishResult(jsonString)==1?SUCCEED_CODE:ERROR_CODE;
					} catch (Exception e) {
						message.what = ERROR_CODE;
					} finally {
						message.sendToTarget();
					}
					break;
					
				case FROM_CODE_RENREN:
					Bundle parameters = new Bundle();
					parameters.putString("method", "status.addComment");
					parameters.putString("status_id", tweet.getId());
					parameters.putString("owner_id", tweet.getOwnerId());
					parameters.putString("content", content);
					String jsonString = renren.requestJSON(parameters);
					message.what = RenrenJsonUtil.publishResult(jsonString);
					message.sendToTarget();
					break;
				}
			};
		}.start();
	}
	
	/**
	 * 为nicks和head赋值
	 */
	private void initNicksAndHeads() {
		switch (fromCode) {
		case FROM_CODE_SINA:
			nick = RunningData.nicks.sinaNick;
			head = RunningData.heads.sinaHead;
			break;

		case FROM_CODE_TENCENT:
			nick = RunningData.nicks.tencentNick;
			head = RunningData.heads.tencentHead;
			break;
			
		case FROM_CODE_RENREN:
			nick = RunningData.nicks.renrenNick;
			head = RunningData.heads.renrenHead;
			break;
		}
	}
	
	/**
	 * 【通用】异步拉取评论
	 */
	private void loadComments(final Tweet tweet) {
		loadingDialog = ProgressDialog.show(this, "请稍候", "正在拉取评论...", true,false);
		new Thread() {
			public void run() {
				// 根据来源码拉取评论
				Message message = detailUpdateHandler.obtainMessage();
				switch (fromCode) {
				case FROM_CODE_SINA:
					commentsAPI.show(Long.parseLong(tweet.getId()), 0l, 0l, 10,1, WeiboAPI.AUTHOR_FILTER.ALL,new SinaCommentsAPIRequestListener(SINA_API_REQUEST_TYPE_SHOW));
					break;

				case FROM_CODE_TENCENT:
					try {
						String jsonString = tAPI.reList(RunningData.oAuthV2, "json", "1", tweet.getId(), "0", "0", "10", "0");
						// 将原tweet和评论放入集合
						tweets.add(tweet);
						tweets.addAll(TencentWeiboJsonUtil.getComments(jsonString));
						//赋值返回码
						message.what = SUCCEED_CODE;
					} catch (Exception e1) {
						message.what = ERROR_CODE;
					} finally {
						message.sendToTarget();
					}
					break;

				case FROM_CODE_RENREN:
					try {
						Bundle parameters = new Bundle();
						parameters.putString("method", "status.getComment");
						parameters.putString("status_id", tweet.getId());
						parameters.putString("owner_id", tweet.getOwnerId());
						String jsonString = renren.requestJSON(parameters);
						// 将原tweet和评论放入集合
						tweets.add(tweet);
						tweets.addAll(RenrenJsonUtil.getComments(jsonString));
						//赋值返回码
						message.what = SUCCEED_CODE;
					} catch (Exception e) {
						message.what = ERROR_CODE;
					} finally {
						message.sendToTarget();
					}
					break;
				}
			};
		}.start();
	}
	
	
	// 新浪相关
	/**
	 * 新浪拉取评论的回调监听
	 */
	private class SinaCommentsAPIRequestListener implements RequestListener {
		Message message;
		int requestType;
		public SinaCommentsAPIRequestListener(int requestType) {
			this.requestType = requestType;
			if (requestType == SINA_API_REQUEST_TYPE_SHOW) {
				message = detailUpdateHandler.obtainMessage();
			} else {
				message = notifyCommentHandler.obtainMessage();
			}
		}
		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			message.what = ERROR_CODE;
			message.sendToTarget();
		}
		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			message.what = ERROR_CODE;
			message.sendToTarget();
		}
		@Override
		public void onComplete(final String data) {
			// TODO Auto-generated method stub
			//如果是拉取评论的请求，要先把tweets压入集合再发送消息
			if (requestType == SINA_API_REQUEST_TYPE_SHOW) {
				// 将原tweet和评论放入集合
				tweets.add(tweet);
				tweets.addAll(SinaWeiboJsonUtil.getComments(data));
			}
			// 发送消息
			message.what = SUCCEED_CODE;
			message.sendToTarget();
		}
	}
	
	
	/**
	 * 【通用】拉取评论后更新UI的Handler
	 */
	private class DetailUpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			loadingDialog.dismiss();
			if (msg.what == SUCCEED_CODE) {
				//设置适配器，tweets是全局的
				adapter = new SingleTweetListViewAdapter(SingleTweetActivity.this, tweets);
				msgListView.setAdapter(adapter);
			} else {
				Toast.makeText(SingleTweetActivity.this, "评论拉取出错~~", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 *  【通用】用于接收异步传来的消息，它在主线程中被创建，提示用户回复消息的成功与否
	 */
	private class NotifyCommentCompleteHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			loadingDialog.dismiss();
			if (msg.what == SUCCEED_CODE) {
				Toast.makeText(SingleTweetActivity.this, "回复成功", Toast.LENGTH_SHORT).show();
				//将发送的内容添加到内容容器，并提示更新
				Tweet addedTweet = new Tweet(nick, statusInpuText.getText().toString() , new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(System.currentTimeMillis()));
				addedTweet.setUserHeadImgUrl(head);
				addedTweet.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_COMMENTER);
				tweets.add(addedTweet);
				adapter.notifyDataSetChanged();
				//发送文本框清空
				statusInpuText.setText("");
				//置底部
				msgListView.smoothScrollToPosition(tweets.size()-1);
			} else {
				Toast.makeText(SingleTweetActivity.this, "回复失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
