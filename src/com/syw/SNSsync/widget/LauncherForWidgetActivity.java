/**
 * 
 */
package com.syw.SNSsync.widget;

import com.syw.SNSsync.LauncherBaseActivity;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.publish.PublishStatusActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author songyouwei
 *
 */
public class LauncherForWidgetActivity extends LauncherBaseActivity {

	private Handler mHandler = new Handler() {
		/* 处理接收到的信息 */
		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent(LauncherForWidgetActivity.this, PublishStatusActivity.class);
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
				// 发送确认跳转的信息
				mHandler.sendEmptyMessage(1);
			} else {
				LauncherForWidgetActivity.this.startActivity(intent);
				LauncherForWidgetActivity.this.finish();
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

		mHandler.sendEmptyMessage(0);
	}
}
