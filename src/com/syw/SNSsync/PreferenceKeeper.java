/**
 * 
 */
package com.syw.SNSsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author songyouwei
 * 对偏好信息的读取，保存【不包括token信息】
 */
public class PreferenceKeeper {
	//文件名
	public static final String PREFERENCES_FILE_NAME_SETTINGS = "com_syw_SNSsync_settings";
	//属性名
	public static final String SETTINGS_STATUS_COUNT = "status_count";
	public static final String SETTINGS_PIC_MODE = "pic_mode";
	
	//文件名
	public static final String PREFERENCES_FILE_NAME_USER_INFOS = "com_syw_SNSsync_user_infos";
	//属性名
	//昵称
	public static final String USER_NICKS_SINA = "nick_sina";
	public static final String USER_NICKS_TENCENT = "nick_tencent";
	public static final String USER_NICKS_RENREN = "nick_renren";
	//头像url
	public static final String USER_HEAD_IMG_URL_SINA = "head_sina";
	public static final String USER_HEAD_IMG_URL_TENCENT = "head_tencent";
	public static final String USER_HEAD_IMG_URL_RENREN = "head_tencent";
	
	public static void saveSetting(Context context,String preferenceName,String preferenceValue) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME_SETTINGS, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putString(preferenceName, preferenceValue);
		editor.commit();
	}
	
	public static String readSetting(Context context,String preferenceName) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME_SETTINGS, Context.MODE_APPEND);
		
		String statusCountDefaultValue = Constants.settings_default.STATUS_COUNT+"";
		String picModeDefaultValue = Constants.settings_default.PIC_MODE?"true":"false";
		String defaultValue = preferenceName.equals(SETTINGS_STATUS_COUNT)?statusCountDefaultValue:picModeDefaultValue;
		
		return pref.getString(preferenceName, defaultValue);
	}
	
	public static void saveProperty(Context context,String preferenceName,String preferenceValue) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME_USER_INFOS, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putString(preferenceName, preferenceValue);
		editor.commit();
	}
	
	public static String readProperty(Context context,String preferenceName) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME_USER_INFOS, Context.MODE_APPEND);
		return pref.getString(preferenceName, "{myself}");
	}
	
}
