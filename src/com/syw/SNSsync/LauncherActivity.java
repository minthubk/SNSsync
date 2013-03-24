/**
 * 
 */
package com.syw.SNSsync;

import com.syw.SNSsync.R;
import com.syw.SNSsync.R.layout;
import com.syw.SNSsync.oauth.AccessTokenKeeper;
import com.syw.SNSsync.oauth.CommonOAuth2AccessToken;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author songyouwei
 * 这个类用于应用程序的启动，包括显示启动画面，加载一些数据，跳转到MainActivity
 */
public class LauncherActivity extends LauncherBaseActivity {
	private Handler handler = new Handler() {
		/* 处理接收到的信息 */
		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
			if (msg.what == 0) {
				if (RunningData.hasInited == false) {
					//初始化设置信息
					initSettings();
					// 初始化授权状态
					initTokenState();
					//为已授权SNS初始化Token，和接口调用对象
					initToken();
					//修改标识
					RunningData.hasInited = true;
				}
				// 发送确认跳转的信息，可以用延迟的send
				handler.sendEmptyMessageDelayed(1, 1500);
			} else {
				LauncherActivity.this.startActivity(intent);
				LauncherActivity.this.finish();
			}
		}
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置窗口：无标题，全屏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		// 设置内容视图
//		this.setContentView(R.layout.activity_launcher);

		handler.sendEmptyMessage(0);
	}
}
