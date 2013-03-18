package com.syw.SNSsync.oauth.renren;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.renren.api.connect.android.Renren;
import com.syw.SNSsync.Constants;
import com.syw.SNSsync.PullRefreshableListView;
import com.syw.SNSsync.R;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.SingleTweetActivity;
import com.syw.SNSsync.TimeLineViewAdapter;
import com.syw.SNSsync.entity.Tweet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author songyouwei
 * 人人主Activity
 */
public class RenrenActivity extends Activity {

	// 存储数据的容器和适配器
	private TimeLineViewAdapter adapter;
	private List<Tweet> tweets = new ArrayList<Tweet>();
	
	//当前操作的tweet
	private Tweet tweet;

	// api相关
	private Renren renren;
	//返回码
	private int SUCCEED_CODE = 1;
	private int ERROR_CODE = -1;
	
	//请求码
	private static final int REQ_CODE_FORWARD = 0;//转发请求
	private static final int REQ_CODE_PUBLISH = 1;//发送新鲜事请求
	
	//长按TweetListItem弹出菜单的内容和index
	private static final String ITEM_LONG_PRESS_CONTENT_FORWARD = "转发";
	private static final String ITEM_LONG_PRESS_CONTENT_COMMENT = "回复";
	private static final int ITEM_LONG_PRESS_INDEX_FORWARD = 0;
	private static final int ITEM_LONG_PRESS_INDEX_COMMENT = 1;

	// 视图组件
	private TextView statusInpuText;
	private PullRefreshableListView msgListView;
	
	// 加载更多相关
	private TextView loadMoreTextView;
	private ProgressBar loadMoreProgressBar;
	private int currentPage = 1;		
		
	//转发Dialog的UI组件
	private EditText forwardContentEditText;

