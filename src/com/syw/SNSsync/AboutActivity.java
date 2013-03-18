/**
 * 
 */
package com.syw.SNSsync;

import com.syw.SNSsync.R;
import com.syw.SNSsync.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * @author songyouwei 设置中的"关于"页面
 */
public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	// 返回
	public void back(View v) {
		super.onBackPressed();
	}
}
