/**
 * 
 */
package com.syw.SNSsync.publish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.renren.api.connect.android.Renren;
import com.syw.SNSsync.Constants;
import com.syw.SNSsync.R;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.entity.PublishStatusEntity;
import com.syw.SNSsync.oauth.renren.RenrenJsonUtil;
import com.syw.SNSsync.oauth.tencent.TencentWeiboJsonUtil;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @author songyouwei
 *这是发同步的状态的Activity，由首页跳转而来
 */
public class PublishStatusActivity extends Activity {
	
	private EditText statusInpuText;
	private ListView msgListView;
	private PublishStatusViewAdapter adapter;
	
	private PublishResultHandler publishResultHandler;
	//handler消息类型
	private static final int SINA_FAILED = 10;
	private static final int SINA_SUCCEED = 11;
	private static final int TENCENT_FAILED = 20;
	private static final int TENCENT_SUCCEED = 21;
	private static final int RENREN_FAILED = 30;
	private static final int RENREN_SUCCEED = 31;
	
	//存放PublishStatusEntity的容器
	private List<PublishStatusEntity> coll = new ArrayList<PublishStatusEntity>();
	
	//dialog
	private ProgressDialog loadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish_status);
		
		statusInpuText = (EditText) findViewById(R.id.et_send_status);
		msgListView = (ListView) findViewById(R.id.listview);
		
		publishResultHandler = new PublishResultHandler();
		
		//预先加入一个有关说明的消息
		coll.add(new PublishStatusEntity("提示：同步消息会发表到所有的绑定帐号上。进入某帐号的主页可发表单独的消息。", PublishStatusViewAdapter.MSG_TYPE_SYSTEM));
		
		adapter = new PublishStatusViewAdapter(this, coll);
		msgListView.setAdapter(adapter);
	}
	
	//以下是预先绑定的方法
	//按下Title左边的返回按钮
	public void back(View v) {
		super.onBackPressed();
	}
	//按下发送按钮
	public void send(View v) {
		final String content = statusInpuText.getText().toString();
		if (content.length() > 0) {
			//添加到list当中
			coll.add(new PublishStatusEntity(content, PublishStatusViewAdapter.MSG_TYPE_TO));
			//清空输入框并定位到最下面
			statusInpuText.setText("");
			msgListView.setSelection(msgListView.getCount() - 1);
			
			loadingDialog = ProgressDialog.show(this, "请稍候", "正在发表同步消息...", true, false);
			//新浪
			new Thread() {
				@Override
				public void run() {
					StatusesAPI statusesAPI = RunningData.statusesAPI;
					if (statusesAPI != null) {
						statusesAPI.update(content, "0", "0", new SinaAPIRequestListener());
					}
				}
			}.start();
			//腾讯
			new Thread() {
				@Override
				public void run() {
					OAuthV2 oAuthV2 = RunningData.oAuthV2;
					if (oAuthV2 != null) {
						try {
							String jsonString = new TAPI(OAuthConstants.OAUTH_VERSION_2_A).add(oAuthV2, "json", content, "127.0.0.1");
							System.out.println(jsonString);
							publishResultHandler.obtainMessage(TENCENT_SUCCEED).sendToTarget();
						} catch (Exception e) {
							e.printStackTrace();
							publishResultHandler.obtainMessage(TENCENT_FAILED).sendToTarget();
						}
					}
				}
			}.start();
			//人人
			new Thread() {
				@Override
				public void run() {
					if (RunningData.renrenTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
						Renren renren = new Renren(Constants.renren.API_KEY, Constants.renren.SECRET_KEY, Constants.renren.APP_ID, PublishStatusActivity.this);
						Bundle parameters = new Bundle();
						parameters.putString("method", "status.set");
						parameters.putString("status", content);
						String jsonString = renren.requestJSON(parameters);
						if (RenrenJsonUtil.publishResult(jsonString) == 1) {
							publishResultHandler.obtainMessage(RENREN_SUCCEED).sendToTarget();
						} else {
							System.out.println("renren:"+jsonString);
							publishResultHandler.obtainMessage(RENREN_FAILED).sendToTarget();
						}
					}
				}
			}.start();
			//提示更新视图
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * @author songyouwei
	 * 新浪API请求监听回调类
	 */
	private class SinaAPIRequestListener implements RequestListener {
		@Override
		public void onComplete(String arg0) {
			publishResultHandler.obtainMessage(SINA_SUCCEED).sendToTarget();
		}
		@Override
		public void onError(WeiboException arg0) {
			System.out.println("sina:WeiboException");
			publishResultHandler.obtainMessage(SINA_FAILED).sendToTarget();
		}
		@Override
		public void onIOException(IOException arg0) {
			System.out.println("sina:IOException");
			publishResultHandler.obtainMessage(SINA_FAILED).sendToTarget();
		}
	}
	
	
	/**
	 * 接收异步传来的发表信息的结果，更新UI
	 */
	private class PublishResultHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String returnString = null;
			String succeedString = "发表成功";
			String failedString = "发表失败！";
			int msgType = 0;
			PublishStatusEntity returnEntity = null;
			
			int result = msg.what;
			switch (result) {
			case SINA_FAILED:
				returnString = failedString;
				msgType = PublishStatusViewAdapter.MSG_TYPE_FROM_SINA;
				break;
			case SINA_SUCCEED:
				returnString = succeedString;
				msgType = PublishStatusViewAdapter.MSG_TYPE_FROM_SINA;
				break;
			case TENCENT_FAILED:
				returnString = failedString;
				msgType = PublishStatusViewAdapter.MSG_TYPE_FROM_TENCENT;
				break;
			case TENCENT_SUCCEED:
				returnString = succeedString;
				msgType = PublishStatusViewAdapter.MSG_TYPE_FROM_TENCENT;
				break;
			case RENREN_FAILED:
				returnString = failedString;
				msgType = PublishStatusViewAdapter.MSG_TYPE_FROM_RENREN;
				break;
			case RENREN_SUCCEED:
				returnString = succeedString;
				msgType = PublishStatusViewAdapter.MSG_TYPE_FROM_RENREN;
				break;
			}
			
			//组装entity
			returnEntity = new PublishStatusEntity(returnString, msgType);
			//返回的数据放入容器并提示更新
			coll.add(returnEntity);
			//定位到底部
			msgListView.setSelection(msgListView.getCount() - 1);
			//提示更新
			adapter.notifyDataSetChanged();
			//关闭dialog
			loadingDialog.dismiss();
		}
	}
}
