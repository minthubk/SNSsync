/**
 * 
 */
package com.syw.SNSsync;

import android.widget.ListView;

import com.syw.SNSsync.oauth.CommonOAuth2AccessToken;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;

/**
 * @author songyouwei
 *用来存储程序的运行时数据
 */
public class RunningData {
	
	//标识运行时数据是否已作初始化
	public static boolean hasInited;
	
	//token状态
	public static String sinaTokenState;
	public static String tencentTokenState;
	public static String renrenTokenState;
	public static String qzoneTokenState;
	
	//通用token对象
	public static CommonOAuth2AccessToken sinaAccessToken;
	public static CommonOAuth2AccessToken tencentAccessToken;
	public static CommonOAuth2AccessToken renrenAccessToken;
	public static CommonOAuth2AccessToken qzoneAccessToken;
	
	//接口调用对象
	public static StatusesAPI statusesAPI;//新浪状态API调用对象
	public static CommentsAPI commentsAPI;//新浪评论API调用对象
	public static OAuthV2 oAuthV2;//腾讯API调用对象
	//Renren的API调用对象的生成需要Context，故不作保存
	
	//视图缓存
	public static class cache {
		//主页时间线
		public static TimeLineViewAdapter sinaHomeTimeLineViewAdapter;
		public static TimeLineViewAdapter tencentHomeTimeLineViewAdapter;
		public static TimeLineViewAdapter renrenHomeTimeLineViewAdapter;
		
		//广场
		public static TimeLineViewAdapter sinaSquareTimeLineViewAdapter;
		public static TimeLineViewAdapter tencentSquareTimeLineViewAdapter;
		public static TimeLineViewAdapter renrenSquareTimeLineViewAdapter;
	}
	
	//设置信息
	public static class settings {
		public static int statusCount;
		public static boolean picMode;
	}
	
	//用户nicks
	public static class nicks {
		public static String sinaNick;
		public static String tencentNick;
		public static String renrenNick;
	}
	
	// 用户heads
	public static class heads {
		public static String sinaHead;
		public static String tencentHead;
		public static String renrenHead;
	}
}
