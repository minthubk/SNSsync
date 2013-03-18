/**
 * 
 */
package com.syw.SNSsync;

import android.R.bool;

/**
 * @author songyouwei
 *保存一些程序的常量
 */
public class Constants {
	
	//来源码
	public static final class from_code {
		public static final String FROM_CODE = "from_code";
		public static final int SINA = 0;
		public static final int TENCENT = 1;
		public static final int RENREN = 2;
	}
	
	//请求码
	public static final class request_code {
		public static final String REQUEST_CODE = "request_code";
		
		public static final int RECEIVED_COMMENT = 0;
		public static final int ATED_WEIBO = 1;
		public static final int ATED_COMMENT = 2;
		
		public static final int UPDATE = 3;
		public static final int PUBLISH = 4;
		public static final int FORWARD = 5;
		public static final int COMMENT = 6;
	}
	
	//返回码
	public static final class return_code {
		public static final int SUCCEED = 1;
		public static final int FAILED = -1;
	}
	
	public static final class oauth {
		public static final String NOT_AUTHORIZED = "未授权，点击绑定帐号";
		public static final String HAS_AUTHORIZED = "已授权，点击进入主页";
		public static final String EXPIRED= "授权过期，请重新绑定帐号";
	}
	
	public static final class sina {
		public static final String KEY = "2198267696";
		public static final String SECRET = "4d1e0605e9d5b91d090398b586fd2953";
		public static final String REDRECT_URL = "http://www.diandian.com/songyouwei";
	}
	
	public static final class tencent {
		 public static String redirectUri="http://apk.hiapk.com/html/2013/02/1196370.html";                   
		 public static String clientId = "801308663"; 
		 public static String clientSecret="17737dd2e12fd3a928eab9893a03eb80";
	}
	
	public static final class renren {
		public static final String API_KEY = "16ae4bc81a604d4ca59e6ce76fd5513a";
		public static final String SECRET_KEY = "4759924a3fe14204b3ea8377ed4eba33";
		public static final String APP_ID = "226133";
		public static final String REDIRECT_URL = "http://graph.renren.com/oauth/login_success.html";
	}
	
	public static final class sina_api {
		public static final int RERUEST_TYPE_UPDATE = 0;
		public static final int RERUEST_TYPE_HOME_TIME_LINE = 1;
		public static final int RERUEST_TYPE_FORWARD = 2;
	}
	
	public static final class settings_default {
		public static final int STATUS_COUNT = 5;
		public static final boolean PIC_MODE = true;
	}
}