	// 用于异步处理json数据并更新UI的handler
	private TimeLineRefreshHandler timeLineRefreshHandler;
	private NotifyPublishResultHandler notifyPublishResultHandler;
	// 用于显示正在加载的弹出式窗口
	private ProgressDialog loadingDialog;
	//转发窗口
	private AlertDialog forwardDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_renren);
		// 设置不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		statusInpuText = (EditText) findViewById(R.id.et_send_status);
		msgListView = (PullRefreshableListView) findViewById(R.id.listview);
		
		//为list view设置监听
		msgListView.setOnItemClickListener(new ListViewItemOnClickListener());
		msgListView.setonRefreshListener(new PullRefreshableListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshTimeLine();
			}
        });
		
		//初始化人人授权对象
		renren = new Renren(Constants.renren.API_KEY, Constants.renren.SECRET_KEY, Constants.renren.APP_ID, this);
		
		timeLineRefreshHandler = new TimeLineRefreshHandler();
		notifyPublishResultHandler = new NotifyPublishResultHandler();
		
		//在ListView中添加"加载更多".添加"加载更多"一定要在设置Adapter之前
        addPageMore();

		// 根据是否存在缓存的适配器，显示首页
		if (RunningData.cache.renrenHomeTimeLineViewAdapter != null) {
			setCacheListViewAdapter();
		} else {
			refreshTimeLine();
		}
	}
	
	/**
     * 在ListView中添加"加载更多"
     */
    private void addPageMore(){
        View view=LayoutInflater.from(this).inflate(R.layout.list_page_load, null);
        loadMoreTextView=(TextView)view.findViewById(R.id.tv_load_more);
        loadMoreProgressBar = (ProgressBar)view.findViewById(R.id.pb_loading);
        view.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏"加载更多"
                loadMoreTextView.setVisibility(View.INVISIBLE);
                //显示进度条
                loadMoreProgressBar.setVisibility(View.VISIBLE);
                //加载数据
                new Thread() {
        			@Override
        			public void run() {
        				Message message = timeLineRefreshHandler.obtainMessage();
        				try {
        					Bundle parameters = new Bundle();
        					parameters.putString("method", "feed.get");
        					parameters.putString("type", "10");
        					parameters.putString("page", currentPage+"");
        					parameters.putString("count", RunningData.settings.statusCount+"");
        					String jsonString = renren.requestJSON(parameters);
        					message.obj = jsonString;
        					message.what = SUCCEED_CODE;
        				} catch (Exception e) {
        					message.what = ERROR_CODE;
        				} finally {
        					message.sendToTarget();
        				}
        			};
        		}.start();
            }
        });
        msgListView.addFooterView(view);
    }
	
	
	/**
	 * 以下是预先绑定的方法
	 */
	// 按下Title左边的返回按钮
	public void back(View v) {
		super.onBackPressed();
	}
	//点击顶部
	public void goTop(View v) {
		msgListView.smoothScrollToPosition(0);
	}
	//按下刷新按钮
	public void refresh(View v) {
		refreshTimeLine();
	}
	// 按下发送按钮
	public void send(View v) {
		final String content = statusInpuText.getText().toString();
		if (content.length() == 0) {
			return;
		}
		loadingDialog = ProgressDialog.show(this,"请稍候","正在发表...",true,false);
		new Thread() {
			@Override
			public void run() {
				Bundle parameters = new Bundle();
				parameters.putString("method", "status.set");
				parameters.putString("status", content);
				String jsonString = renren.requestJSON(parameters);
				Message message = notifyPublishResultHandler.obtainMessage();
				message.what = RenrenJsonUtil.publishResult(jsonString);
				message.arg1 = REQ_CODE_PUBLISH;
				message.sendToTarget();
			};
		}.start();
	}
	
	/**
	 * 监听list item的点击事件[进入Tweet详情页]
	 */
	private class ListViewItemOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,long arg3) {
			AlertDialog.Builder pickDialog = new AlertDialog.Builder(RenrenActivity.this);
			pickDialog.setItems(new String[]{ITEM_LONG_PRESS_CONTENT_FORWARD,ITEM_LONG_PRESS_CONTENT_COMMENT}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//根据长按弹出菜单的位置来作相应的操作
					switch (which) {
					case ITEM_LONG_PRESS_INDEX_FORWARD:
						View forwardDialogView = LayoutInflater.from(RenrenActivity.this).inflate(R.layout.dialog_forward, null);
						//Dialog中的UI组件
						forwardContentEditText = (EditText) forwardDialogView.findViewById(R.id.et_forward_content);
						Button forwardButton = (Button) forwardDialogView.findViewById(R.id.bt_forward_commit);
						//添加监听
						forwardButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								final String forwardContent = forwardContentEditText.getText().toString();
								loadingDialog = ProgressDialog.show(RenrenActivity.this,"请稍候","正在转发...",true,false);
								new Thread() {
									@Override
									public void run() {
										//先把要转发的tweet拿到
										tweet = (Tweet)adapter.getItem(arg2);
										//组装参数
										Bundle parameters = new Bundle();
										parameters.putString("method", "status.forward");
										parameters.putString("status", forwardContent);
										parameters.putString("forward_id", tweet.getId());
										parameters.putString("forward_owner", tweet.getOwnerId());
										renren.requestJSON(parameters);
										//解析规则又不相同。。不解了，直接refresh
										Message message = notifyPublishResultHandler.obtainMessage();
										message.arg1 = REQ_CODE_FORWARD;
										message.what = SUCCEED_CODE;
										message.sendToTarget();
									};
								}.start();
							}
						});
						//组装并显示Dialog
						AlertDialog.Builder forwardDialogBuilder = new AlertDialog.Builder(RenrenActivity.this);
						forwardDialogBuilder.setTitle("转发");
						forwardDialogBuilder.setView(forwardDialogView);
						forwardDialog = forwardDialogBuilder.show();
						break;

					case ITEM_LONG_PRESS_INDEX_COMMENT:
						//作和查看详情相同的操作
						Intent intent = new Intent(RenrenActivity.this, SingleTweetActivity.class);
						Tweet tweet = (Tweet)adapter.getItem(arg2);
						intent.putExtra("tweet", tweet);
						//赋值来源吗
						intent.setFlags(SingleTweetActivity.FROM_CODE_RENREN);
						startActivity(intent);
						break;
					}
				}
			});
			pickDialog.setTitle("人人");
			pickDialog.show();
		}
	}

	/**
	 * 更新时间线
	 */
	private void refreshTimeLine() {
		//显示正在加载
		loadingDialog = ProgressDialog.show(this, "请稍候", "正在加载首页数据...", true,false);
		new Thread() {
			@Override
			public void run() {
				Message message = timeLineRefreshHandler.obtainMessage();
				try {
					//修改currentPage
					currentPage = 1;
					Bundle parameters = new Bundle();
					parameters.putString("method", "feed.get");
					parameters.putString("type", "10");
					parameters.putString("count", RunningData.settings.statusCount+"");
					String jsonString = renren.requestJSON(parameters);
					message.obj = jsonString;
					message.what = SUCCEED_CODE;
				} catch (Exception e) {
					message.what = ERROR_CODE;
				} finally {
					message.sendToTarget();
				}
			};
		}.start();
	}
	
	/**
	 * 为listView设置缓存的适配器
	 * */
	private void setCacheListViewAdapter() {
		adapter = RunningData.cache.renrenHomeTimeLineViewAdapter;
		msgListView.setAdapter(adapter);
		msgListView.smoothScrollToPosition(0);
	}
	
	
	/**
	 * @author songyouwei
	 * 用于接收异步传来的消息，它在主线程中被创建，更新TimeLine
	 */
	private class TimeLineRefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			loadingDialog.dismiss();
			if (msg.what == SUCCEED_CODE) {
				String jsonString = msg.obj.toString();
				List<Tweet> newTweets = RenrenJsonUtil.getOriginalTweets(jsonString);
				if (currentPage != 1) {
					//将新tweets压入集合
					tweets.addAll(newTweets);
				} else {
					//代替
					tweets = newTweets;
				}
				adapter = new TimeLineViewAdapter(RenrenActivity.this, tweets);
				msgListView.setAdapter(adapter);
				//锁定位置
				msgListView.setSelection((currentPage-1)*RunningData.settings.statusCount);
	            
	            //修改当前页
	            currentPage++;
	            //再次显示"加载更多"
	            loadMoreTextView.setVisibility(View.VISIBLE);
	            //再次隐藏“进度条”
	            loadMoreProgressBar.setVisibility(View.INVISIBLE);
				//将listView缓存下来
				RunningData.cache.renrenHomeTimeLineViewAdapter = adapter;
			} else {
				Toast.makeText(RenrenActivity.this, "网络出错了~~", Toast.LENGTH_SHORT).show();
			}
			msgListView.onRefreshComplete();
		}
	}
	
	
	/**
	 * @author songyouwei
	 * 用于接收异步传来的消息，它在主线程中被创建，提示用户发布消息的成功与否
	 */
	private class NotifyPublishResultHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			loadingDialog.dismiss();
			if (msg.what == SUCCEED_CODE) {
				Toast.makeText(RenrenActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
				String content = null;
				switch (msg.arg1) {
				//如果是发送新鲜事的
				case REQ_CODE_PUBLISH:
					content = statusInpuText.getText().toString();
					//发送文本框清空
					statusInpuText.setText("");
					break;

				//如果是转发请求
				case REQ_CODE_FORWARD:
					//内容为转发的内容和原内容
					content = forwardContentEditText.getText().toString()+" // "+tweet.getText();
					forwardDialog.dismiss();
					break;
				}
				//将发送的内容添加到内容容器，并提示更新
				Tweet newTweet = new Tweet(RunningData.nicks.renrenNick, content , new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(System.currentTimeMillis()));
				newTweet.setUserHeadImgUrl(RunningData.heads.renrenHead);
				tweets.add(0, newTweet);
				adapter.notifyDataSetChanged();
				//置顶部
				msgListView.smoothScrollToPosition(0);
			} else {
				Toast.makeText(RenrenActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
