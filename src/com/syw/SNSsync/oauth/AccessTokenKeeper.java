package com.syw.SNSsync.oauth;

import com.weibo.sdk.android.Oauth2AccessToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
/**
 * 改编于sina的SDK，该类用于保存Oauth2AccessToken到sharepreference，并提供读取功能
 * @author songyouwei
 */
public class AccessTokenKeeper {
	public static final String PREFERENCES_NAME_TOKEN_SINA = "com_syw_SNSsync_sina";
	public static final String PREFERENCES_NAME_TOKEN_TENCENT = "com_syw_SNSsync_tencent";
	public static final String PREFERENCES_NAME_TOKEN_RENREN = "com_syw_SNSsync_renren";
	public static final String PREFERENCES_NAME_TOKEN_QZONE = "com_syw_SNSsync_qzone";
	/**
	 * 保存accesstoken到SharedPreferences
	 * @param context Activity 上下文环境
	 * @param token CommonOAuth2AccessToken
	 * @param preferencesName 
	 */
	public static void keepAccessToken(Context context, CommonOAuth2AccessToken token,String preferencesName) {
		SharedPreferences pref = context.getSharedPreferences(preferencesName, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putString("token", token.getToken());
		editor.putLong("expiresTime", token.getExpiresTime());
		if (preferencesName.equals(PREFERENCES_NAME_TOKEN_TENCENT)) {
			editor.putString("openId", token.getOpenId());
			editor.putString("openKey", token.getOpenKey());
		}
		editor.commit();
	}
	
	/**
	 * 清空sharepreference
	 * @param context
	 * @param preferencesName
	 */
	public static void clear(Context context,String preferencesName){
	    SharedPreferences pref = context.getSharedPreferences(preferencesName, Context.MODE_APPEND);
	    Editor editor = pref.edit();
	    editor.clear();
	    editor.commit();
	}

	/**
	 * 从SharedPreferences读取accessstoken
	 * @param context
	 * @param preferencesName
	 * @return CommonOAuth2AccessToken
	 */
	public static CommonOAuth2AccessToken readAccessToken(Context context,String preferencesName){
		CommonOAuth2AccessToken token = new CommonOAuth2AccessToken();
		SharedPreferences pref = context.getSharedPreferences(preferencesName, Context.MODE_APPEND);
		token.setToken(pref.getString("token", ""));
		token.setExpiresTime(pref.getLong("expiresTime", 0));
		if (preferencesName.equals(PREFERENCES_NAME_TOKEN_TENCENT)) {
			token.setOpenId(pref.getString("openId", ""));
			token.setOpenKey(pref.getString("openKey", ""));
		}
		return token;
	}
	
	/**
	 * 这个方法用来判断相应的SNS是否有存了token信息[第一次使用软件会返回false]
	 */
	public static boolean containsToken(Context context,String preferencesName) {
		SharedPreferences pref = context.getSharedPreferences(preferencesName, Context.MODE_APPEND);
		return pref.contains("token");
	}
}
